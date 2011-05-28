/**
 * Programmer: Jacob Scott
 * Program Name: CreatureItem
 * Description:
 * Date: Apr 1, 2011
 */
package com.jascotty2.Item;

import com.jynxdaddy.wolfspawn_04.UpdatedWolf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * @author jacob
 */
public class CreatureItem {

    // Item Information
    protected CreatureType itemId;
    public String name = "";
    public Player owner = null;
    //private LinkedList<String> itemAliases = new LinkedList<String>();
    public static HashMap<Integer, ArrayList<String>> creatureAliases = new HashMap<Integer, ArrayList<String>>();

    public CreatureItem() {
        setID(0);
        name = itemId.getName();
    }

    public CreatureItem(String name) {
        itemId = CreatureType.fromName(name);
        name = itemId.getName();
    }

    public CreatureItem(Item copy) {
        setID(copy.ID());
        name = itemId.getName();
    }

    public CreatureItem(int id) {
        setID(id);
        name = itemId.getName();
    }

    public CreatureItem(int id, String name) {
        setID(id);
        this.name = name;
    }

    public int ID() {
        return 4000 + itemId.ordinal();
    }

    public final void setID(int id) {
        if (id < CreatureType.values().length) {
            itemId = CreatureType.values()[id];
        } else if (id >= 4000 && id < 4000 + CreatureType.values().length) {
            itemId = CreatureType.values()[id - 4000];
        } else {
            itemId = CreatureType.CHICKEN;
        }
    }

    public final void SetEntity(CreatureItem copy) {
        //itemAliases.clear();
        if (copy == null) {
            this.itemId = CreatureType.CHICKEN;
            name = itemId.getName();
        } else {
            //this.itemAliases.addAll(copy.itemAliases);
            this.itemId = copy.itemId;
            this.name = copy.name;
        }
    }

    public void AddAlias(String a) {
        //itemAliases.add(a.trim().toLowerCase());
        if (!creatureAliases.containsKey(itemId.ordinal())) {
            creatureAliases.put(itemId.ordinal(), new ArrayList<String>());
        }
        creatureAliases.get(itemId.ordinal()).add(a);
    }

    public boolean HasAlias(String a) {
        ///return itemAliases.contains(a.trim().toLowerCase());
        return creatureAliases.containsKey(itemId.ordinal())
                && creatureAliases.get(itemId.ordinal()).contains(a);
    }

    public boolean equals(CreatureItem e) {
        return itemId == e.itemId;
    }

    public boolean equals(Item e) {
        return itemId.ordinal() + 4000 == e.ID();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof CreatureItem) {
            return equals((CreatureItem) obj);
        } else if (obj instanceof Item) {
            return equals((Item) obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.itemId.ordinal();
        return hash;
    }

    @Override
    public String toString() {
        return itemId.toString();
    }

    /*public enum EntityType{
    Chicken, Cow, Pig, Sheep, Wolf, Squid,
    Creeper, Skeleton, Spider, Zombie, PigZombie // , Slime, Ghast, Giant
    }*/
    public static boolean creatureExists(String search) {
        for (CreatureType c : CreatureType.values()) {
            if (c.toString().equalsIgnoreCase(search)
                    || (creatureAliases.containsKey(c.ordinal())
                    && creatureAliases.get(c.ordinal()).contains(search))) {
                return true;
            }

        }
        return false;
    }

    public static boolean creatureExists(int search) {
        if (search >= 4000) {
            search -= 4000;
        }
        return search >= 0 && search < CreatureType.values().length;
    }

    public static CreatureType getCreature(String search) {
        for (CreatureType c : CreatureType.values()) {
            if (c.toString().equalsIgnoreCase(search)
                    || (creatureAliases.containsKey(c.ordinal())
                    && creatureAliases.get(c.ordinal()).contains(search))) {
                return c;
            }

        }
        return CreatureType.CHICKEN;
    }

    public static CreatureType getCreature(int search) {
        if (search >= 4000) {
            search -= 4000;
        }
        return search >= 0 && search < CreatureType.values().length ? CreatureType.values()[search]
                : CreatureType.CHICKEN;
    }

    public void spawnNewWithOwner(Player owner) {
        //CreatureType toSpawn
        Location loc = owner.getLocation();

        Creature creature = (Creature) owner.getWorld().spawnCreature(loc, itemId);

        if (creature instanceof Wolf) {
            //((Wolf)creature)
            UpdatedWolf w = new UpdatedWolf((Wolf) creature);
            w.setOwner(owner.getName());
            //Logger.getAnonymousLogger().info("spawning owner = " + owner.getName());
            //Logger.getAnonymousLogger().info(w.toString());
        } else {
            //Logger.getAnonymousLogger().info("spawning " + itemId);
            addFriends(owner, creature);
        }

        //MonsterTamer.writeUsers();
    }

    public static void spawnNewWithOwner(Player owner, CreatureType toSpawn) {

        Location loc = owner.getLocation();

        LivingEntity c = owner.getWorld().spawnCreature(loc, toSpawn);


        if (c instanceof Wolf) {
            //((Wolf)creature)
            UpdatedWolf w = new UpdatedWolf((Wolf) c);
            //Logger.getAnonymousLogger().info(String.valueOf(w.getHandle().health) + w.getHandle().y());
            w.setOwner(owner.getName());
            //Logger.getAnonymousLogger().info("spawning owner = " + owner.getName());
            //Logger.getAnonymousLogger().info(w.toString());
            //Logger.getAnonymousLogger().info(String.valueOf(w.getHandle().health) + w.getHandle().y());
        } else if (c instanceof Creature) {
            Creature creature = (Creature) c;
            //Logger.getAnonymousLogger().info("spawning " + toSpawn);
            addFriends(owner, creature);
        }
    }
    // revised from MonsterTamer code:
    // player name, list of monster entity ids
    public static ConcurrentHashMap<String, ArrayList<Integer>> friends = new ConcurrentHashMap<String, ArrayList<Integer>>();
    // list of friendly entity ids
    public static ArrayList<Integer> friendlies = new ArrayList<Integer>();
    // entity id, attacking name
    public static ConcurrentHashMap<Integer, String> targets = new ConcurrentHashMap<Integer, String>();

    public static void addFriends(Player p, Creature c) {
        //ArrayList<Integer> array = friends.containsKey(p.getName()) ? friends.get(p.getName()) : new ArrayList<Integer>();
        if (friends.containsKey(p.getName())) {
            friends.get(p.getName()).add(c.getEntityId());
        } else {
            ArrayList<Integer> array = new ArrayList<Integer>();
            array.add(c.getEntityId());
            friends.put(p.getName(), array);
        }
        friendlies.add(c.getEntityId());
        return;
    }

    public static class EntityListen extends EntityListener {

        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if ((event.getCause() == DamageCause.FIRE_TICK || event.getCause() == DamageCause.FIRE)
                    && friendlies.contains(event.getEntity().getEntityId())) {
                event.setCancelled(true);
                event.getEntity().setFireTicks(0);
            } else if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
                if (e.getDamager() instanceof LivingEntity
                        && e.getEntity() instanceof Player
                        && friends.get(((Player) e.getEntity()).getName()) != null) {
                    ArrayList<Integer> array = friends.get(((Player) e.getEntity()).getName());
                    List<LivingEntity> livingEntities = e.getEntity().getWorld().getLivingEntities();
                    for (LivingEntity i : livingEntities) {
                        if (i instanceof Creature
                                && array.contains(i.getEntityId())) {
                            ((Creature) i).setTarget((LivingEntity) e.getDamager());
                        }
                    }
                }
            }
        }

        @Override
        public void onEntityTarget(EntityTargetEvent e) {
            if ((e.getTarget() instanceof Player)) {
                Player p = (Player) e.getTarget();
                if (friendlies.contains(e.getEntity().getEntityId())) {
                    String name = targets.get(e.getEntity().getEntityId());
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
} // end class CreatureItem

