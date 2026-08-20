/**
 * Custom Max-Heap implementation using an array.
 * DSA Concept: Binary Heap (Max-Heap property: parent >= children)
 * 
 * Used for: Finding Top-K highest CTC placement drives efficiently.
 * 
 * Operations:
 * - insert()     : O(log n) - Add element and heapify up
 * - extractMax() : O(log n) - Remove max element and heapify down
 * - getMax()     : O(1)     - View max element without removing
 * - heapifyUp()  : O(log n) - Restore heap property upward
 * - heapifyDown(): O(log n) - Restore heap property downward
 * 
 * Array representation:
 * - Parent of node i    : (i - 1) / 2
 * - Left child of i     : 2 * i + 1
 * - Right child of i    : 2 * i + 2
 */
public class MaxHeap {
    private Drive[] heap;
    private int size;
    private int capacity;

    public MaxHeap(int capacity) {
        this.capacity = capacity;
        this.heap = new Drive[capacity];
        this.size = 0;
    }

    // Get parent index
    private int parent(int i) { return (i - 1) / 2; }

    // Get left child index
    private int leftChild(int i) { return 2 * i + 1; }

    // Get right child index
    private int rightChild(int i) { return 2 * i + 2; }

    // Swap two elements in heap
    private void swap(int i, int j) {
        Drive temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    // Insert a drive into the heap - O(log n)
    public void insert(Drive drive) {
        if (size == capacity) {
            resize();
        }
        heap[size] = drive;
        size++;
        heapifyUp(size - 1);
    }

    // Heapify Up - restore heap property after insertion - O(log n)
    private void heapifyUp(int index) {
        while (index > 0 && heap[parent(index)].getCtc() < heap[index].getCtc()) {
            swap(parent(index), index);
            index = parent(index);
        }
    }

    // Extract maximum element (highest CTC) - O(log n)
    public Drive extractMax() {
        if (size == 0) return null;

        Drive max = heap[0];
        heap[0] = heap[size - 1];
        size--;
        heapifyDown(0);
        return max;
    }

    // Heapify Down - restore heap property after extraction - O(log n)
    private void heapifyDown(int index) {
        int largest = index;
        int left = leftChild(index);
        int right = rightChild(index);

        if (left < size && heap[left].getCtc() > heap[largest].getCtc()) {
            largest = left;
        }
        if (right < size && heap[right].getCtc() > heap[largest].getCtc()) {
            largest = right;
        }

        if (largest != index) {
            swap(index, largest);
            heapifyDown(largest);
        }
    }

    // Get max without removing - O(1)
    public Drive getMax() {
        if (size == 0) return null;
        return heap[0];
    }

    // Get current size
    public int getSize() { return size; }

    // Check if heap is empty
    public boolean isEmpty() { return size == 0; }

    // Resize when capacity is reached
    private void resize() {
        int newCapacity = capacity * 2;
        Drive[] newHeap = new Drive[newCapacity];
        for (int i = 0; i < capacity; i++) {
            newHeap[i] = heap[i];
        }
        heap = newHeap;
        capacity = newCapacity;
    }

    // Display heap as array (for debugging)
    public void displayHeap() {
        if (size == 0) {
            System.out.println("  [Heap is empty]");
            return;
        }
        System.out.println("  Heap array (Max-Heap by CTC):");
        for (int i = 0; i < size; i++) {
            System.out.printf("    [%d] %s - %.2f LPA%n", i, heap[i].getCompanyName(), heap[i].getCtc());
        }
    }
}
