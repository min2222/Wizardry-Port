package electroblob.wizardry.advancement;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import electroblob.wizardry.spell.Spell;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Advancement trigger which is triggered when a spell is cast. The majority of any
 * ICriterionTrigger class is just boilerplate, and this is no exception. */
public class SpellCastTrigger implements CriterionTrigger<SpellCastTrigger.Instance> {
    private final ResourceLocation id;
    private final Map<PlayerAdvancements, SpellCastTrigger.Listeners> listeners = Maps.newHashMap();

    public SpellCastTrigger(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public void addPlayerListener(PlayerAdvancements advancements, Listener<SpellCastTrigger.Instance> listener) {
        SpellCastTrigger.Listeners listeners = this.listeners.get(advancements);

        if (listeners == null) {
            listeners = new SpellCastTrigger.Listeners(advancements);
            this.listeners.put(advancements, listeners);
        }

        listeners.add(listener);
    }

    public void removePlayerListener(PlayerAdvancements advancements, Listener<SpellCastTrigger.Instance> listener) {
        SpellCastTrigger.Listeners listeners = this.listeners.get(advancements);

        if (listeners != null) {
            listeners.remove(listener);

            if (listeners.isEmpty()) {
                this.listeners.remove(advancements);
            }
        }
    }

    public void removePlayerListeners(PlayerAdvancements advancements) {
        this.listeners.remove(advancements);
    }

    public SpellCastTrigger.Instance createInstance(JsonObject json, DeserializationContext context) {
        return new SpellCastTrigger.Instance(this.id, SpellPredicate.deserialize(json.get("spell")), ItemPredicate.fromJson(json.get("item")), json, context);
    }

    public void trigger(ServerPlayer player, Spell spell, ItemStack stack) {
        SpellCastTrigger.Listeners listeners = this.listeners.get(player.getAdvancements());

        if (listeners != null) {
            listeners.trigger(spell, stack);
        }
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final SpellPredicate spell;
        private final ItemPredicate item;

        public Instance(ResourceLocation criterion, SpellPredicate spell, ItemPredicate item, JsonObject json, DeserializationContext context) {
			super(criterion, EntityPredicate.Composite.fromJson(json, "spell", context));
            this.spell = spell;
            this.item = item;
        }

        public boolean test(Spell spell, ItemStack stack) {
            return this.spell.test(spell) && item.matches(stack);
        }
    }

    static class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<SpellCastTrigger.Instance>> listeners = Sets.newHashSet();

        public Listeners(PlayerAdvancements advancements) {
            this.playerAdvancements = advancements;
        }

        public boolean isEmpty() {

            return this.listeners.isEmpty();
        }

        public void add(Listener<SpellCastTrigger.Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(Listener<SpellCastTrigger.Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger(Spell spell, ItemStack stack) {
            List<Listener<SpellCastTrigger.Instance>> list = null;

            for (Listener<SpellCastTrigger.Instance> listener : this.listeners) {
                if (listener.getTriggerInstance().test(spell, stack)) {
                    if (list == null) {
                        list = Lists.newArrayList();
                    }

                    list.add(listener);
                }
            }

            if (list != null) {
                for (Listener<SpellCastTrigger.Instance> listener : list) {
                    listener.run(this.playerAdvancements);
                }
            }
        }
    }
}