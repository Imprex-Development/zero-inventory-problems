package net.imprex.zip.v2;

import net.imprex.zip.util.UniqueId;
import net.imprex.zip.util.ZipBuffer;

public abstract class BaseBackpack<TItem> {

	private final UniqueId id;

	private final BaseBackpackType type;
	private final String rawType;

	private TItem[] content;
	private BackpackHistory[] history;

	public BaseBackpack(BaseBackpackType type) {
		this.id = UniqueId.get();
		this.type = type;
	}  

	public BaseBackpack(ZipBuffer buffer) {
		this.id = UniqueId.fromByteArray(buffer.readByteArray());

		// read type
		this.rawType = buffer.readString();
		
		// read content
		this.content = this.binaryToItemStack(buffer.readByteArray());
	}

	abstract TItem[] binaryToItemStack(byte[] byteArray);
	abstract byte[] itemStackToBinary(TItem[] itemStack);

	public void save(ZipBuffer buffer) {
		
	}
}