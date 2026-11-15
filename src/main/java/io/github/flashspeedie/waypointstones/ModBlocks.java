package io.github.flashspeedie.waypointstones;

import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {

    public static final Block WAYPOINT_STONE = register(
            ModBlockItemIds.WAYPOINT_STONE,
            WaypointStoneBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops()
    );

    private static Block register(
            BlockItemId id,
            Function<BlockBehaviour.Properties, Block> blockFactory,
            BlockBehaviour.Properties properties
    ) {
        Block block = register(
                id.block(),
                blockFactory,
                properties
        );

        BlockItem blockItem = new BlockItem(
                block,
                new Item.Properties()
                        .useBlockDescriptionPrefix()
                        .setId(id.item())
        );

        Registry.register(
                BuiltInRegistries.ITEM,
                id.item(),
                blockItem
        );

        return block;
    }

    private static Block register(
            ResourceKey<Block> id,
            Function<BlockBehaviour.Properties, Block> blockFactory,
            BlockBehaviour.Properties properties
    ) {
        Block block = blockFactory.apply(
                properties.setId(id)
        );

        return Registry.register(
                BuiltInRegistries.BLOCK,
                id,
                block
        );
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(
                CreativeModeTabs.BUILDING_BLOCKS
        ).register(creativeTab -> {
            creativeTab.accept(WAYPOINT_STONE.asItem());
        });
    }

    private ModBlocks() {
    }
}