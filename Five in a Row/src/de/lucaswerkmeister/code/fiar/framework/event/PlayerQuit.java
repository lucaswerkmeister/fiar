package de.lucaswerkmeister.code.fiar.framework.event;

import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Indicates that the game ended because one player quit.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class PlayerQuit extends GameEnd {

	private final Player quittingPlayer;

	/**
	 * A new {@link PlayerQuit} by the specified player.
	 * 
	 * @param quittingPlayer
	 *            The quitting player.
	 */
	public PlayerQuit(Player quittingPlayer) {
		this.quittingPlayer = quittingPlayer;
	}

	/**
	 * Gets the quitting player.
	 * 
	 * @return The quitting player.
	 */
	public Player getQuittingPlayer() {
		return quittingPlayer;
	}
}
