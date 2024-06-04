package electroblob.wizardry.spell;

import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import electroblob.wizardry.entity.living.EntityStrayMinion;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SummonSkeleton extends SpellMinion<EntitySkeletonMinion> {

	public SummonSkeleton(){
		super("summon_skeleton", EntitySkeletonMinion::new);
		this.soundValues(7, 0.6f, 0);
	}

	@Override
	protected EntitySkeletonMinion createMinion(Level world, LivingEntity caster, SpellModifiers modifiers){
		if(caster instanceof Player && ItemArtefact.isArtefactActive((Player)caster, WizardryItems.charm_minion_variants)){
			return new EntityStrayMinion(world);
		}else{
			return super.createMinion(world, caster, modifiers);
		}
	}

	@Override
	protected void addMinionExtras(EntitySkeletonMinion minion, BlockPos pos, LivingEntity caster, SpellModifiers modifiers, int alreadySpawned){
		minion.setItemStackToSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		minion.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
	}

}
