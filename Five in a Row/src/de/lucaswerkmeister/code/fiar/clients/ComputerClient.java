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
package de.lucaswerkmeister.code.fiar.clients;

import de.lucaswerkmeister.code.fiar.framework.Board;
import de.lucaswerkmeister.code.fiar.framework.Client;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.Server;
import de.lucaswerkmeister.code.fiar.framework.event.BlockDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.BlockField;
import de.lucaswerkmeister.code.fiar.framework.event.BoardSizeProposal;
import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.JokerDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.JokerField;
import de.lucaswerkmeister.code.fiar.framework.event.PhaseChange;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerAction;
import de.lucaswerkmeister.code.fiar.framework.event.UnblockField;
import de.lucaswerkmeister.code.fiar.framework.event.UnjokerField;

/**
 * A client that controls one single computer player (AI).
 * <p>
 * This client will typically be created by another client. It is this other client's responsibility to:
 * <ul>
 * <li>Assign this client a unique ID (passed to this client in the constructor)</li>
 * <li>Create a player that this client can control (passed to this client in the constructor)</li>
 * <li>Register this client at the server (passed to this client in the constructor)</li>
 * </ul>
 * This client's player will agree with any board size, block and joker distribution that the other players propose.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class ComputerClient extends Client {
	private final Server server;
	private final int ID;
	private final Player player;

	/**
	 * Creates a new {@link ComputerClient} for the specified server and player with the specified ID.
	 * 
	 * @param server
	 *            The server.
	 * @param id
	 *            This client's ID. It is the caller's responsibility to ensure uniqueness.
	 * @param player
	 *            The player that is operated by this client's AI.
	 */
	public ComputerClient(Server server, int id, Player player) {
		this.server = server;
		this.ID = id;
		this.player = player;
	}

	@Override
	public void gameEvent(GameEvent e) {
		try {
			if (e instanceof PlayerAction && !((PlayerAction) e).getActingPlayer().equals(player)) {
				if (e instanceof BoardSizeProposal)
					server.action(this, new BoardSizeProposal(player, ((BoardSizeProposal) e).getSize()));
				else if (e instanceof BlockField || e instanceof UnblockField)
					server.action(this, new BlockDistributionAccepted(player, server.getCurrentBoard(this)));
				else if (e instanceof JokerField || e instanceof UnjokerField)
					server.action(this, new JokerDistributionAccepted(player, server.getCurrentBoard(this)));
			} else if (e instanceof PhaseChange) {
				int[] phase = ((PhaseChange) e).getNewPhase();
				if (phase[0] == 1 && phase[1] == 1 && phase[2] == player.getID()) {
					// It's our player's turn
					move();
				}
			}
		} catch (Throwable t) {
			System.err.println("Computer client for player " + player.getName() + " experienced an unexpected error.");
			t.printStackTrace();
			if (t instanceof ThreadDeath)
				throw (ThreadDeath) t;
		}
	}

	/**
	 * Computes the best move (placement of a stone) and executes it.
	 * <p>
	 * The current strategy of the AI is:
	 * <ol>
	 * <li>Do I have a row of length <code>{@link Server#IN_A_ROW} - 1</code> that can be completed on one or both
	 * sides? If so, do so. Otherwise:</li>
	 * <li>Does any opponent have a row of length <code>{@link Server#IN_A_ROW} - 1</code> that can be completed on one
	 * or both sides? If so, block it. Otherwise:</li>
	 * <li>Does any opponent have a row of length <code>{@link Server#IN_A_ROW} - 2</code> that can be completed? If so,
	 * block it. Otherwise:</li>
	 * <li>Do I have a row of length <code>{@link Server#IN_A_ROW} - 2</code> that can be completed? If so, extend it by
	 * one stone. Otherwise:</li>
	 * <li>Repeat for <code>n</code> from <code>3</code> to <code>{@link Server#IN_A_ROW} - 1</code> until a stone is
	 * placed:
	 * <ol>
	 * <li>Do I have a row of length <code>{@link Server#IN_A_ROW} - n</code> that can be completed? If so, extend it by
	 * one stone. Otherwise:</li>
	 * <li>Does any oppenent have a row of length <code>{@link Server#IN_A_ROW} - n</code> that can be completed? If so,
	 * block it.</li>
	 * </ol>
	 * </li>
	 * <li>If still no stone was placed: Choose a
	 * <code>({@link Server#IN_A_ROW} + 2) &times; ({@link Server#IN_A_ROW} + 2)</code> area with as few opponents'
	 * stones as possible, as few blocks as possible, and as much jokers as possible, and place a stone in its center.</li>
	 * </ol>
	 */
	private void move() {
		Board board = server.getCurrentBoard(this);
		// TODO this is gonna be ugly.
	}
}