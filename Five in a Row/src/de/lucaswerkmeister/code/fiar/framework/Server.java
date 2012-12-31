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

import java.util.Set;

import de.lucaswerkmeister.code.fiar.framework.event.PlayerAction;
import de.lucaswerkmeister.code.fiar.framework.exception.ClientFiredException;
import de.lucaswerkmeister.code.fiar.framework.exception.IllegalMoveException;
import de.lucaswerkmeister.code.fiar.framework.exception.UnknownClientException;

/**
 * The game server.
 * <p>
 * The server handles all game logic. It decides whether a move is legal, keeps track of the game, and notifies the
 * {@link Client}s about any events.
 * <p>
 * All methods of the server are meant to be called by a client; the first argument of each method call should always be
 * the calling instance.
 * <p>
 * The server's behavior when encountering an unknown client as requester is unspecified, but the
 * {@link UnknownClientException} class is provided for uniform behavior if servers wish to throw such an exception.
 * <p>
 * If, for any reason (e. g., the client "spams" the server with requests), the server decides to "fire" a client, the
 * preferred way for it to do so is to throw a {@link ClientFiredException}.
 * 
 * @author Lucas Werkmeister
 * @version 1.1
 */
public abstract class Server {
	public static final int IN_A_ROW = 5;

	/**
	 * Gets the current phase of the server.
	 * <p>
	 * Phases represent states of the game like "initialization" or "placing stones" (for a complete list, see below).
	 * Each phase can have sub-phases; the value <code>{1,1,3,8}</code> therefore represents "Phase 1.1.3.8" or
	 * "Sub-Phase 8 of Sub-Phase 3 of Sub-Phase 1 of Phase 1"
	 * <p>
	 * Currently defined phases:
	 * <ul>
	 * <li>Version 0:
	 * <ol start="0">
	 * <li>Initialization</li>
	 * <ol start="0">
	 * <li>Choose board size. This phase ends as soon as all players have proposed the same board size.</li>
	 * <li>Block fields. This phase ends as soon as all players have indicated that they are happy with the current
	 * distribution of blocked fields.</li>
	 * <li>Set Joker fields. This phase ends as soon as all players have indicated that they are happy with the current
	 * distribution of Joker fields.</li>
	 * </ol>
	 * <li>Game running</li>
	 * <ol start="0">
	 * <li>Server is calculating. Note: As server calculations typically run very short, a server implementation may
	 * choose to never return this status and instead wait until the server is done calculating.</li>
	 * <li>One specific player may place a stone. The sub-phase of this phase gives the player's ID.</li>
	 * </ol>
	 * <li>Game ended
	 * <ol start="0">
	 * <li>One player won. The sub-phase of this phase gives the winner's ID.</li>
	 * <li>Tie: The board is full and none of the players won.</li>
	 * <li>Quit: The game ended because one player quit the game. Note: One player quitting does not necessarily end the
	 * game - the server can, for example, choose to continue the game, and if only one player is left, declare this
	 * player the winner.</li>
	 * <li>Internal error: The game ended due to an internal server error.</li>
	 * </ol>
	 * </li>
	 * </ul>
	 * 
	 * @param requester
	 *            The requesting client.
	 * @return The current phase.
	 */
	public abstract int[] getPhase(Client requester);

	/**
	 * Gets the version of the Phases list that the server is using. This method is provided for compatibility between
	 * different server and client versions.
	 * <p>
	 * The currently latest version of the Phases list is <code>0</code>.
	 * 
	 * @param requester
	 *            The requesting client.
	 * @return The Phases list version.
	 */
	public abstract int getPhasesVersion(Client requester);

	/**
	 * Determines if the specified player can currently perform any actions.
	 * <p>
	 * For more detailed informations about which actions a player can currently perform, use
	 * {@link #getAllowedActions(Client, Player)}.
	 * 
	 * @param requester
	 *            The requesting client.
	 * @param p
	 *            The player.
	 * @return <code>true</code> if the player can currently perform any actions, <code>false</code> otherwise.
	 */
	public boolean canAct(Client requester, Player p) {
		return getAllowedActions(requester, p).size() != 0;
	}

	/**
	 * Determines which actions the specified player can currently perform.
	 * 
	 * @param requester
	 *            The requesting client.
	 * @param p
	 *            The player.
	 * @return A {@link Set} containing all classes of actions that the player, at the time of the method invocation, is
	 *         allowed to perform.
	 */
	public abstract Set<Class<? extends PlayerAction>> getAllowedActions(Client requester, Player p);

	/**
	 * Performs an action to the game.
	 * 
	 * @param requester
	 *            The requesting client.
	 * @param action
	 *            The action to perform.
	 * @throws IllegalStateException
	 *             If the action is currently not allowed.
	 * @throws IllegalMoveException
	 *             If an illegal move was made.
	 */
	public abstract void action(Client requester, PlayerAction action) throws IllegalStateException,
			IllegalMoveException;

	/**
	 * Gets the current board.
	 * <p>
	 * Note that the server's internal board can not be manipulated by manipulating the {@link Board} instance retrieved
	 * by this method: Either the server returns a copy of its internal Board instance, or its internal board
	 * representation doesn't use the class Board altogether.
	 * <p>
	 * If the board has not yet been decided on, this method returns <code>null</code>.
	 * 
	 * @param requester
	 *            The requesting client.
	 * @return The current board, or <code>null</code>.
	 */
	public abstract Board getCurrentBoard(Client requester);
}