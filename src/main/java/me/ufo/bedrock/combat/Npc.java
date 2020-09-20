package me.ufo.bedrock.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.ufo.bedrock.Bedrock;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public final class Npc {

  private final UUID uniqueId;
  private final String name;
  private Entity entity;
  private List<ItemStack> items;
  private int secondsLeft;

  public Npc(final UUID uniqueId, final String name, final Entity entity) {
    this.uniqueId = uniqueId;
    this.name = name;
    this.entity = entity;
    this.items = new ArrayList<>();
    this.secondsLeft = 60;
  }

  public void setCustomName() {
    if (!entity.isDead()) {
      entity.setCustomName(name + " " + ChatColor.RED.toString() + secondsLeft + "s");
    }
  }

  public int decrementSecondsLeft() {
    secondsLeft = secondsLeft - 1;
    return secondsLeft;
  }

  public void destroy() {
    entity.removeMetadata("COMBAT_NPC", Bedrock.get());
    entity.remove();
    entity = null;
    items = null;
  }

  public UUID getUniqueId() {
    return uniqueId;
  }

  public Entity getEntity() {
    return entity;
  }

  public List<ItemStack> getItems() {
    return items;
  }

}
