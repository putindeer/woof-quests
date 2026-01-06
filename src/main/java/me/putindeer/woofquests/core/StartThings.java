package me.putindeer.woofquests.core;

import me.putindeer.woofquests.Main;
import me.putindeer.woofquests.commands.AddProgressCommand;
import me.putindeer.woofquests.commands.ChangeDayCommand;
import me.putindeer.woofquests.commands.QuestCommand;
import me.putindeer.woofquests.commands.ReviveCommand;
import me.putindeer.woofquests.listeners.GeneralListener;

public class StartThings {
    private final Main plugin;

    public StartThings(Main plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.enable();
    }

    private void enable() {
        registerManagers();
        registerCommands();
    }

    public void disable() {
        plugin.questManager.saveData();
    }

    private void registerManagers() {
        plugin.questManager = new QuestManager(plugin);
        new GeneralListener(plugin);
    }

    private void registerCommands() {
        new QuestCommand(plugin);
        new ChangeDayCommand(plugin);
        new AddProgressCommand(plugin);
        new ReviveCommand(plugin);
    }
}
