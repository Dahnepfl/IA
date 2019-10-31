package template;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Assignment {
    private final HashMap<Task, Task> nextTask;
    private final HashMap<Vehicle, Task> nextTaskVehicle;
    private final HashMap<Task, Integer> time; private final HashMap<Task, Vehicle> vehicle;
    public Assignment(HashMap<Task, Task> nextTask, HashMap<Vehicle, Task> nextTaskVehicle, HashMap<Task, Integer> time, HashMap<Task, Vehicle> vehicle) {
        this.nextTask = nextTask;
        this.nextTaskVehicle = nextTaskVehicle;
        this.time = time;
        this.vehicle = vehicle;
    }

    private Vehicle randomVehicleWithTask(){
        ArrayList<Vehicle> list = new ArrayList<>();
        nextTaskVehicle.forEach((vehicle1, task) -> {
            if(task != null){
                list.add(vehicle1);
            }
        });

        int size = list.size();
        int index = (int) Math.floor(Math.random()*size);

        return list.get(index);
    }

     public List<Assignment> chooseNeighbours(List<Vehicle> vehicles, TaskSet tasks){
        List<Assignment> assignments = new ArrayList<>();

        Vehicle random_vehicle = randomVehicleWithTask();
         if(nextTaskVehicle.get(random_vehicle) == null){
             System.out.println("ERROR ");
             System.out.println("ERROR ");
             System.out.println("ERROR ");
         }
        for(Vehicle v : vehicles)
        {
            if(!v.equals(random_vehicle)){
                Task t = this.nextTaskVehicle.get(random_vehicle);
                if(t.weight <= v.capacity()){
                    System.out.println("CHANGE...");
                    assignments.add(changingVehicle(random_vehicle, v));
                }
            }
        }

        int length = 0;
        Task t = nextTaskVehicle.get(random_vehicle);

        while(t != null){
            t = nextTask.get(t);
            length = length + 1;
        }

         System.out.println(length);

        if (length >= 2){
            for (int i = 1; i < length; i++) {
                for (int j = i + 1; j <= length; j++) {
                    Assignment a = ChangingTaskOrder(random_vehicle, i, j);
                    assignments.add(a);
                }
            }
        }


        return assignments;
    }

    private template.Assignment ChangingTaskOrder(Vehicle vehicle, int i, int j) {
        HashMap<Task, Task> nextTask_new = new HashMap<>(this.nextTask);
        HashMap<Vehicle, Task> nextTaskVehicle_new = new HashMap<>(this.nextTaskVehicle);
        HashMap<Task, Integer> time_new = new HashMap<>(this.time);
        HashMap<Task, Vehicle> vehicle_new = new HashMap<>(this.vehicle);

        Task tPred = null;
        Task t1 = nextTaskVehicle_new.get(vehicle);
        int count = 1;
        while (count < i)
        {
            tPred = t1;
            t1 = nextTask_new.get(t1);
            count++;
        }

        Task tPost = nextTask_new.get(t1);
        Task tPred2 = t1;
        Task t2 = nextTask_new.get(tPred2);
        count++;

        while (count < j)
        {
            tPred2 = t2;
            t2 = nextTask_new.get(t2);
            count++;
        }

        Task tPost2 = nextTask_new.get(t2);

        if ((tPost == null && t2 == null) || (tPost != null && tPost.equals(t2)))
        {
            if(tPred == null){
                nextTaskVehicle_new.replace(vehicle, t2);
            } else {
                nextTask_new.replace(tPred, t2);
            }
            nextTask_new.replace(t2, t1);
            nextTask_new.replace(t1, tPost2);
        } else {
            if(tPred == null){
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
        HashMap<Task, Task> nextTask_new = new HashMap<>(this.nextTask);
        HashMap<Vehicle, Task> nextTaskVehicle_new = new HashMap<>(this.nextTaskVehicle);
        HashMap<Task, Integer> time_new = new HashMap<>(this.time);
        HashMap<Task, Vehicle> vehicle_new = new HashMap<>(this.vehicle);

        Task t1 = nextTaskVehicle_new.get(v1);

        nextTaskVehicle_new.replace(v1, nextTask_new.get(t1));
        nextTask_new.replace(t1, nextTaskVehicle_new.get(v2));
        nextTaskVehicle_new.replace(v2, t1);

        UpdateTime(time_new, nextTask_new, nextTaskVehicle_new, v1);
        UpdateTime(time_new, nextTask_new, nextTaskVehicle_new, v2);

        vehicle_new.replace(t1, v2);

        return new Assignment(nextTask_new, nextTaskVehicle_new, time_new, vehicle_new);
    }

    private void UpdateTime(HashMap<Task, Integer> time_new, HashMap<Task, Task> nextTask_new, HashMap<Vehicle, Task> nextTaskVehicle_new, Vehicle v1) {
        Task t1 = nextTaskVehicle_new.get(v1);

        if(t1 != null){
            time_new.replace(t1, 1);
            Task t2;
            do{
                t2 = nextTask_new.get(t1);
                if(t2 != null){
                    time_new.replace(t2, time_new.get(t1) + 1);
                    t1 = t2;
                }
            } while (t2 != null);
        }
    }

    private double dist(Task Task1, Task Task2){
        if(Task1 == null){
            throw new IllegalArgumentException();
        }

        if(Task2 == null){
            return 0;
        }

        return Task1.deliveryCity.distanceTo(Task2.pickupCity);
    }

    private double dist(Vehicle v, Task task){
        if(v == null){
            throw new IllegalArgumentException();
        }

        if(task == null){
            return 0;
        }

        return v.getCurrentCity().distanceTo(task.pickupCity);
    }

    private double length(Task task){
        if (task == null){
            return 0;
        }

        return task.pickupCity.distanceTo(task.deliveryCity);
    }

   public double total_cost(){
        double sum = 0.0;

        Set<Task> tasks = nextTask.keySet();
        Set<Vehicle> vehicles = nextTaskVehicle.keySet();

        for (Task task :
                tasks) {
            sum += dist(task, nextTask.get(task))+ length(nextTask.get(task)) * vehicle.get(task).costPerKm();
        }

        for (Vehicle v :
                vehicles) {
            sum += dist(v, nextTaskVehicle.get(v))+ length(nextTaskVehicle.get(v)) * v.costPerKm();
        }

        return sum;
    }

}
