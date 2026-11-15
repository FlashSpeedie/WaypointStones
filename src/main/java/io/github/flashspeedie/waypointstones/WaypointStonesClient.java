package io.github.flashspeedie.waypointstones;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class WaypointStonesClient implements ClientModInitializer {

    // Currently selected waypoint.
    private static BlockPos currentPosition;

    private static String currentDimension;

    // Default dismissal key: H.
    private static KeyMapping dismissWaypointKey;

    @Override
    public void onInitializeClient() {

        registerKeyMapping();

        registerHud();

        registerNetworking();

        registerClientEvents();
    }

    /*
     * Registers the key used to dismiss the waypoint HUD.
     */
    private void registerKeyMapping() {

        KeyMapping.Category category =
                KeyMapping.Category.register(
                        Identifier.fromNamespaceAndPath(
                                "waypointstones",
                                "controls"
                        )
                );

        dismissWaypointKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.waypointstones.dismiss_waypoint_hud",
                        InputConstants.Type.KEYSYM,
                        InputConstants.KEY_H,
                        category
                )
        );
    }

    /*
     * Registers the persistent HUD element.
     */
    private void registerHud() {

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(
                        "waypointstones",
                        "waypoint_hud"
                ),
                WaypointStonesClient::renderWaypointHud
        );
    }

    /*
     * Receives waypoint information from the server.
     */
    private void registerNetworking() {

        ClientPlayNetworking.registerGlobalReceiver(
                WaypointHudPayload.TYPE,
                (payload, context) -> {

                    currentPosition = payload.position();
                    currentDimension = payload.dimensionName();
                }
        );
    }

    /*
     * Handles the dismissal key and clears stale data
     * when the player leaves the world.
     */
    private void registerClientEvents() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (dismissWaypointKey.consumeClick()) {

                if (currentPosition != null) {

                    clearWaypointHud();

                    if (client.player != null) {
                        client.player.sendSystemMessage(
                                Component.literal(
                                        "Waypoint HUD dismissed."
                                )
                        );
                    }
                }
            }

            if (client.player == null) {
                clearWaypointHud();
            }
        });
    }

    /*
     * Removes the current waypoint from the HUD.
     */
    private static void clearWaypointHud() {

        currentPosition = null;
        currentDimension = null;
    }

    /*
     * Draws the waypoint panel every frame while
     * a waypoint is selected.
     */
    private static void renderWaypointHud(
            GuiGraphicsExtractor graphics,
            DeltaTracker deltaTracker
    ) {

        if (currentPosition == null || currentDimension == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();

        if (client.player == null) {
            return;
        }

        String title = "WAYPOINT SELECTED";

        String coordinates =
                "X: " + currentPosition.getX()
                + " | Y: " + currentPosition.getY()
                + " | Z: " + currentPosition.getZ();

        String dimension =
                "Dimension: " + currentDimension;

        String instruction =
                "Press H to dismiss";

        // Calculate the panel width based on its text.
        int textWidth = Math.max(
                Math.max(
                        client.font.width(title),
                        client.font.width(coordinates)
                ),
                Math.max(
                        client.font.width(dimension),
                        client.font.width(instruction)
                )
        );

        int panelWidth = textWidth + 22;
        int panelHeight = 64;

        // Position in the upper-left corner.
        int x = 8;
        int y = 8;

        // Dark translucent background.
        graphics.fill(
                x,
                y,
                x + panelWidth,
                y + panelHeight,
                0xD9101922
        );

        // Cyan accent strip.
        graphics.fill(
                x,
                y,
                x + 3,
                y + panelHeight,
                0xFF13D9ED
        );

        // Subtle border.
        graphics.outline(
                x,
                y,
                panelWidth,
                panelHeight,
                0xFF147A8C
        );

        // Header.
        graphics.text(
                client.font,
                title,
                x + 10,
                y + 7,
                0xFF58E5F5,
                true
        );

        // Coordinates.
        graphics.text(
                client.font,
                coordinates,
                x + 10,
                y + 22,
                0xFFFFFFFF,
                true
        );

        // Dimension.
        graphics.text(
                client.font,
                dimension,
                x + 10,
                y + 36,
                0xFFC4D3DF,
                true
        );

        // Dismiss instruction.
        graphics.text(
                client.font,
                instruction,
                x + 10,
                y + 50,
                0xFF91A8B8,
                false
        );
    }
}