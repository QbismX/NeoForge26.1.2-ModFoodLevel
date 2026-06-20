package net.qbismx.foodlevelmod.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.qbismx.foodlevelmod.FoodLevelMod;
import net.qbismx.foodlevelmod.player.ModFoodLevelData;
import net.qbismx.foodlevelmod.registry.ModAttachments;

@EventBusSubscriber(modid = FoodLevelMod.MODID, value = Dist.CLIENT)
public final class ModFoodLevelHud {

    private ModFoodLevelHud() {
    }

    @SubscribeEvent
    public static void FoodLevelHud(RenderGuiLayerEvent.Post event){

        if (!VanillaGuiLayers.PLAYER_HEALTH.equals(event.getName())) {
            return;
        }


        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        GuiGraphicsExtractor guiGraphics = event.getGuiGraphics();
        // 追加したプレイヤーデータ
        ModFoodLevelData moddata = mc.player.getData(ModAttachments.EXTRA_FOOD.get());

        int mp = mc.player.getFoodData().getFoodLevel() + moddata.extraFood();
        int maxmp = 20 + moddata.maxExtraFood();

        String foodlevel = "MP : " + mp + " / " + maxmp;
        //String line2 = "Extra Food   : " ;
        //String line3 = "Max Extra    : " + moddata.maxExtraFood();
        //String line4 = "Last Vanilla : " + moddata.lastVanillaFood();

        // 文字の視認性を高めるための背景用の処理
        int padding = 4;
        int textWidth = mc.font.width("                  ");
        int lineHeight = mc.font.lineHeight + 2;
        int textHeight = lineHeight - 2;

        int backgroundX = guiGraphics.guiWidth() - textWidth - 10;
        int backgroundY = guiGraphics.guiHeight() - textHeight - 30;

        int bgLeft = backgroundX - padding;
        int bgTop = backgroundY - padding;
        int bgRight = backgroundX + textWidth + padding;
        int bgBottom = backgroundY + textHeight + padding;

        guiGraphics.fill(bgLeft, bgTop, bgRight, bgBottom, 0x80000000);

        guiGraphics.text(mc.font, Component.literal(foodlevel), backgroundX, backgroundY, 0xFFFFFFFF, true);

    }

    // 取り除く用
    @SubscribeEvent
    public static void removeHud(RenderGuiLayerEvent.Pre event) {

        Identifier name = event.getName();

        if (VanillaGuiLayers.FOOD_LEVEL.equals(name)) {
            event.setCanceled(true); // バニラの満腹度表示を消す
        }

    }
}
