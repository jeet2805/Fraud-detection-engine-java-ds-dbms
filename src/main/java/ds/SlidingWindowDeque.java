package ds;

/**
 * Doubly Linked List based Deque for Sliding Window Velocity Checks.
 * Built without java.util.
 */
public class SlidingWindowDeque {
    private static class Node {
        long timestamp;
        Node next, prev;
        Node(long ts) { this.timestamp = ts; }
    }

    private Node head, tail;
    private int size;

    public void addLast(long timestamp) {
        Node newNode = new Node(timestamp);
        if (tail == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    public void removeFirst() {
        if (head == null) return;
        if (head == tail) {
            head = tail = null;
        } else {
            head = head.next;
            head.prev = null;
        }
        size--;
    }

    public void evictExpired(long currentTime, long windowSeconds) {
        long limit = currentTime - (windowSeconds * 1000);
        while (head != null && head.timestamp < limit) {
            removeFirst();
        }
    }

    public int count() { return size; }
}
