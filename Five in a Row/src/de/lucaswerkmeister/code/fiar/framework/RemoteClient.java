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

import java.io.Serializable;
import java.rmi.Remote;

import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerQuit;

/**
 * A Remote Client is used in a game running over several Java Virtual Machine instances, possibly on different physical
 * machines. It provides additional methods to be notified of players joining and leaving the game, and the game
 * starting.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public interface RemoteClient extends Client, Remote, Serializable {

	/**
	 * Informs the client that a player joined the game.
	 * 
	 * @param player
	 *            The joining player.
	 */
	public void playerJoined(Player player);

	/**
	 * Informs the client that a player left the game.
	 * <p>
	 * This method may only be called as long as the game hasn't started yet (as indicated by
	 * {@link #gameStarts(Server)}); after that, the leaving of a player should instead be indicated by a call of
	 * {@link #gameEvent(GameEvent)} with an appropriate {@link PlayerQuit} by the server.
	 * 
	 * @param player
	 *            The leaving player.
	 */
	public void playerLeft(Player player);

	/**
	 * Informs the client that the game started, and gives it the server that was created to run the game.
	 * 
	 * @param server
	 *            The server.
	 */
	public void gameStarts(Server server);
}