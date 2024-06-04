package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class Telekinesis extends SpellRay {

	public Telekinesis(){
		super("telekinesis", SpellActions.POINT, false);
		this.aimAssist(0.4f); // Helps with aiming at items
	}

	@Override public boolean requiresPacket(){ return false; }

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(target instanceof EntityItem){

			target.motionX = (origin.x - target.posX) / 6;
			target.motionY = (origin.y - target.posY) / 6;
			target.motionZ = (origin.z - target.posZ) / 6;
			return true;

		} else if (target instanceof Player && (Wizardry.settings.telekineticDisarmament && !ItemArtefact.isArtefactActive((Player) target, WizardryItems.amulet_anchoring))) {

			Player player = (Player) target;

			// IDEA: Disarm the offhand if the mainhand is empty or otherwise harmless?

			if (!player.getHeldItemMainhand().isEmpty()) {

				if (!world.isRemote) {
					EntityItem item = player.entityDropItem(player.getHeldItemMainhand(), 0);
					// Makes the item move towards the caster
					item.motionX = (origin.x - player.posX) / 20;
					item.motionZ = (origin.z - player.posZ) / 20;
				}

				player.setHeldItem(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

				return true;
			}
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(caster instanceof Player){
			
			BlockState blockstate = world.getBlockState(pos);
	
			if(blockstate.getBlock().onBlockActivated(world, pos, blockstate, (Player)caster, InteractionHand.MAIN_HAND,
					side, 0, 0, 0)){
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
