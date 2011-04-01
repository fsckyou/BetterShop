package com.fullwall.MonsterTamer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.server.EntityCreature;
import net.minecraft.server.PathEntity;
import net.minecraft.server.PathPoint;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.fullwall.MonsterTamer.DirectionUtils.CompassDirection;
import org.bukkit.Server;

public class FollowersHandler implements Runnable {

    private Server pluginServer;

    public FollowersHandler(Server plugin) {
        pluginServer = plugin;
    }

    @Override
    public void run() {
        for (Entry<String, ArrayList<Integer>> i : MonsterTamer.followers.entrySet()) {
            if (pluginServer.getPlayer(i.getKey()) == null) {
                MonsterTamer.followers.remove(i.getKey());
                continue;
            }
            Player p = pluginServer.getPlayer(i.getKey());
            List<LivingEntity> list = p.getWorld().getLivingEntities();
            for (LivingEntity le : list) {
                if (le instanceof Creature
                        && i.getValue().contains(le.getEntityId())) {
                    Location loc = p.getLocation();
                    CompassDirection direction = DirectionUtils.getDirectionFromRotation(p.getLocation().getYaw());
                    Block block = DirectionUtils.getBlockBehind(p.getWorld(),
                            direction, loc.getBlockX(), loc.getBlockY(),
                            loc.getBlockZ());
                    PathPoint[] pp = {new PathPoint(block.getX(), block.getY(), block.getZ())};
                    ((EntityCreature) (((CraftEntity) le).getHandle())).a = new PathEntity(pp);
                }
            }
        }
        for (Entry<String, ArrayList<Integer>> i : MonsterTamer.waiters.entrySet()) {
            if (pluginServer.getPlayer(i.getKey()) == null) {
                MonsterTamer.waiters.remove(i.getKey());
                continue;
            }
            Player p = pluginServer.getPlayer(i.getKey());
            for (LivingEntity le : p.getWorld().getLivingEntities()) {
                if (le instanceof Creature
                        && i.getValue().contains(le.getEntityId())) {
                    Location loc = le.getLocation();
                    PathPoint[] pp = {new PathPoint(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())};
                    ((EntityCreature) (((CraftEntity) le).getHandle())).a = new PathEntity(pp);
                }
            }
        }
    }
}
