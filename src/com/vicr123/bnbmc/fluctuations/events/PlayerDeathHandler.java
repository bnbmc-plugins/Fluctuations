package com.vicr123.bnbmc.fluctuations.events;

import com.vicr123.bnbmc.fluctuations.Fluctuations;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import tech.cheating.chaireco.IEconomy;
import tech.cheating.chaireco.exceptions.EconomyBalanceTooLowException;

import java.sql.SQLException;
import java.util.HashMap;

public class PlayerDeathHandler implements Listener {
    Fluctuations plugin;
    IEconomy economy;
    FileConfiguration config;
    HashMap<Player, Integer> pendingPenalties;

    public PlayerDeathHandler(Fluctuations plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
        this.config = plugin.getConfig();
        this.pendingPenalties = new HashMap<>();

        config.addDefault("deathRate", 0.005);
        config.addDefault("deathPenaltyGoesToKiller", false);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        try {
            Player player = e.getEntity();
            int balance = economy.getBalance(player);
            if (balance <= 0) return; //If for some reason the player has an overdraft, don't do anything

            int penalty = (int) ((float) balance * config.getDouble("deathRate"));
            economy.withdraw(player, penalty, "Death Penalty");

            if (player.getKiller() != null && config.getBoolean("deathPenaltyGoesToKiller")) {
                economy.deposit(player.getKiller(), penalty, "Kill Bonus");
                player.getKiller().sendMessage(ChatColor.GREEN + "You killed " + player.getDisplayName() + " and took " + IEconomy.getDollarValue(penalty) + " from them.");
            }

            pendingPenalties.put(player, penalty);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (EconomyBalanceTooLowException ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        if (pendingPenalties.containsKey(e.getPlayer())) {
            e.getPlayer().sendMessage(ChatColor.RED + "You died and lost " + IEconomy.getDollarValue(pendingPenalties.get(e.getPlayer())) + ".");
            pendingPenalties.remove(e.getPlayer());
        }
    }
}
