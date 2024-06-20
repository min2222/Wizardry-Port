package electroblob.wizardry;

import electroblob.wizardry.inventory.ContainerArcaneWorkbench;
import electroblob.wizardry.inventory.ContainerBookshelf;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class WizardryGuiHandler {
	
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Wizardry.MODID);

    public static final RegistryObject<MenuType<ContainerArcaneWorkbench>> ARCANE_WORKBENCH_MENU = MENUS.register("arcane_workbench_container", () -> IForgeMenuType.create(ContainerArcaneWorkbench::new));
    public static final RegistryObject<MenuType<ContainerBookshelf>> BOOKSHELF_MENU = MENUS.register("bookshelf_container", () -> IForgeMenuType.create(ContainerBookshelf::new));
}