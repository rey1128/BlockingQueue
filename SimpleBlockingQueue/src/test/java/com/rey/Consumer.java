package com.rey;

public class Consumer implements Runnable {

	private BoundedBlockingQueueImpl queue;

	public Consumer(BoundedBlockingQueueImpl queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		try {
			this.queue.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
