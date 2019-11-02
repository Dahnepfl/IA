package template;

//the list of imports
import java.io.File;
import java.util.*;

import logist.LogistSettings;

import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */
@SuppressWarnings("unused")
public class CentralizedTemplate implements CentralizedBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    private double p;


    private template.Assignment SelectInitialSolution(TaskSet tasks, List<Vehicle> vehicles){
        if(tasks == null || vehicles == null || tasks.isEmpty() || vehicles.isEmpty()){
            throw new IllegalArgumentException();
        }
        Vehicle v = vehicles.get(0);
        for (Vehicle vehicle :
                vehicles) {
            if(vehicle.capacity() > v.capacity())
                v = vehicle;
        }

        HashMap<template.TaskState, template.TaskState> nextTask = new HashMap<>();
        HashMap<Vehicle, template.TaskState> nextTaskVehicle = new HashMap<>();
        HashMap<template.TaskState, Integer> time = new HashMap<>();
        HashMap<template.TaskState, Vehicle> vehicle = new HashMap<>();

        ArrayList<Task> task_list = new ArrayList<>(tasks);

        Collections.shuffle(task_list);

        int j = 0;
        for(int i = 0; i < task_list.size(); i++) {
            if(task_list.get(i).weight > v.capacity()){
                throw new RuntimeException("Unsolvable");
            }

            if(i == task_list.size() - 1){
                nextTask.put(new TaskState(STATE.PICKUP, task_list.get(i)), new TaskState(STATE.DELIVER, task_list.get(i)));
                nextTask.put(new TaskState(STATE.DELIVER, task_list.get(i)), null);
            } else {
                nextTask.put(new TaskState(STATE.PICKUP, task_list.get(i)), new TaskState(STATE.DELIVER, task_list.get(i)));
                nextTask.put(new TaskState(STATE.DELIVER, task_list.get(i)), new TaskState(STATE.PICKUP, task_list.get(i+1)));
            }

            time.put(new TaskState(STATE.PICKUP, task_list.get(i)), j);
            j++;
            time.put(new TaskState(STATE.DELIVER, task_list.get(i)), j);
            j++;
            vehicle.put(new TaskState(STATE.PICKUP, task_list.get(i)), v);
            vehicle.put(new TaskState(STATE.DELIVER, task_list.get(i)), v);
        }

        for (Vehicle vehicle1 :
                vehicles) {
            if (vehicle1.equals(v)) {
                nextTaskVehicle.put(vehicle1, new TaskState(STATE.PICKUP, task_list.get(0)));
            } else {
                nextTaskVehicle.put(vehicle1, null);
            }
        }

        return new template.Assignment(nextTask, nextTaskVehicle, time, vehicle);
    }

    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("HERE");
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);

        this.p = 0.5;
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        
//		System.out.println("Agent " + agent.id() + " has tasks " + tasks);

        Plan planVehicle1 = naivePlan(vehicles.get(0), tasks);
        findAssignment(vehicles, tasks);

        List<Plan> plans = new ArrayList<Plan>();
        plans.add(planVehicle1);
        while (plans.size() < vehicles.size()) {
            plans.add(Plan.EMPTY);
        }
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");
        
        return plans;
    }

    private template.Assignment findAssignment(List<Vehicle> vehicles, TaskSet tasks) {
        template.Assignment Aold = SelectInitialSolution(tasks, vehicles);

        int i = 10000;
        template.Assignment best = Aold;
        while(i-- > 0){
            List<template.Assignment> assignments = Aold.chooseNeighbours(vehicles, tasks);

            template.Assignment A = LocalChoice(assignments);
            if(Math.random() > this.p){
                Aold = assignments.get((int) Math.floor(Math.random()*assignments.size()));
            } else {
                Aold = A;
            }

            if(A.total_cost() < best.total_cost())
            {
                best = A;
            }

            System.out.println(i + " " + assignments.size() + " " + A.total_cost() + " Best : " + best.total_cost());
        }

        return best;
    }

    private template.Assignment LocalChoice(List<template.Assignment> assignments) {
        if(assignments == null || assignments.isEmpty()){
            throw new IllegalArgumentException();
        }
        return Collections.min(assignments, Comparator.comparingDouble(template.Assignment::total_cost));

        /*System.out.println("END SORT");

        int index = 0;
        template.Assignment best = assignments.get(0);
        for (template.Assignment a :
                assignments) {
            if(a.total_cost() <= (best.total_cost() + 1))
                index++;
            else
                break;
        }

        return assignments.get((int) Math.floor(Math.random()*index));*/
    }

    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }
}
