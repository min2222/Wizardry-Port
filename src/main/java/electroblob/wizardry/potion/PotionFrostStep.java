package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.enchantment.EnchantmentFrostWalker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber
public class PotionFrostStep extends PotionMagicEffect implements ICustomPotionParticles {

	private static final Field prevBlockPos = ObfuscationReflectionHelper.findField(LivingEntity.class, "field_184620_bC");

	public PotionFrostStep(boolean isBadEffect, int liquidColour){
		super(isBadEffect, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/frost_step.png"));
		this.setPotionName("potion." + Wizardry.MODID + ":frost_step");
	}

//	@Override
//	public boolean isReady(int duration, int amplifier){
//		return true; // Execute the effect every tick
//	}

	@Override
	public void spawnCustomParticle(Level world, double x, double y, double z){
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).time(15 + world.random.nextInt(5)).spawn(world);
	}

	// Use LivingUpdateEvent instead of performEffect because it gets called before the actual frost walker processing
	// performEffect is called afterwards, at which point prevBlockPos has already been set to the current position
	// regardless of whether the player is wearing frost walker boots or not

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingEvent.LivingTickEvent event){

		LivingEntity host = event.getEntity();

		if(host.isPotionActive(WizardryPotions.frost_step)){
			// Mimics the behaviour of the frost walker enchantment itself
			if(!host.level.isClientSide){

				BlockPos currentPos = new BlockPos(host);

				try{

					if(!currentPos.equals(prevBlockPos.get(host))){

						prevBlockPos.set(host, currentPos);

						int strength = host.getActivePotionEffect(WizardryPotions.frost_step).getAmplifier();

						EnchantmentFrostWalker.freezeNearby(host, host.world, currentPos, strength);

						if(host instanceof Player && ItemArtefact.isArtefactActive((Player)host, WizardryItems.charm_lava_walking)){
							freezeNearbyLava(host, host.world, currentPos, strength);
						}
					}

				}catch(IllegalAccessException e){
					Wizardry.logger.error("Error accessing living entity previous block pos:", e);
				}
			}
		}
	}

	/** Copied from {@link EnchantmentFrostWalker#freezeNearby(LivingEntity, Level, BlockPos, int)} and modified
	 * to turn lava to obsidian crust blocks instead. */
	private static void freezeNearbyLava(LivingEntity living, Level world, BlockPos pos, int level){

		if(living.onGround){

			float f = (float)Math.min(16, 2 + level);
			BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos(0, 0, 0);

			for(BlockPos.MutableBlockPos pos2 : BlockPos.getAllInBoxMutable(pos.add((double)(-f), -1.0D, (double)(-f)), pos.add((double)f, -1.0D, (double)f))){

				if(pos2.distanceSqToCenter(living.getX(), living.getY(), living.getZ()) <= (double)(f * f)){

					pos1.setPos(pos2.getX(), pos2.getY() + 1, pos2.getZ());
					BlockState state1 = world.getBlockState(pos1);

					if(state1.getMaterial() == Material.AIR){

						BlockState state2 = world.getBlockState(pos2);

						if(BlockUtils.isLavaSource(state2) && world.mayPlace(WizardryBlocks.obsidian_crust, pos2, false, Direction.DOWN, null)){
							world.setBlockState(pos2, WizardryBlocks.obsidian_crust.getDefaultState());
							world.scheduleUpdate(pos2.toImmutable(), WizardryBlocks.obsidian_crust, Mth.getInt(living.getRNG(), 60, 120));
						}
					}
				}
			}
		}
	}

}
