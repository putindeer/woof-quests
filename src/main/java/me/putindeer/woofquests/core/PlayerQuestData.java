package me.putindeer.woofquests.core;

import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class PlayerQuestData {
    private final UUID uuid;
    private final Map<QuestRequirement, Integer> progress;

    public PlayerQuestData(UUID uuid) {
        this.uuid = uuid;
        this.progress = new EnumMap<>(QuestRequirement.class);
    }

    public void updateProgress(QuestRequirement requirement, int amount) {
        int current = progress.getOrDefault(requirement, 0);
        progress.put(requirement, current + amount);
    }

    public int getProgress(QuestRequirement requirement) {
        return progress.getOrDefault(requirement, 0);
    }

    public boolean isCompleted(QuestRequirement requirement) {
        return getProgress(requirement) >= requirement.getRequired();
    }

    public boolean isDayCompleted(int day) {
        QuestRequirement[] requirements = QuestRequirement.getRequirementsForDay(day);

        for (QuestRequirement req : requirements) {
            if (!isCompleted(req)) {
                return false;
            }
        }

        return requirements.length > 0;
    }

    public void clearProgress() {
        progress.clear();
    }
}