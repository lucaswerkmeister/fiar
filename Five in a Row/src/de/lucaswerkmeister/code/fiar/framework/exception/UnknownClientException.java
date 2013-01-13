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
package de.lucaswerkmeister.code.fiar.framework.exception;

import de.lucaswerkmeister.code.fiar.framework.Client;
import de.lucaswerkmeister.code.fiar.framework.Server;

/**
 * A {@link UnknownClientException} should be thrown by a {@link Server} method to indicate that the server does not
 * recognize the {@link Client}.
 * <p>
 * Note that the server is not required to throw this exception when encountering an unknown client; it is only
 * specified that <i>if</i> the server doesn't want to answer the request, it should throw this exception. (For example,
 * a server might allow anyone to poll the current phase, or adopt new clients on-the-fly.)
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
	public UnknownClientException(final Client unknownClient) {
		super("Unknown client!");
		this.unknownClient = unknownClient;
	}

	/**
	 * A new {@link UnknownClientException} with the specified unknown client and message.
	 * 
	 * @param unknownClient
	 *            The unknown client.
	 * @param message
	 *            The message.
	 */
	public UnknownClientException(final Client unknownClient, final String message) {
		super(message);
		this.unknownClient = unknownClient;
	}

	/**
	 * A new {@link UnknownClientException} with the specified unknown client and cause.
	 * 
	 * @param unknownClient
	 *            The unknown client.
	 * @param cause
	 *            The cause.
	 */
	public UnknownClientException(final Client unknownClient, final Throwable cause) {
		super("Unknown client!", cause);
		this.unknownClient = unknownClient;
	}

	/**
	 * A new {@link UnknownClientException} with the specified unknown client, message and cause.
	 * 
	 * @param unknownClient
	 *            The unknown client.
	 * @param message
	 *            The message.
	 * @param cause
	 *            The cause.
	 */
	public UnknownClientException(final Client unknownClient, final String message, final Throwable cause) {
		super(message, cause);
		this.unknownClient = unknownClient;
	}

	/**
	 * A new {@link UnknownClientException} with the specified unknown client, message, cause, enableSuppression and
	 * writableStackTrace.
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
	public UnknownClientException(final Client unknownClient, final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
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