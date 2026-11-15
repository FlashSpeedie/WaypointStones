package io.github.flashspeedie.waypointstones;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record WaypointHudPayload(
        BlockPos position,
        String dimensionName
) implements CustomPacketPayload {

    public static final Type<WaypointHudPayload> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            "waypointstones",
                            "show_waypoint"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            WaypointHudPayload
    > CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            WaypointHudPayload::position,

            ByteBufCodecs.STRING_UTF8,
            WaypointHudPayload::dimensionName,

            WaypointHudPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}