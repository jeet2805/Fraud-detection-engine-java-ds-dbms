package ds;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DataStructureTest {

    @Test
    public void testGraphCycleDetection() {
        TransactionGraph graph = new TransactionGraph(100);
        
        // No cycle: 1 -> 2 -> 3
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        assertFalse(graph.hasCycle(1), "Should not have cycle");

        // Add cycle: 3 -> 1
        graph.addEdge(3, 1);
        assertTrue(graph.hasCycle(1), "Should detect cycle 1->2->3->1");
    }

    @Test
    public void testMaxHeapPriority() {
        MinHeap heap = new MinHeap(10);
        heap.insert(40);
        heap.insert(90);
        heap.insert(60);
        heap.insert(100);

        assertEquals(100, heap.extractMax(), "Highest priority (100) should be first");
        assertEquals(90, heap.extractMax(), "Next highest (90) should follow");
        assertEquals(60, heap.extractMax());
    }

    @Test
    public void testSlidingWindow() {
        SlidingWindowDeque deque = new SlidingWindowDeque();
        long now = 1000000;
        
        // Add 3 transactions
        deque.addLast(now - 2000); // 2 seconds ago
        deque.addLast(now - 1000); // 1 second ago
        deque.addLast(now);        // Just now
        
        assertEquals(3, deque.count());

        // Evict transactions older than 1.5 seconds
        deque.evictExpired(now, 1); // window of 1 second
        assertEquals(2, deque.count(), "Should have evicted the entry from 2 seconds ago");
    }

    @Test
    public void testRuleStack() {
        RuleEngineStack stack = new RuleEngineStack(5);
        stack.push("Rule 1");
        stack.push("Rule 2");

        assertEquals(2, stack.size());
        assertEquals("Rule 2", stack.pop());
        assertEquals("Rule 1", stack.peek());
    }
}
