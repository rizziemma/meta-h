/*package jobshop.encodings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

public class ResourceOrder extends Encoding {
	

	
    public final Task[][] resources;
    
    public int[] nextToSet; //l'index de la prochaine ressource a set sur chaque machine

	public int nextToSetList;

    public ResourceOrder(Instance instance) {
        super(instance);

        nextToSetList = 0;
        
        resources = new Task[instance.numMachines][instance.numJobs];
        for (Task[] row: resources)
            Arrays.fill(row, new Task(-1,-1));
        
        nextToSet = new int[instance.numMachines];
        Arrays.fill(nextToSet, 0);
    }
    
    public int[] findNext(int[][] startTimes) {
    	//find next task
    	for(int m = 0 ; m < instance.numMachines ; m++) {
    		for(int e = 0 ; e < instance.numJobs ; e++) {
    			int task = resources[m][e].task;
    			int job = resources[m][e].job;
    			if((startTimes[job][task] == -1) && (task == 0 || startTimes[job][task-1] >-1) && ((e==0)||(startTimes[resources[m][e-1].job][resources[m][e-1].task] > -1))) {
    				int[] result = {job,task,m};
    				return result;
    			}
    		}
    		
    	}
    	return null;
    }

    @Override
    public Schedule toSchedule() {
        // time at which each machine is going to be freed
        int[] nextFreeTimeResource = new int[instance.numMachines];

        // for each task, its start time
        int[][] startTimes = new int[instance.numJobs][instance.numTasks];
        for (int[] row: startTimes)
            Arrays.fill(row,-1);
        
        
        for(int t = instance.numJobs*instance.numTasks; t > 0 ; t--) {
        	int[] result = findNext(startTimes);
        	int job = result[0];
        	int task = result[1];
            int machine = result[2];
            // earliest start time for this task
            int est = task == 0 ? 0 : startTimes[job][task-1] + instance.duration(job, task-1);
            est = Math.max(est, nextFreeTimeResource[machine]);

            startTimes[job][task] = est;
            nextFreeTimeResource[machine] = est + instance.duration(job, task);
        }

        return new Schedule(instance, startTimes);
    }
    
    public void fromSchedule(Schedule s) {
    	ArrayList<ArrayList<TaskDate>> tasks = new ArrayList<ArrayList<TaskDate>>(); //tableau équivalent a la structure resources, avec pour chaque task sa date de début (classe TaskDate)
    	for(int m = 0 ; m < instance.numMachines; m++) {
    		tasks.add(new ArrayList<TaskDate>()); //initialise les listes de chaque machine
    	}
    	for(int job = 0 ; job < instance.numJobs; job++) {
    		for(int task = 0 ; task < instance.numTasks; task++) {
    			tasks.get(instance.machine(job,task)).add(new TaskDate(s.startTime(job, task), new Task(job, task), instance.machine(job,task)));//pour chaque tache, on linster dans la liste correspondant à la machine utilisée
    		}
    	}
    	tasks.forEach((array) -> Collections.sort(array)); //pour chaque machine, on trie selon la date de début
    	tasks.forEach((array) -> array.forEach((t) -> resources[t.machine][array.indexOf(t)] = t.task)); //adapte au format des resources
    	
    }
    

    @Override
	public boolean equals(Object o) {
		if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceOrder r = (ResourceOrder) o;
        return Arrays.deepEquals(resources,r.resources);
	}
 
}
*/



package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;

public class ResourceOrder extends Encoding {

    // for each machine m, taskByMachine[m] is an array of tasks to be
    // executed on this machine in the same order
    public final Task[][] tasksByMachine;

    // for each machine, indicate on many tasks have been initialized
    public final int[] nextFreeSlot;

    /** Creates a new empty resource order. */
    public ResourceOrder(Instance instance)
    {
        super(instance);

        // matrix of null elements (null is the default value of objects)
        tasksByMachine = new Task[instance.numMachines][instance.numJobs];

        // no task scheduled on any machine (0 is the default value)
        nextFreeSlot = new int[instance.numMachines];
    }

    /** Creates a resource order from a schedule. */
    public ResourceOrder(Schedule schedule)
    {
        super(schedule.pb);
        Instance pb = schedule.pb;

        this.tasksByMachine = new Task[pb.numMachines][];
        this.nextFreeSlot = new int[instance.numMachines];

        for(int m = 0 ; m<schedule.pb.numMachines ; m++) {
            final int machine = m;

            // for thi machine, find all tasks that are executed on it and sort them by their start time
            tasksByMachine[m] =
                    IntStream.range(0, pb.numJobs) // all job numbers
                            .mapToObj(j -> new Task(j, pb.task_with_machine(j, machine))) // all tasks on this machine (one per job)
                            .sorted(Comparator.comparing(t -> schedule.startTime(t.job, t.task))) // sorted by start time
                            .toArray(Task[]::new); // as new array and store in tasksByMachine

            // indicate that all tasks have been initialized for machine m
            nextFreeSlot[m] = instance.numJobs;
        }
    }

    @Override
    public Schedule toSchedule() {
        // indicate for each task that have been scheduled, its start time
        int [][] startTimes = new int [instance.numJobs][instance.numTasks];

        // for each job, how many tasks have been scheduled (0 initially)
        int[] nextToScheduleByJob = new int[instance.numJobs];

        // for each machine, how many tasks have been scheduled (0 initially)
        int[] nextToScheduleByMachine = new int[instance.numMachines];

        // for each machine, earliest time at which the machine can be used
        int[] releaseTimeOfMachine = new int[instance.numMachines];


        // loop while there remains a job that has unscheduled tasks
        while(IntStream.range(0, instance.numJobs).anyMatch(m -> nextToScheduleByJob[m] < instance.numTasks)) {

            // selects a task that has noun scheduled predecessor on its job and machine :
            //  - it is the next to be schedule on a machine
            //  - it is the next to be scheduled on its job
            // if there is no such task, we have cyclic dependency and the solution is invalid
            Optional<Task> schedulable =
                    IntStream.range(0, instance.numMachines) // all machines ...
                    .filter(m -> nextToScheduleByMachine[m] < instance.numJobs) // ... with unscheduled jobs
                    .mapToObj(m -> this.tasksByMachine[m][nextToScheduleByMachine[m]]) // tasks that are next to schedule on a machine ...
                    .filter(task -> task.task == nextToScheduleByJob[task.job])  // ... and on their job
                    .findFirst(); // select the first one if any

            if(schedulable.isPresent()) {
                // we found a schedulable task, lets call it t
                Task t = schedulable.get();
                int machine = instance.machine(t.job, t.task);

                // compute the earliest start time (est) of the task
                int est = t.task == 0 ? 0 : startTimes[t.job][t.task-1] + instance.duration(t.job, t.task-1);
                est = Math.max(est, releaseTimeOfMachine[instance.machine(t)]);
                startTimes[t.job][t.task] = est;

                // mark the task as scheduled
                nextToScheduleByJob[t.job]++;
                nextToScheduleByMachine[machine]++;
                // increase the release time of the machine
                releaseTimeOfMachine[machine] = est + instance.duration(t.job, t.task);
            } else {
                // no tasks are schedulable, there is no solution for this resource ordering
                return null;
            }
        }
        // we exited the loop : all tasks have been scheduled successfully
        return new Schedule(instance, startTimes);
    }

    /** Creates an exact copy of this resource order. */
    public ResourceOrder copy() {
        return new ResourceOrder(this.toSchedule());
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for(int m=0; m < instance.numMachines; m++)
        {
            s.append("Machine ").append(m).append(" : ");
            for(int j=0; j<instance.numJobs; j++)
            {
                s.append(tasksByMachine[m][j]).append(" ; ");
            }
            s.append("\n");
        }

        return s.toString();
    }

}
