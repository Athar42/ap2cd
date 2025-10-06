package me.Athar42.ap2cd;

import me.Athar42.ap2cd.command.AudioPlayer2CustomDiscsCommand;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.*;
import java.util.logging.Logger;

public final class AudioPlayer2CustomDiscs extends JavaPlugin {
	static AudioPlayer2CustomDiscs instance;
	
	@Nullable
	private Logger pluginLogger;
    private static Component[] helpMessage;
    private static final LegacyComponentSerializer LegacyComponentAmpersand = LegacyComponentSerializer.legacyAmpersand();

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
		
        helpMessage = new Component[]{
                LegacyComponentAmpersand.deserialize("&8-[&6AudioPlayer2CustomDiscs v"+ this.getPluginMeta().getVersion() +" - Help Page&8]-"),
                LegacyComponentAmpersand.deserialize("&aAuthor&7: ")
                        .append(Component.text("Athar42")
                        .color(TextColor.color(0xd9a334))
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://github.com/Athar42"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open Athar42's GitHub page")))),
                LegacyComponentAmpersand.deserialize("&fGit&0Hub&7: ")
                        .append(Component.text("https://github.com/Athar42/ap2cd")
                        .color(NamedTextColor.BLUE)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://github.com/Athar42/ap2cd"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open AudioPlayer2CustomDiscs' GitHub page"))))
        };
		
        pluginLogger.info("Successfully registered AudioPlayer2CustomDiscs plugin");

	}
	
	@Override
	public void onDisable() {
		CommandAPI.onDisable();
		pluginLogger.info("Successfully unregistered AudioPlayer2CustomDiscs plugin");
	}

    /**
     * Get the help message.
     *
     * @return The text component for the help message.
     */
    public static Component[] getHelpMessage() { return helpMessage; }
}