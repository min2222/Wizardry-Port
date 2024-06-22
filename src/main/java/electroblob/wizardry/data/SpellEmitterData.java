package electroblob.wizardry.data;

import java.util.ArrayList;
import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.command.SpellEmitter;
import electroblob.wizardry.packet.PacketEmitterData;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Class responsible for storing and keeping track of {@link SpellEmitter}s. Each world has its own instance of
 * {@code SpellEmitterData} which can be retrieved using {@link SpellEmitterData#get(Level)}.<br>
 * <br>
 * To add a new {@code SpellEmitter}, use {@link SpellEmitter#add(Spell, Level, double, double, double, Direction, int, SpellModifiers)}.
 *
 * @since Wizardry 4.2
 * @author Electroblob
 */
@Mod.EventBusSubscriber
public class SpellEmitterData extends SavedData {

	public static final String NAME = Wizardry.MODID + "_spell_emitters";

	private final List<SpellEmitter> emitters = new ArrayList<>();

	private ListTag emitterTags = null;

	// Required constructors
	public SpellEmitterData(){
		this(NAME);
	}

	public SpellEmitterData(String name){
		
	}

	/** Returns the spell emitter data for this world, or creates a new instance if it doesn't exist yet. */
	public static SpellEmitterData get(ServerLevel world){

		SpellEmitterData instance = (SpellEmitterData)world.getDataStorage().get(SpellEmitterData::readFromNBT, NAME);

		if(instance == null){
			instance = new SpellEmitterData();
			world.getDataStorage().set(NAME, instance);
		}else if(instance.emitters.isEmpty() && instance.emitterTags != null){
			instance.loadEmitters(world);
		}

		return instance;
	}

	/** Sends the active spell emitters for this world to the specified player's client. */
	public void sync(ServerPlayer player){
		PacketEmitterData.Message msg = new PacketEmitterData.Message(emitters);
		WizardryPacketHandler.net.send(PacketDistributor.PLAYER.with(() -> player), msg);
		Wizardry.logger.info("Synchronising spell emitters for " + player.getName());
	}

	/** Adds the given {@link SpellEmitter} to the list of emitters for this {@code SpellEmitterData}. */
	public void add(SpellEmitter emitter){
		emitters.add(emitter);
		setDirty();
	}

    public static SpellEmitterData readFromNBT(CompoundTag nbt) {
        SpellEmitterData data = new SpellEmitterData();
        data.emitterTags = nbt.getList("emitters", Tag.TAG_COMPOUND);
        return data;
	}

	private void loadEmitters(Level world){
		emitters.clear();
		emitters.addAll(NBTExtras.NBTToList(emitterTags, (CompoundTag t) -> SpellEmitter.fromNBT(world, t)));
		emitterTags = null; // Now we know it's loaded
	}

	@Override
	public CompoundTag save(CompoundTag compound){
		NBTExtras.storeTagSafely(compound, "emitters", NBTExtras.listToNBT(emitters, SpellEmitter::toNBT));
		return compound;
	}

	public static void update(ServerLevel world){
		SpellEmitterData data = SpellEmitterData.get(world);
		if(!data.emitters.isEmpty()){
			data.emitters.forEach(SpellEmitter::update);
			data.emitters.removeIf(SpellEmitter::needsRemoving);
			data.setDirty(); // Mark dirty if there are changes to be saved
		}
	}

	@SubscribeEvent
	public static void tick(TickEvent.LevelTickEvent event){
		if(!event.level.isClientSide && event.phase == TickEvent.Phase.END){
			update((ServerLevel) event.level);
		}
	}

	@SubscribeEvent
	public static void onWorldLoadEvent(LevelEvent.Load event){
		// Called to initialise the spell emitter data when a world loads, if it isn't already.
		if(!(event.getLevel() instanceof ServerLevel))
			return;
		SpellEmitterData.get((ServerLevel) event.getLevel());
	}

	@SubscribeEvent
	public static void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event){
		// Needs to be done here as well as PlayerLoggedInEvent because SpellEmitterData is dimension-specific
		if(event.getEntity() instanceof ServerPlayer){
			SpellEmitterData.get((ServerLevel) event.getEntity().level).sync((ServerPlayer)event.getEntity());
		}
	}

}
