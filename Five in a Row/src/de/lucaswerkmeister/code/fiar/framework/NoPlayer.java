package de.lucaswerkmeister.code.fiar.framework;

import java.awt.Color;

/**
 * This player holds all fields that are empty; it exists to avoid null-references on the board.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 * 
 */
public final class NoPlayer extends Player {
	private static final long serialVersionUID = -3101883841114616879L;
	private static final NoPlayer instance = new NoPlayer();

	private NoPlayer() {
		super("No player", Color.black, 0);
	}

	private NoPlayer(final String name, final Color color, final int id) { // hide super constructor
		super(name, color, id);
	}

	/**
	 * Gets the one NoPlayer instance in this virtual machine.
	 * 
	 * @return The instance.
	 */
	public static NoPlayer getInstance() {
		return instance;
	}
}