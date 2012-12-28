package de.lucaswerkmeister.code.fiar.framework.event;

import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * Indicates that a stone was placed by a player.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class PlaceStone extends FieldAction {

	/**
	 * A new {@link PlaceStone} by the specified player on the specified field.
	 * 
	 * @param actingPlayer
	 *            The acting player.
	 * @param x
	 *            The x coordinate of the modified field.
	 * @param y
	 *            The y coordinate of the modified field.
	 */
	public PlaceStone(Player actingPlayer, int x, int y) {
		super(actingPlayer, x, y);
	}
}