package de.lucaswerkmeister.code.fiar.framework.exception;

/**
 * This exception should be thrown by server methods to indicate that a move made by a player is illegal - e.&thinsp;g.
 * he tried to place a stone on a field that is already occupied.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class IllegalMoveException extends Exception {
	private static final long serialVersionUID = -5457099503905855781L;

	/**
	 * A new {@link IllegalMoveException}.
	 */
	public IllegalMoveException() {
		super();
	}

	/**
	 * A new {@link IllegalMoveException} with the specified message.
	 * 
	 * @param message
	 *            The message.
	 */
	public IllegalMoveException(final String message) {
		super(message);
	}
}