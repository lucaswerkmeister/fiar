package de.lucaswerkmeister.code.fiar.framework.event;

import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Indicates that the game ended because one player won.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class PlayerVictory extends GameEnd {

	private final Player winningPlayer;

	/**
	 * A new {@link PlayerVictory} by the specified player.
	 * 
	 * @param winningPlayer
	 *            The winning player.
	 */
	public PlayerVictory(Player winningPlayer) {
		this.winningPlayer = winningPlayer;
	}

	/**
	 * Gets the winning player.
	 * 
	 * @return The winning player.
	 */
	public Player getWinningPlayer() {
		return winningPlayer;
	}
}
