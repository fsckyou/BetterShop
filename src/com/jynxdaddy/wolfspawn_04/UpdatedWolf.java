/**
 * Based on work by MikePrimm
 * https://github.com/Bukkit/CraftBukkit/pull/255
 */
package com.jynxdaddy.wolfspawn_04;

import net.minecraft.server.EntityWolf;
import net.minecraft.server.PathEntity;

import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.entity.Wolf;

/**
 * Adapter for Wolf with updated API
 * @author Ashton
 * @edit jascotty2 - health set with setowner, and updated for 1.5
 */
public class UpdatedWolf {

    CraftWolf wolf;

    public UpdatedWolf(Wolf wolf) {
        this.wolf = (CraftWolf) wolf;
    }

    public Wolf getWolf() {
        return wolf;
    }

    public boolean isAngry() {
        return wolf.isAngry();
    }

    public void setAngry(boolean angry) {
        wolf.setAngry(angry);
    }

    public boolean isSitting() {
        return wolf.isSitting();
    }

    public void setSitting(boolean sitting) {
        wolf.setSitting(sitting);
    }

    public boolean isTame() {
        return getHandle().m_();
    }

    public void setTame(boolean tame) {
        if (tame && !wolf.getHandle().m_()) {// if was wild
            wolf.getHandle().health = (int) Math.round(20 * (wolf.getHandle().health / 8.));
        } else if (!tame && wolf.getHandle().m_()) {
            wolf.getHandle().health = (int) Math.round(8 * (wolf.getHandle().health / 20.));
        }
        wolf.getHandle().d(tame);
    }

    public String getOwner() {
        return getHandle().x();
    }

    public void setOwner(String player) {
        EntityWolf e = getHandle();
        if ((player != null) && (player.length() > 0)) {
            if (!e.m_()) {// if was wild
                e.health = (int) Math.round(20 * (e.health / 8.));
            }
            e.d(true); /* Make him tame */
            e.a((PathEntity) null); /* Clear path */
            e.a(player); /* Set owner */
        } else {
            if (e.m_()) {// if was tame
                e.health = (int) Math.round(8 * (e.health / 20.));
            }
            e.d(false); /* Make him not tame */
            e.a(""); /* Clear owner */
        }
    }

    public EntityWolf getHandle() {
        return wolf.getHandle();
    }

    @Override
    public String toString() {
        return "CraftWolf[anger=" + isAngry() + ",owner=" + getOwner() + ",tame=" + isTame() + ",sitting=" + isSitting() + "]";
    }
}
