package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.WizardryEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntityConjuredArrow extends AbstractArrow {

	public EntityConjuredArrow(EntityType<? extends Arrow> type, Level worldIn) {
		super(type, worldIn);
	}

	public EntityConjuredArrow(Level worldIn, double x, double y, double z) {
		super(WizardryEntities.CONJURED_ARROW.get(), x, y, z, worldIn);
	}

	public EntityConjuredArrow(Level worldIn, LivingEntity shooter) {
		super(WizardryEntities.CONJURED_ARROW.get(), shooter, worldIn);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.inGroundTime > 400) {
			this.discard();
		}
	}

	@Override
	protected ItemStack getPickupItem() {
		return ItemStack.EMPTY;
	}
}
