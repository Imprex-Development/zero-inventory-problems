package net.imprex.zip.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.imprex.zip.Backpack;
import net.imprex.zip.BackpackHandler;
import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.NmsInstance;
import net.imprex.zip.config.MessageConfig;
import net.imprex.zip.config.MessageKey;

public abstract class BackpackSubCommand {

	protected final BackpackPlugin plugin;
	protected final BackpackHandler backpackHandler;
	protected final MessageConfig messageConfig;

	private final MessageKey helpLine;
	private final String permission;

	private final Set<String> aliases = Collections.newSetFromMap(new HashMap<>());

	public BackpackSubCommand(BackpackPlugin plugin, MessageKey helpLine, String permission, String command, String... aliases) {
		this.plugin = plugin;
		this.backpackHandler = plugin.getBackpackHandler();
		this.messageConfig = plugin.getBackpackConfig().message();
		this.helpLine = helpLine;
		this.permission = permission;

		this.aliases.add(command);
		Stream.of(aliases).forEach(this.aliases::add);
	}

	public abstract void onCommand(CommandSender sender, String[] args);

	public abstract void onTabComplete(CommandSender sender, String[] args, List<String> result);

	public Player isPlayer(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(this.messageConfig.get(MessageKey.NotAConsoleCommand));
			return null;
		}

		return (Player) sender;	
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

	public boolean hasPermission(CommandSender sender) {
		return sender.isOp() || sender.hasPermission(this.permission);
	}

	public boolean isAliases(String alias) {
		return this.aliases.contains(alias);
	}

	public Set<String> getAliases() {
		return Collections.unmodifiableSet(this.aliases);
	}

	public String getPermission() {
		return this.permission;
	}

	public MessageKey getHelpLine() {
		return this.helpLine;
	}
}
