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

import java.awt.Color;

/**
 * This player holds all fields that are blocked.
 * 
 * @author Lucas Werkmeister
 * @version 1.1
 */
public final class Block extends Player {
	private static final long serialVersionUID = 9169815489612959619L;
	/**
	 * The ID of the Block instance.
	 * 
	 * @see #getInstance()
	 */
	public static final int ID = -1;
	private static final Block instance = new Block();

	private Block() {
		super("Blocked", Color.red, ID);
	}

	private Block(final String name, final Color color, final int id) { // hide super constructor
		super(name, color, id);
	}

	/**
	 * Gets the one Block instance in this virtual machine.
	 * 
	 * @return The Block instance.
	 */
	public static Block getInstance() {
		return instance;
	}
}