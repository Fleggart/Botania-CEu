package vazkii.botania.common.crafting;

import net.minecraft.item.ItemStack;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.ModItems;

public class ModTerrestrialAgglomerationRecipes {
    public static void init() {
        BotaniaAPI.registerTerraPlateRecipe(
                500000,
                new ItemStack(ModItems.manaResource, 1, 4), // Terrasteel
                new Object[] {
                        new ItemStack(ModItems.manaResource, 1, 0), // Manasteel
                        new ItemStack(ModItems.manaResource, 1, 1), // Mana Pearl
                        new ItemStack(ModItems.manaResource, 1, 2) // Mana Diamond
                });
    }
}
