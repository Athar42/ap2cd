package me.Athar42.ap2cd;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.Athar42.ap2cd.command.AudioPlayer2CustomDiscsCommand;
import me.Athar42.ap2cd.listener.AutoConvertJukeBox;
import me.Athar42.ap2cd.listener.AutoConvertHeadPlay;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;

import me.Athar42.ap2cd.utils.AP2CDUtils;
import me.Athar42.ap2cd.utils.TypeChecker;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.logging.Logger;

public final class AudioPlayer2CustomDiscs extends JavaPlugin {
	static AudioPlayer2CustomDiscs instance;

	@Nullable
	private Logger pluginLogger;
    private static Component[] helpMessage;
    private static final LegacyComponentSerializer LegacyComponentAmpersand = LegacyComponentSerializer.legacyAmpersand();
    private static boolean debugMode = false;
    private static boolean automaticConvertMode = false;

	@Override
	public void onLoad() {
		AudioPlayer2CustomDiscs.instance = this;

        CommandAPI.onLoad(new CommandAPIPaperConfig(this).verboseOutput(true).fallbackToLatestNMS(true));

		new AudioPlayer2CustomDiscsCommand(this).register("audioplayer2customdiscs");
	}

	@Override
	public void onEnable() {
		pluginLogger = getLogger();
		
		CommandAPI.onEnable();

        this.saveDefaultConfig();

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
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open AudioPlayer2CustomDiscs' GitHub page"))))
        };

        if (automaticConvertMode) {
            getServer().getPluginManager().registerEvents(new AutoConvertJukeBox(), this);
            getServer().getPluginManager().registerEvents(new AutoConvertHeadPlay(), this);

            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.addPacketListener(new PacketAdapter(instance, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ITEM) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    if (event.getPlayer() == null) return;
                    Player player = event.getPlayer();
                    if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-001 - INSIDE NEW PACKET EVENT."));
                    EnumWrappers.Hand hand = null;
                    try {
                        hand = event.getPacket().getEnumModifier(EnumWrappers.Hand.class, 0).readSafely(0);
                    } catch (Exception ignored) {
                        return;
                    }
                    ItemStack item;
                    if (hand == EnumWrappers.Hand.OFF_HAND) {
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
                    NamespacedKey convertedKeyRetrieval = new NamespacedKey(plugin, "converted");

                    if (data.has(convertedKeyRetrieval, PersistentDataType.BOOLEAN) && Boolean.TRUE.equals(data.get(convertedKeyRetrieval, PersistentDataType.BOOLEAN))) {
                        if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-003 - Already converted!"));
                        return;
                    }

                    EnumWrappers.Hand hand2 = hand;
                    Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> {
                        boolean mainOffHand;
                        ItemStack item2;
                        if (hand2 == EnumWrappers.Hand.OFF_HAND) {
                            item2 = player.getInventory().getItemInOffHand();
                            mainOffHand = false;
                            if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-004A - OFFHAND DETECTED 2."));
                        } else {
                            item2 = player.getInventory().getItemInMainHand();
                            mainOffHand = true;
                            if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-004B - MAINHAND DETECTED 2."));
                        }
                        boolean converted = AP2CDUtils.convertHornIfNeeded(item2, player, instance, mainOffHand);
                        if (debugMode) player.sendMessage(Component.text("AP2CD-DEBUG-005 - Converted result: "+ converted + "."));
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
            });
        }
		
        pluginLogger.info("Successfully registered AudioPlayer2CustomDiscs plugin!");

	}
	
	@Override
	public void onDisable() {
		CommandAPI.onDisable();
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