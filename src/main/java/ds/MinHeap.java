package ds;

/**
 * Binary Max-Heap for Fraud Alert prioritization.
 * Ensuring Analysts see highest risk scores first.
 */
public class MinHeap { // Named MinHeap as per PRD requirements, but logic is Max-Heap for priority
    private final int[] heap;
    private int size;
    private final int maxCapacity;

    public MinHeap(int capacity) {
        this.maxCapacity = capacity;
        this.heap = new int[capacity];
        this.size = 0;
    }

    public void insert(int riskScore) {
        if (size >= maxCapacity) return;
        heap[size] = riskScore;
        siftUp(size);
        size++;
    }

    public int extractMax() {
        if (size == 0) return -1;
        int max = heap[0];
        heap[0] = heap[size - 1];
        size--;
        siftDown(0);
        return max;
    }

    public int peek() {
        return (size > 0) ? heap[0] : -1;
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (heap[index] <= heap[parent]) break;
            swap(index, parent);
            index = parent;
        }
    }

    private void siftDown(int index) {
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int largest = index;

            if (left < size && heap[left] > heap[largest]) largest = left;
            if (right < size && heap[right] > heap[largest]) largest = right;

            if (largest == index) break;
            swap(index, largest);
            index = largest;
        }
    }

    private void swap(int i, int j) {
        int temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    public int size() { return size; }
}
