package de.lucaswerkmeister.code.fiar.framework.event;

import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Indicates that a player unmarked a field as Joker field. Only allowed during
 * "set joker fields" phase.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class UnjokerField extends FieldAction {

	/**
	 * A new {@link UnjokerField} by the specified player on the specified
	 * field.
	 * 
	 * @param actingPlayer
	 *            The acting player.
	 * @param x
	 *            The x coordinate of the modified field.
	 * @param y
	 *            The y coordinate of the modified field.
	 */
	public UnjokerField(Player actingPlayer, int x, int y) {
		super(actingPlayer, x, y);
	}
}