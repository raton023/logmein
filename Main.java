package com.craftilandia.loginmein;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
public class Main extends JavaPlugin implements Listener {
	File archivo = new File(getDataFolder(), "players.yml");;
	FileConfiguration getConfigplayers = YamlConfiguration.loadConfiguration(archivo);;
	@Override
	public void onEnable() {
	getServer().getPluginManager().registerEvents(this, this);
	getConfig().options().copyHeader();
	getConfig().options().copyDefaults(true);
	saveConfig();
	if(!archivo.exists()){
		try {archivo.createNewFile();}
		catch(IOException e) {getServer().getLogger().severe("could not create players.yml");}
	}
	if(getConfig().getBoolean("mysql.use")){
	try { 
		String ip = getConfig().getString("mysql.ip");
		String database = getConfig().getString("mysql.database");
		String user = getConfig().getString("mysql.user");
		String password = getConfig().getString("mysql.password");
		String port = getConfig().getString("mysql.port");
        String url = "jdbc:mysql://"+ip+":"+port+"/"+database; 
        Connection conn = DriverManager.getConnection(url,user,password); 
        
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getTables(null, null, "%", null);
        while (rs.next()) {//the .next is like the for i var...
          if(rs.getString(3).equals("users")){
        	  System.out.println("[LoginMeIn] MySQL the table "+rs.getString(3)+" was found");
        	  rs.close();
          conn.close();
          return;
          }
          
        }
        rs.close();
        Statement st = conn.createStatement(); 
        System.out.println("[LoginMeIn] MySQL the table users was found creating one for you");
        String sql = "CREATE TABLE users (id int NOT NULL AUTO_INCREMENT KEY,player varchar(25) UNIQUE KEY,password varchar(25),motd varchar(55),ip varchar(20),online int);";
        st.executeUpdate(sql);
        st.close();
        conn.close();
	}catch (Exception e) { 
		System.err.println("Login SQL Creating table Error! "); 
        System.err.println(e.getMessage());}}
	
	}
	
	ArrayList<String> loginuser = new ArrayList<String>();

	@EventHandler
	public void entrando(PlayerLoginEvent e){
		loginuser.add(e.getPlayer().getName());
		e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Use /login <password> or /register <password>");
	}
	
	@EventHandler
	public void enreload(PluginEnableEvent e){
		for(Player p : getServer().getOnlinePlayers()){
		loginuser.add(p.getName());
		p.sendMessage(ChatColor.DARK_PURPLE+"Please /login <password> again due a Server reload");
		}
	}
	
	
	@EventHandler
	public void motd(ServerListPingEvent em){
		if(!getConfig().getBoolean("mysql.use")){
			if(getConfigplayers.getString(em.getAddress().getHostName().replace(".", ",") + ".name") == null){
				String motd = getConfig().getString("defaultmotd");
				String setmotd = ChatColor.translateAlternateColorCodes('&', motd);
				em.setMotd(setmotd);
				return;
			}
			String name = getConfigplayers.getString(em.getAddress().getHostName().replace(".", ",") + ".name");

			String getmotd = getConfigplayers.getString(name + ".motd").replace("PLAYER", name);
			String setmotd = ChatColor.translateAlternateColorCodes('&', getmotd);
		em.setMotd(setmotd);
		}
		
		if(getConfig().getBoolean("mysql.use")){
		try { 
			String ip = getConfig().getString("mysql.ip");
			String database = getConfig().getString("mysql.database");
			String user = getConfig().getString("mysql.user");
			String passwd = getConfig().getString("mysql.password");
			String port = getConfig().getString("mysql.port");
            String url = "jdbc:mysql://"+ip+":"+port+"/"+database; 
            Connection conn = DriverManager.getConnection(url,user,passwd); 
            PreparedStatement tomar = conn.prepareStatement("SELECT player,motd,ip FROM `users` WHERE ip=?;");
          //can be player=?,iteminhand=?coins=?,onlineoffline=?;  nameoftable=?
          tomar.setString(1, em.getAddress().getHostName());// ... tomar.setString(2, p.getIteminhand)
          ResultSet obtenido = tomar.executeQuery();//pone en un como array los resultados
          obtenido.next();//de lo mismo que el de arriba
          String laip = obtenido.getString("ip");
          String jugador = obtenido.getString("player");
          if(em.getAddress().getHostName().equals(laip)){
        	  String motd = obtenido.getString("motd").replace("PLAYER", jugador);
      		String setmotd = ChatColor.translateAlternateColorCodes('&', motd);
      	em.setMotd(setmotd);
          }
            obtenido.close();
            tomar.close();
            conn.close(); 
		} catch (Exception e) { 
			String motd = getConfig().getString("defaultmotd");
			String setmotd = ChatColor.translateAlternateColorCodes('&', motd);
			em.setMotd(setmotd);
			
}}}
	
	@EventHandler
	public void saliendo(PlayerQuitEvent e){
		
			getConfigplayers.set("saveloc."+e.getPlayer().getName()+".x",e.getPlayer().getLocation().getX());
			getConfigplayers.set("saveloc."+e.getPlayer().getName()+".y",e.getPlayer().getLocation().getY());
			getConfigplayers.set("saveloc."+e.getPlayer().getName()+".z",e.getPlayer().getLocation().getZ());
			try {getConfigplayers.save(archivo);} catch (IOException e1) {e1.printStackTrace();}
			
		
		
		if(getConfig().getBoolean("mysql.use")){
		try { 
			String ip = getConfig().getString("mysql.ip");
			String database = getConfig().getString("mysql.database");
			String user = getConfig().getString("mysql.user");
			String passwd = getConfig().getString("mysql.password");
			String port = getConfig().getString("mysql.port");
            String url = "jdbc:mysql://"+ip+":"+port+"/"+database; 
            Connection conn = DriverManager.getConnection(url,user,passwd); 
          PreparedStatement cambiar = conn.prepareStatement("UPDATE `users` SET online=? WHERE player=?;");
          cambiar.setInt(1, 0);//la clave 
          cambiar.setString(2, e.getPlayer().getName());
          cambiar.executeUpdate();
          cambiar.close();
            conn.close(); 
		} catch (Exception esql) { 
            System.err.println("Login SQL Error on player leaving!"); 
            System.err.println(esql.getMessage()); 
        }}
	}
	
	@Override
	public void onDisable() {
		
			
				for(Player p : getServer().getOnlinePlayers()){
				getConfigplayers.set("saveloc."+p.getPlayer().getName()+".x",p.getPlayer().getLocation().getX());
				getConfigplayers.set("saveloc."+p.getPlayer().getName()+".y",p.getPlayer().getLocation().getY());
				getConfigplayers.set("saveloc."+p.getPlayer().getName()+".z",p.getPlayer().getLocation().getZ());	
				try {getConfigplayers.save(archivo);} catch (IOException e1) {e1.printStackTrace();}
				
			
			
			if(getConfig().getBoolean("mysql.use")){
			try { 
				String ip = getConfig().getString("mysql.ip");
				String database = getConfig().getString("mysql.database");
				String user = getConfig().getString("mysql.user");
				String passwd = getConfig().getString("mysql.password");
				String port = getConfig().getString("mysql.port");
	            String url = "jdbc:mysql://"+ip+":"+port+"/"+database; 
	            Connection conn = DriverManager.getConnection(url,user,passwd); 
	          PreparedStatement cambiar = conn.prepareStatement("UPDATE `users` SET online=? WHERE player=?;");
	          cambiar.setInt(1, 0);//la clave 
	          cambiar.setString(2, p.getPlayer().getName());
	          cambiar.executeUpdate();
	          cambiar.close();
	            conn.close(); 
			} catch (Exception esql) { 
	            System.err.println("Login SQL Error saving on server stop!"); 
	            System.err.println(esql.getMessage()); 
	        }}}}
		
	@EventHandler
	public void norompe(BlockBreakEvent e) {
		if(loginuser.contains(e.getPlayer().getName())){e.setCancelled(true);
		e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "login before breaking blocks");
		}
		}
	@EventHandler
	public void nopone(BlockPlaceEvent e) {
		if(loginuser.contains(e.getPlayer().getName())){e.setCancelled(true);
		e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "login before placing blocks");}
		}
	@EventHandler
	public void nochat(AsyncPlayerChatEvent e){
		if(loginuser.contains(e.getPlayer().getName())){e.setCancelled(true);
		e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + " login before chat");}
		}
	@EventHandler
	public void nodrop(PlayerDropItemEvent e){
		if(loginuser.contains(e.getPlayer().getName())){e.setCancelled(true);
		e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "login before drop items");}
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
			e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "/login <password> or /register <password>");}}}
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
				if(getConfig().getBoolean("mysql.use")){
						try { 
							String ip = getConfig().getString("mysql.ip");
							String database = getConfig().getString("mysql.database");
							String user = getConfig().getString("mysql.user");
							String password = getConfig().getString("mysql.password");
							String port = getConfig().getString("mysql.port");
				            String url = "jdbc:mysql://"+ip+":"+port+"/"+database; 
				            Connection conn = DriverManager.getConnection(url,user,password); 
				            Statement st = conn.createStatement(); 
				            st.executeUpdate("INSERT INTO users (player, password, motd, ip, online) VALUES ('"+ p.getName() +"', '"+ args[0] +"', '&aWelcome Back PLAYER \n&bWe Missed You!', '"+ p.getAddress().getHostName() +"', 1);"); 
				            st.close();
				            conn.close(); 
				            loginuser.remove(p.getPlayer().getName());
				            p.sendMessage(ChatColor.YELLOW+"Now you are Registered!");
				        } catch (Exception e) { 
				        	if(e.getMessage().contains("Duplicate")){
				            	p.sendMessage(ChatColor.DARK_PURPLE+"You are already registered try /login password");
				            	return true;
				            }
				            System.err.println("Login SQL Error! on player registering"); 
				            System.err.println(e.getMessage()); 
				            p.sendMessage(ChatColor.DARK_PURPLE+"Please comment to admin this error: " + e.getMessage());
				        }}
				
				if(!getConfig().getBoolean("mysql.use")){
					if(getConfigplayers.contains(p.getName())){
						p.sendMessage("you are already registered try /login password");
						return true;
					}
					loginuser.remove(p.getPlayer().getName());
					sender.sendMessage(ChatColor.YELLOW + "your passwd is: " + args[0]);
					getConfigplayers.set(p.getName() + ".password", args[0]);
					getConfigplayers.set(p.getName() + ".motd", "&aWelcome Back PLAYER \n&bWe Missed You!");			
					getConfigplayers.set(p.getAddress().getHostName().replace(".", ",") + ".name", p.getName());
					try {getConfigplayers.save(archivo);} catch (IOException e) {e.printStackTrace();}
				}
					}if(args.length >= 2){
						p.sendMessage(ChatColor.DARK_PURPLE + "use /register password");}}
		
		if(command.getName().equalsIgnoreCase("unregister")){
			if(args.length == 0){
				sender.sendMessage(ChatColor.DARK_PURPLE + "use /unregister player password");
			}
			if(args.length == 1){
				sender.sendMessage(ChatColor.DARK_PURPLE + "use /unregister player password");
			}
			if(args.length == 2){
				if(!getConfig().getBoolean("mysql.use")){
					if(getConfigplayers.contains(args[0])){
					if(!getConfigplayers.getString(args[0] + ".password").isEmpty()){
						if(getConfigplayers.getString(args[0] + ".password").equals(args[1])){
							getConfigplayers.set(args[0], null);
							getConfigplayers.set(p.getAddress().getHostName().replace(".", ","), null);
							try {getConfigplayers.save(archivo);
							sender.sendMessage(ChatColor.DARK_PURPLE + args[0] + " has been unregistered");} 
							catch (IOException e) {e.printStackTrace();}
						}else{
							sender.sendMessage(ChatColor.DARK_PURPLE + "incorrect password");}}}
					else{
						sender.sendMessage(ChatColor.DARK_PURPLE + "that player is not registered");
					}
					}
				
				if(getConfig().getBoolean("mysql.use")){
				try { 
					String ip = getConfig().getString("mysql.ip");
					String database = getConfig().getString("mysql.database");
					String user = getConfig().getString("mysql.user");
					String passwd = getConfig().getString("mysql.password");
					String port = getConfig().getString("mysql.port");
		            String url = "jdbc:mysql://"+ip+":"+port+"/"+database; 
		            Connection conn = DriverManager.getConnection(url,user,passwd); 
		            PreparedStatement tomar = conn.prepareStatement("SELECT password FROM `users` WHERE player=?;");
		          //can be player=?,iteminhand=?coins=?,onlineoffline=?;  nameoftable=?
		          tomar.setString(1, args[0]);// ... tomar.setString(2, p.getIteminhand)
		          ResultSet obtenido = tomar.executeQuery();//pone en un como array los resultados
		          obtenido.next();//de lo mismo que el de arriba
		          PreparedStatement cambiar = conn.prepareStatement("DELETE FROM users WHERE player=?;");
		          cambiar.setString(1, args[0]);
		          String clave = obtenido.getString("password");
		          if(args[1].equals(clave)){
		        	  cambiar.executeUpdate();
		        	  p.sendMessage(ChatColor.GREEN+"Player Unregistered!");
		          }else{
		        	  p.sendMessage(ChatColor.DARK_PURPLE+"Wrong password");
		          }
		          cambiar.close();
		            obtenido.close();
		            tomar.close();
		            conn.close(); 
				} catch (Exception e) { 
		            if(e.getMessage().contains("empty")){
		            	p.sendMessage(ChatColor.DARK_PURPLE+"That player is not registered");
		            	return true;
		            }
		            System.err.println("Login SQL Error! on player unregistered"); 
		            System.err.println(e.getMessage()); 
		            p.sendMessage(ChatColor.DARK_PURPLE+"Please comment to admin this error: " + e.getMessage());
		        }
				
				}}if(args.length >= 3){
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
				
				
				}if(args.length == 3){
						if(!getConfig().getBoolean("mysql.use")){
							if(getConfigplayers.contains(args[0])){
							if(!getConfigplayers.getString(args[0] + ".password").isEmpty()){
								if(getConfigplayers.getString(args[0] + ".password").equals(args[1])){
									getConfigplayers.set(args[0] + ".password", args[2]);
									try {getConfigplayers.save(archivo);
									sender.sendMessage(ChatColor.GREEN + "Password " + args[1] + " of " + args[0] + " has been changed to " + args[2]);} 
									catch (IOException e) {e.printStackTrace();}
									
								}else{
									sender.sendMessage(ChatColor.DARK_PURPLE + "incorrect password");
								}}}
							else{
								sender.sendMessage(ChatColor.DARK_PURPLE + "player is not registered");
								
							}
						}
						
						if(getConfig().getBoolean("mysql.use")){
							try { 
								String ip = getConfig().getString("mysql.ip");
								String database = getConfig().getString("mysql.database");
								String user = getConfig().getString("mysql.user");
								String passwd = getConfig().getString("mysql.password");
								String port = getConfig().getString("mysql.port");
					            String url = "jdbc:mysql://"+ip+":"+port+"/"+database; 
					            Connection conn = DriverManager.getConnection(url,user,passwd); 
					            PreparedStatement tomar = conn.prepareStatement("SELECT password FROM `users` WHERE player=?;");
					          //can be player=?,iteminhand=?coins=?,onlineoffline=?;  nameoftable=?
					          tomar.setString(1, args[0]);// ... tomar.setString(2, p.getIteminhand)
					          ResultSet obtenido = tomar.executeQuery();//pone en un como array los resultados
					          obtenido.next();//de lo mismo que el de arriba
					          PreparedStatement cambiar = conn.prepareStatement("UPDATE `users` SET password=? WHERE player=?;");
					          cambiar.setString(1, args[2]);//la clave 
					          cambiar.setString(2, args[0]);
					          String clave = obtenido.getString("password");
					          if(args[1].equals(clave)){
					        	  cambiar.executeUpdate();
					        	  p.sendMessage(ChatColor.GREEN+"Password Changed!");
					          }else{
					        	  p.sendMessage(ChatColor.DARK_PURPLE+"Wrong password");
					          }
					          cambiar.close();
					            obtenido.close();
					            tomar.close();
					            conn.close(); 
							} catch (Exception e) { 
								if(e.getMessage().contains("empty")){
					            	p.sendMessage(ChatColor.DARK_PURPLE+"That player is not registered");
					            	return true;
					            }
					            System.err.println("Login SQL Error! on player changepasswd "); 
					            System.err.println(e.getMessage()); 
					            p.sendMessage(ChatColor.DARK_PURPLE+"Please comment to admin this error: " + e.getMessage());
					        }
								}
						
				}
					if(args.length >= 4){
						p.sendMessage(ChatColor.DARK_PURPLE + "use /changepass player password newpassword");
					}
		}
		
if(command.getName().equalsIgnoreCase("login")){
	if(args.length == 0){
		sender.sendMessage(ChatColor.DARK_PURPLE + "use /login password");
	}if(args.length == 1){
		
		if(!getConfig().getBoolean("mysql.use")){
		if(getConfigplayers.contains(p.getName() + ".password")){
			if(getConfigplayers.getString(p.getName() + ".password").equals(args[0])){
				if(loginuser.contains(p.getName())){
					loginuser.remove(p.getPlayer().getName());
					sender.sendMessage(ChatColor.YELLOW + "Login Successful.");
					int x = getConfigplayers.getInt("saveloc."+p.getName()+".x");
					int y = getConfigplayers.getInt("saveloc."+p.getName()+".y");
					int z = getConfigplayers.getInt("saveloc."+p.getName()+".z");
					p.getPlayer().teleport(new Location(p.getWorld(),x,y,z));
					
					getConfigplayers.set(p.getAddress().getHostName().replace(".", ",") + ".name", p.getName());
					try {getConfigplayers.save(archivo);} catch (IOException e) {e.printStackTrace();}
				}
				else{sender.sendMessage(ChatColor.YELLOW + "You are already logged in.");}
			}			
			else{p.sendMessage(ChatColor.DARK_PURPLE + "Wrong password");}	

			}else{p.sendMessage(ChatColor.DARK_PURPLE + "you are not registred on this server, please do");
			}}
		
		if(getConfig().getBoolean("mysql.use")){
				try { 
					String ip = getConfig().getString("mysql.ip");
					String database = getConfig().getString("mysql.database");
					String user = getConfig().getString("mysql.user");
					String passwd = getConfig().getString("mysql.password");
					String port = getConfig().getString("mysql.port");
		            String url = "jdbc:mysql://"+ip+":"+port+"/"+database; 
		            Connection conn = DriverManager.getConnection(url,user,passwd); 
		            PreparedStatement tomar = conn.prepareStatement("SELECT password FROM `users` WHERE player=?;");
		          //can be player=?,iteminhand=?coins=?,onlineoffline=?;  nameoftable=?
		          tomar.setString(1, p.getPlayer().getName());// ... tomar.setString(2, p.getIteminhand)
		          ResultSet obtenido = tomar.executeQuery();//pone en un como array los resultados
		          obtenido.next();//de lo mismo que el de arriba
		          
		          PreparedStatement cambiar = conn.prepareStatement("UPDATE `users` SET ip=?,online=1 WHERE player=?;");
		          cambiar.setString(1, p.getAddress().getHostName());
		          cambiar.setString(2, p.getPlayer().getName());
		          cambiar.executeUpdate();
		          String clave = obtenido.getString("password");
		          if(args[0].equals(clave)){
		        	  loginuser.remove(p.getPlayer().getName());
			            p.sendMessage(ChatColor.YELLOW+"Now you are logged in!");
			            int x = getConfigplayers.getInt("saveloc."+p.getName()+".x");
						int y = getConfigplayers.getInt("saveloc."+p.getName()+".y");
						int z = getConfigplayers.getInt("saveloc."+p.getName()+".z");
						p.getPlayer().teleport(new Location(p.getWorld(),x,y,z));
		          }else{
		        	  p.sendMessage(ChatColor.DARK_PURPLE+"Wrong password");
		          }
		          cambiar.close();
		            obtenido.close();
		            tomar.close();
		            conn.close(); 
				} catch (Exception e) { 
					if(e.getMessage().contains("empty")){
		            	p.sendMessage(ChatColor.DARK_PURPLE+"You are not registered, Try /register password");
		            	return true;
		            }
		            System.err.println("Login SQL Error! on player login password"); 
		            System.err.println(e.getMessage()); 
		            p.sendMessage(ChatColor.DARK_PURPLE+"Please comment to admin this error: " + e.getMessage());
		        }}}
		if(args.length >= 2){p.sendMessage(ChatColor.DARK_PURPLE + "You put to many passwords put just one");
		}}return true;}}
