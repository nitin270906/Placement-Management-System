/**
 * Custom Stack implementation using an array.
 * DSA Concept: Stack (LIFO - Last In First Out)
 * 
 * Operations:
 * - push()  : O(1) - Add element to top
 * - pop()   : O(1) - Remove element from top
 * - peek()  : O(1) - View top element without removing
 * - isEmpty(): O(1) - Check if stack is empty
 * - size()  : O(1) - Get number of elements
 */
public class CustomStack {
    private String[] data;
    private int top;
    private int capacity;

    public CustomStack(int capacity) {
        this.capacity = capacity;
        this.data = new String[capacity];
        this.top = -1;
    }

    // Push element onto stack - O(1)
    public boolean push(String element) {
        if (isFull()) {
            // Dynamic resizing - doubles capacity when full
            resize();
        }
        data[++top] = element;
        return true;
    }

    // Pop element from stack - O(1)
    public String pop() {
        if (isEmpty()) {
            return null;
        }
        String element = data[top];
        data[top] = null;
        top--;
        return element;
    }

    // Peek at top element without removing - O(1)
    public String peek() {
        if (isEmpty()) {
            return null;
        }
        return data[top];
    }

    // Check if stack is empty - O(1)
    public boolean isEmpty() {
        return top == -1;
    }

    // Check if stack is full - O(1)
    public boolean isFull() {
        return top == capacity - 1;
    }

    // Get current size - O(1)
    public int size() {
        return top + 1;
    }

    // Display all elements (bottom to top)
    public void display() {
        if (isEmpty()) {
            System.out.println("  [Stack is empty]");
            return;
        }
        System.out.println("  Stack (bottom -> top):");
        for (int i = 0; i <= top; i++) {
            String marker = (i == top) ? " <-- TOP" : "";
            System.out.println("    [" + i + "] " + data[i] + marker);
        }
    }

    public String[] getItems() {
        String[] result = new String[top + 1];
        for (int i = 0; i <= top; i++) {
            result[i] = data[i];
        }
        return result;
    }

    // Resize array when full - O(n) amortized
    private void resize() {
        int newCapacity = capacity * 2;
        String[] newData = new String[newCapacity];
        for (int i = 0; i < capacity; i++) {
            newData[i] = data[i];
        }
        data = newData;
        capacity = newCapacity;
    }
}
