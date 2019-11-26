package com.rey;

import java.util.Date;

public class Producer implements Runnable {

	private BoundedBlockingQueueImpl queue;

	public Producer(BoundedBlockingQueueImpl queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		try {
			this.queue.put(new Date());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
