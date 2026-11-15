
package io.github.flashspeedie.waypointstones;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.phys.BlockHitResult;

public class WaypointStoneBlock extends Block {

    private static final Logger LOGGER =
            LoggerFactory.getLogger("waypointstones");

    public WaypointStoneBlock(Properties properties) {
        super(properties);
    }

    /*
     * Handles empty-hand interaction.
     * Sends the selected waypoint to the client's persistent HUD.
     */
    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {

        LOGGER.debug(
                "Waypoint Stone interaction: side={}, position={}",
                level.isClientSide() ? "CLIENT" : "SERVER",
                pos
        );

        if (!level.isClientSide()
                && player instanceof ServerPlayer serverPlayer) {

            String dimension = getDimensionName(level);

            // Create a stable copy of the clicked block position.
            BlockPos waypointPosition = new BlockPos(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
            );

            // Send the selected waypoint to this player.
            WaypointHudPayload payload = new WaypointHudPayload(
                    waypointPosition,
                    dimension
            );

            if (ServerPlayNetworking.canSend(
                    serverPlayer,
                    WaypointHudPayload.TYPE
            )) {

                ServerPlayNetworking.send(
                        serverPlayer,
                        payload
                );

                LOGGER.info(
                        "Waypoint HUD update sent: position={}, dimension={}",
                        waypointPosition,
                        dimension
                );

            } else {

                LOGGER.warn(
                        "Client cannot receive Waypoint HUD payload."
                );

                serverPlayer.sendSystemMessage(
                        Component.literal(
                                "Your client does not support the Waypoint HUD."
                        )
                );
            }

            // Play activation effects on the server.
            playActivationEffects(level, pos);
        }

        // Tell Minecraft that the block interaction was handled.
        return (
                level.isClientSide()
                        ? InteractionResult.SUCCESS
                        : InteractionResult.SUCCESS_SERVER
        ).withoutItem();
    }

    /*
     * Handles compass interaction.
     * Links a normal compass to this Waypoint Stone.
     */
    @Override
    protected InteractionResult useItemOn(
            ItemStack itemStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {

        // Non-compass items continue to the empty-hand handler.
        if (!itemStack.is(Items.COMPASS)) {

            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        // Only the server modifies the compass.
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        LOGGER.info(
                "Linking compass to Waypoint Stone at {}",
                pos
        );

        // Store the block's dimension and coordinates.
        LodestoneTracker tracker = new LodestoneTracker(
                Optional.of(
                        GlobalPos.of(level.dimension(), pos)
                ),
                false
        );

        ItemStack linkedCompass;

        if (itemStack.getCount() == 1) {

            linkedCompass = itemStack;

        } else {

            // Separate one compass from the stack.
            linkedCompass = itemStack.split(1);
        }

        // Attach the waypoint destination.
        linkedCompass.set(
                DataComponents.LODESTONE_TRACKER,
                tracker
        );

        // Return the separated compass to the inventory.
        if (linkedCompass != itemStack) {

            if (!player.getInventory().add(linkedCompass)) {

                // Restore the original stack if the inventory is full.
                itemStack.grow(1);

                player.sendSystemMessage(
                        Component.literal(
                                "Your inventory is full. Make space first."
                        )
                );

                return InteractionResult.FAIL;
            }
        }

        playActivationEffects(level, pos);

        player.sendSystemMessage(
                Component.literal(
                        "Your compass is now linked to this Waypoint Stone!"
                )
        );

        LOGGER.info(
                "Compass successfully linked to Waypoint Stone at {}",
                pos
        );

        return InteractionResult.SUCCESS_SERVER;
    }

    /*
     * Plays the activation sound and creates particles.
     */
    private void playActivationEffects(
            Level level,
            BlockPos pos
    ) {

        level.playSound(
                null,
                pos,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS,
                0.9F,
                1.15F
        );

        if (level instanceof ServerLevel serverLevel) {

            serverLevel.sendParticles(
                    ParticleTypes.SCULK_SOUL,
                    pos.getX() + 0.5,
                    pos.getY() + 0.8,
                    pos.getZ() + 0.5,
                    12,
                    0.20,
                    0.18,
                    0.20,
                    0.02
            );
        }
    }

    /*
     * Converts the dimension identifier into a readable name.
     */
    private String getDimensionName(Level level) {

        if (level.dimension().equals(Level.OVERWORLD)) {
            return "Overworld";
        }

        if (level.dimension().equals(Level.NETHER)) {
            return "Nether";
        }

        if (level.dimension().equals(Level.END)) {
            return "The End";
        }

        return "Custom Dimension";
    }
}