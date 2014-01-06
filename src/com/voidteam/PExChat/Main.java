package com.voidteam.PExChat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Main extends JavaPlugin
{
	public PermissionManager permissions = null;

	FileConfiguration config;
	File configFile;
	public String chatFormat = "[+prefix+group+suffix&f] +name: +message";
	public String multigroupFormat = "[+prefix+group+suffix&f]";
	public String dateFormat = "HH:mm:ss";
	public List<Track> tracks = new ArrayList<Track>();
	public HashMap<String, String> aliases = new HashMap<String, String>();
	
	public void onEnable()
	{
		
		if (getServer().getPluginManager().isPluginEnabled("PermissionsEx"))
		{
			this.permissions = PermissionsEx.getPermissionManager();
			getLogger().info("[PExChat] PermissionsEx found!");
		}
		else
		{
			getLogger().info("[PExChat] PermissionsEx not found. Disabling PExChat.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		this.configFile = new File(getDataFolder() + "/config.yml");
		if (!this.configFile.exists())
		{
			defaultConfig();
		}
		
		loadConfig();
	
		getServer().getPluginManager().registerEvents(new ChatListener(this), this);
		
		getLogger().info("[" + getDescription().getName() + "] v" + getDescription().getVersion() + " enabled.");
	}
	
	public void onDisable()
	{
		getLogger().info("[" + getDescription().getName() + "] v" + getDescription().getVersion() + " disabled.");
	}
	
	private void loadConfig()
	{
		this.config = YamlConfiguration.loadConfiguration(this.configFile);
		this.chatFormat = this.config.getString("message-format");
		this.multigroupFormat = this.config.getString("multigroup-format");
		this.dateFormat = this.config.getString("date-format");
		Set<String> tracknames = new HashSet<String>();
		tracknames = this.config.getConfigurationSection("tracks").getKeys(false);
		Track loadtrack;
		
		if (tracknames != null)
		{
			for (String track : tracknames)
			{
				loadtrack = new Track();
				loadtrack.groups = this.config.getStringList("tracks." + track + ".groups");
				loadtrack.priority = Integer.valueOf(this.config.getInt("tracks." + track + ".priority", 0));
				loadtrack.name = track;
				this.tracks.add(loadtrack);
			}
		}
		
		Set<String> tmpaliases = new HashSet<String>();
		tmpaliases = this.config.getConfigurationSection("aliases").getKeys(false);
		
		if (tmpaliases != null)
			for (String alias : tmpaliases)
				this.aliases.put(alias, this.config.getString("aliases." + alias));
	}
	
	private void defaultConfig()
	{
		this.config = YamlConfiguration.loadConfiguration(this.configFile);
		this.config.set("message-format", this.chatFormat);
		this.config.set("multigroup-format", this.multigroupFormat);
		this.config.set("date-format", this.dateFormat);
		HashMap<String, String> aliases = new HashMap<String, String>();
		aliases.put("Admin", "A");
		List<String> track = new ArrayList<String>();
		track.add("Admin");
		track.add("Moderator");
		track.add("Builder");
		this.config.set("tracks.default.groups", track);
		this.config.set("tracks.default.priority", Integer.valueOf(1));
		this.config.set("aliases", aliases);
		try
		{
			this.config.save(this.configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public String parseVars(String format, Player p)
	{
		Pattern pattern = Pattern.compile("\\+\\{(.*?)\\}");
		Matcher matcher = pattern.matcher(format);
		StringBuffer sb = new StringBuffer();
		
		while (matcher.find())
		{
			String var = getVariable(p, matcher.group(1));
			matcher.appendReplacement(sb, Matcher.quoteReplacement(var));
		}
		
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	public String replaceVars(String format, String[] search, String[] replace)
	{
		if (search.length != replace.length) return "";
		for (int i = 0; i < search.length; i++)
		{
			if (search[i].contains(","))
			{
				for (String s : search[i].split(","))
					if ((s != null) && (replace[i] != null))
						format = format.replace(s, replace[i]);
			}
			else
			{
				format = format.replace(search[i], replace[i]);
			}
		}
		
		return format.replaceAll("(&([a-f0-9]))", "§$2");
	}
	
	public String parseChat(Player p, String msg, String chatFormat)
	{
		String prefix = getPrefix(p);
		String suffix = getSuffix(p);
		String group = getGroup(p);
		if (prefix == null) prefix = "";
		if (suffix == null) suffix = "";
		if (group == null) group = "";
		String healthbar = healthBar(p);
		String health = String.valueOf(p.getHealth());
		String world = p.getWorld().getName();
		
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
		String time = dateFormat.format(now);
		
		msg = msg.replaceAll("%", "%%");
		
		String format = parseVars(chatFormat, p);
		
		String groups = "";
		if (format.contains("+groups"))
		{
			groups = parseGroups(p, this.multigroupFormat);
		}
	
		ArrayList<String> searchlist = new ArrayList<String>();
		ArrayList<String> replacelist = new ArrayList<String>();
		
		String[] playergroups = this.permissions.getUser(p).getGroupsNames();
		
		for (Track track : this.tracks)
		{
			Boolean found = Boolean.valueOf(false);
		
			for (String playergroup : playergroups)
			{
				if (track.groups.contains(playergroup))
				{
					searchlist.add("+prefix." + track.name);
					searchlist.add("+suffix." + track.name);
					searchlist.add("+group." + track.name);
		
					replacelist.add(getGroupPrefix(playergroup, p.getWorld().getName()));
					replacelist.add(getGroupSuffix(playergroup, p.getWorld().getName()));
					replacelist.add(getAlias(playergroup));
		
					found = Boolean.valueOf(true);
				}
			}
			
			if (found.equals(Boolean.valueOf(false)))
			{
				searchlist.add("+prefix." + track.name);
				searchlist.add("+suffix." + track.name);
				searchlist.add("+group." + track.name);
				replacelist.add("");
				replacelist.add("");
				replacelist.add("");
			}
	
		}
	
		String[] search = { "+suffix,+s", "+prefix,+p", "+groups,+gs", "+group,+g", "+healthbar,+hb", "+health,+h", "+world,+w", "+time,+t", "+name,+n", "+displayname,+d", "+message,+m" };
		String[] replace = { suffix, prefix, groups, group, healthbar, health, world, time, p.getName(), p.getDisplayName(), msg };
		
		for (int i = 0; i < search.length; i++)
		{
			searchlist.add(search[i]);
		}
		for (int i = 0; i < replace.length; i++) 
		{
			replacelist.add(replace[i]);
		}
	
		search = (String[])searchlist.toArray(new String[searchlist.size()]);
		replace = (String[])replacelist.toArray(new String[replacelist.size()]);
	
		return replaceVars(format, search, replace);
	}
	
	public String parseChat(Player p, String msg)
	{
		return parseChat(p, msg, this.chatFormat);
	}
	
	public String parseGroups(Player p, String mgFormat)
	{
		String[] groups = this.permissions.getUser(p).getGroupsNames();
		
		String output = "";
		HashMap<Integer, String> unparsedGroups = new HashMap<Integer, String>();
		int max = 0;
		int key = 0;
		
		for (String group : groups)
		{
			for (Track track : this.tracks)
			{
				if (track.priority.intValue() >= 1)
				{
					for (String trackgroup : track.groups)
					{
						if (trackgroup.equalsIgnoreCase(group))
						{
							key = track.priority.intValue();
							while (unparsedGroups.containsKey(Integer.valueOf(key)))
							{
								key++;
							}
							unparsedGroups.put(Integer.valueOf(key), group);
							if (key > max)
							{
								max = key;
							}
						}
					}
				}
			}
		}
		
		String format = parseVars(mgFormat, p);
		
		for (int i = 0; i <= max; i++)
		{
			if (unparsedGroups.containsKey(Integer.valueOf(i)))
			{
				String groupname = (String)unparsedGroups.get(Integer.valueOf(i));
				String prefix = getGroupPrefix(groupname, p.getWorld().getName());
				
				if (prefix == null)
				{
					prefix = "";
				}
				
				String suffix = getGroupSuffix(groupname, p.getWorld().getName());
			
				if (suffix == null)
				{
					suffix = "";
				}
				
				groupname = getAlias(groupname);
			
				String[] search = { "+suffix,+s", "+prefix,+p", "+group,+g" };
				String[] replace = { suffix, prefix, groupname };
				output = output + replaceVars(format, search, replace);
			}
		}
		
		return output;
	}
	
	private String getAlias(String group)
	{
		if (this.aliases.containsKey(group))
		{
			return (String)this.aliases.get(group);
		}
		
		return group;
	}
	
	public String healthBar(Player player)
	{
		float maxHealth = 20.0F;
		float barLength = 10.0F;
		float health = (float) player.getHealth();
		int fill = Math.round(health / maxHealth * barLength);
		String barColor = "&2";
		
		if (fill <= 4) barColor = "&4";
		else if (fill <= 7) barColor = "&e";
		else barColor = "&2";
		
		StringBuilder out = new StringBuilder();
		out.append(barColor);
		for (int i = 0; i < barLength; i++)
		{
			if (i == fill) out.append("&8");
			out.append("|");
		}
		out.append("&f");
		
		return out.toString();
	}
	
	public boolean hasPerm(Player player, String perm)
	{
		if (this.permissions.has(player, perm))
		{
			return true;
		}
		
		return player.isOp();
	}
	
	public String getPrefix(Player player)
	{
		if (this.permissions != null)
		{
			return this.permissions.getUser(player).getPrefix(player.getWorld().getName());
		}
		getLogger().severe("[ There is no Permissions module, why are we running?!??!?");
		return null;
	}
	
	public String getSuffix(Player player)
	{
		if (this.permissions != null)
		{
			return this.permissions.getUser(player).getSuffix(player.getWorld().getName());
		}
		getLogger().severe("[" + getDescription().getName() + "] There is no Permissions module, why are we running?!??!?");
		return null;
	}
	
	public String getGroupPrefix(String group, String worldname)
	{
		if (this.permissions != null)
		{
			return this.permissions.getGroup(group).getPrefix(worldname);
		}
		getLogger().severe("[" + getDescription().getName() + "] There is no Permissions module, why are we running?!??!?");
		return null;
	}
	
	public String getGroupSuffix(String group, String worldname)
	{
		if (this.permissions != null)
		{
			return this.permissions.getGroup(group).getSuffix(worldname);
		}
		getLogger().severe("[" + getDescription().getName() + "] There is no Permissions module, why are we running?!??!?");
		return null;
	}
	
	public String getGroup(Player player)
	{
		if (this.permissions != null)
		{
			String[] groups = this.permissions.getUser(player).getGroupsNames(player.getWorld().getName());
			return groups[0];
		}
		getLogger().severe("[" + getDescription().getName() + "] There is no Permissions module, why are we running?!??!?");
		return null;
	}
	
	public String getVariable(Player player, String variable)
	{
		if (this.permissions != null)
		{
			String userVar = this.permissions.getUser(player).getOption(variable);
			
			if ((userVar != null) && (!userVar.isEmpty()))
			{
				return userVar;
			}
	
			String group = this.permissions.getGroup(getGroup(player)).getName();
	
			if (group == null) return "";
			String groupVar = this.permissions.getGroup(group).getOption(variable);
	
			if (groupVar == null) return "";
			return groupVar;
		}
		
		getLogger().severe("[" + getDescription().getName() + "] There is no Permissions module, why are we running?!??!?");
		return "";
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!command.getName().equalsIgnoreCase("pexchat")) return false;
		if (((sender instanceof Player)) && (!hasPerm((Player)sender, "pexchat.reload")))
		{
			sender.sendMessage("[PExChat] Permission Denied");
			return true;
		}
		if (args.length != 1) return false;
		if (args[0].equalsIgnoreCase("reload"))
		{
			this.aliases.clear();
			this.tracks.clear();
			loadConfig();
			sender.sendMessage("[PExChat] Config Reloaded");
			return true;
		}
		return false;
	}
	
	public final class Track
	{
		public String name = "";
		public Integer priority = Integer.valueOf(0);
		public List<String> groups = new ArrayList<String>();
		
		public Track()
		{
		}
	}
}