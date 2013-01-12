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
import de.lucaswerkmeister.code.fiar.framework.event.AllOthersForfeit;
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
import de.lucaswerkmeister.code.fiar.framework.event.Tie;
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
	private ClientPlayerPair[] pairs;
	private int currentPlayerIndex;
	private final Client[] allClients;
	private Board board;
	private int[] phase;
	private final Set<BoardSizeProposal> boardSizeProposals;
	private Dimension currentBoardSize;
	private Set<BlockDistributionAccepted> acceptedBlockDistributions;
	private Set<JokerDistributionAccepted> acceptedJokerDistributions;
	private int occupiedFields = 0; // number of occupied fields is cached to avoid having to iterate over the whole
									// board after each move for counting

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
	public FixedServer(final Client[] clients, final Player[][] players) {
		final List<ClientPlayerPair> pairsL = new LinkedList<>();
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
	public int[] getPhase(final Client requester) {
		if (knowsClient(requester))
			return Arrays.copyOf(phase, phase.length);
		throw new UnknownClientException(requester);
	}

	@Override
	public int getPhasesVersion(final Client requester) {
		if (knowsClient(requester))
			return 0;
		throw new UnknownClientException(requester);
	}

	@Override
	public boolean canAct(final Client requester, final Player p) {
		return phase[0] == 0 || (phase[0] == 1 && phase[1] == 1 && phase[2] == p.getID());
	}

	@Override
	public Set<Class<? extends PlayerAction>> getAllowedActions(final Client requester, final Player p) {
		final HashSet<Class<? extends PlayerAction>> ret = new HashSet<>();
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
		} catch (final ArrayIndexOutOfBoundsException e) {
			// Let control fall through to the IllegalStateException below
		}
		throw new IllegalStateException("Server is in unknown phase. This is a serious programming error!");
	}

	@Override
	public void action(final Client requester, final PlayerAction action) throws IllegalStateException,
			IllegalMoveException {
		if (!knowsClient(requester))
			throw new UnknownClientException(requester);
		if (!knowsPlayer(action.getActingPlayer()))
			throw new UnknownPlayerException(action.getActingPlayer());
		if (!clientPlayerMatch(requester, action.getActingPlayer()))
			throw new IllegalArgumentException("Client and player don't match!");
		if (!getAllowedActions(requester, action.getActingPlayer()).contains(action.getClass()))
			throw new IllegalStateException("This action is currently not allowed for "
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
					if (action instanceof BlockField) {
						board.setPlayerAt(((FieldAction) action).getField(), Block.getInstance());
						occupiedFields++;
					} else if (action instanceof UnblockField) {
						board.setPlayerAt(((FieldAction) action).getField(), NoPlayer.getInstance());
						occupiedFields--;
					} else
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
					if (action instanceof JokerField) {
						if (board.getPlayerAt(((JokerField) action).getField()) != NoPlayer.getInstance())
							throw new IllegalMoveException("Field is already occupied by "
									+ board.getPlayerAt(((JokerField) action).getField()).getName() + "!");
						board.setPlayerAt(((FieldAction) action).getField(), Joker.getInstance());
						occupiedFields++;
					} else if (action instanceof UnjokerField) {
						if (board.getPlayerAt(((UnjokerField) action).getField()) != Joker.getInstance())
							throw new IllegalMoveException("Field is not a Joker field but occupied by "
									+ board.getPlayerAt(((UnjokerField) action).getField()).getName() + "!");
						board.setPlayerAt(((FieldAction) action).getField(), NoPlayer.getInstance());
						occupiedFields--;
					} else
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
						int index = 0;
						for (int i = 0; i < pairs.length; i++)
							if (pairs[i].player.equals(action.getActingPlayer())) {
								index = i;
								break;
							}
						final ClientPlayerPair[] newPairs = new ClientPlayerPair[pairs.length - 1];
						System.arraycopy(pairs, 0, newPairs, 0, index);
						System.arraycopy(pairs, index + 1, newPairs, index, pairs.length - index - 1);
						pairs = newPairs;
						if (currentPlayerIndex > index)
							currentPlayerIndex--;
						currentPlayerIndex %= pairs.length;
						phase[2] = pairs[currentPlayerIndex].player.getID();
						fireEvent(action);
						if (pairs.length < 2) {
							phase = new int[] {2, 0, action.getActingPlayer().getID() };
							fireEvent(new AllOthersForfeit(pairs[0].player)); // we know that they forfeit because this
																				// server supports no other way to quit
																				// a running game
						}
						return;
					}
					if (action.getActingPlayer().equals(pairs[currentPlayerIndex].player)) {
						final PlaceStone placeStone = (PlaceStone) action;
						if (!board.getPlayerAt(placeStone.getField()).equals(NoPlayer.getInstance()))
							throw new IllegalMoveException("Field is already occupied by "
									+ board.getPlayerAt(placeStone.getField()).getName() + "!");
						board.setPlayerAt(placeStone.getField(), placeStone.getActingPlayer());
						occupiedFields++;
						fireEvent(action);

						if (board.wasWinningMove(placeStone.getField())) {
							phase = new int[] {2, 0, action.getActingPlayer().getID() };
							fireEvent(new PlayerVictory(action.getActingPlayer()));
							return;
						}

						if (occupiedFields >= board.getWidth() * board.getHeight()) {
							phase = new int[] {2, 1 };
							fireEvent(new Tie());
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
		} catch (final ArrayIndexOutOfBoundsException e) {
			// Let control fall through to the IllegalStateException below
		}
		throw new IllegalStateException("Action currently not allowed!");
	}

	@Override
	public Board getCurrentBoard(final Client requester) {
		if (knowsClient(requester))
			return board == null ? null : board.clone();
		throw new UnknownClientException(requester);
	}

	private boolean knowsClient(final Client c) {
		for (final ClientPlayerPair pair : pairs)
			if (pair.client.equals(c))
				return true;
		return false;
	}

	private boolean knowsPlayer(final Player p) {
		for (final ClientPlayerPair pair : pairs)
			if (pair.player.equals(p))
				return true;
		return false;
	}

	private boolean clientPlayerMatch(final Client c, final Player p) {
		for (final ClientPlayerPair pair : pairs)
			if (pair.player.equals(p))
				return pair.client.equals(c);
		return false;
	}

	private void fireEvent(final GameEvent e) {
		for (final Client c : allClients)
			c.gameEvent(e);
	}

	private boolean boardSizeAgreed() {
		final Set<Player> unagreedPlayers = new HashSet<>();
		for (final ClientPlayerPair pair : pairs)
			unagreedPlayers.add(pair.player);

		for (final BoardSizeProposal p : boardSizeProposals)
			if (p.getSize().equals(currentBoardSize))
				unagreedPlayers.remove(p.getActingPlayer());

		return unagreedPlayers.isEmpty();
	}

	private boolean blockDistributionAgreed() {
		final Set<Player> unagreedPlayers = new HashSet<>();
		for (final ClientPlayerPair pair : pairs)
			unagreedPlayers.add(pair.player);

		for (final BlockDistributionAccepted b : acceptedBlockDistributions)
			if (b.getAcceptedBoard().equals(board))
				unagreedPlayers.remove(b.getActingPlayer());

		return unagreedPlayers.isEmpty();
	}

	private boolean jokerDistributionAgreed() {
		final Set<Player> unagreedPlayers = new HashSet<>();
		for (final ClientPlayerPair pair : pairs)
			unagreedPlayers.add(pair.player);

		for (final JokerDistributionAccepted j : acceptedJokerDistributions)
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
		final Client client;
		final Player player;

		/**
		 * Creates a new Client-Player pair with the specified client and player.
		 * 
		 * @param client
		 *            The client.
		 * @param player
		 *            The player.
		 */
		ClientPlayerPair(final Client client, final Player player) {
			this.client = client;
			this.player = player;
		}

		@Override
		public String toString() {
			return "[" + client.toString() + "," + player.toString() + "]";
		}
	}
}