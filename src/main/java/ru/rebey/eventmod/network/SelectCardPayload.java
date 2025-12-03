// src/main/java/ru/rebey/eventmod/network/SelectCardPayload.java
package ru.rebey.eventmod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

public record SelectCardPayload(String cardId) implements CustomPayload {
    public static final Id<SelectCardPayload> ID = new Id<>(Identifier.of(EventMod.MOD_ID, "select_card"));

    public static final PacketCodec<RegistryByteBuf, SelectCardPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SelectCardPayload::cardId,
            SelectCardPayload::new
    );

    @Override
    public Id<SelectCardPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
    }
}