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
 * This player holds all fields that are "Joker" fields.
 * 
 * @author Lucas Werkmeister
 * @version 1.1
 */
public class Joker extends Player {
	/**
	 * The ID of the Joker instance.
	 * 
	 * @see #getInstance()
	 */
	public static final int ID = -2;
	private static final Joker instance = new Joker();

	private Joker() {
		super("Joker", Color.green, ID);
	}

	private Joker(String name, Color color, int id) { // hide super constructor
		super(name, color, id);
	}

	/**
	 * Gets the one Joker instance in this virtual machine.
	 * 
	 * @return The instance.
	 */
	public static Joker getInstance() {
		return instance;
	}
}