package net.imprex.zip.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public class FixedSizeQueue<T> implements Iterable<T> {

	private final LinkedBlockingQueue<T> queue;

	public FixedSizeQueue(int capacity) {
		this.queue = new LinkedBlockingQueue<>(capacity);
	}

	public void add(T item) {
		if (this.queue.remainingCapacity() == 0) {
			this.queue.poll();
		}

		this.queue.offer(item);
	}

	public T poll() {
		return this.queue.poll();
	}

	public int size() {
		return this.queue.size();
	}

	public void clear() {
		this.queue.clear();
	}

	@Override
	public Iterator<T> iterator() {
		return this.queue.iterator();
	}

	public Collection<T> collection() {
		return this.queue;
	}
}
