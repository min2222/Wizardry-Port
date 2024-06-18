package electroblob.wizardry.advancement;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.gson.JsonObject;

import electroblob.wizardry.Wizardry;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * This class implements a generic custom advancement trigger that can be fired from any point in
 * the code. This replaces the achievement system in instances where the JSON advancement descriptions
 * cannot properly capture the advancement-worthy events. Where possible, advancement conditions
 * should be triggered by JSON descriptions and vanilla advancement triggers.
 *
 * @author 12foo
 * @since 4.1.0
 */
public class CustomAdvancementTrigger implements CriterionTrigger<CustomAdvancementTrigger.Instance> {

    private final ResourceLocation id;
    private final SetMultimap<PlayerAdvancements, Listener<? extends CriterionTriggerInstance>> listeners = HashMultimap.create();

    /**
     * This is a dummy criterion instance that does nothing on its own (but it is bound to this
     * trigger, and via listeners to the player). We later fire this manually when we want the
     * advancement to happen.
     */
    public static class Instance extends AbstractCriterionTriggerInstance {
        public Instance(ResourceLocation triggerId, JsonObject json, DeserializationContext context) {
            super(triggerId, EntityPredicate.Composite.fromJson(json, "entity", context));
        }
    }

    public CustomAdvancementTrigger(String name) {
        super();
        id = new ResourceLocation(Wizardry.MODID, name);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener) {
        listeners.put(playerAdvancementsIn, listener);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener) {
        listeners.remove(playerAdvancementsIn, listener);
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements playerAdvancementsIn) {
        listeners.removeAll(playerAdvancementsIn);
    }

    @Override
    public Instance createInstance(JsonObject json, DeserializationContext context) {
        // Every time a trigger with this name is deserialized from the JSON, we just return a new
        // dummy criterion instance.
        return new CustomAdvancementTrigger.Instance(id, json, context);
    }

    public void triggerFor(Player player) {
        // Fire our dummy criterion manually on all advancements of the player, thereby granting
        // the ones that match it.
        if (player instanceof ServerPlayer) {
            final PlayerAdvancements advances = ((ServerPlayer) player).getAdvancements();
            listeners.get(advances).forEach((listener) -> listener.run(advances));
        }
    }
}
