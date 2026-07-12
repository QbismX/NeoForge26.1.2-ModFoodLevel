package net.qbismx.foodlevelmod.player;

import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.qbismx.foodlevelmod.registry.ModAttachments;

public final class ModFoodLevelHelper {

    private static final int CONTROL_FOOD = 8; // 基準値

    // 時間経過で満腹度が減った時、コマンドで満腹度が回復したとき、コマンドで満腹度セットがなされた時など用
    public static void serverTick(ServerPlayer player) {
        FoodData foodData = player.getFoodData();
        ModFoodLevelData extra_data = player.getData(ModAttachments.EXTRA_FOOD.get());
        int currentFood = foodData.getFoodLevel(); // バニラのFoodレベル
        int lastFood = extra_data.lastVanillaFood(); // 前回処理したFoodレベル

        if (currentFood != lastFood) { // バニラのFoodレベルに変化があった場合(おなかがすいた or 何か食べた)
            int modFood = extra_data.extraFood() + currentFood; // 見かけ上のFoodレベル

            if (modFood < CONTROL_FOOD) { // 見かけ x < 8

                foodData.setFoodLevel(modFood); // 見かけ上のFoodレベルはバニラのFoodレベルと一致する。
                extra_data = extra_data.withExtraFood(0); // 追加用Foodレベルは0
                syncVanillaFood(player);

            } else if (modFood <= CONTROL_FOOD + extra_data.maxExtraFood()) { // 見かけ：8 <= x <= 8 + 追加Foodレベルの最大値

                foodData.setFoodLevel(CONTROL_FOOD); // バニラのFoodレベルは基準値の8
                extra_data = extra_data.withExtraFood(modFood - 8); // 余った分は追加用のFoodレベルにたくわえる
                syncVanillaFood(player);

            } else { // 見かけ: 8 + 追加Foodレベルの最大値 < x

                int maxExtraFood = extra_data.maxExtraFood();
                foodData.setFoodLevel(modFood - maxExtraFood);
                extra_data = extra_data.withExtraFood(maxExtraFood);
                syncVanillaFood(player);

            }

            // 最後に、lastVanillaFoodを合わせる
            extra_data = extra_data.withLastVanillaFood(currentFood);
            player.setData(ModAttachments.EXTRA_FOOD.get(), extra_data);
        }

    /*
        FoodLevelMod.LOGGER.info(
                "food={}, last={}, extra={}, max={}",
                currentFood,
                extra_data.lastVanillaFood(),
                extra_data.extraFood(),
                extra_data.maxExtraFood()
        );
     */
    }

    // バニラの満腹度のデータを同期させる
    private static void syncVanillaFood(ServerPlayer player) {
        player.connection.send(new ClientboundSetHealthPacket(
                player.getHealth(),
                player.getFoodData().getFoodLevel(),
                player.getFoodData().getSaturationLevel()
        ));
    }

}
