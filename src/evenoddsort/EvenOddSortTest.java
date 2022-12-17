package evenoddsort;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EvenOddSortTest {

    private static Scanner sc;
    private static EvenOddSort sortObj;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        
        // TODO code application logic here
        Runtime runtime = Runtime.getRuntime();
        // get the number of processors available to the Java virtual machine
        int numberOfProcessors = runtime.availableProcessors();
        System.out.println("Number of processors available to this JVM: " + numberOfProcessors);

        //Take Input From User
        sc = new Scanner(System.in);
        sortObj = new EvenOddSort();
        int initSizeOfArray = takeInputForArray();
        int initNThreads = takeInputForThreads();
        if (initSizeOfArray > 0) {
            execute(initSizeOfArray, initNThreads);
        } else {
            System.out.println("Main Exit..");
            return;
        }
        int input;

        do {
            //System.out.println("Number of processors available to this JVM: " + numberOfProcessors);
            System.out.println("Do you want to use same array elements? Type '0' for Yes or '1' for No");
            int choice = sc.nextInt();

            if (choice == 0) {
               
                sortObj.setSortArr(sortObj.getMainArr());
                initSizeOfArray=sortObj.getSortArr().length;
                System.out.println("Array Before Sorting");
                for (int i = 0; i <initSizeOfArray; i++) {
                    System.out.print(sortObj.getSortArr()[i] + ",");
                }
                System.out.println();
                int nThreads = takeInputForThreads();
                if (initSizeOfArray > 0) {
                    execute(initSizeOfArray, nThreads);
                } else {
                    System.out.println("Main Exit..");
                    return;
                }
            } else if (choice == 1) {
                int sizeOfArray = takeInputForArray();
                int nThreads = takeInputForThreads();
                if (sizeOfArray > 0) {
                    execute(sizeOfArray, nThreads);
                } else {
                    System.out.println("Main Exit..");
                    return;
                }
            } else {
                System.out.println("Wrong Input..");
                return;
            }

            System.out.println("DO You want to continue Type '0' to continue or '1' to exit");
            input = sc.nextInt();
        } while (input == 0);
        System.out.println("Main Exit");
    }

 
    private static int takeInputForArray() {
        System.out.println("Enter No. of Array elements");
        int sizeOfArray = sc.nextInt();
        System.out.println("Entered :: " + sizeOfArray);
        if (sizeOfArray > 1) {
          
            sortObj.generateRandomNoArray(sizeOfArray);
            System.out.println("Array Before Sorting");
            for (int i = 0; i < sizeOfArray; i++) {
                System.out.print(sortObj.getSortArr()[i] + ",");
            }
            System.out.println();

        } else {
            System.out.println("Enter size of Array greater than 1");
        }
        return sizeOfArray > 1 ? sizeOfArray : 0;
    }

    private static int takeInputForThreads() {
        System.out.println("Use Main Thread type '1' ,\nUse Multiple Threads Type '2'  ");
        int choiceNThread = sc.nextInt();
        System.out.println("Entered :: " + choiceNThread);
        return choiceNThread;
    }

    private static void execute(int sizeOfArray, int choiceNThread) throws InterruptedException, ExecutionException {
        Instant start = null;
        List<Integer> finalList = new ArrayList<>();
        System.out.println("Size of array::" + sizeOfArray);
        if (choiceNThread == 1) {
            start = Instant.now();
            finalList=sortObj.sortEvenOddSingleThread();

        } else if (choiceNThread == 2) {
            int nThread = 1;
            System.out.println("Enter No. of Threads to sort array..");
            nThread = sc.nextInt();
            ExecutorService threadpool = Executors.newFixedThreadPool(nThread);
            System.out.println("Please wait execution in progress..");
            start = Instant.now();
            // sortObj.sortEvenOddMultipleThreads(threadpool);
            finalList = sortObj.sortEvenOddMultipleThreadsOptimized(threadpool, nThread);
            threadpool.shutdown();
            try {
                //wait for all threads complete excution for upto 3600 seconds
                if (!threadpool.awaitTermination(3600, TimeUnit.SECONDS)) {
                    threadpool.shutdownNow();
                }

            } catch (InterruptedException ex) {
                threadpool.shutdownNow();
                Thread.currentThread().interrupt();
            }

        } else {
            System.out.println("Wrong Input..");
            return;
        }
        Instant end = Instant.now();
        System.out.println("Array After Sorting");
        for (int i : finalList) {
            System.out.print(i + ",");
        }
        System.out.println("");
        Duration timeElapsed = Duration.between(start, end);
        System.out.println("Total Execution time ::" + timeElapsed.getSeconds() + " seconds" + "  " + timeElapsed.getNano() + " nanoseconds");
    }
}
