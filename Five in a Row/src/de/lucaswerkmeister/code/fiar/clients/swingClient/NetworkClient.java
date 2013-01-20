package de.lucaswerkmeister.code.fiar.clients.swingClient;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
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
import javax.swing.ListSelectionModel;

import de.lucaswerkmeister.code.fiar.framework.Hoster;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.RemoteClient;
import de.lucaswerkmeister.code.fiar.framework.Server;
import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
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
	private Server server;
	private JFrame lobby;

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
		lobby = new JFrame("Lobby");
		lobby.setLayout(new BorderLayout());
		JButton addPlayerButton = new JButton("Add player");
		addPlayerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Player player = GameFrame.showAddPlayerDialog(false, findID(), lobby);
				while (player != null) {
					try {
						hoster.addPlayer(instance, player);
						ownPlayers.add(player);
						player = null;
					} catch (UnknownClientException e) {
						e.printStackTrace();
						System.exit(1); // this should never happen, I can afford a user-unfriendly shutdown here
					} catch (IllegalArgumentException e) {
						int option =
								JOptionPane.showConfirmDialog(lobby, "Player could not be added. Add another player?",
										"Error", JOptionPane.YES_NO_OPTION);
						if (option == JOptionPane.YES_OPTION)
							player = GameFrame.showAddPlayerDialog(false, findID(), lobby);
						else
							player = null;
						e.printStackTrace();
					} catch (RemoteException e) {
						JOptionPane.showMessageDialog(lobby,
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
		lobby.add(addPlayerPanel, BorderLayout.NORTH);
		playerListModel = new DefaultListModel<>();
		final JList<String> playerList = new JList<>(playerListModel);
		playerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		lobby.add(new JScrollPane(playerList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
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
						JOptionPane.showMessageDialog(lobby,
								"An error occured while sending the information to the hoster. Exiting.", "Error",
								JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
						System.exit(1);
					}
			}
		});
		JPanel removePlayerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		removePlayerPanel.add(removePlayerButton);
		lobby.add(removePlayerPanel, BorderLayout.SOUTH);
		lobby.pack();
		lobby.setVisible(true);
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
				// This should never happen; wait() should be ended by notify(), never by interrupt()
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public void playerJoined(Player player) {
		allPlayers.add(player);
		playerListModel.addElement(player.getName());
		lobby.pack();
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
		// TODO Auto-generated method stub
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
}