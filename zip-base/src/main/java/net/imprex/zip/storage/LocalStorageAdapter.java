package net.imprex.zip.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import io.netty.buffer.Unpooled;
import net.imprex.zip.config.storage.LocalStorageConfig;
import net.imprex.zip.util.UniqueId;
import net.imprex.zip.util.ZipBuffer;

public class LocalStorageAdapter implements StorageAdapter {

	private final Path backpackFolder;

	public LocalStorageAdapter(LocalStorageConfig config) {
		this.backpackFolder = Path.of(config.getFolderPath());
	}

	@Override
	public CompletableFuture<Void> save(UniqueId id, ZipBuffer buffer) {
		try {
			if (Files.notExists(this.backpackFolder)) {
				Files.createDirectories(this.backpackFolder);
			}

			Path file = this.backpackFolder.resolve(id.toString());
			try (OutputStream outputStream = Files.newOutputStream(file)) {
				byte[] data = new byte[buffer.readableBytes()];
				buffer.readBytes(data);
				outputStream.write(data);
			}

			return CompletableFuture.completedFuture(null);
		} catch (IOException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public CompletableFuture<ZipBuffer> load(UniqueId id) {
		Path file = this.backpackFolder.resolve(id.toString());

		if (!Files.isRegularFile(file)) {
			return CompletableFuture.completedFuture(null);
		}

		try (InputStream inputStream = Files.newInputStream(file)) {
			byte[] data = inputStream.readAllBytes();
			ZipBuffer buffer = new ZipBuffer(Unpooled.wrappedBuffer(data));
			return CompletableFuture.completedFuture(buffer);
		} catch (IOException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public CompletableFuture<Boolean> exist(UniqueId id) {
		Path file = this.backpackFolder.resolve(id.toString());
		return CompletableFuture.completedFuture(Files.isRegularFile(file));
	}

	@Override
	public CompletableFuture<Boolean> connect() {
		return CompletableFuture.completedFuture(true);
	}

	@Override
	public CompletableFuture<Void> disconnect() {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public boolean isConnected() {
		return true;
	}
}
