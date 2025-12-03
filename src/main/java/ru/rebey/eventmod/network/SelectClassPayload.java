package ru.rebey.eventmod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

public record SelectClassPayload(String classId) implements CustomPayload {
    public static final Id<SelectClassPayload> ID = new Id<>(Identifier.of(EventMod.MOD_ID, "select_class"));

    public static final PacketCodec<RegistryByteBuf, SelectClassPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SelectClassPayload::classId,
            SelectClassPayload::new
    );

    @Override
    public Id<SelectClassPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
    }
}