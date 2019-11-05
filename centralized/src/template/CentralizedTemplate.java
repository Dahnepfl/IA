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

        HashMap<template.TaskState, template.TaskState> nextTask = new HashMap<>();
        HashMap<Vehicle, template.TaskState> nextTaskVehicle = new HashMap<>();
        HashMap<template.TaskState, Integer> time = new HashMap<>();
        HashMap<template.TaskState, Vehicle> vehicle = new HashMap<>();

        ArrayList<Task> task_list = new ArrayList<>(tasks);

        Collections.shuffle(task_list);

        ArrayList<TaskState> vehicules_last_task = new ArrayList<>();
        vehicles.forEach(vehicle1 -> vehicules_last_task.add(null));

        int j = 0;
        for(int i = 0; i < task_list.size(); i++) {
      //      int index_ve = (int) Math.floor(Math.random()*vehicles.size());
            int index_ve = 0;
            double smallest_dist = Double.MAX_VALUE;
            for(int k = 0; k < vehicles.size(); k++){
                double dist = vehicles.get(k).getCurrentCity().distanceTo(task_list.get(i).pickupCity);
                if(dist < smallest_dist){
                    smallest_dist = dist;
                    index_ve = k;
                }
            }
            Vehicle v = vehicles.get(index_ve);

            System.out.println("Add to v " + index_ve);

            if(task_list.get(i).weight > v.capacity()) {
                for (Vehicle vv :
                        vehicles) {
                    if (vv.capacity() > v.capacity())
                        v = vv;
                }
            }

            if(task_list.get(i).weight > v.capacity()){
                throw new RuntimeException("Unsolvable");
            }

            if(vehicules_last_task.get(index_ve) == null){
                nextTaskVehicle.put(vehicles.get(index_ve), new TaskState(STATE.PICKUP, task_list.get(i)));
            } else {
                nextTask.put(vehicules_last_task.get(index_ve), new TaskState(STATE.PICKUP, task_list.get(i)));
            }
            vehicules_last_task.set(index_ve, new TaskState(STATE.DELIVER, task_list.get(i)));
            nextTask.put(new TaskState(STATE.PICKUP, task_list.get(i)), new TaskState(STATE.DELIVER, task_list.get(i)));


            time.put(new TaskState(STATE.PICKUP, task_list.get(i)), j);
            j++;
            time.put(new TaskState(STATE.DELIVER, task_list.get(i)), j);
            j++;
            vehicle.put(new TaskState(STATE.PICKUP, task_list.get(i)), v);
            vehicle.put(new TaskState(STATE.DELIVER, task_list.get(i)), v);
        }

        vehicules_last_task.forEach(taskState ->
        {
            nextTask.put(taskState, null);
        });

        for(int i = 0; i < vehicles.size(); i++){
            if(vehicules_last_task.get(i) == null){
                nextTaskVehicle.put(vehicles.get(i), null);
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

        Assignment a = findAssignment(vehicles, tasks);
        List<Plan> plan = assignmentToPlan(a, vehicles);


        while (plan.size() < vehicles.size()) {
            plan.add(Plan.EMPTY);
        }

        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");

        System.out.println(plan);
        
        return plan;
    }

    private List<Plan> assignmentToPlan(Assignment a, List<Vehicle> vvv) {
        HashMap<TaskState, Vehicle> vehicles = a.getVehicle();
        HashMap<TaskState, TaskState> next_tasks = a.getNextTask();
        HashMap<Vehicle, TaskState> next_tasks_v = a.getNextTaskVehicle();
        HashMap<TaskState, Integer> time = a.getTime();


        List<Plan> plans = new ArrayList<>();

        for(int i = 0; i < vvv.size(); i++){
            Vehicle vehicle = vvv.get(i);
            TaskState taskState = next_tasks_v.get(vehicle);

            Plan plan = new Plan(vehicle.getCurrentCity());


            if(taskState != null) {
                if(taskState.getState() != STATE.PICKUP){
                    throw new IllegalStateException();
                }
                moveTo(plan, vehicle.getCurrentCity(), taskState.getTask().pickupCity);
                City last_city = taskState.getTask().pickupCity;
                plan.appendPickup(taskState.getTask());
                TaskState t = taskState;
                while(next_tasks.get(t) != null){
                    t = next_tasks.get(t);
                    if (t.getState() == STATE.PICKUP) {
                        moveTo(plan, last_city, t.getTask().pickupCity);
                        last_city = t.getTask().pickupCity;
                        plan.appendPickup(t.getTask());
                    } else {
                        moveTo(plan, last_city, t.getTask().deliveryCity);
                        last_city = t.getTask().deliveryCity;
                        plan.appendDelivery(t.getTask());
                    }
                }
            }
            System.out.println(plan.toString());
            plans.add(i, plan);
        }

        return plans;
    }

    private void moveTo(Plan plan, City c1, City c2){
        List<City> cities = c1.pathTo(c2);
        for (City city : cities) {
            plan.appendMove(city);
        }
    }

    private template.Assignment findAssignment(List<Vehicle> vehicles, TaskSet tasks) {
        template.Assignment Aold = SelectInitialSolution(tasks, vehicles);

        int i = 50000;
        template.Assignment best = Aold;
        int same = 0;
        while(i-- > 0){
            List<template.Assignment> assignments = Aold.chooseNeighbours(vehicles, tasks);
            template.Assignment A = LocalChoice(assignments);
            if(Math.random() < this.p){
                Aold = A;
            }

            if(A.total_cost() < best.total_cost())
            {
                best = A;
            } else{
                same++;
            }

            if(same >= 10){
                same = 0;
                Aold = assignments.get((int) Math.floor(Math.random()*assignments.size()));
            }

            if(i%2==0)
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
