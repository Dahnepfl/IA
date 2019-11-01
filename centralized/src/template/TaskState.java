package template;

import logist.task.Task;

import java.util.Objects;

public class TaskState {
    private template.STATE state;
    private Task task;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskState taskState = (TaskState) o;
        return state == taskState.state &&
                task.equals(taskState.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, task);
    }

    public template.STATE getState() {
        return state;
    }

    public Task getTask() {
        return task;
    }

    public TaskState(template.STATE state, Task task) {
        this.state = state;
        this.task = task;
    }
}
