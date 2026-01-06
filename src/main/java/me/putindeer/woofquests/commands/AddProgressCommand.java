package me.putindeer.woofquests.commands;

import me.putindeer.woofquests.Main;
import me.putindeer.woofquests.core.QuestManager;
import me.putindeer.woofquests.core.QuestRequirement;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AddProgressCommand implements TabExecutor {
    private final Main plugin;
    private final QuestManager questManager;

    public AddProgressCommand(Main plugin) {
        this.plugin = plugin;
        this.questManager = plugin.questManager;
        Objects.requireNonNull(plugin.getCommand("addprogress")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length < 2) {
            plugin.utils.message(sender, "<red>Uso: /addprogress <player> <requirement>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore()) {
            plugin.utils.message(sender, "<red>Jugador no encontrado.");
            return true;
        }

        QuestRequirement requirement;
        try {
            requirement = QuestRequirement.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.utils.message(sender, "<red>Quest inválida.");
            return true;
        }

        questManager.addProgress(target.getUniqueId(), requirement);
        plugin.utils.message(sender, "<green>Progreso añadido.");

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            list.addAll(plugin.questManager.getAlivePlayers().stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).toList());
        }

        if (args.length == 2) {
            list.addAll(Arrays.stream(QuestRequirement.values())
                    .filter(quest -> quest.getDay() == questManager.getCurrentDay())
                    .map(QuestRequirement::getKey).toList());
        }

        list.removeIf(s -> s == null || !s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()));
        Collections.sort(list);
        return list;
    }
}

