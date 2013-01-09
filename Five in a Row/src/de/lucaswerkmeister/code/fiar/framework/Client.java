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

import java.util.Random;

import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerAction;

/**
 * A Client is responsible for any interaction with Players, be they actual players or computer players.
 * 
 * @author Lucas Werkmeister
 * @version 1.1
 */
public abstract class Client {
	private final int mHash = new Random().nextInt();

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
	 * Two clients are equal if and only if they are the same instance.
	 * 
	 * @param other
	 *            The other Client instance.
	 * @return <code>true</code> if this instance and the other instance are the same instance, <code>false</code>
	 *         otherwise.
	 */
	@Override
	public final boolean equals(final Object other) {
		return this == other;
	}

	/**
	 * A client's hash code is a combination of the {@link Class#hashCode()} of its runtime class and a random but
	 * constant number. (0bHHHHRRRR, where HHHH is the top two bytes of the class' hash code and RRRR is the bottom two
	 * bytes of the random constant.)
	 * 
	 * @return The client's hash code. It's always the same for {@link #equals(Object) equal} objects, and should not be
	 *         the same for any other objects.
	 */
	@Override
	public final int hashCode() {
		return (this.getClass().hashCode() & (((short) -1) << 16)) + mHash & ((short) -1);
	}
}