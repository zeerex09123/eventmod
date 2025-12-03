// src/main/java/ru/rebey/eventmod/network/SyncAllPlayersPayload.java
package ru.rebey.eventmod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

import java.util.ArrayList;
import java.util.List;

public record SyncAllPlayersPayload(List<PlayerInfo> players) implements CustomPayload {
    public static final Id<SyncAllPlayersPayload> ID = new Id<>(Identifier.of(EventMod.MOD_ID, "sync_all_players"));

    public static final PacketCodec<RegistryByteBuf, SyncAllPlayersPayload> CODEC = new PacketCodec<>() {
        @Override
        public SyncAllPlayersPayload decode(RegistryByteBuf buf) {
            int size = buf.readVarInt();
            List<PlayerInfo> players = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                players.add(PlayerInfo.CODEC.decode(buf));
            }
            return new SyncAllPlayersPayload(players);
        }

        @Override
        public void encode(RegistryByteBuf buf, SyncAllPlayersPayload payload) {
            buf.writeVarInt(payload.players.size());
            for (PlayerInfo info : payload.players) {
                PlayerInfo.CODEC.encode(buf, info);
            }
        }
    };

    public record PlayerInfo(String name, String className, String teamName) {
        public static final PacketCodec<RegistryByteBuf, PlayerInfo> CODEC = new PacketCodec<>() {
            @Override
            public PlayerInfo decode(RegistryByteBuf buf) {
                return new PlayerInfo(buf.readString(), buf.readString(), buf.readString());
            }

            @Override
            public void encode(RegistryByteBuf buf, PlayerInfo info) {
                buf.writeString(info.name);
                buf.writeString(info.className);
                buf.writeString(info.teamName);
            }
        };
    }

    @Override
    public Id<SyncAllPlayersPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }
}