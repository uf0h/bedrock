package me.ufo.bedrock.combat;

import java.util.UUID;
import me.ufo.bedrock.Bedrock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class CombatListener implements Listener {

  private final Bedrock plugin;
  private final CombatManager manager;

  private final Vector ZERO_VECTOR;

  public CombatListener(final Bedrock plugin, final CombatManager manager) {
    this.plugin = plugin;
    this.manager = manager;

    this.ZERO_VECTOR = new Vector();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent event) {
    final Entity damagedEntity = event.getEntity();

    if (damagedEntity.hasMetadata("COMBAT_NPC")) {
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        if (!damagedEntity.isDead()) {
          damagedEntity.setVelocity(ZERO_VECTOR);
        }
      }, 0L);
    }

    if (!(damagedEntity instanceof Player)) {
      return;
    }

    final Player damaged = (Player) damagedEntity;

    // if damaged player is dead, no need to tag
    if (damaged.getHealth() <= 0 || damaged.isDead()) {
      return;
    }

    final Entity damagerEntity = event.getDamager();

    // if damager and damaged are same player
    if (damagerEntity.getUniqueId().equals(damaged.getUniqueId())) {
      return;
    }

    final Player damager;

    if (damagerEntity instanceof Tameable) {
      final AnimalTamer owner = ((Tameable) damagerEntity).getOwner();
      if (owner instanceof Player) {
        damager = (Player) event.getDamager();
      } else {
        return;
      }
    } else if (damagerEntity instanceof Player) {
      damager = (Player) event.getDamager();
    } else {
      return;
    }

    Bukkit.getLogger().info("damager: " + damager.getName());
    Bukkit.getLogger().info("damaged: " + damaged.getName());

    manager.tag(damager, damaged);
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onPlayerCommandPreprocessEvent(final PlayerCommandPreprocessEvent event) {
    final Player player = event.getPlayer();

    if (player.hasPermission("bedrock.combat.bypass")) {
      return;
    }

    if (manager.isTagged(player)) {
      if (!manager.checkCommandAllowed(player, event.getMessage())) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerJoinEvent(final PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    final Npc npc = manager.getNpcByPlayerUniqueId(player.getUniqueId());

    if (npc == null) {
      Bukkit.getLogger().info("null join");
      return;
    }

    if (npc.getEntity().isDead()) {
      npc.removeEntity();
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        Bukkit.getLogger().info("respawning...");
        // TODO: teleport to spawn
      }, 2L);

      player.getInventory().clear();
      player.getInventory().setArmorContents(null);
      player.setExp(0F);
      player.setHealth(player.getMaxHealth());
      player.setFoodLevel(20);
    }

    manager.removeNpc(npc.getEntity().getUniqueId());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityDeathEvent(final EntityDeathEvent event) {
    final Entity entity = event.getEntity();

    if (entity instanceof Player) {
      return;
    }

    if (entity.hasMetadata("COMBAT_NPC")) {
      final UUID uniqueId = entity.getUniqueId();
      final Npc npc = manager.getNpc(uniqueId);
      final Location location = entity.getLocation();

      for (final ItemStack item : npc.getItems()) {
        location.getWorld().dropItem(location, item);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerDeathEvent(final PlayerDeathEvent event) {
    manager.untag(event.getEntity(), false);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuitEvent(final PlayerQuitEvent event) {
    // TODO: check if tagged then spawn npc
    final Player player = event.getPlayer();

    if (manager.untag(player, false)) {
      manager.spawnNpc(player);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerKickEvent(final PlayerKickEvent event) {
    // TODO: check if tagged then spawn npc
    final Player player = event.getPlayer();

    if (manager.untag(player, false)) {
      manager.spawnNpc(player);
    }
  }

}
