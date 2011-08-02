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
package be.Balor.Manager.Permissions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionDefault;

import be.Balor.Manager.PermParent;
import be.Balor.Tools.Utils;
import be.Balor.bukkit.AdminCmd.AdminCmd;

import com.nijiko.permissions.PermissionHandler;

/**
 * @author Balor (aka Antoine Aflalo)
 * 
 */
public class PermissionManager {
	private LinkedList<PermParent> permissions = new LinkedList<PermParent>();
	private PermParent majorPerm;
	private static PermissionManager instance = null;
	protected static PermissionHandler permission = null;
	public static final Logger log = Logger.getLogger("Minecraft");

	/**
	 * @return the instance
	 */
	public static PermissionManager getInstance() {
		if (instance == null)
			instance = new PermissionManager();
		return instance;
	}

	/**
	 * Set major permission, the root.
	 * 
	 * @param major
	 */
	public void setMajorPerm(PermParent major) {
		majorPerm = major;
		for (PermParent pp : permissions)
			majorPerm.addChild(pp.getPermName());
	}

	/**
	 * Add some important node (like myplygin.item)
	 * 
	 * @param toAdd
	 */
	public void addPermParent(PermParent toAdd) {
		permissions.add(toAdd);
	}

	/**
	 * Add permission child (like myplugin.item.add)
	 * 
	 * @param permNode
	 * @param bukkitDefault
	 * @return
	 */
	public Permission addPermChild(String permNode, PermissionDefault bukkitDefault) {
		Permission bukkitPerm = null;
		if ((bukkitPerm = AdminCmd.getBukkitServer().getPluginManager().getPermission(permNode)) == null) {
			bukkitPerm = new Permission(permNode, bukkitDefault);
			AdminCmd.getBukkitServer().getPluginManager().addPermission(bukkitPerm);
			for (PermParent pp : permissions)
				if (permNode.contains(pp.getCompareName()))
					pp.addChild(permNode);
		}
		return bukkitPerm;
	}

	/**
	 * Add permission child (like myplugin.item.add)
	 * 
	 * @param permNode
	 * @return
	 */
	public Permission addPermChild(String permNode) {
		return addPermChild(permNode, PermissionDefault.OP);
	}

	/**
	 * Register all parent node.
	 */
	public void registerAllPermParent() {
		for (PermParent pp : permissions)
			pp.registerBukkitPerm();
		majorPerm.registerBukkitPerm();
		permissions = null;
		majorPerm = null;
	}

	/**
	 * Check the permissions
	 * 
	 * @param player
	 * @param perm
	 * @return boolean
	 */
	public static boolean hasPerm(CommandSender player, String perm) {
		return hasPerm(player, perm, true);
	}

	public static boolean hasPerm(CommandSender player, Permission perm) {
		return hasPerm(player, perm, true);
	}

	/**
	 * Check the permission with the possibility to disable the error msg
	 * 
	 * @param player
	 * @param perm
	 * @param errorMsg
	 * @return
	 */
	public static boolean hasPerm(CommandSender player, String perm, boolean errorMsg) {
		if (!(player instanceof Player))
			return true;
		if (permission == null) {
			if (perm.contains("admin") || perm.contains("free"))
				return player.hasPermission(perm);
			return true;
		} else if (permission.has((Player) player, perm)) {
			return true;
		} else {
			if (errorMsg) {
				HashMap<String, String> tmp = new HashMap<String, String>();
				tmp.put("p", perm);
				Utils.sI18n(player, "errorNotPerm", tmp);
			}
			return false;
		}

	}

	public static boolean hasPerm(CommandSender player, Permission perm, boolean errorMsg) {
		if (!(player instanceof Player))
			return true;
		if (permission == null) {
			boolean havePerm = player.hasPermission(perm);
			if (!havePerm && errorMsg) {
				HashMap<String, String> tmp = new HashMap<String, String>();
				tmp.put("p", perm.getName());
				Utils.sI18n(player, "errorNotPerm", tmp);
			}
			return havePerm;
		} else if (permission.has((Player) player, perm.getName())) {
			return true;
		} else {
			if (errorMsg) {
				HashMap<String, String> tmp = new HashMap<String, String>();
				tmp.put("p", perm.getName());
				Utils.sI18n(player, "errorNotPerm", tmp);
			}
			return false;
		}

	}

	public static String getPermissionLimit(Player p, String limit) {
		Pattern regex = Pattern.compile("admincmd\\."+limit.toLowerCase()+"\\.[0-9]+");
		for (PermissionAttachmentInfo info : p.getEffectivePermissions()) {
			Matcher regexMatcher = regex.matcher(info.getPermission());
			if (regexMatcher.find())
				return info.getPermission().split("\\.")[2];

		}
		return null;
	}

	/**
	 * Permission plugin
	 * 
	 * @return
	 */
	public static PermissionHandler getPermission() {
		return permission;
	}

	/**
	 * Set Permission Plugin
	 * 
	 * @param plugin
	 * @return
	 */
	public static boolean setPermission(PermissionHandler plugin) {
		if (permission == null) {
			permission = plugin;
		} else {
			return false;
		}
		return true;
	}

}