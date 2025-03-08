package gg.tjr.mc.xrayalerts;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Settings {
    private final FileConfiguration config;
    private Set<String> alertSet;
    private final String messageFormat;
    private final boolean logAlerts;
    private final String logFormat;
    private final AlertMode mode;
    private final Set<String> monitoredBlocks;

    public enum AlertMode {
        VEIN,
        BLOCK,
    }

    public Settings(@NotNull FileConfiguration config) {
        this.config = config;
        this.messageFormat = config.getString(
            "alert-message",
            "&c&lX-Ray&r &7%player% found &6%count% %item% at (%blockX%, %blockY%, %blockZ%)."
        ).replace("&", "§");
        this.logFormat = config.getString(
            "alert-log",
            "%player% found %count% %item% at (%blockX%, %blockY%, %blockZ%)."
        );
        this.mode = AlertMode.valueOf(config.getString("mode", "BLOCK"));
        this.logAlerts = config.getBoolean("log");
        this.monitoredBlocks = Set.copyOf(config.getStringList("monitored-blocks"));

        ConfigurationSection alertsSection = config.getConfigurationSection("alerts");
        if (alertsSection != null) {
            this.alertSet = alertsSection.getKeys(false).stream()
                                .filter(alertsSection::getBoolean)
                                .collect(Collectors.toSet());
        } else {
            this.alertSet = new HashSet<>();
        }
    }

    public boolean isMonitoredBlock(@NotNull Block block) {
        Material blockMaterial = block.getType();
        return this.monitoredBlocks.contains(blockMaterial.name());
    }

    public boolean inAlertSet(@NotNull Player p) {
        return this.alertSet.contains(p.getUniqueId().toString());
    }

    public boolean toggleAlerts(@NotNull Player p) {
        boolean alertsEnabled = !this.config.getBoolean("alerts." + p.getUniqueId(), false);
        this.config.set("alerts." + p.getUniqueId(), alertsEnabled);
        XRayAlertsPlugin.getInstance().saveConfig();

        if (alertsEnabled) {
            addToAlertSet(p);
        } else {
            removeFromAlertSet(p);
        }
        return alertsEnabled;
    }

    private void addToAlertSet(@NotNull Player p) {
        this.alertSet.add(p.getUniqueId().toString());
    }

    private void removeFromAlertSet(@NotNull Player p) {
        this.alertSet.remove(p.getUniqueId().toString());
    }

    public AlertMode getAlertMode() {
        return this.mode;
    }

    public boolean logAlerts() {
        return this.logAlerts;
    }

    public String getMessageFormat() {
        return this.messageFormat;
    }

    public String getLogFormat() {
        return this.logFormat;
    }

    public boolean isInAlertsSection(Player p) {
        String playerUUID = p.getUniqueId().toString();
        ConfigurationSection alertsSection = this.config.getConfigurationSection("alerts");
        if (alertsSection == null) {
            return false;
        } else {
            return alertsSection.getKeys(false).contains(playerUUID);
        }
    }
}
