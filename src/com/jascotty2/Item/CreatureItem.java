/**
 * Programmer: Jacob Scott
 * Program Name: CreatureItem
 * Description:
 * Date: Apr 1, 2011
 */
package com.jascotty2.Item;

import com.fullwall.MonsterTamer_1_3.MonsterTamer;
import com.jynxdaddy.wolfspawn_04.UpdatedWolf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

/**
 * @author jacob
 */
public class CreatureItem {

    // Item Information
    protected CreatureType itemId;
    public String name = "";
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
            Logger.getAnonymousLogger().info("spawning owner = " + owner.getName());
            Logger.getAnonymousLogger().info(w.toString());
        } else {
            Logger.getAnonymousLogger().info("spawning " + itemId);
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
            w.setOwner(owner.getName());
            Logger.getAnonymousLogger().info("spawning owner = " + owner.getName());
            Logger.getAnonymousLogger().info(w.toString());
        } else if (c instanceof Creature) {
            Creature creature = (Creature) c;

            Logger.getAnonymousLogger().info("spawning " + toSpawn);
            addFriends(owner, creature);
        }
        //MonsterTamer.writeUsers();
    }

    public static void addFriends(Player p, Creature c) {
        ArrayList<String> array = new ArrayList<String>();
        if (MonsterTamer.friends.containsKey(p.getName())) {
            array = MonsterTamer.friends.get(p.getName());
        }
        array.add("" + c.getEntityId());
        MonsterTamer.friends.put(p.getName(), array);
        MonsterTamer.friendlies.add("" + c.getEntityId());
        return;
    }
} // end class CreatureItem

