package electroblob.wizardry.advancement;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import electroblob.wizardry.event.DiscoverSpellEvent;
import electroblob.wizardry.spell.Spell;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

/** Advancement trigger that is triggered when a spell is discovered. The majority of any
 * ICriterionTrigger class is just boilerplate, and this is no exception. */
public class SpellDiscoveryTrigger implements CriterionTrigger<SpellDiscoveryTrigger.Instance> {
    private final ResourceLocation id;
    private final Map<PlayerAdvancements, SpellDiscoveryTrigger.Listeners> listeners = Maps.newHashMap();

    public SpellDiscoveryTrigger(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public void addPlayerListener(PlayerAdvancements advancements, Listener<SpellDiscoveryTrigger.Instance> listener) {
        SpellDiscoveryTrigger.Listeners listeners = this.listeners.get(advancements);

        if (listeners == null) {
            listeners = new SpellDiscoveryTrigger.Listeners(advancements);
            this.listeners.put(advancements, listeners);
        }

        listeners.add(listener);
    }

    public void removePlayerListener(PlayerAdvancements advancements, Listener<SpellDiscoveryTrigger.Instance> listener) {
        SpellDiscoveryTrigger.Listeners listeners = this.listeners.get(advancements);

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

    public SpellDiscoveryTrigger.Instance createInstance(JsonObject json, DeserializationContext context) {
        String s = GsonHelper.getAsString(json, "source");
        DiscoverSpellEvent.Source source = DiscoverSpellEvent.Source.byName(s);
        if (source == null) throw new JsonSyntaxException("No such spell discovery source: " + s);
        return new SpellDiscoveryTrigger.Instance(this.id, SpellPredicate.deserialize(json.get("spell")), source, json, context);
    }

    public void trigger(ServerPlayer player, Spell spell, DiscoverSpellEvent.Source source) {
        SpellDiscoveryTrigger.Listeners listeners = this.listeners.get(player.getAdvancements());

        if (listeners != null) {
            listeners.trigger(spell, source);
        }
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final SpellPredicate spell;
        private final DiscoverSpellEvent.Source source;

        public Instance(ResourceLocation criterion, SpellPredicate spell, DiscoverSpellEvent.Source source, JsonObject json, DeserializationContext context) {
			super(criterion, EntityPredicate.Composite.fromJson(json, "source", context));
            this.spell = spell;
            this.source = source;
        }

        public boolean test(Spell spell, DiscoverSpellEvent.Source source) {
            return this.spell.test(spell) && source == this.source;
        }
    }

    static class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<SpellDiscoveryTrigger.Instance>> listeners = Sets.newHashSet();

        public Listeners(PlayerAdvancements advancements) {
            this.playerAdvancements = advancements;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(Listener<SpellDiscoveryTrigger.Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(Listener<SpellDiscoveryTrigger.Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger(Spell spell, DiscoverSpellEvent.Source source) {
            List<Listener<SpellDiscoveryTrigger.Instance>> list = null;

            for (Listener<SpellDiscoveryTrigger.Instance> listener : this.listeners) {
                if (listener.getTriggerInstance().test(spell, source)) {
                    if (list == null) {
                        list = Lists.newArrayList();
                    }

                    list.add(listener);
                }
            }

            if (list != null) {
                for (Listener<SpellDiscoveryTrigger.Instance> listener : list) {
                    listener.run(this.playerAdvancements);
                }
            }
        }
    }
}