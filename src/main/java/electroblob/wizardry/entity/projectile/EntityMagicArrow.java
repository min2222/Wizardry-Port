package electroblob.wizardry.entity.projectile;

import java.lang.ref.WeakReference;
import java.util.UUID;

import javax.annotation.Nullable;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.RayTracer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

public abstract class EntityMagicArrow extends Projectile implements IEntityAdditionalSpawnData {
    public static final double LAUNCH_Y_OFFSET = 0.1;
    public static final int SEEKING_TIME = 15;

    private BlockState stuckInBlock;

    private int inData;
    private boolean inGround;

    public int arrowShake;

    private WeakReference<LivingEntity> caster;

    private UUID casterUUID;
    int ticksInGround;
    int ticksInAir;
    private int knockbackStrength;
    public float damageMultiplier = 1.0f;

    public EntityMagicArrow(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
    }

    public void aim(LivingEntity caster, float speed) {
        this.setCaster(caster);

        this.moveTo(caster.getX(), caster.getY() + (double) caster.getEyeHeight() - LAUNCH_Y_OFFSET, caster.getZ(), caster.getYRot(), caster.getXRot());

        this.setPos(this.getX() - (double) (Mth.cos(this.getYRot() / 180.0F * (float) Math.PI) * 0.16F), this.getY() - 0.10000000149011612D, this.getZ() - (double) (Mth.sin(this.getYRot() / 180.0F * (float) Math.PI) * 0.16F));

        this.setPos(this.position());

        this.setDeltaMovement((double) (-Mth.sin(this.getYRot() / 180.0F * (float) Math.PI) * Mth.cos(this.getXRot() / 180.0F * (float) Math.PI)), (double) (-Mth.sin(this.getXRot() / 180.0F * (float) Math.PI)), (double) (Mth.cos(this.getYRot() / 180.0F * (float) Math.PI) * Mth.cos(this.getXRot() / 180.0F * (float) Math.PI)));

        this.shoot(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z, speed * 1.5F, 1.0F);
    }

    public void aim(LivingEntity caster, Entity target, float speed, float aimingError) {
        this.setCaster(caster);

        this.setPos(this.getX(), caster.getY() + (double) caster.getEyeHeight() - LAUNCH_Y_OFFSET, this.getZ());
        double dx = target.getX() - caster.getX();
        double dy = this.doGravity() ? target.getY() + (double) (target.getBbHeight() / 3.0f) - this.getY() : target.getY() + (double) (target.getBbHeight() / 2.0f) - this.getY();
        double dz = target.getZ() - caster.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        if (horizontalDistance >= 1.0E-7D) {
            float yaw = (float) (Math.atan2(dz, dx) * 180.0d / Math.PI) - 90.0f;
            float pitch = (float) (-(Math.atan2(dy, horizontalDistance) * 180.0d / Math.PI));
            double dxNormalised = dx / horizontalDistance;
            double dzNormalised = dz / horizontalDistance;
            this.moveTo(caster.getX() + dxNormalised, this.getY(), caster.getZ() + dzNormalised, yaw, pitch);
            float bulletDropCompensation = this.doGravity() ? (float) horizontalDistance * 0.2f : 0;
            this.shoot(dx, dy + (double) bulletDropCompensation, dz, speed, aimingError);
        }
    }

    public abstract double getDamage();

    public abstract int getLifetime();

    public DamageType getDamageType() {
        return DamageType.MAGIC;
    }

    public boolean doGravity() {
        return true;
    }

    public boolean doDeceleration() {
        return true;
    }

    public boolean doOverpenetration() {
        return false;
    }

    public float getSeekingStrength() {
        return getCaster() instanceof Player && ItemArtefact.isArtefactActive((Player) getCaster(), WizardryItems.RING_SEEKING.get()) ? 2 : 0;
    }

    public void setKnockbackStrength(int knockback) {
        this.knockbackStrength = knockback;
    }

    public LivingEntity getCaster() {
        return caster == null ? null : caster.get();
    }

    public void setCaster(LivingEntity entity) {
        caster = new WeakReference<>(entity);
    }

    protected void tickInGround() {
        this.discard();
    }

    protected void tickInAir() {
    }

    protected void onEntityHit(LivingEntity entityHit) {
    }

    protected void onBlockHit(BlockHitResult hit) {
    }

    @Override
    protected void onHitEntity(EntityHitResult p_36757_) {
        Entity entity = p_36757_.getEntity();
        DamageSource damagesource = null;

        if (this.getCaster() == null) {
            damagesource = DamageSource.thrown(this, this);
        } else {
            damagesource = MagicDamage.causeIndirectMagicDamage(this, this.getCaster(), this.getDamageType()).setProjectile();
        }

        if (entity.hurt(damagesource, (float) (this.getDamage() * this.damageMultiplier))) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity) entity;
                this.onEntityHit(livingentity);
                if (this.knockbackStrength > 0) {
                    double d0 = Math.max(0.0D, 1.0D - livingentity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                    Vec3 vec3 = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double) this.knockbackStrength * 0.6D * d0);
                    if (vec3.lengthSqr() > 0.0D) {
                        livingentity.push(vec3.x, 0.1D, vec3.z);
                    }
                }

                if (!this.level.isClientSide && this.getCaster() instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity, this.getCaster());
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) this.getCaster(), livingentity);
                }

                if (this.getCaster() != null && livingentity != this.getCaster() && livingentity instanceof Player && this.getCaster() instanceof ServerPlayer) {
                    ((ServerPlayer) this.getCaster()).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!(livingentity instanceof EnderMan) && !this.doOverpenetration()) {
                    this.discard();
                }
            }
        } else {
            if (!this.doOverpenetration()) this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult p_37258_) {
        this.stuckInBlock = this.level.getBlockState(p_37258_.getBlockPos());
        super.onHitBlock(p_37258_);
        Vec3 vec3 = p_37258_.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        this.inGround = true;
        this.arrowShake = 7;
        this.onBlockHit(p_37258_);
    }

    @Override
    public void tick() {
        super.tick();

        if (getLifetime() >= 0 && this.tickCount > getLifetime()) {
            this.discard();
        }

        if (this.getCaster() == null && this.casterUUID != null) {
            Entity entity = EntityUtils.getEntityByUUID(level, casterUUID);
            if (entity instanceof LivingEntity) {
                this.caster = new WeakReference<>((LivingEntity) entity);
            }
        }

        Vec3 vec3 = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double d0 = vec3.horizontalDistance();
            this.setYRot((float) (Mth.atan2(vec3.x, vec3.z) * (double) (180F / (float) Math.PI)));
            this.setXRot((float) (Mth.atan2(vec3.y, d0) * (double) (180F / (float) Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level.getBlockState(blockpos);
        if (!blockstate.isAir()) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level, blockpos);
            if (!voxelshape.isEmpty()) {
                Vec3 vec31 = this.position();

                for (AABB aabb : voxelshape.toAabbs()) {
                    if (aabb.move(blockpos).contains(vec31)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.arrowShake > 0) {
            --this.arrowShake;
        }

        if (this.inGround) {
            ++this.ticksInGround;
            this.tickInGround();
        } else {
            this.tickInAir();

            this.ticksInGround = 0;
            ++this.ticksInAir;

            Vec3 vec32 = this.position();
            Vec3 vec33 = vec32.add(vec3);
            HitResult hitresult = this.level.clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (hitresult.getType() != HitResult.Type.MISS) {
                vec33 = hitresult.getLocation();
            }

            while (!this.isRemoved()) {
                EntityHitResult entityhitresult = this.findHitEntity(vec32, vec33);
                if (entityhitresult != null) {
                    hitresult = entityhitresult;
                }

                if (hitresult != null && hitresult.getType() == HitResult.Type.ENTITY) {
                    Entity entity = ((EntityHitResult) hitresult).getEntity();
                    Entity entity1 = this.getCaster();
                    if (entity instanceof Player && entity1 instanceof Player && !((Player) entity1).canHarmPlayer((Player) entity)) {
                        hitresult = null;
                        entityhitresult = null;
                    }
                }

                if (hitresult != null && hitresult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
                    this.onHit(hitresult);
                    this.hasImpulse = true;
                }

                if (entityhitresult == null || !this.doOverpenetration()) {
                    break;
                }

                hitresult = null;
            }

            if (getSeekingStrength() > 0) {
                Vec3 velocity = this.getDeltaMovement();

                HitResult hit = RayTracer.rayTrace(level, this.position(), this.position().add(velocity.scale(SEEKING_TIME)), getSeekingStrength(), false, true, false, LivingEntity.class, RayTracer.ignoreEntityFilter(null));

                if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult entityHit = (EntityHitResult) hit;
                    if (AllyDesignationSystem.isValidTarget(getCaster(), entityHit.getEntity())) {
                        Vec3 direction = new Vec3(entityHit.getEntity().getX(), entityHit.getEntity().getY() + entityHit.getEntity().getBbHeight() / 2, entityHit.getEntity().getZ()).subtract(this.position()).normalize().scale(velocity.length());

                        double motionX = this.getDeltaMovement().x + 2 * (direction.x - this.getDeltaMovement().x) / SEEKING_TIME;
                        double motionY = this.getDeltaMovement().y + 2 * (direction.y - this.getDeltaMovement().y) / SEEKING_TIME;
                        double motionZ = this.getDeltaMovement().z + 2 * (direction.z - this.getDeltaMovement().z) / SEEKING_TIME;
                        this.setDeltaMovement(motionX, motionY, motionZ);
                    }
                }
            }

            vec3 = this.getDeltaMovement();
            double d5 = vec3.x;
            double d6 = vec3.y;
            double d1 = vec3.z;
            double d7 = this.getX() + d5;
            double d2 = this.getY() + d6;
            double d3 = this.getZ() + d1;
            double d4 = vec3.horizontalDistance();

            this.setYRot((float) (Mth.atan2(d5, d1) * (double) (180F / (float) Math.PI)));

            this.setXRot((float) (Mth.atan2(d6, d4) * (double) (180F / (float) Math.PI)));
            this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
            this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
            float f = 0.99F;

            if (this.isInWater()) {
                for (int j = 0; j < 4; ++j) {
                    this.level.addParticle(ParticleTypes.BUBBLE, d7 - d5 * 0.25D, d2 - d6 * 0.25D, d3 - d1 * 0.25D, d5, d6, d1);
                }

                f = 0.8f;
            }

            if (this.wasTouchingWater) {
                this.clearFire();
            }

            if (this.doDeceleration()) {
                this.setDeltaMovement(vec3.scale((double) f));
            }

            if (this.doGravity()) {
                Vec3 vec34 = this.getDeltaMovement();
                this.setDeltaMovement(vec34.x, vec34.y - (double) 0.05F, vec34.z);
            }

            this.setPos(d7, d2, d3);
            this.checkInsideBlocks();
        }
    }

    @Nullable
    protected EntityHitResult findHitEntity(Vec3 p_36758_, Vec3 p_36759_) {
        return ProjectileUtil.getEntityHitResult(this.level, this, p_36758_, p_36759_, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        if (this.getCaster() != null) buffer.writeInt(this.getCaster().getId());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        if (buffer.isReadable())
            this.caster = new WeakReference<>((LivingEntity) this.level.getEntity(buffer.readInt()));
    }

    @Override
    public void shoot(double p_36775_, double p_36776_, double p_36777_, float p_36778_, float p_36779_) {
        super.shoot(p_36775_, p_36776_, p_36777_, p_36778_, p_36779_);
        this.ticksInGround = 0;
    }

    @Override
    public void lerpTo(double p_36728_, double p_36729_, double p_36730_, float p_36731_, float p_36732_, int p_36733_, boolean p_36734_) {
        this.setPos(p_36728_, p_36729_, p_36730_);
        this.setRot(p_36731_, p_36732_);
    }

    @Override
    public void lerpMotion(double p_36786_, double p_36787_, double p_36788_) {
        super.lerpMotion(p_36786_, p_36787_, p_36788_);
        this.ticksInGround = 0;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("life", (short) this.ticksInGround);
        if (this.stuckInBlock != null) {
            tag.put("inBlockState", NbtUtils.writeBlockState(this.stuckInBlock));
        }
        tag.putByte("inData", (byte) this.inData);
        tag.putByte("shake", (byte) this.arrowShake);
        tag.putByte("inGround", (byte) (this.inGround ? 1 : 0));
        tag.putFloat("damageMultiplier", this.damageMultiplier);
        if (this.getCaster() != null) {
            tag.putUUID("casterUUID", this.getCaster().getUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("inBlockState", 10)) {
            this.stuckInBlock = NbtUtils.readBlockState(tag.getCompound("inBlockState"));
        }
        this.ticksInGround = tag.getShort("life");
        this.inData = tag.getByte("inData") & 255;
        this.arrowShake = tag.getByte("shake") & 255;
        this.inGround = tag.getByte("inGround") == 1;
        this.damageMultiplier = tag.getFloat("damageMultiplier");
        casterUUID = tag.getUUID("casterUUID");
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.PLAYERS;
    }

    @Override
    protected void defineSynchedData() {

    }
}
