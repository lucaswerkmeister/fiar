/*
 * Five in a Row, a short game.
 * Copyright (C) 2012/2013 Lucas Werkmeister
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.lucaswerkmeister.code.fiar.framework;

import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerAction;

/**
 * A Client is responsible for any interaction with Players, be they actual players or computer players.
 * 
 * @author Lucas Werkmeister
 * @version 1.1
 */
public abstract class Client {
	/**
	 * Sends a {@link GameEvent} to the client.
	 * <p>
	 * Implementation of this method is purely optional; a client may choose to silently ignore any or all game events
	 * passed. For some events, however, this means that the client has to constantly poll the server (for example, if
	 * it doesn't listen to foreign {@link PlayerAction}s), which is undesirable and may even provoke the server to fire
	 * the client.
	 * 
	 * @param e
	 *            The game event.
	 */
	public abstract void gameEvent(GameEvent e);

	/**
	 * Gets the client's unique ID. If two different {@link Client} instances return the same ID, the server's behavior
	 * is unspecified.
	 * 
	 * @return The client's ID.
	 */
	public abstract int getID();

	/**
	 * Two clients are equal if and only if they are the same instance.
	 * 
	 * @param other
	 *            The other Client instance.
	 * @return <code>true</code> if this instance and the other instance are the same instance, <code>false</code>
	 *         otherwise.
	 */
	public final boolean equals(Object other) {
		return this == other;
	}

	/**
	 * A client's hash code is its {@link #getID() ID}.
	 * 
	 * @return The client's ID.
	 */
	public final int hashCode() {
		return getID();
	}
}