package net.imprex.zip;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;

import io.netty.buffer.Unpooled;
import net.imprex.zip.common.BPKey;
import net.imprex.zip.common.Ingrim4Buffer;
import net.imprex.zip.common.UniqueId;
import net.imprex.zip.common.ZIPLogger;

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
			json.addProperty(BPKey.VERSION, 2);
			json.addProperty(BPKey.ID, UniqueId.fromByteArray(previousId).toString());
			json.addProperty(BPKey.TYPE_RAW, previousRawType);
			json.add(BPKey.INVENTORY, NmsInstance.migrateToJsonElement(previousContent));

			Path newFile = folderPath.resolve(id.toString() + ".json");
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
