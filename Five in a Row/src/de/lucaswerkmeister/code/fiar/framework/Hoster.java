package de.lucaswerkmeister.code.fiar.framework;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import de.lucaswerkmeister.code.fiar.framework.exception.UnknownClientException;
import de.lucaswerkmeister.code.fiar.framework.exception.UnknownPlayerException;

/**
 * A Hoster collects remote clients and players and at some time starts a server for these clients and players.
 * 
 * @author Lucas Werkmeisteer
 * @version 1.0
 */
public interface Hoster extends Remote, Serializable {

	/**
	 * Adds a client to this hoster.
	 * <p>
	 * If this client was already added previously, this method has no effect.
	 * 
	 * @param client
	 *            The client.
	 */
	public void addClient(RemoteClient client) throws RemoteException;

	/**
	 * Removes a client from this hoster.
	 * <p>
	 * The behavior of this method is unspecified if the client was not {@link #addClient(RemoteClient) added}
	 * previously or if there are still players from this client that have not been
	 * {@link #removePlayer(RemoteClient, Player) removed}.
	 * 
	 * @param client
	 *            The client.
	 */
	public void removeClient(RemoteClient client) throws RemoteException;

	/**
	 * Adds a player, controlled by the specified client, to the hoster.
	 * <p>
	 * The client must be known to the hoster, and an equal player must not yet be controlled by another client. (It is
	 * okay to add a player twice if the same client is given as the controller.)
	 * 
	 * @param controller
	 *            The controlling client.
	 * @param player
	 *            The player.
	 * @throws UnknownClientException
	 *             If the client has not yet been {@link #addClient(RemoteClient) added} to this hoster.
	 * @throws IllegalArgumentException
	 *             If the player is already controlled by another client.
	 */
	public void addPlayer(RemoteClient controller, Player player) throws UnknownClientException,
			IllegalArgumentException, RemoteException;

	/**
	 * Removes a player from the hoster.
	 * <p>
	 * The player must be known to the hoster.
	 * 
	 * @param player
	 *            The player.
	 * @throws UnknownPlayerException
	 *             If the player has not been previously {@link #addPlayer(RemoteClient, Player) added} to this hoster.
	 */
	public void removePlayer(Player player) throws UnknownPlayerException, RemoteException;
}