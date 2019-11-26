package com.rey;

import java.util.LinkedList;

import com.yieldlab.util.BoundedBlockingQueue;

public class BoundedBlockingQueueImpl implements BoundedBlockingQueue {

	private LinkedList<Object> queue;
	private long capacity;
	private static final long DEFAULT_CAPACITY = 10;

	private BoundedBlockingQueueImpl(long capacity) {
		this.capacity = capacity;
		queue = new LinkedList<Object>();
	}

	public BoundedBlockingQueueImpl() {
		this(DEFAULT_CAPACITY);
	}

	@Override
	public synchronized void put(Object element) throws InterruptedException {

		while (this.getSize() >= this.capacity) {
			wait();
		}
//		System.out.println("put: " + element);
		queue.offer(element);
		notify();
	}

	@Override
	public synchronized Object get() throws InterruptedException {

		while (this.getSize() <= 0) {
			wait();
		}
		Object element = queue.poll();
//		System.out.println("get: " + element);
		notify();

		return element;
	}

	public synchronized long getSize() {
//		System.out.println("current queue size: " + this.queue.size());
		return this.queue.size();
	}

	public long getCapacity() {
		return capacity;
	}

	public void setCapacity(long capacity) {
		this.capacity = capacity;
	}
}
