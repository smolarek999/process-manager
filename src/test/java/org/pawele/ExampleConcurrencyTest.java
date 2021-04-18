package org.pawele;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ExampleConcurrencyTest {

    public static final int N_THREADS = 20;
    private final Random random = new Random();

    @Test
    void processManagerShouldWorkForManyThreads() throws ExecutionException, InterruptedException, TimeoutException {
        var capacity = 10;
        var processManager = new ProcessManager(capacity, ProcessManager.OverflowStrategy.REMOVE_OLDEST);
        var executorService = Executors.newFixedThreadPool(N_THREADS);
        var allCreatedProcesses = new ConcurrentLinkedQueue<Process>();
        var futures = new LinkedList<>();

        var processesToCreate = new AtomicInteger(10_000);
        for (int i = 0; i < N_THREADS; i++) {
            CompletableFuture<?> future = CompletableFuture.runAsync(
                    () -> {
                        while (processesToCreate.decrementAndGet() > 0) {
                            var priority = randomPriority();
                            var process = processManager.addProcess(priority);
                            Assertions.assertThat(process).isPresent();
                            allCreatedProcesses.add(process.get());
                        }
                    },
                    executorService
            );
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).get(20, TimeUnit.SECONDS);

        Assertions.assertThat(processManager.getProcesses()).hasSize(capacity);
        Assertions.assertThat(allCreatedProcesses).hasSize(9_999);

        var lastProcesses = new ArrayList<>(allCreatedProcesses)
                .stream()
                .skip(9_999 - capacity)
                .collect(Collectors.toList());

        //it may be flaky
        Assertions.assertThat(processManager.getProcesses()).containsExactlyInAnyOrderElementsOf(lastProcesses);
    }



    private Process.Priority randomPriority() {
        var index = random.nextInt(Process.Priority.values().length);
        return Process.Priority.values()[index];
    }
}
