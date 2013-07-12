package com.voidteam.PExChat;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PExChatMeEvent extends Event
{
  private static final HandlerList handlers = new HandlerList();
private String message;
private Player player;

public PExChatMeEvent(Player player, String message)
{
	this.player = player;
    this.message = message;
}

public String getMessage() {
  return this.message;
}

public Player getPlayer() {
  return this.player;
}
public HandlerList getHandlers()
{
  return handlers;
}

public static HandlerList getHandlerList() {
  return handlers;
}
}