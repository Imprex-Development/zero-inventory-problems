package net.imprex.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class CommandRegistry {

	private final CommandDispatcher<CommandSender> dispatcher = new CommandDispatcher<>();

	public void register(LiteralArgumentBuilder<CommandSender> literal) {
		this.dispatcher.register(literal);
	}

	public CommandDispatcher<CommandSender> getDispatcher() {
		return this.dispatcher;
	}
}