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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

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
	private final List<Player> players; // note that the contents of the list are not final
	private final JFrame gui;
	private final Queue<GameEvent> events;
	private final JPanel board;
	private final JPanel buttons;
	private final JLabel statusBar;
	private Field[][] fields;
	private int playerIndex = 0;
	private static JDialog addPlayerDialog;

	/**
	 * Creates a new {@link SwingClient} that starts an own local server.
	 */
	public SwingClient() {
		instance = this;
		hoster = null;
		gui = new JFrame((Server.IN_A_ROW == 5 ? "Five" : Server.IN_A_ROW) + " in a Row");
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		players = new LinkedList<>();
		players.add(showAddPlayerDialog(true, 1, gui));
		Player player = showAddPlayerDialog(true, 2, gui);
		int id = 3;
		while (player != null) {
			players.add(player);
			player = showAddPlayerDialog(false, id++, gui);
		}
		server = new FixedServer(new Client[] {this }, new Player[][] {players.toArray(new Player[] {}) });
		events = new LinkedList<>();
		final JPanel content = new JPanel(new BorderLayout());
		board = new JPanel();
		content.add(board, BorderLayout.CENTER);
		buttons = new JPanel();
		content.add(buttons, BorderLayout.EAST);
		statusBar = new JLabel("Ready");
		content.add(statusBar, BorderLayout.SOUTH);
		gui.setContentPane(content);
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
		hoster.addClient(this);
		players = new LinkedList<>();
		gui = new JFrame((Server.IN_A_ROW == 5 ? "Five" : Server.IN_A_ROW) + " in a Row");
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		events = new LinkedList<>();
		final JPanel content = new JPanel(new BorderLayout());
		board = new JPanel();
		content.add(board, BorderLayout.CENTER);
		buttons = new JPanel();
		content.add(buttons, BorderLayout.EAST);
		statusBar = new JLabel("Ready");
		content.add(statusBar, BorderLayout.SOUTH);
		gui.setContentPane(content);
	}

	@Override
	public void playerJoined(Player player) {
		players.add(player);
	}

	@Override
	public void playerLeft(Player player) {
		players.remove(player);
	}

	@Override
	public void gameStarts(Server server) {
		if (addPlayerDialog != null) {
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
			final int index = players.indexOf(p);
			players.remove(p);
			if (playerIndex == index) {
				playerIndex %= players.size();
				statusBar.setText(players.get(playerIndex).getName() + "'"
						+ (endsWithSSound(players.get(playerIndex).getName()) ? "" : "s") + " turn!");
			} else if (playerIndex > index)
				playerIndex--;
		}
		if (e instanceof GameEnd) {
			statusBar.setText("Game ended");
			buttons.removeAll();
			gui.pack();
		}
		if (e instanceof FieldAction) {
			final FieldAction fa = (FieldAction) e;
			fields[fa.getField().x][fa.getField().y].setPlayer(server.getCurrentBoard(this).getPlayerAt(fa.getField()));
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
					players.add(player);
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
			for (final Player p : players) {
				server.action(this, new BoardSizeProposal(p, boardSize));
				events.poll(); // BoardSizeProposal
			}
			events.poll(); // PhaseChange

			board.setLayout(new GridLayout(boardSize.width, boardSize.height, 0, 0));
			fields = new Field[boardSize.width][boardSize.height];
			final Dimension fieldSize = new Dimension(10, 10);
			for (int x = 0; x < boardSize.width; x++)
				for (int y = 0; y < boardSize.height; y++) {
					final Field f = new Field(null, fieldSize);
					final Point xy = new Point(x, y);
					// @formatter:off The formatter keeps inserting more and more blank lines before the @Override
					f.addMouseListener(new MouseAdapter() {
						@Override
						// @formatter:on
								public
								void mouseClicked(final MouseEvent e) {
							if (f.isEnabled()) // disabled lightweight components still receive MouseEvents
								try {
									final int[] phase = server.getPhase(instance);
									if (phase[0] == 0 && phase[1] == 1) {
										// blocking
										server.action(instance,
												server.getCurrentBoard(instance).getPlayerAt(xy) == NoPlayer
														.getInstance() ? new BlockField(players.get(0), xy)
														: new UnblockField(players.get(0), xy));
										events.poll(); // BlockField / UnblockField
									} else if (phase[0] == 0 && phase[1] == 2) {
										// jokers
										server.action(instance,
												server.getCurrentBoard(instance).getPlayerAt(xy) == NoPlayer
														.getInstance() ? new JokerField(players.get(0), xy)
														: new UnjokerField(players.get(0), xy));
										events.poll(); // JokerField / UnjokerField
									} else if (phase[0] == 1 && phase[1] == 1) {
										// move
										server.action(instance, new PlaceStone(players.get(playerIndex), xy));
										events.poll(); // PlaceStone
										playerIndex = (playerIndex + 1) % players.size();
										if (events.isEmpty())
											statusBar.setText(players.get(playerIndex).getName() + "'"
													+ (endsWithSSound(players.get(playerIndex).getName()) ? "" : "s")
													+ " turn!");
										else {
											final GameEvent event = events.poll();
											if (event instanceof PlayerVictory) {
												//@formatter:off
												final String message
													= ((PlayerVictory) event).getWinningPlayer().getName() + " wins!";
												//@formatter:on
												JOptionPane.showMessageDialog(gui, message);
												statusBar.setText(message);
											}
											if (event instanceof GameEnd)
												for (int x = 0; x < boardSize.width; x++)
													for (int y = 0; y < boardSize.height; y++)
														fields[x][y].setEnabled(false);
										}
									}
								} catch (IllegalStateException | IllegalMoveException | RemoteException e1) {
									e1.printStackTrace();
								}
						}
					});
					fields[x][y] = f;
					board.add(f);
				}
			gui.add(board);

			// blocks
			statusBar.setText("Select blocked fields");
			buttons.removeAll();
			final JButton doneBlocks = new JButton("Done setting blocked fields");
			doneBlocks.addActionListener(new ActionListener() {

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
			buttons.add(doneBlocks);
			gui.pack();
			gui.setVisible(true);
			synchronized (this) {
				wait();
			}
			// disable blocked fields
			for (int x = 0; x < boardSize.width; x++)
				for (int y = 0; y < boardSize.height; y++)
					if (server.getCurrentBoard(this).getPlayerAt(x, y) == Block.getInstance())
						fields[x][y].setEnabled(false);

			// jokers
			statusBar.setText("Select joker fields");
			buttons.removeAll();
			final JButton doneJokers = new JButton("Done setting joker fields");
			doneJokers.addActionListener(new ActionListener() {

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
			buttons.add(doneJokers);
			gui.pack();
			synchronized (this) {
				wait();
			}
			// disable blocked fields
			for (int x = 0; x < boardSize.width; x++)
				for (int y = 0; y < boardSize.height; y++)
					if (server.getCurrentBoard(this).getPlayerAt(x, y) == Joker.getInstance())
						fields[x][y].setEnabled(false);

			// normal gameplay
			buttons.removeAll();
			final JButton forfeit = new JButton("Forfeit");
			forfeit.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					try {
						server.action(instance, new Forfeit(players.get(playerIndex)));
					} catch (IllegalStateException | IllegalMoveException | RemoteException e1) {
						e1.printStackTrace();
					}
				}
			});
			buttons.add(forfeit);
			gui.pack();
			statusBar.setText(players.get(playerIndex).getName() + "'"
					+ (endsWithSSound(players.get(playerIndex).getName()) ? "" : "s") + " turn!");
			// everything after this point is handled in ActionListeners
		} catch (final Throwable t) { // I will catch Throwable whenever I feel like it and nobody can forbid it.
			System.out.println("WHOOPS! An internal error occured. I'm so sorry.");
			System.out.println("If you want to report this to the developer, please include the information below: ");
			t.printStackTrace(System.out);
			// unneccessary because the thread exits anyways
			// if (t instanceof ThreadDeath || t instanceof VirtualMachineError)
			// throw (Error) t; // OutOfMemoryExceptions etc.
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
			for (Player p : players)
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
	public static Player showAddPlayerDialog(final boolean forcePlayer, final int id, final JFrame owner) {
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
		switch (addPlayerDialog.getName()) {
		case "Add Player":
			// This Easter Egg is clearly of the "WTF" type.
			return new Player(name.getText().equals("All your base are belong to us") ? "CATS" : name.getText(),
					color.getColor(), id);
		case "Stop adding players":
			return null;
		default:
			throw new RuntimeException("Unexpected error in Swing Client while adding player! Name was "
					+ addPlayerDialog.getName() + " (expected: \"Add Player\" or \"Stop adding players\"");
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