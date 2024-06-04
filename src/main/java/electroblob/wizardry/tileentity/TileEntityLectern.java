package electroblob.wizardry.tileentity;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.ITickable;

import java.util.Random;

/** Controls the book animations and remembers the GUI state when not in use. */
public class TileEntityLectern extends BlockEntity implements ITickable {

	public static final double BOOK_OPEN_DISTANCE = 5;

	private static final Random rand = new Random();

	public int ticksExisted;
	public float pageFlip;
	public float pageFlipPrev;
	public float flipT;
	public float flipA;
	public float bookSpread;
	public float bookSpreadPrev;

	public Spell currentSpell = Spells.none;

	@Override
	public void update(){

		this.bookSpreadPrev = this.bookSpread;

		Player entityplayer = this.world.getClosestPlayer(this.pos.getX() + 0.5, this.pos.getY() + 0.5,
				this.pos.getZ() + 0.5, BOOK_OPEN_DISTANCE, false);

		if(entityplayer != null){

			this.bookSpread += 0.1f;

			if(this.bookSpread < 0.5f || random.nextInt(40) == 0){
				float f1 = this.flipT;
				while(f1 == flipT) this.flipT += (float)(random.nextInt(4) - random.nextInt(4));
			}

		}else{
			this.bookSpread -= 0.1f;
		}

		this.bookSpread = Mth.clamp(this.bookSpread, 0.0f, 1.0f);

		this.ticksExisted++;

		this.pageFlipPrev = this.pageFlip;
		float f = (this.flipT - this.pageFlip) * 0.4f;
		f = Mth.clamp(f, -0.2f, 0.2f);
		this.flipA += (f - this.flipA) * 0.9f;
		this.pageFlip += this.flipA;

	}

	/** Called to manually sync the tile entity with clients. */
	public void sync(){
		this.world.markAndNotifyBlock(pos, null, world.getBlockState(pos), world.getBlockState(pos), 3);
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag compound){
		super.writeToNBT(compound); // Confusingly, this method both writes to the supplied compound and returns it
		compound.putInt("spell", currentSpell.metadata());
		return compound;
	}

	@Override
	public void readFromNBT(CompoundTag compound){
		super.readFromNBT(compound);
		currentSpell = Spell.byMetadata(compound.getInt("spell"));
	}

	@Override
	public final CompoundTag getUpdateTag(){
		return this.writeToNBT(new CompoundTag());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		return new SPacketUpdateTileEntity(pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		readFromNBT(pkt.getNbtCompound());
	}

}
