package de.lucaswerkmeister.code.fiar.framework.event;

import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Indicates a forfeit of a player.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class Forfeit extends PlayerAction {

	/**
	 * A new {@link Forfeit} by the specified player.
	 * 
	 * @param capitulatingPlayer
	 *            The capitulating player.
	 */
	public Forfeit(Player capitulatingPlayer) {
		super(capitulatingPlayer);
	}
}