package electroblob.wizardry.tileentity;

import java.util.Random;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.spell.Spell;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Controls the book animations and remembers the GUI state when not in use. */
public class TileEntityLectern extends BlockEntity {

	public static final double BOOK_OPEN_DISTANCE = 5;

	public int tickCount;
	public float pageFlip;
	public float pageFlipPrev;
	public float flipT;
	public float flipA;
	public float bookSpread;
	public float bookSpreadPrev;

	public Spell currentSpell = Spells.none;
	
	public TileEntityLectern(BlockPos p_155229_, BlockState p_155230_) {
		super(WizardryBlocks.LECTERN_BLOCK_ENTITY.get(), p_155229_, p_155230_);
	}

    public static void update(Level p_155014_, BlockPos p_155015_, BlockState p_155016_, TileEntityLectern p_155017_) {

    	p_155017_.bookSpreadPrev = p_155017_.bookSpread;

		Player entityplayer = p_155014_.getNearestPlayer(p_155015_.getX() + 0.5, p_155015_.getY() + 0.5,
				p_155015_.getZ() + 0.5, BOOK_OPEN_DISTANCE, false);

		if(entityplayer != null){

			p_155017_.bookSpread += 0.1f;

			if(p_155017_.bookSpread < 0.5f || p_155014_.random.nextInt(40) == 0){
				float f1 = p_155017_.flipT;
				while(f1 == p_155017_.flipT) p_155017_.flipT += (float)(p_155014_.random.nextInt(4) - p_155014_.random.nextInt(4));
			}

		}else{
			p_155017_.bookSpread -= 0.1f;
		}

		p_155017_.bookSpread = Mth.clamp(p_155017_.bookSpread, 0.0f, 1.0f);

		p_155017_.tickCount++;

		p_155017_.pageFlipPrev = p_155017_.pageFlip;
		float f = (p_155017_.flipT - p_155017_.pageFlip) * 0.4f;
		f = Mth.clamp(f, -0.2f, 0.2f);
		p_155017_.flipA += (f - p_155017_.flipA) * 0.9f;
		p_155017_.pageFlip += p_155017_.flipA;

	}

	/** Called to manually sync the tile entity with clients. */
	public void sync(){
    	this.level.markAndNotifyBlock(this.worldPosition, this.level.getChunkAt(worldPosition), level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3, 512);
	}

	@Override
	public void saveAdditional(CompoundTag compound){
		super.saveAdditional(compound); // Confusingly, this method both writes to the supplied compound and returns it
		compound.putInt("spell", currentSpell.metadata());
	}

	@Override
	public void load(CompoundTag compound){
		super.load(compound);
		currentSpell = Spell.byMetadata(compound.getInt("spell"));
	}

}
