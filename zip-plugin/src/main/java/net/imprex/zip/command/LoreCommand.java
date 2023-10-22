package net.imprex.zip.command;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.config.GeneralConfig;
import net.imprex.zip.config.translation.Message;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class LoreCommand extends BackpackSubCommand {

	private final GeneralConfig generalConfig;

	private final String syntaxMessage;

	public LoreCommand(BackpackPlugin plugin) {
		super(plugin, Message.CommandHelpLore, "zeroinventoryproblems.lore", "lore");
		this.generalConfig = plugin.getBackpackConfig();

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(this.translation.getWithoutPrefix(Message.CommandHelpStart));
		stringBuilder.append("\n");
		stringBuilder.append(this.translation.getWithoutPrefix(Message.CommandHelpLoreSyntax));
		stringBuilder.append("\n");
		stringBuilder.append(this.translation.getWithoutPrefix(Message.CommandHelpEnd));
		this.syntaxMessage = stringBuilder.toString();
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Player player = this.isPlayer(sender);
		if (player == null) {
			return;
		}

		if (args.length == 0) {
			sender.sendMessage(this.syntaxMessage);
			return;
		}

		PlayerInventory playerInventory = player.getInventory();
		ItemStack item = playerInventory.getItemInMainHand();
		if (item == null || !this.backpackHandler.isBackpack(item)) {
			this.translation.send(sender, Message.YouNeedToHoldABackpackInYourHand);
			return;
		}

		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();

		boolean apply = switch (args[0]) {
		case "add", "create" -> this.handleAdd(sender, Arrays.copyOfRange(args, 1, args.length), lore);
		case "edit", "change" -> this.handleChange(sender, Arrays.copyOfRange(args, 1, args.length), lore);
		case "del", "delete" -> this.handleDelete(sender, Arrays.copyOfRange(args, 1, args.length), lore);
		case "list" -> this.handleList(sender, args, lore);
		default -> {
			sender.sendMessage(this.syntaxMessage);
			yield false;
		}
		};

		if (apply) {
			meta.setLore(lore);
			item.setItemMeta(meta);
		} else {
			
		}
	}

	public boolean handleAdd(CommandSender sender, String[] args, List<String> lore) {
		if (args.length < 1) {
			sender.sendMessage(this.syntaxMessage);
			return false;
		}

		if (lore.size() + 1 > this.generalConfig.maxLoreCount) {
			this.translation.send(sender, Message.MaxLoreCountReached, this.generalConfig.maxLoreCount);
			return false;
		}

		String message = String.join(" ", args);
		if (!message.startsWith("&")) {
			message = "&r&7" + message;
		}
		message = ChatColor.translateAlternateColorCodes('&', message);
		lore.add(message);
		this.translation.send(sender, Message.LoreLineCreate, lore.size());
		return true;
	}

	public boolean handleChange(CommandSender sender, String[] args, List<String> lore) {
		if (args.length < 2) {
			sender.sendMessage(this.syntaxMessage);
			return false;
		}

		int line = this.readNumber(sender, args[0], 1, lore.size());
		if (line < 1) {
			return false;
		}

		String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		if (!message.startsWith("&")) {
			message = "&r&7" + message;
		}
		message = ChatColor.translateAlternateColorCodes('&', message);
		lore.set(line - 1,  message);
		this.translation.send(sender, Message.LoreLineChange, line);
		return true;
	}

	public boolean handleDelete(CommandSender sender, String[] args, List<String> lore) {
		if (args.length < 1) {
			sender.sendMessage(this.syntaxMessage);
			return false;
		}

		int line = this.readNumber(sender, args[0], 1, lore.size());
		if (line < 1) {
			return false;
		}

		lore.remove(line - 1);
		this.translation.send(sender, Message.LoreLineDelete, line);
		return true;
	}

	public boolean handleList(CommandSender sender, String[] args, List<String> lore) {
		TextComponent component = new TextComponent();
		component.addExtra(this.translation.getWithoutPrefix(Message.CommandLoreStart));
		component.addExtra("\n");

		for (int line = 1; line < lore.size() + 1; line++) {
			String entry = lore.get(line - 1);
			String messageContent = this.translation.getWithoutPrefix(Message.CommandLoreContent, line, entry);
			String messageEdit = this.translation.getWithoutPrefix(Message.CommandLoreButtonEdit);
			String messageEditHover = this.translation.getWithoutPrefix(Message.CommandLoreButtonEditHover);
			String messageDelete = this.translation.getWithoutPrefix(Message.CommandLoreButtonDelete);
			String messageDeleteHover = this.translation.getWithoutPrefix(Message.CommandLoreButtonDeleteHover);

			TextComponent content = new TextComponent(messageContent);
			content.addExtra("\n");

			String editSuggestEntry = entry.replace("§", "&");
			TextComponent editButton = new TextComponent(messageEdit);
			editButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(messageEditHover)));
			editButton.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/zip lore edit %d %s", line, editSuggestEntry)));

			TextComponent deleteButton = new TextComponent(messageDelete);
			deleteButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(messageDeleteHover)));
			deleteButton.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/zip lore delete %d", line)));

			editButton.addExtra(new TextComponent(" "));
			editButton.addExtra(deleteButton);

			content.addExtra("   ");
			content.addExtra(editButton);

			component.addExtra(content);
			component.addExtra("\n");
		}

		component.addExtra(this.translation.getWithoutPrefix(Message.CommandLoreEnd));
		sender.spigot().sendMessage(component);
		return false;
	}

	public int readNumber(CommandSender sender, String argument, int min, int max) {
		int line;
		try {
			line = Integer.valueOf(argument);
		} catch (NumberFormatException e) {
			this.translation.send(sender, Message.PleaseEnterANumber);
			return -1;
		}

		if (line < min || line > max) {
			this.translation.send(sender, Message.EnterANumberBetweenArgsAndArgs, min, max);
			return -1;
		}

		return line;
	}

	@Override
	public void onTabComplete(CommandSender sender, String[] args, List<String> result) {
		if (sender instanceof Player player) {
			if (args.length == 0) {
				result.add("add");
				result.add("edit");
				result.add("delete");
				result.add("list");
			} else if (args.length == 1) {
				String subCommand = args[0].toLowerCase(Locale.ROOT);
				if ("add".startsWith(subCommand)) {
					result.add("add");
				}
				if ("edit".startsWith(subCommand)) {
					result.add("edit");
				}
				if ("delete".startsWith(subCommand)) {
					result.add("delete");
				}
				if ("list".startsWith(subCommand)) {
					result.add("list");
				}
			} else if (args.length == 2) {
				String subCommand = args[0].toLowerCase(Locale.ROOT);
				if (!("edit".startsWith(subCommand) || "delete".startsWith(subCommand))) {
					return;
				}

				PlayerInventory playerInventory = player.getInventory();
				ItemStack item = playerInventory.getItemInMainHand();
				if (item == null || !this.backpackHandler.isBackpack(item)) {
					return;
				}

				String value = args[1];
				ItemMeta meta = item.getItemMeta();
				for (int line = 1; line < meta.getLore().size() + 1; line++) {
					String lineAsString = String.valueOf(line);
					if (lineAsString.startsWith(value)) {
						result.add(lineAsString);
					}
				}
			}
		}
	}
}