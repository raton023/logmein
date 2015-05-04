package com.craftilandia.logmein;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
	getServer().getPluginManager().registerEvents(this, this);
	}
	ArrayList<String> loginuser = new ArrayList<String>();
	@EventHandler
	public void entrando(PlayerLoginEvent e){
		loginuser.add(e.getPlayer().getName());
	}
	@EventHandler
	public void adentro(PlayerJoinEvent e){
		if(loginuser.contains(e.getPlayer().getName())){
			e.getPlayer().sendMessage(ChatColor.RED + "You need to login.");
			e.getPlayer().sendMessage(ChatColor.RED + "Use /login passwd.");}}

	@EventHandler
	public void nomove(PlayerMoveEvent e) {
		if(loginuser.contains(e.getPlayer().getName())){e.setCancelled(true);}
		else{e.setCancelled(false);}}
	@EventHandler
	public void norompe(BlockBreakEvent e) {
		if(loginuser.contains(e.getPlayer().getName())){e.setCancelled(true);}
		else{e.setCancelled(false);}}
	@EventHandler
	public void nopone(BlockPlaceEvent e) {
		if(loginuser.contains(e.getPlayer().getName())){e.setCancelled(true);}
		else{e.setCancelled(false);}}

	@EventHandler
	public void nochat(AsyncPlayerChatEvent e){
		if(loginuser.contains(e.getPlayer().getName())){e.setCancelled(true);}
		else{e.setCancelled(false);}}
	
	public void nocommand(PlayerCommandPreprocessEvent e){
		if(loginuser.contains(e.getPlayer().getName())){e.setCancelled(true);}
		else{e.setCancelled(false);}}
	@EventHandler
	public void nodamage(EntityDamageByEntityEvent e){
		if(e.getEntity() instanceof Player){
			Player p = (Player)e.getEntity();
			if(loginuser.contains(p.getPlayer().getName())){e.setCancelled(true);}
			else{e.setCancelled(false);}}}
	@EventHandler
	public void items(PlayerInteractEvent e){
		if(loginuser.contains(e.getPlayer().getName())){
			e.setCancelled(true);
		}else{e.setCancelled(false);}}
	@EventHandler
	public void saliendo(PlayerQuitEvent e){
				
	}
	@EventHandler
	public void inventario(InventoryClickEvent e){
		if (e.getWhoClicked().getName() != null) {
			if(loginuser.contains(e.getWhoClicked().getName())){
				e.setCancelled(true);	
			}}return;}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(!(sender instanceof Player)){
			sender.sendMessage("Do you realy want to login console?");
		}
if(command.getName().equalsIgnoreCase("register")){
	loginuser.remove(0);
	sender.sendMessage("Registrado.");
}
if(command.getName().equalsIgnoreCase("login")){
	if(args.length == 0){
		sender.sendMessage("add the password");
	}if(args.length == 1){
		sender.sendMessage("tu clave es: " + args[0]);
		loginuser.remove(0);
		sender.sendMessage("Login Correcto.");	
	}
}
	return true;
	}
	
	
}
