package dev.imprex.zip.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import dev.imprex.zip.BackpackPlugin;
import dev.imprex.zip.BackpackRegistry;
import dev.imprex.zip.config.MessageKey;
import dev.imprex.zip.api.ZIPBackpackType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class TypeCommand extends BackpackSubCommand {

	private final TextComponent message = new TextComponent();

	public TypeCommand(BackpackPlugin plugin) {
		super(plugin, MessageKey.CommandHelpType, "zeroinventoryproblems.type", "type");

		message.addExtra(new TextComponent(this.messageConfig.getWithoutPrefix(MessageKey.CommandTypeStart)));
		message.addExtra(BackpackCommand.LINE_SEPARATOR);

		BackpackRegistry backpackRegistry = plugin.getBackpackRegistry();
		for (ZIPBackpackType backpackType : backpackRegistry.getType()) {
			TextComponent component = new TextComponent(this.messageConfig.getWithoutPrefix(MessageKey.CommandTypeContent, backpackType.getUniqueName()));
			component.addExtra(" ");

			String giveText = this.messageConfig.getWithoutPrefix(MessageKey.CommandTypeButtonGive);
			String giveHoverText = this.messageConfig.getWithoutPrefix(MessageKey.CommandTypeButtonGiveHover, backpackType.getUniqueName());

			TextComponent give = new TextComponent(giveText);
			give.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(giveHoverText)));
			give.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/zip give " + backpackType.getUniqueName()));
			component.addExtra(give);

			message.addExtra(component);
			message.addExtra(BackpackCommand.LINE_SEPARATOR);
		}

		message.addExtra(new TextComponent(this.messageConfig.getWithoutPrefix(MessageKey.CommandTypeEnd)));
	}

	@Override
	public void onTabComplete(CommandSender sender, String[] args, List<String> result) {
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		sender.spigot().sendMessage(this.message);
	}
}