package net.qbismx.foodlevelmod.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.qbismx.foodlevelmod.FoodLevelMod;
import net.qbismx.foodlevelmod.player.ModFoodLevelHelper;

@EventBusSubscriber(modid = FoodLevelMod.MODID)
public final class PlayerFoodLevelEvents {
    private PlayerFoodLevelEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModFoodLevelHelper.serverTick(player);
        }
    }
}