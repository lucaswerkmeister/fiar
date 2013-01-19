package de.lucaswerkmeister.code.fiar.clients.swingClient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.lucaswerkmeister.code.fiar.framework.Board;
import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Displays the game to a user and sents user actions to action listeners.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class GameFrame extends JFrame {
	/**
	 * A {@link BoardListener} receives board events from the {@link GameFrame}.
	 * 
	 * @author Lucas Werkmeister
	 * @version 1.0
	 */
	public interface BoardListener {
		/**
		 * Indicates that the user clicked a specific {@link Field}.
		 * 
		 * @param field
		 *            The clicked field.
		 */
		public void fieldClicked(Field field);
	}

	private final JPanel board;
	private final JPanel buttons;
	private final JLabel statusBar;
	private final Field[][] fields;
	private final Set<ActionListener> actionListeners;
	private final Set<BoardListener> boardListeners;
	private static final Dimension FIELD_SIZE = new Dimension(15, 15);

	/**
	 * Creates a new {@link GameFrame} showing the specified board, with the specified window title.
	 * 
	 * @param b
	 *            The board.
	 * @param title
	 *            The title.
	 */
	public GameFrame(Board b, String title) {
		super(title);
		actionListeners = new HashSet<>();
		boardListeners = new HashSet<>();
		final JPanel content = new JPanel(new BorderLayout());
		board = new JPanel();
		board.setLayout(new GridLayout(b.getWidth(), b.getHeight(), 0, 0));
		fields = new Field[b.getWidth()][b.getHeight()];
		for (int x = 0; x < b.getWidth(); x++)
			for (int y = 0; y < b.getHeight(); y++) {
				final Field f = new Field(null, FIELD_SIZE);
				f.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(final MouseEvent e) {
						if (f.isEnabled()) // disabled lightweight components still receive MouseEvents
							fireFieldClicked(f);
					}
				});
				fields[x][y] = f;
				board.add(f);
			}
		content.add(this.board, BorderLayout.CENTER);
		buttons = new JPanel();
		content.add(buttons, BorderLayout.EAST);
		statusBar = new JLabel("Ready");
		content.add(statusBar, BorderLayout.SOUTH);
		setContentPane(content);
	}

	/**
	 * Sets the buttons that are currently visible to the user.
	 * 
	 * @param labels
	 *            The labels of the currently visible buttons.
	 */
	public void setButtons(String[] labels) {
		boolean[] enabled = new boolean[labels.length];
		Arrays.fill(enabled, true);
		setButtons(labels, enabled);
	}

	/**
	 * Sets the buttons that are currently visible to the user, allowing the caller to specify which of the buttons are
	 * enabled.
	 * 
	 * @param labels
	 *            The labels of the currently visible buttons.
	 * @param enabled
	 *            The <code>i</code><sup>th</sup> button is enabled if and only if <code>enabled[i] == true</code>.
	 * @throws ArrayIndexOutOfBoundsException
	 *             if <code>labels.length != enabled.length</code>. In this case, the {@link GameFrame} will not display
	 *             any buttons.
	 */
	public void setButtons(String[] labels, boolean[] enabled) throws ArrayIndexOutOfBoundsException {
		buttons.removeAll();
		if (labels.length != enabled.length)
			throw new ArrayIndexOutOfBoundsException("labels.length must be equal to enabled.length (" + labels.length
					+ "!=" + enabled.length + ")");
		buttons.setLayout(new GridLayout(labels.length, 1));
		for (int i = 0; i < labels.length; i++) {
			String label = labels[i];
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING));
			JButton b = new JButton(label);
			b.setEnabled(enabled[i]);
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fireActionEvent(e);
				}
			});
			p.add(b);
			buttons.add(p);
		}
		pack();
	}

	/**
	 * Sets the text of the status bar.
	 * 
	 * @param status
	 *            The status text.
	 */
	public void setStatus(String status) {
		statusBar.setText(status);
	}

	/**
	 * Sets the player at the specified field.
	 * 
	 * @param x
	 *            The x coordinate of the field.
	 * @param y
	 *            The y coordinate of the field.
	 * @param p
	 *            The player.
	 */
	public void setPlayerAt(int x, int y, Player p) {
		fields[x][y].setPlayer(p);
	}

	/**
	 * Adds an action listener to this {@link GameFrame}.
	 * <p>
	 * The action listener is notified whenever any of the buttons is clicked, with an {@link ActionEvent} where the
	 * {@link ActionEvent#getActionCommand() action command} is the label of the button clicked.
	 * <p>
	 * The action listener is <i>not</i> notified of clicks on the game board.
	 * 
	 * @param l
	 *            The action listener.
	 * @see {@link #setButtons(String[])}, {@link #setButtons(String[], boolean[])},
	 *      {@link #removeActionListener(ActionListener)}, {@link #addBoardListener(BoardListener)}
	 */
	public void addActionListener(ActionListener l) {
		actionListeners.add(l);
	}

	/**
	 * Removes an action listener from this {@link GameFrame}.
	 * 
	 * @param l
	 *            The action listener.
	 * @see {@link #addActionListener(ActionListener)}
	 */
	public void removeActionListener(ActionListener l) {
		actionListeners.remove(l);
	}

	/**
	 * Adds a board listener to this {@link GameFrame}.
	 * <p>
	 * The board listener is notified of clicks on the game board.
	 * 
	 * @param l
	 *            The board listener.
	 * @ee {@link #removeBoardListener(BoardListener)}
	 */
	public void addBoardListener(BoardListener l) {
		boardListeners.add(l);
	}

	/**
	 * Removes a board listener from this {@link GameFrame}.
	 * 
	 * @param l
	 *            The action listener.
	 * @see {@link #addBoardListener(BoardListener)}
	 */
	public void removeBoardListener(BoardListener l) {
		boardListeners.remove(l);
	}

	/**
	 * Private utility method. Propagates one {@link ActionEvent} to all registered {@link ActionListener}s.
	 * 
	 * @param e
	 *            The action event.
	 */
	private void fireActionEvent(ActionEvent e) {
		for (ActionListener l : actionListeners)
			l.actionPerformed(e);
	}

	/**
	 * Private utility method. Propagates one {@link BoardListener#fieldClicked(Field) fieldClicked} event to all
	 * registered {@link BoardListener}s.
	 * 
	 * @param f
	 *            The field.
	 */
	private void fireFieldClicked(Field f) {
		for (BoardListener l : boardListeners)
			l.fieldClicked(f);
	}
}