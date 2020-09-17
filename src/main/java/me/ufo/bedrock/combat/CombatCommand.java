package me.ufo.bedrock.combat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CombatCommand implements CommandExecutor {

  @Override
  public boolean onCommand(final CommandSender sender, final Command command, final String label,
                           final String[] args) {

    if (!(sender instanceof Player)) {
      return false;
    }

    CombatManager.get().sendTimeLeftTaggedMessage((Player) sender);
    return true;
  }

}
