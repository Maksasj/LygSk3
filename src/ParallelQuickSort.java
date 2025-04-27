import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class ParallelQuickSort {
    private static final int THRESHOLD = 1000;

    private final boolean debugMode;
    private final int[] array;

    public ParallelQuickSort(int[] array, boolean debugMode) {
        this.array = array;
        this.debugMode = debugMode;
    }

    private class SortTask extends RecursiveAction {
        private final int low;
        private final int high;

        SortTask(int low, int high) {
            this.low = low;
            this.high = high;
            if (debugMode) {
                System.out.printf("[%s] Sukurta užduotis: [%d..%d]%n", Thread.currentThread().getName(), low, high);
            }
        }

        @Override
        protected void compute() {
            if (high - low < THRESHOLD) {
                if (debugMode) {
                    System.out.printf("[%s] Vykdomas nuoseklus rūšiavimas: [%d..%d]%n",
                            Thread.currentThread().getName(), low, high);
                }

                 Arrays.sort(array, low, high + 1);

            } else {
                if (debugMode) {
                    System.out.printf("[%s] Vykdomas skaidymas: [%d..%d]%n", Thread.currentThread().getName(), low, high);
                }

                int pivotIndex = partition(low, high);
                if (debugMode) {
                     System.out.printf("[%s] Skaidymas baigtas [%d..%d], pivot index: %d. Kuriamos sub-užduotys.%n",
                            Thread.currentThread().getName(), low, high, pivotIndex);
                }

                // Kuriame dvi sub-užduotis kairiajai ir dešiniajai dalims
                SortTask leftTask = null;
                if (low < pivotIndex - 1) {
                    leftTask = new SortTask(low, pivotIndex - 1);
                }

                SortTask rightTask = null;
                if (pivotIndex + 1 < high) {
                     rightTask = new SortTask(pivotIndex + 1, high);
                }

                // Vykdom sub-užduotis lygiagrečiai (ForkJoinPool pasirūpins paskirstymu)
                // invokeAll efektyviai paleidžia užduotis
                 if (leftTask != null && rightTask != null) {
                    invokeAll(leftTask, rightTask);
                 } else if (leftTask != null) {
                    leftTask.invoke(); // Arba fork() ir join(), bet invoke() paprasčiau čia
                 } else if (rightTask != null) {
                    rightTask.invoke();
                 }
            }
        }
    }

    // QuickSort skaidymo (partition) metodas
    private int partition(int low, int high) {
        // Paprastas pivot pasirinkimas (vidurinis elementas) - galima tobulinti
        int pivotIndex = low + (high - low) / 2;
        int pivotValue = array[pivotIndex];
        if (debugMode) {
            System.out.printf("[%s]      Skaidymas [%d..%d], pivot elementas index %d, value %d%n",
                 Thread.currentThread().getName(), low, high, pivotIndex, pivotValue);
        }

        // Perkeliam pivot į galą laikinai
        swap(pivotIndex, high);

        int storeIndex = low;
        for (int i = low; i < high; i++) {
            if (array[i] < pivotValue) {
                swap(i, storeIndex);
                storeIndex++;
            }
        }
        // Grąžinam pivot į jo galutinę vietą
        swap(storeIndex, high);

        if (debugMode) {
             System.out.printf("[%s]      Skaidymas [%d..%d] baigtas, pivot galutinis index %d%n",
                 Thread.currentThread().getName(), low, high, storeIndex);
        }
        return storeIndex;
    }

    // Nuoseklus QuickSort variantas (naudojamas kaip alternatyva Arrays.sort žemiau slenksčio)
    private void quickSortSequential(int low, int high) {
         if (low < high) {
            int pivotIndex = partition(low, high);
            quickSortSequential(low, pivotIndex - 1);
            quickSortSequential(pivotIndex + 1, high);
        }
    }

    // Pagalbinis metodas elementų sukeitimui
    private void swap(int i, int j) {
        if (i != j) {
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    // Metodas, kuris inicijuoja lygiagretų rūšiavimą
    public void sort() {
        // Sukuriam ForkJoinPool su nurodytu gijų skaičiumi
        // Gijos bus sukurtos ir veiks iki pool.shutdown()
        int numThreads = Runtime.getRuntime().availableProcessors(); // Default
        // Skaityti is komandines eilutes jei reikia
        // ...

        ForkJoinPool pool = new ForkJoinPool(numThreads); // Naudosime tiek gijų, kiek nurodyta

        if (debugMode) {
            System.out.printf("Pradedamas lygiagretus rūšiavimas. Gijų skaičius: %d. Masyvo dydis: %d. Slenkstis: %d%n",
                 pool.getParallelism(), array.length, THRESHOLD);
            if (array.length <= 50) { // Spausdinam tik mažus masyvus
                 System.out.println("Pradinis masyvas: " + Arrays.toString(array));
            }
        }

        long startTime = System.nanoTime();

        // Sukuriam pagrindinę užduotį visam masyvui
        SortTask mainTask = new SortTask(0, array.length - 1);

        // Pradedam vykdymą ForkJoinPool'e
        pool.invoke(mainTask);

        // Laukiam, kol visos užduotys bus baigtos ir uždarom pool'ą
        pool.shutdown();
        try {
             // Palaukiam truputį, kad tikrai visos gijos baigtų darbą (nors invoke turėtų blokuoti)
            pool.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println("Rūšiavimas pertrauktas: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime); // Nanosekundės

        if (debugMode) {
            System.out.println("Rūšiavimas baigtas.");
             if (array.length <= 50) { // Spausdinam tik mažus masyvus
                 System.out.println("Surūšiuotas masyvas: " + Arrays.toString(array));
            }
        }

        System.out.printf("Vykdymo trukmė: %.3f ms%n", duration / 1_000_000.0);

        // Patikrinimas (gerai turėti testavimui)
        // if (!isSorted(array)) {
        //     System.err.println("Klaida: masyvas nėra surūšiuotas!");
        // }
    }

     // Pagalbinis metodas patikrinti ar masyvas surūšiuotas
     public static boolean isSorted(int[] array) {
         for (int i = 0; i < array.length - 1; i++) {
             if (array[i] > array[i + 1]) {
                 return false;
             }
         }
         return true;
     }

    public static void main(String[] args) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        int arraySize = 1_000_000_000;
        boolean debug = false;

        try {
            if (args.length > 0) {
                numThreads = Integer.parseInt(args[0]);
                if (numThreads <= 0) {
                    System.err.println("Gijų skaičius turi būti teigiamas.");
                    return;
                }
            }
            if (args.length > 1) {
                arraySize = Integer.parseInt(args[1]);
                 if (arraySize <= 0) {
                    System.err.println("Masyvo dydis turi būti teigiamas.");
                    return;
                }
            }
            if (args.length > 2 && (args[2].equalsIgnoreCase("debug") || args[2].equalsIgnoreCase("-d"))) {
                 debug = true;
            }
        } catch (NumberFormatException e) {
            System.err.println("Klaida skaitant argumentus. Naudojimas: java ParallelQuickSort [giju_sk] [masyvo_dydis] [debug]");
            System.err.println("Pvz.: java ParallelQuickSort 8 10000000");
            System.err.println("Pvz.: java ParallelQuickSort 4 50000 debug");
            return;
        }

        // Generuojam atsitiktinį masyvą
        System.out.printf("Generuojamas %d dydžio masyvas...%n", arraySize);
        int[] arrayToSort = generateRandomArray(arraySize);
        System.out.println("Masyvas sugeneruotas.");


        // Rūšiavimas
        ParallelQuickSort sorter = new ParallelQuickSort(arrayToSort, debug);

        //--- Lygiagretus vykdymas ---
        System.out.printf("%n--- Pradedamas lygiagretus rūšiavimas (%d gijos) ---%n", numThreads);
        // Sukuriam ForkJoinPool čia, kad galėtume perduoti gijų skaičių
         ForkJoinPool pool = new ForkJoinPool(numThreads);
         System.out.printf("Sukurtas ForkJoinPool su %d gijomis.%n", pool.getParallelism());

         if (debug) {
            System.out.printf("Pradedamas lygiagretus rūšiavimas. Masyvo dydis: %d. Slenkstis: %d%n",
                 arrayToSort.length, THRESHOLD);
             if (arrayToSort.length <= 50) {
                 System.out.println("Pradinis masyvas: " + Arrays.toString(arrayToSort));
             }
         }

         long startTime = System.nanoTime();
         SortTask mainTask = sorter.new SortTask(0, arrayToSort.length - 1); // Reikia sorter instancijos
         pool.invoke(mainTask);
         pool.shutdown(); // Inicijuojam uždarymą
         try {
             if (!pool.awaitTermination(5, TimeUnit.MINUTES)) { // Laukiam iki 5 min.
                 System.err.println("ForkJoinPool neužsidarė laiku!");
                 pool.shutdownNow(); // Bandom priverstinai
             }
         } catch (InterruptedException e) {
             System.err.println("Laukimas pertrauktas: " + e.getMessage());
             pool.shutdownNow();
             Thread.currentThread().interrupt();
         }
         long endTime = System.nanoTime();
         long duration = endTime - startTime;


         if (debug) {
             System.out.println("Lygiagretus rūšiavimas baigtas.");
             if (arrayToSort.length <= 50) {
                 System.out.println("Surūšiuotas masyvas: " + Arrays.toString(arrayToSort));
             }
         }
         System.out.printf("LYGIAGRETUS Vykdymo trukmė: %.3f ms%n", duration / 1_000_000.0);

        // Patikrinam ar tikrai surūšiuota (svarbu!)
         if (isSorted(arrayToSort)) {
            System.out.println("Masyvas surūšiuotas teisingai.");
         } else {
            System.err.println("KLAIDA: Masyvas po lygiagretaus rūšiavimo NĖRA surūšiuotas!");
         }


        //--- Nuoseklus vykdymas palyginimui (pasirinktinai) ---
         // Kad palyginimas būtų sąžiningas, reikėtų generuoti naują masyvą
         // arba kopijuoti pradinį prieš kiekvieną rūšiavimą.
         // Čia naudosim Arrays.sort kaip pavyzdį, nes jis labai optimizuotas.
         System.out.printf("%n--- Pradedamas nuoseklus rūšiavimas (Arrays.sort) ---%n");
         int[] arrayCopy = generateRandomArray(arraySize); // Generuojam tokį patį (arba kopijuojam)

         long startTimeSeq = System.nanoTime();
         Arrays.sort(arrayCopy); // Java integruotas, dažnai lygiagretus dideliems masyvams!
         long endTimeSeq = System.nanoTime();
         long durationSeq = endTimeSeq - startTimeSeq;

         System.out.printf("NUOSEKLUS (Arrays.sort) Vykdymo trukmė: %.3f ms%n", durationSeq / 1_000_000.0);
         if (!isSorted(arrayCopy)) {
             System.err.println("KLAIDA: Masyvas po Arrays.sort NĖRA surūšiuotas!");
         }

         // Galima pridėti ir tikrai nuoseklų QuickSort palyginimui, jei reikia
         // System.out.printf("%n--- Pradedamas nuoseklus rūšiavimas (QuickSortSequential) ---%n");
         // int[] arrayCopy2 = generateRandomArray(arraySize);
         // ParallelQuickSort seqSorter = new ParallelQuickSort(arrayCopy2, false);
         // long startTimeSeqQS = System.nanoTime();
         // seqSorter.quickSortSequential(0, arrayCopy2.length - 1);
         // long endTimeSeqQS = System.nanoTime();
         // long durationSeqQS = endTimeSeqQS - startTimeSeqQS;
         // System.out.printf("NUOSEKLUS (QuickSortSequential) Vykdymo trukmė: %.3f ms%n", durationSeqQS / 1_000_000.0);
         // if (!isSorted(arrayCopy2)) {
         //    System.err.println("KLAIDA: Masyvas po QuickSortSequential NĖRA surūšiuotas!");
         // }


        // Spartos įvertinimas (lyginant su Arrays.sort)
        if (duration > 0) {
            System.out.printf("%nLygiagretaus algoritmo pagreitėjimas lyginant su Arrays.sort: %.2f karto%n",
                 (double)durationSeq / duration);
        }
        // Jei lygintume su tikrai nuosekliu QuickSort:
        // if (duration > 0 && durationSeqQS > 0) {
        //     System.out.printf("Lygiagretaus algoritmo pagreitėjimas lyginant su nuosekliu QuickSort: %.2f karto%n",
        //          (double)durationSeqQS / duration);
        // }

    }

    // Pagalbinis metodas generuoti atsitiktinių skaičių masyvą
    private static int[] generateRandomArray(int size) {
        if (size <= 0) return new int[0];
        int[] array = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(); // Generuoja per visą int diapazoną
        }
        return array;
    }
}