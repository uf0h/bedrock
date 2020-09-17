package me.ufo.bedrock.combat;

import org.bukkit.Bukkit;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class CombatListener implements Listener {

  private final CombatManager manager;

  public CombatListener(final CombatManager manager) {
    this.manager = manager;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    final Player damaged = (Player) event.getEntity();

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

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerDeathEvent(final PlayerDeathEvent event) {
    manager.untag(event.getEntity(), false);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuitEvent(final PlayerQuitEvent event) {
    // TODO: check if tagged then spawn npc

    manager.untag(event.getPlayer(), false);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerKickEvent(final PlayerKickEvent event) {
    // TODO: check if tagged then spawn npc

    manager.untag(event.getPlayer(), false);
  }

}
