package com.vicr123.bnbmc.fluctuations;

import com.vicr123.bnbmc.fluctuations.events.PlayerDeathHandler;
import com.vicr123.bnbmc.fluctuations.events.TimeSchedulerEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import tech.cheating.chaireco.IEconomy;

public class Fluctuations extends JavaPlugin {
    IEconomy economy;

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {
        economy = this.getServer().getServicesManager().getRegistration(IEconomy.class).getProvider();
        this.getServer().getPluginManager().registerEvents(new PlayerDeathHandler(this), this);
        this.getServer().getScheduler().runTaskTimer(this, new TimeSchedulerEvent(this), 0, 200);

        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
    }

    public IEconomy getEconomy() {
        return economy;
    }
}
