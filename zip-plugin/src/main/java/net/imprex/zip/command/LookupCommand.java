package net.imprex.zip.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.config.MessageKey;

public class LookupCommand extends BackpackSubCommand {

	/*
	 * TODO
	 * 
	 * Find a way to handle big amount of files
	 * (maybe as search request and queued in background?)
	 * 
	 * zip lookup pack <backpack id> <- info when click actions
	 * zip lookup pack <backpack id> open
	 * zip lookup pack <backpack id> give
	 * zip lookup pack <backpack id> history
	 * zip lookup user <name | uuid> history <- search all ids (when in history)
	 */
	public LookupCommand(BackpackPlugin plugin) {
		super(plugin, MessageKey.CommandHelpType, "zeroinventoryproblems.lookup", "lookup");
	}

	@Override
	public void onTabComplete(CommandSender sender, String[] args, List<String> result) {
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
	}
}