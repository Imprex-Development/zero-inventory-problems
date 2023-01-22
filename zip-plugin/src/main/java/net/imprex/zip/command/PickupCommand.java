package net.imprex.zip.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.imprex.zip.Backpack;
import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.config.MessageKey;

public class PickupCommand extends BackpackSubCommand {

	public PickupCommand(BackpackPlugin plugin) {
		super(plugin, MessageKey.CommandHelpPickup, "zeroinventoryproblems.pickup", "pickup");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Player player = this.isPlayer(sender);
		if (player == null) {
			return;
		}

		Backpack backpack = this.checkIfHoldingBackpack(player);
		if (backpack == null) {
			this.messageConfig.send(player, MessageKey.YouNeedToHoldABackpackInYourHand);
			return;
		}

		if (!backpack.hasUnuseableItem()) {
			this.messageConfig.send(player, MessageKey.YourBackpackHasNoUnusableItems);
			return;
		}

		if (backpack.giveUnsueableItems(player)) {
			this.messageConfig.send(player, MessageKey.YouReceivedAllUnusableItems);
		} else {
			this.messageConfig.send(player, MessageKey.YouNeedMoreSpaceInYourInventory);
		}
	}

	@Override
	public void onTabComplete(CommandSender sender, String[] args, List<String> result) {
	}
}