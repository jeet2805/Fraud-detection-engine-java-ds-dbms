package ds;

/**
 * Custom Adjacency List Graph for Cycle Detection (A -> B -> C -> A)
 * Built without java.util for core logic.
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
        return accountId % capacity;
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
        if (recStack[idx]) return true;
        if (visited[idx]) return false;

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
