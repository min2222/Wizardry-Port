package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.EntityArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EntityShulkerBullet;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class ShulkerBullet extends Spell {

	public ShulkerBullet(){
		super("shulker_bullet", SpellActions.POINT_DOWN, false);
		this.soundValues(2, 1, 0.3f);
		addProperties(RANGE);
	}

	@Override public boolean canBeCastBy(Mob npc, boolean override){ return true; }

	@Override public boolean canBeCastBy(DispenserBlockEntity dispenser){ return true; }

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){
		if(!shoot(world, caster, caster.getX(), caster.getY(), caster.getZ(), Direction.UP, modifiers)) return false;
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@Override
	public boolean cast(Level world, Mob caster, InteractionHand hand, int ticksInUse, LivingEntity target, SpellModifiers modifiers){
		if(!shoot(world, caster, caster.getX(), caster.getY(), caster.getZ(), Direction.UP, modifiers)) return false;
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@Override
	public boolean cast(Level world, double x, double y, double z, Direction direction, int duration, int ticksInUse, SpellModifiers modifiers){
		if(!shoot(world, null, x, y, z, direction, modifiers)) return false;
		this.playSound(world, x, y, z, ticksInUse, -1, modifiers);
		return true;
	}

	private boolean shoot(Level world, @Nullable LivingEntity caster, double x, double y, double z, Direction direction, SpellModifiers modifiers){

		if(!world.isClientSide){

			double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.RANGE_UPGRADE.get());

			List<LivingEntity> possibleTargets = EntityUtils.getLivingWithinRadius(range, x, y, z, world);

			possibleTargets.remove(caster);
			possibleTargets.removeIf(t -> t instanceof ArmorStand);

			if(possibleTargets.isEmpty()) return false;

			// distanceToSqr doesn't require square-rooting so it's faster when only comparing
			possibleTargets.sort(Comparator.comparingDouble(t -> t.distanceToSqr(x, y, z)));

			Entity target = possibleTargets.get(0);

			// Y axis because the player is always upright
			if(caster != null){
				world.addFreshEntity(new net.minecraft.world.entity.projectile.ShulkerBullet(world, caster, target, direction.getAxis()));
			}else{
				// Can't use the normal constructor because it doesn't accept null for the owner
				net.minecraft.world.entity.projectile.ShulkerBullet bullet = new net.minecraft.world.entity.projectile.ShulkerBullet(EntityType.SHULKER_BULLET, world);
				bullet.moveTo(x, y, z, bullet.getYRot(), bullet.getXRot());

				// Where there's a will there's a way...
				CompoundTag nbt = new CompoundTag();
				bullet.save(nbt);
				nbt.putInt("Dir", direction.get3DDataValue());
				BlockPos pos = target.blockPosition();
				CompoundTag targetTag = NbtUtils.createUUID(target.getUUID());
				targetTag.putInt("X", pos.getX());
				targetTag.putInt("Y", pos.getY());
				targetTag.putInt("Z", pos.getZ());
				NBTExtras.storeTagSafely(nbt, "Target", targetTag);
				bullet.load(nbt); // LOL I just modified private fields without reflection

				bullet.getPersistentData().putFloat(SpellThrowable.DAMAGE_MODIFIER_NBT_KEY, modifiers.get(SpellModifiers.POTENCY));

				world.addFreshEntity(bullet);
			}
		}

		return true;
	}
}
