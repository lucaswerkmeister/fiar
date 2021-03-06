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
 * Indicates that a player unmarked a field as Joker field. Only allowed during "set joker fields" phase.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class UnjokerField extends FieldAction {
	private static final long serialVersionUID = -2669408717603994560L;

	/**
	 * A new {@link UnjokerField} by the specified player on the specified field.
	 * 
	 * @param actingPlayer
	 *            The acting player.
	 * @param x
	 *            The x coordinate of the modified field.
	 * @param y
	 *            The y coordinate of the modified field.
	 */
	public UnjokerField(final Player actingPlayer, final int x, final int y) {
		super(actingPlayer, x, y);
	}

	/**
	 * A new {@link UnjokerField} by the specified player on the specified field.
	 * 
	 * @param actingPlayer
	 *            The acting player.
	 * @param point
	 *            The modified field.
	 */
	public UnjokerField(final Player actingPlayer, final Point point) {
		super(actingPlayer, point);
	}
}