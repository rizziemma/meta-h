package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/** Représentation par numéro de job. */
public class JobNumbers extends Encoding {

    /** A numJobs * numTasks array containing the representation by job numbers. */
    public final int[] jobs;

    /** In case the encoding is only partially filled, indicates the index of first
     * element of `jobs` that has not been set yet. */
    public int nextToSet = 0;

    public JobNumbers(Instance instance) {
        super(instance);

        jobs = new int[instance.numJobs * instance.numMachines];
        Arrays.fill(jobs, -1);
    }

    @Override
    public Schedule toSchedule() {
        // time at which each machine is going to be freed
        int[] nextFreeTimeResource = new int[instance.numMachines];

        // for each job, the first task that has not yet been scheduled
        int[] nextTask = new int[instance.numJobs];

        // for each task, its start time
        int[][] startTimes = new int[instance.numJobs][instance.numTasks];

        // compute the earliest start time for every task of every job
        for(int job : jobs) {
            int task = nextTask[job];
            int machine = instance.machine(job, task);
            // earliest start time for this task
            int est = task == 0 ? 0 : startTimes[job][task-1] + instance.duration(job, task-1);
            est = Math.max(est, nextFreeTimeResource[machine]);

            startTimes[job][task] = est;
            nextFreeTimeResource[machine] = est + instance.duration(job, task);
            nextTask[job] = task + 1;
        }

        return new Schedule(instance, startTimes);
    }
    
    
    public void fromSchedule(Schedule s) {
    	ArrayList<TaskDate> tasks = new ArrayList<TaskDate>();
    	for(int job = 0 ; job < instance.numJobs; job++) {
    		for(int task = 0 ; task < instance.numTasks; task++) {
    			tasks.add(new TaskDate(s.startTime(job, task), new Task(job, task), instance.machine(job,task))); //insert chaque task avec sa date de debut
    		}
    	}
    	Collections.sort(tasks); //trie toutes les tasks selon leur date de debut
    	
    	tasks.forEach((task) -> jobs[nextToSet++]=task.task.job); //insert le num du job dans la liste des jobs -> ordre ok car tri par date de debut
    }

    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOfRange(jobs,0, nextToSet));
    }
    
    @Override
  	public boolean equals(Object o) {
  		if (this == o) return true;
          if (o == null || getClass() != o.getClass()) return false;
          JobNumbers r = (JobNumbers) o;
          return Arrays.equals(jobs,r.jobs);
  	}
}
