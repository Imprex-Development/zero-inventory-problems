package net.imprex.zip.command;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.imprex.zip.Backpack;
import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.config.translation.Message;

public class LinkCommand extends BackpackSubCommand {

	private final Map<Player, ItemStack> linking = new WeakHashMap<>();

	public LinkCommand(BackpackPlugin plugin) {
		super(plugin, Message.CommandHelpLink, "zeroinventoryproblems.link", "link");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Player player = this.isPlayer(sender);
		if (player == null) {
			return;
		}

		if (args.length > 0 && args[0].equalsIgnoreCase("cancel")) {
			if (this.linking.remove(player) != null)  {
				this.translation.send(player, Message.YourBackpackLinkRequestWasCancelled);
			} else {
				this.translation.send(player, Message.YouNeedToLinkABackpackFirst);
			}
			return;
		}

		Backpack backpack = this.checkIfHoldingBackpack(player);
		if (backpack == null) {
			return;
		}

		ItemStack item = player.getInventory().getItemInMainHand();
		ItemStack linking = this.linking.remove(player);
		if (linking == null) {
			this.linking.put(player, item);
			this.translation.send(player, Message.YouCanNowHoldTheBackpackWhichShouldBeLinked);
			return;
		}

		Backpack linkingBackpack = this.backpackHandler.getBackpack(linking);
		if (linkingBackpack == null) {
			this.translation.send(player, Message.ThisShouldNotHappenedPleaseTryToLinkAgain);
			return;
		}

		if (backpack.hasUnuseableContent()) {
			this.translation.send(player, Message.YouHaveUnusableItemsUsePickup);
			return;
		} else if (backpack.hasContent()) {
			this.translation.send(player, Message.YourBackpackIsNotEmpty);
			return;
		} else if (!player.getInventory().contains(linking)) {
			this.translation.send(player, Message.YouNeedToHoldBothBackpacksInYouInventory);
			return;
		} else if (!linkingBackpack.isValid() || !backpack.isValid()) {
			this.translation.send(player, Message.BothBackpacksNeedToBeTheSameType);
			return;
		} else if (!linkingBackpack.getType().getUniqueName().equals(backpack.getType().getUniqueName())) {
			this.translation.send(player, Message.BothBackpacksNeedToBeTheSameType);
			return;
		} else if (linkingBackpack.equals(backpack)) {
			this.translation.send(player, Message.ThisBackpackIsAlreadyLinkedThoThat);
			return;
		}

		linkingBackpack.applyOnItem(item);
		this.translation.send(player, Message.YourBackpackIsNowLinked);
	}

	@Override
	public void onTabComplete(CommandSender sender, String[] args, List<String> result) {
		if (args.length == 0) {
			result.add("cancel");
		} else if (args.length == 1 && "cancel".startsWith(args[0].toLowerCase(Locale.ROOT))) {
			result.add("cancel");
		}
	}
}