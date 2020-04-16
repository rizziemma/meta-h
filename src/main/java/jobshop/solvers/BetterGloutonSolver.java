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

public class BetterGloutonSolver implements Solver {
	Main.arg arg;
	public BetterGloutonSolver(Main.arg arg) {
		super();
		this.arg = arg;
	}
	
	//ajoute toutes les taches possible a chaque iterations : possible car nextToSet[m] garanti de ne pas écraser de taches precedentes, mais pas l'heuristique attendue
	//SPT == SRPT et LPT == LRPT dans les tests

	
    @Override
    public Result solve(Instance instance, long deadline) {

    	//structure pour garder en memoire quelle tache a déjà ete placee sans parcourir sol.resources 
    	int[][] set = new int[instance.numJobs][instance.numTasks];
    	for (int[] row: set)
            Arrays.fill(row, 0);
 
		ArrayList<Task> tasks;
		ResourceOrder sol = new ResourceOrder(instance);
		
		
		do {
			tasks = next(instance, set);			
			tasks.forEach((t)-> {int m = instance.machine(t.job, t.task);  
								sol.tasksByMachine[m][sol.nextFreeSlot[m]++]=t;
								set[t.job][t.task] = 1;
								});				
			
		}while(!tasks.isEmpty()); 
		        
        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
    }
    
    private ArrayList<Task> next(Instance instance, int[][] set){ 
    	//liste des prochaines taches possibles non triee
    	ArrayList<Task> output = new ArrayList<Task>();
    	
    	//pour chaque job, ajoute la prochaine tache a faire
    	for(int j = 0 ; j<instance.numJobs ; j++) {
            for(int t = 0 ; t<instance.numTasks ; t++) {
            	if(set[j][t] == 0) {
            		output.add(new Task(j,t));
            		break;
            	}
            }
    	}
    	
    	//tri selon la methode en arg
    	switch(this.arg) {
    	case SPT : // plus petite duration en premier
    		Collections.sort(output, (a,b)->Integer.compare(instance.duration(a.job, a.task), instance.duration(b.job, b.task)));
    		break;
    		
    		
    	case LPT : //plus longue duration en premier
    		Collections.sort(output, (a,b)->Integer.compare(instance.duration(b.job, b.task), instance.duration(a.job, a.task)));
    		break;
    		
    		
    	case SRPT : //tri selon le plus court temps restant de chaque job
    		Collections.sort(output, new Comparator<Task>() {
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
    		
    		
    	case LRPT :
    		Collections.sort(output, new Comparator<Task>() {
    			public int compare(Task a, Task b) {
    				int remain_a = 0;
        			for(int i = a.task ; i<= instance.numTasks; i++) {
        				remain_a += instance.duration(a.job, a.task);
        			}
        			
        			int remain_b = 0;
        			for(int i = b.task ; i<= instance.numTasks; i++) {
        				remain_b += instance.duration(b.job, b.task);
        			}
        			
        			return Integer.compare(remain_b, remain_a);
    		}});
    		break;
    	}
    	return output;
    
	}
}