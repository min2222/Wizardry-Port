package electroblob.wizardry.item;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.world.item.Item;

public class ItemSpectralDust extends Item {

	private Element element;
	
	public ItemSpectralDust(Element element){
        super(new Item.Properties().tab(WizardryTabs.WIZARDRY));
        this.element = element;
    }
	
    public Element getElement() {
    	return this.element;
    }
}
