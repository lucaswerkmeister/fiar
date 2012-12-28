package de.lucaswerkmeister.code.fiar.framework.event;

import java.awt.Point;

import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Represents an action on one field of the board.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public abstract class FieldAction extends PlayerAction {
	private final int x, y;

	/**
	 * A new {@link FieldAction} by the specified player on the specified field.
	 * 
	 * @param actingPlayer
	 *            The acting player.
	 * @param x
	 *            The x coordinate of the modified field.
	 * @param y
	 *            The y coordinate of the modified field.
	 */
	public FieldAction(Player actingPlayer, int x, int y) {
		super(actingPlayer);
		this.x = x;
		this.y = y;
	}

	/**
	 * Gets the field that was acted upon.
	 * 
	 * @return The field.
	 */
	public final Point getField() {
		return new Point(x, y);
	}
}