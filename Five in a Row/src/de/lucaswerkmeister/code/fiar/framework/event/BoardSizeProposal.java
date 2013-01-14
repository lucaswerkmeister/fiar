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

import java.awt.Dimension;

import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Indicates that a player proposed a specific board size. Only allowed during "Choose board size" phase.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class BoardSizeProposal extends PlayerAction {
	private static final long serialVersionUID = 6868425142825023278L;
	private final Dimension size;

	/**
	 * A new {@link BoardSizeProposal} by the specified player with the specified size.
	 * 
	 * @param actingPlayer
	 *            The player who proposes the board size.
	 * @param size
	 *            The size that the player proposes.
	 */
	public BoardSizeProposal(final Player actingPlayer, final Dimension size) {
		super(actingPlayer);
		this.size = size;
	}

	/**
	 * Gets the size that the player proposed.
	 * 
	 * @return The size.
	 */
	public Dimension getSize() {
		return size;
	}
}