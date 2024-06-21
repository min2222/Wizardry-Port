package electroblob.wizardry.entity.construct;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.RadiantTotem;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.BlockUtils.SurfaceCriteria;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityRadiantTotem extends EntityScaledConstruct {

	private static final int PERIMETER_PARTICLE_DENSITY = 6;

	public EntityRadiantTotem(Level world){
		this(WizardryEntities.RADIANT_TOTEM.get(), world);
		this.setSize(1, 1); // This entity is different in that its area of effect is kind of 'outside' it
	}
	
	public EntityRadiantTotem(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
		this.setSize(1, 1); // This entity is different in that its area of effect is kind of 'outside' it
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
			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_RADIANT_TOTEM_AMBIENT, WizardrySounds.SPELLS, 1, 1, true);
		}

		super.tick();

		double radius = Spells.RADIANT_TOTEM.getProperty(Spell.EFFECT_RADIUS).floatValue() * sizeMultiplier;

		if(level.isClientSide){

			ParticleBuilder.create(Type.DUST, random, getX(), getY() + 0.2, getZ(), 0.3, false)
					.vel(0, -0.02 - level.random.nextFloat() * 0.01, 0).clr(0xffffff).fade(0xffec90).spawn(level);

			for(int i=0; i<PERIMETER_PARTICLE_DENSITY; i++){

				float angle = ((float)Math.PI * 2)/PERIMETER_PARTICLE_DENSITY * (i + random.nextFloat());

				double x = getX() + radius * Mth.sin(angle);
				double z = getZ() + radius * Mth.cos(angle);

				Integer y = BlockUtils.getNearestSurface(level, new BlockPos(x, getY(), z), Direction.UP, 5, true, SurfaceCriteria.COLLIDABLE);

				if(y != null){
					ParticleBuilder.create(Type.DUST).pos(x, y, z).vel(0, 0.01, 0).clr(0xffffff).fade(0xffec90).spawn(level);
				}
			}
		}

		List<LivingEntity> nearby = EntityUtils.getLivingWithinRadius(radius, getX(), getY(), getZ(), level);
		nearby.sort(Comparator.comparingDouble(e -> e.distanceToSqr(this)));

		List<LivingEntity> nearbyAllies = nearby.stream().filter(e -> e == getCaster()
				|| AllyDesignationSystem.isAllied(getCaster(), e)).collect(Collectors.toList());
		nearby.removeAll(nearbyAllies);

		int targetsRemaining = Spells.RADIANT_TOTEM.getProperty(RadiantTotem.MAX_TARGETS).intValue()
				+ (int)((damageMultiplier - 1) / Constants.POTENCY_INCREASE_PER_TIER);

		while(!nearbyAllies.isEmpty() && targetsRemaining > 0){

			LivingEntity ally = nearbyAllies.remove(0);

			if(ally.getHealth() < ally.getMaxHealth()){
				// Slightly slower than healing aura, and it only does 1 at a time (without potency modifiers)
				if(ally.tickCount % 8 == 0) ally.heal(Spells.RADIANT_TOTEM.getProperty(Spell.HEALTH).floatValue());
				targetsRemaining--;

				if(level.isClientSide){
					ParticleBuilder.create(Type.BEAM).pos(this.position().add(0, getBbHeight()/2, 0))
							.target(ally).clr(1, 0.6f + 0.3f * level.random.nextFloat(), 0.2f).spawn(level);
				}
			}
		}

		while(!nearby.isEmpty() && targetsRemaining > 0){

			LivingEntity target = nearby.remove(0);

			if(EntityUtils.isLiving(target) && isValidTarget(target)){

				if(target.tickCount % target.invulnerableDuration == 1){

					float damage = Spells.RADIANT_TOTEM.getProperty(Spell.DAMAGE).floatValue();

					EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeIndirectMagicDamage(this,
							getCaster(), DamageType.RADIANT), damage);
				}

				targetsRemaining--;

				if(level.isClientSide){
					ParticleBuilder.create(Type.BEAM).pos(this.position().add(0, getBbHeight()/2, 0))
							.target(target).clr(1, 0.6f + 0.3f * level.random.nextFloat(), 0.2f).spawn(level);
				}
			}
		}
	}

	@Override
	public void despawn(){
		this.playSound(WizardrySounds.ENTITY_RADIANT_TOTEM_VANISH, 1, 1);
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
