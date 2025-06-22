package net.imprex.zip.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.imprex.zip.BackpackMigrator;
import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.common.UniqueId;
import net.imprex.zip.config.MessageKey;

public class MigrateCommand extends BackpackSubCommand {

	public MigrateCommand(BackpackPlugin plugin) {
		super(plugin, MessageKey.CommandHelpMigrate, "zeroinventoryproblems.migrate", "migrate");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Player player = this.isPlayer(sender);
		if (player == null) {
			return;
		}

		if (!(args.length == 1 && args[0].equalsIgnoreCase("confirm"))) {
			this.messageConfig.send(sender, MessageKey.CommandMigrateUsage);
			return;
		}
		
		long startTime = System.currentTimeMillis();
		int statisticSuccessful = 0;
		int statisticFailed = 0;

		this.messageConfig.send(sender, MessageKey.CommandMigrateOperationStarted);

		Path folderPath = Path.of(plugin.getDataFolder().getAbsolutePath(), "storage");
		try (Stream<Path> stream = Files.walk(folderPath, 1)) {
			Path[] paths = stream
					.filter(file -> !Files.isDirectory(file))
					.filter(file -> Files.isRegularFile(file))
					.filter(file -> !file.getFileName().toString().endsWith(".json"))
					.toArray(Path[]::new);

			this.messageConfig.send(sender, MessageKey.CommandMigrateOperationFound, paths.length);
			
			for (Path file : paths) {
				try {
					UniqueId id = UniqueId.fromString(file.getFileName().toString());
					if (BackpackMigrator.migrate(folderPath, id)) {
						statisticSuccessful++;
					} else {
						statisticFailed++;
					}
				} catch (Exception e) {
					e.printStackTrace();
					statisticFailed++;
				}
			}

			this.messageConfig.send(sender,
					MessageKey.CommandMigrateOperationDone,
					paths.length,
					statisticSuccessful,
					statisticFailed,
					Math.round((System.currentTimeMillis() - startTime) / 1000));
		} catch (IOException e) {
			e.printStackTrace();
			this.messageConfig.send(sender, MessageKey.CommandMigrateOperationFailed);
		}
	}

	@Override
	public void onTabComplete(CommandSender sender, String[] args, List<String> result) {
	}
}