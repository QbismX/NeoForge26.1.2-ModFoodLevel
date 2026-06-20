package net.qbismx.foodlevelmod.event;


import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.qbismx.foodlevelmod.FoodLevelMod;
import net.qbismx.foodlevelmod.player.ModFoodLevelData;
import net.qbismx.foodlevelmod.registry.ModAttachments;

@EventBusSubscriber(modid = FoodLevelMod.MODID)
public final class PlayerExperienceEvents {
    private PlayerExperienceEvents() {
    }

    @SubscribeEvent
    public static void onLevelChange(PlayerXpEvent.LevelChange event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }


        int newLevel = Math.max(0, player.experienceLevel + event.getLevels());
        ModFoodLevelData data = player.getData(ModAttachments.EXTRA_FOOD.get());
        int newMax = Math.min(newLevel * 5, 4096);
        if (data.maxExtraFood() == newMax) {
            return;
        }
        data = data.withMaxExtraFood(newMax);
        data = data.withExtraFood(Math.min(data.extraFood(), newMax));
        player.setData(ModAttachments.EXTRA_FOOD.get(), data);
    }
}
