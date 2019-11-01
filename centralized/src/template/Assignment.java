package template;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;
import org.omg.SendingContext.RunTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Assignment {
    private final HashMap<template.TaskState, template.TaskState> nextTask;
    private final HashMap<Vehicle, template.TaskState> nextTaskVehicle;
    private final HashMap<template.TaskState, Integer> time;
    private final HashMap<template.TaskState, Vehicle> vehicle;

    public Assignment(HashMap<template.TaskState, template.TaskState> nextTask, HashMap<Vehicle, template.TaskState> nextTaskVehicle, HashMap<template.TaskState, Integer> time, HashMap<template.TaskState, Vehicle> vehicle) {
        this.nextTask = nextTask;
        this.nextTaskVehicle = nextTaskVehicle;
        this.time = time;
        this.vehicle = vehicle;
    }

    private Vehicle randomVehicleWithTask() {
        ArrayList<Vehicle> list = new ArrayList<>();
        nextTaskVehicle.forEach((vehicle1, task) -> {
            if (task != null) {
                list.add(vehicle1);
            }
        });

        int size = list.size();
        int index = (int) Math.floor(Math.random() * size);

        return list.get(index);
    }

    public List<Assignment> chooseNeighbours(List<Vehicle> vehicles, TaskSet tasks) {
        List<Assignment> assignments = new ArrayList<>();

        Vehicle random_vehicle = randomVehicleWithTask();

        for (Vehicle v : vehicles) {
            if (!v.equals(random_vehicle)) {
                TaskState t = this.nextTaskVehicle.get(random_vehicle);
                if (t.getTask().weight <= v.capacity()) {
                    assignments.add(changingVehicle(random_vehicle, v));
                }
            }
        }

        int length = 0;
        TaskState t = nextTaskVehicle.get(random_vehicle);

        while (t != null) {
            t = nextTask.get(t);
            length = length + 1;
        }

        if (length >= 2) {
            for (int i = 1; i < length; i++) {
                for (int j = i + 1; j <= length; j++) {
                    Assignment a = ChangingTaskOrder(random_vehicle, i, j);
                    if (a != null) {
                        assignments.add(a);
                    }
                }
            }
        }


        return assignments;
    }

    private template.Assignment ChangingTaskOrder(Vehicle vehicle, int i, int j) {
        HashMap<TaskState, TaskState> nextTask_new = new HashMap<>(this.nextTask);
        HashMap<Vehicle, TaskState> nextTaskVehicle_new = new HashMap<>(this.nextTaskVehicle);
        HashMap<TaskState, Integer> time_new = new HashMap<>(this.time);
        HashMap<TaskState, Vehicle> vehicle_new = new HashMap<>(this.vehicle);

        TaskState tPred = null;
        TaskState t1 = nextTaskVehicle_new.get(vehicle);
        int count = 1;
        while (count < i) {
            tPred = t1;
            t1 = nextTask_new.get(t1);
            count++;
        }
        TaskState tPost = nextTask_new.get(t1);


        TaskState tPred2 = t1;
        TaskState t2 = nextTask_new.get(tPred2);
        count++;

        while (count < j) {
            tPred2 = t2;
            t2 = nextTask_new.get(t2);
            count++;
        }

        TaskState tPost2 = nextTask_new.get(t2);

        if(t2.getState() == STATE.DELIVER){
            if(t1.getTask().id == t2.getTask().id){
            //    System.out.println("SAME");
                return null;
            } else{
    /*            System.out.println("cont");
                System.out.println(t1.getTask().id);
                System.out.println(t2.getTask().id);*/
            }
            TaskState temps = t1;

            while(!nextTask_new.get(temps).equals(t2)){
      //          System.out.println(temps.getTask().id + " NEXT : " + nextTask_new.get(temps).getTask().id);
                temps = nextTask_new.get(temps);
                if(temps.getTask().id == t2.getTask().id){
          //          System.out.println("SAME");
                    return null;
                }
            }
        }
     //   System.out.println("2start");
        if(t1.getState() == STATE.PICKUP){
            TaskState temps = t1;

            while(!nextTask_new.get(temps).equals(t2)){
          //      System.out.println(temps.getTask().id + " NEXT : " + nextTask_new.get(temps).getTask().id);
                temps = nextTask_new.get(temps);
                if(temps.getTask().id == t1.getTask().id){
         //           System.out.println("SAME");
                    return null;
                }
            }
        }
       // System.out.println("2end");



        if ((tPost == null && t2 == null) || (tPost != null && tPost.equals(t2))) {

            if (tPred == null) {
                if(t2.getState() == STATE.DELIVER)
                {
                    System.out.println("ERROR");
                    System.out.println("ERROR");
                    System.out.println("ERROR");
                    System.out.println("ERROR111");
                }
                nextTaskVehicle_new.replace(vehicle, t2);
            } else {
                nextTask_new.replace(tPred, t2);
            }
            nextTask_new.replace(t2, t1);
            nextTask_new.replace(t1, tPost2);
        } else {
            if (tPred == null) {
                if(t2.getState() == STATE.DELIVER)
                {
                    System.out.println("ERROR");
                    System.out.println("ERROR");
                    System.out.println("ERROR");
                    System.out.println("ERRORY");
                }
                nextTaskVehicle_new.replace(vehicle, t2);
            } else {
                nextTask_new.replace(tPred, t2);
            }
            nextTask_new.replace(tPred2, t1);
            nextTask_new.replace(t2, tPost);
            nextTask_new.replace(t1, tPost2);
        }

        UpdateTime(time_new, nextTask_new, nextTaskVehicle_new, vehicle);

        return new Assignment(nextTask_new, nextTaskVehicle_new, time_new, vehicle_new);
    }

    private template.Assignment changingVehicle(Vehicle v1, Vehicle v2) {
        HashMap<TaskState, TaskState> nextTask_new = new HashMap<>(this.nextTask);
        HashMap<Vehicle, TaskState> nextTaskVehicle_new = new HashMap<>(this.nextTaskVehicle);
        HashMap<TaskState, Integer> time_new = new HashMap<>(this.time);
        HashMap<TaskState, Vehicle> vehicle_new = new HashMap<>(this.vehicle);

        TaskState t1 = nextTaskVehicle_new.get(v1);

        TaskState pred = t1;
        TaskState t2 = new TaskState(STATE.DELIVER, t1.getTask());

     //   System.out.println("ICI");

        if (t1.getState() == STATE.PICKUP) {
    //        System.out.println("PICKUP");
      //      System.out.println("TASK ID : " + t1.getTask().id);
            while (!nextTask_new.get(pred).equals(t2)) {
                pred = nextTask_new.get(pred);
                if (pred == null) {
                    throw new IllegalArgumentException();
                }
            }

     //       System.out.println("TASK ID DE : " + nextTask_new.get(pred).getTask().id);


            nextTask_new.replace(pred, nextTask_new.get(t2));
            // vérifier le prééd etc
            // et pour le changing task, autant faire "is legal move"
        } else {
            throw new RuntimeException();
        }

        nextTaskVehicle_new.replace(v1, nextTask_new.get(t1));
        nextTask_new.replace(t1, t2);

        nextTask_new.replace(t2, nextTaskVehicle_new.get(v2));
        nextTaskVehicle_new.replace(v2, t1);

        UpdateTime(time_new, nextTask_new, nextTaskVehicle_new, v1);
        UpdateTime(time_new, nextTask_new, nextTaskVehicle_new, v2);

        vehicle_new.replace(t1, v2);
        vehicle_new.replace(t2, v2);

        return new Assignment(nextTask_new, nextTaskVehicle_new, time_new, vehicle_new);
    }

    private void UpdateTime(HashMap<TaskState, Integer> time_new, HashMap<TaskState, TaskState> nextTask_new, HashMap<Vehicle, TaskState> nextTaskVehicle_new, Vehicle v1) {
        TaskState t1 = nextTaskVehicle_new.get(v1);

        if (t1 != null) {
            time_new.replace(t1, 1);
            TaskState t2;
            do {
                t2 = nextTask_new.get(t1);
                if (t2 != null) {
                    time_new.replace(t2, time_new.get(t1) + 1);
                    t1 = t2;
                }
            } while (t2 != null);
        }
    }

    private double dist(TaskState Task1, TaskState Task2) {
        if (Task1 == null) {
            throw new IllegalArgumentException();
        }

        if (Task2 == null) {
            return 0;
        }

        Topology.City city1;
        Topology.City city2;

        if(Task1.getState()==STATE.PICKUP){
            city1 = Task1.getTask().pickupCity;
        } else {
            city1 = Task1.getTask().deliveryCity;
        }

        if(Task2.getState()==STATE.PICKUP){
            city2 = Task2.getTask().pickupCity;
        } else {
            city2 = Task2.getTask().deliveryCity;
        }



        return city1.distanceTo(city2);
    }

    private double dist(Vehicle v, TaskState task) {
        if (v == null) {
            throw new IllegalArgumentException();
        }

        if (task == null) {
            return 0;
        }

        if (nextTaskVehicle.get(v).getState() != STATE.PICKUP) {
            throw new IllegalArgumentException();
        }

        return v.getCurrentCity().distanceTo(task.getTask().pickupCity);
    }

    private double length(TaskState task) {
        if (task == null) {
            return 0;
        }

        return task.getTask().pickupCity.distanceTo(task.getTask().deliveryCity);
    }

    public double total_cost() {
        double sum = 0.0;

        Set<TaskState> tasks = nextTask.keySet();
        Set<Vehicle> vehicles = nextTaskVehicle.keySet();

        for (TaskState task :
                tasks) {
            double var = dist(task, nextTask.get(task));
            var *= vehicle.get(task).costPerKm();
            sum += var;
        }

        for (Vehicle v :
                vehicles) {
            sum += dist(v, nextTaskVehicle.get(v)) * v.costPerKm();
        }

        return sum;
    }

}
