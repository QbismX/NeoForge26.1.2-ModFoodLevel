package net.qbismx.foodlevelmod.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.qbismx.foodlevelmod.FoodLevelMod;
import net.qbismx.foodlevelmod.player.ModFoodLevelData;

import java.util.function.Supplier;

public final class ModAttachments {

    private ModAttachments() {
    }

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FoodLevelMod.MODID);

    public static final Supplier<AttachmentType<ModFoodLevelData>> EXTRA_FOOD =
            ATTACHMENT_TYPES.register("extra_food", () -> AttachmentType.builder(ModFoodLevelData::new)
                    .serialize(ModFoodLevelData.CODEC)
                    .sync((holder, to) -> holder == to, ModFoodLevelData.STREAM_CODEC)
                    .build());

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }

}
