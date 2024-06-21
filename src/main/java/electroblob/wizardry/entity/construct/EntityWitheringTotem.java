package electroblob.wizardry.entity.construct;

import java.util.Comparator;
import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.WitheringTotem;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.BlockUtils.SurfaceCriteria;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityWitheringTotem extends EntityScaledConstruct {

	private static final int PERIMETER_PARTICLE_DENSITY = 6;

	private static final EntityDataAccessor<Float> HEALTH_DRAINED = SynchedEntityData.defineId(EntityWitheringTotem.class, EntityDataSerializers.FLOAT);

	public EntityWitheringTotem(Level world){
		this(WizardryEntities.WITHERING_TOTEM.get(), world);
		this.setSize(1, 1); // This entity is different in that its area of effect is kind of 'outside' it
	}
	
	public EntityWitheringTotem(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
		this.setSize(1, 1); // This entity is different in that its area of effect is kind of 'outside' it
	}

	@Override
	protected void defineSynchedData(){
		entityData.define(HEALTH_DRAINED, 0f);
	}

	public float getHealthDrained(){
		return entityData.get(HEALTH_DRAINED);
	}

	public void addHealthDrained(float health){
		entityData.set(HEALTH_DRAINED, getHealthDrained() + health);
	}

	@Override
	protected boolean shouldScaleWidth(){
		return false;
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	@Override
	public void tick(){

		if(level.isClientSide && this.tickCount == 1){
			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_WITHERING_TOTEM_AMBIENT, WizardrySounds.SPELLS, 1, 1, true);
		}

		super.tick();

		double radius = Spells.WITHERING_TOTEM.getProperty(Spell.EFFECT_RADIUS).floatValue() * sizeMultiplier;

		if(level.isClientSide){

			ParticleBuilder.create(Type.DUST, random, getX(), getY() + 0.2, getZ(), 0.3, false)
					.vel(0, -0.02 - level.random.nextFloat() * 0.01, 0).clr(0xf575f5).fade(0x382366).spawn(level);

			for(int i=0; i<PERIMETER_PARTICLE_DENSITY; i++){

				float angle = ((float)Math.PI * 2)/PERIMETER_PARTICLE_DENSITY * (i + random.nextFloat());

				double x = getX() + radius * Mth.sin(angle);
				double z = getZ() + radius * Mth.cos(angle);

				Integer y = BlockUtils.getNearestSurface(level, new BlockPos(x, getY(), z), Direction.UP, 5, true, SurfaceCriteria.COLLIDABLE);

				if(y != null){
					ParticleBuilder.create(Type.DUST).pos(x, y, z).vel(0, 0.01, 0).clr(0xf575f5).fade(0x382366).spawn(level);
				}
			}
		}

		List<LivingEntity> nearby = EntityUtils.getLivingWithinRadius(radius, getX(), getY(), getZ(), level);
		nearby.removeIf(e -> !isValidTarget(e));
		nearby.sort(Comparator.comparingDouble(e -> e.distanceToSqr(this)));

		int targetsRemaining = Spells.WITHERING_TOTEM.getProperty(WitheringTotem.MAX_TARGETS).intValue()
				+ (int)((damageMultiplier - 1) / Constants.POTENCY_INCREASE_PER_TIER);

		while(!nearby.isEmpty() && targetsRemaining > 0){

			LivingEntity target = nearby.remove(0);

			if(EntityUtils.isLiving(target)){

				if(target.tickCount % target.invulnerableDuration == 1){

					float damage = Spells.WITHERING_TOTEM.getProperty(Spell.DAMAGE).floatValue();

					if(EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeIndirectMagicDamage(this,
							getCaster(), DamageType.WITHER), damage)){
						addHealthDrained(damage);
					}
				}

				targetsRemaining--;

				if(level.isClientSide){

					Vec3 centre = GeometryUtils.getCentre(this);
					Vec3 pos = GeometryUtils.getCentre(target);

					ParticleBuilder.create(Type.BEAM).pos(centre).target(target)
							.clr(0.1f + 0.2f * level.random.nextFloat(), 0, 0.3f).spawn(level);

					for(int i = 0; i < 3; i++){
						ParticleBuilder.create(Type.DUST, random, pos.x, pos.y, pos.z, 0.3, false)
								.vel(pos.subtract(centre).normalize().scale(-0.1)).clr(0x0c0024).fade(0x610017).spawn(level);
					}
				}
			}
		}
	}

	@Override
	public void despawn(){

		double radius = Spells.WITHERING_TOTEM.getProperty(Spell.EFFECT_RADIUS).floatValue() * sizeMultiplier;

		List<LivingEntity> nearby = EntityUtils.getLivingWithinRadius(radius, getX(), getY(), getZ(), level);
		nearby.removeIf(e -> !isValidTarget(e));

		float damage = Math.min(getHealthDrained() * 0.2f, Spells.WITHERING_TOTEM.getProperty(WitheringTotem.MAX_EXPLOSION_DAMAGE).floatValue());

		for(LivingEntity target : nearby){

			if(EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeIndirectMagicDamage(this,
					getCaster(), DamageType.MAGIC), damage)){
				target.addEffect(new MobEffectInstance(MobEffects.WITHER, Spells.WITHERING_TOTEM.getProperty(Spell.EFFECT_DURATION).intValue(),
						Spells.WITHERING_TOTEM.getProperty(Spell.EFFECT_STRENGTH).intValue()));
			}
		}

		if(level.isClientSide) ParticleBuilder.create(Type.SPHERE).pos(GeometryUtils.getCentre(this)).scale((float)radius).clr(0xbe1a53)
				.fade(0x210f4a).spawn(level);

		this.playSound(WizardrySounds.ENTITY_WITHERING_TOTEM_EXPLODE, 1, 1);
		super.despawn();
	}

	// Usually damage multipliers don't need syncing, but here we're using it for the non-standard purpose of targeting

	@Override
	public void writeSpawnData(FriendlyByteBuf data){
		super.writeSpawnData(data);
		data.writeFloat(damageMultiplier);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf data){
		super.readSpawnData(data);
		damageMultiplier = data.readFloat();
	}
}
