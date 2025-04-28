import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class ParallelQuickSort {
    private final int[] array;

    private final int splitThreshold;
    private final boolean debugModeMode;
    private final int threadCount;

    public ParallelQuickSort(int[] array, boolean debugModeMode, int threadCount, int splitThreshold) {
        this.array = array;
        this.debugModeMode = debugModeMode;
        this.threadCount = threadCount;
        this.splitThreshold = splitThreshold;
    }

    private class SortTask extends RecursiveAction {
        private final int low;
        private final int high;

        SortTask(int low, int high) {
            if(debugModeMode)
                System.out.printf("Created sort task with bound parameters [%d, %d]%n", low, high);

            this.low = low;
            this.high = high;
        }

        @Override
        protected void compute() {
            if (high - low < splitThreshold) {
                System.out.println("Range is under split threshold, stop split");

                Arrays.sort(array, low, high + 1);
                return;
            }

            int pivotIndex = partition(low, high);

            System.out.printf("Partiion pivot index %d %n", pivotIndex);

            SortTask leftTask = null;
            if (low < pivotIndex - 1)
                leftTask = new SortTask(low, pivotIndex - 1);

            SortTask rightTask = null;
            if (pivotIndex + 1 < high)
                 rightTask = new SortTask(pivotIndex + 1, high);

            if (leftTask != null && rightTask != null) {
                invokeAll(leftTask, rightTask);
            } else if (leftTask != null) {
                leftTask.invoke();
            } else if (rightTask != null) {
                rightTask.invoke();
            }
        }
    }

    private int partition(int low, int high) {
        int pivotIndex = low + (high - low) / 2;
        int pivotValue = array[pivotIndex];

        swap(pivotIndex, high);

        int storeIndex = low;

        for (int i = low; i < high; i++) {
            if (array[i] < pivotValue) {
                swap(i, storeIndex);
                storeIndex++;
            }
        }

        swap(storeIndex, high);

        return storeIndex;
    }

    private void swap(int i, int j) {
        if (i != j) {
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    public void sort() {
        if(debugModeMode)
            System.out.println("Started sorting");

        ForkJoinPool pool = new ForkJoinPool(threadCount);

        long startTime = System.nanoTime();

        SortTask mainTask = new SortTask(0, array.length - 1);
        pool.invoke(mainTask);
        pool.shutdown();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        if(debugModeMode)
            System.out.println("Ended sorting");
    }

    public static void runTest(int sampleArraySize, int threadCount, int splitThreshold, boolean debugMode) {
        int[] array = generateRandomArray(sampleArraySize);

        ParallelQuickSort sorter = new ParallelQuickSort(array, debugMode, threadCount, splitThreshold);

        long startTime = System.nanoTime();

        sorter.sort();

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        System.out.printf("%d %d %d %.8f %n", threadCount, sampleArraySize, splitThreshold,  duration / 1_000_000.0);
    }

    public static void main(String[] args) {
        int threadCount = 8;
        int sampleArraySize = 10_000_000;
        int splitThreshold = 1000;
        boolean debugMode = false;

        try {
            if (args.length > 0) {
                threadCount = Integer.parseInt(args[0]);

                if (threadCount <= 0)
                    return;
            }

            if (args.length > 1) {
                sampleArraySize = Integer.parseInt(args[1]);

                 if (sampleArraySize <= 0)
                    return;
            }

            if (args.length > 2) {
                splitThreshold = Integer.parseInt(args[2]);

                if (splitThreshold <= 0)
                    return;
            }

            if (args.length > 3 && (args[3].equalsIgnoreCase("debugMode")))
                 debugMode = true;

        } catch (NumberFormatException e) {
            System.err.println("Error parsing arguments");
            return;
        }

        runTest(10_000_000, 1, 1000, false);
        runTest(10_000_000, 2, 1000, false);
        runTest(10_000_000, 4, 1000, false);
        runTest(10_000_000, 8, 1000, false);
        runTest(10_000_000, 16, 1000, false);
        runTest(10_000_000, 32, 1000, false);

        // runTest(sampleArraySize, threadCount, splitThreshold, debugMode);
    }

    private static int[] generateRandomArray(int size) {
        Random random = new Random();
        int[] array = new int[size];

        for (int i = 0; i < size; i++)
            array[i] = random.nextInt();

        return array;
    }
}