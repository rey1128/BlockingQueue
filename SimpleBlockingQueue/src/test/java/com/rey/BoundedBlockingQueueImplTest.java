package com.rey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.RepeatedTest;
import org.opentest4j.AssertionFailedError;

public class BoundedBlockingQueueImplTest {

	final int REPEATED_TIMES = 3;
	final String CHECK_MSG = "Checkpoints are %s, checks will run after those miliseconds";

	@RepeatedTest(value = REPEATED_TIMES, name = "get from empty queue, blocking until someone put")
	public void testBlockingGet() throws Exception {
		BoundedBlockingQueueImpl blockingQueue = new BoundedBlockingQueueImpl();
		ExecutorService consumerExecutorService = Executors.newSingleThreadExecutor();

		ScheduledExecutorService producerExecutorService = new ScheduledThreadPoolExecutor(1);
		producerExecutorService.schedule(new Producer(blockingQueue), 2000, TimeUnit.MILLISECONDS);
		Future<Object> result = consumerExecutorService.submit(new ConsumerWithFuture(blockingQueue));

		// exceptions expected, since producer is not running yet, queue is still empty
		assertThrows(AssertionFailedError.class, () -> {
			assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
				result.get();
			});
		});

		assertEquals(0, blockingQueue.getSize());
	}

	@RepeatedTest(value = REPEATED_TIMES, name = "put into full queue, blocking until someone get")
	public void testBlockingPut() throws Exception {
		BoundedBlockingQueueImpl blockingQueue = new BoundedBlockingQueueImpl();
		for (int i = 0; i < blockingQueue.getCapacity(); i++) {
			blockingQueue.put(new Date());
		}

		ScheduledExecutorService consumerExecutorService = new ScheduledThreadPoolExecutor(1);
		consumerExecutorService.schedule(new Consumer(blockingQueue), 2000, TimeUnit.MILLISECONDS);

		// exceptions expected, since consumer is not running yet, queue is still full
		assertThrows(AssertionFailedError.class, () -> {
			assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
				blockingQueue.put(new Date());
			});
		});
		assertEquals(blockingQueue.getCapacity(), blockingQueue.getSize());
	}

	@RepeatedTest(value = REPEATED_TIMES, name = "produce and consume at the same rate")
	public void testQueueSameRate() throws Exception {
		BoundedBlockingQueueImpl blockingQueue = new BoundedBlockingQueueImpl();

		ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);
		scheduledExecutorService.scheduleAtFixedRate(new Producer(blockingQueue), 0, 100, TimeUnit.MILLISECONDS);
		scheduledExecutorService.scheduleAtFixedRate(new Consumer(blockingQueue), 0, 100, TimeUnit.MILLISECONDS);

		// elements in the queue is either 0 or 1
		List<Integer> checkpoints = RandomListUtil.generateNonEmptyRandomList(0, 1000);
		System.out.println(String.format(CHECK_MSG, checkpoints));
		try {
			for (Integer checkpoint : checkpoints) {
				scheduledExecutorService.awaitTermination(checkpoint, TimeUnit.MILLISECONDS);
				assertTrue(blockingQueue.getSize() == 0 || blockingQueue.getSize() == 1);
			}
			scheduledExecutorService.shutdown();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@RepeatedTest(value = REPEATED_TIMES, name = "produce faster than consume")
	public void testQueueProduceFaster() throws Exception {
		BoundedBlockingQueueImpl blockingQueue = new BoundedBlockingQueueImpl();
		long capacity = blockingQueue.getCapacity();
		
		ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);
		scheduledExecutorService.scheduleAtFixedRate(new Producer(blockingQueue), 0, 100, TimeUnit.MILLISECONDS);
		scheduledExecutorService.scheduleAtFixedRate(new Consumer(blockingQueue), 0, 200, TimeUnit.MILLISECONDS);

		// after some time, the queue is between the state full or with one element left
		List<Integer> checkpoints = RandomListUtil.generateNonEmptyRandomList(0, 1000);
		System.out.println(String.format(CHECK_MSG, checkpoints));
		try {
			scheduledExecutorService.awaitTermination(2000, TimeUnit.MILLISECONDS);
			for (Integer checkpoint : checkpoints) {
				scheduledExecutorService.awaitTermination(checkpoint, TimeUnit.MILLISECONDS);
				assertTrue(blockingQueue.getSize() == (capacity-1) || blockingQueue.getSize() == capacity);
			}

			scheduledExecutorService.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@RepeatedTest(value = REPEATED_TIMES, name = "consume faster than produce")
	public void testQueueConsumeFaster() throws Exception {
		BoundedBlockingQueueImpl blockingQueue = new BoundedBlockingQueueImpl();

		ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);
		scheduledExecutorService.scheduleAtFixedRate(new Producer(blockingQueue), 0, 200, TimeUnit.MILLISECONDS);
		scheduledExecutorService.scheduleAtFixedRate(new Consumer(blockingQueue), 0, 100, TimeUnit.MILLISECONDS);
		
		// after some time, the queue is between the state empty or with one element
		List<Integer> checkpoints = RandomListUtil.generateNonEmptyRandomList(0, 1000);
		System.out.println(String.format(CHECK_MSG, checkpoints));
		try {
			scheduledExecutorService.awaitTermination(2000, TimeUnit.MILLISECONDS);
			for (Integer checkpoint : checkpoints) {
				scheduledExecutorService.awaitTermination(checkpoint, TimeUnit.MILLISECONDS);
				assertTrue(blockingQueue.getSize() == 0 || blockingQueue.getSize() == 1);
			}

			scheduledExecutorService.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@RepeatedTest(value = REPEATED_TIMES, name = "consume and produce at same rate, 4 consumers and 1 producer")
	public void testQueueConsumeMore() throws Exception {
		BoundedBlockingQueueImpl blockingQueue = new BoundedBlockingQueueImpl();

		ScheduledExecutorService produccExecutorService = new ScheduledThreadPoolExecutor(1);
		ScheduledExecutorService consumerExecutorService = new ScheduledThreadPoolExecutor(4);
		produccExecutorService.scheduleAtFixedRate(new Producer(blockingQueue), 0, 100, TimeUnit.MILLISECONDS);
		for (int i = 0; i < 4; i++) {
			consumerExecutorService.scheduleAtFixedRate(new Consumer(blockingQueue), i * 100, 100,
					TimeUnit.MILLISECONDS);
		}

		// after some time, the queue is between the state empty or with one element
		List<Integer> checkpoints = RandomListUtil.generateNonEmptyRandomList(0, 1000);
		System.out.println(String.format(CHECK_MSG, checkpoints));
		try {
			for (Integer checkpoint : checkpoints) {
				produccExecutorService.awaitTermination(checkpoint, TimeUnit.MILLISECONDS);
				consumerExecutorService.awaitTermination(checkpoint, TimeUnit.MILLISECONDS);
				assertTrue(blockingQueue.getSize() == 0 || blockingQueue.getSize() == 1);
			}

			consumerExecutorService.shutdown();
			produccExecutorService.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
