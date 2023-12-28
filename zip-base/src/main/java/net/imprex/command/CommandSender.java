package net.imprex.command;

public interface CommandSender {

	void sendMessage(String message, Object... args);

	boolean hasPermission(String permission);
}
