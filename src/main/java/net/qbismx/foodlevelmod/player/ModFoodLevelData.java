package net.qbismx.foodlevelmod.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public record ModFoodLevelData(int extraFood, int maxExtraFood, int lastVanillaFood) {

    public static final int MIN = 0;
    public static final int MAX_EXTRA_FOOD = 4096;
    public static final int MAX_VANILLA_FOOD = 20;

    public ModFoodLevelData() {
        this(0, 0, 20);
    }

    public ModFoodLevelData {
        extraFood = Mth.clamp(extraFood, MIN, MAX_EXTRA_FOOD);
        maxExtraFood = Mth.clamp(maxExtraFood, MIN, MAX_EXTRA_FOOD);
        lastVanillaFood = Mth.clamp(lastVanillaFood, MIN, MAX_VANILLA_FOOD);

        if (extraFood > maxExtraFood) {
            extraFood = maxExtraFood;
        }
    }

    public static final MapCodec<ModFoodLevelData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("extra_food", 0).forGetter(ModFoodLevelData::extraFood),
            Codec.INT.optionalFieldOf("max_extra_food", 0).forGetter(ModFoodLevelData::maxExtraFood),
            Codec.INT.optionalFieldOf("last_vanilla_food", 20).forGetter(ModFoodLevelData::lastVanillaFood)
    ).apply(instance, ModFoodLevelData::new));


    public static final StreamCodec<RegistryFriendlyByteBuf, ModFoodLevelData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ModFoodLevelData::extraFood,
            ByteBufCodecs.VAR_INT,
            ModFoodLevelData::maxExtraFood,
            ByteBufCodecs.VAR_INT,
            ModFoodLevelData::lastVanillaFood,
            ModFoodLevelData::new
    );

    public ModFoodLevelData withExtraFood(int value) {
        return new ModFoodLevelData(value, this.maxExtraFood, this.lastVanillaFood);
    }

    public ModFoodLevelData withMaxExtraFood(int value) {
        return new ModFoodLevelData(this.extraFood, value, this.lastVanillaFood);
    }

    public ModFoodLevelData withLastVanillaFood(int value) {
        return new ModFoodLevelData(this.extraFood, this.maxExtraFood, value);
    }

}
