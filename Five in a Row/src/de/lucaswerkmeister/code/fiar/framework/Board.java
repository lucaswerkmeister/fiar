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

/**
 * Provides access to the game board.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public interface Board {
	/**
	 * Gets the player that occupies the field at (x,y), or <code>null</code> if
	 * that field is still free.
	 * 
	 * @param x
	 *            The x coordinate of the field.
	 * @param y
	 *            The y coordinate of the field.
	 * @return The player at this position, or <code>null</code>.
	 */
	public Player getPlayerAt(int x, int y);

	/**
	 * Gets the width of the board.
	 * 
	 * @return The width of the board.
	 */
	public int getWidth();

	/**
	 * Gets the height of the board.
	 * 
	 * @return The height of the board.
	 */
	public int getHeight();
}