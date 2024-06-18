package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.potion.Curse;
import electroblob.wizardry.potion.CurseEnfeeblement;
import electroblob.wizardry.potion.CurseUndeath;
import electroblob.wizardry.potion.PotionContainment;
import electroblob.wizardry.potion.PotionDecay;
import electroblob.wizardry.potion.PotionDiamondflesh;
import electroblob.wizardry.potion.PotionFrost;
import electroblob.wizardry.potion.PotionFrostStep;
import electroblob.wizardry.potion.PotionIronflesh;
import electroblob.wizardry.potion.PotionMagicEffect;
import electroblob.wizardry.potion.PotionMagicEffectParticles;
import electroblob.wizardry.potion.PotionOakflesh;
import electroblob.wizardry.potion.PotionSlowTime;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

/**
 * Class responsible for defining, storing and registering all of wizardry's potion effects.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Mod.EventBusSubscriber
public final class WizardryPotions {

	private WizardryPotions(){} // No instances!

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Wizardry.MODID);

	public static final RegistryObject<MobEffect> FROST = placeholder();
	public static final RegistryObject<MobEffect> TRANSIENCE = placeholder();
	public static final RegistryObject<MobEffect> FIRESKIN = placeholder();
	public static final RegistryObject<MobEffect> ICE_SHROUD = placeholder();
	public static final RegistryObject<MobEffect> STATIC_AURA = placeholder();
	public static final RegistryObject<MobEffect> DECAY = placeholder();
	public static final RegistryObject<MobEffect> SIXTH_SENSE = placeholder();
	public static final RegistryObject<MobEffect> ARCANE_JAMMER = placeholder();
	public static final RegistryObject<MobEffect> MIND_TRICK = placeholder();
	public static final RegistryObject<MobEffect> MIND_CONTROL = placeholder();
	public static final RegistryObject<MobEffect> FONT_OF_MANA = placeholder();
	public static final RegistryObject<MobEffect> FEAR = placeholder();
	public static final RegistryObject<MobEffect> CURSE_OF_SOULBINDING = placeholder();
	public static final RegistryObject<MobEffect> PARALYSIS = placeholder();
	public static final RegistryObject<MobEffect> MUFFLE = placeholder();
	public static final RegistryObject<MobEffect> WARD = placeholder();
	public static final RegistryObject<MobEffect> SLOW_TIME = placeholder();
	public static final RegistryObject<MobEffect> EMPOWERMENT = placeholder();
	public static final RegistryObject<MobEffect> CURSE_OF_ENFEEBLEMENT = placeholder();
	public static final RegistryObject<MobEffect> CURSE_OF_UNDEATH = placeholder();
	public static final RegistryObject<MobEffect> CONTAINMENT = placeholder();
	public static final RegistryObject<MobEffect> FROST_STEP = placeholder();
	public static final RegistryObject<MobEffect> MARK_OF_SARCRIFICE = placeholder();
	public static final RegistryObject<MobEffect> MIRAGE = placeholder();
	public static final RegistryObject<MobEffect> OAKFLESH = placeholder();
	public static final RegistryObject<MobEffect> IRONFLESH = placeholder();
	public static final RegistryObject<MobEffect> DIAMONDFLESH = placeholder();

	/**
	 * Sets both the registry and unlocalised names of the given potion, then registers it with the given registry. Use
	 * this instead of {@link MobEffect#setRegistryName(String)} and {@link MobEffect#setPotionName(String)} during
	 * construction, for convenience and consistency.
	 * 
	 * @param registry The registry to register the given potion to.
	 * @param name The name of the potion, without the mod ID or the .name stuff. The registry name will be
	 *        {@code ebwizardry:[name]}. The unlocalised name will be {@code potion.ebwizardry:[name].name}.
	 * @param potion The potion to register.
	 */
	public static RegistryObject<MobEffect> registerPotion(String name, MobEffect potion){
		return EFFECTS.register(name, () -> potion);
	}

	@SubscribeEvent
	public static void register(RegistryEvent.Register<MobEffect> event){

		IForgeRegistry<MobEffect> registry = event.getRegistry();

		// Interestingly, setting the colour to black stops the particles from rendering.

		registerPotion("frost", new PotionFrost(MobEffectCategory.HARMFUL, 0)); // Colour was 0x38ddec (was arbitrary anyway)
		
		registerPotion("transience", new PotionMagicEffectParticles(MobEffectCategory.BENEFICIAL, 0,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/transience.png")){
			@Override
			public void spawnCustomParticle(Level world, double x, double y, double z){
				ParticleBuilder.create(Type.DUST).pos(x, y, z).clr(0.8f, 0.8f, 1.0f).shaded(true).spawn(world);
			}
		}); // 0xffe89b
		
		registerPotion("fireskin", new PotionMagicEffectParticles(MobEffectCategory.BENEFICIAL, 0,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/fireskin.png")){
			@Override
			public void spawnCustomParticle(Level world, double x, double y, double z){
				world.spawnParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
			}

			@Override
			public void performEffect(LivingEntity entitylivingbase, int strength){
				entitylivingbase.extinguish(); // Stops melee mobs that are on fire from setting the player on fire,
				// without allowing the player to actually stand in fire or swim in lava without taking damage.
			}
		}); // 0xff2f02
		
		registerPotion("ice_shroud", new PotionMagicEffectParticles(MobEffectCategory.BENEFICIAL, 0,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/ice_shroud.png")){
			@Override
			public void spawnCustomParticle(Level world, double x, double y, double z){
				float brightness = 0.5f + (world.random.nextFloat() / 2);
				ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).clr(brightness, brightness + 0.1f, 1.0f).gravity(true).spawn(world);
				ParticleBuilder.create(Type.SNOW).pos(x, y, z).spawn(world);
			}
		}); // 0x52f1ff
		
		registerPotion("static_aura", new PotionMagicEffectParticles(MobEffectCategory.BENEFICIAL, 0,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/static_aura.png")){
			@Override
			public void spawnCustomParticle(Level world, double x, double y, double z){
				ParticleBuilder.create(Type.SPARK).pos(x, y, z).spawn(world);
			}
		}); // 0x0070ff
		
		registerPotion("decay", new PotionDecay(MobEffectCategory.HARMFUL, 0x3c006c));

		registerPotion("sixth_sense", new PotionMagicEffect(MobEffectCategory.BENEFICIAL, 0xc6ff01,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/sixth_sense.png")){
			@Override
			public void performEffect(LivingEntity target, int strength){
				// Reset the shader (a bit dirty but both the potion expiry hooks are only fired server-side, and
				// there's no point sending packets unnecessarily if we can just do this instead)
				if(target.getActivePotionEffect(this).getDuration() <= 1 && target.level.isClientSide
						&& target == net.minecraft.client.Minecraft.getMinecraft().player){
					net.minecraft.client.Minecraft.getMinecraft().entityRenderer.stopUseShader();
				}
			}
		});

		registerPotion("arcane_jammer", new PotionMagicEffect(MobEffectCategory.HARMFUL, 0xcf4aa2,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/arcane_jammer.png")));

		registerPotion("mind_trick", new PotionMagicEffect(MobEffectCategory.HARMFUL, 0x601683,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/mind_trick.png")));

		registerPotion("mind_control", new PotionMagicEffectParticles(MobEffectCategory.HARMFUL, 0x320b44,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/mind_control.png")) {
			@Override
			public void spawnCustomParticle(Level world, double x, double y, double z){} // We only want the syncing
		});

		registerPotion("font_of_mana", new PotionMagicEffect(MobEffectCategory.BENEFICIAL, 0xffe5bb,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/font_of_mana.png")));

		registerPotion("fear", new PotionMagicEffect(MobEffectCategory.HARMFUL, 0xbd0100,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/fear.png")));
		
		registerPotion("curse_of_soulbinding", new Curse(true, 0x0f000f,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/curse_of_soulbinding.png")){
			@Override // We're not removing any attributes, but it's called when we want it to be so...
			public void removeAttributesModifiersFromEntity(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AbstractAttributeMap attributeMapIn, int amplifier){
				// TODO: Hmmmm...
			}
		});
		
		registerPotion("paralysis", new PotionMagicEffectParticles(MobEffectCategory.HARMFUL, 0,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/paralysis.png")){
			@Override
			public void spawnCustomParticle(Level world, double x, double y, double z){
				ParticleBuilder.create(Type.SPARK).pos(x, y, z).spawn(world);
			}
		});
		
		registerPotion("muffle", new PotionMagicEffect(MobEffectCategory.BENEFICIAL, 0x4464d9,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/muffle.png")));

		registerPotion("ward", new PotionMagicEffect(MobEffectCategory.BENEFICIAL, 0xc991d0,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/ward.png")));

		registerPotion("slow_time", new PotionSlowTime(MobEffectCategory.BENEFICIAL, 0x5be3bb));

		registerPotion("empowerment", new PotionMagicEffect(MobEffectCategory.BENEFICIAL, 0x8367bd,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/empowerment.png")));

		registerPotion("curse_of_enfeeblement", new CurseEnfeeblement(true, 0x36000b));

		registerPotion("curse_of_undeath", new CurseUndeath(true, 0x685c00));

		registerPotion("containment", new PotionContainment(true, 0x7988cc));

		registerPotion("frost_step", new PotionFrostStep(MobEffectCategory.BENEFICIAL, 0));

		registerPotion("mark_of_sacrifice", new PotionMagicEffect(MobEffectCategory.HARMFUL, 0xe90e48,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/mark_of_sacrifice.png")));

		registerPotion("mirage", new PotionMagicEffectParticles(MobEffectCategory.BENEFICIAL, 0, // No particles or we'll see the player's actual position!
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/mirage.png")){
			@Override
			public void spawnCustomParticle(Level world, double x, double y, double z){} // We only want the syncing
		});

		registerPotion("oakflesh", new PotionOakflesh(MobEffectCategory.BENEFICIAL, 0));
		registerPotion("ironflesh", new PotionIronflesh(MobEffectCategory.BENEFICIAL, 0));
		registerPotion("diamondflesh", new PotionDiamondflesh(MobEffectCategory.BENEFICIAL, 0));

	}

}