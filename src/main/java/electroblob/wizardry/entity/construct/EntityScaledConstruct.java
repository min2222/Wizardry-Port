package electroblob.wizardry.entity.construct;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;

/**
 * Extension of {@link EntityMagicConstruct} that implements saving and loading of size (blast) multipliers. What
 * the entity actually does with the multiplier value is up to subclasses to define; however, by default this class
 * scales the entity bounding box according to the size multiplier (this can be controlled by overriding
 * {@link EntityScaledConstruct#shouldScaleWidth()} and {@link EntityScaledConstruct#shouldScaleHeight()}).
 *
 * @author Electroblob
 * @since Wizardry 4.3
 */
public abstract class EntityScaledConstruct extends EntityMagicConstruct {

	/** The size multiplier for this construct, usually determined by the blast modifier the spell was cast with. */
	protected float sizeMultiplier = 1;
    protected EntityDimensions size = EntityDimensions.scalable(this.getBbWidth(), this.getBbHeight());

	public EntityScaledConstruct(EntityType<? extends EntityMagicConstruct> type, Level world){
		super(type, world);
        this.refreshDimensions();
	}

	public float getSizeMultiplier(){
		return sizeMultiplier;
	}

	public void setSizeMultiplier(float sizeMultiplier){
		this.sizeMultiplier = sizeMultiplier;
		setSize(shouldScaleWidth() ? getBbWidth() * sizeMultiplier : getBbWidth(), shouldScaleHeight() ? getBbHeight() * sizeMultiplier : getBbHeight());
	}
	
	public void setSize(float width, float height) {
		this.size = EntityDimensions.scalable(width, height);
	}

	@Override
	public EntityDimensions getDimensions(Pose p_19975_) {
		return this.size;
	}

	/** Returns true if the width of this entity's bounding box should be scaled by the size multiplier on creation. */
	protected boolean shouldScaleWidth(){
		return true;
	}

	/** Returns true if the height of this entity's bounding box should be scaled by the size multiplier on creation. */
	protected boolean shouldScaleHeight(){
		return true;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt){
		super.readAdditionalSaveData(nbt);
		setSizeMultiplier(nbt.getFloat("sizeMultiplier"));
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt){
		super.addAdditionalSaveData(nbt);
		nbt.putFloat("sizeMultiplier", sizeMultiplier);

	}

	@Override
	public void readSpawnData(FriendlyByteBuf data){
		super.readSpawnData(data);
		setSizeMultiplier(data.readFloat()); // Set the width correctly on the client side
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf data){
		super.writeSpawnData(data);
		data.writeFloat(sizeMultiplier);
	}
}
