package org.pawele;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * This is not most performant implementation, potentially if speed of adding/removal elements is very crucial
 * then we can sacrifice some space complexity and use hash-maps for storing processes and partition
 * them by group
 * - sorting is not implemented as it should be not responsibility of that class
 */
public class ProcessManager {

    private final Queue<Process> processes;
    private final OverflowStrategy overflowStrategy;

    //TODO: with Lombok create Builder for that ProcessManager.builder().capacity(n).overflowStrategy(strategy).build()
    // we may consider to do also this like: ProcessManager.builder().capacity(n).onOverflow().removeOldest().build()
    public ProcessManager(int capacity) {
        this(capacity, OverflowStrategy.SKIP);
    }

    public ProcessManager(int capacity, OverflowStrategy overflowStrategy) {
        if(capacity < 1){
            throw new IllegalArgumentException("Capacity must be positive");
        }
        processes = new LinkedBlockingQueue<>(capacity);
        this.overflowStrategy = overflowStrategy;
    }

    public Optional<Process> addProcess(Process.Priority priority) {
        if(priority == null){
            throw new IllegalArgumentException("Process priority must be not null");
        }
        var process = new Process(priority, UUID.randomUUID().toString());
        boolean added;
        int iteration = 0;

        while (!(added =  processes.offer(process))) {
            // sanity check to break in case of infinite loop (i.e. bug in code)
            if(iteration++ == Integer.MAX_VALUE){
                throw new RuntimeException("PANIC: Cannot add new process: retry exhausted");
            }
            if(!tryToFindSpace(priority)){
                break;
            }
        }

        if(added){
            return Optional.of(process);
        }
        return Optional.empty();
    }

    public List<Process> getProcesses() {
        return new LinkedList<>(processes);
    }

    public boolean delete(Process process) {
        if(process == null){
            return false;
        }
        var removed = processes.remove(process);
        if(removed){
            process.kill();
        }
        return removed;
    }

    public boolean deleteAll() {
        var processes = getProcesses();
        for (Process process : processes) {
            delete(process);
        }
        return !processes.isEmpty();
    }

    /**
     * bad complexity, O(n-squared)
     * @param priority
     * @return
     */
    public boolean deleteAllProcessesWithPriority(Process.Priority priority) {
        var processes = getProcesses()
                .stream()
                .filter(p -> p.getPriority() == priority)
                .collect(Collectors.toList());
        for (Process process : processes) {
            delete(process);
        }
        return !processes.isEmpty();
    }

    private boolean tryToFindSpace(Process.Priority newProcessPriority) {

        if(overflowStrategy == OverflowStrategy.REMOVE_OLDEST){
            //TODO: if someone remove element in between and add another then we potentially can remove newer processes
            // not sure if it is acceptable, otherwise we need to use locking
            var peek = processes.peek();
            delete(peek);
            return true;
        }
        if(overflowStrategy == OverflowStrategy.REMOVE_OLDEST_LESS_IMPORTANT){
            var candidateToRemoval = findOldestLessImportantProcess(newProcessPriority);
            candidateToRemoval.ifPresent(process -> delete(process));

            return candidateToRemoval.isPresent();
        }
        return false;
    }

    private Optional<Process> findOldestLessImportantProcess(Process.Priority newProcessPriority) {
        for (Process.Priority lookupPriority : List.of(Process.Priority.LOW, Process.Priority.MEDIUM)) {
            if (!newProcessPriority.isMoreImportantThan(lookupPriority)) {
                return Optional.empty();
            }
            var candidate = processes.stream()
                    .filter(p -> p.getPriority() == lookupPriority)
                    .findFirst();

            if (candidate.isPresent()) {
                return candidate;
            }

        }
        return Optional.empty();
    }

    public enum OverflowStrategy{
        SKIP,
        REMOVE_OLDEST,
        REMOVE_OLDEST_LESS_IMPORTANT;
    }
}
