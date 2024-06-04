package electroblob.wizardry.entity.projectile;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntityConjuredArrow extends Arrow {

	public EntityConjuredArrow(Level worldIn) {
		super(worldIn);
	}

	public EntityConjuredArrow(Level worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	public EntityConjuredArrow(Level worldIn, LivingEntity shooter) {
		super(worldIn, shooter);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.timeInGround > 400) {
			this.discard();
		}
	}

	@Override
	protected ItemStack getArrowStack() {
		return ItemStack.EMPTY;
	}
}
