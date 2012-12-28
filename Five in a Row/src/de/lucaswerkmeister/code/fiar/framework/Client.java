package de.lucaswerkmeister.code.fiar.framework;

import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerAction;

/**
 * A Client is responsible for any interaction with Players, be they actual
 * players or computer players.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public interface Client {
	/**
	 * Sends a {@link GameEvent} to the client.
	 * <p>
	 * Implementation of this method is purely optional; a client may choose to
	 * silently ignore any or all game events passed. For some events, however,
	 * this means that the client has to constantly poll the server (for
	 * example, if it doesn't listen to foreign {@link PlayerAction}s), which is
	 * undesirable and may even provoke the server to fire the client.
	 * 
	 * @param e
	 *            The game event.
	 */
	public void gameEvent(GameEvent e);

	/**
	 * Gets the client's unique ID. If two different {@link Client} instances
	 * return the same ID, the server's behavior is unspecified.
	 * 
	 * @return The client's ID.
	 */
	public int getID();

	/**
	 * Two clients are equal if and only if they are the same instance.
	 * 
	 * @param ojb
	 *            The other Client instance.
	 * @return <code>true</code> if this instance and the other instance are the
	 *         same instance, <code>false</code> otherwise.
	 */
	public boolean equals(Object ojb);

	/**
	 * A client's hash code is its {@link #getID() ID}.
	 * 
	 * @return The client's ID.
	 */
	public int hashCode();
}