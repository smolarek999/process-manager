package org.pawele;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProcessManagerTest extends BaseProcessManagerTest{

    @Test
    void newProcessWontBeNotAddedWhenThereIsNoCapacity() {
        var manager = new ProcessManager(1);
        var process1 = manager.addProcess(Process.Priority.LOW);
        var process2 = manager.addProcess(Process.Priority.LOW);

        Assertions.assertThat(process1).isPresent();
        Assertions.assertThat(process2).isEmpty();
    }

    @Override
    protected ProcessManager createManager(int capacity) {
        return new ProcessManager(capacity);
    }


}
