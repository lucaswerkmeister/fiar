import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.Queue;

import junit.framework.Assert;

import org.junit.Test;

import de.lucaswerkmeister.code.fiar.framework.Block;
import de.lucaswerkmeister.code.fiar.framework.Board;
import de.lucaswerkmeister.code.fiar.framework.Client;
import de.lucaswerkmeister.code.fiar.framework.Joker;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.Server;
import de.lucaswerkmeister.code.fiar.framework.event.BlockDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.BlockField;
import de.lucaswerkmeister.code.fiar.framework.event.BoardSizeProposal;
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
import de.lucaswerkmeister.code.fiar.servers.FixedServer;

/**
 * The tests in this class test the {@link FixedServer}.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class FixedServerTest extends Client {
	private Queue<GameEvent> events;

	// Eclipse formatter is stupid and inserts line breaks after every td :(
	/**
	 * Tests basic gameplay of the server: No blocking and jokers, only two players, no illegal action attempts.
	 * <p>
	 * After the game has finished, the board looks like this:<br>
	 * <table border="1">
	 * <colgroup width="25px" span="10"/>
	 * <tr style="height:25px;">
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * </tr>
	 * <tr style="height:25px;">
	 * <td>2</td>
	 * <td>1</td>
	 * <td>1</td>
	 * <td>1</td>
	 * <td>1</td>
	 * <td>1</td>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * </tr>
	 * <tr style="height:25px;">
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * </tr>
	 * <tr style="height:25px;">
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * </tr>
	 * <tr style="height:25px;">
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * </tr>
	 * <tr style="height:25px;">
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * </tr>
	 * <tr style="height:25px;">
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * </tr>
	 * <tr style="height:25px;">
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td>2</td>
	 * <td/>
	 * <td/>
	 * </tr>
	 * <tr style="height:25px;">
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td>2</td>
	 * <td/>
	 * </tr>
	 * <tr style="height:25px;">
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td/>
	 * <td>2</td>
	 * </tr>
	 * </table>
	 */
	@Test
	public void testBasic() {
		try {
			events = new LinkedList<>();
			Player p1 = new Player("Player 1", Color.blue, 1);
			Player p2 = new Player("Player 2", Color.cyan, 2);
			Server server = new FixedServer(new Client[] {this }, new Player[][] {new Player[] {p1, p2 } });

			// Init: Board Size, Block Distribution, Joker Distribution
			act(server, new BoardSizeProposal(p1, new Dimension(10, 10)));
			act(server, new BoardSizeProposal(p2, new Dimension(10, 10)));
			Assert.assertTrue(PhaseChange.class.isInstance(events.poll()));
			act(server, new BlockDistributionAccepted(p1, server.getCurrentBoard(this)));
			act(server, new BlockDistributionAccepted(p2, server.getCurrentBoard(this)));
			Assert.assertTrue(PhaseChange.class.isInstance(events.poll()));
			act(server, new JokerDistributionAccepted(p1, server.getCurrentBoard(this)));
			act(server, new JokerDistributionAccepted(p2, server.getCurrentBoard(this)));
			Assert.assertTrue(PhaseChange.class.isInstance(events.poll()));

			// Game
			act(server, new PlaceStone(p1, 1, 1));
			act(server, new PlaceStone(p2, 9, 9));
			act(server, new PlaceStone(p1, 2, 1));
			act(server, new PlaceStone(p2, 8, 8));
			act(server, new PlaceStone(p1, 3, 1));
			act(server, new PlaceStone(p2, 7, 7));
			act(server, new PlaceStone(p1, 4, 1));
			act(server, new PlaceStone(p2, 0, 1));
			act(server, new PlaceStone(p1, 5, 1));

			// Victory
			Assert.assertTrue(PlayerVictory.class.isInstance(events.peek()));
			Assert.assertEquals(p1, ((PlayerVictory) events.poll()).getWinningPlayer());
			Assert.assertNull(events.poll());
		} catch (Throwable t) {
			fail("Exception: " + t.toString());
			if (t instanceof ThreadDeath)
				throw (ThreadDeath) t;
		}
	}

	/**
	 * Tests advanced gameplay with blocked fields, jokers and three players. No illegal action attempts are made.
	 */
	@Test
	public void testAdvanced() {
		try {
			events = new LinkedList<>();
			Player p1 = new Player("Player 1", Color.blue, 1);
			Player p2 = new Player("Player 2", Color.cyan, 2);
			Player p3 = new Player("Player 3", Color.pink, 3);
			Server server = new FixedServer(new Client[] {this }, new Player[][] {new Player[] {p1, p2, p3 } });

			// Board size
			act(server, new BoardSizeProposal(p1, new Dimension(10, 10)));
			act(server, new BoardSizeProposal(p2, new Dimension(10, 10)));
			act(server, new BoardSizeProposal(p3, new Dimension(10, 10)));
			Assert.assertTrue(PhaseChange.class.isInstance(events.poll()));

			// Block (3,5) and (3,6)
			// To confuse the server, we accept the block distribution before we set the blocks.
			// The server should be able to handle this.
			Board targetBoard = server.getCurrentBoard(this);
			targetBoard.setPlayerAt(3, 5, Block.getInstance());
			targetBoard.setPlayerAt(3, 6, Block.getInstance());
			act(server, new BlockDistributionAccepted(p1, targetBoard));
			act(server, new BlockDistributionAccepted(p2, targetBoard));
			act(server, new BlockDistributionAccepted(p3, targetBoard));
			// Now set the blocks. We add and remove another block in between.
			act(server, new BlockField(p1, 3, 5));
			act(server, new BlockField(p1, 3, 7));
			act(server, new BlockField(p1, 3, 6));
			act(server, new UnblockField(p1, 3, 7));
			Assert.assertTrue(PhaseChange.class.isInstance(events.poll()));

			// Set (0,0) and (9,9) as joker fields. This is completely analogous to the blocking procedure.
			targetBoard.setPlayerAt(0, 0, Joker.getInstance());
			targetBoard.setPlayerAt(9, 9, Joker.getInstance());
			act(server, new JokerDistributionAccepted(p1, targetBoard));
			act(server, new JokerDistributionAccepted(p2, targetBoard));
			act(server, new JokerDistributionAccepted(p3, targetBoard));
			act(server, new JokerField(p1, 0, 0));
			act(server, new JokerField(p1, 1, 7));
			act(server, new JokerField(p1, 9, 9));
			act(server, new UnjokerField(p1, 1, 7));
			Assert.assertTrue(PhaseChange.class.isInstance(events.poll()));

			// Game
			act(server, new PlaceStone(p1, 1, 1));
			act(server, new PlaceStone(p2, 8, 8));
			act(server, new PlaceStone(p3, 0, 9));
			act(server, new PlaceStone(p1, 2, 2));
			act(server, new PlaceStone(p2, 7, 7));
			act(server, new PlaceStone(p3, 1, 8));
			act(server, new PlaceStone(p1, 3, 3));
			act(server, new PlaceStone(p2, 6, 6));
			act(server, new PlaceStone(p3, 4, 4));
			act(server, new PlaceStone(p1, 2, 7));
			act(server, new PlaceStone(p2, 5, 5));

			// Victory
			Assert.assertTrue(PlayerVictory.class.isInstance(events.peek()));
			Assert.assertEquals(p2, ((PlayerVictory) events.poll()).getWinningPlayer());
			Assert.assertNull(events.poll());
		} catch (Throwable t) {
			fail("Exception: " + t.toString());
			if (t instanceof ThreadDeath)
				throw (ThreadDeath) t;
		}
	}

	/**
	 * Combines sending an action to the player and checking if the server re-sent it.
	 * 
	 * @param server
	 *            The server.
	 * @param action
	 *            The action.
	 * @throws IllegalStateException
	 *             Thrown by the server.
	 * @throws IllegalMoveException
	 *             Thrown by the server.
	 */
	private void act(Server server, PlayerAction action) throws IllegalStateException, IllegalMoveException {
		server.action(this, action);
		Assert.assertEquals(action, events.poll());
	}

	@Override
	public void gameEvent(GameEvent e) {
		events.add(e);
	}

	@Override
	public int getID() {
		return 0;
	}
}