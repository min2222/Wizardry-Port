package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.monster.EntitySnowman;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SummonSnowGolem extends Spell {

	public SummonSnowGolem(){
		super("summon_snow_golem", SpellActions.SUMMON, false);
		this.soundValues(1, 1, 0.4f);
		addProperties(SpellMinion.SUMMON_RADIUS);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		BlockPos pos = BlockUtils.findNearbyFloorSpace(caster, 2, 4);
		if(pos == null) return false;

		if(!level.isClientSide){
			
			EntitySnowman snowman = new EntitySnowman(world);
			snowman.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			world.spawnEntity(snowman);
			
		}else{
			
			for(int i=0; i<10; i++){
				double x = pos.getX() + world.random.nextDouble() * 2 - 1;
				double y = pos.getY() + 0.5 + world.random.nextDouble();
				double z = pos.getZ() + world.random.nextDouble() * 2 - 1;
				ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.1, 0).clr(0.6f, 0.6f, 1).spawn(world);
			}
		}
		
		playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@Override
	protected String getTranslationKey(){
		return Wizardry.tisTheSeason ? super.getTranslationKey() + "_festive" : super.getTranslationKey();
	}
}
