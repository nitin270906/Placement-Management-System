# Placement Management System — DSA Project

A console-based **Placement Management System** built in pure Java, demonstrating **4 core Data Structures** implemented from scratch (no built-in Java collections for the DSA logic).

## Data Structures Implemented

| # | Data Structure | File | Operations | Use Case |
|---|---------------|------|------------|----------|
| 1 | **Custom Stack** (LIFO) | `CustomStack.java` | `push O(1)`, `pop O(1)`, `peek O(1)` | Track application status history with undo |
| 2 | **Custom Queue** (FIFO) | `CustomQueue.java` | `enqueue O(1)`, `dequeue O(1)`, `peek O(1)` | Schedule interviews in first-come-first-served order |
| 3 | **Custom QuickSort** | `QuickSort.java` | `sort O(n log n)` avg | Sort eligible drives by CTC (descending) |
| 4 | **Custom Max-Heap** | `MaxHeap.java` | `insert O(log n)`, `extractMax O(log n)` | Find Top-K highest CTC placement drives |

## Project Structure

```
PlacementDSA/
├── Main.java                  # Entry point — Console menu (6 options)
├── PlacementSystem.java       # Core business logic (uses all 4 DSA structures)
│
├── CustomStack.java           # DSA 1: Stack (LIFO) — Application status tracking
├── CustomQueue.java           # DSA 2: Queue (FIFO) — Interview scheduling
├── QuickSort.java             # DSA 3: QuickSort — Sort drives by CTC
├── MaxHeap.java               # DSA 4: Max-Heap — Top-K drives
│
├── Student.java               # Model: Student data
├── Drive.java                 # Model: Placement drive data
└── Application.java           # Model: Application (uses CustomStack internally)
```

## How to Run

```bash
cd PlacementDSA
javac *.java
java Main
```

## Features

### 1. Eligibility Check (QuickSort)
- Filters drives based on CGPA, branch, backlogs, and batch year
- Sorts eligible drives by CTC in descending order using **custom QuickSort**
- Time Complexity: `O(m) filter + O(m log m) sort`

### 2. Application Tracker (Stack)
- Apply to drives → `push("Applied")`
- Update status (Shortlisted, Interview, Selected, etc.) → `push(newStatus)`
- Undo last status → `pop()`
- View full history → traverse stack bottom-to-top

### 3. Top-K Placements (Max-Heap)
- Insert all drives into a **custom Max-Heap** → `O(n log n)`
- Extract top K drives by CTC → `O(K log n)`
- More efficient than sorting when K << n

### 4. Interview Scheduler (Queue)
- Schedule interview → `enqueue()` — student joins the queue
- Start next interview → `dequeue()` — first-in-first-out
- View queue → shows waiting students in order

## Sample Data

Pre-loaded with **12 students** and **10 placement drives** from companies like Google, Microsoft, Amazon, TCS, Infosys, Goldman Sachs, etc.

## Technologies

- **Language:** Java (JDK 8+)
- **No external libraries** — all DSA implemented from scratch
- **Data persistence:** File-based (auto-saves to `data/` directory)
