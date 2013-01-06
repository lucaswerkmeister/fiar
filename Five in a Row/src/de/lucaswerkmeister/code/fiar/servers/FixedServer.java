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
package de.lucaswerkmeister.code.fiar.servers;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.lucaswerkmeister.code.fiar.framework.Block;
import de.lucaswerkmeister.code.fiar.framework.Board;
import de.lucaswerkmeister.code.fiar.framework.Client;
import de.lucaswerkmeister.code.fiar.framework.Joker;
import de.lucaswerkmeister.code.fiar.framework.NoPlayer;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.Server;
import de.lucaswerkmeister.code.fiar.framework.event.BlockDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.BlockField;
import de.lucaswerkmeister.code.fiar.framework.event.BoardSizeProposal;
import de.lucaswerkmeister.code.fiar.framework.event.FieldAction;
import de.lucaswerkmeister.code.fiar.framework.event.Forfeit;
import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.JokerDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.JokerField;
import de.lucaswerkmeister.code.fiar.framework.event.PhaseChange;
import de.lucaswerkmeister.code.fiar.framework.event.PlaceStone;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerAction;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerVictory;
import de.lucaswerkmeister.code.fiar.framework.event.UnblockField;
import de.lucaswerkmeister.code.fiar.framework.event.UnjokerField;
import de.lucaswerkmeister.code.fiar.framework.exception.IllegalMoveException;
import de.lucaswerkmeister.code.fiar.framework.exception.UnknownClientException;
import de.lucaswerkmeister.code.fiar.framework.exception.UnknownPlayerException;

/**
 * A server with a fixed {@link Client} and {@link Player} set, where each player is bound to one client.
 * <p>
 * This server is not thread-safe.
 * 
 * @author Lucas Werkmeister
 * @version 1.1
 */
public class FixedServer extends Server {
	private final ClientPlayerPair[] pairs;
	private int currentPlayerIndex;
	private final Client[] allClients;
	private Board board;
	private int[] phase;
	private Set<BoardSizeProposal> boardSizeProposals;
	private Dimension currentBoardSize;
	private Set<BlockDistributionAccepted> acceptedBlockDistributions;
	private Set<JokerDistributionAccepted> acceptedJokerDistributions;

	/**
	 * Creates a new {@link FixedServer} instance. The players in <code>players[i]</code> are bound to client
	 * <code>clients[i]</code>. The condition <code>players.length == clients.length</code> must be fulfilled; any
	 * clients where the according players array is empty are "watching" clients.
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
		pairs = pairsL.toArray(new ClientPlayerPair[] {});
		allClients = new Client[clients.length];
		System.arraycopy(clients, 0, allClients, 0, clients.length);
		phase = new int[] {0, 0 };
		boardSizeProposals = new HashSet<>(pairs.length);
		currentBoardSize = new Dimension(0, 0);
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
		return phase[0] == 0 || (phase[0] == 1 && phase[1] == 1 && phase[2] == p.getID());
	}

	@Override
	public Set<Class<? extends PlayerAction>> getAllowedActions(Client requester, Player p) {
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
					ret.add(BlockDistributionAccepted.class);
					return ret;
				case 2:
					ret.add(JokerField.class);
					ret.add(UnjokerField.class);
					ret.add(JokerDistributionAccepted.class);
					return ret;
				}
			case 1:
				switch (phase[1]) {
				case 0:
					return Collections.emptySet();
				case 1:
					if (p.getID() == phase[2])
						ret.add(PlaceStone.class);
					ret.add(Forfeit.class);
					return ret;
				}
			case 2:
				return Collections.emptySet();
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// Let control fall through to the IllegalStateException below
		}
		throw new IllegalStateException("Server is in unknown phase. This is a serious programming error!");
	}

	@Override
	public void action(Client requester, PlayerAction action) throws IllegalStateException, IllegalMoveException {
		if (!knowsClient(requester))
			throw new UnknownClientException(requester);
		if (!knowsPlayer(action.getActingPlayer()))
			throw new UnknownPlayerException(action.getActingPlayer());
		if (!clientPlayerMatch(requester, action.getActingPlayer()))
			throw new IllegalArgumentException("Client and player don't match!");
		if (!getAllowedActions(requester, action.getActingPlayer()).contains(action.getClass()))
			throw new IllegalStateException("This action is currently not permissible for player "
					+ action.getActingPlayer().getName() + "!");
		try {
			switch (phase[0]) {
			case 0:
				switch (phase[1]) {
				case 0:
					boardSizeProposals.add((BoardSizeProposal) action);
					currentBoardSize = ((BoardSizeProposal) action).getSize();
					fireEvent(action);
					if (boardSizeAgreed()) {
						board = new ArrayBoard(currentBoardSize);
						acceptedBlockDistributions = new HashSet<>();
						phase = new int[] {0, 1 };
						fireEvent(new PhaseChange(phase));
					}
					return;
				case 1:
					if (action.getClass() == BlockField.class)
						board.setPlayerAt(((FieldAction) action).getField(), Block.getInstance());
					else if (action.getClass() == UnblockField.class)
						board.setPlayerAt(((FieldAction) action).getField(), NoPlayer.getInstance());
					else
						acceptedBlockDistributions.add((BlockDistributionAccepted) action);
					fireEvent(action);
					if (blockDistributionAgreed()) {
						acceptedBlockDistributions = null; // free memory
						acceptedJokerDistributions = new HashSet<>();
						phase = new int[] {0, 2 };
						fireEvent(new PhaseChange(phase));
					}
					return;
				case 2:
					if (action.getClass() == JokerField.class)
						board.setPlayerAt(((FieldAction) action).getField(), Joker.getInstance());
					else if (action.getClass() == UnjokerField.class)
						board.setPlayerAt(((FieldAction) action).getField(), NoPlayer.getInstance());
					else
						acceptedJokerDistributions.add((JokerDistributionAccepted) action);
					fireEvent(action);
					if (jokerDistributionAgreed()) {
						acceptedJokerDistributions = null; // free memory
						phase = new int[] {1, 1, pairs[0].player.getID() };
						currentPlayerIndex = 0;
						fireEvent(new PhaseChange(phase));
					}
					return;
				}
			case 1:
				switch (phase[1]) {
				case 0:
					break; // Let control fall through to the
							// IllegalStateException below
				case 1:
					if (action instanceof Forfeit) {
						int index = Arrays.asList(pairs).indexOf(action.getActingPlayer());
						System.arraycopy(pairs, index + 1, pairs, index, pairs.length - index - 1);
						if (currentPlayerIndex > index) {
							currentPlayerIndex--;
							phase[2] = pairs[currentPlayerIndex].player.getID();
						}
						fireEvent(action);
						return;
					}
					if (action.getActingPlayer().equals(pairs[currentPlayerIndex].player)) {
						PlaceStone placeStone = (PlaceStone) action;
						if (!board.getPlayerAt(placeStone.getField()).equals(NoPlayer.getInstance()))
							throw new IllegalMoveException("Field is already occupied by "
									+ board.getPlayerAt(placeStone.getField()).getName() + "!");
						board.setPlayerAt(placeStone.getField(), placeStone.getActingPlayer());
						fireEvent(action);

						if (board.wasWinningMove(placeStone.getField())) {
							phase = new int[] {2, 0, action.getActingPlayer().getID() };
							fireEvent(new PlayerVictory(action.getActingPlayer()));
							return;
						}

						currentPlayerIndex++;
						currentPlayerIndex %= pairs.length;
						phase[2] = pairs[currentPlayerIndex].player.getID();
						return;
					}
					break; // Let control fall through to the
							// IllegalStateException below
				}
			case 2:
				break; // Let control fall through to the IllegalStateException
						// below
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// Let control fall through to the IllegalStateException below
		}
		throw new IllegalStateException("Action currently not allowed!");
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

	private void fireEvent(GameEvent e) {
		for (Client c : allClients)
			c.gameEvent(e);
	}

	private boolean boardSizeAgreed() {
		Set<Player> unagreedPlayers = new HashSet<>();
		for (ClientPlayerPair pair : pairs)
			unagreedPlayers.add(pair.player);

		for (BoardSizeProposal p : boardSizeProposals)
			if (p.getSize().equals(currentBoardSize))
				unagreedPlayers.remove(p.getActingPlayer());

		return unagreedPlayers.isEmpty();
	}

	private boolean blockDistributionAgreed() {
		Set<Player> unagreedPlayers = new HashSet<>();
		for (ClientPlayerPair pair : pairs)
			unagreedPlayers.add(pair.player);

		for (BlockDistributionAccepted b : acceptedBlockDistributions)
			if (b.getAcceptedBoard().equals(board))
				unagreedPlayers.remove(b.getActingPlayer());

		return unagreedPlayers.isEmpty();
	}

	private boolean jokerDistributionAgreed() {
		Set<Player> unagreedPlayers = new HashSet<>();
		for (ClientPlayerPair pair : pairs)
			unagreedPlayers.add(pair.player);

		for (JokerDistributionAccepted j : acceptedJokerDistributions)
			if (j.getAcceptedBoard().equals(board))
				unagreedPlayers.remove(j.getActingPlayer());

		return unagreedPlayers.isEmpty();
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
		 * Creates a new Client-Player pair with the specified client and player.
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