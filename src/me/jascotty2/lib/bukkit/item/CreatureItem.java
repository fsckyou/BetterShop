/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: for selling creatures through a shop
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.jascotty2.lib.bukkit.item;

import me.jascotty2.lib.util.Str;
//import com.jynxdaddy.wolfspawn_04.UpdatedWolf;
//import org.bukkit.entity.Wolf;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * @author jacob
 */
public class CreatureItem extends JItem {

    protected EntityType type;
	
	private static EntityType[] entities;

    public CreatureItem(EntityType creature) {
        this.type = creature;
        super.name = Str.titleCase(creature.getName());
        super.itemId = 4000 + type.ordinal();
    }

    public CreatureItem(EntityType creature, String name) {
        this.type = creature;
        super.name = name;
        super.itemId = 4000 + type.ordinal();
    }

	public static void init() {
		// all entities, beginning in 1.0
		entities = new EntityType[] {
						EntityType.CHICKEN, EntityType.COW,
						EntityType.CREEPER, EntityType.GHAST,
						EntityType.GIANT, 
						EntityType.PIG, EntityType.PIG_ZOMBIE,
						EntityType.SHEEP, EntityType.SKELETON,
						EntityType.SLIME, EntityType.SPIDER,
						EntityType.SQUID, EntityType.ZOMBIE,
						EntityType.WOLF,
						EntityType.CAVE_SPIDER, EntityType.ENDERMAN, EntityType.SILVERFISH,
						EntityType.ENDER_DRAGON, EntityType.VILLAGER,
						EntityType.BLAZE, EntityType.MUSHROOM_COW,
						EntityType.MAGMA_CUBE, EntityType.SNOWMAN };
		// add new entities, and allow backwards-compatibility
//		try {
//			EntityType t = EntityType.UNKNOWN;
//			// success, add to array
//			ordered = ArrayManip.arrayConcat(ordered,
//					new EntityType[]{EntityType.UNKNOWN});
//		} catch (Throwable t) {
//		}
	}
	
	public static EntityType[] getCreatures() {
		return entities;
	}
	
	public static EntityType[] getNewEntities() {
		if(OldEntityType.values().length == EntityType.values().length) {
			return new EntityType[0];
		}
		ArrayList<EntityType> unknown = new ArrayList<EntityType>();
		for(EntityType e : EntityType.values()) {
			if(OldEntityType.fromId(e.getTypeId()) == null) {
				unknown.add(e);
			}
		}
		return unknown.toArray(new EntityType[0]);
	}
	
//    public final void setID(int id) {
//        if (id < CreatureType.values().length) {
//            type = CreatureType.values()[id];
//        } else if (id >= 4000 && id < 4000 + CreatureType.values().length) {
//            type = CreatureType.values()[id - 4000];
//        } else {
//            type = CreatureType.CHICKEN;
//        }
//    }
//    public final void SetEntity(CreatureItem copy) {
//        //itemAliases.clear();
//        if (copy == null) {
//            this.type = CreatureType.CHICKEN;
//            name = type.getName();
//        } else {
//            //this.itemAliases.addAll(copy.itemAliases);
//            this.type = copy.type;
//            this.name = copy.name;
//        }
//    }
    public boolean equals(CreatureItem e) {
        return type == e.type;
    }

    public boolean equals(EntityType e) {
        return type == e;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof CreatureItem) {
            return equals((CreatureItem) obj);
        } else if (obj instanceof JItem) {
            return equals((JItem) obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.type.ordinal();
        return hash;
    }

//    public static CreatureItem getCreature(String search) {
//        search = search.trim().toLowerCase();
//        ///for (CreatureItem c : CreatureItem.values()) {
//        for (CreatureType c : CreatureType.values()) {
//            if (c.toString().equalsIgnoreCase(search)
//                    || (creatureAliases.containsKey(c.ordinal())
//                    && creatureAliases.get(c.ordinal()).contains(search))) {
//                return new CreatureItem(c);
//            }
//        }
//        return null;
//    }
//    public static boolean creatureExists(String search) {
////        for (CreatureType c : CreatureType.values()) {
////            if (c.toString().equalsIgnoreCase(search)
////                    || (creatureAliases.containsKey(c.ordinal())
////                    && creatureAliases.get(c.ordinal()).contains(search))) {
////                return true;
////            }
////
////        }
//        return getCreature(search) != null;
//    }
    public static boolean creatureExists(int search) {
        if (search >= 4000) {
            search -= 4000;
        }
        return search >= 0 && search < entities.length;
    }

    public static CreatureItem getCreature(int search) {
        if (search >= 4000) {
            search -= 4000;
        }
        return search >= 0 && search < entities.length
                ? new CreatureItem(entities[search])
                : new CreatureItem(EntityType.CHICKEN);
    }

    public static EntityType getCreatureType(int search) {
        if (search >= 4000) {
            search -= 4000;
        }
        return search >= 0 && search < entities.length
                ? entities[search]
                : EntityType.CHICKEN;
    }

    public void spawnNewWithOwner(Player owner) {
        //CreatureType toSpawn
        Location loc = owner.getLocation();

		LivingEntity e = owner.getWorld().spawnCreature(loc, type);
        if (e instanceof Creature){ //type == CreatureType.SLIME || type == CreatureType.GHAST) {
            addFriends(owner, e);
        } else {
            Creature creature = (Creature) e;

            if (creature instanceof CraftWolf) {
                ((CraftWolf) creature).setOwner(owner);
                creature.setHealth(20);
            } else {
                addFriends(owner, creature);
            }
        }

        //MonsterTamer.writeUsers();
    }

    public static void spawnNewWithOwner(Player owner, EntityType toSpawn) {

        Location loc = owner.getLocation();

        LivingEntity c = owner.getWorld().spawnCreature(loc, toSpawn);

        if (c instanceof CraftWolf) {
            ((CraftWolf) c).setOwner(owner);
            c.setHealth(20);
            //UpdatedWolf w = new UpdatedWolf((Wolf) c);
            //Logger.getAnonymousLogger().info(String.valueOf(w.getHandle().health) + w.getHandle().y());
            //w.setOwner(owner.getName());
            //Logger.getAnonymousLogger().info("spawning owner = " + owner.getName());
            //Logger.getAnonymousLogger().info(w.toString());
            //Logger.getAnonymousLogger().info(String.valueOf(w.getHandle().health) + w.getHandle().y());
            addFriends(owner, c);
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

    public static void addFriends(Player p, LivingEntity c) {
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

    public static class EntityListen implements Listener {

		@EventHandler
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

		@EventHandler
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

