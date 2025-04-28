@echo off

javac ParallelQuickSort.java

java ParallelQuickSort

:: echo "Thread count relation"
:: echo "nThreads workload threshold timeS"
:: java ParallelQuickSort 1  1000000 1000
:: java ParallelQuickSort 2 1000000 1000
:: java ParallelQuickSort 4 1000000 1000
:: java ParallelQuickSort 8 1000000 1000
:: java ParallelQuickSort 16 1000000 1000
:: java ParallelQuickSort 32 1000000 1000
:: 
:: echo "Sample size relation"
:: echo "nThreads workload threshold timeS"
:: java ParallelQuickSort 4 100 1000
:: java ParallelQuickSort 4 1000 1000
:: java ParallelQuickSort 4 10000 1000
:: java ParallelQuickSort 4 100000 1000
:: java ParallelQuickSort 4 1000000 1000
:: java ParallelQuickSort 4 10000000 1000
:: 
:: echo "Split threshold relation"
:: java ParallelQuickSort 4 1000000 1000
:: java ParallelQuickSort 4 1000000 2000
:: java ParallelQuickSort 4 1000000 4000
:: java ParallelQuickSort 4 1000000 8000
:: java ParallelQuickSort 4 1000000 16000