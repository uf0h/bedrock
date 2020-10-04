package me.ufo.bedrock.module;

import me.ufo.bedrock.Bedrock;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;

public interface Module {

  default void registerCommands(final String[] names, final CommandExecutor... commands) {
    final Bedrock plugin = Bedrock.get();
    final int size = commands.length;
    for (int i = 0; i < size; i++) {
      plugin.getCommand(names[i]).setExecutor(commands[i]);
    }
  }

  default void registerListeners(final Listener... listeners) {
    final Bedrock plugin = Bedrock.get();
    for (final Listener listener : listeners) {
      plugin.getServer().getPluginManager().registerEvents(listener, Bedrock.get());
    }
  }

  default void onDisable() {

  }

}
