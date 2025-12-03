// src/main/java/ru/rebey/eventmod/network/OpenClassSelectionPayload.java
package ru.rebey.eventmod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

public record OpenClassSelectionPayload() implements CustomPayload {
    public static final Id<OpenClassSelectionPayload> ID = new Id<>(Identifier.of(EventMod.MOD_ID, "open_class_selection"));
    private static final OpenClassSelectionPayload INSTANCE = new OpenClassSelectionPayload();
    public static final PacketCodec<RegistryByteBuf, OpenClassSelectionPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<OpenClassSelectionPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }
}