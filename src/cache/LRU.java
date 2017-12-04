package cache;

import java.util.HashMap;
import java.util.Map;

public class LRU<K, T> {

	public static class Node<T> {
		Node<T> prev = null;
		Node<T> next = null;
		T value;
		public Node(T v) {
			this.value = v;
		}
		public Node() {}
	}
	
	private Map<K, Node<T>> map = new HashMap<>();
	private Node<T> head = new Node<T>();
	private Node<T> tail = new Node<T>();
	int SIZE = 3;
	
	public void put(K k, T v) {
		Node<T> node = null;
		if(!map.containsKey(k)) {
			node = new Node<T>(v);
			removeTail();
			map.put(k, node);
			node.next = head.next;
			if(head.next != null)
				head.next.prev = node;
			head.next = node;
			
			if(tail.next == null)
				tail.next = node;
		}
	}
	
	public void removeTail(){
		if(map.size() < SIZE) 
			return;
		
		Node<T> tmp = tail.next;
		tail.next = tmp.prev;
		tmp.prev.next = null;
		
	}
	
	public void moveTop(Node<T> node) {
		if(head.next == node)
			return;
		
		Node<T> tmp = head.next;
		if(head.next != null)
			head.next.prev = node;
		head.next = node;
		
		Node<T> tmpNodeNext = node.next;
		Node<T> tmpNodePrev = node.prev;
		
		if(tmpNodePrev != null)
			tmpNodePrev.next = tmpNodeNext;
		if(tmpNodeNext != null)
			tmpNodeNext.prev = tmpNodePrev;
		
		node.next = tmp;
		node.prev = null;
		
		if(tail.next == node) {
			tail.next = tmpNodePrev;
		}
	}
	
	public T get(K k) {
		if(!map.containsKey(k))
			return null;
		
		Node<T> node = map.get(k);
		moveTop(node);
		
		return node.value;
	}
	
	public void remove(K k) {
		if(!map.containsKey(k))
			return;
		
		Node<T> node = map.get(k);
		map.remove(k);
		removeNode(node);
	}
	
	public void removeNode(Node<T> node){
		if(head.next == node && tail.next == node) {
			head.next = null;
			tail.next = null;
			node = null;
			return;
		}
		
		if(head.next == node) {
			head.next = node.next;
			node.next.prev = null;
			node.next = null;
			node = null;
			return;
		}
		if(tail.next == node) {
			tail.next = node.prev;
			node.prev.next = null;
			node.prev = null;
			node = null;
			return;
		}
		
		node.next.prev = node.prev;
		node.prev.next = node.next;
		node.prev = null;
		node.next = null;
		node = null;
	}
	
	public void print(){
		Node<T> tmp = head.next;
		System.out.println();
		while(tmp != null) {
			System.out.print(tmp.value + " - ");
			tmp = tmp.next;
		}
		System.out.println();
		tmp = tail.next;
		while(tmp != null) {
			System.out.print(tmp.value + " - ");
			tmp = tmp.prev;
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
		LRU<String, Integer> lru = new LRU();
		lru.put("ONE", 1);
		lru.put("TWO", 2);
		lru.put("THRE", 3);
		lru.print();
		
		System.out.println( lru.get("ONE") );
		lru.print();
		lru.put("FR", 4);
		lru.print();
		
		lru.remove("FR");
		lru.print();
		
		lru.remove("THRE");
		lru.print();
	}
}
