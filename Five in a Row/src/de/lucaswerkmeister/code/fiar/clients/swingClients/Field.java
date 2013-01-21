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
package de.lucaswerkmeister.code.fiar.clients.swingClients;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JComponent;

import de.lucaswerkmeister.code.fiar.framework.Block;
import de.lucaswerkmeister.code.fiar.framework.Joker;
import de.lucaswerkmeister.code.fiar.framework.NoPlayer;
import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Displays one field on the board.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class Field extends JComponent {
	private static final long serialVersionUID = -1716687517822009845L;
	private Player player;
	private final Dimension size;
	private final Point field;

	/**
	 * A new {@link Field} occupied by the specified player, of the specified size.
	 * 
	 * @param player
	 *            The player occupying this field, or <code>null</code> if this field is still free.
	 * @param size
	 *            The size of the field.
	 * @param field
	 *            The coordinates of this field.
	 */
	public Field(final Player player, final Dimension size, Point field) {
		setPlayer(player);
		this.size = size;
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		this.field = field;
	}

	@Override
	public void paint(final Graphics g) {
		if (player == null || player instanceof NoPlayer)
			g.setColor(getBackground());
		else
			g.setColor(player.getColor());
		g.fillRect(1, 1, size.width - 2, size.height - 2);
		g.setColor(Color.black);
		g.drawRect(0, 0, size.width - 1, size.height - 1);
	}

	/**
	 * Sets the player currently occupying this field to the specified player and, if it is an actual player, disables
	 * this component.
	 * <p>
	 * More specifically, the component is disabled if and only if all of the following conditions are met:
	 * <ul>
	 * <li>the player is not <code>null</code></li>
	 * <li>it is not the <code>NoPlayer</code></li>
	 * <li>it is not the <code>Block</code></li>
	 * <li>it is not the <code>Joker</code>.</li>
	 * </ul>
	 * In code: The component is disabled if and only if the condition
	 * <code>player != null && !(player instanceof NoPlayer)
	 * && !(player instanceof Block) && !(player instanceof Joker)</code> is met.
	 * 
	 * @param player
	 *            The player now occupying this field.
	 */
	public void setPlayer(final Player player) {
		this.player = player;
		if (player != null && !(player instanceof NoPlayer)) {
			setToolTipText(player.getName());
			if (!(player instanceof Block) && !(player instanceof Joker))
				setEnabled(false);
		} else
			setToolTipText(null);
		repaint();
	}

	/**
	 * Gets the coordinates of this {@link Field}.
	 * 
	 * @return The coordinates of this field.
	 */
	public Point getField() {
		return field;
	}
}