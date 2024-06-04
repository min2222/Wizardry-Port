package electroblob.wizardry.spell;

import electroblob.wizardry.entity.living.EntityWitherSkeletonMinion;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class SummonWitherSkeleton extends SpellMinion<EntityWitherSkeletonMinion> {

	public SummonWitherSkeleton(){
		super("summon_wither_skeleton", EntityWitherSkeletonMinion::new);
		this.soundValues(7, 0.6f, 0);
	}
	
	@Override
	protected void addMinionExtras(EntityWitherSkeletonMinion minion, BlockPos pos, LivingEntity caster, SpellModifiers modifiers, int alreadySpawned){
		minion.setItemStackToSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
		minion.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
	}

}
