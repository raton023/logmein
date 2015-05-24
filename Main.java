package com.craftilandia.loginmein;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
public class Main extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
	getServer().getPluginManager().registerEvents(this, this);
	getConfig().options().copyHeader();
	getConfig().options().copyDefaults(true);
	saveConfig();}
	ArrayList<String> loginuser = new ArrayList<String>();

	@EventHandler
	public void motd(ServerListPingEvent e){
		
		if(getConfig().getString(e.getAddress().getHostName().replace(".", ",") + ".name") == null){
			e.setMotd("Welcome Player");
			return;
		}
		String name = getConfig().getString(e.getAddress().getHostName().replace(".", ",") + ".name");

		String getmotd = getConfig().getString(name + ".motd").replace("PLAYER", name);
		String setmotd = ChatColor.translateAlternateColorCodes('&', getmotd);
	e.setMotd(setmotd);
}
	@EventHandler
	public void entrando(PlayerLoginEvent e){loginuser.add(e.getPlayer().getName());}
	@EventHandler
	public void adentro(PlayerJoinEvent e){
		if(loginuser.contains(e.getPlayer().getName())){
			e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Use /login <password> or /register <password>");}}
	@EventHandler
	public void nomove(PlayerMoveEvent e) {
		if(loginuser.contains(e.getPlayer().getName())){
			if(e.getTo().getChunk() != e.getFrom().getChunk()){
				//e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 18000, 1));
				e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Use /login <password> or /register <password> before walking");
			}
		}
		
		}
	@EventHandler
	public void norompe(BlockBreakEvent e) {
		if(loginuser.contains(e.getPlayer().getName())){e.setCancelled(true);
		e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Use /login <password> or /register <password> before breaking blocks");
		}
		}
	@EventHandler
	public void nopone(BlockPlaceEvent e) {
		if(loginuser.contains(e.getPlayer().getName())){e.setCancelled(true);
		e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Use /login <password> or /register <password> before placing blocks");}
		}
	@EventHandler
	public void nochat(AsyncPlayerChatEvent e){
		if(loginuser.contains(e.getPlayer().getName())){e.setCancelled(true);
		e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Use /login <password> or /register <password> before chat");}
		}
	@EventHandler
	public void nodrop(PlayerDropItemEvent e){
		if(loginuser.contains(e.getPlayer().getName())){e.setCancelled(true);
		e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Use /login <password> or /register <password> before drop items");}
		}
	@EventHandler
	public void nodamage(EntityDamageByEntityEvent e){
		if(e.getEntity() instanceof Player){
			Player p = (Player)e.getEntity();
			if(loginuser.contains(p.getPlayer().getName())){e.setCancelled(true);
			}
			}}
	@EventHandler
	public void nodie(EntityDamageEvent e){
		if(e.getEntity() instanceof Player){
			Player p = (Player)e.getEntity();
			if(loginuser.contains(p.getPlayer().getName())){
				if(e.getCause() == DamageCause.FALL) {e.setCancelled(true);}
				if(e.getCause() == DamageCause.BLOCK_EXPLOSION) {e.setCancelled(true);}
				if(e.getCause() == DamageCause.CONTACT) {e.setCancelled(true);}
				if(e.getCause() == DamageCause.DROWNING) {e.setCancelled(true);}
				if(e.getCause() == DamageCause.STARVATION) {e.setCancelled(true);}
				if(e.getCause() == DamageCause.FALLING_BLOCK) {e.setCancelled(true);}
				if(e.getCause() == DamageCause.FIRE) {e.setCancelled(true);}
				if(e.getCause() == DamageCause.FIRE_TICK) {e.setCancelled(true);}
				if(e.getCause() == DamageCause.LAVA) {e.setCancelled(true);}
				if(e.getCause() == DamageCause.SUFFOCATION) {e.setCancelled(true);}
				if(e.getCause() == DamageCause.MAGIC) {e.setCancelled(true);}
				}
			}}
	@EventHandler
	public void items(PlayerInteractEvent e){
		if(loginuser.contains(e.getPlayer().getName())){
			e.setCancelled(true);
		}}
	@EventHandler
	public void inventario(InventoryClickEvent e){
		if (e.getWhoClicked().getName() != null) {
			if(loginuser.contains(e.getWhoClicked().getName())){
				e.setCancelled(true);	
			}}return;}
	@EventHandler
	public void comandos(PlayerCommandPreprocessEvent e){
		if(loginuser.contains(e.getPlayer().getName())){
			if(e.getMessage().startsWith("/login") || e.getMessage().startsWith("/register")){
				e.setCancelled(false);}
			else{e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Use /login <password> or /register <password> before use commands");}}}
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(!(sender instanceof Player)){
			sender.sendMessage("Do you realy want to login console?");
			return false;}
		Player p = (Player) sender;
		if(command.getName().equalsIgnoreCase("register")){
			if(args.length == 0){
				sender.sendMessage(ChatColor.DARK_PURPLE + "use /register password");
			}if(args.length == 1){
				if(getConfig().contains(p.getName())){
					p.sendMessage(ChatColor.GREEN + "You are already register.");
					return false;}
				else {
					loginuser.remove(p.getPlayer().getName());
					sender.sendMessage(ChatColor.YELLOW + "your passwd is: " + args[0]);
					getConfig().set(p.getName() + ".password", args[0]);
					getConfig().set(p.getName() + ".motd", "&aWelcome Back PLAYER \n&bWe Missed You!");			
					getConfig().set(p.getAddress().getHostName().replace(".", ",") + ".name", p.getName());
					getConfig().set(p.getName() + ".online", 1);
					saveConfig();
					}}if(args.length >= 2){
						p.sendMessage(ChatColor.DARK_PURPLE + "too many passwords, just give one");}}
		
		
		if(command.getName().equalsIgnoreCase("unregister")){
			if(args.length == 0){
				sender.sendMessage(ChatColor.DARK_PURPLE + "use /unregister player password");
			}
			if(args.length == 1){
				sender.sendMessage(ChatColor.DARK_PURPLE + "use /unregister player password");
			}if(args.length == 2){
				if(!getConfig().getString(args[0] + ".password").isEmpty()){
				if(getConfig().getString(args[0] + ".password").equals(args[1])){
					getConfig().set(args[0], null);
					getConfig().set(p.getAddress().getHostName().replace(".", ","), null);
					saveConfig();
					sender.sendMessage(ChatColor.DARK_PURPLE + args[0] + " has been unregistered");
				}}else{
					sender.sendMessage(ChatColor.DARK_PURPLE + "incorrect password or player name");
				}
				}if(args.length >= 3){
						p.sendMessage(ChatColor.DARK_PURPLE + "use /unregister player password");}}
		if(command.getName().equalsIgnoreCase("changepass")){
			if(args.length == 0){
				sender.sendMessage(ChatColor.DARK_PURPLE + "use /changepass player password newpassword");
			}
			if(args.length == 1){
				sender.sendMessage(ChatColor.DARK_PURPLE + "use /changepass player password newpassword");
			}
			if(args.length == 2){
				sender.sendMessage(ChatColor.DARK_PURPLE + "use /changepass player password newpassword");
			}
			if(args.length == 3){
				if(!getConfig().getString(args[0] + ".password").isEmpty()){
					if(getConfig().getString(args[0] + ".password").equals(args[1])){
						getConfig().set(args[0] + ".password", args[2]);
						saveConfig();
						sender.sendMessage(ChatColor.GREEN + "Password " + args[1] + " of " + args[0] + " has been changed to " + args[2]);
					}}else{
						sender.sendMessage(ChatColor.DARK_PURPLE + "incorrect password or player name");
					}
					}if(args.length >= 4){
						p.sendMessage(ChatColor.DARK_PURPLE + "use /changepass player password newpassword");}}
		
if(command.getName().equalsIgnoreCase("login")){
	if(args.length == 0){
		sender.sendMessage(ChatColor.DARK_PURPLE + "use /login password");
	}if(args.length == 1){
		if(getConfig().contains(p.getName() + ".password")){
		if(getConfig().getString(p.getName() + ".password").equals(args[0])){
			if(loginuser.contains(p.getName())){
				loginuser.remove(p.getPlayer().getName());
				sender.sendMessage(ChatColor.YELLOW + "Login Successful.");
				getConfig().set(p.getAddress().getHostName().replace(".", ",") + ".name", p.getName());
				saveConfig();
			}
			else{sender.sendMessage(ChatColor.YELLOW + "You are already logged in.");}}
		}else{p.sendMessage(ChatColor.DARK_PURPLE + "you are not registred on this server, please do");
		}}if(args.length >= 2){p.sendMessage(ChatColor.DARK_PURPLE + "You put to many passwords put just one");
		}}return true;}}
