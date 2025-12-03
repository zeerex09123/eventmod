// src/main/java/ru/rebey/eventmod/network/SyncPlayerEventPlayerDataPayload.java
package ru.rebey.eventmod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

public record SyncPlayerEventPlayerDataPayload(String className, String teamColor) implements CustomPayload {
    public static final Id<SyncPlayerEventPlayerDataPayload> ID = new Id<>(Identifier.of(EventMod.MOD_ID, "sync_player_data"));

    public static final PacketCodec<RegistryByteBuf, SyncPlayerEventPlayerDataPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SyncPlayerEventPlayerDataPayload::className,
            PacketCodecs.STRING, SyncPlayerEventPlayerDataPayload::teamColor,
            SyncPlayerEventPlayerDataPayload::new
    );

    @Override
    public Id<SyncPlayerEventPlayerDataPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }
}