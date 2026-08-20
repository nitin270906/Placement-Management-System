# Placement Management System (DSA Project)

A Java-based Placement Management System featuring a **Pixel-Perfect Swing Desktop GUI** (`PlacementApp`), a **Console CLI Interface** (`Main`), and custom **Data Structure implementations** (Stack, Queue, Max-Heap, QuickSort) built without relying on external collections for core operations.

---

## 🌟 Key Features

### 👨‍🎓 Student Portal
* **Student Registration & Authentication**: 8-digit Student ID authentication with password policy validation.
* **Drive Registration & Eligibility Filtering**: Instant filtering of placement drives based on CGPA, Branch, and Backlog criteria.
* **Company Skill Matcher & Gap Analysis**: Calculates match percentage against required job skills and provides missing skill recommendations.
* **Profile Builder**: Interactive skill selection checklist and resume upload manager.
* **Notifications Feed**: Real-time notifications for drive announcements, status changes, and admin messages.

### 👮 Placement Officer / Admin Portal
* **Metrics Dashboard**: Metric cards for Total Students, Placed/Unplaced metrics, and Total Drives.
* **Company Drive Management**: Add, edit, and toggle open/closed registration status for company drives.
* **Applicant Management Modal**: Shortlist candidate resumes, schedule interviews, and mark candidates as Selected or Rejected.
* **Interview Scheduler Queue**: Manages FIFO candidate queues for company interview rounds.

---

## 🛠️ Data Structures Implemented

1. **Custom Stack (`CustomStack.java`)**: 
   * Tracks application status history (LIFO).
   * Supports `push()`, `pop()`, `peek()`, and dynamic array resizing $O(1)$.
2. **Custom Circular Queue (`CustomQueue.java`)**: 
   * Schedules candidates for company interview rounds (FIFO).
   * Supports `enqueue()`, `dequeue()`, `peek()`, and dynamic capacity expansion $O(1)$.
3. **Custom Max-Heap (`MaxHeap.java`)**: 
   * Retrieves Top-K highest CTC placement drives efficiently.
   * Operations: `insert()` $O(\log n)$, `extractMax()` $O(\log n)$, `getMax()` $O(1)$.
4. **Custom QuickSort (`QuickSort.java`)**: 
   * Ranks eligible drives by CTC in descending order.
   * Divide & Conquer algorithm achieving $O(n \log n)$ average time complexity.

---

## 🚀 How to Run

### Method 1: Using the Batch Launcher (Recommended on Windows)
Double-click `run.bat` or run in terminal:
```cmd
PlacementDSA\run.bat
```
Select:
* `1` — Launch Pixel-Perfect Swing GUI (`PlacementApp`)
* `2` — Launch Console CLI App (`Main`)
* `3` — Run Automated Test Suite (`PlacementDSATest`)

### Method 2: Manual Terminal Commands (UTF-8 Encoding)
Navigate into `PlacementDSA` and compile with UTF-8 encoding flag:
```bash
cd PlacementDSA
javac -encoding UTF-8 *.java
```

* **Run GUI Application:**
  ```bash
  java -Dfile.encoding=UTF-8 -cp . PlacementApp
  ```
* **Run Console CLI:**
  ```bash
  java -Dfile.encoding=UTF-8 -cp . Main
  ```
* **Run Test Suite:**
  ```bash
  java -Dfile.encoding=UTF-8 -cp . PlacementDSATest
  ```

---

## 🧪 Automated Test Suite

Run `PlacementDSATest` to execute 26 automated unit & integration assertions covering:
* Custom Stack push/pop/peek & dynamic resizing
* Custom Queue enqueue/dequeue & circular dynamic resizing
* Custom Max-Heap insertion & max extraction order
* QuickSort descending CTC sorting
* Core Placement System persistence & eligibility logic

---

## 📂 Project Structure

```
PlacementDSA/
├── Application.java         # Application tracking & status stack
├── CustomQueue.java         # Custom Circular Queue (FIFO)
├── CustomStack.java         # Custom Stack (LIFO)
├── Drive.java               # Drive entity model
├── Main.java                # Console CLI interface
├── MaxHeap.java             # Custom Max-Heap
├── PlacementApp.java        # Swing GUI Application
├── PlacementDSATest.java    # Automated Test Suite (26 assertions)
├── PlacementSystem.java     # Core system controller & persistence
├── QuickSort.java           # Custom QuickSort algorithm
├── Student.java             # Student entity model
├── run.bat                  # Build & Execution script
└── data/                    # Flat-file database storage
    ├── applications.txt
    ├── drives.txt
    ├── queues.txt
    └── students.txt
```
