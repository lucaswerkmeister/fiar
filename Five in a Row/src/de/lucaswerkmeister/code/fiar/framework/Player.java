package de.lucaswerkmeister.code.fiar.framework;

import java.awt.Color;

/**
 * This class is only used to identify players and determine their
 * representation for clients; it does not perform any game logic.
 * 
 * @author Lucas Werkmeister
 * @version 1.1
 */
public abstract class Player {
	/**
	 * Gets the player's name.
	 * 
	 * @return The player's name.
	 */
	public abstract String getName();

	/**
	 * Gets the player's color.
	 * 
	 * @return The player's color.
	 */
	public abstract Color getColor();

	/**
	 * Gets the player's unique ID.
	 * <p>
	 * If two different {@link Player} instances return the same ID, the server
	 * treats them as equal; the clients' behavior if these instances differ in
	 * name and/or color is unspecified.
	 * <p>
	 * A player's ID must always be positive, as negative values and
	 * <code>0</code> are used by the server.
	 * 
	 * @return The player's ID.
	 */
	public abstract int getID();

	/**
	 * Two players are equal if and only if their {@link #getID() IDs} are
	 * equal.
	 * 
	 * @param other
	 *            The other Player instance.
	 * @return <code>true</code> if this player's ID is equal to the other
	 *         player's ID, <code>false</code> otherwise.
	 */
	public boolean equals(Object other) {
		if (other instanceof Player)
			return getID() == ((Player) other).getID();
		return false;
	}

	/**
	 * A player's hash code is its {@link #getID() ID}.
	 * 
	 * @return The player's ID.
	 */
	public int hashCode() {
		return getID();
	}
}