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
package de.lucaswerkmeister.code.fiar.defaultServer;

import de.lucaswerkmeister.code.fiar.framework.Board;
import de.lucaswerkmeister.code.fiar.framework.Player;

/**
 * An implementation of the {@link Board} interface that uses an array for
 * internal representation of the board.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class ArrayBoard implements Board {
	private final Player[][] board;

	/**
	 * Creates a new {@link ArrayBoard} with the specified dimensions.
	 * 
	 * @param width
	 *            The width of the new ArrayBoard.
	 * @param height
	 *            The height of the new ArrayBoard.
	 */
	public ArrayBoard(int width, int height) {
		board = new Player[width][height];
	}

	@Override
	public Player getPlayerAt(int x, int y) {
		return board[x][y];
	}

	@Override
	public void setPlayerAt(int x, int y, Player p) {
		board[x][y] = p;
	}

	@Override
	public int getWidth() {
		return board.length;
	}

	@Override
	public int getHeight() {
		return board[0].length;
	}

	@Override
	public Board clone() {
		ArrayBoard ret = new ArrayBoard(getWidth(), getHeight());
		for (int i = 0; i < ret.board.length; i++)
			System.arraycopy(board[0], 0, ret.board[i], 0, board[i].length);
		return ret;
	}
}