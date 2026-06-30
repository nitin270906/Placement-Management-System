class HashMap:
    def __init__(self, initial_capacity=16):
        self.capacity = initial_capacity
        self.size_count = 0
        self.buckets = [[] for _ in range(self.capacity)]

    def _hash(self, key):
        return hash(key) % self.capacity

    def put(self, key, value):
        hash_val = self._hash(key)
        bucket = self.buckets[hash_val]
        for i, (k, v) in enumerate(bucket):
            if k == key:
                bucket[i] = (key, value)
                return
        bucket.append((key, value))
        self.size_count += 1

        # Resize if load factor > 0.75
        if self.size_count / self.capacity > 0.75:
            self._resize()

    def get(self, key):
        hash_val = self._hash(key)
        bucket = self.buckets[hash_val]
        for k, v in bucket:
            if k == key:
                return v
        return None

    def remove(self, key):
        hash_val = self._hash(key)
        bucket = self.buckets[hash_val]
        for i, (k, v) in enumerate(bucket):
            if k == key:
                bucket.pop(i)
                self.size_count -= 1
                return True
        return False

    def contains(self, key):
        return self.get(key) is not None

    def size(self):
        return self.size_count

    def keys(self):
        all_keys = []
        for bucket in self.buckets:
            for k, v in bucket:
                all_keys.append(k)
        return all_keys

    def _resize(self):
        old_buckets = self.buckets
        self.capacity *= 2
        self.size_count = 0
        self.buckets = [[] for _ in range(self.capacity)]
        for bucket in old_buckets:
            for k, v in bucket:
                self.put(k, v)
