package de.lucaswerkmeister.code.fiar.framework;

/**
 * Provides access to the game board.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public interface Board {
	/**
	 * Gets the player that occupies the field at (x,y), or <code>null</code> if
	 * that field is still free.
	 * 
	 * @param x
	 *            The x coordinate of the field.
	 * @param y
	 *            The y coordinate of the field.
	 * @return The player at this position, or <code>null</code>.
	 */
	public Player getPlayerAt(int x, int y);

	/**
	 * Gets the width of the board.
	 * 
	 * @return The width of the board.
	 */
	public int getWidth();

	/**
	 * Gets the height of the board.
	 * 
	 * @return The height of the board.
	 */
	public int getHeight();
}