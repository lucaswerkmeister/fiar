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

import java.awt.Point;

/**
 * Provides access to the game board.
 * 
 * @author Lucas Werkmeister
 * @version 1.2
 */
public abstract class Board {
	/**
	 * Gets the player that occupies the field at (x,y).
	 * 
	 * @param x
	 *            The x coordinate of the field.
	 * @param y
	 *            The y coordinate of the field.
	 * @return The player at this position.
	 */
	public abstract Player getPlayerAt(int x, int y);

	/**
	 * Gets the player that occupies the field at the specified position.
	 * 
	 * @param position
	 *            The position of the field.
	 * @return The player at this position.
	 */
	public abstract Player getPlayerAt(Point position);

	/**
	 * Sets the player that occupies the field at (x,y) to the specified player.
	 * 
	 * @param x
	 *            The x coordinate of the field.
	 * @param y
	 *            The y coordinate of the field.
	 * @param p
	 *            The player that is to occupy this field.
	 */
	public abstract void setPlayerAt(int x, int y, Player p);

	/**
	 * Sets the player that occupies the field at the specified position to the specified player.
	 * 
	 * @param position
	 *            The position of the field.
	 * @param p
	 *            The player that is to occupy this field.
	 */
	public abstract void setPlayerAt(Point position, Player p);

	/**
	 * Gets the width of the board.
	 * 
	 * @return The width of the board.
	 */
	public abstract int getWidth();

	/**
	 * Gets the height of the board.
	 * 
	 * @return The height of the board.
	 */
	public abstract int getHeight();

	@Override
	public abstract Board clone();

	/**
	 * Two boards are equal if and only if they have the same size and return the same player at every field.
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Board))
			return false;
		Board otherBoard = (Board) other;
		if (getWidth() != otherBoard.getWidth() || getHeight() != otherBoard.getHeight())
			return false;
		for (int x = 0; x < getWidth(); x++)
			for (int y = 0; y < getHeight(); y++) {
				final Player p1 = getPlayerAt(x, y);
				final Player p2 = otherBoard.getPlayerAt(x, y);
				if ((p1 == null && p2 != null) || (p1 != null && p2 == null) || !p1.equals(p2))
					return false;
			}
		return true;
	}
}