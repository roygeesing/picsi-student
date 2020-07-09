package utils;

import java.util.*;

/**
 * Space bounded priority queue (thread-safe). The elements are ordered by the natural order of the elements.
 * Stores a specified number of elements with high priority.
 * 
 * @author Christoph Stamm
 *
 * @param <E> data type of elements
 */
public class BoundedPQ<E> {
	private int m_capacity;		// maximum number of elements in the priority queue
	private TreeSet<E> m_pq;	// priority queue implemented by a tree set
	
	/**
	 * Creates a bounded priority queue for a maximum of capacity elements
	 * @param capacity
	 */
	public BoundedPQ(int capacity) {
		m_capacity = capacity;
		m_pq = new TreeSet<E>();
	}
	
	/**
	 * Adds a new element e to the priority queue
	 * @param e
	 */
	public synchronized void add(E e) {
		m_pq.add(e);
		if (m_pq.size() > m_capacity) {
			m_pq.pollFirst();
		}
	}
	
	/**
	 * Returns an element with highest priority
	 * @return
	 */
	public synchronized E last() {
		return m_pq.last();
	}
	
	/**
	 * Removes and returns an element with highest priority
	 * @return
	 */
	public synchronized E pollLast() {
		return m_pq.pollLast();
	}
	
	/**
	 * Returns the number of elements in the priority queue
	 * @return
	 */
	public int size() {
		return m_pq.size();
	}
}
