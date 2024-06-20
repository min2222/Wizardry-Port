package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

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

		List<BlockPos> sphere = BlockUtils.getBlockSphere(caster.blockPosition(),
				getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.BLAST_UPGRADE.get()));

		for(BlockPos pos : sphere){

			BlockState state = world.getBlockState(pos);

			if(state.getBlock() instanceof BonemealableBlock){

				BonemealableBlock plant = (BonemealableBlock)state.getBlock();

				if(plant.isValidBonemealTarget(world, pos, state, world.isClientSide)){

					if(!world.isClientSide){
						if(plant.isBonemealSuccess(world, world.random, pos, state)){
							if(world.random.nextFloat() < 0.35f && ItemArtefact.isArtefactActive(caster, WizardryItems.CHARM_GROWTH.get())){
								int i = 0;
								while(plant.isValidBonemealTarget(world, pos, state, false) && i++ < FULL_GROWTH_TIMEOUT){
									plant.performBonemeal((ServerLevel) world, world.random, pos, state);
									state = world.getBlockState(pos); // Update the state with the new one
									plant = (BonemealableBlock)state.getBlock(); // Update the block with the new one
								}
							}else{
								plant.performBonemeal((ServerLevel) world, world.random, pos, state);
							}
						}
					}else{
						// Yes, it's meant to be 0, and it automatically changes it to 15.
                        BoneMealItem.addGrowthParticles(world, pos, 0);
					}

					flag = true;
				}
			}
		}

		if(flag) this.playSound(world, caster, ticksInUse, -1, modifiers);

		return flag;
	}

}
