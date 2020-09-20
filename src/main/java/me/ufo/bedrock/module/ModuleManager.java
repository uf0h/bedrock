package me.ufo.bedrock.module;

import me.ufo.bedrock.Bedrock;
import me.ufo.bedrock.combat.CombatManager;
import me.ufo.bedrock.spawn.SpawnManager;
import org.bukkit.configuration.ConfigurationSection;

public final class ModuleManager {

  private final Bedrock plugin;

  /* MODULES */
  private final CombatManager combatManager;
  private final SpawnManager spawnManager;

  public ModuleManager(final Bedrock plugin) {
    this.plugin = plugin;

    final ConfigurationSection section = plugin.getConfig().getConfigurationSection("modules");

    this.combatManager = section.getBoolean("combat-module", false) ? new CombatManager(plugin) : null;
    this.spawnManager = section.getBoolean("spawn-module", false) ? new SpawnManager(plugin) : null;
  }

  public void disableModules() {
    this.combatManager.onDisable();
  }

  public CombatManager getCombatManager() {
    return combatManager;
  }

  public SpawnManager getSpawnManager() {
    return spawnManager;
  }

}
