// src/main/java/ru/rebey/eventmod/network/OpenCardSelectionPayload.java
package ru.rebey.eventmod.network;

import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

public record OpenCardSelectionPayload(String card1Json, String card2Json, String card1Id, String card2Id) implements CustomPayload {
    public static final Id<OpenCardSelectionPayload> ID = new Id<>(Identifier.of(EventMod.MOD_ID, "open_card_selection"));

    public static final PacketCodec<RegistryByteBuf, OpenCardSelectionPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, OpenCardSelectionPayload::card1Json,
            PacketCodecs.STRING, OpenCardSelectionPayload::card2Json,
            PacketCodecs.STRING, OpenCardSelectionPayload::card1Id,
            PacketCodecs.STRING, OpenCardSelectionPayload::card2Id,
            OpenCardSelectionPayload::new
    );

    public Text card1Text() {
        return TextCodecs.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(card1Json))
                .result()
                .orElse(Text.literal("[Ошибка]"));
    }

    public Text card2Text() {
        return TextCodecs.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(card2Json))
                .result()
                .orElse(Text.literal("[Ошибка]"));
    }

    public static OpenCardSelectionPayload of(Text c1, Text c2, String id1, String id2) {
        String j1 = TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, c1).result().map(Object::toString).orElse("\"[Err]\"");
        String j2 = TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, c2).result().map(Object::toString).orElse("\"[Err]\"");
        return new OpenCardSelectionPayload(j1, j2, id1, id2);
    }

    @Override
    public Id<OpenCardSelectionPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }
}