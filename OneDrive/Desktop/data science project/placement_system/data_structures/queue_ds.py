class Node:
    def __init__(self, value):
        self.value = value
        self.next = None

class Queue:
    def __init__(self):
        self.head = None
        self.tail = None
        self.size_count = 0

    def enqueue(self, item):
        new_node = Node(item)
        if self.tail is None:
            self.head = new_node
            self.tail = new_node
        else:
            self.tail.next = new_node
            self.tail = new_node
        self.size_count += 1

    def dequeue(self):
        if self.head is None:
            return None
        temp = self.head
        self.head = self.head.next
        if self.head is None:
            self.tail = None
        self.size_count -= 1
        return temp.value

    def peek(self):
        if self.head is None:
            return None
        return self.head.value

    def is_empty(self):
        return self.size_count == 0

    def size(self):
        return self.size_count

    def to_list(self):
        result = []
        curr = self.head
        while curr:
            result.append(curr.value)
            curr = curr.next
        return result
