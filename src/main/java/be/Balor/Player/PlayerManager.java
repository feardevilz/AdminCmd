/************************************************************************
 * This file is part of AdminCmd.									
 *																		
 * AdminCmd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by	
 * the Free Software Foundation, either version 3 of the License, or		
 * (at your option) any later version.									
 *																		
 * AdminCmd is distributed in the hope that it will be useful,	
 * but WITHOUT ANY WARRANTY; without even the implied warranty of		
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the			
 * GNU General Public License for more details.							
 *																		
 * You should have received a copy of the GNU General Public License
 * along with AdminCmd.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************/
package be.Balor.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;

/**
 * @author Balor (aka Antoine Aflalo)
 * 
 */
public class PlayerManager {
	private ConcurrentMap<String, ACPlayer> players = new MapMaker().concurrencyLevel(8)
			.weakValues().makeMap();
	private Set<ACPlayer> onlinePlayers = new HashSet<ACPlayer>();
	private static PlayerManager instance = null;
	private ACPlayerFactory playerFactory;

	/**
	 * 
	 */
	private PlayerManager() {
		EmptyPlayer console = new EmptyPlayer("serverConsole");
		onlinePlayers.add(console);
		addPlayer(console);
	}

	/**
	 * @return the instance
	 */
	public static PlayerManager getInstance() {
		if (instance == null)
			instance = new PlayerManager();
		return instance;
	}

	/**
	 * @param playerFactory
	 *            the playerFactory to set
	 */
	public void setPlayerFactory(ACPlayerFactory playerFactory) {
		this.playerFactory = playerFactory;
	}

	/**
	 * Add a new player
	 * 
	 * @param player
	 */
	private synchronized boolean addPlayer(ACPlayer player) {
		final String name = player.getName();
		if (name == null) {
			throw new NullPointerException();
		}

		ACPlayer ref = players.get(name);
		if (ref != null)
			return false;
		players.put(name, player);
		if (player.getHandler() != null) {
			onlinePlayers.add(player);
			player.setOnline(true);
		}
		return true;
	}

	/**
	 * Get the wanted player
	 * 
	 * @param name
	 *            name of the player
	 * @return the ACPlayer if found, else null
	 */
	private synchronized ACPlayer getPlayer(String name) {
		return players.get(name);
	}

	/**
	 * Set Offline an online player. The player will lost his strong reference,
	 * when the gc will be called, the reference will be deleted.
	 * 
	 * @param player
	 *            player to setOffline
	 * @return
	 */
	public boolean setOffline(ACPlayer player) {
		player.updatePlayedTime();
		player.forceSave();
		player.setOnline(false);
		return onlinePlayers.remove(player);
	}

	public void setOnline(String player) {
		playerFactory.addExistingPlayer(player);
	}

	ACPlayer demandACPlayer(String name) {
		ACPlayer result = getPlayer(name);
		if (result == null) {
			result = playerFactory.createPlayer(name);
			addPlayer(result);
			result = getPlayer(name);
		} else if (result instanceof EmptyPlayer) {
			ACPlayer tmp = playerFactory.createPlayer(name);
			if (tmp.equals(result))
				return result;
			result = tmp;
			players.remove(name);
			addPlayer(result);
			result = getPlayer(name);
		}

		return result;
	}

}
