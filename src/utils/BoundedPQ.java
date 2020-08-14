package utils;

import java.util.*;

/**
 * Space bounded priority queue. The elements are ordered by the natural order of the elements.
 * Stores a bounded number of elements in order of their priority. Element with lowest priority will be replaced first.
 * 
 * @author Christoph Stamm
 *
 * @param <E> data type of elements
 */
public class BoundedPQ<E extends Comparable<? super E>> implements Iterable<E> {
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
	public void add(E e) {
		m_pq.add(e);
		if (m_pq.size() > m_capacity) {
			m_pq.pollFirst();
		}
	}
	
	/**
	 * Returns an element with highest priority
	 * @return
	 */
	public E getMax() {
		return m_pq.last();
	}
	
	/**
	 * Returns an element with lowest priority
	 * @return
	 */
	public E getMin() {
		return m_pq.first();
	}

	/**
	 * Removes and returns an element with highest priority
	 * @return
	 */
	public E removeMax() {
		return m_pq.pollLast();
	}
	
	/**
	 * Removes and returns an element with lowest priority
	 * @return
	 */
	public E removeMin() {
		return m_pq.pollFirst();
	}
	
	/**
	 * Returns the number of elements in the priority queue
	 * @return
	 */
	public int size() {
		return m_pq.size();
	}

	/**
	 * Returns the maximum number of elements that can be stored in the bounded priority queue
	 * @return
	 */
	public int capacity() {
		return m_capacity;
	}

	@Override
	public Iterator<E> iterator() {
		return m_pq.iterator();
	}
}
