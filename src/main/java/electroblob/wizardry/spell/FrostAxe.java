package electroblob.wizardry.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class FrostAxe extends SpellConjuration {

	public FrostAxe(){
		super("frost_axe", WizardryItems.frost_axe);
		addProperties(DAMAGE);
	}

	@Override
	protected void spawnParticles(Level world, LivingEntity caster, SpellModifiers modifiers){
		
		for(int i=0; i<10; i++){
			double x = caster.posX + world.rand.nextDouble() * 2 - 1;
			double y = caster.posY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
			double z = caster.posZ + world.rand.nextDouble() * 2 - 1;
			ParticleBuilder.create(Type.SNOW).pos(x, y, z).spawn(world);
		}
	}

}
