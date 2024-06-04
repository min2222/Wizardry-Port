package electroblob.wizardry.client.audio;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

// Copied from MovingSoundMinecart; if it ever breaks between updates take a look at that.
//@SideOnly(Side.CLIENT)
public class MovingSoundEntity<T extends Entity> extends MovingSound {
	
	protected final T source;
	protected float distance = 0.0F;

	public MovingSoundEntity(T entity, SoundEvent sound, SoundSource category, float volume, float pitch, boolean repeat){
		super(sound, category);
		this.source = entity;
		this.repeat = repeat;
		this.volume = volume;
		this.pitch = pitch;
		this.repeatDelay = 0;
	}

	@Override
	public void update(){
		
		if(this.source.isDead){
			this.donePlaying = true;
		}else{
			this.xPosF = (float)this.source.posX;
			this.yPosF = (float)this.source.posY;
			this.zPosF = (float)this.source.posZ;
		}
	}
}