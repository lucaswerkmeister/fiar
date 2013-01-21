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
package de.lucaswerkmeister.code.fiar.clients.swingClient;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import de.lucaswerkmeister.code.fiar.clients.swingClient.GameFrame.BoardListener;
import de.lucaswerkmeister.code.fiar.framework.Block;
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
import de.lucaswerkmeister.code.fiar.framework.event.GameEnd;
import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.JokerDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.JokerField;
import de.lucaswerkmeister.code.fiar.framework.event.PlaceStone;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerVictory;
import de.lucaswerkmeister.code.fiar.framework.event.UnblockField;
import de.lucaswerkmeister.code.fiar.framework.event.UnjokerField;
import de.lucaswerkmeister.code.fiar.framework.exception.IllegalMoveException;
import de.lucaswerkmeister.code.fiar.servers.FixedServer;

/**
 * A client that runs in a Swing GUI and handles two or more players.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public final class LocalClient implements Client, Runnable {
	private static LocalClient instance;
	private Server server;
	private final List<Player> players; // note that the contents of the list are not final
	private GameFrame gui;
	private final Queue<GameEvent> events;
	private int playerIndex = 0;

	/**
	 * Creates a new {@link LocalClient} that starts an own local server.
	 */
	public LocalClient() {
		instance = this;
		players = new LinkedList<>();
		players.add(GameFrame.showAddPlayerDialog(true, 1, gui));
		Player player = GameFrame.showAddPlayerDialog(true, 2, gui);
		int id = 3;
		while (player != null) {
			players.add(player);
			player = GameFrame.showAddPlayerDialog(false, id++, gui);
		}
		server = new FixedServer(new Client[] {this }, new Player[][] {players.toArray(new Player[] {}) });
		events = new LinkedList<>();
	}

	@Override
	public void gameEvent(final GameEvent e) throws RemoteException {
		if (e instanceof Forfeit) {
			final Player p = ((Forfeit) e).getActingPlayer();
			final int index = players.indexOf(p);
			players.remove(p);
			if (playerIndex == index) {
				playerIndex %= players.size();
				gui.setStatus(players.get(playerIndex).getName() + "'"
						+ (GameFrame.endsWithSSound(players.get(playerIndex).getName()) ? "" : "s") + " turn!");
			} else if (playerIndex > index)
				playerIndex--;
		}
		if (e instanceof GameEnd) {
			gui.setStatus("Game ended");
			gui.setButtons(new String[0]);
		}
		if (e instanceof FieldAction) {
			final FieldAction fa = (FieldAction) e;
			gui.setPlayerAt(fa.getField().x, fa.getField().y, server.getCurrentBoard(this).getPlayerAt(fa.getField()));
		}
		events.add(e);
	}

	@Override
	public void run() {
		try {
			// choose board size
			final Dimension boardSize = GameFrame.showChooseBoardSizeDialog(gui);
			for (final Player p : players) {
				server.action(this, new BoardSizeProposal(p, boardSize));
				events.poll(); // BoardSizeProposal
			}
			events.poll(); // PhaseChange

			gui =
					new GameFrame(server.getCurrentBoard(this), (Server.IN_A_ROW == 5 ? "Five" : Server.IN_A_ROW)
							+ " in a Row");
			gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			gui.addBoardListener(new BoardListener() {
				@Override
				public void fieldClicked(Field field) {
					try {
						final int[] phase = server.getPhase(instance);
						final Point xy = field.getField();
						if (phase[0] == 0 && phase[1] == 1) {
							// blocking
							server.action(instance,
									server.getCurrentBoard(instance).getPlayerAt(xy) == NoPlayer.getInstance()
											? new BlockField(players.get(0), xy) : new UnblockField(players.get(0), xy));
							events.poll(); // BlockField / UnblockField
						} else if (phase[0] == 0 && phase[1] == 2) {
							// jokers
							server.action(instance,
									server.getCurrentBoard(instance).getPlayerAt(xy) == NoPlayer.getInstance()
											? new JokerField(players.get(0), xy) : new UnjokerField(players.get(0), xy));
							events.poll(); // JokerField / UnjokerField
						} else if (phase[0] == 1 && phase[1] == 1) {
							// move
							server.action(instance, new PlaceStone(players.get(playerIndex), xy));
							events.poll(); // PlaceStone
							playerIndex = (playerIndex + 1) % players.size();
							if (events.isEmpty())
								gui.setStatus(players.get(playerIndex).getName() + "'"
										+ (GameFrame.endsWithSSound(players.get(playerIndex).getName()) ? "" : "s")
										+ " turn!");
							else {
								final GameEvent event = events.poll();
								if (event instanceof PlayerVictory) {
									final String message =
											((PlayerVictory) event).getWinningPlayer().getName() + " wins!";
									JOptionPane.showMessageDialog(gui, message);
									gui.setStatus(message);
								}
								if (event instanceof GameEnd)
									gui.setEnabled(false);
							}
						}
					} catch (IllegalStateException | IllegalMoveException | RemoteException e1) {
						e1.printStackTrace();
					}
				}
			});

			// blocks
			gui.setStatus("Select blocked fields");
			gui.setButtons(new String[] {"Done setting blocked fields" });
			gui.removeAllActionListeners();
			gui.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					for (final Player p : players) {
						try {
							server.action(instance, new BlockDistributionAccepted(p, server.getCurrentBoard(instance)));
						} catch (IllegalStateException | IllegalMoveException | RemoteException e1) {
							e1.printStackTrace();
						}
						events.poll(); // BlockDistributionAccepted
					}
					events.poll(); // PhaseChange
					synchronized (instance) {
						instance.notify();
					}
				}
			});
			gui.setVisible(true);
			synchronized (this) {
				wait();
			}
			gui.removeAllActionListeners();
			// disable blocked fields
			for (int x = 0; x < boardSize.width; x++)
				for (int y = 0; y < boardSize.height; y++)
					if (server.getCurrentBoard(this).getPlayerAt(x, y) == Block.getInstance())
						gui.setEnabled(x, y, false);

			// jokers
			gui.setStatus("Select joker fields");
			gui.setButtons(new String[] {"Done setting joker fields" });
			gui.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					for (final Player p : players) {
						try {
							server.action(instance, new JokerDistributionAccepted(p, server.getCurrentBoard(instance)));
						} catch (IllegalStateException | IllegalMoveException | RemoteException e1) {
							e1.printStackTrace();
						}
						events.poll(); // JokerDistributionAccepted
					}
					events.poll(); // PhaseChange
					synchronized (instance) {
						instance.notify();
					}
				}
			});
			synchronized (this) {
				wait();
			}
			gui.removeAllActionListeners();
			// disable blocked fields
			for (int x = 0; x < boardSize.width; x++)
				for (int y = 0; y < boardSize.height; y++)
					if (server.getCurrentBoard(this).getPlayerAt(x, y) == Joker.getInstance())
						gui.setEnabled(x, y, false);

			// normal gameplay
			gui.setButtons(new String[] {"Forfeit" });
			gui.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					try {
						server.action(instance, new Forfeit(players.get(playerIndex)));
					} catch (IllegalStateException | IllegalMoveException | RemoteException e1) {
						e1.printStackTrace();
					}
				}
			});
			gui.setStatus(players.get(playerIndex).getName() + "'"
					+ (GameFrame.endsWithSSound(players.get(playerIndex).getName()) ? "" : "s") + " turn!");
			// everything after this point is handled in ActionListeners
		} catch (final Throwable t) { // I will catch Throwable whenever I feel like it and nobody can forbid it.
			// I always want the user to see this message before the confusing log starts
			System.out.println("WHOOPS! An internal error occured. I'm so sorry.");
			System.out.println("If you want to report this to the developer, please include the information below:");
			// VMErrors and ThreadDeaths should always be re-thrown;
			// for ThreadDeaths, the stack trace normally isn't printed,
			// so I do that manually here.
			// For all other exceptions, I print the stack trace and don't re-throw them.
			if (t instanceof VirtualMachineError)
				throw (VirtualMachineError) t;
			else if (t instanceof ThreadDeath) {
				t.printStackTrace();
				throw (ThreadDeath) t;
			}
			t.printStackTrace();
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            The arguments.
	 *            <ul>
	 *            <li>If <code>args.length == 0</code>, the LocalClient runs on a local server.</li>
	 *            <li>If <code>args.length == 2</code>, the LocalClient runs on the specified remote server (arguments:
	 *            host + port).</li>
	 *            </ul>
	 */
	public static void main(final String[] args) {
		instance = new LocalClient();
		new Thread(instance).start();
	}
}