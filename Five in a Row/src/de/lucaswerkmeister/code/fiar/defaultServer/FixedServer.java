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
package de.lucaswerkmeister.code.fiar.defaultServer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.lucaswerkmeister.code.fiar.framework.Block;
import de.lucaswerkmeister.code.fiar.framework.Board;
import de.lucaswerkmeister.code.fiar.framework.Client;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.Server;
import de.lucaswerkmeister.code.fiar.framework.UnknownClientException;
import de.lucaswerkmeister.code.fiar.framework.event.BlockField;
import de.lucaswerkmeister.code.fiar.framework.event.BoardSizeProposal;
import de.lucaswerkmeister.code.fiar.framework.event.FieldAction;
import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.JokerField;
import de.lucaswerkmeister.code.fiar.framework.event.PlaceStone;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerAction;
import de.lucaswerkmeister.code.fiar.framework.event.UnblockField;
import de.lucaswerkmeister.code.fiar.framework.event.UnjokerField;

/**
 * A server with a fixed {@link Client} and {@link Player} set, where each
 * player is bound to one client.
 * <p>
 * This server is not thread-safe.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class FixedServer extends Server {
	private final ClientPlayerPair[] pairs;
	private final Client[] allClients;
	private ArrayBoard board;
	private int[] phase;

	/**
	 * Creates a new {@link FixedServer} instance. The players in
	 * <code>players[i]</code> are bound to client <code>clients[i]</code>. The
	 * condition <code>players.length == clients.length</code> must be
	 * fulfilled; any clients where the according players array is empty are
	 * "watching" clients.
	 * 
	 * @param clients
	 *            The clients that this server recognizes.
	 * @param players
	 *            The players that this server recognizes.
	 */
	public FixedServer(Client[] clients, Player[][] players) {
		List<ClientPlayerPair> pairsL = new LinkedList<>();
		for (int i = 0; i < clients.length; i++)
			if (players[i].length > 0)
				for (int j = 0; j < players[i].length; j++)
					pairsL.add(new ClientPlayerPair(clients[i], players[i][j]));
		pairs = (ClientPlayerPair[]) pairsL.toArray();
		allClients = new Client[clients.length];
		System.arraycopy(clients, 0, allClients, 0, clients.length);
		phase = new int[] { 0, 0 };
	}

	@Override
	public int[] getPhase(Client requester) {
		if (knowsClient(requester))
			return Arrays.copyOf(phase, phase.length);
		throw new UnknownClientException(requester);
	}

	@Override
	public int getPhasesVersion(Client requester) {
		if (knowsClient(requester))
			return 0;
		throw new UnknownClientException(requester);
	}

	@Override
	public boolean canAct(Client requester, Player p) {
		return phase[0] == 0
				|| (phase[0] == 1 && phase[1] == 1 && phase[2] == p.getID());
	}

	@Override
	public Set<Class<? extends PlayerAction>> getAllowedActions(
			Client requester, Player p) {
		HashSet<Class<? extends PlayerAction>> ret = new HashSet<>();
		try {
			switch (phase[0]) {
			case 0:
				switch (phase[1]) {
				case 0:
					ret.add(BoardSizeProposal.class);
					return ret;
				case 1:
					ret.add(BlockField.class);
					ret.add(UnblockField.class);
					return ret;
				case 2:
					ret.add(JokerField.class);
					ret.add(UnjokerField.class);
					return ret;
				}
			case 1:
				switch (phase[1]) {
				case 0:
					return Collections.emptySet();
				case 1:
					if (p.getID() != phase[2])
						return Collections.emptySet();
					ret.add(PlaceStone.class);
					return ret;
				}
			case 2:
				return Collections.emptySet();
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// Let control fall through to the IllegalStateException below
		}
		throw new IllegalStateException(
				"Server is in unknown phase. This is a serious programming error!");
	}

	@Override
	public void action(Client requester, PlayerAction action)
			throws IllegalStateException {
		if (!getAllowedActions(requester, action.getActingPlayer()).contains(
				action.getClass()))
			throw new IllegalStateException(
					"This action is currently not permissible for this player!");
		try {
			switch (phase[0]) {
			case 0:
				switch (phase[1]) {
				case 0:
					ret.add(BoardSizeProposal.class);
					return ret;
				case 1:
					FieldAction a = (FieldAction) action;
					if (action.getClass() == BlockField.class)
						board.setPlayerAt(a.getField(), Block.getInstance());
					else
						board.setPlayerAt(a.getField(), null);
					break;
				case 2:
					ret.add(JokerField.class);
					ret.add(UnjokerField.class);
					return ret;
				}
			case 1:
				switch (phase[1]) {
				case 0:
					return Collections.emptySet();
				case 1:
					if (p.getID() != phase[2])
						return Collections.emptySet();
					ret.add(PlaceStone.class);
					return ret;
				}
			case 2:
				return Collections.emptySet();
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// Let control fall through to the IllegalStateException below
		}
		throw new IllegalStateException(
				"Server is in unknown phase. This is a serious programming error!");
	}

	@Override
	public Board getCurrentBoard(Client requester) {
		if (knowsClient(requester))
			return board == null ? null : board.clone();
		throw new UnknownClientException(requester);
	}

	private boolean knowsClient(Client c) {
		for (ClientPlayerPair pair : pairs)
			if (pair.client.equals(c))
				return true;
		return false;
	}

	private boolean knowsPlayer(Player p) {
		for (ClientPlayerPair pair : pairs)
			if (pair.player.equals(p))
				return true;
		return false;
	}

	private boolean clientPlayerMatch(Client c, Player p) {
		for (ClientPlayerPair pair : pairs)
			if (pair.player.equals(p))
				return pair.client.equals(c);
		return false;
	}

	private void sendEvent(GameEvent e) {
		for (Client c : allClients)
			c.gameEvent(e);
	}

	/**
	 * A Client-Player pair.
	 * 
	 * @author Lucas Werkmeister
	 * @version 1.0
	 */
	private class ClientPlayerPair {
		Client client;
		Player player;

		/**
		 * Creates a new Client-Player pair with the specified client and
		 * player.
		 * 
		 * @param client
		 *            The client.
		 * @param player
		 *            The player.
		 */
		ClientPlayerPair(Client client, Player player) {
			this.client = client;
			this.player = player;
		}
	}
}