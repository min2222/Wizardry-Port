package electroblob.wizardry.spell;

import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Satiety extends SpellBuff {

	public static final String HUNGER_POINTS = "hunger_points";
	public static final String SATURATION_MODIFIER = "saturation_modifier";

	public Satiety(){
		super("satiety", 1, 0.7f, 0.3f);
		this.soundValues(0.7f, 1.2f, 0.4f);
		addProperties(HUNGER_POINTS, SATURATION_MODIFIER);
	}
	
	@Override public boolean canBeCastBy(Mob npc, boolean override){ return false; }
	
	@Override
	protected boolean applyEffects(LivingEntity caster, SpellModifiers modifiers){
		return true; // In this case the best solution is to remove the functionality of this method and override cast.
	}
	
	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.getFoodData().needsFood()){
			int foodAmount = (int)(getProperty(HUNGER_POINTS).floatValue() * modifiers.get(SpellModifiers.POTENCY));
			// Fixed issue #6: Changed to addStats, since setFoodLevel is client-side only
			caster.getFoodData().eat(foodAmount, getProperty(SATURATION_MODIFIER).floatValue());
			return super.cast(world, caster, hand, ticksInUse, modifiers);
		}
		
		return false;
	}

}
