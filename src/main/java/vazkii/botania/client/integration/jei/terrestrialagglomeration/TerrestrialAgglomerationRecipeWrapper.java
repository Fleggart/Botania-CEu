// Copied with modifications from Botania Tweaks, which is licensed under MPL
// Original code can be obtained here: https://github.com/quat1024/BotaniaTweaks

package vazkii.botania.client.integration.jei.terrestrialagglomeration;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.api.recipe.RecipeTerrestrialAgglomeration;
import vazkii.botania.client.core.handler.HUDHandler;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.core.handler.ConfigHandler;

public class TerrestrialAgglomerationRecipeWrapper implements IRecipeWrapper {

    RecipeTerrestrialAgglomeration recipe;

    List<List<ItemStack>> inputs;
    List<ItemStack> outputs;

    ItemStack multiblockCenterStack;
    ItemStack multiblockEdgeStack;
    ItemStack multiblockCornerStack;

    // yeah i know it's stupid

    @Nullable
    ItemStack multiblockReplaceCenterStack;
    @Nullable
    ItemStack multiblockReplaceEdgeStack;
    @Nullable
    ItemStack multiblockReplaceCornerStack;

    int manaCost;

    public TerrestrialAgglomerationRecipeWrapper(RecipeTerrestrialAgglomeration recipe) {
        this.recipe = recipe;
        ImmutableList.Builder<List<ItemStack>> inputs_ = ImmutableList.builder();

        // Itemstack inputs
        for (ItemStack stack : recipe.getRecipeStacks()) {
            inputs_.add(ImmutableList.of(stack));
        }

        // Ore key inputs
        for (String key : recipe.getRecipeOreKeys()) {
            inputs_.add(ImmutableList.copyOf(OreDictionary.getOres(key)));
        }

        // The three multiblock pieces
        multiblockCenterStack = stackFromState(recipe.multiblockCenter);
        multiblockEdgeStack = stackFromState(recipe.multiblockEdge);
        multiblockCornerStack = stackFromState(recipe.multiblockCorner);

        inputs_.add(ImmutableList.of(multiblockCenterStack));
        inputs_.add(ImmutableList.of(multiblockEdgeStack));
        inputs_.add(ImmutableList.of(multiblockCornerStack));

        ImmutableList.Builder<ItemStack> outputs_ = ImmutableList.builder();

        // Recipe output
        outputs_.add(recipe.getRecipeOutputCopy());

        // The multiblock replacements
        if (recipe.multiblockCenterReplace != null) {
            multiblockReplaceCenterStack = stackFromState(recipe.multiblockCenterReplace);
            outputs_.add(multiblockReplaceCenterStack);
        }

        if (recipe.multiblockEdgeReplace != null) {
            multiblockReplaceEdgeStack = stackFromState(recipe.multiblockEdgeReplace);
            outputs_.add(multiblockReplaceEdgeStack);
        }

        if (recipe.multiblockCornerReplace != null) {
            multiblockReplaceCornerStack = stackFromState(recipe.multiblockCornerReplace);
            outputs_.add(multiblockReplaceCornerStack);
        }

        inputs = inputs_.build();
        outputs = outputs_.build();

        manaCost = recipe.manaCost;
    }

    @Override
    public void getIngredients(IIngredients ing) {
        ing.setInputLists(VanillaTypes.ITEM, inputs);
        ing.setOutputs(VanillaTypes.ITEM, outputs);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        GlStateManager.enableAlpha();
        HUDHandler.renderManaBar(35, 60, 0x0000FF, 0.75f, manaCost, TilePool.MAX_MANA);

        if (ConfigHandler.showManaNumbers) {
            minecraft.fontRenderer.drawString(Integer.toString(manaCost) + " mana", 36, 51, 0);
        } else if (manaCost > 1_000_000) {
            int roughPoolCount = (250_000 * Math.round(manaCost / 250_000f)) / 1_000_000;
            Minecraft.getMinecraft().fontRenderer.drawString("x" + roughPoolCount, 140, 58, 0x000000);
        }

        GlStateManager.disableAlpha();
    }

    public static ItemStack stackFromState(IBlockState state) {
        if (state == null)
            return ItemStack.EMPTY;
        Block block = state.getBlock();

        try {
            // this is a forge added method that takes a world, hit vector, few other things
            // and we don't have any of those!
            // so let's just null them all and hope for the best
            // (usually this delegates to a vanilla method that just returns a new itemstack
            // with item.getitemfromblock and whatever datavalue damageDropped gives)
            return block.getPickBlock(state, null, null, null, null); // Ughhhh
        } catch (Exception e) {
            // oof
        }

        // ok that didn't work, as a last-ditch effort try emulating vanilla
        // block#getitem
        Item item = Item.getItemFromBlock(block);
        if (item == Items.AIR)
            return ItemStack.EMPTY;
        else
            return new ItemStack(item, 1, block.getMetaFromState(state));
    }
}
