package net.imprex.zip.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;

/**
 * Helps you to read and write packets. The reading sequence must be the same as the write sequence
 * @author Ingrim4
 */
public class ZipBuffer extends ByteBuf {

	private final ByteBuf buf;

	public ZipBuffer(ByteBuf buf) {
		this.buf = buf;
	}

	/**
	 * Returns variable int
	 * 
	 * @return read variable int
	 */
	public int readVarInt() {
		int out = 0;
		int bytes = 0;
		byte in;
		while (true) {
			in = this.buf.readByte();
			out |= (in & 0x7F) << (bytes++ * 7);
			if (bytes > 5)
				throw new IllegalArgumentException("Attempt to read int bigger than 5 bytes");
			if ((in & 0x80) != 0x80)
				break;
		}
		return out;
	}

	/**
	 * Writes variable int
	 * 
	 * @param value int to write
	 */
	public void writeVarInt(int value) {
		byte part;
		while (true) {
			part = (byte) (value & 0x7F);
			value >>>= 7;
			if (value != 0)
				part |= 0x80;
			this.buf.writeByte(part);
			if (value == 0)
				break;
		}
	}

	/**
	 * Reads a string with max length of {@link Short#MAX_VALUE}
	 * @return string
	 */
	public String readString() {
		return this.readString(Short.MAX_VALUE);
	}

	/**
	 * Reads a string with specific max length
	 * @param max max allowed length of string
	 * @return string
	 */
	public String readString(int length) {
		final int len = this.readVarInt();
		if(len > length)
			throw new IllegalArgumentException("The received encoded string buffer length is longer than maximum allowed (" + len + " > " + length + ")");
		final byte[] bytes = new byte[len];
		this.buf.readBytes(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	/**
	 * Writes string with max length of {@link Short#MAX_VALUE}
	 * @param value string to write
	 */
	public void writeString(String value) {
		final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		if (bytes.length >= Short.MAX_VALUE)
			throw new IllegalArgumentException("Attempt to write a string with a length greater than " + Short.MAX_VALUE + " to ByteBuf!");
		this.writeVarInt(bytes.length);
		this.buf.writeBytes(bytes);
	}

	/**
	 * Reads a byte array which got write byte {@link ZipBuffer#writeByteArray(byte[])}
	 * @return byte[]
	 */
	public byte[] readByteArray() {
		byte array[] = new byte[this.readVarInt()];
		this.readBytes(array);
		return array;
	}

	/**
	 * Writes a byte array
	 * @param array byte[] to write
	 */
	public void writeByteArray(byte[] array) {
		this.writeVarInt(array.length);
		this.writeBytes(array);
	}

	@Override
	public int capacity() {
		return this.buf.capacity();
	}

	@Override
	public ByteBuf capacity(int i) {
		return this.buf.capacity(i);
	}

	@Override
	public int maxCapacity() {
		return this.buf.maxCapacity();
	}

	@Override
	public ByteBufAllocator alloc() {
		return this.buf.alloc();
	}

	@Override
	@SuppressWarnings("deprecation")
	public ByteOrder order() {
		return this.buf.order();
	}

	@Override
	@SuppressWarnings("deprecation")
	public ByteBuf order(ByteOrder byteorder) {
		return this.buf.order(byteorder);
	}

	@Override
	public ByteBuf unwrap() {
		return this.buf.unwrap();
	}

	@Override
	public boolean isDirect() {
		return this.buf.isDirect();
	}

	@Override
	public int readerIndex() {
		return this.buf.readerIndex();
	}

	@Override
	public ByteBuf readerIndex(int i) {
		return this.buf.readerIndex(i);
	}

	@Override
	public int writerIndex() {
		return this.buf.writerIndex();
	}

	@Override
	public ByteBuf writerIndex(int i) {
		return this.buf.writerIndex(i);
	}

	@Override
	public ByteBuf setIndex(int i, int j) {
		return this.buf.setIndex(i, j);
	}

	@Override
	public int readableBytes() {
		return this.buf.readableBytes();
	}

	@Override
	public int writableBytes() {
		return this.buf.writableBytes();
	}

	@Override
	public int maxWritableBytes() {
		return this.buf.maxWritableBytes();
	}

	@Override
	public boolean isReadable() {
		return this.buf.isReadable();
	}

	@Override
	public boolean isReadable(int i) {
		return this.buf.isReadable(i);
	}

	@Override
	public boolean isWritable() {
		return this.buf.isWritable();
	}

	@Override
	public boolean isWritable(int i) {
		return this.buf.isWritable(i);
	}

	@Override
	public ByteBuf clear() {
		return this.buf.clear();
	}

	@Override
	public ByteBuf markReaderIndex() {
		return this.buf.markReaderIndex();
	}

	@Override
	public ByteBuf resetReaderIndex() {
		return this.buf.resetReaderIndex();
	}

	@Override
	public ByteBuf markWriterIndex() {
		return this.buf.markWriterIndex();
	}

	@Override
	public ByteBuf resetWriterIndex() {
		return this.buf.resetWriterIndex();
	}

	@Override
	public ByteBuf discardReadBytes() {
		return this.buf.discardReadBytes();
	}

	@Override
	public ByteBuf discardSomeReadBytes() {
		return this.buf.discardSomeReadBytes();
	}

	@Override
	public ByteBuf ensureWritable(int i) {
		return this.buf.ensureWritable(i);
	}

	@Override
	public int ensureWritable(int i, boolean flag) {
		return this.buf.ensureWritable(i, flag);
	}

	@Override
	public boolean getBoolean(int i) {
		return this.buf.getBoolean(i);
	}

	@Override
	public byte getByte(int i) {
		return this.buf.getByte(i);
	}

	@Override
	public short getUnsignedByte(int i) {
		return this.buf.getUnsignedByte(i);
	}

	@Override
	public short getShort(int i) {
		return this.buf.getShort(i);
	}

	@Override
	public int getUnsignedShort(int i) {
		return this.buf.getUnsignedShort(i);
	}

	@Override
	public int getMedium(int i) {
		return this.buf.getMedium(i);
	}

	@Override
	public int getUnsignedMedium(int i) {
		return this.buf.getUnsignedMedium(i);
	}

	@Override
	public int getInt(int i) {
		return this.buf.getInt(i);
	}

	@Override
	public long getUnsignedInt(int i) {
		return this.buf.getUnsignedInt(i);
	}

	@Override
	public long getLong(int i) {
		return this.buf.getLong(i);
	}

	@Override
	public char getChar(int i) {
		return this.buf.getChar(i);
	}

	@Override
	public float getFloat(int i) {
		return this.buf.getFloat(i);
	}

	@Override
	public double getDouble(int i) {
		return this.buf.getDouble(i);
	}

	@Override
	public ByteBuf getBytes(int i, ByteBuf bytebuf) {
		return this.buf.getBytes(i, bytebuf);
	}

	@Override
	public ByteBuf getBytes(int i, ByteBuf bytebuf, int j) {
		return this.buf.getBytes(i, bytebuf, j);
	}

	@Override
	public ByteBuf getBytes(int i, ByteBuf bytebuf, int j, int k) {
		return this.buf.getBytes(i, bytebuf, j, k);
	}

	@Override
	public ByteBuf getBytes(int i, byte abyte[]) {
		return this.buf.getBytes(i, abyte);
	}

	@Override
	public ByteBuf getBytes(int i, byte abyte[], int j, int k) {
		return this.buf.getBytes(i, abyte, j, k);
	}

	@Override
	public ByteBuf getBytes(int i, ByteBuffer bytebuffer) {
		return this.buf.getBytes(i, bytebuffer);
	}

	@Override
	public ByteBuf getBytes(int i, OutputStream outputstream, int j) throws IOException {
		return this.buf.getBytes(i, outputstream, j);
	}

	@Override
	public int getBytes(int i, GatheringByteChannel gatheringbytechannel, int j) throws IOException {
		return this.buf.getBytes(i, gatheringbytechannel, j);
	}

	@Override
	public ByteBuf setBoolean(int i, boolean flag) {
		return this.buf.setBoolean(i, flag);
	}

	@Override
	public ByteBuf setByte(int i, int j) {
		return this.buf.setByte(i, j);
	}

	@Override
	public ByteBuf setShort(int i, int j) {
		return this.buf.setShort(i, j);
	}

	@Override
	public ByteBuf setMedium(int i, int j) {
		return this.buf.setMedium(i, j);
	}

	@Override
	public ByteBuf setInt(int i, int j) {
		return this.buf.setInt(i, j);
	}

	@Override
	public ByteBuf setLong(int i, long j) {
		return this.buf.setLong(i, j);
	}

	@Override
	public ByteBuf setChar(int i, int j) {
		return this.buf.setChar(i, j);
	}

	@Override
	public ByteBuf setFloat(int i, float f) {
		return this.buf.setFloat(i, f);
	}

	@Override
	public ByteBuf setDouble(int i, double d0) {
		return this.buf.setDouble(i, d0);
	}

	@Override
	public ByteBuf setBytes(int i, ByteBuf bytebuf) {
		return this.buf.setBytes(i, bytebuf);
	}

	@Override
	public ByteBuf setBytes(int i, ByteBuf bytebuf, int j) {
		return this.buf.setBytes(i, bytebuf, j);
	}

	@Override
	public ByteBuf setBytes(int i, ByteBuf bytebuf, int j, int k) {
		return this.buf.setBytes(i, bytebuf, j, k);
	}

	@Override
	public ByteBuf setBytes(int i, byte abyte[]) {
		return this.buf.setBytes(i, abyte);
	}

	@Override
	public ByteBuf setBytes(int i, byte abyte[], int j, int k) {
		return this.buf.setBytes(i, abyte, j, k);
	}

	@Override
	public ByteBuf setBytes(int i, ByteBuffer bytebuffer) {
		return this.buf.setBytes(i, bytebuffer);
	}

	@Override
	public int setBytes(int i, InputStream inputstream, int j) throws IOException {
		return this.buf.setBytes(i, inputstream, j);
	}

	@Override
	public int setBytes(int i, ScatteringByteChannel scatteringbytechannel, int j) throws IOException {
		return this.buf.setBytes(i, scatteringbytechannel, j);
	}

	@Override
	public ByteBuf setZero(int i, int j) {
		return this.buf.setZero(i, j);
	}

	@Override
	public boolean readBoolean() {
		return this.buf.readBoolean();
	}

	@Override
	public byte readByte() {
		return this.buf.readByte();
	}

	@Override
	public short readUnsignedByte() {
		return this.buf.readUnsignedByte();
	}

	@Override
	public short readShort() {
		return this.buf.readShort();
	}

	@Override
	public int readUnsignedShort() {
		return this.buf.readUnsignedShort();
	}

	@Override
	public int readMedium() {
		return this.buf.readMedium();
	}

	@Override
	public int readUnsignedMedium() {
		return this.buf.readUnsignedMedium();
	}

	@Override
	public int readInt() {
		return this.buf.readInt();
	}

	@Override
	public long readUnsignedInt() {
		return this.buf.readUnsignedInt();
	}

	@Override
	public long readLong() {
		return this.buf.readLong();
	}

	@Override
	public char readChar() {
		return this.buf.readChar();
	}

	@Override
	public float readFloat() {
		return this.buf.readFloat();
	}

	@Override
	public double readDouble() {
		return this.buf.readDouble();
	}

	@Override
	public ByteBuf readBytes(int i) {
		return this.buf.readBytes(i);
	}

	@Override
	public ByteBuf readSlice(int i) {
		return this.buf.readSlice(i);
	}

	@Override
	public ByteBuf readBytes(ByteBuf bytebuf) {
		return this.buf.readBytes(bytebuf);
	}

	@Override
	public ByteBuf readBytes(ByteBuf bytebuf, int i) {
		return this.buf.readBytes(bytebuf, i);
	}

	@Override
	public ByteBuf readBytes(ByteBuf bytebuf, int i, int j) {
		return this.buf.readBytes(bytebuf, i, j);
	}

	@Override
	public ByteBuf readBytes(byte abyte[]) {
		return this.buf.readBytes(abyte);
	}

	@Override
	public ByteBuf readBytes(byte abyte[], int i, int j) {
		return this.buf.readBytes(abyte, i, j);
	}

	@Override
	public ByteBuf readBytes(ByteBuffer bytebuffer) {
		return this.buf.readBytes(bytebuffer);
	}

	@Override
	public ByteBuf readBytes(OutputStream outputstream, int i) throws IOException {
		return this.buf.readBytes(outputstream, i);
	}

	@Override
	public int readBytes(GatheringByteChannel gatheringbytechannel, int i) throws IOException {
		return this.buf.readBytes(gatheringbytechannel, i);
	}

	@Override
	public ByteBuf skipBytes(int i) {
		return this.buf.skipBytes(i);
	}

	@Override
	public ByteBuf writeBoolean(boolean flag) {
		return this.buf.writeBoolean(flag);
	}

	@Override
	public ByteBuf writeByte(int i) {
		return this.buf.writeByte(i);
	}

	@Override
	public ByteBuf writeShort(int i) {
		return this.buf.writeShort(i);
	}

	@Override
	public ByteBuf writeMedium(int i) {
		return this.buf.writeMedium(i);
	}

	@Override
	public ByteBuf writeInt(int i) {
		return this.buf.writeInt(i);
	}

	@Override
	public ByteBuf writeLong(long i) {
		return this.buf.writeLong(i);
	}

	@Override
	public ByteBuf writeChar(int i) {
		return this.buf.writeChar(i);
	}

	@Override
	public ByteBuf writeFloat(float f) {
		return this.buf.writeFloat(f);
	}

	@Override
	public ByteBuf writeDouble(double d0) {
		return this.buf.writeDouble(d0);
	}

	@Override
	public ByteBuf writeBytes(ByteBuf bytebuf) {
		return this.buf.writeBytes(bytebuf);
	}

	@Override
	public ByteBuf writeBytes(ByteBuf bytebuf, int i) {
		return this.buf.writeBytes(bytebuf, i);
	}

	@Override
	public ByteBuf writeBytes(ByteBuf bytebuf, int i, int j) {
		return this.buf.writeBytes(bytebuf, i, j);
	}

	@Override
	public ByteBuf writeBytes(byte abyte[]) {
		return this.buf.writeBytes(abyte);
	}

	@Override
	public ByteBuf writeBytes(byte abyte[], int i, int j) {
		return this.buf.writeBytes(abyte, i, j);
	}

	@Override
	public ByteBuf writeBytes(ByteBuffer bytebuffer) {
		return this.buf.writeBytes(bytebuffer);
	}

	@Override
	public int writeBytes(InputStream inputstream, int i) throws IOException {
		return this.buf.writeBytes(inputstream, i);
	}

	@Override
	public int writeBytes(ScatteringByteChannel scatteringbytechannel, int i) throws IOException {
		return this.buf.writeBytes(scatteringbytechannel, i);
	}

	@Override
	public ByteBuf writeZero(int i) {
		return this.buf.writeZero(i);
	}

	@Override
	public int indexOf(int i, int j, byte b0) {
		return this.buf.indexOf(i, j, b0);
	}

	@Override
	public int bytesBefore(byte b0) {
		return this.buf.bytesBefore(b0);
	}

	@Override
	public int bytesBefore(int i, byte b0) {
		return this.buf.bytesBefore(i, b0);
	}

	@Override
	public int bytesBefore(int i, int j, byte b0) {
		return this.buf.bytesBefore(i, j, b0);
	}

	@Override
	public ByteBuf copy() {
		return this.buf.copy();
	}

	@Override
	public ByteBuf copy(int i, int j) {
		return this.buf.copy(i, j);
	}

	@Override
	public ByteBuf slice() {
		return this.buf.slice();
	}

	@Override
	public ByteBuf slice(int i, int j) {
		return this.buf.slice(i, j);
	}

	@Override
	public ByteBuf duplicate() {
		return this.buf.duplicate();
	}

	@Override
	public int nioBufferCount() {
		return this.buf.nioBufferCount();
	}

	@Override
	public ByteBuffer nioBuffer() {
		return this.buf.nioBuffer();
	}

	@Override
	public ByteBuffer nioBuffer(int i, int j) {
		return this.buf.nioBuffer(i, j);
	}

	@Override
	public ByteBuffer internalNioBuffer(int i, int j) {
		return this.buf.internalNioBuffer(i, j);
	}

	@Override
	public ByteBuffer[] nioBuffers() {
		return this.buf.nioBuffers();
	}

	@Override
	public ByteBuffer[] nioBuffers(int i, int j) {
		return this.buf.nioBuffers(i, j);
	}

	@Override
	public boolean hasArray() {
		return this.buf.hasArray();
	}

	@Override
	public byte[] array() {
		return this.buf.array();
	}

	@Override
	public int arrayOffset() {
		return this.buf.arrayOffset();
	}

	@Override
	public boolean hasMemoryAddress() {
		return this.buf.hasMemoryAddress();
	}

	@Override
	public long memoryAddress() {
		return this.buf.memoryAddress();
	}

	@Override
	public String toString(Charset charset) {
		return this.buf.toString(charset);
	}

	@Override
	public String toString(int i, int j, Charset charset) {
		return this.buf.toString(i, j, charset);
	}

	@Override
	public int hashCode() {
		return this.buf.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return this.buf.equals(object);
	}

	@Override
	public int compareTo(ByteBuf bytebuf) {
		return this.buf.compareTo(bytebuf);
	}

	@Override
	public String toString() {
		return this.buf.toString();
	}

	@Override
	public ByteBuf retain(int i) {
		return this.buf.retain(i);
	}

	@Override
	public ByteBuf retain() {
		return this.buf.retain();
	}

	@Override
	public int refCnt() {
		return this.buf.refCnt();
	}

	@Override
	public boolean release() {
		return this.buf.release();
	}

	@Override
	public boolean release(int i) {
		return this.buf.release(i);
	}

	@Override
	public ByteBuf asReadOnly() {
		return this.buf.asReadOnly();
	}

	@Override
	public int forEachByte(ByteProcessor byteProcessor) {
		return this.buf.forEachByte(byteProcessor);
	}

	@Override
	public int forEachByte(int i, int j, ByteProcessor byteProcessor) {
		return this.buf.forEachByte(i, j, byteProcessor);
	}

	@Override
	public int forEachByteDesc(ByteProcessor byteProcessor) {
		return this.buf.forEachByteDesc(byteProcessor);
	}

	@Override
	public int forEachByteDesc(int i, int j, ByteProcessor byteProcessor) {
		return this.buf.forEachByteDesc(i, j, byteProcessor);
	}

	@Override
	public int getBytes(int i, FileChannel fileChannel, long l, int j) throws IOException {
		return this.buf.getBytes(i, fileChannel, l, j);
	}

	@Override
	public CharSequence getCharSequence(int i, int j, Charset charset) {
		return this.buf.getCharSequence(i, j, charset);
	}

	@Override
	public int getIntLE(int i) {
		return this.buf.getIntLE(i);
	}

	@Override
	public long getLongLE(int i) {
		return this.buf.getLongLE(i);
	}

	@Override
	public int getMediumLE(int i) {
		return this.buf.getMediumLE(i);
	}

	@Override
	public short getShortLE(int i) {
		return this.buf.getShortLE(i);
	}

	@Override
	public long getUnsignedIntLE(int i) {
		return this.buf.getUnsignedIntLE(i);
	}

	@Override
	public int getUnsignedMediumLE(int i) {
		return this.buf.getUnsignedMediumLE(i);
	}

	@Override
	public int getUnsignedShortLE(int i) {
		return this.buf.getUnsignedShortLE(i);
	}

	@Override
	public boolean isReadOnly() {
		return this.buf.isReadOnly();
	}

	@Override
	public int readBytes(FileChannel fileChannel, long l, int i) throws IOException {
		return this.buf.readBytes(fileChannel, l, i);
	}

	@Override
	public CharSequence readCharSequence(int i, Charset charset) {
		return this.buf.readCharSequence(i, charset);
	}

	@Override
	public int readIntLE() {
		return this.buf.readIntLE();
	}

	@Override
	public long readLongLE() {
		return this.buf.readLongLE();
	}

	@Override
	public int readMediumLE() {
		return this.buf.readMediumLE();
	}

	@Override
	public ByteBuf readRetainedSlice(int i) {
		return this.buf.readRetainedSlice(i);
	}

	@Override
	public short readShortLE() {
		return this.buf.readShortLE();
	}

	@Override
	public long readUnsignedIntLE() {
		return this.buf.readUnsignedIntLE();
	}

	@Override
	public int readUnsignedMediumLE() {
		return this.buf.readUnsignedMediumLE();
	}

	@Override
	public int readUnsignedShortLE() {
		return this.buf.readUnsignedShortLE();
	}

	@Override
	public ByteBuf retainedDuplicate() {
		return this.buf.retainedDuplicate();
	}

	@Override
	public ByteBuf retainedSlice() {
		return this.buf.retainedSlice();
	}

	@Override
	public ByteBuf retainedSlice(int i, int j) {
		return this.buf.retainedSlice(i, j);
	}

	@Override
	public int setBytes(int i, FileChannel fileChannel, long l, int j) throws IOException {
		return this.buf.setBytes(i, fileChannel, l, j);
	}

	@Override
	public int setCharSequence(int i, CharSequence charSequence, Charset charset) {
		return this.buf.setCharSequence(i, charSequence, charset);
	}

	@Override
	public ByteBuf setIntLE(int i, int j) {
		return this.buf.setIntLE(i, j);
	}

	@Override
	public ByteBuf setLongLE(int i, long l) {
		return this.buf.setLongLE(i, l);
	}

	@Override
	public ByteBuf setMediumLE(int i, int j) {
		return this.buf.setMediumLE(i, j);
	}

	@Override
	public ByteBuf setShortLE(int i, int j) {
		return this.buf.setShortLE(i, j);
	}

	@Override
	public ByteBuf touch() {
		return this.buf.touch();
	}

	@Override
	public ByteBuf touch(Object object) {
		return this.buf.touch(object);
	}

	@Override
	public int writeBytes(FileChannel fileChannel, long l, int i) throws IOException {
		return this.buf.writeBytes(fileChannel, l, i);
	}

	@Override
	public int writeCharSequence(CharSequence charSequence, Charset charset) {
		return this.buf.writeCharSequence(charSequence, charset);
	}

	@Override
	public ByteBuf writeIntLE(int i) {
		return this.buf.writeIntLE(i);
	}

	@Override
	public ByteBuf writeLongLE(long l) {
		return this.buf.writeLongLE(l);
	}

	@Override
	public ByteBuf writeMediumLE(int i) {
		return this.buf.writeMediumLE(i);
	}

	@Override
	public ByteBuf writeShortLE(int i) {
		return this.buf.writeShortLE(i);
	}
}
