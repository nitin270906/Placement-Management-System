/**
 * Custom Queue Implementation (FIFO) for Interview Scheduling
 * 
 * Demonstrates: Queue Data Structure (Array-based)
 * Operations: enqueue O(1), dequeue O(1), peek O(1)
 */
public class CustomQueue {
    private Application[] queue;
    private int front;
    private int rear;
    private int size;
    private int capacity;

    public CustomQueue(int capacity) {
        this.capacity = capacity;
        queue = new Application[capacity];
        front = 0;
        rear = -1;
        size = 0;
    }

    public boolean isFull() {
        return size == capacity;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void enqueue(Application app) {
        if (isFull()) {
            System.out.println("Queue is full! Cannot add more students.");
            return;
        }
        rear = (rear + 1) % capacity;
        queue[rear] = app;
        size++;
    }

    public Application dequeue() {
        if (isEmpty()) {
            return null;
        }
        Application app = queue[front];
        front = (front + 1) % capacity;
        size--;
        return app;
    }

    public Application peek() {
        if (isEmpty()) {
            return null;
        }
        return queue[front];
    }

    public int getSize() {
        return size;
    }

    public Application[] toArray() {
        Application[] result = new Application[size];
        for (int i = 0; i < size; i++) {
            result[i] = queue[(front + i) % capacity];
        }
        return result;
    }
}
