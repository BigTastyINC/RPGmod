package com.example.examplemod.profession;

import com.example.examplemod.item.Rarity;
import java.util.Random;

public class RarityRoller {

    private static final Random RNG = new Random();
    private static final int[][] CHANCES = new int[101][5];

    static {
        set(1,   100, 0,  0,  0,  0);
        set(10,  90,  10, 0,  0,  0);
        set(20,  80,  20, 0,  0,  0);
        set(30,  65,  25, 10, 0,  0);
        set(40,  50,  30, 20, 0,  0);
        set(50,  40,  30, 25, 5,  0);
        set(60,  30,  30, 30, 10, 0);
        set(70,  20,  30, 30, 15, 5);
        set(80,  10,  25, 30, 25, 10);
        set(90,  5,   20, 30, 30, 15);
        set(100, 0,   10, 20, 40, 30);
    }

    private static void set(int fromLevel, int... weights) {
        int[] nexts = {1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 101};
        int end = 101;
        for (int n : nexts) if (n > fromLevel) { end = n; break; }
        for (int lvl = fromLevel; lvl < end && lvl <= 100; lvl++) {
            System.arraycopy(weights, 0, CHANCES[lvl], 0, 5);
        }
    }

    public static Rarity roll(int professionLevel, int luck) {
        // ИЗМЕНЕНО: Удача теперь делится на 20 (было 2), влияние уменьшено в 10 раз
        int effectiveLevel = professionLevel + (luck / 20);
        int lvl = Math.max(1, Math.min(100, effectiveLevel));

        int[] w = CHANCES[lvl];
        int r = RNG.nextInt(100);
        int acc = 0;
        for (int i = 0; i < 5; i++) {
            acc += w[i];
            if (r < acc) return Rarity.values()[i];
        }
        return Rarity.COMMON;
    }

    private RarityRoller() {}
}