package com.rey.concurrent;

import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.yieldlab.util.BoundedBlockingQueue;

public class BoundedBlockingQueueImpl implements BoundedBlockingQueue {
	private LinkedList<Object> queue;
	private ReentrantLock lock;
	private Condition isEmpty;
	private Condition isFull;
	private int capacity;

	public BoundedBlockingQueueImpl() {
		queue = new LinkedList<>();
		lock = new ReentrantLock();
		isEmpty = lock.newCondition();
		isFull = lock.newCondition();
		capacity = 10;
	}

	public static void main(String[] args) {
		BoundedBlockingQueueImpl demo = new BoundedBlockingQueueImpl();
		Runnable producer = new Runnable() {
			@Override
			public void run() {
				try {
					demo.put(new Date());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		Runnable consumer = new Runnable() {
			@Override
			public void run() {
				try {
					demo.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		
		Executors.newScheduledThreadPool(4).scheduleAtFixedRate(producer, 0, 10, TimeUnit.MILLISECONDS);
		Executors.newScheduledThreadPool(4).scheduleAtFixedRate(consumer, 0, 20,TimeUnit.MILLISECONDS);

	}

	@Override
	public void put(Object element) throws InterruptedException {
		lock.lock();
		try {
			while (this.queue.size() >= capacity) {
				isFull.await();
			}
			queue.add(element);
			System.out.println("put: "+element+ " curr size: "+queue.size());
			isEmpty.signal();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Object get() throws InterruptedException {
		Object element = null;
		lock.lock();
		try {
			while (this.queue.size() <= 0) {
				isEmpty.await();
			}
			element = queue.poll();
			System.out.println("get: "+element+" curr size: "+queue.size());
			isFull.signal();
		} finally {
			lock.unlock();
		}

		return element;
	}

}
