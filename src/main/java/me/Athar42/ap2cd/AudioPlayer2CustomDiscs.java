package me.Athar42.ap2cd;

import me.Athar42.ap2cd.command.AudioPlayer2CustomDiscsCommand;
import me.Athar42.ap2cd.listener.AutoConvertJukeBox;
import me.Athar42.ap2cd.listener.AutoConvertHeadPlay;
import me.Athar42.ap2cd.utils.AP2CDUtils;
import me.Athar42.ap2cd.utils.TypeChecker;
import me.Athar42.ap2cd.utils.UpdateChecker;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;

import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseItem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class AudioPlayer2CustomDiscs extends JavaPlugin {
	static AudioPlayer2CustomDiscs instance;
	private static final int CONFIG_VERSION = 1;

	// For my own memo (Athar) - Implemented during development cycle where keys changed, isn't needed for now by could be in the future, so keeping it just in case.
	// Mapping Keys/Values for when a default value need to be overridden upon update.
	// If a key format changed at any given config-version, we must declare it there.
	// Example: if "automaticConvert" changed format at version 2, we need a line like this one :
	// 2, List.of("automaticConvert")
	// The "Integer" is the targeted config-version, and the List is the key to overwrite the value with the new default from the config.yml file (so we didn't keep the user value, by safety due to major change).
	private static final Map<Integer, List<String>> MIGRATION_EXCLUDED_KEYS = Map.of(
			//2, List.of("automaticConvert")
    );

	@Nullable
	private UpdateChecker updateChecker;
    private Logger pluginLogger;
    private static Component[] helpMessage;
    private static final LegacyComponentSerializer LegacyComponentAmpersand = LegacyComponentSerializer.legacyAmpersand();
    private static boolean debugMode = false;
    private static boolean automaticConvertMode = false;

	@Override
	public void onLoad() {
		AudioPlayer2CustomDiscs.instance = this;

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();

        CommandAPI.onLoad(new CommandAPIPaperConfig(this).verboseOutput(true).fallbackToLatestNMS(true));
	}

	@Override
	public void onEnable() {
		pluginLogger = getLogger();

        PacketEvents.getAPI().init();
		
		CommandAPI.onEnable();

        new AudioPlayer2CustomDiscsCommand(this).register("audioplayer2customdiscs");

        this.saveDefaultConfig();
        migrateConfig();

        debugMode = getConfig().getBoolean("debugMode", false);
        automaticConvertMode = getConfig().getBoolean("automaticConvert", false);

        String stgAutomaticConvertMode;
        if (automaticConvertMode) {
            stgAutomaticConvertMode = "&aEnabled";
        } else {
            stgAutomaticConvertMode = "&cDisabled";
        }

        helpMessage = new Component[]{
                LegacyComponentAmpersand.deserialize("&8-[&6AudioPlayer2CustomDiscs v"+ this.getPluginMeta().getVersion() +" - Help Page&8]-"),
                LegacyComponentAmpersand.deserialize("&8-[&6Automatic Conversion mode: "+ stgAutomaticConvertMode + "&8]-"),
                LegacyComponentAmpersand.deserialize("&aAuthor&7: ")
                        .append(Component.text("Athar42")
                        .color(TextColor.color(0xd9a334))
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://github.com/Athar42"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open Athar42's GitHub page")))),
                LegacyComponentAmpersand.deserialize("&2Modrinth&7: ")
                        .append(Component.text("https://modrinth.com/project/uGyuzT6k")
                        .color(NamedTextColor.BLUE)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://modrinth.com/project/uGyuzT6k"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open AudioPlayer2CustomDiscs' Modrinth page")))),
                LegacyComponentAmpersand.deserialize("&fGit&0Hub&7: ")
                        .append(Component.text("https://github.com/Athar42/ap2cd")
                        .color(NamedTextColor.BLUE)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://github.com/Athar42/ap2cd"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open AudioPlayer2CustomDiscs' GitHub page")))),
                LegacyComponentAmpersand.deserialize("&aDiscord&7: ")
                        .append(Component.text("https://discord.gg/rJtBRmRFCr")
                        .color(NamedTextColor.BLUE)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://discord.gg/rJtBRmRFCr"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to join our Discord !"))))
        };

        if (automaticConvertMode) {
            getServer().getPluginManager().registerEvents(new AutoConvertJukeBox(), this);
            getServer().getPluginManager().registerEvents(new AutoConvertHeadPlay(), this);

            PacketEvents.getAPI().getEventManager().registerListeners(
                new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
                    @Override
                    public void onPacketReceive(@NonNull PacketReceiveEvent event) {
                        if (event.getPacketType() != PacketType.Play.Client.USE_ITEM) return;

                        Player player = event.getPlayer();
                        if (player == null) return;
                        if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-001 - INSIDE NEW PACKET EVENT."));

                        WrapperPlayClientUseItem wrapper = new WrapperPlayClientUseItem(event);
                        InteractionHand hand = wrapper.getHand();

                        ItemStack item;
                        if (hand == InteractionHand.OFF_HAND) {
                            item = player.getInventory().getItemInOffHand();
                            if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-002A - OFFHAND DETECTED."));
                        } else {
                            item = player.getInventory().getItemInMainHand();
                            if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-002B - MAINHAND DETECTED."));
                        }

                        if (!TypeChecker.isGoatHornPlayer(player)) return;

                        ItemMeta meta = item.getItemMeta();
                        if (meta == null) return;
                        PersistentDataContainer data = meta.getPersistentDataContainer();
                        NamespacedKey convertedKeyRetrieval = new NamespacedKey(instance, "converted");

                        if (data.has(convertedKeyRetrieval, PersistentDataType.BOOLEAN) && Boolean.TRUE.equals(data.get(convertedKeyRetrieval, PersistentDataType.BOOLEAN))) {
                            if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-003 - Already converted!"));
                            return;
                        }

                        Bukkit.getGlobalRegionScheduler().run(instance, scheduledTask -> {
                            boolean mainOffHand;
                            ItemStack item2;
                            if (hand == InteractionHand.OFF_HAND) {
                                item2 = player.getInventory().getItemInOffHand();
                                mainOffHand = false;
                                if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-004A - OFFHAND DETECTED 2."));
                            } else {
                                item2 = player.getInventory().getItemInMainHand();
                                mainOffHand = true;
                                if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-004B - MAINHAND DETECTED 2."));
                            }
                            boolean converted = AP2CDUtils.convertHornIfNeeded(item2, player, instance, mainOffHand);
                            if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-005 - Converted result: " + converted + "."));
                            if (converted) {
                                if (mainOffHand) {
                                    player.getInventory().setItemInMainHand(item2);
                                    player.setCooldown(Material.GOAT_HORN, 0);
                                    if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-006A - Goat horn converted from MAINHAND."));
                                } else {
                                    player.getInventory().setItemInOffHand(item2);
                                    player.setCooldown(Material.GOAT_HORN, 0);
                                    if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-006B - Goat horn converted from OFFHAND."));
                                }
                            } else {
                                if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-007 - Goat horn NOT converted."));
                            }
                        });
                    }
                }
            );
        }

        if (getConfig().getBoolean("update-checker.enabled", true)) {
			String updateChannel = getConfig().getString("update-checker.channel", "release");
			updateChecker = new UpdateChecker(this, updateChannel);
			updateChecker.start();
		}

        getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onJoin(PlayerJoinEvent event) {
				if (updateChecker == null) return;
				String latestVersion = updateChecker.getLatestVersion();
				if (latestVersion == null) return;
				Player player = event.getPlayer();
				if (!player.isOp()) return;

				String currentVersion = getPluginMeta().getVersion();
				Component playerUpdateMessage = LegacyComponentAmpersand.deserialize("&8[&6AudioPlayer-to-CustomDiscs&8]&r &eA new version of CustomDiscs is available: &6" + latestVersion + " &7(current: " + currentVersion + ")");
				Component linkUpdateMessage = LegacyComponentAmpersand.deserialize("&8[&6AudioPlayer-to-CustomDiscs&8]&r &7➜ ").append(Component.text(UpdateChecker.MODRINTH_PAGE_URL).color(NamedTextColor.AQUA).decorate(TextDecoration.UNDERLINED).clickEvent(ClickEvent.openUrl(UpdateChecker.MODRINTH_PAGE_URL)).hoverEvent(HoverEvent.showText(Component.text("Click to open the Modrinth page"))));
				player.sendMessage(playerUpdateMessage);
				player.sendMessage(linkUpdateMessage);
			}
		}, this);
		
        pluginLogger.info("Successfully registered AudioPlayer2CustomDiscs plugin!");

	}
	
	private void migrateConfig() {
		int currentConfigVersion = getConfig().getInt("config-version", 0);
		if (currentConfigVersion == CONFIG_VERSION) return;

		pluginLogger.info("Updating config.yml from version " + currentConfigVersion + " to " + CONFIG_VERSION + "...");

		Map<String, Object> savedValues = new HashMap<>();
		for (String key : getConfig().getKeys(true)) {
			if (!getConfig().isConfigurationSection(key)) {
				savedValues.put(key, getConfig().get(key));
			}
		}

		File configFile = new File(getDataFolder(), "config.yml");
		File backupFile = new File(getDataFolder(), "config.yml.bak");
		if (backupFile.exists()) backupFile.delete();
		configFile.renameTo(backupFile);

		saveResource("config.yml", false);
		reloadConfig();

		List<String> excludedKeys = new ArrayList<>();
		for (int version = currentConfigVersion + 1; version <= CONFIG_VERSION; version++) {
			excludedKeys.addAll(MIGRATION_EXCLUDED_KEYS.getOrDefault(version, List.of()));
		}

		for (Map.Entry<String, Object> configEntry : savedValues.entrySet()) {
			String key = configEntry.getKey();
			if (key.equals("config-version")) continue;
			if (excludedKeys.contains(key)) continue;
			if (getConfig().contains(key)) {
				getConfig().set(key, configEntry.getValue());
			}
		}

		saveConfig();
		pluginLogger.info("Config migration complete. Backup saved as config.yml.bak");
	}

	@Override
	public void onDisable() {
		CommandAPI.onDisable();
        PacketEvents.getAPI().terminate();
		if (updateChecker != null) {
			updateChecker.stop();
		}
        if(pluginLogger != null) pluginLogger.info("Successfully unregistered AudioPlayer2CustomDiscs plugin!");
	}

    public static AudioPlayer2CustomDiscs getInstance() { return instance; }

    public static boolean getDebugMode() { return debugMode; }

    public static boolean getReverseAutomaticConvertMode() {
        return !automaticConvertMode;
    }

    /**
     * Get the help message.
     *
     * @return The text component for the help message.
     */
    public static Component[] getHelpMessage() { return helpMessage; }

}