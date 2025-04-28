@echo off

javac ParallelQuickSort.java

:: java ParallelQuickSort 1  10000000 1000 debugMode
java ParallelQuickSort

:: echo "Thread count relation"
:: echo "nThreads workload threshold timeS"
:: java ParallelQuickSort 1  10000000 32000
:: java ParallelQuickSort 2 10000000 32000
:: java ParallelQuickSort 3 10000000 32000
:: java ParallelQuickSort 4 10000000 32000
:: java ParallelQuickSort 5 10000000 32000
:: java ParallelQuickSort 6 10000000 32000
:: java ParallelQuickSort 7 10000000 32000
:: java ParallelQuickSort 8 10000000 32000
:: java ParallelQuickSort 9 10000000 32000
:: java ParallelQuickSort 10 10000000 32000
:: java ParallelQuickSort 12 10000000 32000
:: java ParallelQuickSort 14 10000000 32000
:: java ParallelQuickSort 16 10000000 32000
:: java ParallelQuickSort 18 10000000 32000
:: java ParallelQuickSort 20 10000000 32000
:: java ParallelQuickSort 22 10000000 32000
:: java ParallelQuickSort 24 10000000 32000
:: java ParallelQuickSort 32 10000000 32000
:: 
:: 
:: echo "Sample size relation"
:: echo "nThreads workload threshold timeS"
:: java ParallelQuickSort 6 100 1000
:: java ParallelQuickSort 6 1000 1000
:: java ParallelQuickSort 6 10000 1000
:: java ParallelQuickSort 6 100000 1000
:: java ParallelQuickSort 6 10000000 1000
:: java ParallelQuickSort 6 100000000 1000
:: 
:: echo "Split threshold relation"
:: java ParallelQuickSort 6 10000000 1000
:: java ParallelQuickSort 6 10000000 2000
:: java ParallelQuickSort 6 10000000 4000
:: java ParallelQuickSort 6 10000000 8000
:: java ParallelQuickSort 6 10000000 16000
:: java ParallelQuickSort 6 10000000 32000
:: java ParallelQuickSort 6 10000000 64000
:: java ParallelQuickSort 6 10000000 128000