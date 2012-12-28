package de.lucaswerkmeister.code.fiar.framework.event;

import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Represents some action that one player acts.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public abstract class PlayerAction extends GameEvent {

	private final Player actingPlayer;

	/**
	 * A new {@link PlayerAction} with the specified acting player.
	 * 
	 * @param actingPlayer
	 *            The acting player.
	 */
	public PlayerAction(Player actingPlayer) {
		this.actingPlayer = actingPlayer;
	}

	/**
	 * Gets the player that fired the action.
	 * 
	 * @return The acting player.
	 */
	public Player getActingPlayer() {
		return actingPlayer;
	}
}