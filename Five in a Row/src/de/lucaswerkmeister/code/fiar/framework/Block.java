package de.lucaswerkmeister.code.fiar.framework;

import java.awt.Color;

/**
 * This player holds all fields that are blocked.
 * 
 * @author Lucas Werkmeister
 * @version 1.0 *
 */
public class Block extends Player {

	@Override
	public String getName() {
		return "Blocked";
	}

	@Override
	public Color getColor() {
		return Color.red;
	}

	@Override
	public int getID() {
		return 0;
	}
}