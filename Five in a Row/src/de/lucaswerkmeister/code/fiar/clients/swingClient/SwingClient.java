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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;

import de.lucaswerkmeister.code.fiar.framework.Client;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.Server;
import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.servers.FixedServer;

public class SwingClient extends Client implements Runnable {
	private final Server server;
	private final List<Player> players; // note that the contents of the list are not final
	private final JFrame gui;

	public SwingClient() {
		gui = new JFrame("Five in a row");
		players = new LinkedList<>();
		players.add(showAddPlayerDialog(true, 1, gui));
		int id = 2;
		Player player = showAddPlayerDialog(true, id++, gui);
		while (player != null) {
			players.add(player);
			player = showAddPlayerDialog(false, id++, gui);
		}
		server = new FixedServer(new Client[] {this }, new Player[][] {players.toArray(new Player[] {}) });
	}

	@Override
	public void gameEvent(GameEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            The arguments. Currently ignored.
	 */
	public static void main(String[] args) {
		new Thread(new SwingClient()).start();
	}

	/**
	 * Shows the Add Player Dialog and returns the player that was added.
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
		final JDialog dialog = new JDialog(owner, "Add player");
		dialog.setLayout(new FlowLayout());
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setModal(true);

		JTextField name = new JTextField("Player name");
		dialog.add(name);
		SelectableColor color = new SelectableColor(Color.black);
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
}