package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class SlowTime extends SpellBuff {

	/** A {@code ResourceLocation} representing the shader file used when possessing an entity. */
	public static final ResourceLocation SHADER = new ResourceLocation(Wizardry.MODID, "shaders/post/slow_time.json");

	public SlowTime(){
		super("slow_time", 0.2f, 0.8f, 0.8f, () -> WizardryPotions.slow_time);
		addProperties(EFFECT_RADIUS);
		soundValues(0.6f, 1.5f, 0);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.world.isClientSide){
			Wizardry.proxy.loadShader(caster, SHADER);
		}

		return super.cast(world, caster, hand, ticksInUse, modifiers);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean canBeCastBy(DispenserBlockEntity dispenser){
		return false;
	}

	@Override
	public boolean canBeCastBy(Mob npc, boolean override){
		return false;
	}
}
