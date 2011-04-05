package com.fullwall.MonsterTamer_1_3;

import java.util.ArrayList;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;

public class PlayerListen extends PlayerListener {

    //@SuppressWarnings("unused")
    //private static MonsterTamer plugin;
    //private long delay = 0;
    //public static Timer t = new Timer();

    //@SuppressWarnings("static-access")
    public PlayerListen(final MonsterTamer plugin) {
        //this.plugin = plugin;
    }
/*
    @Override
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (Permission.check(e.getPlayer())) {
            Item id = e.getItemDrop();
            if (checkMaps(id)) {
                if (System.currentTimeMillis() <= (delay + 1300)) {
                    e.getPlayer().sendMessage(
                            ChatColor.RED
                            + "You have to wait for at least a second before releasing another monster.");
                    return;
                }
                delay = System.currentTimeMillis();
                t.schedule(new RemindTask(e, id), 1250);
            }
        }
    }

    /*
     * public void onPlayerAnimation(PlayerAnimationEvent e) { if
     * (e.getAnimationType() == PlayerAnimationType.ARM_SWING) { Location loc =
     * e.getPlayer().getTargetBlock(null, 4).getLocation(); for (Entity entity :
     * e.getPlayer().getWorld().getEntities()) { if (entity instanceof Item) {
     * Location l = entity.getLocation(); if ((loc.getX() <= l.getX() + 4 &&
     * loc.getX() >= l.getX() - 4) && (loc.getY() >= l.getY() - 4 && loc.getY()
     * <= l .getY() + 4) && (loc.getZ() >= l.getZ() - 4 && loc.getZ() <= l
     * .getZ() + 4) && !getName((Item) entity, e.getPlayer()).isEmpty()) {
     * MonsterTamer.log.info("!!!!"); } } } } }
     * /
    public static void spawnFromItemDrop(PlayerDropItemEvent e, Item id) {
        if (MonsterTamer.playerMonsters.get(e.getPlayer().getName()) == null
                || ((MonsterTamer.playerMonsters.get(e.getPlayer().getName()).size()) < 2)) {
            return;
        }
        Location loc = id.getLocation();
        String name = getName(id, e.getPlayer());
        if (name.isEmpty()) {
            return;
        }
        String item = "" + id.getItemStack().getTypeId();
        if (!isInArray(name, item, e.getPlayer())) {
            return;
        }
        if (MonsterTamer.consumeItems) {
            int amount = id.getItemStack().getAmount();
            amount -= 1;
            if (amount == 0) {
                id.remove();
            } else {
                ItemStack is = id.getItemStack();
                is.setAmount(amount);
                id.setItemStack(is);
            }
        }
        try {
            CreatureType ct = CreatureType.fromName(name);
            Creature creature = (Creature) id.getWorld().spawnCreature(loc, ct);
            removeNameFromArray(name, item, e.getPlayer());
            e.getPlayer().sendMessage(
                    ChatColor.LIGHT_PURPLE
                    + "You released your "
                    + ChatColor.YELLOW
                    + name
                    + ChatColor.LIGHT_PURPLE
                    + ". You have "
                    + ChatColor.GREEN
                    + +(MonsterTamer.playerMonsters.get(
                    e.getPlayer().getName()).size() / 2)
                    + ChatColor.LIGHT_PURPLE + " monsters remaining.");
            if (Permission.friendly(e.getPlayer())) {
                addFriends(e.getPlayer(), creature);
            }

        } catch (Exception e1) {
            MonsterTamer.log.info("[MonsterTamer]: Error spawning monster.");

        }
        MonsterTamer.writeUsers();
    }

    public static void spawnFromLocation(Player p, int ID) {
        if (MonsterTamer.playerMonsters.get(p.getName()) == null
                || ((MonsterTamer.playerMonsters.get(p.getName()).size()) < 2)) {
            return;
        }
        Location loc = p.getLocation();
        String name = getName(p, ID);
        if (name.isEmpty()) {
            return;
        }
        String item = "" + ID;
        if (!isInArray(name, item, p)) {
            return;
        }
        try {
            CreatureType ct = CreatureType.fromName(name);
            Creature creature = (Creature) p.getWorld().spawnCreature(loc, ct);
            removeNameFromArray(name, item, p);
            p.sendMessage(ChatColor.LIGHT_PURPLE + "You released your "
                    + ChatColor.YELLOW + name + ChatColor.LIGHT_PURPLE
                    + ". You have " + ChatColor.GREEN
                    + (MonsterTamer.playerMonsters.get(p.getName()).size() / 2)
                    + ChatColor.LIGHT_PURPLE + " monsters remaining.");
            if (Permission.friendly(p)) {
                addFriends(p, creature);
            }
        } catch (Exception e1) {
            MonsterTamer.log.info("[MonsterTamer]: Error spawning monster.");

        }
        MonsterTamer.writeUsers();
    }

    public static String checkMonsters(LivingEntity le) {
        String name = "";
        if (le instanceof Chicken) {
            name = "chicken";
        } else if (le instanceof Cow) {
            name = "cow";
        } else if (le instanceof Creeper) {
            name = "creeper";
        } else if (le instanceof Ghast) {
            name = "ghast";
        } else if (le instanceof Giant) {
            name = "giant";
        } else if (le instanceof Pig) {
            name = "pig";
        } else if (le instanceof PigZombie) {
            name = "pigzombie";
        } else if (le instanceof Sheep) {
            name = "sheep";
        } else if (le instanceof Skeleton) {
            name = "skeleton";
        } else if (le instanceof Slime) {
            name = "slime";
        } else if (le instanceof Spider) {
            name = "spider";
        } else if (le instanceof Squid) {
            name = "squid";
        } else if (le instanceof Zombie) {
            name = "zombie";
        }
        return name;
    }

    public static String checkMonsters(String name) {
        if (name.equals("chicken")) {
            name = "chicken";
        } else if (name.equals("cow")) {
            name = "cow";
        } else if (name.equals("creeper")) {
            name = "creeper";
        } else if (name.equals("ghast")) {
            name = "ghast";
        } else if (name.equals("giant")) {
            name = "giant";
        } else if (name.equals("pig")) {
            name = "pig";
        } else if (name.equals("pigzombie")) {
            name = "pigzombie";
        } else if (name.equals("sheep")) {
            name = "sheep";
        } else if (name.equals("skeleton")) {
            name = "skeleton";
        } else if (name.equals("slime")) {
            name = "slime";
        } else if (name.equals("spider")) {
            name = "spider";
        } else if (name.equals("squid")) {
            name = "squid";
        } else if (name.equals("zombie")) {
            name = "zombie";
        }
        return name;
    }

    public boolean checkMaps(Item id) {
        if (MonsterTamer.catchItems.containsKey(""
                + id.getItemStack().getTypeId())) {
            return true;
        }
        return false;
    }

    public static String getName(Item id, Player p) {
        ArrayList<String> array = MonsterTamer.playerMonsters.get(p.getName());
        if (array == null || array.isEmpty()) {
            return "";
        }
        int i2 = 0;
        String monsterName = "";
        String itemID = "";
        for (int i = array.size() - 1; i >= 0; --i) {
            if (i2 == 0) {
                itemID = array.get(i);
            } else if (i2 == 1) {
                monsterName = array.get(i);
                if (id.getItemStack().getTypeId() == Integer.parseInt(itemID)) {
                    break;
                }
            }
            if (i2 == 2) {
                i -= 1;
                i2 = 0;
                itemID = "";
                monsterName = "";
            } else {
                i2 += 1;
            }

        }
        return monsterName;
    }

    public static String getName(Player p, int ID) {
        ArrayList<String> array = MonsterTamer.playerMonsters.get(p.getName());
        if (array == null || array.isEmpty()) {
            return "";
        }

        int i2 = 0;
        String monsterName = "";
        String itemID = "";
        for (int i = array.size() - 1; i >= 0; --i) {
            if (i2 == 0) {
                itemID = array.get(i);
            } else if (i2 == 1) {
                monsterName = array.get(i);
                if (ID == Integer.parseInt(itemID)) {
                    break;
                }
            }
            if (i2 == 2) {
                i -= 1;
                i2 = 0;
                itemID = "";
                monsterName = "";
            } else {
                i2 += 1;
            }

        }
        return monsterName;
    }*/

    public static boolean isInArray(String name, String item, Player p) {
        ArrayList<String> array = MonsterTamer.playerMonsters.get(p.getName());
        int i2 = 0;
        String monster = "";
        String itemID = "";
        for (int i = 0; i < array.size(); ++i) {
            if (i2 == 0) {
                monster = array.get(i);
            } else if (i2 == 1) {
                itemID = array.get(i);
                if (monster.equalsIgnoreCase(name)
                        && itemID.equalsIgnoreCase(item)) {
                    return true;
                }
            }
            if (i2 == 2) {
                i -= 1;
                i2 = 0;
                itemID = "";
                monster = "";
            } else {
                i2 += 1;
            }
        }
        return false;
    }

    public static boolean removeNameFromArray(String name, String item, Player p) {
        ArrayList<String> array = MonsterTamer.playerMonsters.get(p.getName());
        int i2 = 0;
        int index = 0;
        String monster = "";
        String itemID = "";
        for (int i = 0; i < array.size(); ++i) {
            if (i2 == 0) {
                monster = array.get(i);
                index = i;
            } else if (i2 == 1) {
                itemID = array.get(i);
                if (monster.equalsIgnoreCase(name)
                        && itemID.equalsIgnoreCase(item)) {
                    array.remove(index + 1);
                    array.remove(index);

                    MonsterTamer.playerMonsters.put(p.getName(), array);
                    return true;
                }
            }
            if (i2 == 2) {
                i -= 1;
                i2 = 0;
                itemID = "";
                monster = "";
                index = 0;
            } else {
                i2 += 1;
            }

        }
        return false;
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
/*
    class RemindTask extends TimerTask {

        private PlayerDropItemEvent event;
        private Item itemdrop;

        public RemindTask(PlayerDropItemEvent e, Item id) {
            this.event = e;
            this.itemdrop = id;

        }

        @Override
        public void run() {
            PlayerListen.spawnFromItemDrop(event, itemdrop);
        }
    }*/
}
