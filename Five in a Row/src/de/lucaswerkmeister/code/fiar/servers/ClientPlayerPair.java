package de.lucaswerkmeister.code.fiar.servers;

import de.lucaswerkmeister.code.fiar.framework.Client;
import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * A Client-Player pair.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class ClientPlayerPair {
	private final Client client;
	private final Player player;

	/**
	 * Creates a new Client-Player pair with the specified client and player.
	 * 
	 * @param client
	 *            The client.
	 * @param player
	 *            The player.
	 */
	ClientPlayerPair(final Client client, final Player player) {
		this.client = client;
		this.player = player;
	}

	@Override
	public String toString() {
		return "[" + getClient().toString() + "," + getPlayer().toString() + "]";
	}

	/**
	 * Gets the client.
	 * 
	 * @return The client.
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * Gets the player.
	 * 
	 * @return The player.
	 */
	public Player getPlayer() {
		return player;
	}
}