package com.fullwall.MonsterTamer_1_3;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class EntityListen extends EntityListener {

    public EntityListen(){
    }

    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == DamageCause.FIRE_TICK
                || event.getCause() == DamageCause.FIRE) {
            if (MonsterTamer.friendlies.contains(""
                    + event.getEntity().getEntityId())) {
                event.setCancelled(true);
                event.getEntity().setFireTicks(0);
                return;
            }
        }
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        if (e.getDamager() instanceof LivingEntity
                && e.getEntity() instanceof Player
                && MonsterTamer.friends.get(((Player) e.getEntity()).getName()) != null) {
            ArrayList<String> array = MonsterTamer.friends.get(((Player) e.getEntity()).getName());
            List<LivingEntity> livingEntities = e.getEntity().getWorld().getLivingEntities();
            for (LivingEntity i : livingEntities) {
                if (i instanceof Creature
                        && array.contains("" + i.getEntityId())) {
                    Creature c = (Creature) i;
                    c.setTarget((LivingEntity) e.getDamager());
                }
            }
            return;
        }/*
        if (!(e.getDamager() instanceof Player)) {
            return;
        }
        if (!(e.getEntity() instanceof LivingEntity)
                && (!(e.getEntity() instanceof Animals) || !(e.getEntity() instanceof Monster))) {
            return;
        }
        LivingEntity le = (LivingEntity) e.getEntity();
        Player player = (Player) e.getDamager();

        if ((MonsterTamer.catchItems.get(""
                + player.getItemInHand().getTypeId()) == null)) {
            return;
        }
        if (MonsterTamer.monsterChances.get(checkMonsters(le)) == null) {
            return;
        }
        if (!Permission.check(player)) {
            return;
        }
        String name = checkMonsters(le);
        String isCatching;
        if (MonsterTamer.playerCatching.containsKey(player.getName())) {
            isCatching = MonsterTamer.playerCatching.get(player.getName());
        } else {
            isCatching = null;
        }
        if (isCatching != null) {
            if (MonsterTamer.playerCatching.get(player.getName()).equals(name)) {
                return;
            }
        }
        MonsterTamer.playerCatching.put(player.getName(), name);
        double chance = MonsterTamer.monsterChances.get(checkMonsters(le));
        if (chance < 3.0D) {
            chance = 3.0D;
        }
        double bonus = (MonsterTamer.catchItems.get(""
                + player.getItemInHand().getTypeId()).doubleValue());
        double ran = random.nextDouble();
        ran *= 100;
        // magic pokemon chance to catch.
        chance = ((((3 * 20 - 2 * le.getHealth()) * chance * bonus) / (3 * 20) / 256) * 100);
        // friendlies get a 100% catch rate.
        ArrayList<String> temparray = MonsterTamer.friends.get(player.getName());
        if (temparray != null
                && temparray.contains("" + e.getEntity().getEntityId())) {
            chance = 9000;
        }
        if (ran > chance) {
            if (ran - chance <= 10) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "No! The " + name
                        + " escaped. So close!");
            } else if (ran - chance <= 30) {
                player.sendMessage(ChatColor.AQUA + "Nearly had it!");
            } else if (ran - chance <= 60) {
                player.sendMessage(ChatColor.BLUE
                        + "Oh no! Couldn't capture the " + name + "!");
            } else if (ran - chance <= 80) {
                player.sendMessage(ChatColor.GREEN
                        + "Nowhere near catching the " + name + ".");
            } else {
                player.sendMessage(ChatColor.GRAY + "Failed to catch the "
                        + name + ".");
            }
            MonsterTamer.playerCatching.remove(player.getName());
            return;
        } else {
            if (System.currentTimeMillis() < 1000L + this.delay) {
                player.sendMessage(ChatColor.DARK_AQUA
                        + "You were moving too fast and stumbled.");
                MonsterTamer.playerCatching.remove(player.getName());
                return;
            }
            ArrayList<String> array = new ArrayList<String>();
            if (MonsterTamer.playerMonsters.get(player.getName()) != null) {
                array = MonsterTamer.playerMonsters.get(player.getName());
            }
            this.delay = System.currentTimeMillis();
            if ((array.size() / 2) > MonsterTamer.limit) {
                player.sendMessage(ChatColor.RED
                        + "You don't have enough room for more monsters! The limit is "
                        + MonsterTamer.limit + ".");
                MonsterTamer.playerCatching.remove(player.getName());
                return;
            }
            // get rid of the monster
            if (MonsterTamer.friends.containsKey(player.getName())) {
                ArrayList<String> friendsArray = MonsterTamer.friends.get(player.getName());
                if (friendsArray.contains("" + le.getEntityId())) {
                    friendsArray.remove("" + le.getEntityId());
                }
            }
            // stop other monsters overwriting the friendly ID
            if (MonsterTamer.friendlies.contains("" + le.getEntityId())) {
                MonsterTamer.friendlies.remove("" + le.getEntityId());
            }
            le.remove();

            array.add(name);
            array.add("" + player.getItemInHand().getTypeId());
            MonsterTamer.playerMonsters.put(player.getName(), array);
            player.sendMessage(ChatColor.GOLD + "You caught a " + name + "!");

        }
        MonsterTamer.playerCatching.remove(player.getName());
        MonsterTamer.writeUsers();//*/
    }

    @Override
    public void onEntityTarget(EntityTargetEvent e) {
        if ((e.getTarget() instanceof Player)) {
            Player p = (Player) e.getTarget();
            if (MonsterTamer.friendlies.contains(""
                    + e.getEntity().getEntityId())) {
                String name = MonsterTamer.targets.get("" + e.getEntity().getEntityId());
                if (name == null || name.isEmpty()) {
                    e.setCancelled(true);
                    return;
                }
                if (!(name.equals(p.getName()))) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }
}
