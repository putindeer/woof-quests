package me.putindeer.woofquests.core;

import me.putindeer.woofquests.Main;
import me.putindeer.woofquests.commands.*;
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
        plugin.mannequin.cleanupAll();
    }

    private void registerManagers() {
        plugin.questManager = new QuestManager(plugin);
        plugin.mannequin = new MannequinCombatSystem(plugin);
        new GeneralListener(plugin);
    }

    private void registerCommands() {
        new QuestCommand(plugin);
        new ChangeDayCommand(plugin);
        new AddProgressCommand(plugin);
        new ReviveCommand(plugin);
        new PlayersCommand(plugin);
    }
}
