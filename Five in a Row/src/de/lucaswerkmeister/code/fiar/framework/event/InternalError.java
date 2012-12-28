package de.lucaswerkmeister.code.fiar.framework.event;

/**
 * Indicates that the game ended due to an internal error.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class InternalError extends GameEnd {

	private final Throwable cause;
	private final String message;

	/**
	 * A new {@link InternalError}.
	 */
	public InternalError() {
		this(null, "Unknown internal error");
	}

	/**
	 * A new {@link InternalError} with the specified cause.
	 * 
	 * @param cause
	 *            The cause of the internal error.
	 */
	public InternalError(Throwable cause) {
		this(cause, "Internal error: " + cause.getMessage());
	}

	/**
	 * A new {@link InternalError} with the specified message.
	 * 
	 * @param message
	 *            The message.
	 */
	public InternalError(String message) {
		this(null, message);
	}

	/**
	 * A new {@link InternalError} with the specified cause and message.
	 * 
	 * @param cause
	 *            The cause of the internal error.
	 * @param message
	 *            The message.
	 */
	public InternalError(Throwable cause, String message) {
		this.cause = cause;
		this.message = message;
	}

	/**
	 * Gets the cause of the internal error.
	 * 
	 * @return The cause of the internal error.
	 */
	public Throwable getCause() {
		return cause;
	}

	/**
	 * Gets the message of the internal error.
	 * 
	 * @return The message of the internal error.
	 */
	public String getMessage() {
		return message;
	}
}