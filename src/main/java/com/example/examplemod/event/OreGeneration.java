package com.example.examplemod.event;

import com.example.examplemod.block.ModBlocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod")
public class OreGeneration {

    @SubscribeEvent
    public static void onBiomeLoad(BiomeLoadingEvent event) {
        if (event.getCategory() == Biome.Category.NETHER || event.getCategory() == Biome.Category.THEEND) {
            return;
        }

        // Серебро: 8 жил, Y от 10 до 60, 4 попытки на чанк
        event.getGeneration().addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
                Feature.ORE.configured(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                                ModBlocks.SILVER_ORE.get().defaultBlockState(), 8))
                        .decorated(Placement.RANGE.configured(new TopSolidRangeConfig(10, 0, 60)))
                        .squared()
                        .count(4));

        // Кобальт: 6 жил, Y от 5 до 40, 3 попытки на чанк
        event.getGeneration().addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
                Feature.ORE.configured(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                                ModBlocks.COBALT_ORE.get().defaultBlockState(), 6))
                        .decorated(Placement.RANGE.configured(new TopSolidRangeConfig(5, 0, 40)))
                        .squared()
                        .count(3));

        // Мифрил: 4 жилы, Y от 0 до 25, 2 попытки на чанк
        event.getGeneration().addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
                Feature.ORE.configured(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                                ModBlocks.MITHRIL_ORE.get().defaultBlockState(), 4))
                        .decorated(Placement.RANGE.configured(new TopSolidRangeConfig(0, 0, 25)))
                        .squared()
                        .count(2));

        // Адамантий: 3 жилы, Y от 0 до 15, 1 попытка на чанк
        event.getGeneration().addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
                Feature.ORE.configured(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                                ModBlocks.ADAMANTIUM_ORE.get().defaultBlockState(), 3))
                        .decorated(Placement.RANGE.configured(new TopSolidRangeConfig(0, 0, 15)))
                        .squared()
                        .count(1));

        // Орихалк: 2 жилы, Y от 0 до 12, 1 попытка на чанк
        event.getGeneration().addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
                Feature.ORE.configured(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                                ModBlocks.ORICHALCUM_ORE.get().defaultBlockState(), 2))
                        .decorated(Placement.RANGE.configured(new TopSolidRangeConfig(0, 0, 12)))
                        .squared()
                        .count(1));
    }
}