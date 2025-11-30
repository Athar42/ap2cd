package me.Athar42.ap2cd.command;

import me.Athar42.ap2cd.AudioPlayer2CustomDiscs;
import me.Athar42.ap2cd.command.SubCommands.Convert;
import me.Athar42.ap2cd.command.SubCommands.SetWAV;
import me.Athar42.ap2cd.command.SubCommands.SetMP3;
import me.Athar42.ap2cd.command.SubCommands.SetFLAC;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.executors.CommandArguments;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class AudioPlayer2CustomDiscsCommand extends CommandAPICommand {

    public AudioPlayer2CustomDiscsCommand(AudioPlayer2CustomDiscs plugin) {
		super("audioplayer2customdiscs");

        this.withAliases("ap2cd");
		this.withFullDescription("The AudioPlayer2CustomDiscs command (displaying help).");
		this.withPermission(CommandPermission.NONE);

		this.withSubcommand(new Convert(plugin));
        this.withSubcommand(new SetWAV(plugin));
        this.withSubcommand(new SetMP3(plugin));
        this.withSubcommand(new SetFLAC(plugin));

		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}
	
	private int onCommandPlayer(Player player, CommandArguments arguments) {
        for (Component message : AudioPlayer2CustomDiscs.getHelpMessage()) {
            player.sendMessage(message);
        }
		
		return 1;
	}
	
	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "AP2CD - Only players can use this command : '"+arguments+"'!");
		return 1;
	}

}