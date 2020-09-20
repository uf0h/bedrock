package me.ufo.bedrock.spawn;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SetSpawnCommand implements CommandExecutor {

  @Override
  public boolean onCommand(final CommandSender sender, final Command command, final String label,
                           final String[] args) {

    if (!(sender instanceof Player)) {
      return false;
    }

    if (!sender.hasPermission("bedrock.command.setspawn")) {
      return false;
    }

    SpawnManager.get().setSpawnLocation(((Player) sender).getLocation());
    sender.sendMessage(ChatColor.RED.toString() + "Spawn location has been set to your location.");
    return true;
  }

}
