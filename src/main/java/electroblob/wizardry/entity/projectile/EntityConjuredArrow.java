package electroblob.wizardry.entity.projectile;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntityConjuredArrow extends EntityArrow {

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
			this.setDead();
		}
	}

	@Override
	protected ItemStack getArrowStack() {
		return ItemStack.EMPTY;
	}
}
