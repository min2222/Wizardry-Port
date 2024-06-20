package electroblob.wizardry.spell;

import electroblob.wizardry.entity.living.EntityHuskMinion;
import electroblob.wizardry.entity.living.EntityZombieMinion;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SummonZombie extends SpellMinion<EntityZombieMinion> {

	public SummonZombie(){
		super("summon_zombie", EntityZombieMinion::new);
		this.soundValues(7, 0.6f, 0);
	}

	@Override
	protected EntityZombieMinion createMinion(Level world, LivingEntity caster, SpellModifiers modifiers){
		if(caster instanceof Player && ItemArtefact.isArtefactActive((Player)caster, WizardryItems.CHARM_MINION_VARIANTS.get())){
			return new EntityHuskMinion(world);
		}else{
			return super.createMinion(world, caster, modifiers);
		}
	}

}
