package de.lucaswerkmeister.code.fiar.clients.swingClient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.lucaswerkmeister.code.fiar.framework.Board;

public class GameFrame extends JFrame {
	public interface BoardListener {
		public void fieldClicked(Field field);
	}

	private final JPanel board;
	private final JPanel buttons;
	private final JLabel statusBar;
	private final Field[][] fields;
	private final Set<ActionListener> actionListeners;
	private final Set<BoardListener> boardListeners;
	private static final Dimension FIELD_SIZE = new Dimension(15, 15);

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

	public void setButtons(String[] labels) {
		buttons.removeAll();
		buttons.setLayout(new GridLayout(labels.length, 1));
		for (String label : labels) {
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING));
			JButton b = new JButton(label);
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fireActionEvent(e);
				}
			});
			p.add(b);
			buttons.add(p);
		}
	}

	public void setStatus(String status) {
		statusBar.setText(status);
	}

	private void fireActionEvent(ActionEvent e) {
		for (ActionListener l : actionListeners)
			l.actionPerformed(e);
	}

	private void fireFieldClicked(Field f) {
		for (BoardListener l : boardListeners)
			l.fieldClicked(f);
	}
}