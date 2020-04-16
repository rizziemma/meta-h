package jobshop.encodings;

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
