package org.corderun.cordepvp;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class CordePvP extends JavaPlugin {

    @Override
    public void onEnable() {
        FileConfiguration config = getConfig();
        saveDefaultConfig();
        File playersfile = new File("plugins/CordePvP/players-toggle.yml");
        if(!playersfile.exists()) {
            try {
                playersfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.getCommand("pvp").setExecutor(new PvPCommand(this));
        getCommand("pvp").setTabCompleter(this);
        getCommand("cordepvp").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if(args.length==0){
                    sender.sendMessage("§cCordePvP 1.1.1");
                    sender.sendMessage("§fАвтор: §cCorderuN");
                    sender.sendMessage("§fСпециально для §cMeltarion Network");
                    return true;
                }
                if(args[0].equalsIgnoreCase("reload") && sender.hasPermission("cordepvp.reload")){
                    reloadConfig();
                    sender.sendMessage("da");
                    return true;
                }
                if(args[0].equalsIgnoreCase("reload") && !sender.hasPermission("cordepvp.reload")){
                    sender.sendMessage("net");
                    return true;
                }
                return true;
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tabCompletions = new ArrayList<>();
        if (args.length == 1) {
            tabCompletions.add("named");
            tabCompletions.add("leave");
            tabCompletions.add("help");
            tabCompletions.add("bc");
            tabCompletions.add("toggle");
            tabCompletions.add("invite");
            tabCompletions.add("accept");
        }
        if(args.length == 2 && args[0].equalsIgnoreCase("invite")){
            for(Player player : Bukkit.getOnlinePlayers()){
                tabCompletions.add(player.getName());
            }
        }
        if(args.length == 2 && args[0].equalsIgnoreCase("accept")){
            for(Player player : Bukkit.getOnlinePlayers()){
                tabCompletions.add(player.getName());
            }
        }

        return tabCompletions;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
