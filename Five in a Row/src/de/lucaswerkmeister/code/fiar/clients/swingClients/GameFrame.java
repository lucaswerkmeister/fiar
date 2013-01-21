package de.lucaswerkmeister.code.fiar.clients.swingClients;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import de.lucaswerkmeister.code.fiar.framework.Board;
import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Displays the game to a user and sents user actions to action listeners.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class GameFrame extends JFrame {
	private static final long serialVersionUID = -8649428295633759928L;

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
	private static final Random random = new Random();
	private static final Dimension FIELD_SIZE = new Dimension(15, 15);
	private static JDialog addPlayerDialog;

	/**
	 * Creates a new {@link GameFrame} showing the specified board, with the specified window title.
	 * 
	 * @param b
	 *            The board.
	 * @param title
	 *            The title.
	 */
	public GameFrame(final Board b, final String title) {
		super(title);
		actionListeners = new HashSet<>();
		boardListeners = new HashSet<>();
		final JPanel content = new JPanel(new BorderLayout());
		board = new JPanel();
		board.setLayout(new GridLayout(b.getWidth(), b.getHeight(), 0, 0));
		fields = new Field[b.getWidth()][b.getHeight()];
		for (int x = 0; x < b.getWidth(); x++)
			for (int y = 0; y < b.getHeight(); y++) {
				final Field f = new Field(null, FIELD_SIZE, new Point(x, y));
				f.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(final MouseEvent e) {
						if (f.isEnabled() && isEnabled()) // disabled lightweight components still receive MouseEvents
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
	public void setButtons(final String[] labels) {
		final boolean[] enabled = new boolean[labels.length];
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
	public void setButtons(final String[] labels, final boolean[] enabled) throws ArrayIndexOutOfBoundsException {
		buttons.removeAll();
		if (labels.length != enabled.length)
			throw new ArrayIndexOutOfBoundsException("labels.length must be equal to enabled.length (" + labels.length
					+ "!=" + enabled.length + ")");
		buttons.setLayout(new GridLayout(labels.length, 1));
		for (int i = 0; i < labels.length; i++) {
			final String label = labels[i];
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING));
			final JButton b = new JButton(label);
			b.setEnabled(enabled[i]);
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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
	public void setStatus(final String status) {
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
	public void setPlayerAt(final int x, final int y, final Player p) {
		fields[x][y].setPlayer(p);
	}

	/**
	 * Sets the player at the specified field.
	 * 
	 * @param xy
	 *            The coordinates of the field.
	 * @param p
	 *            The player.
	 */
	public void setPlayerAt(final Point xy, final Player p) {
		setPlayerAt(xy.x, xy.y, p);
	}

	/**
	 * Enables or disables the specified field.
	 * 
	 * @param x
	 *            The x coordinate of the field.
	 * @param y
	 *            The y coordinate of the field.
	 * @param enabled
	 *            If the field is enabled or not.
	 */
	public void setEnabled(final int x, final int y, final boolean enabled) {
		fields[x][y].setEnabled(enabled);
	}

	/**
	 * Enables or disables the specified field.
	 * 
	 * @param xy
	 *            The coordinates of the field.
	 * @param enabled
	 *            If the field is enabled or not.
	 */
	public void setEnabled(final Point xy, final boolean enabled) {
		setEnabled(xy.x, xy.y, enabled);
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
	public void addActionListener(final ActionListener l) {
		actionListeners.add(l);
	}

	/**
	 * Removes an action listener from this {@link GameFrame}.
	 * 
	 * @param l
	 *            The action listener.
	 * @see {@link #addActionListener(ActionListener)}
	 */
	public void removeActionListener(final ActionListener l) {
		actionListeners.remove(l);
	}

	/**
	 * Removes all registered {@link ActionListener}s.
	 */
	public void removeAllActionListeners() {
		for (final ActionListener l : actionListeners)
			removeActionListener(l);
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
	public void addBoardListener(final BoardListener l) {
		boardListeners.add(l);
	}

	/**
	 * Removes a board listener from this {@link GameFrame}.
	 * 
	 * @param l
	 *            The action listener.
	 * @see {@link #addBoardListener(BoardListener)}
	 */
	public void removeBoardListener(final BoardListener l) {
		boardListeners.remove(l);
	}

	/**
	 * Removes all registered {@link BoardListener}s.
	 */
	public void removeAllBoardListeners() {
		for (final BoardListener l : boardListeners)
			removeBoardListener(l);
	}

	/**
	 * Private utility method. Propagates one {@link ActionEvent} to all registered {@link ActionListener}s.
	 * 
	 * @param e
	 *            The action event.
	 */
	private void fireActionEvent(final ActionEvent e) {
		for (final ActionListener l : actionListeners)
			l.actionPerformed(e);
	}

	/**
	 * Private utility method. Propagates one {@link BoardListener#fieldClicked(Field) fieldClicked} event to all
	 * registered {@link BoardListener}s.
	 * 
	 * @param f
	 *            The field.
	 */
	private void fireFieldClicked(final Field f) {
		for (final BoardListener l : boardListeners)
			l.fieldClicked(f);
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
	static boolean endsWithSSound(final String token) {
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
		while (true)
			switch (addPlayerDialog.getName()) {
			case "Add Player":
				// This Easter Egg is clearly of the "WTF" type.
				return new Player(name.getText().equals("All your base are belong to us") ? "CATS" : name.getText(),
						color.getColor(), id);
			case "Stop adding players":
				return null;
			default:
				if (addPlayerDialog.getName().startsWith("Reload dialog:")) {
					final boolean needToReplaceName = name.getText().equals("Player " + id);
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

	/**
	 * Reloads the Add Player dialog to return a player with the specified ID.
	 * <p>
	 * This method is intended to be used when the ID the Add Player dialog is offering was taken by a remote player in
	 * the meantime.
	 * 
	 * @param id
	 *            The new ID for the player.
	 */
	public static void reshowAddPlayerDialog(final int id) {
		if (addPlayerDialog != null && addPlayerDialog.isVisible()) {
			addPlayerDialog.setName("Reload dialog:" + id);
			addPlayerDialog.setVisible(false);
		}
	}

	/**
	 * Hides the Add Player dialog.
	 */
	public static void hideAddPlayerDialog() {
		if (addPlayerDialog != null && addPlayerDialog.isVisible()) {
			addPlayerDialog.setName("Stop adding players");
			addPlayerDialog.setVisible(false);
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
}