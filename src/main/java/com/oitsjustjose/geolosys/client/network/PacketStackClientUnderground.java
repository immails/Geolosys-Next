package com.oitsjustjose.geolosys.client.network;

import java.util.function.Supplier;

import com.oitsjustjose.geolosys.common.network.PacketStackUnderground;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class PacketStackClientUnderground {
    public static void handleClient(PacketStackUnderground msg, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
            context.get().enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    sendProspectingMessage(mc.player, msg);
                }
            });
        }
        context.get().setPacketHandled(true);
    }
    
    private static void sendProspectingMessage(LocalPlayer player, PacketStackUnderground msg) {
        MutableComponent component = Component.empty();
        Integer i = 0;
        for (String block : msg.blocks) {
            component.append(Component.translatable("block." + block)).withStyle(ChatFormatting.GOLD);
            i++;
            if (i < msg.blocks.size() - 1) component.append(Component.literal(" & "));
        }
        player.displayClientMessage(
            MutableComponent.create(
                new TranslatableContents("geolosys.pro_pick.tooltip.found", null, new Object[]{
                    component, 
                    Component.translatable("item.geolosys.pro_pick.direction." + Integer.valueOf(msg.direction))
                        .withStyle(ChatFormatting.WHITE)
            })).withStyle(ChatFormatting.GRAY), true
        );
    }
}
