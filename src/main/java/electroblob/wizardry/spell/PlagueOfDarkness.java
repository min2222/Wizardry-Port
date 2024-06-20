package electroblob.wizardry.spell;

import javax.annotation.Nullable;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PlagueOfDarkness extends SpellAreaEffect {

	public PlagueOfDarkness(){
		super("plague_of_darkness", SpellActions.POINT_DOWN, false);
		this.alwaysSucceed(true);
		addProperties(DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
		soundValues(1, 1.1f, 0.2f);
	}

	@Override
	protected boolean affectEntity(Level world, Vec3 origin, @Nullable LivingEntity caster, LivingEntity target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		if(!MagicDamage.isEntityImmune(DamageType.WITHER, target)){
			target.hurt(MagicDamage.causeDirectMagicDamage(caster, DamageType.WITHER),
					getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
			target.addEffect(new MobEffectInstance(MobEffects.WITHER,
					(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.DURATION_UPGRADE.get())),
					getProperty(EFFECT_STRENGTH).intValue() + SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY))));
		}

		return true;
	}

	@Override
	protected void spawnParticleEffect(Level world, Vec3 origin, double radius, @Nullable LivingEntity caster, SpellModifiers modifiers){

		double particleX, particleZ;

		for(int i = 0; i < 40 * modifiers.get(WizardryItems.BLAST_UPGRADE.get()); i++){

			particleX = origin.x - 1.0d + 2 * world.random.nextDouble();
			particleZ = origin.z - 1.0d + 2 * world.random.nextDouble();
			ParticleBuilder.create(Type.DARK_MAGIC).pos(particleX, origin.y, particleZ)
					.vel(particleX - origin.x, 0, particleZ - origin.z).clr(0.1f, 0, 0).spawn(world);

			particleX = origin.x - 1.0d + 2 * world.random.nextDouble();
			particleZ = origin.z - 1.0d + 2 * world.random.nextDouble();
			ParticleBuilder.create(Type.SPARKLE).pos(particleX, origin.y, particleZ)
					.vel(particleX - origin.x, 0, particleZ - origin.z).time(30).clr(0.1f, 0, 0.05f).spawn(world);

			particleX = origin.x - 1.0d + 2 * world.random.nextDouble();
			particleZ = origin.z - 1.0d + 2 * world.random.nextDouble();

			BlockState block = world.getBlockState(new BlockPos(origin.x, origin.y - 0.5, origin.z));

			if(block != null){
				world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, block), particleX, origin.y,
						particleZ, particleX - origin.x, 0, particleZ - origin.z);
			}
		}

		ParticleBuilder.create(Type.SPHERE).pos(origin.add(0, 0.1, 0)).scale((float)radius * 0.8f).clr(0.8f, 0, 0.05f).spawn(world);
	}

}
