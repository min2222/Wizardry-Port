package electroblob.wizardry.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;

public class Flamecatcher extends SpellConjuration {

	public static final String SHOT_COUNT = "shot_count";
	public static final String SHOTS_REMAINING_NBT_KEY = "shotsRemaining";

	public Flamecatcher(){
		super("flamecatcher", WizardryItems.flamecatcher);
		addProperties(RANGE, SHOT_COUNT, DAMAGE, BURN_DURATION);
	}

	@Override
	protected void addItemExtras(Player caster, ItemStack stack, SpellModifiers modifiers){
		if(stack.getTag() == null) stack.setTag(new CompoundTag());
		stack.getTag().putInt(SHOTS_REMAINING_NBT_KEY, (int)(getProperty(SHOT_COUNT).intValue() * modifiers.get(SpellModifiers.POTENCY)));
	}

	@Override
	protected void spawnParticles(Level world, LivingEntity caster, SpellModifiers modifiers){

		ParticleBuilder.create(Type.BUFF).entity(caster).clr(0xff6d00).spawn(world);

		for(int i=0; i<10; i++){
			double x = caster.getX() + world.random.nextDouble() * 2 - 1;
			double y = caster.getY() + caster.getEyeHeight() - 0.5 + world.random.nextDouble();
			double z = caster.getZ() + world.random.nextDouble() * 2 - 1;
			world.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
		}
	}

}
