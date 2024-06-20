package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Thunderstorm extends Spell {

	public static final String LIGHTNING_BOLTS = "lightning_bolts";

	public static final String SECONDARY_DAMAGE = "secondary_damage";
	public static final String TERTIARY_DAMAGE = "tertiary_damage";

	public static final String SECONDARY_RANGE = "secondary_range";
	public static final String TERTIARY_RANGE = "tertiary_range";

	public static final String SECONDARY_MAX_TARGETS = "secondary_max_targets";
	public static final String TERTIARY_MAX_TARGETS = "tertiary_max_targets"; // This is per secondary target

	private static final float CENTRE_RADIUS_FRACTION = 0.5f;

	public Thunderstorm(){
		super("thunderstorm", SpellActions.POINT_UP, false);
		this.soundValues(1, 1.7f, 0.2f);
		addProperties(EFFECT_RADIUS, LIGHTNING_BOLTS, SECONDARY_DAMAGE, TERTIARY_DAMAGE, SECONDARY_RANGE,
				TERTIARY_RANGE, SECONDARY_MAX_TARGETS, TERTIARY_MAX_TARGETS);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){
		return doCasting(world, caster, modifiers);
	}
	
	@Override
	public boolean cast(Level world, Mob caster, InteractionHand hand, int ticksInUse, LivingEntity target, SpellModifiers modifiers){
		return doCasting(world, caster, modifiers);
	}
	
	// This spell is exactly the same for players and NPCs.
	private boolean doCasting(Level world, LivingEntity caster, SpellModifiers modifiers){
		
		if(world.canSeeSky(caster.blockPosition())){

			double maxRadius = getProperty(EFFECT_RADIUS).doubleValue();

			for(int i = 0; i < getProperty(LIGHTNING_BOLTS).intValue(); i++){

				double radius = maxRadius * CENTRE_RADIUS_FRACTION + world.random.nextDouble() * maxRadius
						* (1 - CENTRE_RADIUS_FRACTION) * modifiers.get(WizardryItems.BLAST_UPGRADE.get());
				float angle = world.random.nextFloat() * (float)Math.PI * 2;

				double x = caster.getX() + radius * Mth.cos(angle);
				double z = caster.getZ() + radius * Mth.sin(angle);
				Integer y = BlockUtils.getNearestFloor(world, new BlockPos(x, caster.getY(), z), (int)maxRadius);

				if(y != null){

					if(!world.isClientSide){
						net.minecraft.world.entity.LightningBolt lightning = new net.minecraft.world.entity.LightningBolt(EntityType.LIGHTNING_BOLT, world);
						lightning.setPos(x, y, z);
						lightning.setVisualOnly(false);
						lightning.getPersistentData().putUUID(LightningBolt.SUMMONER_NBT_KEY, caster.getUUID());
						lightning.getPersistentData().putFloat(LightningBolt.DAMAGE_MODIFIER_NBT_KEY, modifiers.get(SpellModifiers.POTENCY));
						world.addFreshEntity(lightning);
					}

					// Secondary chaining effect
					List<LivingEntity> secondaryTargets = EntityUtils.getLivingWithinRadius(
							getProperty(SECONDARY_RANGE).doubleValue(), x, y + 1, z, world);

					for(int j = 0; j < Math.min(secondaryTargets.size(), getProperty(SECONDARY_MAX_TARGETS).intValue()); j++){

						LivingEntity secondaryTarget = secondaryTargets.get(j);

						if(AllyDesignationSystem.isValidTarget(caster, secondaryTarget)){

							if(world.isClientSide){

								ParticleBuilder.create(Type.LIGHTNING).pos(x, y, z).target(secondaryTarget).spawn(world);

								ParticleBuilder.spawnShockParticles(world, secondaryTarget.getX(),
										secondaryTarget.getY() + secondaryTarget.getBbHeight() / 2,
										secondaryTarget.getZ());
							}

							playSound(world, secondaryTarget, 0, -1, modifiers);

							secondaryTarget.hurt(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
									getProperty(SECONDARY_DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));

							// Tertiary chaining effect

							List<LivingEntity> tertiaryTargets = EntityUtils.getLivingWithinRadius(
									getProperty(TERTIARY_RANGE).doubleValue(), secondaryTarget.getX(),
									secondaryTarget.getY() + secondaryTarget.getBbHeight() / 2, secondaryTarget.getZ(), world);

							for(int k = 0; k < Math.min(tertiaryTargets.size(), getProperty(TERTIARY_MAX_TARGETS).intValue()); k++){

								LivingEntity tertiaryTarget = tertiaryTargets.get(k);

								if(!secondaryTargets.contains(tertiaryTarget)
										&& AllyDesignationSystem.isValidTarget(caster, tertiaryTarget)){

									if(world.isClientSide){
										ParticleBuilder.create(Type.LIGHTNING).entity(secondaryTarget)
												.pos(0, secondaryTarget.getBbHeight() / 2, 0).target(tertiaryTarget).spawn(world);
										ParticleBuilder.spawnShockParticles(world, tertiaryTarget.getX(),
												tertiaryTarget.getY() + tertiaryTarget.getBbHeight() / 2,
												tertiaryTarget.getZ());
									}

									playSound(world, tertiaryTarget, 0, -1, modifiers);

									tertiaryTarget.hurt(
											MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
											getProperty(TERTIARY_DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
								}
							}
						}
					}
				}
			}

			return true;
		}

		return false;
	}

}
