package net.qbismx.foodlevelmod.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.qbismx.foodlevelmod.FoodLevelMod;
import net.qbismx.foodlevelmod.player.ModFoodLevelData;
import net.qbismx.foodlevelmod.registry.ModAttachments;

@EventBusSubscriber(modid = FoodLevelMod.MODID)
public final class PlayerLifecycleEvents {
    private PlayerLifecycleEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) { // ログイン時
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ModFoodLevelData data = player.getData(ModAttachments.EXTRA_FOOD.get());

        int newMax = Math.min(player.experienceLevel * 5, 4096);

        data = data.withMaxExtraFood(newMax);
        data = data.withExtraFood(Math.min(data.extraFood(), newMax));
        data = data.withLastVanillaFood(player.getFoodData().getFoodLevel());
        player.setData(ModAttachments.EXTRA_FOOD.get(), data);
        player.syncData(ModAttachments.EXTRA_FOOD.get());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }

        if (!event.isWasDeath()) {
            return;
        }
        // 死亡時
        //ModFoodLevelData oldData = event.getOriginal().getData(ModAttachments.EXTRA_FOOD.get());

        ModFoodLevelData newData = new ModFoodLevelData(
                0,
                Math.min(newPlayer.experienceLevel * 5, 4096),
                newPlayer.getFoodData().getFoodLevel()
        );

        newPlayer.setData(ModAttachments.EXTRA_FOOD.get(), newData);
        newPlayer.syncData(ModAttachments.EXTRA_FOOD.get());
    }
}
