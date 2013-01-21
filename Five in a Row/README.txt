CHANGES TO THE PREVIOUS VERSION
This version adds networking support over the Java Remote Method Invocation (RMI) framework.
If you don't want to put up with reading even more code, I hereby officially grant you permission to instead rate my previous solution (Solution 9).
The only other change is that I split up the SwingClient into client and GUI separately,
as it proved impractical to have local client, remote client and GUI all in one class; now, they are each in their own class.
Please note that to actually access the game remotely, you have to set up an actual server; I am writing this at 12:02 monday (1h before deadline), so I don't have time for that.
However, the fact that you can play the game distributed over several virtual machines on the same physical machine (and I suspect that it might even work in the Local Area Network,
but have not been able to test this) proves that the RMI part is actually working.

ABOUT FIVE IN A ROW

Run: src\...\clients\swingClients\LocalClient or \clients\ConsoleClient for local gameplay, src\...\hosters\FixedHoster for remote hosting, src\...\clients\swingClients\NetworkClient for remote playing
Tests: test\...\ConsoleClientTest, \FixedServerTest

The game is divided into Server and Client. The Server handles the game logic. The Client controls players and displays the game to them.
Server and Client communicate through GameEvents. Important GameEvents are:

* Server-sent GameEvents (gameEvent())
PhaseChange: The Phase of the game has changed. See the documentation on Server.getPhase() for more detailed info.
 A PhaseChange is only fired by the server if it is not implied by the previous event:
 For example, a PlayerVictory implies a PhaseChange to the "Game Ended" Phase, so no extra PhaseChange is fired.
GameEnd: The game ended. Most common subclass: PlayerVictory.
PlayerAction: See below.

* Client-sent GameEvents
The client sends PlayerActions to the server (action()). These are always bound to one player. Important subclasses are:
PlaceStone: Places a stone on a field of the board.
BlockField, UnblockField, JokerField, UnjokerField: Sent during game initialization. Self-explanatory.
BlockDistributionAccepted, JokerDistributionAccepted: Sent during game initialization. Once all clients have
 accepted one distribution, a PhaseChange initiates.
The Server re-sends all PlayerActions it receives to all registered clients, including the one that fired
 the event.

In addition to receiving GameEvents, the client can optionally:
Request a copy of the current Board from the server at any time (getCurrentBoard())
Ask the server's current phase (getPhase())
Ask the version of the phases protocol the server is using (getPhasesVersion())
Ask if a player can currently act at all, and which actions it can take (canAct() and getAllowedActions())


COMMENTS ON PRAKTOMAT CHECKS

*Imports
Every single one of those "unused" imports is actually used - by javadoc.

*Naming
Fuck naming. instance is not a public constant and has nothing to do with constants
such as SOUTH or EXIT_ON_CLOSE; I will not rename it to capslock.

*Coding
Why should inner statements be avoided? I think they were added to Java for a reason.
As far as I see, catching Throwable on the outermost level of Runnable.run() is no problem as long as ThreadDeaths and VirtualMachineErrors are handled properly.
The empty statements are for loops were everything is done in the loop declaration.
All those missing defaults are intentional: control falls through to the exception thrown below.