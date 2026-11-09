package com.example.examplemod.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestGenerator {
    private static final Random RANDOM = new Random();

    private static final String[] Mobs = {"minecraft:zombie", "minecraft:skeleton", "minecraft:pig", "minecraft:cow", "minecraft:spider", "minecraft:creeper"};
    private static final String[] Bosses = {"minecraft:zombie", "minecraft:skeleton", "minecraft:spider", "minecraft:enderman"};

    // НОВОЕ: Оставляем только те предметы, которые могут иметь RarityLevel в нашем моде (еда, кристаллы, зелья)
    private static final String[] DeliverItems = {
            "minecraft:cooked_beef",
            "minecraft:cooked_porkchop",
            "minecraft:cooked_chicken",
            "minecraft:cooked_mutton",
            "examplemod:small_magic_crystal",
            "examplemod:mana_potion",
            "examplemod:health_potion"
    };

    public static List<Quest> generateDailyQuests(int playerLevel) {
        List<Quest> quests = new ArrayList<>();

        // 1. Квест на убийство моба
        String mob = Mobs[RANDOM.nextInt(Mobs.length)];
        int mobAmount = 3 + RANDOM.nextInt(5); // 3-7 штук
        int mobMinLevel = Math.max(1, playerLevel - 5 + RANDOM.nextInt(10));
        quests.add(new Quest(Quest.Type.KILL_MOB, mob, mobMinLevel, 0, mobAmount, 500 + playerLevel * 10, 50 + playerLevel * 5));

        // 2. Квест на Лорда (если уровень игрока > 10)
        if (playerLevel > 10) {
            String boss = Bosses[RANDOM.nextInt(Bosses.length)];
            quests.add(new Quest(Quest.Type.KILL_BOSS, boss, 1, 0, 1, 2000 + playerLevel * 20, 200 + playerLevel * 10));
        }

        // 3. Квест на доставку (только предметы с редкостью)
        String item = DeliverItems[RANDOM.nextInt(DeliverItems.length)];
        int itemAmount = 3 + RANDOM.nextInt(8); // 3-10 штук
        int minRarity = RANDOM.nextInt(3); // Common, Uncommon, Rare
        quests.add(new Quest(Quest.Type.DELIVER_ITEM, item, 1, minRarity, itemAmount, 1000 + playerLevel * 15, 100 + playerLevel * 5));

        return quests;
    }
}