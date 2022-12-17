package evenoddsort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EvenOddSort {

    int[] sortArr, mainArr;
    List<CompletableFuture<List<Integer>>> allFuturesList = new ArrayList();

    public List<Integer> sortEvenOddSingleThread() {
        if (this.sortArr != null && this.sortArr.length > 0) {
            boolean isSorted = false;
            int counter = 0;
            while (!isSorted) {
                isSorted = true;
                int temp = 0;
                // Perform Bubble sort on even indexed element
                for (int i = 0; i < sortArr.length - 1; i = i + 2) {
                    if (sortArr[i] > sortArr[i + 1]) {
                        temp = sortArr[i];
                        sortArr[i] = sortArr[i + 1];
                        sortArr[i + 1] = temp;
                        isSorted = false;
                    }
                    //System.out.println("EVEN sortEvenOddSingleThread  Thread::" + Thread.currentThread().getName()+" idx::"+i);
                }
                //Perform Bubble sort on odd indexed element
                for (int i = 1; i < sortArr.length - 1; i = i + 2) {
                    if (sortArr[i] > sortArr[i + 1]) {
                        temp = sortArr[i];
                        sortArr[i] = sortArr[i + 1];
                        sortArr[i + 1] = temp;
                        isSorted = false;
                    }
                    //System.out.println("Odd sortEvenOddSingleThread  Thread::" + Thread.currentThread().getName()+" idx::"+i);
                }
                counter += 1;
            }
            //System.out.println("counter sortEvenOddSingleThread  Thread::" + Thread.currentThread().getName() + " idx::" + counter);
        }
        return IntStream.of(this.sortArr).boxed().collect(Collectors.toList());
    }

    public List<Integer> sortEvenOddMultipleThreadsOptimized(ExecutorService executorService, int nThreads) throws InterruptedException, ExecutionException {

        if (this.sortArr != null && this.sortArr.length > 0) {

            int splitFactor = this.sortArr.length / nThreads;//this decides how many elements are in each sublist
            int firstIndex = 0, lastIndex = splitFactor - 1;

            //initiate
            callPartition(firstIndex, lastIndex, splitFactor, nThreads, executorService);

            //collect all futures
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    allFuturesList.toArray(new CompletableFuture[allFuturesList.size()]));
            //join result of all futures means (sorted sublist produced by each thread)
            CompletableFuture<List<List<Integer>>> allPageContentsFuture = allFutures.thenApply(v -> {
                return allFuturesList.stream()
                        .map(future -> future.join())
                        .collect(Collectors.toList());
            });
            //get all sorted sublist like[[2,3,4,5],[63,65,67,78],[13,14,15,16]]  
            //"note lets suppose  thread1 sorting sublist1 in 1.2 sec , thread2 sorting sublist2 in 1.4 sec, thread3 sorting sublist3 in 1.3 and thread4 sorting sublist4 takes 1.6 sec to sort, so using completable futures it takes only 1.6 seconds to return final result bcoz all sublist are sorting using each thread parallely and highest time in all threads time is consider to return the result. In sequesntal process it takes 1.2+1.4+1.3+1.6 seconds. This is the power of parallel computation
            List<List<Integer>> finalList = allPageContentsFuture.get();
            //join each sublist elements in one single list like [2,3,4,5,63,75,67,68,13,14,15,16]
            List<Integer> joinedList = finalList.stream().flatMap(Collection::stream).collect(Collectors.toList());
            //sort final list
            Collections.sort(joinedList);

            return joinedList;
        }
        return new ArrayList<>();
    }

    private void callPartition(int firstIndex, int lastIndex, int splitFactor, int nThreads, ExecutorService executorService) {
        //break condition
        if (nThreads == 0) {
            return;
        }
        //System.out.println("callPartition  Thread::" + Thread.currentThread().getName() + " firstIndex::" + firstIndex+" ,lastIndex:: "+lastIndex);
        //partitioning array
        int[] arr = Arrays.copyOfRange(this.sortArr, firstIndex, lastIndex + 1);
        //completable future run in seprate thread from thread pool
//        CompletableFuture<List<Integer>> compFuture = CompletableFuture.supplyAsync(() -> {
//            return this.sortOddEven(arr);
//
//        }, executorService);


    CompletableFuture<List<Integer>> compFuture = CompletableFuture.supplyAsync(() -> {
            return this.sortOddEven(arr);

        }, executorService);
        //collecting all futures in a list
        allFuturesList.add(compFuture);

        firstIndex = lastIndex + 1;
        lastIndex = lastIndex + splitFactor;
        nThreads = nThreads - 1;

        callPartition(firstIndex, lastIndex, splitFactor, nThreads, executorService);
    }

    private List<Integer> sortOddEven(int[] sortArr) {
        boolean flag = false;
        while (!flag) {
            flag = true;

            for (int i = 0; i < sortArr.length - 1; i = i + 2) {
                if (sortArr[i] > sortArr[(i + 1)]) {
                    //swap value
                    int temp = sortArr[i];
                    sortArr[i] = sortArr[i + 1];
                    sortArr[i + 1] = temp;
                    flag = false;

                }
            }
            for (int i = 1; i < sortArr.length - 1; i = i + 2) {
                if (sortArr[i] > sortArr[(i + 1)]) {
                    //swap value
                    int temp = sortArr[i];
                    sortArr[i] = sortArr[i + 1];
                    sortArr[i + 1] = temp;
                    flag = false;

                }
            }

        }
        //System.out.println("sortOddEven  Thread::" + Thread.currentThread().getName()+", Sublist array size::"+sortArr.length);
        return IntStream.of(sortArr).boxed().collect(Collectors.toList());
    }

    /**
     * This method generated random array*
     */
    protected int[] generateRandomNoArray(int n) {
        Random random = new Random();
        int[] randomArr = new int[n];

        for (int i = 0; i < n; i++) {
            randomArr[i] = random.nextInt(90) + 1;
        }
        this.setMainArr(randomArr);
        this.setSortArr(randomArr);
        return randomArr;
    }

    /**
     * This method sleeps thread to specified milliseconds*
     */
    private void sleepThread(int ms) {
        try {
            //sleep thread for 0 miliseconds and 2000 nano seconds
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            //TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setSortArr(int[] sortArr) {
        this.sortArr = new int[sortArr.length];
        for (int i = 0; i < sortArr.length; i++) {
            this.sortArr[i] = sortArr[i];
        }
    }

    public void setMainArr(int[] mainArr) {
        this.mainArr = new int[mainArr.length];
        for (int i = 0; i < mainArr.length; i++) {
            this.mainArr[i] = mainArr[i];
        }

    }

    public int[] getSortArr() {
        return sortArr;
    }

    public int[] getMainArr() {
        return mainArr;
    }

//    public void sortEvenOddMultipleThreads(ExecutorService executorService) {
//
//        if (this.sortArr != null && this.sortArr.length > 0) {
//            int capacity = this.sortArr.length / 2;
//            //holds even indexes of an array
//            evenIdxArr = new int[capacity];
//            //holds odd indexes of an array
//            oddIdxArr = new int[capacity];
//
//            //if length of array is even assign capacity as below
//            if (this.sortArr.length % 2 == 0) {
//                evenIdxArr = new int[capacity];
//                oddIdxArr = new int[capacity - 1];
//            }
//
//            int n = this.sortArr.length - 1;
//            //Generate Indexes for even pass
//            for (int i = 0, j = 0; i < n & j < evenIdxArr.length;) {
//                this.evenIdxArr[j++] = i;
//                i += 2;
//            }
//            //Generate Indexes for odd pass
//            for (int i = 1, j = 0; i < n & j < oddIdxArr.length;) {
//                this.oddIdxArr[j++] = i;
//                i += 2;
//            }
//
//            while (!sortFlag.get()) {
//
//                executorService.execute(() -> {
//                    try {
//                        this.consumerEven();
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(EvenOddSort.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                });
//
//                executorService.execute(() -> {
//                    try {
//                        this.consumerOdd();
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(EvenOddSort.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                });
//
//            }
//
//        }
//    }
//
//    /**
//     *  * This method is passed to a thread, This iterated to evenIdx array*
//     * 
//     */
//    private void consumerEven() throws InterruptedException {
//        //synchronized (this) {
//           sortFlag.set(true);
//            int idx,temp=0;
//            for (int i = 0; i < evenIdxArr.length; i++) {
//               // current index 
//                idx = evenIdxArr[i];
//               // comapre if current index is greater than current index +1
//                if (sortArr[idx] > sortArr[(idx + 1)]) {
//                   // swap value
//                    temp = sortArr[idx];
//                    sortArr[idx] = sortArr[idx + 1];
//                    sortArr[idx + 1] = temp;
//                    sortFlag.set(false);
//                }
//
//            }
//           //evenFlag.set(false);
//            System.out.println("Even pass Completed..  Thread::" + Thread.currentThread().getName()+" flag::"+this.sortFlag.get());
//
//        //}
//    }
//
//    /**
//     * This method is passed to a thread, This iterated to oddIdx array*
//     */
//    private void consumerOdd() throws InterruptedException {
//        //synchronized (this) {
//        sortFlag.set(true);
//            int idx,temp=0;
//            for (int i = 0; i < oddIdxArr.length; i++) {
//                idx = oddIdxArr[i];
//                if (sortArr[idx] > sortArr[(idx + 1)]) {
//                    temp = sortArr[idx];
//                    sortArr[idx] = sortArr[idx + 1];
//                    sortArr[idx + 1] = temp;
//                    sortFlag.set(false);
//                }
//            }
//             //oddFlag.set(false);
//           System.out.println("Odd pass Completed..  Thread::" + Thread.currentThread().getName()+" flag::"+this.sortFlag.get());
//        //}
//    }
//    private void compareAndSwap(int idx) {
//
//        if (sortArr[idx] > sortArr[(idx + 1)]) {
//            //swap value
//            int temp = sortArr[idx];
//            sortArr[idx] = sortArr[idx + 1];
//            sortArr[idx + 1] = temp;
//             sortFlag.set(false);
//
//        }
//
//        String st=(idx%2)==0?"Even":"Odd";    
//         System.out.println(st+" Compare and Swap Completed..  Thread::" + Thread.currentThread().getName()+" idx::"+idx);
//    }
}
