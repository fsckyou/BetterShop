// $Id$
/*
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
//package com.sk89q.minecraft.util.commands;

package me.jascotty2.lib.bukkit.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * A list of commands that can be used for the command
     */
    String[] commands();
    /**
     * A list of aliases for the command (for nested commands)
     */
    String[] aliases() default {};

    /**
     * Usage instruction. Example text for usage could be
     * <code>[-h] [name] [message]</code>.
     */
    String usage() default "";

    /**
     * A short description for the command.
     */
    String desc();

    /**
     * The minimum number of arguments. This should be 0 or above.
     */
    int min() default 0;

    /**
     * The maximum number of arguments. Use -1 for an unlimited number
     * of arguments.
     */
    int max() default -1;

    /**
	 * Permission node to use this command
     */
    String[] permissions() default "";
}
