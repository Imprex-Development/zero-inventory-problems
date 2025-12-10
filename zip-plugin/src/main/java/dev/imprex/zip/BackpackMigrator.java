package dev.imprex.zip;

import java.awt.geom.IllegalPathStateException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;

import io.netty.buffer.Unpooled;
import dev.imprex.zip.common.BPConstants;
import dev.imprex.zip.common.Ingrim4Buffer;
import dev.imprex.zip.common.UniqueId;
import dev.imprex.zip.common.ZIPLogger;

public class BackpackMigrator {
	
	/*
	 * Previous implementation
	 * 
	 * LOAD
	 * read id buffer.readByteArray();
	 * read typeRaw buffer.readString();
	 * read content buffer.readByteArray();
	 * 
	 * SAVE
	 * buffer.writeByteArray(this.id.toByteArray());
	 * buffer.writeString(this.typeRaw);
	 * buffer.writeByteArray(NmsInstance.itemstackToBinary(this.content));
	 */
	
	public static void checkForMigrations(Path folderPath) {
		long startTime = System.currentTimeMillis();
		int statisticSuccessful = 0;
		int statisticFailed = 0;
	
		ZIPLogger.info("Checking for migration data...");

		try (Stream<Path> stream = Files.walk(folderPath, 1)) {
			Path[] paths = stream
					.filter(file -> !Files.isDirectory(file))
					.filter(file -> Files.isRegularFile(file))
					.filter(file -> !file.getFileName().toString().endsWith(".json"))
					.toArray(Path[]::new);
	
			ZIPLogger.info("Migration found " + paths.length + " outdated backpacks.");
			
			if (paths.length == 0) {
				return;
			}
			
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
			
			ZIPLogger.info(String.format("Migration finished. %d/%d backpacks migrated and %d failed to migrate in %d seconds.",
					paths.length,
					statisticSuccessful,
					statisticFailed,
					Math.round((System.currentTimeMillis() - startTime) / 1000)));
		} catch (IOException e) {
			ZIPLogger.error("Error when migrating backpacks", e);
		}
	}

	public static boolean migrate(Path folderPath, UniqueId id) {
		try {
			Path previousFile = folderPath.resolve(id.toString());
			
			if (!Files.isRegularFile(previousFile)) {
				return false;
			}
			
			Ingrim4Buffer buffer;
			try (FileInputStream inputStream = new FileInputStream(previousFile.toFile())) {
				byte[] data = ByteStreams.toByteArray(inputStream);
				buffer = new Ingrim4Buffer(Unpooled.wrappedBuffer(data));
			}
			
			byte[] previousId = buffer.readByteArray();
			String previousRawType = buffer.readString();
			byte[] previousContent = buffer.readByteArray();
			
			JsonObject json = new JsonObject();
			json.addProperty(BPConstants.KEY_VERSION, BPConstants.VERSION);
			json.addProperty(BPConstants.KEY_ID, UniqueId.fromByteArray(previousId).toString());
			json.addProperty(BPConstants.KEY_TYPE_RAW, previousRawType);
			json.add(BPConstants.KEY_INVENTORY, NmsInstance.migrateToJsonElement(previousContent));

			Path newFile = folderPath.resolve(id.toString() + ".json");
			if (Files.exists(newFile)) {
				throw new IllegalPathStateException("File path for migration " + id.toString() + " already exist!");
			}
			
			try (FileOutputStream outputStream = new FileOutputStream(newFile.toFile());
					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
				Backpack.GSON.toJson(json, outputStreamWriter);
			}
			
			Files.deleteIfExists(previousFile);

			ZIPLogger.info("Successful migrated backpack id '" + id.toString() + "'");
			return true;
		} catch (Exception e) {
			ZIPLogger.error("Unable to migrate backpack id '" + id.toString() + "'", e);
		}
		return false;
	}
}
