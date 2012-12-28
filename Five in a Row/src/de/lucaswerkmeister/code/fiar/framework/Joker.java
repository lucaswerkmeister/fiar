package de.lucaswerkmeister.code.fiar.framework;

import java.awt.Color;

/**
 * This player holds all fields that are "Joker" fields.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class Joker extends Player {

	@Override
	public String getName() {
		return "Joker";
	}

	@Override
	public Color getColor() {
		return Color.green;
	}

	@Override
	public int getID() {
		return -1;
	}
}