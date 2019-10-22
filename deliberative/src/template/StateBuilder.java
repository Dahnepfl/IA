package template;

import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.HashMap;

public final class StateBuilder {
    private Topology.City origin_city;
    private int capacity;

    public StateBuilder(Topology.City origin_city, int capacity) {
        this.origin_city = origin_city;
        this.capacity   = capacity;


    }

    public State build(TaskSet tasks) {
        HashMap<Task, State.TASK_STATE> states = new HashMap<>();
        tasks.forEach(task -> {
                    states.put(task, State.TASK_STATE.PENDING);
                }
        );
        return new State(origin_city, states, null, 0.0, 0, capacity);
    }

    public State build(HashMap<Task, State.TASK_STATE> states) {
        return new State(origin_city, states, null, 0.0, 0, capacity);
    }
}