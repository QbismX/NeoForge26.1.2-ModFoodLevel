package net.qbismx.foodlevelmod.player;

import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.qbismx.foodlevelmod.registry.ModAttachments;

public final class ModFoodLevelHelper {

    private static final int CONTROL_FOOD = 8; // 基準値

    // 以下のような関数をベースに設計する：第1成分(バニラのFoodレベル)と第2成分(追加Foodレベル)の和が見かけ上のFoodレベルとなる
    // t (modFood) = (modFood, 0)                                   if modFood < 8
    // t (modFood) = (8, modFood - 8)                               if 8 <= modFood <= 8 + (ExtraFoodの最大値)
    // t (modFood) = (ExtraFoodの最大値 - modFood, ExtraFoodの最大値)  otherwise

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

        } else { // 別のシステムによって、予想外の状態になったときの保険

            int extraFood = extra_data.extraFood();
            int maxExtraFood = extra_data.maxExtraFood();

            if (extraFood < maxExtraFood && CONTROL_FOOD < currentFood) { // 基準値を越えているけど、extra_foodがMaxではない場合

                int modFood = extra_data.extraFood() + currentFood; // 見かけ上のFoodレベル：x < 8はあり得ない

                if (modFood <= CONTROL_FOOD + extra_data.maxExtraFood()) { // 見かけ：8 <= x <= 8 + 追加Foodレベルの最大値

                    foodData.setFoodLevel(CONTROL_FOOD); // バニラのFoodレベルは基準値の8
                    extra_data = extra_data.withExtraFood(modFood - 8); // 余った分は追加用のFoodレベルにたくわえる
                    syncVanillaFood(player);

                } else { // 見かけ: 8 + 追加Foodレベルの最大値 < x

                    foodData.setFoodLevel(modFood - maxExtraFood);
                    extra_data = extra_data.withExtraFood(maxExtraFood);
                    syncVanillaFood(player);

                }

                // 最後に、lastVanillaFoodを合わせる
                extra_data = extra_data.withLastVanillaFood(currentFood);
                player.setData(ModAttachments.EXTRA_FOOD.get(), extra_data);

            } else if (0 < extraFood && currentFood < CONTROL_FOOD) { // 基準値は越えていないけど、extra_foodに余りがある場合

                int modFood = extra_data.extraFood() + currentFood; // 見かけ上のFoodレベル: 8 + 追加Foodレベルの最大値 < xはありえない

                if (modFood < CONTROL_FOOD) { // 見かけ x < 8

                    foodData.setFoodLevel(modFood); // 見かけ上のFoodレベルはバニラのFoodレベルと一致する。
                    extra_data = extra_data.withExtraFood(0); // 追加用Foodレベルは0
                    syncVanillaFood(player);

                } else { // 見かけ：8 <= x <= 8 + 追加Foodレベルの最大値

                    foodData.setFoodLevel(CONTROL_FOOD); // バニラのFoodレベルは基準値の8
                    extra_data = extra_data.withExtraFood(modFood - 8); // 余った分は追加用のFoodレベルにたくわえる
                    syncVanillaFood(player);

                }

                // 最後に、lastVanillaFoodを合わせる
                extra_data = extra_data.withLastVanillaFood(currentFood);
                player.setData(ModAttachments.EXTRA_FOOD.get(), extra_data);

            }

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
