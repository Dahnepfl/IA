package template;

import logist.task.Task;
import logist.topology.Topology;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public final class State {
    private final Topology.City actual_city;
    private final Map<Task, TASK_STATE> task_states;
    private final State parent;
    private final double kilometers;
    private final int weight;
    private final int capacity;
    private double estimated_cost;

    public State(Topology.City actual_city, HashMap<Task, TASK_STATE> task_states, State parent, double kilometers, int weight, int capacity) {
        assert(actual_city != null);
        this.weight     = weight;
        this.parent      = parent;
        this.actual_city = actual_city;
        this.task_states = Collections.unmodifiableMap(task_states);
        this.kilometers = kilometers;
        this.capacity   = capacity;
        this.estimated_cost = -1.0;
    }

    public Topology.City getActual_city() {
        return actual_city;
    }

    public HashMap<Task, TASK_STATE> getTaskState() {
        return new HashMap<>(task_states);
    }

    public State move_to_city(Task task, TASK_STATE state) {
        HashMap<Task, TASK_STATE> new_states = new HashMap<>(task_states);

        int task_weight = 0;

        Topology.City city_dst = null;

        switch(state){
            case PENDING:
                city_dst = task.pickupCity;
                task_weight = task.weight;
                new_states.replace(task, TASK_STATE.ACTIVE);
                break;
            case ACTIVE:
                task_weight = -task.weight;
                city_dst = task.deliveryCity;
                new_states.replace(task, TASK_STATE.FINISHED);
                break;
            default:
                throw new AssertionError();
        }

        if(city_dst == null){
            throw new RuntimeException();
        }


        return new State(city_dst, new_states, this, kilometers + actual_city.distanceTo(city_dst), weight + task_weight, capacity);
    }

    public ArrayList<State> generateChild(){
        ArrayList<State> states = new ArrayList<State>();

        this.task_states.forEach((task, state) -> {
            if(state != TASK_STATE.FINISHED && !(state == TASK_STATE.PENDING && ((weight+task.weight) > capacity))) {
                states.add(move_to_city(task, state));
            }
        });

        return states;
    }

    public State getParent() {
        return parent;
    }

    /**
     * Compute the difference with the parent
     * @return TaskState the Task and the State that has changed
     */
    public Task computeDifference() {

        if (getParent() != null) {
            HashMap<Task, TASK_STATE> list_parent = getParent().getTaskState();
            HashMap<Task, TASK_STATE> list_current = this.getTaskState();

            for (Task task : list_current.keySet()) {

                if (list_parent.get(task) != list_current.get(task)) {
                    return task;
                }
            }
        }

        return null;
    }

    public double getKilometers(){
        return kilometers;
    }
    public double getKilometersAstar(){
        if(this.estimated_cost >= 0.0) {
            return (this.kilometers + this.estimated_cost);
        }

        List<Double> costs = new ArrayList<>();

        task_states.forEach((task, task_state) -> {
            Double dist;
            if(task_state == TASK_STATE.PENDING){
                dist = actual_city.distanceTo(task.pickupCity) + task.pickupCity.distanceTo(task.deliveryCity);
                costs.add(dist);
            } else if (task_state == TASK_STATE.ACTIVE){
                dist = actual_city.distanceTo(task.deliveryCity);
                costs.add(dist);
            } else {
                costs.add(0.0);
            }
        });

        this.estimated_cost = Collections.max(costs);

        return (this.kilometers + this.estimated_cost);
    }

    @Override
    public String toString() {
        return "State{" +
                "actual_city=" + actual_city +
                ", task_states=" + task_states.values().toString() +
                ", parent=" + (parent != null ? parent.toString() : "null") +
                ", kilometers=" + kilometers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return actual_city.equals(state.actual_city) &&
                task_states.equals(state.task_states);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actual_city, task_states);
    }

    enum TASK_STATE {
        PENDING, ACTIVE, FINISHED
    }




}
