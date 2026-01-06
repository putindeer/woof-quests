package me.putindeer.woofquests.commands;

import me.putindeer.woofquests.Main;
import me.putindeer.woofquests.core.QuestManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ReviveCommand implements TabExecutor {
    private final Main plugin;
    private final QuestManager questManager;

    public ReviveCommand(Main plugin) {
        this.plugin = plugin;
        this.questManager = plugin.questManager;
        Objects.requireNonNull(plugin.getCommand("revive")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length != 1) {
            plugin.utils.message(sender, "<red>Uso: <gray>/revive <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore()) {
            plugin.utils.message(sender, "<red>Jugador no válido.");
            return true;
        }

        questManager.addAlivePlayer(target);

        plugin.utils.message(sender, "<green>Has revivido a <white>" + target.getName() + "</white>.");

        if (target.isOnline()) {
            plugin.utils.message(target.getPlayer(), "<green>Has sido revivido.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(player -> !questManager.isPlayerAlive(player.getUniqueId()))
                    .map(Player::getName)
                    .toList();
        }
        return List.of();
    }
}
