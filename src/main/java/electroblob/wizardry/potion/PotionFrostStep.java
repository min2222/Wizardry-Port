package electroblob.wizardry.potion;

import java.lang.reflect.Field;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber
public class PotionFrostStep extends PotionMagicEffect implements ICustomPotionParticles {

	private static final Field prevBlockPos = ObfuscationReflectionHelper.findField(LivingEntity.class, "field_184620_bC");

	public PotionFrostStep(MobEffectCategory category, int liquidColour){
		super(category, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/frost_step.png"));
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

		if(host.hasEffect(WizardryPotions.FROST_STEP.get())){
			// Mimics the behaviour of the frost walker enchantment itself
			if(!host.level.isClientSide){

				BlockPos currentPos = host.blockPosition();

				try{

					if(!currentPos.equals(prevBlockPos.get(host))){

						prevBlockPos.set(host, currentPos);

						int strength = host.getEffect(WizardryPotions.FROST_STEP.get()).getAmplifier();

						FrostWalkerEnchantment.onEntityMoved(host, host.level, currentPos, strength);

						if(host instanceof Player && ItemArtefact.isArtefactActive((Player)host, WizardryItems.CHARM_LAVA_WALKING.get())){
							freezeNearbyLava(host, host.level, currentPos, strength);
						}
					}

				}catch(IllegalAccessException e){
					Wizardry.logger.error("Error accessing living entity previous block pos:", e);
				}
			}
		}
	}

	/** Copied from {@link FrostWalkerEnchantment#onEntityMoved(LivingEntity, Level, BlockPos, int)} and modified
	 * to turn lava to obsidian crust blocks instead. */
	private static void freezeNearbyLava(LivingEntity living, Level world, BlockPos pos, int level){

		if(living.isOnGround()){

			float f = (float)Math.min(16, 2 + level);
			BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos(0, 0, 0);

			for(BlockPos pos2 : BlockPos.betweenClosed(pos.offset((double)(-f), -1.0D, (double)(-f)), pos.offset((double)f, -1.0D, (double)f))){

				if(pos2.distToCenterSqr(living.getX(), living.getY(), living.getZ()) <= (double)(f * f)){

					pos1.set(pos2.getX(), pos2.getY() + 1, pos2.getZ());
					BlockState state1 = world.getBlockState(pos1);

					if(state1.getMaterial() == Material.AIR){

						BlockState state2 = world.getBlockState(pos2);

						if(BlockUtils.isLavaSource(state2) && world.isUnobstructed(WizardryBlocks.OBSIDIAN_CRUST.get().defaultBlockState(), pos2, CollisionContext.empty())){
							world.setBlockAndUpdate(pos2, WizardryBlocks.OBSIDIAN_CRUST.get().defaultBlockState());
							world.scheduleTick(pos2.immutable(), WizardryBlocks.OBSIDIAN_CRUST.get(), Mth.nextInt(living.getRandom(), 60, 120));
						}
					}
				}
			}
		}
	}

}
