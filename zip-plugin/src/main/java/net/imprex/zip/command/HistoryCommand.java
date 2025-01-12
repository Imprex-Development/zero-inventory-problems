package net.imprex.zip.command;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.imprex.zip.Backpack;
import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.api.ZIPBackpackHistory;
import net.imprex.zip.config.MessageKey;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;

public class HistoryCommand extends BackpackSubCommand {

	public HistoryCommand(BackpackPlugin plugin) {
		super(plugin, MessageKey.CommandHelpPickup, "zeroinventoryproblems.history", "history");
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

		List<ZIPBackpackHistory> historyList = backpack.getHistroy();
		if (historyList.isEmpty()) {
			this.messageConfig.send(player, MessageKey.HistoryIsEmpty);
			return;
		}

		TextComponent component = new TextComponent();
		component.addExtra(new TextComponent(this.messageConfig.getWithoutPrefix(MessageKey.CommandHistorySpacer)));
		component.addExtra("\n");

		for (ZIPBackpackHistory history : backpack.getHistroy()) {
			TextComponent header = new TextComponent(this.messageConfig.getWithoutPrefix(
					MessageKey.CommandHistoryEntryHeader,
					this.plugin.formatDateTime(history.dateTime()),
					Bukkit.getOfflinePlayer(history.player()).getName()));
			component.addExtra(header);
			component.addExtra("\n");
			
			for (Entry<ItemStack, Integer> entry : history.items().entrySet()) {
				ItemStack item = entry.getKey();
				boolean itemAdded = entry.getValue() > 0;

				TextComponent itemComponent = new TextComponent(this.messageConfig.getWithoutPrefix(
						itemAdded
						? MessageKey.CommandHistoryEntryAdded
						: MessageKey.CommandHistoryEntryRemoved,
						entry.getValue(),
						entry.getKey().getType().toString()));

				itemComponent.setHoverEvent(new HoverEvent(Action.SHOW_ITEM, new Item(
						item.getType().getKey().toString(),
						entry.getValue(),
						ItemTag.ofNbt(item.getItemMeta().getAsString()))));

				component.addExtra(itemComponent);
				component.addExtra("\n");
			}
		}

		component.addExtra(new TextComponent(this.messageConfig.getWithoutPrefix(MessageKey.CommandHistorySpacer)));
	}

	@Override
	public void onTabComplete(CommandSender sender, String[] args, List<String> result) {
	}
}