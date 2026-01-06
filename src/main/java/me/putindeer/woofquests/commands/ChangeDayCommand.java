package me.putindeer.woofquests.commands;

import me.putindeer.woofquests.Main;
import me.putindeer.woofquests.core.QuestManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChangeDayCommand implements CommandExecutor {
    private final QuestManager quest;

    public ChangeDayCommand(Main plugin) {
        this.quest = plugin.questManager;
        Objects.requireNonNull(plugin.getCommand("changeday")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!quest.isEventStarted()) {
            quest.startEvent();
        } else {
            quest.changeDay();
        }
        return true;
    }
}
