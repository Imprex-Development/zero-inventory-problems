package net.imprex.zip.command;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.imprex.zip.Backpack;
import net.imprex.zip.BackpackHandler;
import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.NmsInstance;
import net.imprex.zip.config.MessageConfig;
import net.imprex.zip.config.MessageKey;

public class BackpackCommand implements CommandExecutor {

	private final BackpackHandler backpackHandler;
	private final MessageConfig messageConfig;

	private final Map<Player, ItemStack> linking = new WeakHashMap<>();

	public BackpackCommand(BackpackPlugin plugin) {
		this.backpackHandler = plugin.getBackpackHandler();
		this.messageConfig = plugin.getBackpackConfig().message();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(this.messageConfig.get(MessageKey.NotAConsoleCommand));
			return true;
		}

		Player player = (Player) sender;
		if (args.length != 0) {
			String subCommand = args[0].toLowerCase();
			if (subCommand.equals("pickup")) {
				if (sender.hasPermission("zeroinventoryproblems.pickup")) {
					this.handlePickupCommand(player);
				} else {
					this.messageConfig.send(player, MessageKey.YouDontHaveTheFollowingPermission, "zeroinventoryproblems.pickup");
				}
				return true;
			} else if (subCommand.equals("link")) {
				if (sender.hasPermission("zeroinventoryproblems.link")) {
					this.handleLinkCommand(player);
				} else {
					this.messageConfig.send(player, MessageKey.YouDontHaveTheFollowingPermission, "zeroinventoryproblems.link");
				}
				return true;
			}
		}

		this.sendHelp(player);
		return true;
	}

	public void sendHelp(CommandSender sender) {
		sender.sendMessage(String.format("""
				%s
				%s
				%s
				%s
				""",
				this.messageConfig.getWithoutPrefix(MessageKey.CommandHelpStart),
				this.messageConfig.getWithoutPrefix(MessageKey.CommandHelpLine1),
				this.messageConfig.getWithoutPrefix(MessageKey.CommandHelpLine2),
				this.messageConfig.getWithoutPrefix(MessageKey.CommandHelpEnd)));
	}

	public void handlePickupCommand(Player player) {
		Backpack backpack = this.checkIfHoldingBackpack(player);
		if (backpack == null) {
			return;
		}

		if (!backpack.hasUnuseableItem()) {
			this.messageConfig.send(player, MessageKey.YourBackpackHasNoUnuseableItems);
			return;
		}

		if (backpack.giveUnsueableItems(player)) {
			this.messageConfig.send(player, MessageKey.YouRecivedAllUnuseableItems);
		} else {
			this.messageConfig.send(player, MessageKey.YouNeedMoreSpaceInYourInventory);
		}
	}

	public void handleLinkCommand(Player player) {
		Backpack backpack = this.checkIfHoldingBackpack(player);
		if (backpack == null) {
			return;
		}

		ItemStack item = player.getInventory().getItemInMainHand();
		ItemStack linking = this.linking.remove(player);
		if (linking == null) {
			this.linking.put(player, item);
			this.messageConfig.send(player, MessageKey.YouCanNowHoldTheBackpackWichShoudBeLinked);
			return;
		}

		Backpack linkingBackpack = this.backpackHandler.getBackpack(linking);
		if (linkingBackpack == null) {
			this.messageConfig.send(player, MessageKey.ThisShoudNotHappendPleaseTryToLinkAgain);
			return;
		}

		if (backpack.hasUnuseableItem()) {
			this.messageConfig.send(player, MessageKey.YouHaveUnuseableItemsUsePickup);
			return;
		} else if (backpack.hasContent()) {
			this.messageConfig.send(player, MessageKey.YourBackpackIsNotEmpty);
			return;
		} else if (!player.getInventory().contains(linking)) {
			this.messageConfig.send(player, MessageKey.YouNeedToHoldBothBackpacksInYouInventory);
			return;
		} else if (!linkingBackpack.getType().getUniqueName().equals(backpack.getType().getUniqueName())) {
			this.messageConfig.send(player, MessageKey.BothBackpacksNeedToBeTheSameType);
			return;
		} else if (linkingBackpack.equals(backpack)) {
			this.messageConfig.send(player, MessageKey.ThisBackpackIsAlreadyLinkedThoThat);
			return;
		}

		linkingBackpack.applyOnItem(item);
		this.messageConfig.send(player, MessageKey.YourBackpackIsNowLinked);
	}

	public Backpack checkIfHoldingBackpack(Player player) {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item == null || NmsInstance.isAir(item.getType())) {
			this.messageConfig.send(player, MessageKey.YouNeedToHoldABackpackInYourHand);
			return null;
		}

		Backpack backpack = this.backpackHandler.getBackpack(item);
		if (backpack == null) {
			this.messageConfig.send(player, MessageKey.YouNeedToHoldABackpackInYourHand);
			return null;
		}
		return backpack;
	}
}