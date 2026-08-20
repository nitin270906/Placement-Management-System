/**
 * Custom QuickSort implementation for sorting drives by CTC.
 * DSA Concept: Divide and Conquer Sorting Algorithm
 * 
 * Time Complexity:
 * - Best Case:    O(n log n) - balanced partitions
 * - Average Case: O(n log n) - random pivot selection
 * - Worst Case:   O(n^2)     - already sorted (mitigated by choosing middle pivot)
 * 
 * Space Complexity: O(log n) - recursive call stack
 * 
 * How it works:
 * 1. Choose a pivot element
 * 2. Partition array: elements < pivot go left, elements > pivot go right
 * 3. Recursively sort left and right sub-arrays
 */
public class QuickSort {

    // Public method to sort an array of drives by CTC (descending order)
    public static void sortByCTC(Drive[] drives, int n) {
        if (drives == null || n <= 1) return;
        quickSort(drives, 0, n - 1);
    }

    // Recursive QuickSort - O(n log n) average
    private static void quickSort(Drive[] arr, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(arr, low, high);
            quickSort(arr, low, pivotIndex - 1);  // Sort left sub-array
            quickSort(arr, pivotIndex + 1, high); // Sort right sub-array
        }
    }

    // Partition function - places pivot in correct position
    // Using middle element as pivot to avoid worst case on sorted arrays
    private static int partition(Drive[] arr, int low, int high) {
        // Choose middle element as pivot (avoids O(n^2) on sorted input)
        int mid = low + (high - low) / 2;
        swap(arr, mid, high); // Move pivot to end

        double pivot = arr[high].getCtc();
        int i = low - 1; // Index of smaller element

        for (int j = low; j < high; j++) {
            // For descending order: use > instead of <
            if (arr[j].getCtc() > pivot) {
                i++;
                swap(arr, i, j);
            }
        }

        swap(arr, i + 1, high);
        return i + 1;
    }

    // Swap utility
    private static void swap(Drive[] arr, int i, int j) {
        Drive temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    // ============ ALSO: Sort students by CGPA (descending) ============

    public static void sortStudentsByCGPA(Student[] students, int n) {
        if (students == null || n <= 1) return;
        quickSortStudents(students, 0, n - 1);
    }

    private static void quickSortStudents(Student[] arr, int low, int high) {
        if (low < high) {
            int pivotIndex = partitionStudents(arr, low, high);
            quickSortStudents(arr, low, pivotIndex - 1);
            quickSortStudents(arr, pivotIndex + 1, high);
        }
    }

    private static int partitionStudents(Student[] arr, int low, int high) {
        int mid = low + (high - low) / 2;
        swapStudents(arr, mid, high);

        double pivot = arr[high].getCgpa();
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (arr[j].getCgpa() > pivot) { // Descending by CGPA
                i++;
                swapStudents(arr, i, j);
            }
        }

        swapStudents(arr, i + 1, high);
        return i + 1;
    }

    private static void swapStudents(Student[] arr, int i, int j) {
        Student temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
