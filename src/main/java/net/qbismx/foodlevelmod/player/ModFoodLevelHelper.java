package net.qbismx.foodlevelmod.player;

import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.qbismx.foodlevelmod.FoodLevelMod;
import net.qbismx.foodlevelmod.registry.ModAttachments;

public final class ModFoodLevelHelper {

    private static final int CONTROL_FOOD = 8; // 基準値
   // private static final int UP_FOOD = 9;

    /*
    // 食料を食べて、満腹度が回復した時用 (減らない)
    public static void eatExtraFood(ServerPlayer player, int nutrition){
        FoodData foodData = player.getFoodData();
        ModFoodLevelData extra_data = player.getData(ModAttachments.EXTRA_FOOD.get());
        int currentFood = foodData.getFoodLevel(); // バニラのFoodレベル

        if ((currentFood + nutrition < UP_FOOD) || (extra_data.extraFood() == extra_data.maxExtraFood())) { // 何も実行しなくてよい場合。nutrition == 0のときは本当に何も処理しなくてよい
            int vanilla_food = Math.min(currentFood + nutrition, 20);
            foodData.setFoodLevel(vanilla_food); // バニラと同じ処理で終了
            extra_data = extra_data.withLastVanillaFood(vanilla_food);
            player.setData(ModAttachments.EXTRA_FOOD.get(), extra_data);
        } else if (extra_data.extraFood() < extra_data.maxExtraFood()) { // 追加Foodレベルの容量が残っていて、バニラのFoodレベルが9以上になれば、様々な処理を実行する
            int overflow = (currentFood + nutrition + extra_data.extraFood()) - (extra_data.maxExtraFood() + CONTROL_FOOD); // 追加Foodレベルの容量にあふれた余剰分はあるか？
            int vanilla_food = Math.min(CONTROL_FOOD + Math.max(0, overflow), 20);
            foodData.setFoodLevel(vanilla_food); // バニラの体力の設定、surplusが0未満であれば、バニラの体力は基準値の8となる。
            extra_data = extra_data.withLastVanillaFood(vanilla_food);
            extra_data = extra_data.withExtraFood(Math.min(currentFood + extra_data.extraFood() + nutrition - CONTROL_FOOD, extra_data.maxExtraFood()));
            player.setData(ModAttachments.EXTRA_FOOD.get(), extra_data);
        }
    }
     */

    // 時間経過で満腹度が減った時、コマンドで満腹度が回復したとき、コマンドで満腹度セットがなされた時など用
    public static void serverTick(ServerPlayer player) {
        FoodData foodData = player.getFoodData();
        ModFoodLevelData extra_data = player.getData(ModAttachments.EXTRA_FOOD.get());
        int currentFood = foodData.getFoodLevel(); // バニラのFoodレベル
        int lastFood = extra_data.lastVanillaFood();

        if (currentFood != lastFood) {
            if (currentFood < lastFood) { // Foodレベルが下がった場合
                if (0 < extra_data.extraFood() && currentFood < CONTROL_FOOD) { // 、追加Foodレベルの容量が残っているならば、様々な処理を実行する
                    int foodLoss = lastFood - currentFood; // 下がっているので、1以上
                    int restored = Math.min(foodLoss, extra_data.extraFood()); // 補填可能な量
                    restored = Math.min(restored, CONTROL_FOOD - currentFood); // 可能な限り、基準値8に近付ける
                    extra_data = extra_data.withExtraFood(extra_data.extraFood() - restored);
                    currentFood = currentFood + restored;
                    foodData.setFoodLevel(currentFood);
                    syncVanillaFood(player);
                }

            }
            // 最後に、lastVanillaFoodを合わせる
            extra_data = extra_data.withLastVanillaFood(currentFood);
            player.setData(ModAttachments.EXTRA_FOOD.get(), extra_data);
        }

        if (CONTROL_FOOD < currentFood) {
            if (extra_data.extraFood() < extra_data.maxExtraFood()) { // コマンドによって回復した場合(追加Foodレベルについては、無視された増加となる)

                int absorbed = Math.min(currentFood - CONTROL_FOOD, extra_data.maxExtraFood() - extra_data.extraFood()); // 増加量の吸収量
                extra_data = extra_data.withExtraFood(extra_data.extraFood() + absorbed);
                currentFood = currentFood - absorbed;
                foodData.setFoodLevel(currentFood);
                syncVanillaFood(player);
            }
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
