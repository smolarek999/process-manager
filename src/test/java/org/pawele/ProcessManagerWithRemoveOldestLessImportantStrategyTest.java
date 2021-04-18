package org.pawele;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessManagerWithRemoveOldestLessImportantStrategyTest extends BaseProcessManagerTest {

    @Override
    protected ProcessManager createManager(int capacity) {
        return new ProcessManager(capacity, ProcessManager.OverflowStrategy.REMOVE_OLDEST_LESS_IMPORTANT);
    }

    @Test
    void oldestLowProcessWillBeRemoved_highIsAdded() {
        //given
        var manager = new ProcessManager(5, ProcessManager.OverflowStrategy.REMOVE_OLDEST_LESS_IMPORTANT);
        var process1 = manager.addProcess(Process.Priority.HIGH);
        var process2 = manager.addProcess(Process.Priority.MEDIUM);
        var process3 = manager.addProcess(Process.Priority.LOW);
        var process4 = manager.addProcess(Process.Priority.LOW);
        var process5 = manager.addProcess(Process.Priority.MEDIUM);

        //when
        var newProcess = manager.addProcess(Process.Priority.HIGH);

        Assertions.assertThat(process1).isPresent();
        Assertions.assertThat(process2).isPresent();
        Assertions.assertThat(process3).isPresent();
        Assertions.assertThat(process4).isPresent();
        Assertions.assertThat(process5).isPresent();

        Assertions.assertThat(newProcess).isPresent();

        Assertions.assertThat(manager.getProcesses()).containsExactly(
                process1.get(),
                process2.get(),
                process4.get(),
                process5.get(),
                newProcess.get()
        );
    }

    @Test
    void oldestLowProcessWillBeRemoved_mediumIsAdded() {
        //given
        var manager = new ProcessManager(5, ProcessManager.OverflowStrategy.REMOVE_OLDEST_LESS_IMPORTANT);
        var process1 = manager.addProcess(Process.Priority.HIGH);
        var process2 = manager.addProcess(Process.Priority.MEDIUM);
        var process3 = manager.addProcess(Process.Priority.LOW);
        var process4 = manager.addProcess(Process.Priority.LOW);
        var process5 = manager.addProcess(Process.Priority.MEDIUM);

        //when
        var newProcess = manager.addProcess(Process.Priority.MEDIUM);

        Assertions.assertThat(process1).isPresent();
        Assertions.assertThat(process2).isPresent();
        Assertions.assertThat(process3).isPresent();
        Assertions.assertThat(process4).isPresent();
        Assertions.assertThat(process5).isPresent();

        Assertions.assertThat(newProcess).isPresent();

        Assertions.assertThat(manager.getProcesses()).containsExactly(
                process1.get(),
                process2.get(),
                process4.get(),
                process5.get(),
                newProcess.get()
        );
    }

    @Test
    void addingNewWillBeSkippedIfLowIsAdded() {
        //given
        var manager = new ProcessManager(5, ProcessManager.OverflowStrategy.REMOVE_OLDEST_LESS_IMPORTANT);
        var process1 = manager.addProcess(Process.Priority.HIGH);
        var process2 = manager.addProcess(Process.Priority.MEDIUM);
        var process3 = manager.addProcess(Process.Priority.LOW);
        var process4 = manager.addProcess(Process.Priority.LOW);
        var process5 = manager.addProcess(Process.Priority.MEDIUM);

        //when
        var newProcess = manager.addProcess(Process.Priority.LOW);

        Assertions.assertThat(process1).isPresent();
        Assertions.assertThat(process2).isPresent();
        Assertions.assertThat(process3).isPresent();
        Assertions.assertThat(process4).isPresent();
        Assertions.assertThat(process5).isPresent();

        Assertions.assertThat(newProcess).isEmpty();

        Assertions.assertThat(manager.getProcesses()).containsExactly(
                process1.get(),
                process2.get(),
                process3.get(),
                process4.get(),
                process5.get()
        );
    }

    @Test
    void oldestMediumWillBeRemovedIfThereIsNoLow_highIsAdded() {
        //given
        var manager = new ProcessManager(4, ProcessManager.OverflowStrategy.REMOVE_OLDEST_LESS_IMPORTANT);
        var process1 = manager.addProcess(Process.Priority.HIGH);
        var process2 = manager.addProcess(Process.Priority.MEDIUM);
        var process3 = manager.addProcess(Process.Priority.MEDIUM);
        var process4 = manager.addProcess(Process.Priority.HIGH);

        //when
        var newProcess = manager.addProcess(Process.Priority.HIGH);

        Assertions.assertThat(process1).isPresent();
        Assertions.assertThat(process2).isPresent();
        Assertions.assertThat(process3).isPresent();
        Assertions.assertThat(process4).isPresent();

        Assertions.assertThat(newProcess).isPresent();

        Assertions.assertThat(manager.getProcesses()).containsExactly(
                process1.get(),
                process3.get(),
                process4.get(),
                newProcess.get()
        );
    }

    @Test
    void addingNewWillBeSkippedWhenMediumIsAddedAndThereIsNoLow() {
        //given
        var manager = new ProcessManager(4, ProcessManager.OverflowStrategy.REMOVE_OLDEST_LESS_IMPORTANT);
        var process1 = manager.addProcess(Process.Priority.HIGH);
        var process2 = manager.addProcess(Process.Priority.MEDIUM);
        var process3 = manager.addProcess(Process.Priority.MEDIUM);
        var process4 = manager.addProcess(Process.Priority.HIGH);

        //when
        var newProcess = manager.addProcess(Process.Priority.MEDIUM);

        Assertions.assertThat(process1).isPresent();
        Assertions.assertThat(process2).isPresent();
        Assertions.assertThat(process3).isPresent();
        Assertions.assertThat(process4).isPresent();

        Assertions.assertThat(newProcess).isEmpty();

        Assertions.assertThat(manager.getProcesses()).containsExactly(
                process1.get(),
                process2.get(),
                process3.get(),
                process4.get()
        );
    }

    @Test
    void addingHighWillBeSkippedWhenTheyAreOnlyHigh() {
        //given
        var manager = new ProcessManager(1, ProcessManager.OverflowStrategy.REMOVE_OLDEST_LESS_IMPORTANT);
        var process1 = manager.addProcess(Process.Priority.HIGH);

        //when
        var newProcess = manager.addProcess(Process.Priority.HIGH);

        Assertions.assertThat(process1).isPresent();

        Assertions.assertThat(newProcess).isEmpty();

        Assertions.assertThat(manager.getProcesses()).containsExactly(
                process1.get()
        );
    }
}
