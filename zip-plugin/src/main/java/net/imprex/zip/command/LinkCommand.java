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
import net.imprex.zip.config.MessageKey;

public class LinkCommand extends BackpackSubCommand {

	private final Map<Player, ItemStack> linking = new WeakHashMap<>();

	public LinkCommand(BackpackPlugin plugin) {
		super(plugin, MessageKey.CommandHelpLink, "zeroinventoryproblems.link", "link");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Player player = this.isPlayer(sender);
		if (player == null) {
			return;
		}

		if (args.length > 0 && args[0].equalsIgnoreCase("cancel")) {
			if (this.linking.remove(player) != null)  {
				this.messageConfig.send(player, MessageKey.YourBackpackLinkRequestWasCancelled);
			} else {
				this.messageConfig.send(player, MessageKey.YouNeedToLinkABackpackFirst);
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
			this.messageConfig.send(player, MessageKey.YouCanNowHoldTheBackpackWhichShouldBeLinked);
			return;
		}

		Backpack linkingBackpack = this.backpackHandler.getBackpack(linking);
		if (linkingBackpack == null) {
			this.messageConfig.send(player, MessageKey.ThisShouldNotHappenedPleaseTryToLinkAgain);
			return;
		}

		if (backpack.hasUnuseableItem()) {
			this.messageConfig.send(player, MessageKey.YouHaveUnusableItemsUsePickup);
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

	@Override
	public void onTabComplete(CommandSender sender, String[] args, List<String> result) {
		if (args.length == 0) {
			result.add("cancel");
		} else if (args.length == 1 && "cancel".startsWith(args[0].toLowerCase(Locale.ROOT))) {
			result.add("cancel");
		}
	}
}