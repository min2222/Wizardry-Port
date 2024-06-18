package electroblob.wizardry.spell;

import java.util.Random;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class InvokeWeather extends Spell {

	public static final String THUNDERSTORM_CHANCE = "thunderstorm_chance";

	public InvokeWeather(){
		super("invoke_weather", SpellActions.POINT_UP, false);
		addProperties(THUNDERSTORM_CHANCE);
		soundValues(0.5f, 1, 0);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.level.dimension().equals(Level.OVERWORLD)){

			if(!world.isClientSide){
				
				int standardWeatherTime = (300 + (new Random()).nextInt(600)) * 20;
				
				if(world.isRaining()){
					caster.displayClientMessage(Component.translatable("spell." + this.getUnlocalisedName() + ".sun"), true);
                    ((ServerLevel) world).setWeatherParameters(standardWeatherTime, 0, false, false);
				}else{
					caster.displayClientMessage(Component.translatable("spell." + this.getUnlocalisedName() + ".rain"), true);
					// Thunderstorm is guaranteed if the caster has a bottled thundercloud charm equipped
                    ((ServerLevel) world).setWeatherParameters(0, standardWeatherTime, true, ItemArtefact.isArtefactActive(caster, WizardryItems.CHARM_STORM.get()) || world.random.nextFloat() < getProperty(THUNDERSTORM_CHANCE).floatValue());
				}
			}

			if(world.isClientSide){
				for(int i = 0; i < 10; i++){
					double x = caster.getX() + world.random.nextDouble() * 2 - 1;
					double y = caster.getY() + caster.getEyeHeight() - 0.5 + world.random.nextDouble();
					double z = caster.getZ() + world.random.nextDouble() * 2 - 1;
					ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.1, 0).clr(0.5f, 0.7f, 1).spawn(world);
				}
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}
		
		return false;
	}

}
