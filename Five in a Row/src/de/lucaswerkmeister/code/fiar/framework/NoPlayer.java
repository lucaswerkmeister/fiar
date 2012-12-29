package de.lucaswerkmeister.code.fiar.framework;

import java.awt.Color;

/**
 * This player holds all fields that are empty; it exists to avoid null-references on the board.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 * 
 */
public class NoPlayer extends Player {
	private static final NoPlayer instance = new NoPlayer();

	private NoPlayer() {
		super("No player", Color.black, 0);
	}

	private NoPlayer(String name, Color color, int id) { // hide super constructor
		super(name, color, id);
	}

	public static NoPlayer getInstance() {
		return instance;
	}
}