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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

import de.lucaswerkmeister.code.fiar.framework.Client;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.Server;
import de.lucaswerkmeister.code.fiar.framework.event.BlockDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.BoardSizeProposal;
import de.lucaswerkmeister.code.fiar.framework.event.FieldAction;
import de.lucaswerkmeister.code.fiar.framework.event.GameEnd;
import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.JokerDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.PlaceStone;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerVictory;
import de.lucaswerkmeister.code.fiar.framework.exception.IllegalMoveException;
import de.lucaswerkmeister.code.fiar.servers.FixedServer;

/**
 * A client that runs in a Swing GUI and handles two or more players.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class SwingClient extends Client implements Runnable {
	private static final Random random = new Random();
	private final Server server;
	private final List<Player> players; // note that the contents of the list are not final
	private final JFrame gui;
	private final Queue<GameEvent> events;
	private final JPanel board;
	private final JPanel buttons;
	private final JLabel statusBar;
	private Field[][] fields;
	private int playerIndex = 0;
	private static final SwingClient instance = new SwingClient();

	public SwingClient() {
		gui = new JFrame("Five in a Row");
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
		JPanel content = new JPanel(new BorderLayout());
		board = new JPanel();
		content.add(board, BorderLayout.CENTER);
		buttons = new JPanel();
		content.add(buttons, BorderLayout.EAST);
		statusBar = new JLabel("Ready");
		content.add(statusBar, BorderLayout.SOUTH);
		gui.setContentPane(content);
	}

	@Override
	public void gameEvent(GameEvent e) {
		if (e instanceof GameEnd)
			System.out.println("Game ended");
		if (e instanceof FieldAction) {
			FieldAction fa = (FieldAction) e;
			fields[fa.getField().x][fa.getField().y].setPlayer(fa.getActingPlayer());
		}
		events.add(e);
	}

	@Override
	public int getID() {
		return 0;
	}

	@Override
	public void run() {
		try {
			// choose board size
			final Dimension boardSize = showChooseBoardSizeDialog(gui);
			for (Player p : players) {
				server.action(this, new BoardSizeProposal(p, boardSize));
				events.poll(); // BoardSizeProposal
			}
			events.poll(); // PhaseChange

			for (Player p : players) {
				server.action(this, new BlockDistributionAccepted(p, server.getCurrentBoard(this)));
				events.poll(); // BlockDistributionAccepted
			}
			events.poll(); // PhaseChange

			for (Player p : players) {
				server.action(this, new JokerDistributionAccepted(p, server.getCurrentBoard(this)));
				events.poll(); // JokerDistributionAccepted
			}
			events.poll(); // PhaseChange

			board.setLayout(new GridLayout(boardSize.width, boardSize.height, 0, 0));
			fields = new Field[boardSize.width][boardSize.height];
			final Dimension fieldSize = new Dimension(10, 10);
			for (int x = 0; x < boardSize.width; x++) {
				for (int y = 0; y < boardSize.height; y++) {
					final Field f = new Field(null, fieldSize);
					final Point xy = new Point(x, y);
					f.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if (f.isEnabled()) // disabled lightweight components still receive MouseEvents
								try {
									server.action(instance, new PlaceStone(players.get(playerIndex), xy));
									events.poll(); // PlaceStone
									playerIndex = (playerIndex + 1) % players.size();
									if (events.isEmpty()) {
										statusBar.setText(players.get(playerIndex).getName() + "'"
												+ (endsWithSSound(players.get(playerIndex).getName()) ? "" : "s")
												+ " turn!");
									} else {
										GameEvent event = events.poll();
										if (event instanceof PlayerVictory) {
											JOptionPane.showMessageDialog(gui, ((PlayerVictory) event)
													.getWinningPlayer().getName() + " won!");
										}
										if (event instanceof GameEnd) {
											for (int x = 0; x < boardSize.width; x++) {
												for (int y = 0; y < boardSize.height; y++) {
													fields[x][y].setEnabled(false);
												}
											}
										}
									}
								} catch (IllegalStateException | IllegalMoveException e1) {
									e1.printStackTrace();
								}
						}
					});
					fields[x][y] = f;
					board.add(f);
				}
			}
			gui.add(board);
			gui.pack();
			gui.setVisible(true);
		} catch (Throwable t) {
			System.out.println("WHOOPS! An internal error occured. I'm so sorry.");
			t.printStackTrace();
			if (t instanceof ThreadDeath)
				throw (ThreadDeath) t;
		}
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
	public static Player showAddPlayerDialog(boolean forcePlayer, int id, JFrame owner) {
		final JDialog dialog = new JDialog(owner, "Add player", true);
		dialog.setLayout(new FlowLayout());
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setAlwaysOnTop(true);

		JTextField name = new JTextField("Player " + id, 10);
		dialog.add(name);
		// This "random color" code is based on the following stackoverflow answer:
		// http://stackoverflow.com/a/4247219/1420237
		SelectableColor color =
				new SelectableColor(Color.getHSBColor(random.nextFloat(), (random.nextInt(2) + 7) / 10f, 0.9f));
		dialog.add(color);
		JButton addPlayer = new JButton("Add Player");
		addPlayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setName(e.getActionCommand());
				dialog.setVisible(false);
			}
		});
		dialog.add(addPlayer);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(addPlayer.getActionListeners()[0]);
		if (forcePlayer) {
			cancel.setEnabled(false);
			cancel.setToolTipText("You need at least two players to play");
		}
		dialog.add(cancel);

		dialog.pack();
		dialog.setVisible(true);
		switch (dialog.getName()) {
		case "Add Player":
			return new Player(name.getText(), color.getColor(), id);
		case "Cancel":
			return null;
		default:
			throw new RuntimeException("Unexpected error in Swing Client while adding player! Name was "
					+ dialog.getName() + " (expected: \"Add Player\" or \"Cancel\"");
		}
	}

	/**
	 * Shows the Choose Board Size dialog and returns the chosen size.
	 * 
	 * @param owner
	 *            The owner of the dialog.
	 * @return The user-chosen size.
	 */
	public static Dimension showChooseBoardSizeDialog(JFrame owner) {
		final JDialog dialog = new JDialog(owner, "Choose board size", true);
		dialog.setLayout(new FlowLayout());
		dialog.toFront();
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		JSpinner boardWidth = new JSpinner(new SpinnerNumberModel(15, 5, Integer.MAX_VALUE, 1));
		boardWidth.setPreferredSize(new Dimension(50, boardWidth.getPreferredSize().height));
		dialog.add(boardWidth);
		dialog.add(new JLabel("×"));
		JSpinner boardHeight = new JSpinner(new SpinnerNumberModel(15, 5, Integer.MAX_VALUE, 1));
		boardHeight.setPreferredSize(new Dimension(50, boardHeight.getPreferredSize().height));
		dialog.add(boardHeight);
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
	private static boolean endsWithSSound(String token) {
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
	 *            The arguments. Currently ignored.
	 */
	public static void main(String[] args) {
		new Thread(instance).start();
	}
}