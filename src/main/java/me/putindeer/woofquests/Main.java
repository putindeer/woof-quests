package me.putindeer.woofquests;

import lombok.Getter;
import me.putindeer.api.util.PluginUtils;
import me.putindeer.woofquests.core.MannequinCombatSystem;
import me.putindeer.woofquests.core.QuestManager;
import me.putindeer.woofquests.core.StartThings;
import org.bukkit.entity.Mannequin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    @Getter public static Main instance;
    public PluginUtils utils;
    public StartThings start;
    public QuestManager questManager;
    public MannequinCombatSystem mannequin;
    @Override
    public void onEnable() {
        instance = this;
        utils = new PluginUtils(this, "<sprite:gui:hud/heart/hardcore_full> <reset>");
        start = new StartThings(this);
    }

    @Override
    public void onDisable() {
        start.disable();
    }
}
