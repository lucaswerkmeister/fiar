/*
 * Five in a Row, a short game.
 * Copyright (C) 2012/2013 Lucas Werkmeister
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
package de.lucaswerkmeister.code.fiar.framework;

import java.awt.Color;

/**
 * This class is only used to identify players and determine their representation for clients; it does not perform any
 * game logic.
 * 
 * @author Lucas Werkmeister
 * @version 1.2
 */
public class Player {
	private final String name;
	private final Color color;
	private final int id;

	/**
	 * Creates a new {@link Player} with the specified properties.
	 * 
	 * @param name
	 *            The player's name.
	 * @param color
	 *            The player's color.
	 * @param id
	 *            The player's ID. The caller is responsible for ensuring uniqueness of this ID (see {@link #getID()}).
	 */
	public Player(String name, Color color, int id) {
		this.name = name;
		this.color = color;
		this.id = id;
	}

	/**
	 * Gets the player's name.
	 * 
	 * @return The player's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the player's color.
	 * 
	 * @return The player's color.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Gets the player's unique ID.
	 * <p>
	 * If two different {@link Player} instances return the same ID, the server treats them as equal; the clients'
	 * behavior if these instances differ in name and/or color is unspecified.
	 * <p>
	 * A player's ID must always be positive, as negative values and <code>0</code> are used by the server.
	 * 
	 * @return The player's ID.
	 */
	public int getID() {
		return id;
	}

	/**
	 * Two players are equal if and only if their {@link #getID() IDs} are equal.
	 * 
	 * @param other
	 *            The other Player instance.
	 * @return <code>true</code> if this player's ID is equal to the other player's ID, <code>false</code> otherwise.
	 */
	public boolean equals(Object other) {
		if (other instanceof Player)
			return getID() == ((Player) other).getID();
		return false;
	}

	/**
	 * Returns <code>true</code> if this player or the other player is a Joker or if the two players are
	 * {@link #equals(Object) equal}.
	 * 
	 * @param other
	 *            The other Player instance.
	 * @return <code>true</code> if the players are to be deemed equals when checking for winning moves,
	 *         <code>false</code> otherwise.
	 */
	public boolean equalsWithJoker(Player other) {
		return this instanceof Joker || other instanceof Joker || equals(other);
	}

	/**
	 * A player's hash code is its {@link #getID() ID}.
	 * 
	 * @return The player's ID.
	 */
	public int hashCode() {
		return getID();
	}

	@Override
	public String toString() {
		return "(" + getName() + "," + getColor().toString() + "," + getID() + ")";
	}
}