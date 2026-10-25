package com.yourname.genericspawners.objects;

import org.bukkit.entity.EntityType;

public enum SpawnerType {
    PIG("Pig", EntityType.PIG),
    COW("Cow", EntityType.COW),
    SHEEP("Sheep", EntityType.SHEEP),
    CHICKEN("Chicken", EntityType.CHICKEN),
    ZOMBIE("Zombie", EntityType.ZOMBIE),
    SKELETON("Skeleton", EntityType.SKELETON),
    SPIDER("Spider", EntityType.SPIDER),
    CREEPER("Creeper", EntityType.CREEPER),
    BLAZE("Blaze", EntityType.BLAZE),
    ENDERMAN("Enderman", EntityType.ENDERMAN),
    WITCH("Witch", EntityType.WITCH),
    VILLAGER("Villager", EntityType.VILLAGER),
    IRON_GOLEM("Iron Golem", EntityType.IRON_GOLEM);

    private final String formattedName;
    private final EntityType entityType;

    SpawnerType(String formattedName, EntityType entityType) {
        this.formattedName = formattedName;
        this.entityType = entityType;
    }

    public String getFormattedName() {
        return formattedName;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public static SpawnerType match(String input) {
        if (input == null || input.isEmpty()) return null;
        String clean = input.toUpperCase().replace("-", "_").replace(" ", "_").trim();

        for (SpawnerType type : values()) {
            if (type.name().equals(clean) || type.name().contains(clean) || clean.contains(type.name())) {
                return type;
            }
        }
        return null;
    }
}