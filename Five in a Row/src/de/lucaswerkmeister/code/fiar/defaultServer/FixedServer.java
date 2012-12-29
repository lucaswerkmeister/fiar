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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.lucaswerkmeister.code.fiar.framework.Board;
import de.lucaswerkmeister.code.fiar.framework.Client;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.Server;
import de.lucaswerkmeister.code.fiar.framework.UnknownClientException;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerAction;

/**
 * A server with a fixed {@link Client} and {@link Player} set, where each
 * player is bound to one client.
 * <p>
 * This server is not thread-safe.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class FixedServer implements Server {
	private final ClientPlayerPair[] pairs;
	private final Client[] watchingClients;
	private ArrayBoard board;

	/**
	 * Creates a new {@link FixedServer} instance. The players in
	 * <code>players[i]</code> are bound to client <code>clients[i]</code> for
	 * <code>int i < players.length</code>. The condition
	 * <code>players.length == clients.length</code> must be fulfilled; any
	 * clients where the according players array is empty are "watching"
	 * clients.
	 * 
	 * @param clients
	 *            The clients that this server recognizes.
	 * @param players
	 *            The players that this server recognizes.
	 */
	public FixedServer(Client[] clients, Player[][] players) {
		List<ClientPlayerPair> pairsL = new LinkedList<>();
		List<Client> watchingClientsL = new LinkedList<>();
		for (int i = 0; i < clients.length; i++)
			if (players[i].length == 0)
				watchingClientsL.add(clients[i]);
			else
				for (int j = 0; j < players[i].length; j++)
					pairsL.add(new ClientPlayerPair(clients[i], players[i][j]));
		pairs = (ClientPlayerPair[]) pairsL.toArray();
		watchingClients = (Client[]) watchingClientsL.toArray();
	}

	@Override
	public int[] getPhase(Client requester) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPhasesVersion(Client requester) {
		return 0;
	}

	@Override
	public boolean canAct(Client requester, Player p) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<Class<PlayerAction>> getAllowedActions(Client requester, Player p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void action(Client requester, PlayerAction action)
			throws IllegalStateException {
		// TODO Auto-generated method stub

	}

	@Override
	public Board getCurrentBoard(Client requester) {
		if (!knowsClient(requester))
			throw new UnknownClientException(requester);
		return board == null ? null : board.clone();
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
		public ClientPlayerPair(Client client, Player player) {
			this.client = client;
			this.player = player;
		}
	}
}