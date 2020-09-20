package me.ufo.bedrock.combat;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import me.ufo.architect.util.Style;
import me.ufo.bedrock.Bedrock;
import me.ufo.bedrock.module.Module;
import me.ufo.shaded.it.unimi.dsi.fastutil.objects.Object2LongMap;
import me.ufo.shaded.it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import me.ufo.shaded.it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import me.ufo.shaded.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class CombatManager implements Module {

  private final Bedrock plugin;
  private static CombatManager instance;

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
  private final Object2ObjectMap<UUID, Npc> npcs;
  private BukkitTask taggedTask;
  private BukkitTask npcNameTask;

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
    this.npcs = new Object2ObjectOpenHashMap<>();

    plugin.registerListeners(new CombatListener(plugin, this));
    // TODO: NMS command registration
    plugin.getCommand("combat").setExecutor(new CombatCommand());

    if (plugin.isVerboseMode()) {
      plugin.info("CombatModule enabled.");
    }
  }

  @Override
  public void onDisable() {
    this.cancelTaggedTask();
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

  public boolean untag(final Player player, final boolean notify) {
    if (tagged.removeLong(player.getUniqueId()) != 0L) {
      Bukkit.getLogger().info("untagged");
      if (notify) {
        player.sendMessage(finishTagged);
      }
      return true;
    }

    return false;
  }

  public boolean isTagged(final Player player) {
    return tagged.getLong(player.getUniqueId()) != 0L;
  }

  private BukkitRunnable getTaggedTask() {
    return new BukkitRunnable() {
      @Override
      public void run() {
        if (tagged.isEmpty()) {
          cancelTaggedTask();
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

  private void cancelTaggedTask() {
    if (taggedTask != null) {
      taggedTask.cancel();
      taggedTask = null;
    }
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

  public void spawnNpc(final Player player) {
    Bukkit.getLogger().info("spawning npc");
    final Location location = player.getLocation();
    final Npc npc = new Npc(
      player.getUniqueId(),
      player.getName(),
      location.getWorld().spawnEntity(location, EntityType.VILLAGER)
    );
    final LivingEntity entity = (LivingEntity) npc.getEntity();

    // TODO: check if nametag is always visible
    entity.setCustomNameVisible(true);
    npc.setCustomName();
    entity.setMetadata("COMBAT_NPC", new FixedMetadataValue(plugin, true));
    entity.setCanPickupItems(false);
    entity.setHealth(player.getHealth());
    entity.setMaxHealth(player.getMaxHealth());

    for (final ItemStack armor : player.getInventory().getArmorContents()) {
      if (armor != null && armor.getType() != Material.AIR) {
        npc.getItems().add(armor);
      }
    }

    for (final ItemStack item : player.getInventory().getContents()) {
      if (item != null && item.getType() != Material.AIR) {
        npc.getItems().add(item);
      }
    }

    npcs.put(entity.getUniqueId(), npc);

    this.triggerNpcNameTask();
  }

  public void removeNpc(final UUID uniqueId) {
    npcs.remove(uniqueId).destroy();
  }

  public Npc getNpc(final UUID uniqueId) {
    return npcs.get(uniqueId);
  }

  public Npc getNpcByPlayerUniqueId(final UUID uniqueId) {
    for (final Object2ObjectMap.Entry<UUID, Npc> entry : npcs.object2ObjectEntrySet()) {
      final Npc npc = entry.getValue();
      if (npc.getUniqueId().equals(uniqueId)) {
        return npc;
      }
    }

    return null;
  }

  private BukkitRunnable getNpcNameTask() {
    return new BukkitRunnable() {
      @Override
      public void run() {
        if (npcs.isEmpty()) {
          cancelNpcNameTask();
          return;
        }

        final Iterator<Object2ObjectMap.Entry<UUID, Npc>> iterator = npcs.object2ObjectEntrySet().iterator();
        while (iterator.hasNext()) {
          final Npc npc = iterator.next().getValue();

          if (npc.decrementSecondsLeft() <= 0) {
            npc.destroy();
            iterator.remove();
            return;
          }

          npc.setCustomName();
        }
      }
    };
  }

  private void triggerNpcNameTask() {
    if (npcNameTask == null) {
      npcNameTask = this.getNpcNameTask().runTaskTimer(plugin, 0L, 20L);
    }
  }

  private void cancelNpcNameTask() {
    if (npcNameTask != null) {
      npcNameTask.cancel();
      npcNameTask = null;
    }
  }

  public static CombatManager get() {
    if (instance == null) {
      instance = Bedrock.get().getModuleManager().getCombatManager();
      return instance;
    }

    return instance;
  }

}
