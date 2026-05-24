/**
 * This class was created by <Mikeemoo/boq/nevercast>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [? (GMT)]
 */
package vazkii.botania.common.core.helper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.*;
import vazkii.botania.api.corporea.InvWithLocation;
import vazkii.botania.common.block.tile.TileSimpleInventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class InventoryHelper {

	public static InvWithLocation getInventoryWithLocation(World world, BlockPos pos, EnumFacing side) {
		IItemHandler ret = getInventory(world, pos, side);
		if(ret == null)
			return null;
		else return new InvWithLocation(ret, world, pos);
	}

	public static IItemHandler getInventory(World world, BlockPos pos, EnumFacing side) {
		TileEntity te = world.getTileEntity(pos);

		if(te == null)
			return null;

		IItemHandler ret = te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) ?
				te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) : null;

				if(ret == null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
					ret = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

				return ret;
	}

	public static void dropInventory(TileSimpleInventory inv, World world, IBlockState state, BlockPos pos) {
		if(inv != null) {
			for(int j1 = 0; j1 < inv.getSizeInventory(); ++j1) {
				ItemStack itemstack = inv.getItemHandler().getStackInSlot(j1);

				if(!itemstack.isEmpty()) {
					net.minecraft.inventory.InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemstack);
				}
			}

			world.updateComparatorOutputLevel(pos, state.getBlock());
		}
	}

	public static void withdrawFromInventory(TileSimpleInventory inv, EntityPlayer player) {
		for(int i = inv.getSizeInventory() - 1; i >= 0; i--) {
			ItemStack stackAt = inv.getItemHandler().getStackInSlot(i);
			if(!stackAt.isEmpty()) {
				ItemStack copy = stackAt.copy();
				ItemHandlerHelper.giveItemToPlayer(player, copy);
				inv.getItemHandler().setStackInSlot(i, ItemStack.EMPTY);
				player.world.updateComparatorOutputLevel(inv.getPos(), null);
				break;
			}
		}
	}

	public static String stringifyStack(@Nonnull ItemStack s) {
		String itemId = Objects.requireNonNull(s.getItem().getRegistryName()).toString();
		if (s.getMetadata() != 0) itemId += "@" + s.getMetadata();
		return itemId;
	}

	public static @Nullable ItemStack destringifyStack(String s) {
		int meta = 0;
		String[] split = s.split("@");
		if (split.length > 1) {
			meta = Integer.parseInt(split[1]);
			s = split[0];
		}
		ResourceLocation rl = new ResourceLocation(s);
		Item it = Item.REGISTRY.getObject(rl);
		if (it != null) {
			return new ItemStack(it, 1, meta);
		}
		return null;
	}

	public static List<String> expandMetaInStringifiedStacks(List<String> input) {
		List<String> output = new ArrayList<>();
		output.addAll(input);
		for (String x : input) {
			if (x.endsWith("@0")) {
				output.add(x.substring(0, x.length() - 2));
			}
		}
		return output;
	}

	public static List<String> expandMetaInStringifiedStacks(String[] input) {
		return expandMetaInStringifiedStacks(Arrays.asList(input));
	}

	public static void resizeInventory(ItemStackHandler inv, int newSize) {
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < inv.getSlots(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			stacks.add(stack);
		}
		inv.setSize(newSize);
		for (int i = 0; i < newSize && i < stacks.size(); i++) {
			inv.setStackInSlot(i, stacks.get(i));
		}
	}

}
