package net.alonzurro.rotd.entity.custom;

import net.alonzurro.rotd.entity.ModEntities;
import net.alonzurro.rotd.item.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SlugProjectileEntity extends ThrowableItemProjectile {

    public SlugProjectileEntity(EntityType<? extends SlugProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public SlugProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SLUG_PROJECTILE.get(), shooter, level);
    }

    public SlugProjectileEntity(Level level, double x, double y, double z) {
        super(ModEntities.SLUG_PROJECTILE.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SLUG_ITEM.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.1f);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            SlugEntity slug = ModEntities.SLUG.get().create(this.level());
            if (slug != null) {
                slug.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0f);
                if (this.getOwner() instanceof Player player) {
                    slug.setOwnerUUID(player.getUUID());
                }
                this.level().addFreshEntity(slug);
            }
            this.discard();
        }
    }
}
