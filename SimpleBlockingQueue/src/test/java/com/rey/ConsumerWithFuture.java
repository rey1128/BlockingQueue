package com.rey;

import java.util.concurrent.Callable;

public class ConsumerWithFuture implements Callable<Object> {

	private BoundedBlockingQueueImpl queue;

	public ConsumerWithFuture(BoundedBlockingQueueImpl queue) {
		this.queue = queue;
	}

	@Override
	public Object call() throws Exception {

		return this.queue.get();

	}

}
