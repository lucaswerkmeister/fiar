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

import java.rmi.RemoteException;

import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerAction;

/**
 * A Client is responsible for any interaction with Players, be they actual players or computer players.
 * 
 * @author Lucas Werkmeister
 * @version 1.1
 */
public interface Client {
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
	 * @throws RemoteException
	 *             Only {@link RemoteClient}s: If a remote exception occurs.
	 */
	public void gameEvent(GameEvent e) throws RemoteException;
}