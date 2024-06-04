package electroblob.wizardry.tileentity;

import electroblob.wizardry.block.BlockThorns;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ITickable;

public class TileEntityThorns extends TileEntityPlayerSave implements ITickable {

	private int tickCount = 0;
	private int lifetime;
	private int age;

	public float damageMultiplier = 1;

	public TileEntityThorns(){
		this.lifetime = 600;
	}

	@Override
	public void update(){

		tickCount++;

		if(tickCount > lifetime && !this.level.isClientSide){
			this.world.destroyBlock(pos, false);
		}

		if(tickCount % BlockThorns.GROWTH_STAGE_DURATION == 0 && age < BlockThorns.GROWTH_STAGES - 1){
			age++;
			sync(); // Update displayed block
		}
	}

	public int getAge(){
		return age;
	}

	public void setLifetime(int lifetime){
		this.lifetime = lifetime;
	}

	@Override
	public void readFromNBT(CompoundTag tagCompound){
		super.readFromNBT(tagCompound);
		tickCount = tagCompound.getInt("timer");
		lifetime = tagCompound.getInt("maxTimer"); // Left as maxTimer for backwards compatibility
		age = tagCompound.getInt("age");
		damageMultiplier = tagCompound.getFloat("damageMultiplier");
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag tagCompound){
		super.writeToNBT(tagCompound);
		tagCompound.putInt("timer", tickCount);
		tagCompound.putInt("maxTimer", lifetime);
		tagCompound.putInt("age", age);
		tagCompound.setFloat("damageMultiplier", damageMultiplier);
		return tagCompound;
	}

}
