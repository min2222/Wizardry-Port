package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class Telekinesis extends SpellRay {

	public Telekinesis(){
		super("telekinesis", SpellActions.POINT, false);
		this.aimAssist(0.4f); // Helps with aiming at items
	}

	@Override public boolean requiresPacket(){ return false; }

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(target instanceof ItemEntity){
			target.setDeltaMovement((origin.x - target.getX()) / 6, (origin.y - target.getY()) / 6, (origin.z - target.getZ()) / 6);
			return true;

		} else if (target instanceof Player && (Wizardry.settings.telekineticDisarmament && !ItemArtefact.isArtefactActive((Player) target, WizardryItems.AMULET_ANCHORING.get()))) {

			Player player = (Player) target;

			// IDEA: Disarm the offhand if the mainhand is empty or otherwise harmless?

			if (!player.getMainHandItem().isEmpty()) {

				if (!world.isClientSide) {
					ItemEntity item = player.spawnAtLocation(player.getMainHandItem(), 0);
					// Makes the item move towards the caster
					item.setDeltaMovement((origin.x - player.getX()) / 20, item.getDeltaMovement().y, (origin.z - player.getZ()) / 20);
				}

				player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

				return true;
			}
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(caster instanceof Player){
			
			BlockState blockstate = world.getBlockState(pos);
	
			if(blockstate.use(world, (Player)caster, InteractionHand.MAIN_HAND,
					new BlockHitResult(origin, side, pos, false)) == InteractionResult.SUCCESS){
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
