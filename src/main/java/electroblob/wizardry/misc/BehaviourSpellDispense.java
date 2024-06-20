package electroblob.wizardry.misc;

import electroblob.wizardry.data.DispenserCastingData;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.legacy.IMetadata;
import electroblob.wizardry.packet.PacketDispenserCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;

/**
 * Dispenser behaviour for casting spells from dispensers based on the metadata of the dispensed item. This class, along
 * with the capability {@link DispenserCastingData DispenserCastingData}, forms the dispenser
 * equivalent of {@link electroblob.wizardry.entity.living.EntityAIAttackSpell EntityAIAttackSpell}, which handles spell
 * casting for NPCs.
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 */
/*
 * The dispenser and NPC casting systems are somewhat comparable in structure:
 * 
 | Dispensers				| NPCs						|
 | -------------------------|---------------------------|
 | BehaviourSpellDispense	| EntityAIAttackSpell		| Handles the actual spell casting. These are slightly different
 | 			|				|		|					| in that EntityAIAttackSpell handles ticking for NPC spells,
 | 			|				|		|					| whereas for dispensers this is handled in DispenserCastingData.
 | 			V				|		V					|
 | DispenserCastingData		| ISpellCaster				| Deals with data storage for spell casting. Of course, being an
 | 							|							| interface, ISpellCaster doesn't actually store the data itself.
 * 
 */
public class BehaviourSpellDispense extends OptionalDispenseItemBehavior {

	public BehaviourSpellDispense(){}
	
	@Override
	protected ItemStack execute(BlockSource source, ItemStack stack){
		
		// This is only ever called server-side.
		
		setSuccess(false);
		
		Level world = source.getLevel();
		// This returns a position that is 0.2 blocks away from the middle of the front face of the dispenser
		Position position = DispenserBlock.getDispensePosition(source);
		Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
		
		Spell spell = Spell.byMetadata(((IMetadata) stack.getItem()).getMetadata(stack));

		// If there's a block in the way, nothing happens
		if(world.getBlockState(source.getPos()).isFaceSturdy(world, source.getPos().relative(direction), direction.getOpposite())) return stack;
		
		// If the scroll can never be cast by a dispenser, it should be dispensed as an item.
		if(!spell.canBeCastBy(source.getEntity())) return super.execute(source, stack);
		
		SpellModifiers modifiers = new SpellModifiers();
		
		double x = position.x();
		double y = position.y();
		double z = position.z();
		
		// For horizontal dispensers, the position is lowered by 0.125 so it actually lines up with the hole.
		if(direction.getAxis().isHorizontal()) y -= 0.125;
		
		// If the scroll can be cast by a dispenser, it should be cast. If this fails, then the scroll should stay in
		// the dispenser.
		if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Pre(Source.DISPENSER, spell, world, x, y, z, direction, modifiers)))
			return stack;
		
		setSuccess(spell.cast(world, x, y, z, direction, 0, -1, modifiers));
		
		if(isSuccess()){
			
			MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.DISPENSER, spell, world, x, y, z, direction, modifiers));
			
			stack.shrink(1);
		    
			if(spell.isContinuous || spell.requiresPacket()){
				// Sends a packet to all players in dimension to tell them to spawn particles.
				PacketDispenserCastSpell.Message msg = new PacketDispenserCastSpell.Message(x, y, z, direction, source.getPos(), spell,
						spell.isContinuous ? ItemScroll.CASTING_TIME : 0, modifiers); // Non-continuous spells ignore duration
				WizardryPacketHandler.net.send(PacketDistributor.DIMENSION.with(() -> world.dimension()), msg);
			}
			
			if(spell.isContinuous){
				DispenserCastingData data = DispenserCastingData.get(source.getEntity());
				data.startCasting(spell, x, y, z, ItemScroll.CASTING_TIME, modifiers);
			}
		}
		
	    return stack;
	}

}
