package me.putindeer.woofquests.commands;

import me.putindeer.woofquests.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PlayersCommand implements TabExecutor {
    private final Main plugin;

    public PlayersCommand(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("players")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            plugin.utils.message(sender, "<red>Uso: <gray>/players <dead|alive>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "dead" -> plugin.utils.message(sender, plugin.questManager.getDeadPlayers().stream()
                    .map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).collect(Collectors.joining(", ")));
            case "alive" -> plugin.utils.message(sender, plugin.questManager.getAlivePlayers().stream()
                    .map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).collect(Collectors.joining(", ")));
            default -> plugin.utils.message(sender, "<red>Uso: <gray>/players <dead|alive>");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            list.addAll(List.of("dead", "alive"));
        }

        list.removeIf(s -> true);
        Collections.sort(list);
        return list;
    }
}
