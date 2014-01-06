package com.voidteam.PExChat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener
{	
    private final Main plugin;

    ChatListener(Main plugin) {
        this.plugin = plugin;
    }
		
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e)
	{
		if (this.plugin.permissions == null) return;
		if (e.isCancelled()) return;
		
		Player p = e.getPlayer();
		String msg = e.getMessage();
		
		e.setFormat(this.plugin.parseChat(p, msg) + " ");
	}
}