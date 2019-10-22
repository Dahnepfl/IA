package template;

/* import table */

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.*;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeTemplate implements DeliberativeBehavior {

    private TaskSet carriedTasks;

    enum Algorithm {BFS, ASTAR, NAIVE}

    /* Environment */
    Topology topology;
    TaskDistribution td;

    /* the properties of the agent */
    Agent agent;
    int capacity;

    /* the planning class */
    Algorithm algorithm;

    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {
        this.topology = topology;
        this.td = td;
        this.agent = agent;

        // initialize the planner
        int capacity = agent.vehicles().get(0).capacity();
        String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

        // Throws IllegalArgumentException if algorithm is unknown
        algorithm = Algorithm.valueOf(algorithmName.toUpperCase());


        // ...
    }

    @Override
    public Plan plan(Vehicle vehicle, TaskSet tasks) {
        Plan plan;

        // Compute the plan with the selected algorithm.s
        switch (algorithm) {
            case ASTAR:
                // ...
                plan = astarPlan(vehicle, tasks);
                break;
            case NAIVE:
                // ...
                plan = naivePlan(vehicle, tasks);
                break;
            case BFS:
                // ...
                plan = bfsPlan(vehicle, tasks);
                break;
            default:
                throw new AssertionError("Should not happen.");
        }
        return plan;
    }

    private Plan astarPlan(Vehicle vehicle, TaskSet tasks) {
        // State depends on actually carried tasks
        State initial_node;
        if (this.carriedTasks == null || this.carriedTasks.isEmpty()) {
            initial_node = (new StateBuilder(vehicle.getCurrentCity(), vehicle.capacity())).build(tasks);
        } else {
            HashMap<Task, State.TASK_STATE> task_state = new HashMap<>();
            tasks.forEach(task -> {
                        task_state.put(task, State.TASK_STATE.PENDING);
                    }
            );
            this.carriedTasks.forEach(task -> {
                task_state.put(task, State.TASK_STATE.ACTIVE);
            });
            initial_node = (new StateBuilder(vehicle.getCurrentCity(), vehicle.capacity())).build(task_state);
        }

        boolean cont = true;

        LinkedList<State> nodes = new LinkedList<>();
        nodes.add(initial_node);

        State best_state = initial_node;
        double least_kilometers = Double.MAX_VALUE;



        do {
            State first_node = nodes.removeFirst();

            if (isGoalState(first_node)) {
                if(least_kilometers > first_node.getKilometers()) {
                    best_state = first_node;
                    break;
                }

            } else {
                ArrayList<State> new_nodes = first_node.generateChild();

                new_nodes.forEach(state -> {
                    int index = nodes.indexOf(state);
                    if (index < 0) {
                        nodes.add(state);
                    } else if (nodes.get(index).getKilometers() > state.getKilometers()) {
                        nodes.set(index, state);
                    }
                });
                nodes.sort(Comparator.comparingDouble(State::getKilometersAstar));
            }

        } while (!nodes.isEmpty());

        Plan plan = stateToPlan(best_state, vehicle.getCurrentCity());

        System.out.println(plan.toString());
        System.out.println(best_state.getKilometers()*vehicle.costPerKm());

        return plan;
    }

    private Plan stateToPlan(State state, City initial_city){
        Plan plan;

        if(state.getParent() == null){
            plan = new Plan(initial_city);
        } else {
            plan = stateToPlan(state.getParent(), initial_city);
            City city_from = state.getParent().getActual_city();
            City city_to = state.getActual_city();

            for (City city :
                    city_from.pathTo(city_to)) {
                plan.appendMove(city);
            }

            Task task = state.computeDifference();
            if (task == null) {
                throw new RuntimeException();
            }

            if (task.pickupCity.equals(state.getActual_city())) {
                plan.appendPickup(task);
            } else if (task.deliveryCity.equals(state.getActual_city())) {
                plan.appendDelivery(task);
            } else {
                throw new AssertionError();
            }
        }

        return plan;
    }

    private Plan bfsPlan(Vehicle vehicle, TaskSet tasks) {
        State initial_node;
        if (this.carriedTasks == null || this.carriedTasks.isEmpty()) {
            initial_node = (new StateBuilder(vehicle.getCurrentCity(), vehicle.capacity())).build(tasks);
        } else {
            HashMap<Task, State.TASK_STATE> task_state = new HashMap<>();
            tasks.forEach(task -> {
                        task_state.put(task, State.TASK_STATE.PENDING);
                    }
            );
            this.carriedTasks.forEach(task -> {
                task_state.put(task, State.TASK_STATE.ACTIVE);
            });
            initial_node = (new StateBuilder(vehicle.getCurrentCity(), vehicle.capacity())).build(task_state);
        }

        boolean cont = true;

        LinkedList<State> nodes = new LinkedList<>();
        nodes.add(initial_node);

        State best_state = initial_node;
        double actual_least_kilometers = Double.MAX_VALUE;

        do {
            State first_node = nodes.removeFirst();

            if (isGoalState(first_node)) {
                if(actual_least_kilometers > first_node.getKilometers()) {
                    best_state = first_node;
                    actual_least_kilometers = first_node.getKilometers();
                }

            } else {
                ArrayList<State> new_nodes = first_node.generateChild();

                new_nodes.forEach(state -> {
                    int index = nodes.indexOf(state);
                    if (index < 0) {
                        nodes.add(state);
                    } else if (nodes.get(index).getKilometers() > state.getKilometers()) {
                        nodes.set(index, state);
                    }
                });

            }

        } while (!nodes.isEmpty());

        Plan plan = stateToPlan(best_state, vehicle.getCurrentCity());

        System.out.println(plan.toString());

        return plan;
    }

    private boolean isGoalState(State first_node) {
        for (State.TASK_STATE state : first_node.getTaskState().values()) {
            if (state != State.TASK_STATE.FINISHED)
                return false;
        }

        return true;
    }

    private double computeCost(Vehicle vehicle, City city_from, City city_to) {
        return vehicle.costPerKm() * city_from.distanceTo(city_to);
    }

    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity))
                plan.appendMove(city);

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path())
                plan.appendMove(city);

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }

    @Override
    public void planCancelled(TaskSet carriedTasks) {
        System.out.println("herE");
        if (!carriedTasks.isEmpty()) {
            this.carriedTasks = carriedTasks;
            // This cannot happen for this simple agent, but typically
            // you will need to consider the carriedTasks when the next
            // plan is computed.

        }
    }
}
