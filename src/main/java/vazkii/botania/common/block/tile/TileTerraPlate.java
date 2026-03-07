/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Nov 8, 2014, 5:25:32 PM (GMT)]
 */
package vazkii.botania.common.block.tile;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.lexicon.multiblock.Multiblock;
import vazkii.botania.api.lexicon.multiblock.MultiblockSet;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.api.recipe.RecipeTerrestrialAgglomeration;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.core.handler.ModSounds;
import vazkii.botania.common.network.PacketBotaniaEffect;
import vazkii.botania.common.network.PacketHandler;

public class TileTerraPlate extends TileMod implements ISparkAttachable, ITickable {

	private static final BlockPos[] SIDE_BLOCKS = {
			new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
			new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)
	};

	private static final BlockPos[] CORNER_BLOCKS = {
			new BlockPos(1, 0, 1),
			new BlockPos(1, 0, -1), new BlockPos(-1, 0, 1),
			new BlockPos(-1, 0, -1)
	};

	private static final BlockPos[] CENTER_BLOCKS = { new BlockPos(0, 0, 0) };

	private static final String TAG_MANA = "mana";

	int mana;

	public static MultiblockSet makeMultiblockSet() {
		Multiblock mb = new Multiblock();

		for (BlockPos relativePos : SIDE_BLOCKS)
			mb.addComponent(relativePos, Blocks.LAPIS_BLOCK.getDefaultState());
		for (BlockPos relativePos : CORNER_BLOCKS)
			mb.addComponent(relativePos, ModBlocks.livingrock.getDefaultState());
		for (BlockPos relativePos : CENTER_BLOCKS)
			mb.addComponent(relativePos, ModBlocks.livingrock.getDefaultState());

		mb.addComponent(new BlockPos(0, 1, 0), ModBlocks.terraPlate.getDefaultState());
		mb.setRenderOffset(new BlockPos(0, 1, 0));

		return mb.makeSet();
	}

	@Override
	public void update() {
		if (world.isRemote)
			return;

		boolean removeMana = true;

		List<EntityItem> items = getItems();
		RecipeTerrestrialAgglomeration recipe = findRecipe(items);
		if (recipe != null) {
			removeMana = false;
			ISparkEntity spark = getAttachedSpark();
			if (spark != null) {
				List<ISparkEntity> sparkEntities = SparkHelper.getSparksAround(world, pos.getX() + 0.5,
						pos.getY() + 0.5, pos.getZ() + 0.5);
				for (ISparkEntity otherSpark : sparkEntities) {
					if (spark == otherSpark)
						continue;

					if (otherSpark.getAttachedTile() != null && otherSpark.getAttachedTile() instanceof IManaPool)
						otherSpark.registerTransfer(spark);
				}
			}
			if (mana > 0) {
				float progress = ((float) mana) / recipe.getManaCost();
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, pos);
				PacketHandler.sendToNearby(world, getPos(),
						new PacketBotaniaEffect(PacketBotaniaEffect.EffectType.TERRA_PLATE,
								getPos().getX(), getPos().getY(), getPos().getZ(),
								recipe.color1, recipe.color2, (int) (progress * 100)));
			}

			if (mana >= recipe.manaCost) {
				EntityItem item = items.get(0);
				for (EntityItem otherItem : items)
					if (otherItem != item)
						otherItem.setDead();
					else
						item.setItem(recipe.getRecipeOutputCopy());
				world.playSound(null, item.posX, item.posY, item.posZ, ModSounds.terrasteelCraft,
						SoundCategory.BLOCKS, 1, 1);
				mana = 0;
				replacePlatform(recipe);
				world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, pos);
			}
		}

		if (removeMana)
			recieveMana(-1000);
	}

	List<EntityItem> getItems() {
		return world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos, pos.add(1, 1, 1)));
	}

	boolean hasValidPlatform(RecipeTerrestrialAgglomeration recipe) {
		return checkAll(CENTER_BLOCKS, recipe.multiblockCenter)
				&& checkAll(SIDE_BLOCKS, recipe.multiblockEdge)
				&& checkAll(CORNER_BLOCKS, recipe.multiblockCorner);
	}

	void replacePlatform(RecipeTerrestrialAgglomeration recipe) {
		for (BlockPos delta : CENTER_BLOCKS) {
			replaceBlock(delta, recipe.multiblockCenterReplace);
		}
		for (BlockPos delta : SIDE_BLOCKS) {
			replaceBlock(delta, recipe.multiblockEdgeReplace);
		}
		for (BlockPos delta : CORNER_BLOCKS) {
			replaceBlock(delta, recipe.multiblockCornerReplace);
		}
	}

	boolean checkAll(BlockPos[] relPositions, IBlockState block) {
		for (BlockPos position : relPositions) {
			if (!checkPlatform(position.getX(), position.getZ(), block))
				return false;
		}

		return true;
	}

	private boolean areStatesSimilar(IBlockState a, IBlockState b) {
		if (a.getBlock() != b.getBlock())
			return false;
		else
			return equalizeDirectionProperties(a).equals(equalizeDirectionProperties(b));
	}

	@SuppressWarnings("unchecked")
	private IBlockState equalizeDirectionProperties(IBlockState state) {
		Collection<IProperty<?>> props = state.getPropertyKeys();
		if (props.contains(BlockDirectional.FACING))
			return state.withProperty(BlockDirectional.FACING, EnumFacing.NORTH);
		if (props.contains(BotaniaStateProps.FACING))
			return state.withProperty(BotaniaStateProps.FACING, EnumFacing.NORTH);

		for (IProperty<?> prop : props) {
			if (prop.getValueClass() != EnumFacing.class)
				continue;
			if (!prop.getAllowedValues().contains(EnumFacing.NORTH))
				continue;
			state = state.withProperty((IProperty<EnumFacing>) prop, EnumFacing.NORTH);
		}

		return state;
	}

	boolean checkPlatform(int xOff, int zOff, IBlockState block) {
		return areStatesSimilar(world.getBlockState(pos.add(xOff, -1, zOff)), block);
	}

	void replaceBlock(BlockPos delta, @Nullable IBlockState target) {
		if (target != null) {
			BlockPos targetPos = pos.add(delta).add(0, -1, 0);
			world.playEvent(2001, targetPos, Block.getStateId(world.getBlockState(pos)));
			world.setBlockState(targetPos, target, 3);
		}
	}

	RecipeTerrestrialAgglomeration findRecipe(List<EntityItem> items) {
		for (RecipeTerrestrialAgglomeration r : BotaniaAPI.terraPlateRecipes) {
			if (r.itemsMatch(items.stream().map(x -> x.getItem()).collect(Collectors.toList()))
					&& hasValidPlatform(r)) {
				return r;
			}
		}
		return null;
	}

	@Override
	public void writePacketNBT(NBTTagCompound cmp) {
		cmp.setInteger(TAG_MANA, mana);
	}

	@Override
	public void readPacketNBT(NBTTagCompound cmp) {
		mana = cmp.getInteger(TAG_MANA);
	}

	@Override
	public int getCurrentMana() {
		return mana;
	}

	public int getManaCost() {
		RecipeTerrestrialAgglomeration recipe = findRecipe(getItems());
		return recipe == null ? 0 : recipe.getManaCost();
	}

	@Override
	public boolean isFull() {
		return mana >= getManaCost();
	}

	@Override
	public void recieveMana(int mana) {
		this.mana = Math.max(0, Math.min(getManaCost(), this.mana + mana));
		world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
	}

	@Override
	public boolean canRecieveManaFromBursts() {
		return findRecipe(getItems()) != null;
	}

	@Override
	public boolean canAttachSpark(ItemStack stack) {
		return true;
	}

	@Override
	public void attachSpark(ISparkEntity entity) {
	}

	@Override
	public ISparkEntity getAttachedSpark() {
		List<Entity> sparks = world.getEntitiesWithinAABB(Entity.class,
				new AxisAlignedBB(pos.up(), pos.up().add(1, 1, 1)), Predicates.instanceOf(ISparkEntity.class));
		if (sparks.size() == 1) {
			Entity e = sparks.get(0);
			return (ISparkEntity) e;
		}

		return null;
	}

	@Override
	public boolean areIncomingTranfersDone() {
		return findRecipe(getItems()) == null;
	}

	@Override
	public int getAvailableSpaceForMana() {
		return Math.max(0, getManaCost() - getCurrentMana());
	}

}
