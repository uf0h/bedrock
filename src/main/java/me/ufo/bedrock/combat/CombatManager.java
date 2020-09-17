package me.ufo.bedrock.combat;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import me.ufo.architect.util.Style;
import me.ufo.bedrock.Bedrock;
import me.ufo.bedrock.module.Module;
import me.ufo.shaded.it.unimi.dsi.fastutil.objects.Object2LongMap;
import me.ufo.shaded.it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class CombatManager implements Module {

  private static CombatManager instance;
  private final Bedrock plugin;

  /* CONFIG VALUES */
  private final long tagTime;
  private final String damagerTagged;
  private final String damagedTagged;
  private final String finishTagged;
  private final String timeLeftTagged;
  private final String notTagged;
  private final String commandTagged;
  private final List<String> whitelistedCommands;

  private final Object2LongMap<UUID> tagged;
  private BukkitTask taggedTask;

  public CombatManager(final Bedrock plugin) {
    this.plugin = plugin;

    final ConfigurationSection section = plugin.getConfig().getConfigurationSection("combat-module");
    this.tagTime = section.getInt("tag-time", 10) * 1000;
    this.damagerTagged = Style.translate(section.getString("lang.damager-tagged"));
    this.damagedTagged = Style.translate(section.getString("lang.damaged-tagged"));
    this.finishTagged = Style.translate(section.getString("lang.finish-tagged"));
    this.timeLeftTagged = Style.translate(section.getString("lang.time-left-tagged"));
    this.notTagged = Style.translate(section.getString("lang.not-tagged"));
    this.commandTagged = Style.translate(section.getString("lang.command-tagged"));
    this.whitelistedCommands = section.getStringList("whitelisted-commands");

    this.tagged = new Object2LongOpenHashMap<>();

    plugin.registerListeners(new CombatListener(this));
    // TODO: NMS command registration
    plugin.getCommand("combat").setExecutor(new CombatCommand());

    if (plugin.isVerboseMode()) {
      plugin.info("CombatModule enabled.");
    }
  }

  @Override
  public void onDisable() {
    this.cancelTask();
  }

  public void tag(final Player damager, final Player damaged) {
    final long tagExpireTime = System.currentTimeMillis() + tagTime;

    if (tagged.getLong(damager.getUniqueId()) != 0L) {
      tagged.put(damager.getUniqueId(), tagExpireTime);
    } else {
      tagged.put(damager.getUniqueId(), tagExpireTime);
      damager.sendMessage(Style.replace(damagerTagged, "{player}", damaged.getName()));
      damager.sendMessage(Style.replace(
        timeLeftTagged,
        "{time}",
        "" + (tagTime / 1000)
      ));
    }

    if (tagged.getLong(damaged.getUniqueId()) != 0L) {
      tagged.put(damaged.getUniqueId(), tagExpireTime);
    } else {
      tagged.put(damaged.getUniqueId(), tagExpireTime);
      damaged.sendMessage(Style.replace(damagedTagged, "{player}", damager.getName()));
      damaged.sendMessage(Style.replace(
        timeLeftTagged,
        "{time}",
        "" + (tagTime / 1000)
      ));
    }

    this.triggerTaggedTask();
  }

  public void untag(final Player player, final boolean notify) {
    tagged.removeLong(player.getUniqueId());

    if (notify) {
      player.sendMessage(finishTagged);
    }
  }

  public boolean isTagged(final Player player) {
    return tagged.getLong(player.getUniqueId()) != 0L;
  }

  private BukkitRunnable getTaggedTask() {
    return new BukkitRunnable() {
      @Override
      public void run() {
        plugin.info("size: " + tagged.size());

        if (tagged.isEmpty()) {
          plugin.info("cancelling: " + tagged.size());
          cancelTask();
          return;
        }

        final long now = System.currentTimeMillis();

        final Iterator<Object2LongMap.Entry<UUID>> iterator = tagged.object2LongEntrySet().iterator();
        while (iterator.hasNext()) {
          final Object2LongMap.Entry<UUID> entry = iterator.next();

          if (now >= entry.getLongValue()) {
            plugin.getServer().getPlayer(entry.getKey()).sendMessage(finishTagged);
            iterator.remove();
          }
        }
      }
    };
  }

  public void triggerTaggedTask() {
    if (taggedTask == null) {
      taggedTask = this.getTaggedTask().runTaskTimer(plugin, 0L, 20L);
    }
  }

  private void cancelTask() {
    if (taggedTask != null) {
      taggedTask.cancel();
    }
    taggedTask = null;
  }

  public void sendTimeLeftTaggedMessage(final Player player) {
    final long timeUntilNoTag = tagged.getLong(player.getUniqueId());
    if (timeUntilNoTag == 0L) {
      player.sendMessage(notTagged);
    } else {
      player.sendMessage(Style.replace(
        timeLeftTagged,
        "{time}",
        "" + ((timeUntilNoTag - System.currentTimeMillis()) / 1000)
      ));
    }
  }

  public boolean checkCommandAllowed(final Player player, final String command) {
    for (final String whitelistedCommand : whitelistedCommands) {
      if (command.startsWith(whitelistedCommand)) {
        return true;
      }
    }
    player.sendMessage(commandTagged);
    return false;
  }

  public static CombatManager get() {
    if (instance == null) {
      instance = Bedrock.get().getModuleManager().getCombatManager();
      return instance;
    }

    return instance;
  }

}
