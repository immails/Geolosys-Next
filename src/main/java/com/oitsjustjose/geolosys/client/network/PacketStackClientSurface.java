package com.oitsjustjose.geolosys.client.network;

import java.util.function.Supplier;

import com.oitsjustjose.geolosys.common.network.PacketStackSurface;
import com.oitsjustjose.geolosys.common.utils.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class PacketStackClientSurface {
    public static void handleClient(PacketStackSurface msg, Supplier<NetworkEvent.Context> context) {
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

    private static void sendProspectingMessage(LocalPlayer player, PacketStackSurface msg) {
        player.displayClientMessage(
            Utils.getProspectingTranslatedComponent(msg.blocks), true
        );
    }
}
