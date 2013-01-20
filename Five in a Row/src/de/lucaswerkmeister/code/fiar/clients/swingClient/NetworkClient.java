package de.lucaswerkmeister.code.fiar.clients.swingClient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.lucaswerkmeister.code.fiar.clients.swingClient.GameFrame.BoardListener;
import de.lucaswerkmeister.code.fiar.framework.Hoster;
import de.lucaswerkmeister.code.fiar.framework.NoPlayer;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.RemoteClient;
import de.lucaswerkmeister.code.fiar.framework.Server;
import de.lucaswerkmeister.code.fiar.framework.event.BlockDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.BlockField;
import de.lucaswerkmeister.code.fiar.framework.event.BoardSizeProposal;
import de.lucaswerkmeister.code.fiar.framework.event.GameEnd;
import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.JokerField;
import de.lucaswerkmeister.code.fiar.framework.event.PlaceStone;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerVictory;
import de.lucaswerkmeister.code.fiar.framework.event.UnblockField;
import de.lucaswerkmeister.code.fiar.framework.event.UnjokerField;
import de.lucaswerkmeister.code.fiar.framework.exception.IllegalMoveException;
import de.lucaswerkmeister.code.fiar.framework.exception.UnknownClientException;
import de.lucaswerkmeister.code.fiar.framework.exception.UnknownPlayerException;

/**
 * A client that runs in a Swing GUI and is connected to a remote {@link Server} / {@link Hoster}.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class NetworkClient implements RemoteClient, Runnable {
	private final Hoster hoster;
	private final List<Player> ownPlayers; // note that the contents of the list are not final
	private final List<Player> allPlayers;
	private final Queue<GameEvent> events;
	final NetworkClient instance; // needed for event listeners
	private DefaultListModel<String> playerListModel;
	private DefaultListModel<Dimension> dimensionListModel;
	private Server server;
	private JFrame initFrame;
	private GameFrame gameFrame;
	private volatile int playerIndex;

	public NetworkClient() throws AccessException, NumberFormatException, HeadlessException, RemoteException,
			NotBoundException {
		this(JOptionPane.showInputDialog("Please enter the hoster address (hostname:port)"));
	}

	public NetworkClient(String address) throws AccessException, NumberFormatException, RemoteException,
			NotBoundException {
		this(address.substring(0, address.indexOf(':')), Integer.parseInt(address.substring(address.indexOf(':') + 1)));
	}

	public NetworkClient(String hostName, int port) throws AccessException, RemoteException, NotBoundException {
		instance = this;
		hoster = (Hoster) LocateRegistry.getRegistry(hostName, port).lookup("hoster");
		UnicastRemoteObject.exportObject(this, 0);
		ownPlayers = new LinkedList<>();
		allPlayers = new LinkedList<>();
		hoster.addClient(this);
		events = new LinkedList<>();
	}

	@Override
	public void run() {
		try {
			initFrame = new JFrame("Lobby");
			initFrame.setLayout(new BorderLayout());
			JButton addPlayerButton = new JButton("Add players");
			addPlayerButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					Player player = GameFrame.showAddPlayerDialog(false, findID(), initFrame);
					while (player != null) {
						try {
							hoster.addPlayer(instance, player);
							ownPlayers.add(player);
							player = GameFrame.showAddPlayerDialog(false, findID(), initFrame);
						} catch (UnknownClientException e) {
							e.printStackTrace();
							System.exit(1); // this should never happen, I can afford a user-unfriendly shutdown here
						} catch (IllegalArgumentException e) {
							int option =
									JOptionPane.showConfirmDialog(initFrame,
											"Player could not be added. Add another player?", "Error",
											JOptionPane.YES_NO_OPTION);
							if (option == JOptionPane.YES_OPTION)
								player = GameFrame.showAddPlayerDialog(false, findID(), initFrame);
							else
								player = null;
							e.printStackTrace();
						} catch (RemoteException e) {
							JOptionPane.showMessageDialog(initFrame,
									"An error occured while sending the information to the hoster. Exiting.", "Error",
									JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
							System.exit(1);
						}
					}
				}
			});
			JPanel addPlayerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			addPlayerPanel.add(addPlayerButton);
			initFrame.add(addPlayerPanel, BorderLayout.NORTH);
			playerListModel = new DefaultListModel<>();
			final JList<String> playerList = new JList<>(playerListModel);
			initFrame.add(new JScrollPane(playerList), BorderLayout.CENTER);
			JButton removePlayerButton = new JButton("Remove player(s)");
			removePlayerButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					int[] players = playerList.getSelectedIndices();
					if (players.length > 0)
						try {
							for (int index : players)
								hoster.removePlayer(allPlayers.get(index));
						} catch (UnknownPlayerException e) {
							// This should NEVER EVER happen, so again no user-friendly shutdown
							e.printStackTrace();
							System.exit(1);
						} catch (RemoteException e) {
							JOptionPane.showMessageDialog(initFrame,
									"An error occured while sending the information to the hoster. Exiting.", "Error",
									JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
							System.exit(1);
						}
				}
			});
			JPanel removePlayerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			removePlayerPanel.add(removePlayerButton);
			initFrame.add(removePlayerPanel, BorderLayout.SOUTH);
			initFrame.pack();
			initFrame.setVisible(true);
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					// This should never happen; wait() should be ended by notify(), never by interrupt()
					e.printStackTrace();
					System.exit(1);
				}
			}
			initFrame.setVisible(false);
			initFrame.dispose();

			initFrame = new JFrame("Board size");
			initFrame.setLayout(new BorderLayout());
			JButton addDimensionButton = new JButton("Add size");
			addDimensionButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					Dimension size = GameFrame.showChooseBoardSizeDialog(initFrame);
					for (Player p : ownPlayers)
						try {
							server.action(instance, new BoardSizeProposal(p, size));
						} catch (IllegalStateException | IllegalMoveException e) {
							e.printStackTrace();
						} catch (RemoteException e) {
							failRemote(e);
						}
				}
			});
			JPanel addDimensionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			addDimensionPanel.add(addDimensionButton);
			initFrame.add(addDimensionPanel, BorderLayout.NORTH);
			dimensionListModel = new DefaultListModel<>();
			final JList<Dimension> dimensionList = new JList<>(dimensionListModel);
			initFrame.add(new JScrollPane(dimensionList), BorderLayout.CENTER);
			JButton acceptDimensionButton = new JButton("Accept size(s)");
			acceptDimensionButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					for (Dimension d : dimensionList.getSelectedValuesList())
						for (Player p : ownPlayers)
							try {
								server.action(instance, new BoardSizeProposal(p, d));
							} catch (IllegalStateException | IllegalMoveException e) {
								e.printStackTrace();
							} catch (RemoteException e) {
								failRemote(e);
							}
				}
			});
			JPanel acceptDimensionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			acceptDimensionPanel.add(acceptDimensionButton);
			initFrame.add(acceptDimensionPanel, BorderLayout.SOUTH);
			initFrame.pack();
			initFrame.setVisible(true);
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					// This should never happen; wait() should be ended by notify(), never by interrupt()
					e.printStackTrace();
					System.exit(1);
				}
			}
			initFrame.setVisible(false);
			initFrame.dispose();
			initFrame = null;

			gameFrame =
					new GameFrame(server.getCurrentBoard(this), (Server.IN_A_ROW == 5 ? "Five" : Server.IN_A_ROW)
							+ " in a Row");
			gameFrame.setButtons(new String[] {"Accept current block distribution" });
			gameFrame.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					try {
						for (Player p : ownPlayers)
							server.action(instance, new BlockDistributionAccepted(p, server.getCurrentBoard(instance)));
					} catch (RemoteException e) {
						failRemote(e);
					} catch (IllegalStateException | IllegalMoveException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			});
			playerIndex = 0;
			gameFrame.addBoardListener(new BoardListener() {
				@Override
				public void fieldClicked(Field field) {
					try {
						// This board listener is going to stay in place for the rest of the game, so it doesn't just
						// handle blocking
						final int[] phase = server.getPhase(instance);
						final Point xy = field.getField();
						if (phase[0] == 0 && phase[1] == 1) {
							// blocking
							server.action(instance, server.getCurrentBoard(instance).getPlayerAt(xy) == NoPlayer
									.getInstance() ? new BlockField(ownPlayers.get(0), xy) : new UnblockField(
									ownPlayers.get(0), xy));
							events.poll(); // BlockField / UnblockField
						} else if (phase[0] == 0 && phase[1] == 2) {
							// jokers
							server.action(instance, server.getCurrentBoard(instance).getPlayerAt(xy) == NoPlayer
									.getInstance() ? new JokerField(ownPlayers.get(0), xy) : new UnjokerField(
									ownPlayers.get(0), xy));
							events.poll(); // JokerField / UnjokerField
						} else if (phase[0] == 1 && phase[1] == 1) {
							// move
							server.action(instance, new PlaceStone(allPlayers.get(playerIndex), xy));
							events.poll(); // PlaceStone
							playerIndex = (playerIndex + 1) % allPlayers.size();
							if (events.isEmpty())
								gameFrame.setStatus(allPlayers.get(playerIndex).getName() + "'"
										+ (GameFrame.endsWithSSound(allPlayers.get(playerIndex).getName()) ? "" : "s")
										+ " turn!");
							else {
								final GameEvent event = events.poll();
								if (event instanceof PlayerVictory) {
									final String message =
											((PlayerVictory) event).getWinningPlayer().getName() + " wins!";
									JOptionPane.showMessageDialog(gameFrame, message);
									gameFrame.setStatus(message);
								}
								if (event instanceof GameEnd)
									gameFrame.setEnabledAll(false);
							}
						}
					} catch (RemoteException e) {
						failRemote(e);
					} catch (IllegalMoveException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			});
			gameFrame.setStatus(allPlayers.get(playerIndex).getName() + "'"
					+ (GameFrame.endsWithSSound(allPlayers.get(playerIndex).getName()) ? "" : "s") + " turn!");
			gameFrame.setVisible(true);
		} catch (RemoteException e) {
			failRemote(e);
		}
	}

	public void playerJoined(Player player) {
		allPlayers.add(player);
		playerListModel.addElement(player.getName());
		initFrame.pack();
		GameFrame.reshowAddPlayerDialog(findID());
	}

	@Override
	public void playerLeft(Player player) {
		playerListModel.removeElementAt(allPlayers.indexOf(player));
		allPlayers.remove(player);
	}

	@Override
	public void gameStarts(Server server) {
		GameFrame.hideAddPlayerDialog();
		this.server = server;
		System.out.println("STARTED");
		synchronized (this) {
			this.notify();
		}
	}

	@Override
	public void gameEvent(GameEvent e) throws RemoteException {
		if (e instanceof BoardSizeProposal) {
			BoardSizeProposal b = (BoardSizeProposal) e;
			if (!dimensionListModel.contains(b.getSize())) {
				int index = Arrays.binarySearch(dimensionListModel.toArray(), b.getSize(), new Comparator<Object>() {
					@Override
					public int compare(Object o1, Object o2) {
						Dimension d1 = (Dimension) o1;
						Dimension d2 = (Dimension) o2;
						int first = Integer.compare(d1.width, d2.width);
						if (first == 0)
							return Integer.compare(d1.height, d2.height);
						return first;
					}
				});
				dimensionListModel.add(-(index + 1), b.getSize());
			}
		}
		// TODO all other events
	}

	/**
	 * Finds an unused ID.
	 * 
	 * @return An ID that is not yet used by any player known to the client.
	 */
	private int findID() {
		int id = 0;
		boolean isUsed;
		do {
			id++;
			isUsed = false;
			for (Player p : allPlayers)
				if (p.getID() == id) {
					isUsed = true;
					break;
				}
		} while (isUsed);
		return id;
	}

	private void failRemote(RemoteException e) {
		JOptionPane.showMessageDialog(initFrame,
				"An error occured while sending the information to the server. Exiting.", "Error",
				JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
		System.exit(1);
	}
}