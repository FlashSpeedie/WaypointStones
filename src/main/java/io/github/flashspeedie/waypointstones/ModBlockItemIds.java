package io.github.flashspeedie.waypointstones;

import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;

public final class ModBlockItemIds {

    public static final BlockItemId WAYPOINT_STONE =
            create("waypoint_stone");

    private static BlockItemId create(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(
                "waypointstones",
                name
        );

        return BlockItemId.create(id, id);
    }

    private ModBlockItemIds() {
    }
}