package de.lucaswerkmeister.code.fiar.framework.event;

import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Indicates that a player marked a field as Joker field. Only allowed during
 * "Set joker fields" phase.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class JokerField extends FieldAction {

	/**
	 * A new {@link JokerField} by the specified player on the specified field.
	 * 
	 * @param actingPlayer
	 *            The acting player.
	 * @param x
	 *            The x coordinate of the modified field.
	 * @param y
	 *            The y coordinate of the modified field.
	 */
	public JokerField(Player actingPlayer, int x, int y) {
		super(actingPlayer, x, y);
	}
}