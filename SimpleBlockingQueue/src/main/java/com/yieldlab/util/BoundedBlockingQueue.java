/* Copyright (c) 2003-2012 Yieldlab GmbH, All Rights Reserved
 * This document is strictly confidential and sole property of Yieldlab GmbH, Hamburg, Germany
 */
package com.yieldlab.util;


/**
 * Interface for a fixed-sized first-in-first-out Queue.
 * 
 * @copyright (c) 2003-2012 Yieldlab GmbH, All Rights Reserved
 * @contact dev-contact@yieldlab.de http://www.yieldlab.de
 */
public interface BoundedBlockingQueue
{
	/**
	 * Add an element to the queue.
	 * Attempts to add an element to a full queue should block the calling thread.
	 * 
	 * @param element Element to add to the queue.
     * @throws InterruptedException		
	 */
	public void put(Object element) throws InterruptedException;
	
	/**
	 * Remove an element from the queue.
	 * Attempts to remove an element from an empty queue should block the calling thread.
	 * 
	 * @return The element that is on the queue for the longest time. 
     * @throws InterruptedException	
	 */
	public Object get() throws InterruptedException;
}
