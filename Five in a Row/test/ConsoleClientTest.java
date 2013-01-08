import static junit.framework.Assert.assertEquals;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import junit.framework.Assert;

import org.junit.Test;

import de.lucaswerkmeister.code.fiar.clients.ConsoleClient;
import de.lucaswerkmeister.code.fiar.framework.Block;
import de.lucaswerkmeister.code.fiar.framework.Board;
import de.lucaswerkmeister.code.fiar.framework.Joker;
import de.lucaswerkmeister.code.fiar.framework.NoPlayer;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.servers.ArrayBoard;

/**
 * The tests in this class test the {@link ConsoleClient}.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class ConsoleClientTest {
	// Note that out is a Reader reading from the client's out and in is a Writer writing to the client's in.
	private BufferedReader out;
	private BufferedWriter in;
	private Board board;

	/**
	 * Tests basic gameplay: No blocking and jokers, no illegal input.
	 * <p>
	 * This plays exactly the same game as {@link FixedServerTest#testBasic()}.
	 * <p>
	 * Warning: This test takes a long time to finish (ca. 13s on the ATIS computers) - don't be fooled by that!
	 */
	@Test
	public void testBasic() {
		try {
			// Capture System.out and System.in.
			captureIO();
			ConsoleClient.main(new String[] {}); // runs in a new thread
			read("Welcome to Five in a Row!");
			read("Please enter the field size:");
			write("10");
			read("You may now mark certain fields as blocked:");
			read("A blocked field will not count as part of a row for any player.");
			read("Please enter the coordinates of fields you wish to block (x|y) or \"quit\" to continue.");
			read("Enter the same coordinates again to unblock a field again.");
			write("quit");
			read("You may now mark certain fields as joker fields:");
			read("A joker field will count as part of a row for every player.");
			read("Please enter the coordinates of fields you wish to set as joker (x|y) or \"quit\" to continue.");
			read("Enter the same coordinates again to \"un-joker\" a field again.");
			write("quit");
			read("Game started!");
			board = new ArrayBoard(10, 10);
			readBoard(board);
			placeStone(1, 1, 1);
			placeStone(2, 9, 9);
			placeStone(1, 2, 1);
			placeStone(2, 8, 8);
			placeStone(1, 3, 1);
			placeStone(2, 7, 7);
			placeStone(1, 4, 1);
			placeStone(2, 0, 1);
			placeStone(1, 5, 1);
			read("Player 1 won!");
		} catch (Throwable t) {
			Assert.fail("Exception: " + t.toString());
			if (t instanceof ThreadDeath)
				throw (ThreadDeath) t;
		}
	}

	/**
	 * Tests advanced gameplay with blocked fields and jokers. Additionally, some invalid inputs are sent.
	 * <p>
	 * This plays exactly the same game as {@link FixedServerTest#testAdvanced()}.
	 */
	@Test
	public void testAdvanced() {
		try {
			captureIO();
			ConsoleClient.main(new String[] {}); // runs in a new thread
			read("Welcome to Five in a Row!");
			read("Please enter the field size:");
			write("10");
			read("You may now mark certain fields as blocked:");
			read("A blocked field will not count as part of a row for any player.");
			read("Please enter the coordinates of fields you wish to block (x|y) or \"quit\" to continue.");
			read("Enter the same coordinates again to unblock a field again.");
			block(3, 5);
			block(3, 7);
			block(3, 6);
			block(3, 7);
			write("HEYYEYAAEYAAAEYAEYAA"); // weird rubbish
			read("Invalid input. Please re-type the coordinates, but in a different format.");
			write("quit");
			read("You may now mark certain fields as joker fields:");
			read("A joker field will count as part of a row for every player.");
			read("Please enter the coordinates of fields you wish to set as joker (x|y) or \"quit\" to continue.");
			read("Enter the same coordinates again to \"un-joker\" a field again.");
			joker(0, 0);
			joker(1, 7);
			joker(9, 9);
			joker(1, 7);
			write("quit");
			read("Game started!");
			placeStone(1, 1, 1);
			placeStone(2, 8, 8);
			placeStone(3, 0, 9);
			placeStone(1, 2, 2);
			placeStone(2, 7, 7);
			placeStone(3, 1, 8);
			placeStone(1, 3, 3);
			placeStone(2, 6, 6);
			placeStone(3, 4, 4);
			placeStone(1, 2, 7);
			placeStone(2, 5, 5);
			read("Player 2 won!");
		} catch (Throwable t) {
			Assert.fail("Exception: " + t.toString());
			if (t instanceof ThreadDeath)
				throw (ThreadDeath) t;
		}
	}

	/**
	 * Captures <code>System.in</code> and <code>System.out</code>.
	 * 
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	private void captureIO() throws IOException {
		PipedInputStream pOut = new PipedInputStream();
		System.setOut(new PrintStream(new PipedOutputStream(pOut)));
		PipedOutputStream pIn = new PipedOutputStream();
		System.setIn(new PipedInputStream(pIn));
		out = new BufferedReader(new InputStreamReader(pOut));
		in = new BufferedWriter(new OutputStreamWriter(pIn));
	}

	/**
	 * Reads something from the client's output and compares it to the expected output.
	 * 
	 * @param expectedClientOutput
	 *            The expected output.
	 * @throws IOException
	 *             If an I/O eror occurs.
	 */
	private void read(String expectedClientOutput) throws IOException {
		assertEquals(expectedClientOutput, out.readLine());
	}

	/**
	 * Writes the specified input, terminated by a newline character, to the client's in.
	 * 
	 * @param clientInput
	 *            The input to write to the client.
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	private void write(String clientInput) throws IOException {
		in.write(clientInput + '\n');
		in.flush();
	}

	/**
	 * Reads a printed board from the client's output and compares it to the expected board.
	 * 
	 * @param b
	 *            The board that is expected.
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	private void readBoard(Board b) throws IOException {
		StringBuilder line = new StringBuilder(b.getWidth());
		for (int y = 0; y < b.getHeight(); y++) {
			for (int x = 0; x < b.getWidth(); x++)
				switch (b.getPlayerAt(x, y) == null ? 0 : b.getPlayerAt(x, y).getID()) {
				case Joker.ID:
					line.append('J');
					break;
				case Block.ID:
					line.append('B');
					break;
				case 1:
					line.append('X');
					break;
				case 2:
					line.append('O');
					break;
				default:
					line.append('·');
					break;
				}
			read(line.toString());
			line.delete(0, line.length()); // clear the line for re-using
		}
	}

	/**
	 * Reads the "place stone" query, writes the "place stone" action, writes it to the board, and reads the board.
	 * 
	 * @param player
	 *            The player placing the stone.
	 * @param x
	 *            The x-coordinate of the stone to place.
	 * @param y
	 *            The y-coordinate of the stone to place.
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	private void placeStone(int player, int x, int y) throws IOException {
		read("Player " + player + ", where do you want to place your stone?");
		write(x + "|" + y);
		// We don't have access to the player instance, so we use a "Phantom player".
		// This works because two players with the same ID are considered equal.
		board.setPlayerAt(x, y, new Player("Phantom player", Color.black, player));
		readBoard(board);
	}

	/**
	 * Writes the "place stone" action, writes it to the board, and reads the board.
	 * <p>
	 * Note: This method is NOT analogous to {@link #placeStone(int, int, int)}, since this method does <i>not</i> read
	 * the "place stone" query.
	 * 
	 * @param player
	 *            The player that places the stone.
	 * @param x
	 *            The x-coordinate of the stone to place.
	 * @param y
	 *            The y-coordinate of the stone to place.
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	private void placeStone(Player player, int x, int y) throws IOException {
		write(x + "|" + y);
		board.setPlayerAt(x, y, player);
		readBoard(board);
	}

	/**
	 * Blocks or unblocks the specified field.
	 * <p>
	 * More specifically: {@link #placeStone(Player, int, int) Places the stone} and reads the "field blocked/unblocked"
	 * response.
	 * 
	 * @param x
	 *            The x-coordinate of the stone to place.
	 * @param y
	 *            The y-coordinate of the stone to place.
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	private void block(int x, int y) throws IOException {
		boolean block = board.getPlayerAt(x, y).equals(NoPlayer.getInstance());
		placeStone(block ? Block.getInstance() : NoPlayer.getInstance(), x, y);
		read("Field " + x + "|" + y + (block ? " blocked." : " unblocked."));
	}

	/**
	 * Marks or unmarks the specified field as joker field.
	 * <p>
	 * More specifically: {@link #placeStone(Player, int, int) Places the stone} and reads the
	 * "field marked/unmarked as joker" response.
	 * 
	 * @param x
	 *            The x-coordinate of the stone to place.
	 * @param y
	 *            The y-coordinate of the stone to place.
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	private void joker(int x, int y) throws IOException {
		boolean joker = board.getPlayerAt(x, y).equals(NoPlayer.getInstance());
		placeStone(joker ? Joker.getInstance() : NoPlayer.getInstance(), x, y);
		read("Field " + x + "|" + y + (joker ? " marked" : " unmarked") + " as joker field");
	}
}