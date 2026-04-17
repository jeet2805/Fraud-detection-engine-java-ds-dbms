package ds;

/**
 * Custom Adjacency List Graph for Cycle Detection (A -> B -> C -> A)
 * Built without java.util for core logic.
 * 
 * IMPORTANT: capacity must be >= max(account_id) + 1
 * so that getIndex(id) == id, eliminating hash collisions.
 */
public class TransactionGraph {
    private static class EdgeNode {
        int toAccountId;
        EdgeNode next;

        EdgeNode(int toId, EdgeNode next) {
            this.toAccountId = toId;
            this.next = next;
        }
    }

    private final EdgeNode[] adjList;
    private final int capacity;

    public TransactionGraph(int maxAccounts) {
        this.capacity = maxAccounts;
        this.adjList = new EdgeNode[maxAccounts];
    }

    private int getIndex(int accountId) {
        // When capacity > max(account_id), this is just accountId
        return Math.abs(accountId % capacity);
    }

    public void addEdge(int fromId, int toId) {
        int index = getIndex(fromId);
        adjList[index] = new EdgeNode(toId, adjList[index]);
    }

    public boolean hasCycle(int startAccountId) {
        boolean[] visited = new boolean[capacity];
        boolean[] recStack = new boolean[capacity];
        return dfs(startAccountId, visited, recStack);
    }

    private boolean dfs(int current, boolean[] visited, boolean[] recStack) {
        int idx = getIndex(current);
        if (recStack[idx]) return true;   // Back-edge found = cycle
        if (visited[idx]) return false;   // Already fully explored

        visited[idx] = true;
        recStack[idx] = true;

        EdgeNode node = adjList[idx];
        while (node != null) {
            if (dfs(node.toAccountId, visited, recStack)) return true;
            node = node.next;
        }

        recStack[idx] = false;
        return false;
    }
}
