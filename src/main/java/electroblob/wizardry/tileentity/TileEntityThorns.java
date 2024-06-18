package electroblob.wizardry.tileentity;

import electroblob.wizardry.block.BlockThorns;
import electroblob.wizardry.registry.WizardryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityThorns extends TileEntityPlayerSave {

	private int tickCount = 0;
	private int lifetime;
	private int age;

	public float damageMultiplier = 1;

	public TileEntityThorns(BlockPos p_155229_, BlockState p_155230_) {
		super(WizardryBlocks.THORNS_BLOCK_ENTITY.get(), p_155229_, p_155230_);
		this.lifetime = 600;
	}

	public static void update(Level p_155014_, BlockPos p_155015_, BlockState p_155016_, TileEntityThorns p_155017_) {

		p_155017_.tickCount++;

		if(p_155017_.tickCount > p_155017_.lifetime && !p_155014_.isClientSide){
			p_155014_.destroyBlock(p_155015_, false);
		}

		if(p_155017_.tickCount % BlockThorns.GROWTH_STAGE_DURATION == 0 && p_155017_.age < BlockThorns.GROWTH_STAGES - 1){
			p_155017_.age++;
			p_155017_.sync(); // Update displayed block
		}
	}

	public int getAge(){
		return age;
	}

	public void setLifetime(int lifetime){
		this.lifetime = lifetime;
	}

	@Override
	public void load(CompoundTag tagCompound){
		super.load(tagCompound);
		tickCount = tagCompound.getInt("timer");
		lifetime = tagCompound.getInt("maxTimer"); // Left as maxTimer for backwards compatibility
		age = tagCompound.getInt("age");
		damageMultiplier = tagCompound.getFloat("damageMultiplier");
	}

	@Override
	public void saveAdditional(CompoundTag tagCompound){
		super.saveAdditional(tagCompound);
		tagCompound.putInt("timer", tickCount);
		tagCompound.putInt("maxTimer", lifetime);
		tagCompound.putInt("age", age);
		tagCompound.putFloat("damageMultiplier", damageMultiplier);
	}

}
