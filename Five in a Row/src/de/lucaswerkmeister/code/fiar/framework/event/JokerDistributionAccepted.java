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

import de.lucaswerkmeister.code.fiar.framework.Board;
import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Indicates that the player accepts the distribution of "joker" fields as it is saved in this instance.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class JokerDistributionAccepted extends StoneDistributionAccepted {
	private static final long serialVersionUID = -8196308727321069709L;

	/**
	 * A new {@link JokerDistributionAccepted} by the specified player with the specified board.
	 * 
	 * @param acceptingPlayer
	 *            The player that accepts the distribution.
	 * @param acceptedBoard
	 *            The board that the player accepts.
	 */
	public JokerDistributionAccepted(final Player acceptingPlayer, final Board acceptedBoard) {
		super(acceptingPlayer, acceptedBoard);
	}
}