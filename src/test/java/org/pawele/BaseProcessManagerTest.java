package org.pawele;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class BaseProcessManagerTest {

    @Test
    void managerWithZeroCapacityWillThrowAnException() {
        Assertions.assertThatThrownBy(() -> createManager(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Capacity must be positive");
    }

    @Test
    void managerWithNegativeCapacityWillThrowAnException() {
        Assertions.assertThatThrownBy(() -> createManager(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Capacity must be positive");
    }

    @Test
    void creatingProcessWithNullPriorityWillThrowAnException() {
        var manager = createManager(1);

        Assertions.assertThatThrownBy(() -> manager.addProcess(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Process priority must be not null");
    }

    @Test
    void processWillBeAddedWhenThereIsRemainingCapacity() {
        //given
        var manager = new ProcessManager(1);
        var process = manager.addProcess(Process.Priority.LOW);

        //when
        assertTrue(process.isPresent());

        //then
        Assertions.assertThat(process).isNotEmpty();
        Assertions.assertThat(process.get().getPriority()).isEqualTo(Process.Priority.LOW);
        Assertions.assertThat(process.get().getId()).isNotBlank();
    }

    @Test
    void createdProcessesShouldHaveUniqueId() {
        //given
        var manager = createManager(2);

        //when
        var process1 = manager.addProcess(Process.Priority.LOW);
        var process2 = manager.addProcess(Process.Priority.LOW);

        //then
        Assertions.assertThat(process1).isPresent();
        Assertions.assertThat(process2).isPresent();

        Assertions.assertThat(process1.get().getId()).isNotBlank();
        Assertions.assertThat(process1.get().getId()).isNotEqualTo(process2.get().getId());
        Assertions.assertThat(process2.get().getId()).isNotBlank();
    }

    @Test
    void getListShouldReturnActualProcesses() {
        //given
        var manager = createManager(2);
        var process1 = manager.addProcess(Process.Priority.LOW);
        var process2 = manager.addProcess(Process.Priority.LOW);

        //when
        var processed = manager.getProcesses();

        //then
        Assertions.assertThat(processed).containsExactly(process1.get(), process2.get());
    }

    @Test
    void getListShouldReturnEmptyListForNoProcesses() {
        //given
        var manager = createManager(2);

        //when
        var processed = manager.getProcesses();

        //then
        Assertions.assertThat(processed).isEmpty();
    }

    @Test
    void deleteExistingProcessShouldReturnTrue() {
        //given
        var manager = createManager(3);
        var process1 = manager.addProcess(Process.Priority.HIGH);
        var process2 = manager.addProcess(Process.Priority.MEDIUM);

        //when
        var deleted = manager.delete(process2.get());

        //then
        Assertions.assertThat(deleted).isTrue();
        Assertions.assertThat(manager.getProcesses()).extracting(Process::getId).containsExactly(
                process1.get().getId()
        );
    }

    @Test
    void deleteNotExistingProcessShouldReturnTrue() {
        //given
        var manager = createManager(3);
        var process1 = manager.addProcess(Process.Priority.HIGH);

        //when
        var deleted = manager.delete(new Process(Process.Priority.LOW, UUID.randomUUID().toString()));

        //then
        Assertions.assertThat(deleted).isFalse();
        Assertions.assertThat(manager.getProcesses()).extracting(Process::getId).containsExactly(
                process1.get().getId()
        );
    }

    @Test
    void deleteAllShouldReturnTrueIfThereWereProcesses() {
        //given
        var manager = createManager(3);
        manager.addProcess(Process.Priority.HIGH);
        manager.addProcess(Process.Priority.HIGH);
        manager.addProcess(Process.Priority.HIGH);

        //when
        var atLeastOneDeleted = manager.deleteAll();

        //then
        Assertions.assertThat(manager.getProcesses()).isEmpty();
        Assertions.assertThat(atLeastOneDeleted).isTrue();
    }

    @Test
    void deleteAllShouldReturnFalseIfThereWereNoProcesses() {
        //given
        var manager = createManager(3);

        //when
        var atLeastOneDeleted = manager.deleteAll();

        //then
        Assertions.assertThat(manager.getProcesses()).isEmpty();
        Assertions.assertThat(atLeastOneDeleted).isFalse();
    }

    @Test
    void deleteByGroupShouldReturnTrueIfThereProcessesWithThatPriority() {
        //given
        var manager = new ProcessManager(4, ProcessManager.OverflowStrategy.REMOVE_OLDEST_LESS_IMPORTANT);
        var low = manager.addProcess(Process.Priority.LOW);
        manager.addProcess(Process.Priority.HIGH);
        manager.addProcess(Process.Priority.HIGH);
        var medium = manager.addProcess(Process.Priority.MEDIUM);

        //when
        var atLeastOneDeleted = manager.deleteAllProcessesWithPriority(Process.Priority.HIGH);

        //then
        Assertions.assertThat(manager.getProcesses()).extracting(Process::getId).containsExactly(
                low.get().getId(),
                medium.get().getId()
        );
        Assertions.assertThat(atLeastOneDeleted).isTrue();
    }

    @Test
    void deleteByGroupShouldReturnFalseIfThereWereNoProcessesWithSuchPriority() {
        //given
        var manager = new ProcessManager(4, ProcessManager.OverflowStrategy.REMOVE_OLDEST_LESS_IMPORTANT);
        var low = manager.addProcess(Process.Priority.LOW);
        var medium = manager.addProcess(Process.Priority.MEDIUM);

        //when
        var atLeastOneDeleted = manager.deleteAllProcessesWithPriority(Process.Priority.HIGH);

        //then
        Assertions.assertThat(manager.getProcesses()).extracting(Process::getId).containsExactly(
                low.get().getId(),
                medium.get().getId()
        );
        Assertions.assertThat(atLeastOneDeleted).isFalse();
    }

    protected abstract ProcessManager createManager(int capacity);
}
