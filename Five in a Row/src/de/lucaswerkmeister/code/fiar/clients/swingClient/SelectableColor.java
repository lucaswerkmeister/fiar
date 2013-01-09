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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * A swing component that displays a color. When clicked, it opens a dialog with a {@link JColorChooser} where the user
 * can select a color.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class SelectableColor extends JComponent implements MouseListener {
	private static final long serialVersionUID = 8280662059844997029L;
	private static final Dimension size = new Dimension(15, 15);
	private Color color = Color.black;
	private final List<ActionListener> listeners = new LinkedList<>();

	/**
	 * Creates a new {@link SelectableColor} with the specified color as initial color.
	 * 
	 * @param c
	 *            The initial color.
	 */
	public SelectableColor(final Color c) {
		color = c;
		addMouseListener(this);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
	}

	@Override
	public void paint(final Graphics g) {
		g.setColor(Color.black);
		g.drawRect(0, 0, size.width, size.height);
		g.setColor(color);
		g.fillRect(1, 1, size.width - 1, size.height - 1);
	}

	/**
	 * Sets the color.
	 * 
	 * @param c
	 *            The color to set.
	 */
	public void setColor(final Color c) {
		color = c;
		repaint();
	}

	/**
	 * Gets the color.
	 * 
	 * @return The color.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Adds an action listener to this component.
	 * <p>
	 * The action listener is notified whenever the user closes the "select color" dialog, regardless of the result (OK
	 * or Cancel).
	 * 
	 * @param listener
	 *            The action listener for this component.
	 */
	public void addActionListener(final ActionListener listener) {
		listeners.add(listener);
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		Container parent = getParent();
		while (!(parent instanceof Frame))
			parent = parent.getParent();
		final ColorDialog d = new ColorDialog((Frame) parent, color);
		d.setAlwaysOnTop(true);
		d.setVisible(true);
		color = d.getColor();
		repaint();
		if (!listeners.isEmpty()) {
			final ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Color changed");
			for (final ActionListener l : listeners)
				l.actionPerformed(ae);
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {

	}

	@Override
	public void mouseReleased(final MouseEvent e) {

	}

	@Override
	public void mouseEntered(final MouseEvent e) {

	}

	@Override
	public void mouseExited(final MouseEvent e) {

	}
}

/**
 * The dialog that pops up when the user clicks on a {@link SelectableColor}.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
class ColorDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 4347923773976010884L;
	private boolean okClicked = false;
	private final Color startColor;
	private final JColorChooser chooser;

	/**
	 * Creates a new {@link ColorDialog} with the specified owner and default color.
	 * 
	 * @param owner
	 *            The dialog owner.
	 * @param c
	 *            The default color.
	 */
	public ColorDialog(final Frame owner, final Color c) {
		super(owner, "Choose color", true);
		startColor = c;
		setLayout(new BorderLayout());
		chooser = new JColorChooser();
		chooser.setColor(c);
		add(chooser, BorderLayout.CENTER);
		final JPanel south = new JPanel();
		final JButton ok = new JButton("OK");
		ok.addActionListener(this);
		south.add(ok);
		final JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		south.add(cancel);
		add(south, BorderLayout.SOUTH);
		pack();
		invalidate();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		okClicked = e.getActionCommand().equals("OK");
		dispose();
	}

	/**
	 * Gets the user-selected color.
	 * <p>
	 * If the user canceled, then this is the startup color.
	 * 
	 * @return The color.
	 */
	public Color getColor() {
		if (okClicked)
			return chooser.getColor();
		else
			return startColor;
	}
}
