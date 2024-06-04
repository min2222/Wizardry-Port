package electroblob.wizardry.spell;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.block.IGrowable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.ItemDye;
import net.minecraft.world.level.Level;

import java.util.List;

public class GrowthAura extends Spell {

	private static final int FULL_GROWTH_TIMEOUT = 100;

	public GrowthAura(){
		super("growth_aura", SpellActions.POINT_DOWN, false);
		addProperties(EFFECT_RADIUS);
		soundValues(0.7f, 1.2f, 0.2f);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		boolean flag = false;

		List<BlockPos> sphere = BlockUtils.getBlockSphere(caster.getPosition(),
				getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade));

		for(BlockPos pos : sphere){

			BlockState state = world.getBlockState(pos);

			if(state.getBlock() instanceof IGrowable){

				IGrowable plant = (IGrowable)state.getBlock();

				if(plant.canGrow(world, pos, state, world.isRemote)){

					if(!world.isRemote){
						if(plant.canUseBonemeal(world, world.rand, pos, state)){
							if(world.rand.nextFloat() < 0.35f && ItemArtefact.isArtefactActive(caster, WizardryItems.charm_growth)){
								int i = 0;
								while(plant.canGrow(world, pos, state, false) && i++ < FULL_GROWTH_TIMEOUT){
									plant.grow(world, world.rand, pos, state);
									state = world.getBlockState(pos); // Update the state with the new one
									plant = (IGrowable)state.getBlock(); // Update the block with the new one
								}
							}else{
								plant.grow(world, world.rand, pos, state);
							}
						}
					}else{
						// Yes, it's meant to be 0, and it automatically changes it to 15.
						ItemDye.spawnBonemealParticles(world, pos, 0);
					}

					flag = true;
				}
			}
		}

		if(flag) this.playSound(world, caster, ticksInUse, -1, modifiers);

		return flag;
	}

}
