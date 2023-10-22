package net.imprex.zip.command;

import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.BackpackRegistry;
import net.imprex.zip.BackpackType;
import net.imprex.zip.config.translation.Message;

public class GiveCommand extends BackpackSubCommand {

	private final BackpackRegistry backpackRegistry;

	public GiveCommand(BackpackPlugin plugin) {
		super(plugin, Message.CommandHelpGive, "zeroinventoryproblems.give", "give");
		this.backpackRegistry = plugin.getBackpackRegistry();
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Player player = this.isPlayer(sender);
		if (player == null) {
			return;
		}

		if (args.length < 1) {
			this.translation.send(sender, Message.PleaseEnterABackpackType);
			return;
		}

		BackpackType backpackType = this.backpackRegistry.getTypeByName(args[0]);
		if (backpackType == null) {
			this.translation.send(sender, Message.BackpackTypeWasNotFound, args[0]);
			return;
		}

		if (args.length > 1) {
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null || !target.isOnline()) {
				this.translation.send(sender, Message.NoOnlinePlayerWasFound, args[1]);
				return;
			}

			ItemStack backpack = backpackType.createItem();
			if (target.getInventory().addItem(backpack).isEmpty()) {
				this.translation.send(sender, Message.YouHaveGivenTargetPlayerABackpack, backpackType.getUniqueName(), target.getDisplayName());
			} else {
				this.translation.send(sender, Message.TargetPlayerNeedMoreSpaceInYourInventory, target.getDisplayName());
			}
		} else {
			ItemStack backpack = backpackType.createItem();
			if (player.getInventory().addItem(backpack).isEmpty()) {
				this.translation.send(sender, Message.YouHaveGivenYourselfABackpack, backpackType.getUniqueName());
			} else {
				this.translation.send(sender, Message.YouNeedMoreSpaceInYourInventory, args[0]);
			}
		}
	}

	@Override
	public void onTabComplete(CommandSender sender, String[] args, List<String> result) {
		if (args.length == 0) {
			this.backpackRegistry.getType().forEach(type -> result.add(type.getUniqueName()));
		} else if (args.length == 1) {
			String search = args[0].toLowerCase(Locale.ROOT);
			this.backpackRegistry.getType().stream()
				.filter(type -> type.getUniqueName().startsWith(search))
				.forEach(type -> result.add(type.getUniqueName()));
		} else if (args.length == 2) {
			String search = args[1].toLowerCase(Locale.ROOT);
			Bukkit.getOnlinePlayers().stream()
				.filter(player -> player.getDisplayName().toLowerCase(Locale.ROOT).startsWith(search))
				.forEach(player -> result.add(player.getName()));
		}
	}
}