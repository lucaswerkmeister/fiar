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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
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
import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.JokerDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.JokerField;
import de.lucaswerkmeister.code.fiar.framework.event.PlaceStone;
import de.lucaswerkmeister.code.fiar.framework.event.UnblockField;
import de.lucaswerkmeister.code.fiar.framework.event.UnjokerField;
import de.lucaswerkmeister.code.fiar.servers.FixedServer;

/**
 * A Client that runs in the console and handles two players.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class ConsoleClient extends Client implements Runnable {
	private final Server server;
	private final Player p1, p2;
	private final Queue<GameEvent> eventQueue;

	/**
	 * Creates a new {@link ConsoleClient} instance.
	 */
	private ConsoleClient() {
		p1 = new Player("Player 1", Color.blue, 1);
		p2 = new Player("Player 2", Color.yellow, 2);
		server = new FixedServer(new Client[] {this }, new Player[][] {{p1, p2 } });
		eventQueue = new LinkedList<>();
	}

	@Override
	public void gameEvent(GameEvent e) {
		eventQueue.add(e);
		if (e instanceof FieldAction)
			printBoard(server.getCurrentBoard(this));
	}

	@Override
	public int getID() {
		return 0;
	}

	@Override
	public void run() {
		try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in))) {
			System.out.println("Welcome to Five in a Row!");
			System.out.println("Please enter the field size:");
			int boardSize = 0;
			do {
				try {
					boardSize = Integer.parseInt(inputReader.readLine());
					if (boardSize < FixedServer.IN_A_ROW) {
						System.out.println("Board size must be at least " + FixedServer.IN_A_ROW + ", please re-type.");
						continue;
					}
					break;
				} catch (NumberFormatException e) {
					System.out.println("Invalid input - please re-type.");
					continue;
				}
			} while (true);
			server.action(this, new BoardSizeProposal(p1, new Dimension(boardSize, boardSize)));
			server.action(this, new BoardSizeProposal(p2, new Dimension(boardSize, boardSize)));
			eventQueue.poll(); // BoardSizeProposal
			eventQueue.poll(); // BoardSizeProposal
			eventQueue.poll(); // PhaseChange

			// Set Blocks
			System.out.println("You may now mark certain fields as blocked:");
			System.out.println("A blocked field will not count as part of a row for any player.");
			System.out
					.println("Please enter the coordinates of fields you wish to block (x|y) or \"quit\" to continue.");
			System.out.println("Enter the same coordinates again to unblock a field again.");
			String input = inputReader.readLine();
			while (!(input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit") || input
					.equalsIgnoreCase("leave"))) {
				try {
					for (Point p : parseCoordinates(input)) {
						if (server.getCurrentBoard(this).getPlayerAt(p) == Block.getInstance())
							server.action(this, new UnblockField(p1, p));
						else
							server.action(this, new BlockField(p1, p));
						GameEvent event = eventQueue.poll(); // (Un)BlockField
						if (event instanceof BlockField)
							System.out.println("Field " + p.x + "|" + p.y + " blocked.");
						else if (event instanceof UnblockField)
							System.out.println("Field " + p.x + "|" + p.y + " unblocked.");
						else
							throw new Exception("Unexpected event during \"Block fields\" phase.");
					}
				} catch (IllegalArgumentException e) {
					System.out.println("Invalid input. Please re-type the coordinates, but in a different format.");
				}
				input = inputReader.readLine();
			}
			server.action(this, new BlockDistributionAccepted(p1, server.getCurrentBoard(this)));
			server.action(this, new BlockDistributionAccepted(p2, server.getCurrentBoard(this)));
			eventQueue.poll(); // BlockDistributionAccepted
			eventQueue.poll(); // BlockDistributionAccepted
			eventQueue.poll(); // PhaseChange

			// Set Jokers
			System.out.println("You may now mark certain fields as joker fields:");
			System.out.println("A joker field will count as part of a row for every player.");
			System.out
					.println("Please enter the coordinates of fields you wish to set as joker (x|y) or \"quit\" to continue.");
			System.out.println("Enter the same coordinates again to \"un-joker\" a field again.");
			input = inputReader.readLine();
			while (!(input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit") || input
					.equalsIgnoreCase("leave"))) {
				try {
					for (Point p : parseCoordinates(input)) {
						if (server.getCurrentBoard(this).getPlayerAt(p) == Joker.getInstance())
							server.action(this, new UnjokerField(p1, p));
						else if (!server.getCurrentBoard(this).getPlayerAt(p).equals(NoPlayer.getInstance())) {
							System.out.println("That field is already in use! Please enter another field.");
							input = inputReader.readLine();
							continue;
						} else
							server.action(this, new JokerField(p1, p));
						GameEvent event = eventQueue.poll(); // (Un)JokerField
						if (event instanceof JokerField)
							System.out.println("Field " + p.x + "|" + p.y + " marked as joker field.");
						else if (event instanceof UnjokerField)
							System.out.println("Field " + p.x + "|" + p.y + " unmarked as joker field.");
						else
							throw new Exception("Unexpected event during \"Joker fields\" phase.");
					}
				} catch (IllegalArgumentException e) {
					System.out.println("Invalid input. Please re-type the coordinates, but in a different format.");
				}
				input = inputReader.readLine();
			}
			server.action(this, new JokerDistributionAccepted(p1, server.getCurrentBoard(this)));
			server.action(this, new JokerDistributionAccepted(p2, server.getCurrentBoard(this)));
			eventQueue.poll(); // JokerDistributionAccepted
			eventQueue.poll(); // JokerDistributionAccepted
			eventQueue.poll(); // PhaseChange

			// Game
			System.out.println("Game started!");
			printBoard(server.getCurrentBoard(this));
			int player = 1;
			while (server.getPhase(this)[0] == 1) {
				System.out.println("Player " + player + ", where do you want to place your stone?");
				do {
					Set<Point> coordinate;
					try {
						coordinate = parseCoordinates(inputReader.readLine());
					} catch (IllegalArgumentException e) {
						System.out.println("Invalid input. Please re-type the coordinates, but in a different format.");
						continue;
					}
					if (coordinate.size() != 1) {
						System.out.println("Wrong coordinates count! Please re-type the coordinates.");
						continue;
					}
					if (!server.getCurrentBoard(this).getPlayerAt(coordinate.iterator().next())
							.equals(NoPlayer.getInstance())) {
						System.out.println("That field is already in use! Please enter another field.");
						continue;
					}
					server.action(this, new PlaceStone(player == 1 ? p1 : p2, coordinate.iterator().next()));
					eventQueue.poll(); // PlaceStone or GameEnd
					break;
				} while (true);
				player = (player % 2) + 1;
			}
			System.out.println("Player " + server.getPhase(this)[2] + " won!");
		} catch (Throwable t) {
			System.out.println("WHOOPS! An internal error occured. I'm so sorry.");
			t.printStackTrace();
			if (t instanceof ThreadDeath)
				throw (ThreadDeath) t;
		}
	}

	/**
	 * Prints the board to the console.
	 * 
	 * @param b
	 *            The board to print.
	 */
	private void printBoard(Board b) {
		for (int y = 0; y < b.getHeight(); y++) {
			for (int x = 0; x < b.getWidth(); x++)
				switch (b.getPlayerAt(x, y) == null ? 0 : b.getPlayerAt(x, y).getID()) {
				case Joker.ID:
					System.out.print('J');
					break;
				case Block.ID:
					System.out.print('B');
					break;
				case 1:
					System.out.print('X');
					break;
				case 2:
					System.out.print('O');
					break;
				default:
					System.out.print('·');
					break;
				}
			System.out.println();
		}
	}

	/**
	 * Parses an input string into coordinates.
	 * <p>
	 * The following formats are recognized:
	 * <ul>
	 * <li><code>1|1 2|2 3|3</code></li>
	 * <li><code>1|1, 2|2, 3|3</code> (spaces optional)</li>
	 * <li><code>(1|1), (2|2), (3|3)</code> (spaces optional)</li>
	 * <li><code>1,1 2,2 3,3</code></li>
	 * </ul>
	 * 
	 * @param input
	 *            The input string.
	 * @return A {@link Set} containing the recognized coordinates.
	 * @throws IllegalArgumentException
	 *             If the input can't be parsed.
	 */
	private Set<Point> parseCoordinates(String input) throws IllegalArgumentException {
		HashSet<Point> ret = new HashSet<>();
		if (input.contains("|")) {
			// coordinates separated by '|'
			String[] pairs = input.split("[ ,]");
			for (String pair : pairs) {
				if (pair.isEmpty())
					continue;
				String[] subpair = pair.split("\\|");
				ret.add(new Point(Integer.parseInt(subpair[0]), Integer.parseInt(subpair[1])));
			}
		} else {
			// coordinates separated by ','
			String[] pairs = input.split(" ");
			for (String pair : pairs) {
				if (pair.isEmpty())
					continue;
				String[] subpair = pair.split(",");
				ret.add(new Point(Integer.parseInt(subpair[0]), Integer.parseInt(subpair[1])));
			}
		}
		return ret;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            The arguments. Currently ignored.
	 */
	public static void main(String[] args) {
		new Thread(new ConsoleClient()).start();
	}
}