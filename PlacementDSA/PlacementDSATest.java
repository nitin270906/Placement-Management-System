/**
 * Automated Test Suite for Placement Management System & DSA Components
 */
public class PlacementDSATest {
    private static int totalTests = 0;
    private static int passedTests = 0;

    private static void assertEquals(Object expected, Object actual, String testName) {
        totalTests++;
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName + " - Expected: " + expected + ", Got: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName + " - Condition was FALSE");
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       RUNNING AUTOMATED UNIT & INTEGRATION TESTS  ");
        System.out.println("==================================================");

        testCustomStack();
        testCustomQueue();
        testMaxHeap();
        testQuickSort();
        testPlacementSystemCore();

        System.out.println("\n--------------------------------------------------");
        System.out.printf("  SUMMARY: %d / %d Tests Passed%n", passedTests, totalTests);
        System.out.println("--------------------------------------------------");

        if (passedTests < totalTests) {
            System.exit(1);
        }
    }

    private static void testCustomStack() {
        System.out.println("\n--- Testing CustomStack ---");
        CustomStack stack = new CustomStack(2);
        assertTrue(stack.isEmpty(), "Stack initial isEmpty");
        stack.push("Applied");
        stack.push("Shortlisted");
        assertEquals(2, stack.size(), "Stack size = 2");
        stack.push("Interview"); // Trigger resize
        assertTrue(stack.size() == 3, "Stack resized to hold 3 elements");
        assertEquals("Interview", stack.peek(), "Stack peek = Interview");
        assertEquals("Interview", stack.pop(), "Stack pop = Interview");
        assertEquals("Shortlisted", stack.peek(), "Stack peek after pop = Shortlisted");
    }

    private static void testCustomQueue() {
        System.out.println("\n--- Testing CustomQueue ---");
        CustomQueue queue = new CustomQueue(2);
        assertTrue(queue.isEmpty(), "Queue initial isEmpty");
        
        Application app1 = new Application(10000001, 1, "Student 1", "Company 1");
        Application app2 = new Application(10000002, 1, "Student 2", "Company 1");
        Application app3 = new Application(10000003, 1, "Student 3", "Company 1");

        queue.enqueue(app1);
        queue.enqueue(app2);
        queue.enqueue(app3); // Test dynamic resizing/overflow handling

        assertEquals(3, queue.getSize(), "Queue dynamic resize size = 3");
        assertEquals(app1, queue.peek(), "Queue peek = app1");
        assertEquals(app1, queue.dequeue(), "Queue dequeue = app1");
        assertEquals(app2, queue.dequeue(), "Queue dequeue = app2");
        assertEquals(1, queue.getSize(), "Queue size after 2 dequeues = 1");
    }

    private static void testMaxHeap() {
        System.out.println("\n--- Testing MaxHeap ---");
        MaxHeap heap = new MaxHeap(2);
        Drive d1 = new Drive(1, "LowCTC", 5.0, "ALL", 6.0, 0);
        Drive d2 = new Drive(2, "HighCTC", 25.0, "ALL", 6.0, 0);
        Drive d3 = new Drive(3, "MidCTC", 15.0, "ALL", 6.0, 0);

        heap.insert(d1);
        heap.insert(d2);
        heap.insert(d3); // Trigger resize

        assertEquals(3, heap.getSize(), "MaxHeap size = 3");
        assertEquals(d2, heap.getMax(), "MaxHeap max = d2 (25.0 LPA)");
        assertEquals(d2, heap.extractMax(), "MaxHeap extractMax = d2");
        assertEquals(d3, heap.extractMax(), "MaxHeap next extractMax = d3 (15.0 LPA)");
        assertEquals(d1, heap.extractMax(), "MaxHeap last extractMax = d1 (5.0 LPA)");
    }

    private static void testQuickSort() {
        System.out.println("\n--- Testing QuickSort ---");
        Drive d1 = new Drive(1, "Company A", 10.0, "ALL", 6.0, 0);
        Drive d2 = new Drive(2, "Company B", 30.0, "ALL", 6.0, 0);
        Drive d3 = new Drive(3, "Company C", 20.0, "ALL", 6.0, 0);
        Drive[] drives = {d1, d2, d3};

        QuickSort.sortByCTC(drives, 3);
        assertEquals("Company B", drives[0].getCompanyName(), "QuickSort index 0 = Highest CTC (30 LPA)");
        assertEquals("Company C", drives[1].getCompanyName(), "QuickSort index 1 = Mid CTC (20 LPA)");
        assertEquals("Company A", drives[2].getCompanyName(), "QuickSort index 2 = Lowest CTC (10 LPA)");
    }

    private static void testPlacementSystemCore() {
        System.out.println("\n--- Testing PlacementSystem Core Logic ---");
        PlacementSystem sys = new PlacementSystem();
        assertTrue(sys.getStudentCount() > 0, "System has students loaded");
        assertTrue(sys.getDriveCount() > 0, "System has drives loaded");

        Student s = sys.findStudentById(10000001);
        if (s == null) {
            s = sys.getStudents()[0];
        }
        assertTrue(s != null, "Found student for eligibility test");

        Drive[] eligible = sys.getEligibleDrives(s.getId());
        assertTrue(eligible != null, "Eligible drives returned non-null");

        // Test dynamic capacity addition (>50 drives)
        int initialDrives = sys.getDriveCount();
        for (int i = 100; i < 160; i++) {
            Drive d = new Drive(i, "TestComp" + i, 10.0 + i, "ALL", 6.0, 0);
            sys.addNewDrive(d);
        }
        assertTrue(sys.getDriveCount() == initialDrives + 60, "PlacementSystem dynamically grew beyond 50 drives");

        // Test Match Score calculation
        Drive testDrive = sys.getDrives()[0];
        s.setSkills("java, spring boot, sql");
        testDrive.setRequiredSkills("java, sql");
        double score = sys.calculateMatchScore(s, testDrive);
        assertEquals(100.0, score, "Match score = 100%");
    }
}
