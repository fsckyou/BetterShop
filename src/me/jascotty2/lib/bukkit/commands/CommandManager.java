/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: Generic Commands Manager.. <br/>
 * much of the code was borrowed from sk89q's WorldEdit
 * https://github.com/sk89q/worldedit
 * (why reinvent the wheel? :)
 * WorldEdit Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package me.jascotty2.lib.bukkit.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.jascotty2.lib.util.Str;
import org.bukkit.command.CommandSender;

public abstract class CommandManager {

	protected final Map<Method, Map<String, Method>> commands =
			new HashMap<Method, Map<String, Method>>();
	protected Map<String, String> descs = new HashMap<String, String>();

	protected abstract Logger getLogger();

	public void registerCommandClass(Class<?> c) {
		registerCommandClass(c, null);
	}

	public void registerCommandClass(Class<?> c, Method parent) {
		if (c == null) {
			return;
		}
		Map<String, Method> map;
		if (commands.containsKey(parent)) {
			map = commands.get(parent);
		} else {
			map = new HashMap<String, Method>();
			commands.put(parent, map);
		}

		for (Method method : c.getMethods()) {
			if (!method.isAnnotationPresent(Command.class)
					|| !Modifier.isStatic(method.getModifiers())) {
				continue;
			}

			Command cmd = method.getAnnotation(Command.class);

			for (String alias : cmd.commands()) {
				if (map.containsKey(alias)) {
//					System.out.println("notice: duplicate key '" + alias
//							+ "' in " + (parent == null ? "root" : parent.getName()));
				} else {
					map.put(alias, method);
//					System.out.println("registering " + (parent == null ? "root" : parent.getName())
//							+ ":" + alias + " to " + method);
//					if(alias.equalsIgnoreCase("region")){
//						System.out.println("mapping: " + Str.concatStr(map.keySet(), ", "));
				}
			}
			if (parent != null) {
				// Cache the aliases
				for (String alias : cmd.aliases()) {
					if (!map.containsKey(alias)) {
						map.put(alias, method);
					}
				}
			}

			// Build a list of commands and their usage details, 
			//		at least for root level commands
			if (parent == null && cmd.commands().length > 0) {
				if (cmd.usage().length() == 0) {
					descs.put(cmd.commands()[0], cmd.desc());
				} else {
					descs.put(cmd.commands()[0], cmd.usage() + " - " + cmd.desc());
				}
			}

			// Look for nested commands -- if there are any, those have
			// to be cached too so that they can be quickly looked
			// up when processing commands
			if (method.isAnnotationPresent(NestedCommand.class)) {
				for (Class<?> nestedCls : method.getAnnotation(NestedCommand.class).value()) {
//					System.out.println("regisering " + nestedCls + " under " + method.getName());
					registerCommandClass(nestedCls, method);
				}
			}
		}
	}

	/**
	 * Checks to see whether there is a command named such at the root level.
	 * This will check aliases as well.
	 *
	 * @param command
	 * @return
	 */
	public boolean hasCommand(String command) {
		return commands.get(null).containsKey(command.toLowerCase());
	}

	/**
	 * Get the usage string for a command.
	 *
	 * @param args
	 * @param level
	 * @param cmd
	 * @return
	 */
	protected String getUsage(String[] args, int level, Command cmd) {
		StringBuilder command = new StringBuilder("/");

		for (int i = 0; i <= level; ++i) {
			command.append(args[i]).append(" ");
		}
		command.append(cmd.usage());

		return command.toString();
	}

	/**
	 * Get the usage string for a nested command.
	 *
	 * @param player
	 * @param args
	 * @param level
	 * @param method
	 * @return
	 * @throws CommandException
	 */
	protected String getNestedUsage(CommandSender player, String[] args,
			int level, Method method) throws CommandException {
		StringBuilder command = new StringBuilder();

		command.append("/");
		for (int i = 0; i <= level; ++i) {
			command.append(args[i]).append(" ");
		}
		command.append(" ");

		Map<String, Method> map = commands.get(method);
		if (map == null) {
			return command.toString(); // shouldn't get here, really..
		}
		boolean found = false;

		command.append("<");

		Set<String> allowedCommands = new HashSet<String>();

		for (Entry<String, Method> entry : map.entrySet()) {
			Method childMethod = entry.getValue();
			found = true;

			if (checkHasPermission(player, childMethod)) {
				Command childCmd = childMethod.getAnnotation(Command.class);

				allowedCommands.add(childCmd.aliases()[0]);
			}
		}

		if (allowedCommands.size() > 0) {
			command.append(Str.concatStr(allowedCommands, "|"));
		} else {
			if (!found) {
				command.append("?");
			} else {
				throw new CommandPermissionsException();
			}
		}

		command.append(">");

		return command.toString();
	}

	/**
	 * Returns whether a player has access to a command.
	 *
	 * @param method
	 * @param player
	 * @return
	 */
	protected boolean checkHasPermission(CommandSender player, Method method) {
		//CommandPermissions perms = method.getAnnotation(CommandPermissions.class);
		Command perms = method.getAnnotation(Command.class);
		if (perms == null
				|| perms.permissions() == null
				|| perms.permissions().length == 0) {
			return true;
		}

		for (String perm : perms.permissions()) {
			if (hasPermission(player, perm)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns whether a player permission..
	 *
	 * @param player
	 * @param perm
	 * @return
	 */
	public abstract boolean hasPermission(CommandSender player, String perm);

	/**
	 * Attempt to execute a command.
	 *
	 * @param player
	 * @param args
	 * @throws CommandException
	 */
	public void execute(CommandSender player, String... args) throws CommandException {
		if (args.length == 0) {
			throw new UnhandledCommandException();
		} /*else if(args.length >= 1) {
		//			String[] newArgs = new String[args.length - 1];
		//			System.arraycopy(args, 1, newArgs, 0, newArgs.length);
		//
		//			executeMethod(player, args[0], newArgs, null, 0);
		}*/ else {
//			executeMethod(player, args[0], new String[0], null, 0);
			executeMethod(player, args[0], args, null, 0);
		}
	}

	/**
	 * Attempt to execute a command. This version takes a separate command
	 * name (for the root command) and then a list of following arguments.
	 *
	 * @param player command source
	 * @param cmd command to run
	 * @param args arguments
	 * @throws CommandException
	 */
	public void execute(CommandSender player, String cmd, String[] args) throws CommandException {
		String[] allArgs = new String[args.length + 1];
		allArgs[0] = cmd;
		System.arraycopy(args, 0, allArgs, 1, args.length);

		executeMethod(player, cmd, allArgs, null, 0);
		//executeMethod(player, cmd, args, null, 0);
	}

	/**
	 * Attempt to execute a command.
	 *
	 * @param player
	 * @param cmd Command Executed
	 * @param args Command arguments
	 * @param parent
	 * @throws CommandException
	 */
	public void executeMethod(CommandSender player, String cmd, String[] args, Method parent) throws CommandException {
		String[] allArgs = new String[args.length + 1];
		allArgs[0] = cmd;
		System.arraycopy(args, 0, allArgs, 1, args.length);

		executeMethod(player, cmd, allArgs, null, 0);
		//executeMethod(player, cmd, args, parent, 0);
	}

	/**
	 * Attempt to execute a command.
	 *
	 * @param player
	 * @param cmd Command Executed
	 * @param args Command arguments
	 * @param parent
	 * @param level
	 * @throws CommandException
	 */
	public void executeMethod(CommandSender player, String cmd, String[] args, Method parent, int level) throws CommandException {

		Map<String, Method> map = commands.get(parent);
		Method method = map.get(cmd.toLowerCase());

		if (method == null) {
			if (parent == null) { // Root
//				//System.out.println(Str.concatStr(commands.keySet(), "\n"));
//				for (Method m : commands.keySet()) {
//					System.out.println((m == null ? "null" : m.toString()) + ":");
//					for (String c : commands.get(m).keySet()) {
//						System.out.println(c);
//					}
//				}
//				System.out.println(Str.concatStr(map.values(), "\n"));
//				System.out.println(Str.concatStr(map.keySet(), ", "));
//				System.out.println();
//				System.out.println(Str.concatStr(commands.keySet(), "\n"));

				throw new UnhandledCommandException();
			} else {
				throw new MissingNestedCommandException("Unknown command: " + cmd,
						getNestedUsage(player, args, 0, parent));
			}
		}

		if (!checkHasPermission(player, method)) {
			throw new CommandPermissionsException();
		}

		if (method.isAnnotationPresent(NestedCommand.class)) {
			if (args.length - level == 1) {
				throw new MissingNestedCommandException("Sub-command required.",
						getNestedUsage(player, args, level, method));
			} else {
//				System.out.println("following nested command to " + method.getName() + " (" + (level+1) + ")");
//				String[] newArgs = new String[args.length - 1];
//				System.arraycopy(args, 1, newArgs, 0, newArgs.length);
//				executeMethod(player, args[0], newArgs, method, level + 1);
				executeMethod(player, args[level + 1], args, method, level + 1);
				return;
			}
		}

		Command cmdInfo = method.getAnnotation(Command.class);

		if (args.length - (level + 1) < cmdInfo.min()) {
			throw new CommandUsageException("Too few arguments.",
					getUsage(args, level, cmdInfo));
		}

		if (cmdInfo.max() != -1 && args.length - (level + 1) > cmdInfo.max()) {
			throw new CommandUsageException("Too many arguments.",
					getUsage(args, level, cmdInfo));
		}

		try {
			//method.invoke(instance, (Object[])args);
			//method.invoke(null, (Object[]) args);
			String[] newArgs = new String[args.length - 1 - level];
			System.arraycopy(args, level + 1, newArgs, 0, newArgs.length);
			method.invoke(null, new Object[]{player, newArgs});
		} catch (IllegalArgumentException e) {
			getLogger().log(Level.SEVERE, "Failed to execute command", e);
		} catch (IllegalAccessException e) {
			getLogger().log(Level.SEVERE, "Failed to execute command", e);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof CommandException) {
				throw (CommandException) e.getCause();
			}

			throw new WrappedCommandException(e.getCause());
		}
	}
} // end class CommandManager

