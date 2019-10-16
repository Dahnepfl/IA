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

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeTemplate implements DeliberativeBehavior {

	enum Algorithm { BFS, ASTAR }

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

		// Compute the plan with the selected algorithm.
		switch (algorithm) {
			case ASTAR:
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

	private Plan bfsPlan(Vehicle vehicle, TaskSet tasks) {
		State initial_node = (new StateBuilder(vehicle.getCurrentCity(), tasks)).build();

		boolean cont = true;

		LinkedList<State> nodes = new LinkedList<>();
		nodes.add(initial_node);

		ArrayList<State> finalStates = new ArrayList<>();

		do{
			State first_node = nodes.pop();

			if(isGoalState(first_node)){
				finalStates.add(first_node);
			}



		} while(!nodes.isEmpty());

		return null;
	}

	private boolean isGoalState(State first_node) {
		for (TaskState taskState : first_node.getTaskState()) {
			if(taskState.getState() != State.TASK_STATE.FINISHED)
				return false;
		}

		return true;
	}

	private double computeCost(Vehicle vehicle, City city_from, City city_to){
		return vehicle.costPerKm()*city_from.distanceTo(city_to);
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

		if (!carriedTasks.isEmpty()) {
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
}
