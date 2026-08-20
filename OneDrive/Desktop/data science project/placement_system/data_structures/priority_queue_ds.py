class PriorityQueue:
    def __init__(self):
        # List of tuples: (item, priority)
        # We keep it sorted so that pop gets the highest priority element
        self.elements = []

    def push(self, item, priority):
        self.elements.append((item, priority))
        # Sort by priority descending (so higher priority is at the end for easy O(1) popping)
        self.elements.sort(key=lambda x: x[1])

    def pop(self):
        if self.is_empty():
            return None
        return self.elements.pop()[0]

    def peek(self):
        if self.is_empty():
            return None
        return self.elements[-1][0]

    def is_empty(self):
        return len(self.elements) == 0

    def size(self):
        return len(self.elements)

    def to_list(self):
        # Return elements sorted by priority descending
        return [el[0] for el in reversed(self.elements)]
