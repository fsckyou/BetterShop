package com.fullwall.MonsterTamer;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Block;

public abstract class DirectionUtils {
	public enum CompassDirection {
		NO_DIRECTION(-1), NORTH(0), NORTH_EAST(1), EAST(2), SOUTH_EAST(3), SOUTH(
				4), SOUTH_WEST(5), WEST(6), NORTH_WEST(7);
		private int id;
		private static Map<Integer, CompassDirection> map;

		private CompassDirection(int id) {
			this.id = id;
			add(id, this);
		}

		private static void add(int type, CompassDirection name) {
			if (map == null) {
				map = new HashMap<Integer, CompassDirection>();
			}

			map.put(type, name);
		}

		public int getType() {
			return id;
		}

		public static CompassDirection fromId(final int type) {
			return map.get(type);
		}

		public String toString() {
			if (this.equals(CompassDirection.NORTH)) {
				return "North";
			}
			if (this.equals(CompassDirection.NORTH_EAST)) {
				return "North-East";
			}
			if (this.equals(CompassDirection.EAST)) {
				return "East";
			}
			if (this.equals(CompassDirection.SOUTH_EAST)) {
				return "South-East";
			}
			if (this.equals(CompassDirection.SOUTH)) {
				return "South";
			}
			if (this.equals(CompassDirection.SOUTH_WEST)) {
				return "South-West";
			}
			if (this.equals(CompassDirection.WEST)) {
				return "West";
			}
			if (this.equals(CompassDirection.NORTH_WEST)) {
				return "North-West";
			}
			return "No Direction";
		}
	}

	private static boolean isFacingNorth(double degrees, double leeway) {
		return ((0 <= degrees) && (degrees < 45 + leeway))
				|| ((315 - leeway <= degrees) && (degrees <= 360));
	}

	private static boolean isFacingEast(double degrees, double leeway) {
		return (45 - leeway <= degrees) && (degrees < 135 + leeway);
	}

	private static boolean isFacingSouth(double degrees, double leeway) {
		return (135 - leeway <= degrees) && (degrees < 225 + leeway);
	}

	private static boolean isFacingWest(double degrees, double leeway) {
		return (225 - leeway <= degrees) && (degrees < 315 + leeway);
	}

	public static CompassDirection getDirectionFromRotation(double degrees) {

		while (degrees < 0D) {
			degrees += 360D;
		}
		while (degrees > 360D) {
			degrees -= 360D;
		}
		if (isFacingNorth(degrees, 0)) {
			return CompassDirection.NORTH;
		}
		if (isFacingEast(degrees, 0)) {
			return CompassDirection.EAST;
		}
		if (isFacingSouth(degrees, 0)) {
			return CompassDirection.SOUTH;
		}
		if (isFacingWest(degrees, 0)) {
			return CompassDirection.WEST;
		}

		return CompassDirection.NO_DIRECTION;
	}

	public static Block getBlockBehind(World w, CompassDirection efacingDir,
			int x, int y, int z) {
		if (efacingDir == CompassDirection.NORTH)
			return w.getBlockAt(x + 2, y, z);
		if (efacingDir == CompassDirection.EAST)
			return w.getBlockAt(x, y, z + 2);
		if (efacingDir == CompassDirection.SOUTH)
			return w.getBlockAt(x - 2, y, z);
		if (efacingDir == CompassDirection.WEST)
			return w.getBlockAt(x, y, z - 2);
		return null;
	}
}
