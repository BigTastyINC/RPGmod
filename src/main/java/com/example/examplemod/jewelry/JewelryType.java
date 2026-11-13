package com.example.examplemod.jewelry;

public enum JewelryType {
    RING("ring", "Кольцо", 2, "ring"),
    AMULET("amulet", "Амулет", 1, "necklace"),
    EARRING("earring", "Серьга", 2, "charm"),
    BRACELET("bracelet", "Браслет", 2, "bracelet");

    private final String id;
    private final String displayName;
    private final int maxSlots;
    private final String curiosSlot;

    JewelryType(String id, String displayName, int maxSlots, String curiosSlot) {
        this.id = id;
        this.displayName = displayName;
        this.maxSlots = maxSlots;
        this.curiosSlot = curiosSlot;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getMaxSlots() { return maxSlots; }
    public String getCuriosSlot() { return curiosSlot; }

    public static JewelryType byId(String id) {
        for (JewelryType t : values()) if (t.id.equals(id)) return t;
        return RING;
    }
}