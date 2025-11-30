package me.Athar42.ap2cd.command.SubCommands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import me.Athar42.ap2cd.AudioPlayer2CustomDiscs;
import me.Athar42.ap2cd.utils.TypeChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SetFLAC extends CommandAPICommand {
    AudioPlayer2CustomDiscs plugin = AudioPlayer2CustomDiscs.getInstance();

	public SetFLAC(AudioPlayer2CustomDiscs plugin) {
		super("setflac");

		this.withFullDescription(NamedTextColor.GRAY + "Change the extension of the audio file to .flac");
		this.withUsage("/ap2cd setflac");
		//this.withPermission("audioplayer2customdiscs.setflac");

		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}
	
	private int onCommandPlayer(Player player, CommandArguments arguments) {

		ItemStack item = player.getInventory().getItemInMainHand();
		boolean resultIsMusicDisc = TypeChecker.isMusicDisc(item);
		boolean resultIsHorn = TypeChecker.isGoatHornPlayer(player);
		boolean resultIsHead = TypeChecker.isHeadPlayer(player);

		if (!resultIsMusicDisc && !resultIsHorn && !resultIsHead) {
            player.sendMessage(Component.text("AP2CD - Not holding a music disc, goat horn or player head!"));
			return 0;
		}

        if (!TypeChecker.isConverted(item, plugin)) {
            player.sendMessage(Component.text("AP2CD - This item hasn't been converted yet or isn't a valid item."));
            return 0;
        }

		if (resultIsMusicDisc) {
			ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
			ItemMeta metaDisc = disc.getItemMeta();
			PersistentDataContainer dataDisc = metaDisc.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey("customdiscs", "customdisc");
            String currentValue = dataDisc.get(key, PersistentDataType.STRING);
            if (currentValue != null) {
                String newExtensionValue = currentValue.replaceFirst("\\.[^.]+$", ".flac");
                dataDisc.set(key, PersistentDataType.STRING, newExtensionValue);
                player.sendMessage(Component.text("AP2CD - New disc filename is: " + newExtensionValue + "."));
            } else {
                player.sendMessage(Component.text("AP2CD - No 'customdisc' value found to update."));
            }
			player.getInventory().getItemInMainHand().setItemMeta(metaDisc);
		} else if (resultIsHorn) {
			ItemStack horn = new ItemStack(player.getInventory().getItemInMainHand());
			ItemMeta metaHorn = horn.getItemMeta();
			PersistentDataContainer dataHorn = metaHorn.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey("customdiscs", "customhorn");
            String currentValue = dataHorn.get(key, PersistentDataType.STRING);
            if (currentValue != null) {
                String newExtensionValue = currentValue.replaceFirst("\\.[^.]+$", ".flac");
                dataHorn.set(key, PersistentDataType.STRING, newExtensionValue);
                player.sendMessage(Component.text("AP2CD - New horn filename is: " + newExtensionValue + "."));
            } else {
                player.sendMessage(Component.text("AP2CD - No 'customhorn' value found to update."));
            }
			player.getInventory().getItemInMainHand().setItemMeta(metaHorn);
		} else {
			ItemStack head = new ItemStack(player.getInventory().getItemInMainHand());
			ItemMeta metaHead = head.getItemMeta();
			PersistentDataContainer dataHead = metaHead.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey("customdiscs", "customhead");
            String currentValue = dataHead.get(key, PersistentDataType.STRING);
            if (currentValue != null) {
                String newExtensionValue = currentValue.replaceFirst("\\.[^.]+$", ".flac");
                dataHead.set(key, PersistentDataType.STRING, newExtensionValue);
                player.sendMessage(Component.text("AP2CD - New head filename is: " + newExtensionValue + "."));
            } else {
                player.sendMessage(Component.text("AP2CD - No 'customhead' value found to update."));
            }
			player.getInventory().getItemInMainHand().setItemMeta(metaHead);
		}
		return 1;
	}
	
	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "AP2CD - Only players can use this command : '"+arguments+"'!");
		return 1;
	}

}