package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityDispenser;
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

	@Override public boolean canBeCastBy(EntityLiving npc, boolean override){ return true; }

	@Override public boolean canBeCastBy(TileEntityDispenser dispenser){ return true; }

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){
		if(!shoot(world, caster, caster.posX, caster.posY, caster.posZ, Direction.UP, modifiers)) return false;
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@Override
	public boolean cast(Level world, EntityLiving caster, InteractionHand hand, int ticksInUse, LivingEntity target, SpellModifiers modifiers){
		if(!shoot(world, caster, caster.posX, caster.posY, caster.posZ, Direction.UP, modifiers)) return false;
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

		if(!world.isRemote){

			double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);

			List<LivingEntity> possibleTargets = EntityUtils.getLivingWithinRadius(range, x, y, z, world);

			possibleTargets.remove(caster);
			possibleTargets.removeIf(t -> t instanceof EntityArmorStand);

			if(possibleTargets.isEmpty()) return false;

			// getDistanceSq doesn't require square-rooting so it's faster when only comparing
			possibleTargets.sort(Comparator.comparingDouble(t -> t.getDistanceSq(x, y, z)));

			Entity target = possibleTargets.get(0);

			// Y axis because the player is always upright
			if(caster != null){
				world.spawnEntity(new EntityShulkerBullet(world, caster, target, direction.getAxis()));
			}else{
				// Can't use the normal constructor because it doesn't accept null for the owner
				EntityShulkerBullet bullet = new EntityShulkerBullet(world);
				bullet.setLocationAndAngles(x, y, z, bullet.rotationYaw, bullet.rotationPitch);

				// Where there's a will there's a way...
				CompoundTag nbt = new CompoundTag();
				bullet.writeToNBT(nbt);
				nbt.setInteger("Dir", direction.getIndex());
				BlockPos pos = new BlockPos(target);
				CompoundTag targetTag = NBTUtil.createUUIDTag(target.getUniqueID());
				targetTag.setInteger("X", pos.getX());
				targetTag.setInteger("Y", pos.getY());
				targetTag.setInteger("Z", pos.getZ());
				NBTExtras.storeTagSafely(nbt, "Target", targetTag);
				bullet.readFromNBT(nbt); // LOL I just modified private fields without reflection

				bullet.getEntityData().setFloat(SpellThrowable.DAMAGE_MODIFIER_NBT_KEY, modifiers.get(SpellModifiers.POTENCY));

				world.spawnEntity(bullet);
			}
		}

		return true;
	}
}
