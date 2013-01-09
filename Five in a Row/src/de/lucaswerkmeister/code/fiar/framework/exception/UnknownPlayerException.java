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

import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.Server;

/**
 * A {@link UnknownPlayerException} should be thrown by a {@link Server} method to indicate that the server does not
 * recognize the {@link Player}.
 * <p>
 * Note that the server is not required to throw this exception when encountering an unknown player; it is only
 * specified that <i>if</i> the server doesn't want to answer the request, it should throw this exception. (For example,
 * a server might adopt new players on-the-fly.)
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class UnknownPlayerException extends RuntimeException {
	private static final long serialVersionUID = -3122140481713036479L;
	private final Player unknownPlayer;

	/**
	 * A new {@link UnknownPlayerException} with the specified unknown player.
	 * 
	 * @param unknownPlayer
	 *            The unknown player.
	 */
	public UnknownPlayerException(final Player unknownPlayer) {
		super();
		this.unknownPlayer = unknownPlayer;
	}

	/**
	 * A new {@link UnknownPlayerException} with the specified unknown player and message.
	 * 
	 * @param unknownPlayer
	 *            The unknown player.
	 * @param message
	 *            The message.
	 */
	public UnknownPlayerException(final Player unknownPlayer, final String message) {
		super(message);
		this.unknownPlayer = unknownPlayer;
	}

	/**
	 * A new {@link UnknownPlayerException} with the specified unknown player and cause.
	 * 
	 * @param unknownPlayer
	 *            The unknown player.
	 * @param cause
	 *            The cause.
	 */
	public UnknownPlayerException(final Player unknownPlayer, final Throwable cause) {
		super(cause);
		this.unknownPlayer = unknownPlayer;
	}

	/**
	 * A new {@link UnknownPlayerException} with the specified unknown player, message and cause.
	 * 
	 * @param unknownPlayer
	 *            The unknown player.
	 * @param message
	 *            The message.
	 * @param cause
	 *            The cause.
	 */
	public UnknownPlayerException(final Player unknownPlayer, final String message, final Throwable cause) {
		super(message, cause);
		this.unknownPlayer = unknownPlayer;
	}

	/**
	 * A new {@link UnknownPlayerException} with the specified unknown player, message, cause, enableSuppression and
	 * writableStackTrace.
	 * 
	 * @param unknownPlayer
	 *            The unknown player.
	 * @param message
	 *            The message.
	 * @param cause
	 *            The cause.
	 * @param enableSuppression
	 *            Whether to enable or disable suppression.
	 * @param writableStackTrace
	 *            Whether or not the stack trace should be writable.
	 */
	public UnknownPlayerException(final Player unknownPlayer, final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.unknownPlayer = unknownPlayer;
	}

	/**
	 * Gets the unknown player.
	 * 
	 * @return The unknown player.
	 */
	public Player getUnknownPlayer() {
		return unknownPlayer;
	}
}
