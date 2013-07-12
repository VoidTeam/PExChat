/*    */ package com.voidteam.PExChat;
/*    */ 
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.player.AsyncPlayerChatEvent;
/*    */ import org.bukkit.event.player.PlayerCommandPreprocessEvent;
/*    */ 
/*    */ public class playerListener
/*    */   implements Listener
/*    */ {
/*    */   PExChat pexchat;
/*    */ 
/*    */   playerListener(PExChat ichat)
/*    */   {
/* 33 */     this.pexchat = ichat;
/*    */   }
/*    */ 
/*    */   @EventHandler
/*    */   public void onPlayerChat(AsyncPlayerChatEvent event){
/* 38 */     if (this.pexchat.permissions == null) return;
/* 39 */     if (event.isCancelled()) return;
/* 40 */     Player p = event.getPlayer();
/* 41 */     String msg = event.getMessage();
/*    */ 
/* 43 */     event.setFormat(this.pexchat.parseChat(p, msg) + " ");
/*    */   }
/*    */ 
/*    */   @EventHandler
/*    */   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
/*    */   {
/* 49 */     if (this.pexchat.permissions == null) return;
/* 50 */     if (event.isCancelled()) return;
/* 51 */     Player p = event.getPlayer();
/* 52 */     String message = event.getMessage();
/*    */ 
/* 54 */     if (message.toLowerCase().startsWith("/me ")) {
/* 55 */       String s = message.substring(message.indexOf(" ")).trim();
/* 56 */       String formatted = this.pexchat.parseChat(p, s, this.pexchat.meFormat);
/*    */ 
/* 58 */       PExChatMeEvent meEvent = new PExChatMeEvent(p, formatted);
/* 59 */       Bukkit.getServer().getPluginManager().callEvent(meEvent);
/* 60 */       Bukkit.getServer().broadcastMessage(formatted);
/*    */ 
/* 62 */       event.setCancelled(true);
/*    */     }
/*    */   }
/*    */ }