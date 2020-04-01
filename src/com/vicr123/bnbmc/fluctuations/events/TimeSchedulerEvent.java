package com.vicr123.bnbmc.fluctuations.events;

import com.vicr123.bnbmc.fluctuations.Fluctuations;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import tech.cheating.chaireco.IEconomy;

import java.sql.SQLException;

public class TimeSchedulerEvent implements Runnable {
    Fluctuations plugin;
    IEconomy economy;

    long lastDay = -1;
    FileConfiguration config;

    public TimeSchedulerEvent(Fluctuations plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
        this.config = plugin.getConfig();

        config.addDefault("interestRate", 0.005);

        World world = plugin.getServer().getWorld("world");
        lastDay = world.getFullTime() / 24000;
    }

    @Override
    public void run() {
        World world = plugin.getServer().getWorld("world");
        long currentDay = world.getFullTime() / 24000;
        if (currentDay != lastDay) compoundInterest();
    }

    public void compoundInterest() {
        World world = plugin.getServer().getWorld("world");
        lastDay = world.getFullTime() / 24000;

        double interestRate = config.getDouble("interestRate");
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            try {
                int balance = economy.getBalance(player);
                if (balance <= 0) continue; //If for some reason the player has an overdraft, don't do anything

                int interest = (int) ((float) balance * interestRate);
                economy.deposit(player, interest, "Interest Credit");

                player.sendMessage(ChatColor.GREEN + "Thanks for playing! An interest payment of " + IEconomy.getDollarValue(interest) + " has been credited to your account!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
