package electroblob.wizardry.advancement;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import electroblob.wizardry.worldgen.WorldGenWizardryStructure;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

/** Copied from PositionTrigger and modified to work with wizardry's structures. The majority of any
 * ICriterionTrigger class is just boilerplate, and this is no exception. */
public class StructureTrigger implements CriterionTrigger<StructureTrigger.Instance> {

	private final ResourceLocation id;
	private final Map<PlayerAdvancements, StructureTrigger.Listeners> listeners = Maps.newHashMap();

	public StructureTrigger(ResourceLocation id){
		this.id = id;
	}

	public ResourceLocation getId(){
		return this.id;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements advancements, Listener<StructureTrigger.Instance> listener){

		StructureTrigger.Listeners listeners = this.listeners.get(advancements);

		if(listeners == null){
			listeners = new StructureTrigger.Listeners(advancements);
			this.listeners.put(advancements, listeners);
		}

		listeners.add(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements advancements, Listener<StructureTrigger.Instance> listener){

		StructureTrigger.Listeners listeners = this.listeners.get(advancements);

		if(listeners != null){
			listeners.remove(listener);

			if(listeners.isEmpty()){
				this.listeners.remove(advancements);
			}
		}
	}

	@Override
	public void removePlayerListeners(PlayerAdvancements advancements){
		this.listeners.remove(advancements);
	}

	@Override
	public StructureTrigger.Instance createInstance(JsonObject json, DeserializationContext context){
		return new StructureTrigger.Instance(this.id, GsonHelper.getAsString(json, "structure_type"), json, context);
	}

	public void trigger(ServerPlayer player){

		StructureTrigger.Listeners listeners = this.listeners.get(player.getAdvancements());

		if(listeners != null){
			listeners.trigger(player.getLevel(), player.getX(), player.getY(), player.getZ());
		}
	}

	public static class Instance extends AbstractCriterionTriggerInstance {

		private final WorldGenWizardryStructure structureType;

		public Instance(ResourceLocation criterionIn, String name, JsonObject json, DeserializationContext context){
			super(criterionIn, EntityPredicate.Composite.fromJson(json, name, context));
			this.structureType = WorldGenWizardryStructure.byName(name);
		}

		public boolean test(ServerLevel world, double x, double y, double z){
			return structureType.isInsideStructure(world, x, y, z);
		}
	}

	static class Listeners {

		private final PlayerAdvancements playerAdvancements;
		private final Set<Listener<StructureTrigger.Instance>> listeners = Sets.newHashSet();

		public Listeners(PlayerAdvancements advancements){
			this.playerAdvancements = advancements;
		}

		public boolean isEmpty(){
			return this.listeners.isEmpty();
		}

		public void add(Listener<StructureTrigger.Instance> listener){
			this.listeners.add(listener);
		}

		public void remove(Listener<StructureTrigger.Instance> listener){
			this.listeners.remove(listener);
		}

		public void trigger(ServerLevel world, double x, double y, double z){

			List<Listener<StructureTrigger.Instance>> list = null;

			for(Listener<StructureTrigger.Instance> listener : this.listeners){

				if(listener.getTriggerInstance().test(world, x, y, z)){

					if(list == null){
						list = Lists.newArrayList();
					}

					list.add(listener);
				}
			}

			if(list != null){
				for(Listener<StructureTrigger.Instance> listener : list){
					listener.run(this.playerAdvancements);
				}
			}
		}
	}
}