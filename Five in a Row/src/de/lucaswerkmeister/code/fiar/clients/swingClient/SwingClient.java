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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import de.lucaswerkmeister.code.fiar.clients.swingClient.GameFrame.BoardListener;
import de.lucaswerkmeister.code.fiar.framework.Block;
import de.lucaswerkmeister.code.fiar.framework.Client;
import de.lucaswerkmeister.code.fiar.framework.Hoster;
import de.lucaswerkmeister.code.fiar.framework.Joker;
import de.lucaswerkmeister.code.fiar.framework.NoPlayer;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.RemoteClient;
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
public final class SwingClient implements RemoteClient, Runnable {
	private static final long serialVersionUID = -742632519403100569L;
	private static final Random random = new Random();
	private static SwingClient instance;
	private Server server;
	private final Hoster hoster;
	private final List<Player> ownPlayers; // note that the contents of the list are not final
	private final List<Player> allPlayers;
	private GameFrame gui;
	private final Queue<GameEvent> events;
	private int playerIndex = 0;
	private static JDialog addPlayerDialog;

	/**
	 * Creates a new {@link SwingClient} that starts an own local server.
	 */
	public SwingClient() {
		instance = this;
		hoster = null;

		// TODO insert further down
		// gui = new JFrame((Server.IN_A_ROW == 5 ? "Five" : Server.IN_A_ROW) + " in a Row");
		ownPlayers = new LinkedList<>();
		ownPlayers.add(showAddPlayerDialog(true, 1, gui));
		Player player = showAddPlayerDialog(true, 2, gui);
		int id = 3;
		while (player != null) {
			ownPlayers.add(player);
			player = showAddPlayerDialog(false, id++, gui);
		}
		allPlayers = new LinkedList<>(ownPlayers);
		server = new FixedServer(new Client[] {this }, new Player[][] {ownPlayers.toArray(new Player[] {}) });
		events = new LinkedList<>();
	}

	/**
	 * Creates a new {@link SwingClient} running on the specified remote server.
	 * 
	 * @param host
	 *            The host address of the remote server.
	 * @param port
	 *            The port of the remote server.
	 * @throws NotBoundException
	 *             If the remote server can't be found.
	 * @throws RemoteException
	 *             If some remote error occurs.
	 * @throws AccessException
	 *             If the remote server can't be accessed.
	 */
	public SwingClient(String host, int port) throws AccessException, RemoteException, NotBoundException {
		super(); // avoid call to this()
		instance = this;
		hoster = (Hoster) LocateRegistry.getRegistry(host, port).lookup("hoster");
		UnicastRemoteObject.exportObject(this, 0);
		ownPlayers = new LinkedList<>();
		allPlayers = new LinkedList<>();
		hoster.addClient(this);
		// gui = new JFrame((Server.IN_A_ROW == 5 ? "Five" : Server.IN_A_ROW) + " in a Row");
		events = new LinkedList<>();
	}

	@Override
	public void playerJoined(Player player) {
		allPlayers.add(player);
		if (addPlayerDialog != null && addPlayerDialog.isVisible()) {
			addPlayerDialog.setName("Reload dialog:" + findID());
			addPlayerDialog.setVisible(false);
		}
	}

	@Override
	public void playerLeft(Player player) {
		allPlayers.remove(player);
	}

	@Override
	public void gameStarts(Server server) {
		if (addPlayerDialog != null && addPlayerDialog.isVisible()) {
			addPlayerDialog.setName("Stop adding players");
			addPlayerDialog.setVisible(false);
		}
		this.server = server;
		System.out.println("STARTED");
		synchronized (this) {
			this.notify();
		}
	}

	@Override
	public void gameEvent(final GameEvent e) throws RemoteException {
		if (e instanceof Forfeit) {
			final Player p = ((Forfeit) e).getActingPlayer();
			final int index = allPlayers.indexOf(p);
			allPlayers.remove(p);
			if (playerIndex == index) {
				playerIndex %= allPlayers.size();
				gui.setStatus(allPlayers.get(playerIndex).getName() + "'"
						+ (endsWithSSound(allPlayers.get(playerIndex).getName()) ? "" : "s") + " turn!");
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
			if (server == null) {
				if (hoster == null)
					throw new Exception("No server and no hoster! Aborting.");
				Player player = showAddPlayerDialog(false, findID(), gui);
				while (player != null) {
					ownPlayers.add(player);
					// allPlayers.add(player); don't add here as it will be added in playerJoined() above
					hoster.addPlayer(this, player);
					player = showAddPlayerDialog(false, findID(), gui);
				}
				JOptionPane.showMessageDialog(gui, "Waiting for the game to start...", "Info",
						JOptionPane.INFORMATION_MESSAGE);
				synchronized (this) {
					wait();
				}
				System.out.println("Continuing");
			}
			// choose board size
			final Dimension boardSize = showChooseBoardSizeDialog(gui);
			for (final Player p : ownPlayers) {
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
							server.action(instance, server.getCurrentBoard(instance).getPlayerAt(xy) == NoPlayer
									.getInstance() ? new BlockField(ownPlayers.get(0), xy) : new UnblockField(
									ownPlayers.get(0), xy));
							events.poll(); // BlockField / UnblockField
						} else if (phase[0] == 0 && phase[1] == 2) {
							// jokers
							server.action(instance, server.getCurrentBoard(instance).getPlayerAt(xy) == NoPlayer
									.getInstance() ? new JokerField(ownPlayers.get(0), xy) : new UnjokerField(
									ownPlayers.get(0), xy));
							events.poll(); // JokerField / UnjokerField
						} else if (phase[0] == 1 && phase[1] == 1) {
							// move
							server.action(instance, new PlaceStone(ownPlayers.get(playerIndex), xy));
							events.poll(); // PlaceStone
							playerIndex = (playerIndex + 1) % ownPlayers.size();
							if (events.isEmpty())
								gui.setStatus(ownPlayers.get(playerIndex).getName() + "'"
										+ (endsWithSSound(ownPlayers.get(playerIndex).getName()) ? "" : "s") + " turn!");
							else {
								final GameEvent event = events.poll();
								if (event instanceof PlayerVictory) {
									final String message =
											((PlayerVictory) event).getWinningPlayer().getName() + " wins!";
									JOptionPane.showMessageDialog(gui, message);
									gui.setStatus(message);
								}
								if (event instanceof GameEnd)
									gui.setEnabledAll(false);
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
					for (final Player p : ownPlayers) {
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
					for (final Player p : ownPlayers) {
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
						server.action(instance, new Forfeit(ownPlayers.get(playerIndex)));
					} catch (IllegalStateException | IllegalMoveException | RemoteException e1) {
						e1.printStackTrace();
					}
				}
			});
			gui.setStatus(ownPlayers.get(playerIndex).getName() + "'"
					+ (endsWithSSound(ownPlayers.get(playerIndex).getName()) ? "" : "s") + " turn!");
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
	 * Finds an unused ID.
	 * 
	 * @return An ID that is not yet used by any player known to the client.
	 */
	private int findID() {
		int id = 0;
		boolean isUsed;
		do {
			id++;
			isUsed = false;
			for (Player p : ownPlayers)
				if (p.getID() == id) {
					isUsed = true;
					break;
				}
		} while (isUsed);
		return id;
	}

	/**
	 * Shows the Add Player dialog and returns the player that was added.
	 * 
	 * @param forcePlayer
	 *            If set to <code>true</code>, the user is forced to add a player. Otherwise, he is allowed to cancel
	 *            the addition of more players.
	 * @param id
	 *            The id of the new player.
	 * @param owner
	 *            The owner of the dialog.
	 * @return A {@link Player} with the user-specified name and color, or <code>null</code> if the user chose not to
	 *         add another player.
	 */
	public static Player showAddPlayerDialog(final boolean forcePlayer, int id, final JFrame owner) {
		addPlayerDialog = new JDialog(owner, "Add player", true);
		addPlayerDialog.setLayout(new FlowLayout());
		addPlayerDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addPlayerDialog.setAlwaysOnTop(true);

		final JTextField name = new JTextField("Player " + id, 10);
		name.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				addPlayerDialog.setName("Add Player");
				addPlayerDialog.setVisible(false);
			}
		});
		addPlayerDialog.add(name);
		// This "random color" code is based on the following stackoverflow answer:
		// http://stackoverflow.com/a/4247219/1420237
		// @formatter:off
		final SelectableColor color 
			= new SelectableColor(Color.getHSBColor(random.nextFloat(), (random.nextInt(2) + 7) / 10f, 0.9f));
		// @formatter:on
		addPlayerDialog.add(color);
		final JButton addPlayer = new JButton("Add Player");
		addPlayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				addPlayerDialog.setName(e.getActionCommand());
				addPlayerDialog.setVisible(false);
			}
		});
		addPlayerDialog.add(addPlayer);
		final JButton cancel = new JButton("Stop adding players");
		cancel.addActionListener(addPlayer.getActionListeners()[0]);
		if (forcePlayer) {
			cancel.setEnabled(false);
			cancel.setToolTipText("You need at least two players to play");
		}
		addPlayerDialog.add(cancel);

		addPlayerDialog.pack();
		addPlayerDialog.setVisible(true);
		while (true) {
			switch (addPlayerDialog.getName()) {
			case "Add Player":
				// This Easter Egg is clearly of the "WTF" type.
				return new Player(name.getText().equals("All your base are belong to us") ? "CATS" : name.getText(),
						color.getColor(), id);
			case "Stop adding players":
				return null;
			default:
				if (addPlayerDialog.getName().startsWith("Reload dialog:")) {
					boolean needToReplaceName = name.getText().equals("Player " + id);
					id = Integer.parseInt(addPlayerDialog.getName().substring("Reload dialog:".length()));
					if (needToReplaceName)
						name.setText("Player " + id);
					addPlayerDialog.setVisible(true);
					continue;
				}
				throw new RuntimeException("Unexpected error in Swing Client while adding player! Name was "
						+ addPlayerDialog.getName() + " (expected: \"Add Player\" or \"Stop adding players\"");
			}
		}
	}

	/**
	 * Shows the Choose Board Size dialog and returns the chosen size.
	 * 
	 * @param owner
	 *            The owner of the dialog.
	 * @return The user-chosen size.
	 */
	public static Dimension showChooseBoardSizeDialog(final JFrame owner) {
		final JDialog dialog = new JDialog(owner, "Choose board size", true);
		dialog.setLayout(new FlowLayout());
		dialog.toFront();
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		final JSpinner boardWidth = new JSpinner(new SpinnerNumberModel(15, 5, Integer.MAX_VALUE, 1));
		boardWidth.setPreferredSize(new Dimension(50, boardWidth.getPreferredSize().height));
		((JSpinner.DefaultEditor) boardWidth.getEditor()).getTextField().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					dialog.setVisible(false);
			}
		});
		dialog.add(boardWidth);
		dialog.add(new JLabel("×"));
		final JSpinner boardHeight = new JSpinner(new SpinnerNumberModel(15, 5, Integer.MAX_VALUE, 1));
		boardHeight.setPreferredSize(new Dimension(50, boardHeight.getPreferredSize().height));
		((JSpinner.DefaultEditor) boardHeight.getEditor()).getTextField().addKeyListener(
				((JSpinner.DefaultEditor) boardWidth.getEditor()).getTextField().getKeyListeners()[0]);
		dialog.add(boardHeight);
		final JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		dialog.add(ok);

		dialog.pack();
		dialog.setVisible(true);
		return new Dimension((Integer) boardWidth.getValue(), (Integer) boardHeight.getValue());
	}

	/**
	 * Determines if a specific string ends with an "s" sound.
	 * <p>
	 * This is used to determine how the genitive of that string is built.
	 * 
	 * @param token
	 *            The string.
	 * @return <code>true</code> if that string ends with an "s" sound, <code>false</code> otherwise.
	 */
	private static boolean endsWithSSound(final String token) {
		if (token.endsWith("ques"))
			return false;
		if (token.endsWith("aux"))
			return false;
		if (token.endsWith("s"))
			return true;
		if (token.endsWith("x"))
			return true;
		if (token.endsWith("ce"))
			return true;
		if (token.endsWith("se"))
			return true;
		return false;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            The arguments.
	 *            <ul>
	 *            <li>If <code>args.length == 0</code>, the SwingClient runs on a local server.</li>
	 *            <li>If <code>args.length == 2</code>, the SwingClient runs on the specified remote server (arguments:
	 *            host + port).</li>
	 *            </ul>
	 */
	public static void main(final String[] args) {
		if (instance == null) {
			switch (args.length) {
			case 0:
				instance = new SwingClient();
				break;
			case 2:
				try {
					instance = new SwingClient(args[0], Integer.parseInt(args[1]));
				} catch (NumberFormatException | RemoteException | NotBoundException e) {
					e.printStackTrace();
					System.exit(1);
				}
				break;
			default:
				System.out
						.println("Incorrect command line! Please specify either zero or two (host + port) arguments!");
				System.exit(1);
			}
		}
		new Thread(instance).start();
	}
}