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
 *
 */
public class UpdatedWolf {

    CraftWolf wolf;
	
    public UpdatedWolf(Wolf wolf) {
        this.wolf = (CraftWolf)wolf;
    }
    
    public Wolf getWolf() {
    	return wolf;
    }

    public boolean isAngry() {
        return getHandle().x();
    }

    public void setAngry(boolean angry) {
        getHandle().c(angry);
    }

    public boolean isSitting() {
        return getHandle().w();
    }

    public void setSitting(boolean sitting) {
        getHandle().b(sitting);
    }

    public boolean isTame() {
        return getHandle().y();
    }
    
    public void setTame(boolean tame) {
        getHandle().d(tame);
    }
    
    public String getOwner() {
        return getHandle().v();
    }
    
    public void setOwner(String player) {
        EntityWolf e = getHandle();

        if ((player != null) && (player.length() > 0)) {
            e.d(true); /* Make him tame */
            e.a((PathEntity)null); /* Clear path */
            e.a(player); /* Set owner */
        }
        else {
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