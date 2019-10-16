package template;

import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.HashMap;

public final class StateBuilder {
    private HashMap<Task, State.TASK_STATE> states;
    private Topology.City origin_city;
    private int capacity;

    public StateBuilder(Topology.City origin_city, TaskSet tasks, int capacity) {
        this.origin_city = origin_city;
        this.capacity   = capacity;

        states = new HashMap<>();
        tasks.forEach(task -> {
                    states.put(task, State.TASK_STATE.PENDING);
                }
        );
    }

    public State build() {
        return new State(origin_city, states, null, 0.0, 0, capacity);
    }
}