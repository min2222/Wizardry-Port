package electroblob.wizardry.client;

import electroblob.wizardry.CommonProxy;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.client.animation.ActionAnimation;
import electroblob.wizardry.client.animation.PlayerAnimator;
import electroblob.wizardry.client.audio.MovingSoundEntity;
import electroblob.wizardry.client.audio.MovingSoundSpellCharge;
import electroblob.wizardry.client.audio.SoundLoop;
import electroblob.wizardry.client.audio.SoundLoopSpell;
import electroblob.wizardry.client.gui.GuiLectern;
import electroblob.wizardry.client.gui.GuiSpellDisplay;
import electroblob.wizardry.client.gui.config.NamedBooleanEntry;
import electroblob.wizardry.client.gui.config.SpellHUDSkinChooserEntry;
import electroblob.wizardry.client.gui.handbook.GuiWizardHandbook;
import electroblob.wizardry.client.model.ModelRobeArmour;
import electroblob.wizardry.client.model.ModelSageArmour;
import electroblob.wizardry.client.model.ModelWizardArmour;
import electroblob.wizardry.client.particle.*;
import electroblob.wizardry.client.particle.ParticleWizardry.IWizardryParticleFactory;
import electroblob.wizardry.client.renderer.RenderSpectralGolem;
import electroblob.wizardry.client.renderer.entity.*;
import electroblob.wizardry.client.renderer.entity.layers.*;
import electroblob.wizardry.client.renderer.overlay.RenderBlinkEffect;
import electroblob.wizardry.client.renderer.tileentity.*;
import electroblob.wizardry.command.SpellEmitter;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.data.DispenserCastingData;
import electroblob.wizardry.data.SpellEmitterData;
import electroblob.wizardry.data.SpellGlyphData;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.EntityShield;
import electroblob.wizardry.entity.construct.*;
import electroblob.wizardry.entity.living.*;
import electroblob.wizardry.entity.projectile.*;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.integration.antiqueatlas.WizardryAntiqueAtlasIntegration;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.packet.*;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems.Materials;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.*;
import electroblob.wizardry.tileentity.*;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WandHelper;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBlaze;
import net.minecraft.client.renderer.entity.RenderHusk;
import net.minecraft.client.renderer.entity.RenderSkeleton;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EntityBlaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfigEntries.NumberSliderEntry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import org.lwjgl.input.Keyboard;

import com.mojang.blaze3d.platform.InputConstants;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The client proxy for wizardry.
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
public class ClientProxy extends CommonProxy {

	/** Static instance of the mixed font renderer */
	public static MixedFontRenderer mixedFontRenderer;

	/** Static particle factory map */
	private static final Map<ResourceLocation, IWizardryParticleFactory> factories = new HashMap<>();

	// Key Bindings
	public static final KeyMapping NEXT_SPELL = new KeyMapping("key." + Wizardry.MODID + ".next_spell", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_N, "key.categories." + Wizardry.MODID);
	public static final KeyMapping PREVIOUS_SPELL = new KeyMapping("key." + Wizardry.MODID + ".previous_spell", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_B, "key.categories." + Wizardry.MODID);
	public static final KeyMapping[] SPELL_QUICK_ACCESS = new KeyMapping[ItemWand.BASE_SPELL_SLOTS + Constants.UPGRADE_STACK_LIMIT];

	static {
		for(int i = 0; i < SPELL_QUICK_ACCESS.length; i++){
			SPELL_QUICK_ACCESS[i] = new KeyMapping("key." + Wizardry.MODID + ".spell_" + (i+1),
					KeyConflictContext.IN_GAME, KeyModifier.ALT, InputConstants.Type.KEYSYM, InputConstants.KEY_1 + i, "key.categories." + Wizardry.MODID);
		}
	}

	// Armour Models
	// Can't use an EnumMap here because our additional values aren't really part of the enum
	public static final Map<ArmorMaterial, HumanoidModel> wizard_armour_models = new HashMap<>();

	static {
		wizard_armour_models.put(Materials.SILK, new ModelWizardArmour(0.75f));
		wizard_armour_models.put(Materials.SAGE, new ModelSageArmour(0.75f));
		wizard_armour_models.put(Materials.BATTLEMAGE, new ModelRobeArmour(0.75f, true));
		wizard_armour_models.put(Materials.WARLOCK, new ModelRobeArmour(0.75f, false));
	}

	/** The wrap width for standard multi-line descriptions (see {@link ClientProxy#addMultiLineDescription(List, String, Style, Object...)}). */
	private static final int TOOLTIP_WRAP_WIDTH = 140;

	// Registry
	// ===============================================================================================================

	@Override
	public ModelBiped getWizardArmourModel(ArmorMaterial material){
		return wizard_armour_models.get(material);
	}

	@Override
	public void registerKeyBindings(){
		ClientRegistry.registerKeyBinding(NEXT_SPELL);
		ClientRegistry.registerKeyBinding(PREVIOUS_SPELL);
		for(KeyBinding key : SPELL_QUICK_ACCESS) ClientRegistry.registerKeyBinding(key);
	}

	@Override
	public void initGuiBits(){
		mixedFontRenderer = new MixedFontRenderer(Minecraft.getInstance().gameSettings, new ResourceLocation("textures/font/ascii.png"),
				Minecraft.getInstance().renderEngine, false);
	}

	@Override
	public void registerResourceReloadListeners(){
		IResourceManager manager = Minecraft.getInstance().getResourceManager();
		if(manager instanceof IReloadableResourceManager){
			((IReloadableResourceManager)manager).registerReloadListener(GuiSpellDisplay::loadSkins);
			if (Wizardry.settings.loadHandbook)
				((IReloadableResourceManager)manager).registerReloadListener(GuiWizardHandbook::loadHandbookFile);
		}
	}

//	@Override
//	public void registerSoundEventListener(){
//		Minecraft.getInstance().getSoundHandler().addListener(ContinuousSpellSoundEntity::soundPlayed);
//	}

	public void registerAtlasMarkers(){
		WizardryAntiqueAtlasIntegration.registerMarkers();
	}

	// Misc
	// ===============================================================================================================

	@Override
	public void setToNumberSliderEntry(Property property){
		property.setConfigEntryClass(NumberSliderEntry.class);
	}

	@Override
	public void setToHUDChooserEntry(Property property){
		property.setConfigEntryClass(SpellHUDSkinChooserEntry.class);
	}

	@Override
	public void setToNamedBooleanEntry(Property property){
		property.setConfigEntryClass(NamedBooleanEntry.class);
	}

	@Override
	public Level getTheWorld(){
		return Minecraft.getInstance().level;
	}

	@Override
	public Player getThePlayer(){
		return Minecraft.getInstance().player;
	}

	@Override
	public boolean isFirstPerson(Entity entity){
		return entity == Minecraft.getInstance().getRenderViewEntity() && Minecraft.getInstance().gameSettings.thirdPersonView == 0;
	}

	@Override
	public void playBlinkEffect(Player player){
		if(Minecraft.getInstance().player == player) RenderBlinkEffect.playBlinkEffect();
	}

	@Override
	public void shakeScreen(Player player, float intensity){
		if(Minecraft.getInstance().player == player) ScreenShakeHandler.shakeScreen(intensity);
	}

	@Override
	public void loadShader(Player player, ResourceLocation shader){
		if(Minecraft.getInstance().player == player && Wizardry.settings.useShaders
				&& !Minecraft.getInstance().entityRenderer.isShaderActive())
			Minecraft.getInstance().entityRenderer.loadShader(shader);
	}

	@Override
	public Set<String> getSpellHUDSkins(){
		return GuiSpellDisplay.getSkinKeys();
	}

	@Override
	public void notifyBookshelfChange(Level world, BlockPos pos){

		super.notifyBookshelfChange(world, pos);

		Player player = Minecraft.getInstance().player;

		if(player.distanceToSqr(pos) < BlockBookshelf.PLAYER_NOTIFY_RANGE * BlockBookshelf.PLAYER_NOTIFY_RANGE){
			if(Minecraft.getInstance().currentScreen instanceof GuiLectern){
				((GuiLectern)Minecraft.getInstance().currentScreen).refreshAvailableSpells();
			}
		}
	}

	// Sound
	// ===============================================================================================================

	@Override
	public void playMovingSound(Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch, boolean repeat){
		Minecraft.getInstance().getSoundHandler().playSound(new MovingSoundEntity<>(entity, sound, category, volume, pitch, repeat));
	}

	@Override
	public void playChargeupSound(LivingEntity entity){
		Minecraft.getInstance().getSoundHandler().playSound(new MovingSoundSpellCharge(entity, WizardrySounds.ITEM_WAND_CHARGEUP, WizardrySounds.SPELLS, 2.5f, 1.4f, false));
	}

	@Override
	public void playSpellSoundLoop(LivingEntity entity, Spell spell, SoundEvent start, SoundEvent loop, SoundEvent end, SoundSource category, float volume, float pitch){
		SoundLoop.addLoop(new SoundLoopSpell.SoundLoopSpellEntity(start, loop, end, spell, entity, volume, pitch));
	}

	@Override
	public void playSpellSoundLoop(Level world, double x, double y, double z, Spell spell, SoundEvent start, SoundEvent loop, SoundEvent end, SoundSource category, float volume, float pitch, int duration){
		if(duration == -1){
			SoundLoop.addLoop(new SoundLoopSpell.SoundLoopSpellDispenser(start, loop, end, spell, world, x, y, z, volume, pitch));
		}else{
			SoundLoop.addLoop(new SoundLoopSpell.SoundLoopSpellPosTimed(start, loop, end, spell, duration, x, y, z, volume, pitch));
		}
	}

	// Items
	// ===============================================================================================================

	@Override
	public boolean shouldDisplayDiscovered(Spell spell, @Nullable ItemStack stack){

		EntityPlayerSP player = Minecraft.getInstance().player;

		if(player == null) return false;

		// Displayed recipe
		// Weirdly, the gui is actually the only way of accessing the current IMerchant or their recipes, other than a
		// brute-force search through all the entities in the world to find the merchant interacting with the player
		// Since we only need this client-side anyway, we might as well go via the gui
		if(Minecraft.getInstance().currentScreen instanceof GuiMerchant){
			// It doesn't actually matter if the recipe is selected or not, since the itemstack will only ever
			// match one of them anyway - and we'd have to reflect into GuiMerchant to get the selected recipe
			MerchantRecipeList recipes = ((GuiMerchant)Minecraft.getInstance().currentScreen).getMerchant().getRecipes(player);
			if(recipes != null && recipes.stream().anyMatch(r -> r.getItemToSell() == stack)){
				// Spell books are always discovered when wizards are selling them
				return true;
			}
		}

		// Recipe output slot
		// Required or players would be able to find out what the spell is without actually completing the trade
		if(player.openContainer instanceof ContainerMerchant){

			if(((ContainerMerchant)player.openContainer).getMerchantInventory().getStackInSlot(2) == stack){
				return true;
			}
		}

		if(!Wizardry.settings.discoveryMode) return true;
		if(player.isCreative()) return true;
		if(WizardData.get(player) != null && WizardData.get(player).hasSpellBeenDiscovered(spell)) return true;

		return false;
	}

	@Override
	public Font getFontRenderer(ItemStack stack){

		Spell spell = Spells.none;

		if(stack.getItem() instanceof ItemWand){
			spell = WandHelper.getCurrentSpell(stack);
		}else if(stack.getItem() instanceof ItemSpellBook || stack.getItem() instanceof ItemScroll){
			spell = Spell.byMetadata(stack.getItemDamage());
		}

		if(!shouldDisplayDiscovered(spell, stack)){
			return mixedFontRenderer;
		}

		return null;
	}

	@Override
	public String getScrollDisplayName(ItemStack scroll){

		Spell spell = Spell.byMetadata(scroll.getItemDamage());

		Player player = Minecraft.getInstance().player;

		boolean discovered = true;
		// It seems that this method is called when the world is loading, before thePlayer has been initialised.
		// If the player is null, the spell is assumed to be discovered.
		if(player != null && Wizardry.settings.discoveryMode && !player.isCreative() && WizardData.get(player) != null
				&& !WizardData.get(player).hasSpellBeenDiscovered(spell)){
			discovered = false;
		}

		if(discovered){
			return I18n.format("item." + Wizardry.MODID + ":scroll.name", spell.getDisplayName()).trim();
		}else{
			return I18n.format("item." + Wizardry.MODID + ":scroll.undiscovered.name", "#" + SpellGlyphData.getGlyphName(spell, player.world) + "#").trim();
		}
	}

	@Override
	public double getConjuredBowDurability(ItemStack stack){
		Player player = Minecraft.getInstance().player;
		if(player.getActiveItemStack() == stack){
			return (double)(stack.getItemDamage() + (player.getItemInUseMaxCount())) / (double)stack.getMaxDamage();
		}
		return super.getConjuredBowDurability(stack);
	}

	@Override
	public String translate(String key, Style style, Object... args){
		return style.getFormattingCode() + I18n.format(key, args);
	}

	@Override
	public void addMultiLineDescription(List<String> tooltip, String key, Style style, Object... args){
		tooltip.addAll(Minecraft.getInstance().fontRenderer.listFormattedStringToWidth(translate(key, style, args), TOOLTIP_WRAP_WIDTH));
	}

	// Particles
	// ===============================================================================================================

	/** Use {@link ParticleWizardry#registerParticle(ResourceLocation, IWizardryParticleFactory)}, this is internal. */
	// I mean, it does exactly the same thing but I might want to make it do something else in future...
	public static void addParticleFactory(ResourceLocation name, IWizardryParticleFactory factory){
		factories.put(name, factory);
	}

	@Override
	public void registerParticles(){
		// I'll be a good programmer and use the API method rather than the one above. Lead by example, as they say...
		ParticleWizardry.registerParticle(Type.BEAM, ParticleBeam::new);
		ParticleWizardry.registerParticle(Type.BLOCK_HIGHLIGHT, ParticleBlockHighlight::new);
		ParticleWizardry.registerParticle(Type.BUFF, ParticleBuff::new);
		ParticleWizardry.registerParticle(Type.CLOUD, ParticleCloud::new);
		ParticleWizardry.registerParticle(Type.DARK_MAGIC, ParticleDarkMagic::new);
		ParticleWizardry.registerParticle(Type.DUST, ParticleDust::new);
		ParticleWizardry.registerParticle(Type.FLASH, ParticleFlash::new);
		ParticleWizardry.registerParticle(Type.GUARDIAN_BEAM, ParticleGuardianBeam::new);
		ParticleWizardry.registerParticle(Type.ICE, ParticleIce::new);
		ParticleWizardry.registerParticle(Type.LEAF, ParticleLeaf::new);
		ParticleWizardry.registerParticle(Type.LIGHTNING, ParticleLightning::new);
		ParticleWizardry.registerParticle(Type.LIGHTNING_PULSE, ParticleLightningPulse::new);
		ParticleWizardry.registerParticle(Type.MAGIC_BUBBLE, ParticleMagicBubble::new);
		ParticleWizardry.registerParticle(Type.MAGIC_FIRE, ParticleMagicFlame::new);
		ParticleWizardry.registerParticle(Type.PATH, ParticlePath::new);
		ParticleWizardry.registerParticle(Type.SCORCH, ParticleScorch::new);
		ParticleWizardry.registerParticle(Type.SNOW, ParticleSnow::new);
		ParticleWizardry.registerParticle(Type.SPARK, ParticleSpark::new);
		ParticleWizardry.registerParticle(Type.SPARKLE, ParticleSparkle::new);
		ParticleWizardry.registerParticle(Type.SPHERE, ParticleSphere::new);
		ParticleWizardry.registerParticle(Type.VINE, ParticleVine::new);
	}

	@Override
	public ParticleWizardry createParticle(ResourceLocation type, Level world, double x, double y, double z){
		IWizardryParticleFactory factory = factories.get(type);
		if(factory == null){
			Wizardry.logger.warn("Unrecognised particle type {} ! Ensure the particle is properly registered.", type);
			return null;
		}
		return factory.createParticle(world, x, y, z);
	}

	@Override
	public void spawnTornadoParticle(Level world, double x, double y, double z, double velX, double velZ, double radius, int maxAge,
                                     BlockState block, BlockPos pos){
		Minecraft.getInstance().effectRenderer.addEffect(new ParticleTornado(world, maxAge, x, z, radius, y, velX, velZ, block).setBlockPos(pos));// , world.random.nextInt(6)));
	}

	// Packet Handlers
	// ===============================================================================================================

	@Override
	public void handleCastSpellPacket(PacketCastSpell.Message message){

		Level world = Minecraft.getInstance().world;
		Entity caster = level.getEntityByID(message.casterID);
		Spell spell = Spell.byNetworkID(message.spellID);
		// Should always be true
		if(caster instanceof Player){

			((Player)caster).setActiveHand(message.hand);

			spell.cast(world, (Player)caster, message.hand, 0, message.modifiers);

			Source source = Source.OTHER;

			Item item = ((Player)caster).getItemInHand(message.hand).getItem();

			if(item instanceof ItemWand){
				source = Source.WAND;
			}else if(item instanceof ItemScroll){
				source = Source.SCROLL;
			}

			// No need to check if the spell succeeded, because the packet is only ever sent when it succeeds.
			// The handler for this event now deals with discovery.
			MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(source, spell, (Player)caster, message.modifiers));

		}else{
			Wizardry.logger.warn("Recieved a PacketCastSpell, but the caster ID was not the ID of a player");
		}
	}

	@Override
	public void handleCastSpellAtPosPacket(PacketCastSpellAtPos.Message message){

		Level world = Minecraft.getInstance().world;
		Spell spell = Spell.byNetworkID(message.spellID);

		spell.cast(world, message.position.x, message.position.y, message.position.z, message.direction, 0, message.duration, message.modifiers);

		MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.COMMAND, spell, world, message.position.x, message.position.y, message.position.z, message.direction, message.modifiers));

		if(spell.isContinuous){
			SpellEmitter.add(spell, world, message.position.x, message.position.y, message.position.z, message.direction, message.duration, message.modifiers);
		}
	}

	@Override
	public void handleCastContinuousSpellPacket(PacketCastContinuousSpell.Message message){

		Level world = Minecraft.getInstance().world;
		Entity caster = level.getEntityByID(message.casterID);
		Spell spell = Spell.byNetworkID(message.spellID);
		// Should always be true
		if(caster instanceof Player){

			WizardData data = WizardData.get((Player)caster);

			if(data != null){
				if(data.isCasting()){
					WizardData.get((Player)caster).stopCastingContinuousSpell();
				}else{
					WizardData.get((Player)caster).startCastingContinuousSpell(spell, message.modifiers, message.duration);
				}
			}
		}else{
			Wizardry.logger.warn("Recieved a PacketCastContinuousSpell, but the caster ID was not the ID of a player");
		}
	}

	@Override
	public void handleNPCCastSpellPacket(PacketNPCCastSpell.Message message){

		Level world = Minecraft.getInstance().world;
		Entity caster = level.getEntityByID(message.casterID);
		Entity target = message.targetID == -1 ? null : level.getEntityByID(message.targetID);
		Spell spell = Spell.byNetworkID(message.spellID);
		// Should always be true
		if(caster instanceof Mob){

			if(target instanceof LivingEntity){

				spell.cast(world, (Mob)caster, message.hand, 0, (LivingEntity)target, message.modifiers);
				// Again, no need to check if the spell succeeded, because the packet is only ever sent when it
				// succeeds.
				MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.NPC, spell, (Mob)caster, message.modifiers));

				if(caster instanceof ISpellCaster){
					if(spell.isContinuous || spell instanceof None){
						((ISpellCaster)caster).setContinuousSpell(spell);
						((ISpellCaster)caster).setSpellCounter(spell instanceof None ? 0 : 1);
						((Mob)caster).setAttackTarget((LivingEntity)target);
					}
				}
			}

		}else if(caster != null){
			Wizardry.logger.warn("Recieved a PacketNPCCastSpell, but the caster ID was not the ID of an EntityLiving");
		}
	}

	@Override
	public void handleDispenserCastSpellPacket(PacketDispenserCastSpell.Message message){

		Level world = Minecraft.getInstance().world;

		if(level.getTileEntity(message.pos) instanceof DispenserBlockEntity){ // Should always be true

			Spell spell = Spell.byNetworkID(message.spellID);

			spell.cast(world, message.x, message.y, message.z, message.direction, 0, -1, message.modifiers);
			// No need to check if the spell succeeded, because the packet is only ever sent when it succeeds.
			MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.DISPENSER, spell, world, message.x, message.y,
					message.z, message.direction, message.modifiers));

			if(spell.isContinuous || spell instanceof None){

				DispenserCastingData data = DispenserCastingData.get((DispenserBlockEntity)level.getTileEntity(message.pos));

				if(spell.isContinuous){
					data.startCasting(spell, message.x, message.y, message.z, message.duration, message.modifiers);
				}else{
					data.stopCasting();
				}
			}

		}else{
			Wizardry.logger.warn("Recieved a PacketDispenserCastSpell, but no tileEntity was found at the supplied location.");
		}

	}

	@Override
	public void handleTransportationPacket(PacketTransportation.Message message){

		Level world = Minecraft.getInstance().world;
		BlockPos pos = message.destination;

		Entity entity = level.getEntityByID(message.dismountEntityID);
		if(message.dismountEntityID != -1 && entity != null) entity.dismountRidingEntity();

		// Moved from when the packet is sent to when it is received; fixes the sound not playing in first person.
		// Changed to a position to avoid syncing issues
		world.playSound(pos.getX(), pos.getY(), pos.getZ(), WizardrySounds.SPELL_TRANSPORTATION_TRAVEL, WizardrySounds.SPELLS, 1, 1, false);

		for(int i = 0; i < 20; i++){
			double radius = 1;
			float angle = world.random.nextFloat() * (float)Math.PI * 2;
			double x = pos.getX() + 0.5 + radius * Mth.cos(angle);
			double y = pos.getY() + world.random.nextDouble() * 2;
			double z = pos.getZ() + 0.5 + radius * Mth.sin(angle);
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.02, 0).clr(0.6f, 1, 0.6f)
			.time(80 + world.random.nextInt(10)).spawn(world);
		}
		for(int i = 0; i < 20; i++){
			double radius = 1;
			float angle = world.random.nextFloat() * (float)Math.PI * 2;
			double x = pos.getX() + 0.5 + radius * Mth.cos(angle);
			double y = pos.getY() + world.random.nextDouble() * 2;
			double z = pos.getZ() + 0.5 + radius * Mth.sin(angle);
			world.spawnParticle(ParticleTypes.VILLAGER_HAPPY, x, y, z, 0, 0.02, 0);
		}
		for(int i = 0; i < 20; i++){
			double radius = 1;
			float angle = world.random.nextFloat() * (float)Math.PI * 2;
			double x = pos.getX() + 0.5 + radius * Mth.cos(angle);
			double y = pos.getY() + world.random.nextDouble() * 2;
			double z = pos.getZ() + 0.5 + radius * Mth.sin(angle);
			world.spawnParticle(ParticleTypes.ENCHANTMENT_TABLE, x, y, z, 0, 0.02, 0);
		}
	}

	@Override
	public void handlePlayerSyncPacket(PacketPlayerSync.Message message){

		WizardData data = WizardData.get(Minecraft.getInstance().player);

		if(data != null){

			data.synchronisedRandom.setSeed(message.seed);
			data.spellsDiscovered = message.spellsDiscovered;

			message.spellData.forEach(data::setVariable);

			if(message.selectedMinionID == -1){
				data.selectedMinion = null;
			}else{
				Entity entity = Minecraft.getInstance().level.getEntityByID(message.selectedMinionID);

				if(entity instanceof ISummonedCreature){
					data.selectedMinion = new WeakReference<>((ISummonedCreature)entity);
				}else{
					data.selectedMinion = null;
				}
			}
		}
	}

	@Override
	public void handleGlyphDataPacket(PacketGlyphData.Message message){

		SpellGlyphData data = SpellGlyphData.get(Minecraft.getInstance().world);

		data.randomNames = new HashMap<>();
		data.randomDescriptions = new HashMap<>();

		for(Spell spell : Spell.getAllSpells()){

			if(spell.networkID() > message.names.size()){
				Wizardry.logger.warn("Received no glyph data for spell {}, skipping", spell.getRegistryName());
				continue;
			}

			// -1 because the none spell isn't included
			// This is a case where we must use the network ID, not the metadata
			data.randomNames.put(spell, message.names.get(spell.networkID() - 1));
			data.randomDescriptions.put(spell, message.descriptions.get(spell.networkID() - 1));
		}
	}

	@Override
	public void handleEmitterDataPacket(PacketEmitterData.Message message){
		message.emitters.forEach(e -> e.setWorld(Minecraft.getInstance().world)); // Do this as soon as possible!
		SpellEmitterData data = SpellEmitterData.get(Minecraft.getInstance().world);
		// We shouldn't need to clear the emitters because when a player logs in or changes dimension the client world
		// is wiped anyway, so the call to get() above should result in a fresh SpellEmitterData instance
		message.emitters.forEach(data::add);
	}

	@Override
	public void handleClairvoyancePacket(PacketClairvoyance.Message message){
		Clairvoyance.spawnPathPaticles(Minecraft.getInstance().world, message.path, message.durationMultiplier);
	}

	@Override
	public void handleAdvancementSyncPacket(PacketSyncAdvancements.Message message){
		GuiWizardHandbook.updateUnlockStatus(message.showToasts, message.completedAdvancements);
	}

	@Override
	public void handleResurrectionPacket(PacketResurrection.Message message){
		Entity entity = Minecraft.getInstance().level.getEntityByID(message.playerID);
		if(entity instanceof Player){
			((Resurrection)Spells.resurrection).resurrect((Player)entity);
			if(entity == Minecraft.getInstance().player){
				Minecraft.getInstance().world.addFreshEntity(entity);
				Minecraft.getInstance().displayGuiScreen(null);
			}
		}
		else Wizardry.logger.warn("Received a PacketResurrection, but the entity ID did not match any player");
	}

	@Override
	public void handlePossessionPacket(PacketPossession.Message message){

		Entity entity = Minecraft.getInstance().level.getEntityByID(message.playerID);

		if(entity instanceof Player){

			Player player = (Player)entity;

			if(message.targetID == -1){
				((Possession)Spells.possession).endPossession(player);
			}else{
				Entity target = Minecraft.getInstance().level.getEntityByID(message.targetID);
				if(target instanceof Mob){
					((Possession)Spells.possession).possess(player, (Mob)target, message.duration);
					player.sendStatusMessage(Component.translatable("spell." + Spells.possession.getRegistryName()
							+ ".success", Minecraft.getInstance().gameSettings.keyBindSneak.getDisplayName()), true);
				}
				else Wizardry.logger.warn("Received a PacketPossession, but the target ID did not match any living entity");
			}
		}
		else Wizardry.logger.warn("Received a PacketPossession, but the player ID did not match any player");
	}

	public void handleConquerShrinePacket(PacketConquerShrine.Message message){

		BlockEntity tileEntity = Minecraft.getInstance().level.getTileEntity(new BlockPos(message.x, message.y, message.z));

		if(tileEntity instanceof TileEntityShrineCore){
			((TileEntityShrineCore)tileEntity).conquer();

		}else Wizardry.logger.warn("Received a PacketConquerShrine, but there was no shrine core at the position sent");
	}

	// Rendering
	// ===============================================================================================================

	private static final ResourceLocation ICE_WRAITH_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/ice_wraith.png");
	private static final ResourceLocation LIGHTNING_WRAITH_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/lightning_wraith.png");

	/** Static instance of the statue renderer, used to access the block breaking texture. */
	public static RenderStatue renderStatue;

	@Override
	public void initialiseLayers(){
		LayerTiledOverlay.initialiseLayers(LayerStone::new);
		LayerTiledOverlay.initialiseLayers(LayerFrost::new);
		LayerTiledOverlay.initialiseLayers(LayerMindControl::new);
		LayerTiledOverlay.initialiseLayers(LayerSummonAnimation::new);
		LayerTiledOverlay.initialiseLayers(LayerDisintegrateAnimation::new);
		LayerTiledOverlay.initialiseLayers(LayerOak::new);
		LayerTiledOverlay.initialiseLayers(LayerIron::new);
		LayerTiledOverlay.initialiseLayers(LayerDiamond::new);
	}

	@Override
	public void initialiseAnimations(){
		PlayerAnimator.init();
		ActionAnimation.register();
	}

	@Override
	public void registerRenderers(){

		// Minions
		// Yet another advantage to the new system: turns out you don't even need to register the renderer if you
		// just want the vanilla one for the mob you're extending.

		// Luckily for us, the vanilla husk renderer is only parametrised to EntityZombie
		RenderingRegistry.registerEntityRenderingHandler(EntityHuskMinion.class, RenderHusk::new);
		// This now extends AbstractSkeleton so we need to bind the renderer ourselves
		RenderingRegistry.registerEntityRenderingHandler(EntitySkeletonMinion.class, RenderSkeleton::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityStrayMinion.class, RenderStrayMinion::new);

		// An anonymous class in a lambda expression! No point writing a separate class really, is there?
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningWraith.class, manager -> new RenderBlaze(manager){
			@Override
			public ResourceLocation getTextureLocation(EntityBlaze entity){
				return LIGHTNING_WRAITH_TEXTURE;
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityIceWraith.class, manager -> new RenderBlaze(manager){
			@Override
			public ResourceLocation getTextureLocation(EntityBlaze entity){
				return ICE_WRAITH_TEXTURE;
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityIceGiant.class, RenderIceGiant::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityPhoenix.class, RenderPhoenix::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySpectralGolem.class, RenderSpectralGolem::new);

		// Projectiles
		RenderingRegistry.registerEntityRenderingHandler(EntityMagicMissile.class, manager -> new RenderMagicArrow(manager,
				new ResourceLocation(Wizardry.MODID, "textures/entity/magic_missile.png"), false, 8.0, 4.0, 16, 9, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityIceShard.class, manager -> new RenderMagicArrow(manager,
				new ResourceLocation(Wizardry.MODID, "textures/entity/ice_shard.png"), false, 8.0, 2.0, 16, 5, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningArrow.class, manager -> new RenderMagicArrow(manager,
				new ResourceLocation(Wizardry.MODID, "textures/entity/lightning_arrow.png"), true, 8.0, 2.0, 16, 5, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityDart.class, manager -> new RenderMagicArrow(manager,
				new ResourceLocation(Wizardry.MODID, "textures/entity/dart.png"), false, 8.0, 2.0, 16, 5, true));
		RenderingRegistry.registerEntityRenderingHandler(EntityIceLance.class, manager -> new RenderMagicArrow(manager,
				new ResourceLocation(Wizardry.MODID, "textures/entity/ice_lance.png"), false, 16.0, 3.0, 22, 5, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityFlamecatcherArrow.class, manager -> new RenderMagicArrow(manager,
				new ResourceLocation(Wizardry.MODID, "textures/entity/flamecatcher_arrow.png"), false, 8, 2.0, 16, 5, true));
		RenderingRegistry.registerEntityRenderingHandler(EntityConjuredArrow.class, RenderConjuredArrow::new);

		RenderingRegistry.registerEntityRenderingHandler(EntityForceArrow.class, RenderForceArrow::new);

		// Creatures
		RenderingRegistry.registerEntityRenderingHandler(EntitySpiritWolf.class, RenderSpiritWolf::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySpiritHorse.class, RenderSpiritHorse::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityWizard.class, RenderWizard::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityEvilWizard.class, RenderEvilWizard::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityDecoy.class, RenderDecoy::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityRemnant.class, RenderRemnant::new);

		// Throwables
		RenderingRegistry.registerEntityRenderingHandler(EntitySparkBomb.class,
				manager -> new RenderProjectile(manager, 0.6f, new ResourceLocation(Wizardry.MODID, "textures/items/spark_bomb.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityFirebomb.class,
				manager -> new RenderProjectile(manager, 0.6f, new ResourceLocation(Wizardry.MODID, "textures/items/firebomb.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityPoisonBomb.class,
				manager -> new RenderProjectile(manager, 0.6f, new ResourceLocation(Wizardry.MODID, "textures/items/poison_bomb.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityIceCharge.class,
				manager -> new RenderProjectile(manager, 0.6f, new ResourceLocation(Wizardry.MODID, "textures/entity/ice_charge.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityForceOrb.class,
				manager -> new RenderProjectile(manager, 0.7f, new ResourceLocation(Wizardry.MODID, "textures/entity/force_orb.png"), true));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpark.class,
				manager -> new RenderProjectile(manager, 0.4f, new ResourceLocation(Wizardry.MODID, "textures/entity/spark.png"), true));
		RenderingRegistry.registerEntityRenderingHandler(EntityDarknessOrb.class,
				manager -> new RenderProjectile(manager, 0.6f, new ResourceLocation(Wizardry.MODID, "textures/entity/darkness_orb.png"), true));
		RenderingRegistry.registerEntityRenderingHandler(EntityFirebolt.class,
				manager -> new RenderProjectile(manager, 0.2f, new ResourceLocation(Wizardry.MODID, "textures/entity/firebolt.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningDisc.class,
				manager -> new RenderLightningDisc(manager, new ResourceLocation(Wizardry.MODID, "textures/entity/lightning_sigil.png"), 2.0f));
		RenderingRegistry.registerEntityRenderingHandler(EntitySmokeBomb.class,
				manager -> new RenderProjectile(manager, 0.6f, new ResourceLocation(Wizardry.MODID, "textures/items/smoke_bomb.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityEmber.class,
				manager -> new RenderProjectile(manager, 0.15f, new ResourceLocation(Wizardry.MODID, "textures/entity/ember.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityMagicFireball.class,
				manager -> new RenderProjectile(manager, 0.7f, new ResourceLocation(Wizardry.MODID, "textures/entity/fireball.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityLargeMagicFireball.class,
				manager -> new RenderProjectile(manager, 1.5f, new ResourceLocation(Wizardry.MODID, "textures/entity/fireball.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityIceball.class,
				manager -> new RenderProjectile(manager, 0.7f, new ResourceLocation(Wizardry.MODID, "textures/entity/iceball.png"), false));

		// Effects and constructs
		RenderingRegistry.registerEntityRenderingHandler(EntityBlackHole.class, RenderBlackHole::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityShield.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityBubble.class, RenderBubble::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityHammer.class, RenderHammer::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityIceSpike.class, RenderIceSpike::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityForcefield.class, RenderForcefield::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityZombieSpawner.class, RenderZombieSpawner::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityRadiantTotem.class, RenderRadiantTotem::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityBoulder.class, RenderBoulder::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityWitheringTotem.class, RenderWitheringTotem::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityIceBarrier.class, RenderIceBarrier::new);
		//RenderingRegistry.registerEntityRenderingHandler(EntityContainmentField.class, RenderContainmentField::new);

		// Stuff that doesn't render
		RenderingRegistry.registerEntityRenderingHandler(EntityBlizzard.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTornado.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityArrowRain.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityShadowWraith.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityThunderbolt.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityStormElemental.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityEarthquake.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityHailstorm.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityStormcloud.class, RenderBlank::new);

		// Runes on ground
		RenderingRegistry.registerEntityRenderingHandler(EntityHealAura.class,
				manager -> new RenderSigil(manager, new ResourceLocation(Wizardry.MODID, "textures/entity/healing_aura.png"), 0.3f, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityFireSigil.class,
				manager -> new RenderSigil(manager, new ResourceLocation(Wizardry.MODID, "textures/entity/fire_sigil.png"), 0, true));
		RenderingRegistry.registerEntityRenderingHandler(EntityFrostSigil.class,
				manager -> new RenderSigil(manager, new ResourceLocation(Wizardry.MODID, "textures/entity/frost_sigil.png"), 0, true));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningSigil.class,
				manager -> new RenderSigil(manager, new ResourceLocation(Wizardry.MODID, "textures/entity/lightning_sigil.png"), 0, true));
		RenderingRegistry.registerEntityRenderingHandler(EntityFireRing.class,
				manager -> new RenderFireRing(manager, new ResourceLocation(Wizardry.MODID, "textures/entity/ring_of_fire.png")));
		RenderingRegistry.registerEntityRenderingHandler(EntityDecay.class, RenderDecay::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityCombustionRune.class,
				manager -> new RenderSigil(manager, new ResourceLocation(Wizardry.MODID, "textures/entity/combustion_rune.png"), 0, true));

		// TESRs
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityArcaneWorkbench.class, new RenderArcaneWorkbench());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStatue.class, renderStatue = new RenderStatue());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMagicLight.class, new RenderMagicLight());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLectern.class, new RenderLectern());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityImbuementAltar.class, new RenderImbuementAltar());

	}
}