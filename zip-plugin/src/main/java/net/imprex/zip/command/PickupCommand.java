package net.imprex.zip.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.imprex.zip.Backpack;
import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.config.translation.Message;

public class PickupCommand extends BackpackSubCommand {

	public PickupCommand(BackpackPlugin plugin) {
		super(plugin, Message.CommandHelpPickup, "zeroinventoryproblems.pickup", "pickup");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Player player = this.isPlayer(sender);
		if (player == null) {
			return;
		}

		Backpack backpack = this.checkIfHoldingBackpack(player);
		if (backpack == null) {
			this.translation.send(player, Message.YouNeedToHoldABackpackInYourHand);
			return;
		}

		if (!backpack.hasUnuseableContent()) {
			this.translation.send(player, Message.YourBackpackHasNoUnusableItems);
			return;
		}

		if (backpack.giveUnsueableContent(player)) {
			this.translation.send(player, Message.YouReceivedAllUnusableItems);
		} else {
			this.translation.send(player, Message.YouNeedMoreSpaceInYourInventory);
		}
	}

	@Override
	public void onTabComplete(CommandSender sender, String[] args, List<String> result) {
	}
}