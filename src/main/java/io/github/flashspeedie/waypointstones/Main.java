package io.github.flashspeedie.waypointstones;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class Main implements ModInitializer {

    @Override
    public void onInitialize() {

        PayloadTypeRegistry.clientboundPlay().register(
                WaypointHudPayload.TYPE,
                WaypointHudPayload.CODEC
        );

        ModBlocks.initialize();
    }
}