package de.lucaswerkmeister.code.fiar.framework;

/**
 * A {@link UnknownClientException} should be thrown by a {@link Server} method
 * to indicate that the server does not recognize the {@link Client}.
 * <p>
 * Note that the {@link Server} is not required to throw this exception when
 * encountering an unknown client; it is only specified that <i>if</i> the
 * server doesn't want to answer the request, it should throw this exception.
 * (For example, a server might allow anyone to poll the current phase, or adopt
 * new Clients on-the-fly.)
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class UnknownClientException extends RuntimeException {

	private static final long serialVersionUID = 8896967121002150097L;
	private final Client unknownClient;

	/**
	 * A new {@link UnknownClientException} with the specified unknown client.
	 * 
	 * @param unknownClient
	 *            The unknown client.
	 */
	public UnknownClientException(Client unknownClient) {
		super();
		this.unknownClient = unknownClient;
	}

	/**
	 * A new {@link UnknownClientException} with the specified unknown client
	 * and message.
	 * 
	 * @param unknownClient
	 *            The unknown client.
	 * @param message
	 *            The message.
	 */
	public UnknownClientException(Client unknownClient, String message) {
		super(message);
		this.unknownClient = unknownClient;
	}

	/**
	 * A new {@link UnknownClientException} with the specified unknown client
	 * and cause.
	 * 
	 * @param unknownClient
	 *            The unknown client.
	 * @param cause
	 *            The cause.
	 */
	public UnknownClientException(Client unknownClient, Throwable cause) {
		super(cause);
		this.unknownClient = unknownClient;
	}

	/**
	 * A new {@link UnknownClientException} with the specified unknown client,
	 * message and cause.
	 * 
	 * @param unknownClient
	 *            The unknown client.
	 * @param message
	 *            The message.
	 * @param cause
	 *            The cause.
	 */
	public UnknownClientException(Client unknownClient, String message,
			Throwable cause) {
		super(message, cause);
		this.unknownClient = unknownClient;
	}

	/**
	 * A new {@link UnknownClientException} with the specified unknown client,
	 * message, cause, enableSuppression and writableStackTrace.
	 * 
	 * @param unknownClient
	 *            The unknown client.
	 * @param message
	 *            The message.
	 * @param cause
	 *            The cause.
	 * @param enableSuppression
	 *            Whether to enable or disable suppression.
	 * @param writableStackTrace
	 *            Whether or not the stack trace should be writable.
	 */
	public UnknownClientException(Client unknownClient, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.unknownClient = unknownClient;
	}

	/**
	 * Gets the unknown client.
	 * 
	 * @return The unknown client.
	 */
	public Client getUnknownClient() {
		return unknownClient;
	}
}