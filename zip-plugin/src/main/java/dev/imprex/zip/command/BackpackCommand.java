package dev.imprex.zip.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import dev.imprex.zip.BackpackPlugin;
import dev.imprex.zip.config.MessageConfig;
import dev.imprex.zip.config.MessageKey;

public class BackpackCommand implements CommandExecutor, TabCompleter {

	public static final String LINE_SEPARATOR = "\n";

	private final MessageConfig messageConfig;

	private final Map<String, BackpackSubCommand> subCommand = new HashMap<>();

	private String helpMessage;

	public BackpackCommand(BackpackPlugin plugin) {
		this.messageConfig = plugin.getBackpackConfig().message();

		this.registerSubCommand(new GiveCommand(plugin));
		this.registerSubCommand(new LinkCommand(plugin));
		this.registerSubCommand(new PickupCommand(plugin));
		this.registerSubCommand(new TypeCommand(plugin));
		this.registerSubCommand(new LoreCommand(plugin));

		this.buildHelpMessage();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 0) {
			String alias = args[0].toLowerCase();
			BackpackSubCommand subCommand = this.subCommand.get(alias);
			if (subCommand != null) {
				if (!subCommand.hasPermission(sender)) {
					this.messageConfig.send(sender, MessageKey.YouDontHaveTheFollowingPermission, subCommand.getPermission());
					return true;
				}

				subCommand.onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
				return true;
			}
		}

		sender.sendMessage(this.helpMessage);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		ArrayList<String> result = new ArrayList<>();
		if (args.length == 0) {
			this.subCommand.keySet().forEach(result::add);
		} else if (args.length == 1) {
			String subCommand = args[0].toLowerCase(Locale.ROOT);
			this.subCommand.keySet().stream().filter(alias -> alias.startsWith(subCommand)).forEach(result::add);
		} else {
			String alias = args[0].toLowerCase(Locale.ROOT);
			BackpackSubCommand subCommand = this.subCommand.get(alias);
			if (subCommand != null) {
				subCommand.onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length), result);
			}
		}
		return result;
	}

	public void registerSubCommand(BackpackSubCommand subCommand) {
		subCommand.getAliases().stream().forEach(alias -> this.subCommand.put(alias, subCommand));
	}

	public void buildHelpMessage() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(this.messageConfig.getWithoutPrefix(MessageKey.CommandHelpStart));
		stringBuilder.append(LINE_SEPARATOR);

		for (BackpackSubCommand subCommand : this.subCommand.values()) {
			stringBuilder.append(this.messageConfig.getWithoutPrefix(subCommand.getHelpLine()));
			stringBuilder.append(LINE_SEPARATOR);
		}

		stringBuilder.append(this.messageConfig.getWithoutPrefix(MessageKey.CommandHelpEnd));
		this.helpMessage = stringBuilder.toString();
	}
}