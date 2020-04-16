package jobshop.solvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import jobshop.Instance;
import jobshop.Main;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;


public class GloutonSolver implements Solver {
	Main.arg arg;
	public GloutonSolver(Main.arg arg) {
		super();
		this.arg = arg;
	}
	
    @Override
    public Result solve(Instance instance, long deadline) {

    	//structure pour garder en memoire quelle tache a déjà ete placee sans parcourir sol.resources 
    	int[][] set = new int[instance.numJobs][instance.numTasks];
    	for (int[] row: set)
            Arrays.fill(row, 0);
 
		Task t = next(instance, set);
		ResourceOrder sol = new ResourceOrder(instance);
		
		
		while(t != null){
					
			int m = instance.machine(t.job, t.task);  
			sol.resources[m][sol.nextToSet[m]++]=t;
			set[t.job][t.task] = 1;
			
			t = next(instance, set);							
			
		} 
		        
        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
    }
    
    private Task next(Instance instance, int[][] set){ 
    	Task output = null;
    	//liste des prochaines taches possibles non triee
    	ArrayList<Task> possible = new ArrayList<Task>();
    	
    	//pour chaque job, ajoute la prochaine tache a faire
    	for(int j = 0 ; j<instance.numJobs ; j++) {
            for(int t = 0 ; t<instance.numTasks ; t++) {
            	if(set[j][t] == 0) {
            		possible.add(new Task(j,t));
            		break;
            	}
            }
    	}
    	if(possible.isEmpty()) {
    		return null;
    	}
    	//tri selon la methode en arg
    	switch(this.arg) {
    	case SPT : // plus petite duration
    		output = Collections.min(possible, (a,b)->Integer.compare(instance.duration(a.job, a.task), instance.duration(b.job, b.task)));
    		break;
    		
    		
    	case LPT : //plus longue duration
    		output = Collections.max(possible, (a,b)->Integer.compare(instance.duration(a.job, a.task), instance.duration(b.job, b.task)));
    		break;
    		
    		
    	case SRPT : //plus petit temps restant
    		output = Collections.min(possible, new Comparator<Task>() {
    			public int compare(Task a, Task b) {
    				int remain_a = 0;
        			for(int i = a.task ; i<= instance.numTasks; i++) {
        				remain_a += instance.duration(a.job, a.task);
        			}
        			
        			int remain_b = 0;
        			for(int i = b.task ; i<= instance.numTasks; i++) {
        				remain_b += instance.duration(b.job, b.task);
        			}
        			
        			return Integer.compare(remain_a, remain_b);
    		}});
    		break;
    		
    		
    	case LRPT ://plus grand temps restant
    		output = Collections.max(possible, new Comparator<Task>() {
    			public int compare(Task a, Task b) {
    				int remain_a = 0;
        			for(int i = a.task ; i<= instance.numTasks; i++) {
        				remain_a += instance.duration(a.job, a.task);
        			}
        			
        			int remain_b = 0;
        			for(int i = b.task ; i<= instance.numTasks; i++) {
        				remain_b += instance.duration(b.job, b.task);
        			}
        			
        			return Integer.compare(remain_a, remain_b);
    		}});
    		break;
    	}
    	return output;
    
	}
}