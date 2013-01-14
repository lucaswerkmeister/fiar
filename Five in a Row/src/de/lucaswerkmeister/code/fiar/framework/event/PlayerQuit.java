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
 * Indicates that the game ended because one player quit.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class PlayerQuit extends GameEnd {
	private static final long serialVersionUID = -2536174002275043650L;
	private final Player quittingPlayer;

	/**
	 * A new {@link PlayerQuit} by the specified player.
	 * 
	 * @param quittingPlayer
	 *            The quitting player.
	 */
	public PlayerQuit(final Player quittingPlayer) {
		this.quittingPlayer = quittingPlayer;
	}

	/**
	 * Gets the quitting player.
	 * 
	 * @return The quitting player.
	 */
	public Player getQuittingPlayer() {
		return quittingPlayer;
	}
}
