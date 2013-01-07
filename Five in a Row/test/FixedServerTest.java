import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.Queue;

import junit.framework.Assert;

import org.junit.Test;

import de.lucaswerkmeister.code.fiar.framework.Client;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.Server;
import de.lucaswerkmeister.code.fiar.framework.event.BlockDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.BoardSizeProposal;
import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.JokerDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.PhaseChange;
import de.lucaswerkmeister.code.fiar.framework.event.PlaceStone;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerAction;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerVictory;
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

	/**
	 * Tests basic gameplay of the server: No blocking and jokers, only two players, no illegal action attempts.
	 */
	@Test
	public void testBasic() {
		try {
			events = new LinkedList<>();
			Player p1 = new Player("Player 1", Color.blue, 1);
			Player p2 = new Player("Player 2", Color.cyan, 2);
			FixedServer server = new FixedServer(new Client[] {this }, new Player[][] {new Player[] {p1, p2 } });

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
			Assert.assertTrue(PlayerVictory.class.isInstance(events.peek()));
			Assert.assertEquals(p1, ((PlayerVictory) events.peek()).getWinningPlayer());
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