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
 * @version 1.3
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
	 * 
	 * @param other
	 *            The other object.
	 * @return <code>true</code> if the boards are equal, <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Board))
			return false;
		final Board otherBoard = (Board) other;
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

	/**
	 * Checks if a specific stone completes a row in any direction.
	 * 
	 * @param x
	 *            The x coordinate of the stone.
	 * @param y
	 *            The y coordinate of the stone.
	 * @return <code>true</code> if the stone completes a row of length {@link Server#IN_A_ROW} in any direction,
	 *         <code>false</code> otherwise.
	 */
	public boolean wasWinningMove(final int x, final int y) {
		int equalStonesLeft = 0;
		int equalStonesRight = 0;
		for (; x - equalStonesLeft - 1 >= 0
				&& getPlayerAt(x - equalStonesLeft - 1, y).equalsWithJoker(getPlayerAt(x, y)); equalStonesLeft++)
			;
		for (; x + equalStonesRight + 1 < getWidth()
				&& getPlayerAt(x + equalStonesRight + 1, y).equalsWithJoker(getPlayerAt(x, y)); equalStonesRight++)
			;
		if (equalStonesLeft + 1 + equalStonesRight >= Server.IN_A_ROW)
			return true;

		int equalStonesUp = 0;
		int equalStonesDown = 0;
		for (; y - equalStonesUp - 1 >= 0 && getPlayerAt(x, y - equalStonesUp - 1).equalsWithJoker(getPlayerAt(x, y)); equalStonesUp++)
			;
		for (; y + equalStonesDown + 1 < getHeight()
				&& getPlayerAt(x, y + equalStonesDown + 1).equalsWithJoker(getPlayerAt(x, y)); equalStonesDown++)
			;
		if (equalStonesUp + 1 + equalStonesDown >= Server.IN_A_ROW)
			return true;

		int equalStonesUpLeft = 0;
		int equalStonesDownRight = 0;
		for (; x - equalStonesUpLeft - 1 >= 0 && y - equalStonesUpLeft - 1 >= 0
				&& getPlayerAt(x - equalStonesUpLeft - 1, y - equalStonesUpLeft - 1).equalsWithJoker(getPlayerAt(x, y)); equalStonesUpLeft++)
			;
		for (; x + equalStonesDownRight + 1 < getWidth()
				&& y + equalStonesDownRight + 1 < getHeight()
				&& getPlayerAt(x + equalStonesDownRight + 1, y + equalStonesDownRight + 1).equalsWithJoker(
						getPlayerAt(x, y)); equalStonesDownRight++)
			;
		if (equalStonesUpLeft + 1 + equalStonesDownRight >= Server.IN_A_ROW)
			return true;

		int equalStonesDownLeft = 0;
		int equalStonesUpRight = 0;
		for (; x - equalStonesDownLeft - 1 >= 0
				&& y + equalStonesDownLeft + 1 < getHeight()
				&& getPlayerAt(x - equalStonesDownLeft - 1, y + equalStonesDownLeft + 1).equalsWithJoker(
						getPlayerAt(x, y)); equalStonesDownLeft++)
			;
		for (; x + equalStonesUpRight + 1 < getWidth()
				&& y - equalStonesUpRight - 1 >= 0
				&& getPlayerAt(x + equalStonesUpRight + 1, y - equalStonesUpRight - 1).equalsWithJoker(
						getPlayerAt(x, y)); equalStonesUpRight++)
			;
		return equalStonesDownLeft + 1 + equalStonesUpRight >= Server.IN_A_ROW;
	}

	/**
	 * Checks if a specific stone completes a row in any direction.
	 * 
	 * @param location
	 *            The coordinates of the stone.
	 * @return <code>true</code> if the stone completes a row of length {@link Server#IN_A_ROW} in any direction,
	 *         <code>false</code> otherwise.
	 */
	public boolean wasWinningMove(final Point location) {
		return wasWinningMove(location.x, location.y);
	}
}