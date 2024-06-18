package electroblob.wizardry.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Same as {@link EntityMagicProjectile}, but with an additional blast multiplier field which is synced and saved to
 * allow for the spread of particles to be changed depending on the blast area.
 * 
 * @author Electroblob
 * @since Wizardry 1.2
 */
public abstract class EntityBomb extends EntityMagicProjectile {

	/** The entity blast multiplier. This is now synced and saved centrally from {@link EntityBomb}. */
	public float blastMultiplier = 1.0f;

	public EntityBomb(EntityType<? extends EntityMagicProjectile> type, Level world){
		super(type, world);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer){
		buffer.writeFloat(blastMultiplier);
		super.writeSpawnData(buffer);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf buffer){
		blastMultiplier = buffer.readFloat();
		super.readSpawnData(buffer);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbttagcompound){
		super.readAdditionalSaveData(nbttagcompound);
		blastMultiplier = nbttagcompound.getFloat("blastMultiplier");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbttagcompound){
		super.addAdditionalSaveData(nbttagcompound);
		nbttagcompound.putFloat("blastMultiplier", blastMultiplier);
	}

}
