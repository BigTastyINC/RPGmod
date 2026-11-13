package com.example.examplemod.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestGenerator {
    private static final Random RANDOM = new Random();

    // НОВОЕ: Расширенные пулы
    private static final String[] Mobs = {
            "minecraft:zombie", "minecraft:skeleton", "minecraft:spider", "minecraft:creeper",
            "minecraft:enderman", "minecraft:witch", "minecraft:slime", "minecraft:husk",
            "minecraft:stray", "minecraft:drowned", "minecraft:pillager", "minecraft:vindicator"
    };
    private static final String[] Bosses = {
            "minecraft:zombie", "minecraft:skeleton", "minecraft:spider", "minecraft:enderman",
            "minecraft:witch", "minecraft:pillager", "minecraft:vindicator", "minecraft:ravager"
    };
    private static final String[] DeliverItems = {
            "minecraft:cooked_beef", "minecraft:cooked_porkchop", "minecraft:cooked_chicken",
            "minecraft:cooked_mutton", "minecraft:cooked_rabbit",
            "examplemod:small_magic_crystal", "examplemod:medium_magic_crystal", "examplemod:large_magic_crystal",
            "examplemod:mana_potion", "examplemod:health_potion",
            "examplemod:silver_ingot", "examplemod:cobalt_ingot", "examplemod:mithril_ingot",
            "examplemod:adamantium_ingot", "examplemod:orichalcum_ingot"
    };

    public static List<Quest> generateDailyQuests(int playerLevel) {
        List<Quest> quests = new ArrayList<>();

        // Генерируем от 4 до 5 квестов
        int numQuests = 4 + RANDOM.nextInt(2);

        for (int i = 0; i < numQuests; i++) {
            int type = RANDOM.nextInt(3); // 0 = KILL_MOB, 1 = KILL_BOSS, 2 = DELIVER_ITEM

            // Боссов даем только с 10 уровня
            if (type == 1 && playerLevel < 10) type = 0;

            switch (type) {
                case 0: // Убить моба
                    String mob = Mobs[RANDOM.nextInt(Mobs.length)];
                    int mobAmount = 3 + RANDOM.nextInt(8); // 3-10 штук
                    int mobMinLevel = Math.max(1, playerLevel - 5 + RANDOM.nextInt(10));
                    quests.add(new Quest(Quest.Type.KILL_MOB, mob, mobMinLevel, 0, mobAmount, 500 + playerLevel * 10, 50 + playerLevel * 5));
                    break;

                case 1: // Убить босса
                    String boss = Bosses[RANDOM.nextInt(Bosses.length)];
                    quests.add(new Quest(Quest.Type.KILL_BOSS, boss, 1, 0, 1, 2000 + playerLevel * 20, 200 + playerLevel * 10));
                    break;

                case 2: // Принести предмет
                    String item = DeliverItems[RANDOM.nextInt(DeliverItems.length)];
                    int itemAmount = 3 + RANDOM.nextInt(8);
                    int minRarity = RANDOM.nextInt(3); // 0-2
                    quests.add(new Quest(Quest.Type.DELIVER_ITEM, item, 1, minRarity, itemAmount, 1000 + playerLevel * 15, 100 + playerLevel * 5));
                    break;
            }
        }

        return quests;
    }
}