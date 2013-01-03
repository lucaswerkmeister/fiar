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
package de.lucaswerkmeister.code.fiar.clients.swingClient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Displays one field on the board.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class Field extends JComponent {
	private static final long serialVersionUID = 1004050893930330880L;
	private Player player;
	private Dimension size;

	/**
	 * A new {@link Field} occupied by the specified player, of the specified size.
	 * 
	 * @param player
	 *            The player occupying this field, or <code>null</code> if this field is still free.
	 * @param size
	 *            The size of the field.
	 */
	public Field(Player player, Dimension size) {
		this.player = player;
		this.size = size;
		if (player != null)
			setToolTipText(player.getName());
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
	}

	@Override
	public void paint(Graphics g) {
		System.out.println("draw " + System.currentTimeMillis());
		g.setColor(Color.black);
		g.fillRect(2, 2, 5, 5);
		g.drawRect(0, 0, size.width, size.height);
		if (player == null)
			g.setColor(getBackground());
		else
			g.setColor(player.getColor());
		g.fillRect(0, 0, size.width, size.height);
	}

	/**
	 * Sets the player currently occupying this field to the specified player.
	 * 
	 * @param player
	 *            The player now occupying this field.
	 */
	public void setPlayer(Player player) {
		this.player = player;
		setToolTipText(player.getName());
		repaint();
	}
}