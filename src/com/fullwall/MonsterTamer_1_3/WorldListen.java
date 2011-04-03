package com.fullwall.MonsterTamer_1_3;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;

public class WorldListen extends WorldListener {

    private static EntityListen entityListener;
    private ConcurrentHashMap<Location, ArrayList<String>> toRespawn = new ConcurrentHashMap<Location, ArrayList<String>>();

    public WorldListen(EntityListen entListener) {
        entityListener = entListener;
    }

    @Override
    public void onChunkUnload(ChunkUnloadEvent e) {
        if (MonsterTamer.stopDespawning == true) {
            for (Entity entity : e.getChunk().getEntities()) {
                if (!(entity instanceof Player)
                        && entity instanceof LivingEntity
                        && entity instanceof Creature
                        && MonsterTamer.friendlies.contains(""
                        + entity.getEntityId())) {
                    LivingEntity living = (LivingEntity) entity;
                    String playerName = "";
                    Location loc = living.getLocation();
                    ArrayList<String> toPut = new ArrayList<String>();
                    toPut.add(MonsterTamer.checkMonsters(living));
                    for (Entry<String, ArrayList<String>> i : MonsterTamer.friends.entrySet()) {
                        if (i.getValue().contains("" + living.getEntityId())) {
                            playerName = i.getKey();
                            i.getValue().remove("" + i.getValue().indexOf(living.getEntityId()));
                            break;
                        }
                    }
                    toPut.add(playerName);

                    if (MonsterTamer.targets.containsKey("" + living.getEntityId())) {
                        toPut.add(MonsterTamer.targets.get("" + living.getEntityId()));
                        MonsterTamer.targets.remove("" + living.getEntityId());
                    } else {
                        toPut.add("");
                    }

                    String ownerName = "";
                    for (Entry<String, ArrayList<Integer>> i : MonsterTamer.followers.entrySet()) {
                        if (i.getValue().contains(living.getEntityId())) {
                            ownerName = i.getKey();
                            i.getValue().remove(i.getValue().indexOf(living.getEntityId()));
                            break;
                        }
                    }
                    toPut.add(ownerName);
                    MonsterTamer.friendlies.remove(MonsterTamer.friendlies.indexOf("" + living.getEntityId()));
                    toRespawn.put(loc, toPut);
                }
            }
            return;
        }
    }

    @Override
    public void onChunkLoad(ChunkLoadEvent e){
        if (MonsterTamer.stopDespawning == true && toRespawn.size() > 0) {
            for (Entry<Location, ArrayList<String>> entry : toRespawn.entrySet()) {
                if (e.getChunk().getWorld().getChunkAt(entry.getKey()).equals(e.getChunk())) {
                    Creature monster = (Creature) entry.getKey().getWorld().spawnCreature(
                            entry.getKey(),
                            CreatureType.fromName(entry.getValue().get(
                            0)));
                    MonsterTamer.friendlies.add("" + monster.getEntityId());
                    String name = entry.getValue().get(1);
                    if (!name.isEmpty()) {
                        ArrayList<String> array = MonsterTamer.friends.get(name);
                        array.add("" + monster.getEntityId());
                        MonsterTamer.friends.put(name, array);
                    }
                    name = entry.getValue().get(2);
                    if (!name.isEmpty()) {
                        MonsterTamer.targets.put("" + monster.getEntityId(),
                                name);
                    }
                    name = entry.getValue().get(3);
                    if (!name.isEmpty()) {
                        ArrayList<Integer> array = MonsterTamer.followers.get(name);
                        array.add(monster.getEntityId());
                        MonsterTamer.followers.put(name, array);
                    }
                    toRespawn.remove(entry.getKey());
                    // friends(!) targets, followers, friendlies
                }
            }
        }
    }
}
