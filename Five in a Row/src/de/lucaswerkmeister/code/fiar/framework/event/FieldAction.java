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
package de.lucaswerkmeister.code.fiar.framework.event;

import java.awt.Point;

import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Represents an action on one field of the board.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public abstract class FieldAction extends PlayerAction {
	private final int x, y;

	/**
	 * A new {@link FieldAction} by the specified player on the specified field.
	 * 
	 * @param actingPlayer
	 *            The acting player.
	 * @param x
	 *            The x coordinate of the modified field.
	 * @param y
	 *            The y coordinate of the modified field.
	 */
	public FieldAction(Player actingPlayer, int x, int y) {
		super(actingPlayer);
		this.x = x;
		this.y = y;
	}

	/**
	 * A new {@link FieldAction} by the specified player on the specified field.
	 * 
	 * @param actingPlayer
	 *            The acting player.
	 * @param point
	 *            The modified field.
	 */
	public FieldAction(Player actingPlayer, Point point) {
		this(actingPlayer, point.x, point.y);
	}

	/**
	 * Gets the field that was acted upon.
	 * 
	 * @return The field.
	 */
	public final Point getField() {
		return new Point(x, y);
	}
}