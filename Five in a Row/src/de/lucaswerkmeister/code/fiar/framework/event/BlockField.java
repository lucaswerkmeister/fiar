package de.lucaswerkmeister.code.fiar.framework.event;

import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Indicates that a player has marked a field as blocked. Only allowed during
 * "Block fields" phase.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class BlockField extends FieldAction {

	/**
	 * A new {@link BlockField} by the specified player on the specified field.
	 * 
	 * @param actingPlayer
	 *            The acting player.
	 * @param x
	 *            The x coordinate of the modified field.
	 * @param y
	 *            The y coordinate of the modified field.
	 */
	public BlockField(Player actingPlayer, int x, int y) {
		super(actingPlayer, x, y);
	}
}