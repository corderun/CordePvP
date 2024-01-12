package org.corderun.cordepvp;

import dev.geco.gsit.api.GSitAPI;
import nl.marido.deluxecombat.api.DeluxeCombatAPI;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PvPCommand implements CommandExecutor {

    private List<Player> players = new ArrayList<>();

    private Random random = new Random();

    private JavaPlugin plugin;

    public PvPCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = plugin.getConfig();
        File playersfile = new File("plugins/CordePvP/players-toggle.yml");
        FileConfiguration playerstoggle = YamlConfiguration.loadConfiguration(playersfile);
        List<String> bclist = playerstoggle.getStringList("Bc");
        List<String> togglelist = playerstoggle.getStringList("Toggle");
        Player player = (Player) sender;

        if(GSitAPI.isSitting((Player) sender)) {
            sender.sendMessage(config.getString("messages.sitting").replace("&", "§"));
            return true;
        }
        if (!player.getPassengers().isEmpty()) {
            sender.sendMessage(config.getString("messages.sitting").replace("&", "§"));
            return true;
        }

        if (args.length == 0) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("Только игрок может использовать эту команду.");
                return true;
            }

            if (command.getName().equalsIgnoreCase("pvp")) {
                if(((Player) sender).hasMetadata("inviting")){
                    sender.sendMessage(config.getString("messages.already-invite").replace("&", "§"));
                    return true;
                }
                if (players.contains(player)) {
                    player.sendMessage(config.getString("messages.already").replace("&", "§"));
                    return true;
                }
                if (players.size() < 2) {
                    players.add(player);
                    sender.sendMessage(config.getString("messages.pvp-find").replace("&", "§"));

                    for (Player playeronline : Bukkit.getOnlinePlayers()) {
                        if(!bclist.contains(playeronline.getName())){
                            playeronline.sendMessage(config.getString("messages.finding").replace("&", "§").replace("%amount%", String.valueOf(players.size())));
                        }
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            players.remove(sender);
                        }
                    }.runTaskLater(plugin, 6000L);


                    if (players.size() == 2) {
                        Player first = players.get(0);
                        Player second = players.get(1);
                        if(!first.isOnline()){
                            players.remove(first);
                            return true;
                        }
                        if(!second.isOnline()){
                            players.remove(second);
                            return true;
                        }
                        first.sendMessage(config.getString("messages.found").replace("&", "§").replace("%player%", second.getName()));
                        second.sendMessage(config.getString("messages.found").replace("&", "§").replace("%player%", first.getName()));
                        teleportPlayersToRandomLocation();
                    }
                } else {
                    player.sendMessage(config.getString("messages.wait").replace("&", "§"));
                }

                return true;
            }
            return false;
        }

        if(args[0].equalsIgnoreCase("toggle")){
            if(!togglelist.contains(sender.getName())){
                togglelist.add(sender.getName());
                sender.sendMessage(config.getString("messages.toggle-success").replace("&", "§"));
            }else{
                togglelist.remove(sender.getName());
                sender.sendMessage(config.getString("messages.toggle-off").replace("&", "§"));
            }
            playerstoggle.set("Toggle", togglelist);
            try {
                playerstoggle.save(playersfile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        if(args[0].equalsIgnoreCase("bc")){
            if(!bclist.contains(sender.getName())){
                bclist.add(sender.getName());
                sender.sendMessage(config.getString("messages.bc-success").replace("&", "§"));
            }else{
                bclist.remove(sender.getName());
                sender.sendMessage(config.getString("messages.bc-off").replace("&", "§"));
            }
            playerstoggle.set("Bc", bclist);
            try {
                playerstoggle.save(playersfile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }


        if(args[0].equalsIgnoreCase("named")){
            if(((Player) sender).hasMetadata("inviting")){
                sender.sendMessage(config.getString("messages.already-invite").replace("&", "§"));
                return true;
            }
            if (players.contains(player)) {
                player.sendMessage(config.getString("messages.already").replace("&", "§"));
                return true;
            }
            if (players.size() < 2) {
                players.add(player);
                sender.sendMessage(config.getString("messages.pvp-find").replace("&", "§"));
                for (int i = 0; i < Bukkit.getOnlinePlayers().size(); i++) {
                    Player playeronline = players.get(i);
                    if(!togglelist.contains(playeronline.getName())){
                        playeronline.sendMessage(config.getString("messages.finding-named").replace("&", "§").replace("%player%", sender.getName()));
                    }
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        players.remove(sender);
                    }
                }.runTaskLater(plugin, 6000L);

                if (players.size() == 2) {
                    Player first = players.get(0);
                    Player second = players.get(1);
                    if(!first.isOnline()){
                        players.remove(first);
                        return true;
                    }
                    if(!second.isOnline()){
                        players.remove(second);
                        return true;
                    }
                    first.sendMessage(config.getString("messages.found").replace("&", "§").replace("%player%", second.getName()));
                    second.sendMessage(config.getString("messages.found").replace("&", "§").replace("%player%", first.getName()));
                    teleportPlayersToRandomLocation();
                }
            } else {
                player.sendMessage(config.getString("messages.wait").replace("&", "§"));
            }
        }
        if(args[0].equalsIgnoreCase("leave")){
            if(players.contains(sender)) {
                players.remove(sender);
                sender.sendMessage(config.getString("messages.leave").replace("&", "§"));
                return true;
            } else{
                sender.sendMessage(config.getString("messages.not-leave").replace("&", "§"));
                return true;
            }
        }
        if(args[0].equalsIgnoreCase("help")){
            sender.sendMessage(config.getString("messages.help.heading").replace("&", "§"));
            sender.sendMessage(config.getString("messages.help.pvp").replace("&", "§"));
            sender.sendMessage(config.getString("messages.help.named").replace("&", "§"));
            sender.sendMessage(config.getString("messages.help.pvp-player").replace("&", "§"));
            sender.sendMessage(config.getString("messages.help.accept").replace("&", "§"));
            sender.sendMessage(config.getString("messages.help.accept-name").replace("&", "§"));
            sender.sendMessage(config.getString("messages.help.leave").replace("&", "§"));
            sender.sendMessage(config.getString("messages.help.toggle").replace("&", "§"));
            sender.sendMessage(config.getString("messages.help.bc").replace("&", "§"));
            sender.sendMessage(config.getString("messages.help.help").replace("&", "§"));
            return true;
        }
        if(args[0].equalsIgnoreCase("invite") && !args[1].isEmpty()){
            Player target = Bukkit.getPlayer(args[1]);
            if(target == null){
                sender.sendMessage(config.getString("messages.target-offline").replace("&", "§"));
                return true;
            }
            if(target.getName().equals(sender.getName())){
                sender.sendMessage(config.getString("messages.self-invite").replace("&", "§"));
                return true;
            }
            if(togglelist.contains(sender.getName())){
                sender.sendMessage(config.getString("messages.self-toggle").replace("&", "§"));
                return true;
            }
            if(togglelist.contains(target.getName())){
                sender.sendMessage(config.getString("messages.inviter-toggle").replace("&", "§"));
                return true;
            }
            if (players.contains(player)) {
                player.sendMessage(config.getString("messages.already").replace("&", "§"));
                return true;
            }
            if(((Player) sender).hasMetadata("inviting")){
                sender.sendMessage(config.getString("messages.already-invite").replace("&", "§"));
                return true;
            }

            if (target.hasMetadata("invited")) {
                List<String> invitedBy = (List<String>) target.getMetadata("invited").stream()
                        .filter(meta -> meta.getOwningPlugin().getName().equals(plugin.getName()))
                        .findFirst().get().value();
                invitedBy.add(sender.getName());
                target.setMetadata("invited", new FixedMetadataValue(plugin, invitedBy));
            } else {
                List<String> invitedBy = new ArrayList<>();
                invitedBy.add(sender.getName());
                target.setMetadata("invited", new FixedMetadataValue(plugin, invitedBy));
            }

            sender.sendMessage(config.getString("messages.sender-invite").replace("&", "§").replace("%player%", target.getName()));
            target.sendMessage(config.getString("messages.target-invite").replace("&", "§").replace("%player%", sender.getName()));
            ((Player) sender).setMetadata("inviting", new FixedMetadataValue(plugin, true));
            target.setMetadata(sender.getName(), new FixedMetadataValue(plugin, true));
            new BukkitRunnable() {
                @Override
                public void run() {
                    ((Player) sender).removeMetadata("inviting", plugin);

                    target.removeMetadata("invited", plugin);
                    target.removeMetadata(sender.getName(), plugin);
                }
            }.runTaskLater(plugin, 600L);
        }

        if(args[0].equalsIgnoreCase("accept") && args.length == 1){
            if(!((Player) sender).hasMetadata("invited")){
                sender.sendMessage(config.getString("messages.zero-invites").replace("&", "§"));
                return true;
            }
            List<String> invitedBy = (List<String>)  player.getMetadata("invited").stream()
                    .filter(meta -> meta.getOwningPlugin().getName().equals(plugin.getName()))
                    .findFirst().get().value();
            String lastTarget = invitedBy.get(invitedBy.size()-1);

            if(!Bukkit.getPlayer(lastTarget).isOnline()){
                sender.sendMessage(config.getString("target-offline").replace("&", "§"));
                return true;
            }
            teleportPlayersToRandomLocationInvite((Player) sender, Bukkit.getPlayer(lastTarget));
        }
        if(args[0].equalsIgnoreCase("accept") && args.length != 1){
            if(!((Player) sender).hasMetadata("invited")){
                sender.sendMessage(config.getString("messages.zero-invites").replace("&", "§"));
                return true;
            }
            if(!Bukkit.getPlayer(args[1]).isOnline()){
                sender.sendMessage(config.getString("target-offline").replace("&", "§"));
                return true;
            }
            if(!Bukkit.getPlayer(args[1]).hasMetadata("inviting") || !((Player) sender).hasMetadata(Bukkit.getPlayer(args[1]).getName())){
                sender.sendMessage(config.getString("messages.zero-invites"));
                return true;
            }
            if(Bukkit.getPlayer(args[1]).hasMetadata("inviting") && ((Player) sender).hasMetadata(Bukkit.getPlayer(args[1]).getName())){
                teleportPlayersToRandomLocationInvite((Player) sender, Bukkit.getPlayer(args[1]));
            }
        }
        return false;
    }

    private void teleportPlayersToRandomLocationInvite(Player sender, Player target) {
        World world = Bukkit.getWorld("world");
        Location location1, location2;

        do {
            int x1 = random.nextInt(3000) - 1500;
            int z1 = random.nextInt(3000) - 1500;
            int x2 = x1 + 10;
            int z2 = z1;

            int y1 = world.getHighestBlockYAt(x1, z1);
            int y2 = world.getHighestBlockYAt(x2, z2);

            location1 = new Location(world, x1, y1, z1, -90, 0);
            location2 = new Location(world, x2, y2, z2, 90, 0);
        } while (!isSafeLocation(location1) || !isSafeLocation(location2));

        if (Bukkit.getPluginManager().getPlugin("DeluxeCombat") != null) {
            DeluxeCombatAPI dc_api = new DeluxeCombatAPI();
            dc_api.tag(sender, sender, 30);
            dc_api.tag(target, target, 30);
        }
        location1.setY(location1.getY()+1);
        location2.setY(location2.getY()+1);
        sender.teleport(location1);
        target.teleport(location2);
        sender.setGameMode(GameMode.SURVIVAL);
        target.setGameMode(GameMode.SURVIVAL);
        sender.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,100,1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,100,1));

        target.removeMetadata("invited", plugin);
        sender.removeMetadata("invited", plugin);
    }

    private void teleportPlayersToRandomLocation() {
        World world = Bukkit.getWorld("world");
        Location location1, location2;

        do {
            int x1 = random.nextInt(3000) - 1500;
            int z1 = random.nextInt(3000) - 1500;
            int x2 = x1 + 10;
            int z2 = z1;

            int y1 = world.getHighestBlockYAt(x1, z1);
            int y2 = world.getHighestBlockYAt(x2, z2);

            location1 = new Location(world, x1, y1, z1, -90, 0);
            location2 = new Location(world, x2, y2, z2, 90, 0);
        } while (!isSafeLocation(location1) || !isSafeLocation(location2));

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (Bukkit.getPluginManager().getPlugin("DeluxeCombat") != null) {
                DeluxeCombatAPI dc_api = new DeluxeCombatAPI();
                dc_api.tag(player, player, 30);
            }
            Location teleportLocation = i == 0 ? location1 : location2;
            teleportLocation.setY(teleportLocation.getY() + 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,100,1));
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(teleportLocation);
        }

        players.clear();
    }

    private boolean isSafeLocation(Location location) {
        Material feet = location.getBlock().getType();
        Material head = location.clone().add(new Vector(0, 1, 0)).getBlock().getType();
        Material below = location.clone().subtract(new Vector(0, 1, 0)).getBlock().getType();
        boolean safe = feet.isSolid() && !head.isSolid() && below.isSolid() && location.getWorld().getWorldBorder().isInside(location);
        boolean pidorBlock1 = feet != Material.OAK_LEAVES && feet != Material.SPRUCE_LEAVES && feet != Material.BIRCH_LEAVES;
        boolean pidorBlock2 = feet != Material.JUNGLE_LEAVES && feet != Material.ACACIA_LEAVES && feet != Material.DARK_OAK_LEAVES;
        boolean pidorBlock3 = feet != Material.MANGROVE_LEAVES && feet != Material.CHERRY_LEAVES && feet != Material.AZALEA_LEAVES;
        boolean notLava = feet != Material.LAVA && head != Material.LAVA && below != Material.LAVA;
        boolean notFire = feet != Material.FIRE && head != Material.FIRE;
        boolean notVoid = location.getY() > 0;

        return safe && notLava && notFire && notVoid && pidorBlock1 && pidorBlock2 && pidorBlock3;
    }

}
