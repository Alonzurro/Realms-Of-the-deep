package net.alonzurro.rotd.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class SlugEntity extends Monster {

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(SlugEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public final AnimationState idleAnimationState = new AnimationState();

    public SlugEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 1;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, Optional.empty());
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public boolean isOwned() {
        return this.getOwnerUUID() != null;
    }

    @Nullable
    public Player getOwner() {
        UUID uuid = this.getOwnerUUID();
        if (uuid == null) return null;
        return this.level().getPlayerByUUID(uuid);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID uuid = this.getOwnerUUID();
        if (uuid != null) {
            tag.putUUID("OwnerUUID", uuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("OwnerUUID")) {
            this.setOwnerUUID(tag.getUUID("OwnerUUID"));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.animateWhen(true, this.tickCount);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new SlugFollowOwnerGoal(this, 1.0, 4.0f, 12.0f));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new SlugOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new SlugOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                LivingEntity attacker = SlugEntity.this.getLastHurtByMob();
                if (attacker != null && attacker == SlugEntity.this.getOwner()) return false;
                return super.canUse();
            }
        });
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return !SlugEntity.this.isOwned() && super.canUse();
            }
        });
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isOwned();
    }

    @Override
    public boolean isPreventingPlayerRest(Player player) {
        return !this.isOwned();
    }

    @Override
    public void die(DamageSource damageSource) {
        if (this.isOwned()) {
            this.xpReward = 0;
        }
        super.die(damageSource);
        if (!this.level().isClientSide() && this.isOwned()) {
            Player owner = this.getOwner();
            if (owner instanceof ServerPlayer serverPlayer
                    && this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)) {
                serverPlayer.sendSystemMessage(this.getCombatTracker().getDeathMessage());
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isOwned() && source.getEntity() instanceof IronGolem golem) {
            if (!this.level().isClientSide()) golem.setTarget(null);
            return false;
        }
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide()) {
            LivingEntity attacker = source.getEntity() instanceof LivingEntity le ? le : null;
            if (attacker != null && !(attacker instanceof Player p && p.getUUID().equals(this.getOwnerUUID()))) {
                AABB range = this.getBoundingBox().inflate(16.0, 10.0, 16.0);
                for (SlugEntity nearby : this.level().getEntitiesOfClass(SlugEntity.class, range)) {
                    if (nearby != this && !nearby.isOwned() && nearby.getTarget() == null) {
                        nearby.setTarget(attacker);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected int decreaseAirSupply(int currentAir) {
        return currentAir;
    }

    @Override
    public float maxUpStep() {
        return 1.0f;
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> pFluidTag) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, this.getJumpPower(), 0.0));
    }

    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    // -------------------------------------------------------------------------
    // Inner goal: follow the owner
    // -------------------------------------------------------------------------
    private static class SlugFollowOwnerGoal extends Goal {
        private final SlugEntity slug;
        private final double speedModifier;
        private final float stopDistance;
        private final float teleportDistance;
        private Player owner;
        private int timeToRecalcPath;

        SlugFollowOwnerGoal(SlugEntity slug, double speed, float stopDist, float teleportDist) {
            this.slug = slug;
            this.speedModifier = speed;
            this.stopDistance = stopDist;
            this.teleportDistance = teleportDist;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Player owner = slug.getOwner();
            if (owner == null) return false;
            this.owner = owner;
            double distSq = slug.distanceToSqr(owner);
            return distSq > (double)(stopDistance * stopDistance);
        }

        @Override
        public boolean canContinueToUse() {
            if (!slug.isOwned()) return false;
            Player owner = slug.getOwner();
            if (owner == null) return false;
            this.owner = owner;
            return slug.distanceToSqr(owner) > (double)(stopDistance * stopDistance);
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
        }

        @Override
        public void stop() {
            this.owner = null;
            slug.getNavigation().stop();
        }

        @Override
        public void tick() {
            slug.getLookControl().setLookAt(owner, 10.0f, slug.getMaxHeadXRot());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                double distSq = slug.distanceToSqr(owner);
                if (distSq > (double)(teleportDistance * teleportDistance)) {
                    teleportToOwner();
                } else {
                    slug.getNavigation().moveTo(owner, speedModifier);
                }
            }
        }

        private void teleportToOwner() {
            Vec3 ownerPos = owner.position();
            slug.moveTo(ownerPos.x, ownerPos.y, ownerPos.z, slug.getYRot(), slug.getXRot());
            slug.getNavigation().stop();
        }
    }

    // -------------------------------------------------------------------------
    // Inner goal: attack what hurt the owner
    // -------------------------------------------------------------------------
    private static class SlugOwnerHurtByTargetGoal extends TargetGoal {
        private final SlugEntity slug;
        private LivingEntity ownerLastHurtBy;
        private int timestamp;

        SlugOwnerHurtByTargetGoal(SlugEntity slug) {
            super(slug, false);
            this.slug = slug;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!slug.isOwned()) return false;
            Player owner = slug.getOwner();
            if (owner == null) return false;
            this.ownerLastHurtBy = owner.getLastHurtByMob();
            int lastHurtTime = owner.getLastHurtByMobTimestamp();
            return lastHurtTime != this.timestamp
                    && this.ownerLastHurtBy != null
                    && this.ownerLastHurtBy != slug
                    && canAttack(this.ownerLastHurtBy, TargetingConditions.forCombat());
        }

        @Override
        public void start() {
            slug.setTarget(this.ownerLastHurtBy);
            Player owner = slug.getOwner();
            if (owner != null) {
                this.timestamp = owner.getLastHurtByMobTimestamp();
            }
            super.start();
        }
    }

    // -------------------------------------------------------------------------
    // Inner goal: attack what the owner attacked
    // -------------------------------------------------------------------------
    private static class SlugOwnerHurtTargetGoal extends TargetGoal {
        private final SlugEntity slug;
        private LivingEntity ownerLastHurt;
        private int timestamp;

        SlugOwnerHurtTargetGoal(SlugEntity slug) {
            super(slug, false);
            this.slug = slug;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!slug.isOwned()) return false;
            Player owner = slug.getOwner();
            if (owner == null) return false;
            this.ownerLastHurt = owner.getLastHurtMob();
            int lastHurtTime = owner.getLastHurtMobTimestamp();
            return lastHurtTime != this.timestamp
                    && this.ownerLastHurt != null
                    && this.ownerLastHurt != slug
                    && canAttack(this.ownerLastHurt, TargetingConditions.forCombat());
        }

        @Override
        public void start() {
            slug.setTarget(this.ownerLastHurt);
            Player owner = slug.getOwner();
            if (owner != null) {
                this.timestamp = owner.getLastHurtMobTimestamp();
            }
            super.start();
        }
    }
}
