package me.putindeer.woofquests.core;

import lombok.Getter;

@Getter
public enum QuestRequirement {
    // DÍA 1
    DIAMOND_SWORD("diamond_sword", 1, 1, "Consigue una espada de diamante"),
    DIAMOND_AXE("diamond_axe", 1, 1, "Consigue un hacha de diamante"),
    DIAMOND_PICKAXE("diamond_pickaxe", 1, 1, "Consigue un pico de diamante"),
    DIAMOND_SHOVEL("diamond_shovel", 1, 1, "Consigue una pala de diamante"),
    DIAMOND_SPEAR("diamond_spear", 1, 1, "Consigue una lanza de diamante"),

    // DÍA 2
    WITHER_SKELETONS("wither_skeletons", 20, 2, "Mata wither skeletons"),
    BLAZES("blazes", 20, 2, "Mata blazes"),
    PIGLINS("piglins", 20, 2, "Mata piglins"),
    PIGLIN_BRUTES("piglin_brutes", 5, 2, "Mata piglin brutes"),

    // DÍA 3
    ENTERED_END("entered_end", 1, 3, "Entra al End"),

    // DÍA 4
    EVOKERS("evokers", 2, 4, "Mata evokers"),
    VINDICATORS("vindicators", 15, 4, "Mata vindicators"),
    RAVAGERS("ravagers", 2, 4, "Mata ravagers"),

    // DÍA 5
    VAULTS("vaults", 5, 5, "Abre vaults normales"),
    OMINOUS_VAULTS("ominous_vaults", 3, 5, "Abre ominous vaults"),

    // DÍA 6
    WITHERS("withers", 2, 6, "Mata withers"),
    GUARDIANS("guardians", 10, 6, "Mata guardians"),
    ELDER_GUARDIANS("elder_guardians", 3, 6, "Mata elder guardians");

    private final String key;
    private final int required;
    private final int day;
    private final String displayName;

    QuestRequirement(String key, int required, int day, String displayName) {
        this.key = key;
        this.required = required;
        this.day = day;
        this.displayName = displayName;
    }

    public static QuestRequirement[] getRequirementsForDay(int day) {
        return switch (day) {
            case 1 -> new QuestRequirement[]{DIAMOND_SWORD, DIAMOND_AXE, DIAMOND_PICKAXE, DIAMOND_SHOVEL, DIAMOND_SPEAR};
            case 2 -> new QuestRequirement[]{WITHER_SKELETONS, BLAZES, PIGLINS, PIGLIN_BRUTES};
            case 3 -> new QuestRequirement[]{ENTERED_END};
            case 4 -> new QuestRequirement[]{EVOKERS, VINDICATORS, RAVAGERS};
            case 5 -> new QuestRequirement[]{VAULTS, OMINOUS_VAULTS};
            case 6 -> new QuestRequirement[]{WITHERS, GUARDIANS, ELDER_GUARDIANS};
            default -> new QuestRequirement[]{};
        };
    }

    public static QuestRequirement fromKey(String key) {
        for (QuestRequirement req : values()) {
            if (req.key.equals(key)) {
                return req;
            }
        }
        return null;
    }
}