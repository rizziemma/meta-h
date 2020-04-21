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

    	ResourceOrder sol = new ResourceOrder(instance);
		Task t = next(instance, set, sol);
		
		
		while(t != null){
					
			int m = instance.machine(t.job, t.task);  
			sol.tasksByMachine[m][sol.nextFreeSlot[m]++]=t;
			set[t.job][t.task] = 1;
			
			t = next(instance, set, sol);							
			
		} 
		        
        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
    }
    
    private Task next(Instance instance, int[][] set, ResourceOrder sol){ 
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
    		output = Collections.min(possible, new SPT_comparator(instance));
    		break;
    		
    		
    	case LPT : //plus longue duration
    		output = Collections.max(possible, new LPT_comparator(instance));
    		break;
    		
    		
    	case SRPT : //plus petit temps restant
    		output = Collections.min(possible, new SRPT_comparator(instance));
    		break;
    		
    		
    	case LRPT ://plus grand temps restant
    		output = Collections.max(possible, new LRPT_comparator(instance));
    		break;
    		
    	case EST_SPT : // date de début le plus tôt + plus petite duration
    		output = Collections.min(possible, new EST_comparator(instance, sol).thenComparing(new SPT_comparator(instance)));
    		break;
    	
    	case EST_LRPT : // date de début le plus tôt + plus grand temps restant
    		output = Collections.min(possible, new EST_comparator(instance, sol).thenComparing(new LRPT_comparator(instance)));
    		break;
		}
    	
    	return output;
    
	}
    
    
    public class SPT_comparator implements Comparator<Task> {
    	Instance instance;
    	public SPT_comparator(Instance instance) {
    		super();
    		this.instance = instance;
    	}
    	public int compare(Task a, Task b) {
			return Integer.compare(instance.duration(a.job, a.task), instance.duration(b.job, b.task));
		}
    }
    
    public class LPT_comparator implements Comparator<Task> {
    	Instance instance;
    	public LPT_comparator(Instance instance) {
    		super();
    		this.instance = instance;
    	}
    	public int compare(Task a, Task b) {
			return Integer.compare(instance.duration(a.job, a.task), instance.duration(b.job, b.task));
		}
    }
    

	public class SRPT_comparator implements Comparator<Task> {
    	Instance instance;
    	public SRPT_comparator(Instance instance) {
    		super();
    		this.instance = instance;
    	}
    	
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
    	}
    }
	
	public class LRPT_comparator implements Comparator<Task> {
    	Instance instance;
    	public LRPT_comparator(Instance instance) {
    		super();
    		this.instance = instance;
    	}
    	
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
    	}
    }
	
	public class EST_comparator implements Comparator<Task> {
    	Instance instance;
    	ResourceOrder sol;
    	public EST_comparator(Instance instance,ResourceOrder sol ) {
    		super();
    		this.instance = instance;
    		this.sol = sol;
    	}
    	
    	public int compare(Task a, Task b) {
			int start_a = 0;
			int i = 0;
			while(sol.tasksByMachine[instance.machine(a)][i] != null) {
				start_a+=instance.duration(sol.tasksByMachine[instance.machine(a)][i]);
				i++;
			}
			
			int start_b = 0 ;
			i = 0;
			while(sol.tasksByMachine[instance.machine(b)][i] != null) {
				start_b+=instance.duration(sol.tasksByMachine[instance.machine(b)][i]);
				i++;
			}
			return Integer.compare(start_a, start_b);
    	}
    }
	
}