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

import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Indicates a forfeit of a player.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class Forfeit extends PlayerAction {

	/**
	 * A new {@link Forfeit} by the specified player.
	 * 
	 * @param capitulatingPlayer
	 *            The capitulating player.
	 */
	public Forfeit(final Player capitulatingPlayer) {
		super(capitulatingPlayer);
	}
}