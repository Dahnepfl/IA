package template;

import logist.task.Task;

public class TaskState {
    private final Task t;
    private final State.TASK_STATE ts;

    TaskState(Task t, State.TASK_STATE ts) {
        this.t = t;
        this.ts = ts;
    }

    public Task getTask() {
        return t;
    }

    public State.TASK_STATE getState() {
        return ts;
    }
}
