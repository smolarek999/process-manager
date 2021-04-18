package org.pawele;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessManagerWithRemoveOldestStrategyTest extends BaseProcessManagerTest{

    @Override
    protected ProcessManager createManager(int capacity) {
        return new ProcessManager(capacity, ProcessManager.OverflowStrategy.REMOVE_OLDEST);
    }

    @Test
    void whenRemoveOldestStrategy() {
        var manager = createManager(1);
        var process1 = manager.addProcess(Process.Priority.LOW);
        var process2 = manager.addProcess(Process.Priority.LOW);

        Assertions.assertThat(process1).isPresent();
        Assertions.assertThat(process2).isPresent();
        Assertions.assertThat(manager.getProcesses()).containsExactly(process2.get());
    }}
