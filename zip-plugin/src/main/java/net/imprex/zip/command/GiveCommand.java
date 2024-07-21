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
import net.imprex.zip.config.MessageKey;

public class GiveCommand extends BackpackSubCommand {

	private final BackpackRegistry backpackRegistry;

	public GiveCommand(BackpackPlugin plugin) {
		super(plugin, MessageKey.CommandHelpGive, "zeroinventoryproblems.give", "give");
		this.backpackRegistry = plugin.getBackpackRegistry();
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (args.length < 1) {
			this.messageConfig.send(sender, MessageKey.PleaseEnterABackpackType);
			return;
		}

		BackpackType backpackType = this.backpackRegistry.getTypeByName(args[0]);
		if (backpackType == null) {
			this.messageConfig.send(sender, MessageKey.BackpackTypeWasNotFound, args[0]);
			return;
		}

		if (args.length > 1) {
			Player target = Bukkit.getPlayerExact(args[1]);
			if (target == null || !target.isOnline()) {
				this.messageConfig.send(sender, MessageKey.NoOnlinePlayerWasFound, args[1]);
				return;
			}

			ItemStack backpack = backpackType.createItem();
			if (target.getInventory().addItem(backpack).isEmpty()) {
				this.messageConfig.send(sender, MessageKey.YouHaveGivenTargetPlayerABackpack, backpackType.getUniqueName(), target.getDisplayName());
			} else {
				this.messageConfig.send(sender, MessageKey.TargetPlayerNeedMoreSpaceInYourInventory, target.getDisplayName());
			}
		} else {
			Player player = this.isPlayer(sender);
			if (player == null) {
				return;
			}
			
			ItemStack backpack = backpackType.createItem();
			if (player.getInventory().addItem(backpack).isEmpty()) {
				this.messageConfig.send(sender, MessageKey.YouHaveGivenYourselfABackpack, backpackType.getUniqueName());
			} else {
				this.messageConfig.send(sender, MessageKey.YouNeedMoreSpaceInYourInventory, args[0]);
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