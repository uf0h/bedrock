package me.ufo.bedrock.spawn;

import java.io.File;
import java.io.IOException;
import me.ufo.bedrock.Bedrock;
import me.ufo.bedrock.combat.CombatCommand;
import me.ufo.bedrock.combat.CombatManager;
import me.ufo.bedrock.module.Module;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class SpawnManager implements Module {


  private final Bedrock plugin;
  private static SpawnManager instance;

  private Location spawnLocation;

  public SpawnManager(final Bedrock plugin) {
    this.plugin = plugin;

    final File spawnFile = new File(plugin.getDataFolder().getPath() + "/spawn.yml");
    if (!spawnFile.exists()) {
      plugin.saveResource("spawn.yml", false);
    }

    final YamlConfiguration config = new YamlConfiguration();

    try {
      config.load(spawnFile);
    } catch (final IOException | InvalidConfigurationException e) {
      e.printStackTrace();
    }

    final String[] location = ((String) config.get("spawn-location")).split(",");
    final World world = plugin.getServer().getWorld(location[0]);
    if (location[1].isEmpty()) {
      spawnLocation = world.getSpawnLocation().add(new Vector(0.5, 0, 0.5));

      config.set("spawn-location", this.locationToString(spawnLocation));
      try {
        config.save(spawnFile);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    } else {
      spawnLocation = new Location(
        world,
        Double.parseDouble(location[1]),
        Double.parseDouble(location[2]),
        Double.parseDouble(location[3]),
        Float.parseFloat(location[4]),
        Float.parseFloat(location[5])
      );
    }

    // TODO: NMS command registration
    plugin.getCommand("setspawn").setExecutor(new SetSpawnCommand());

    if (plugin.isVerboseMode()) {
      plugin.info("SpawnModule enabled.");
    }
  }

  public void teleport(final Player player) {
    player.teleport(spawnLocation);
  }

  public void setSpawnLocation(final Location location) {
    spawnLocation = location;

    final File spawnFile = new File(plugin.getDataFolder().getPath() + "/spawn.yml");
    if (!spawnFile.exists()) {
      plugin.saveResource("spawn.yml", false);
    }

    final YamlConfiguration config = new YamlConfiguration();

    try {
      config.load(spawnFile);
    } catch (final IOException | InvalidConfigurationException e) {
      e.printStackTrace();
    }

    config.set("spawn-location", this.locationToString(location));
    try {
      config.save(spawnFile);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private String locationToString(final Location location) {
    return location.getWorld().getName() + "," +
           location.getX() + "," +
           location.getY() + "," +
           location.getZ() + "," +
           location.getYaw() + "," +
           location.getPitch();
  }

  public static SpawnManager getNullable() {
    return instance;
  }

  public static SpawnManager get() {
    if (instance == null) {
      instance = Bedrock.get().getModuleManager().getSpawnManager();
      return instance;
    }

    return instance;
  }

}
