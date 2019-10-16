package template;

import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.ArrayList;
import java.util.Collections;

public final class State {
    private final Topology.City actual_city;
    private final ArrayList<TaskState> task_states;

    public State(Topology.City actual_city, ArrayList<TaskState> task_states) {
        this.actual_city = actual_city;
        this.task_states = (ArrayList<TaskState>) Collections.unmodifiableList(task_states);
    }

    public Topology.City getActual_city() {
        return actual_city;
    }

    public ArrayList<TaskState> getTaskState() {
        return new ArrayList<>(task_states);
    }

    public State move_to_city(Topology.City city_dst, boolean pickup, boolean delivery) {
        if(pickup == delivery){
            throw new IllegalArgumentException("Pickup and delivery are both true/false !");
        }

        ArrayList<TaskState> new_states = new ArrayList<>(task_states);

        for (int i = 0; i < new_states.size(); i++) {
            if(pickup){
                Task task = new_states.get(i).getTask();
                TASK_STATE state = new_states.get(i).getState();

                if(task.pickupCity == city_dst && state == TASK_STATE.PENDING){
                    new_states.add(i, new TaskState(task, TASK_STATE.ACTIVE));
                }
            }

            if(delivery){
                Task task = new_states.get(i).getTask();
                TASK_STATE state = new_states.get(i).getState();

                if(task.deliveryCity == city_dst && state == TASK_STATE.ACTIVE){
                    new_states.add(i, new TaskState(task, TASK_STATE.FINISHED));
                }
            }
        }


        return new State(city_dst, new_states);
    }

    public ArrayList<State> generateTree(){
        ArrayList<State> states = new ArrayList<State>();

        this.getTaskState().forEach(taskState -> {
            Task task = taskState.getTask();
            TASK_STATE state = taskState.getState();

            switch (state){
                case PENDING:
                    states.add(move_to_city(task.pickupCity, true, false));
                    break;
                case ACTIVE:
                    states.add(move_to_city(task.deliveryCity, false, true));
                    break;
            }

        });

        return states;
    }

    enum TASK_STATE {
        PENDING, ACTIVE, FINISHED
    }




}
