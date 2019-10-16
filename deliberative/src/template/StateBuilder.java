package template;

import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.ArrayList;

public final class StateBuilder {
    private ArrayList<TaskState> states;
    private Topology.City origin_city;

    public StateBuilder(Topology.City origin_city, TaskSet tasks) {
        this.origin_city = origin_city;

        states = new ArrayList<>();
        tasks.forEach(task -> {
                    states.add(new TaskState(task, State.TASK_STATE.PENDING));
                }
        );
    }

    public State build() {
        return new State(origin_city, states);
    }
}