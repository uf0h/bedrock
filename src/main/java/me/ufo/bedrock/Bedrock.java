package me.ufo.bedrock;

import java.util.logging.Logger;
import me.ufo.bedrock.module.ModuleManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Bedrock extends JavaPlugin {

  private static Bedrock plugin;

  private Logger logger;
  private ModuleManager moduleManager;
  private boolean verbose;

  public Bedrock() {
    plugin = this;
  }

  @Override
  public void onLoad() {
    saveDefaultConfig();
    logger = this.getLogger();
    verbose = this.getConfig().getBoolean("verbose", false);
  }

  @Override
  public void onEnable() {
    moduleManager = new ModuleManager(this);
  }

  @Override
  public void onDisable() {
    moduleManager.disableModules();
  }

  public void registerListeners(final Listener... listeners) {
    final PluginManager pm = this.getServer().getPluginManager();
    for (final Listener listener : listeners) {
      pm.registerEvents(listener, this);
    }
  }

  public ModuleManager getModuleManager() {
    return moduleManager;
  }

  public boolean isVerboseMode() {
    return verbose;
  }

  public void toggleVerboseMode() {
    verbose = !verbose;
  }

  public void info(final String in) {
    logger.info(in);
  }

  public void error(final String in) {
    logger.severe(in);
  }

  public static Bedrock get() {
    return plugin;
  }

}
