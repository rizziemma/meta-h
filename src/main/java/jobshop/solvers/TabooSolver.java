package jobshop.solvers;

import jobshop.Instance;
import jobshop.Main.arg;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.encodings.ResourceOrder;

public class TabooSolver extends DescentSolver {

	
	int maxIter;
	int dureeTaboo;
	int [][][] sTaboo;

	
	//def pour utiliser dans les for each
	int min;
	Swap swap;
	Schedule sol;
	int k;

    @Override
    public Result solve(Instance instance, long deadline) {
    	this.maxIter = 500;
    	this.dureeTaboo = 20;
    	this.sTaboo = new int[instance.numMachines][instance.numJobs][instance.numJobs]; //init a 0 par defaut
    	
    	sol = new GloutonSolver(arg.EST_SPT).solve(instance, deadline).schedule;
    	Schedule best = sol;
    	k = 0;
    	while(k < this.maxIter) {
    		if(deadline - System.currentTimeMillis() <= 1) {
    			return new Result(instance, sol, Result.ExitCause.Timeout);
    		}else {
    			
    			//liste toutes les solutions voisines
    			ResourceOrder r_sol = new ResourceOrder(sol);
    			ResourceOrder r = r_sol.copy();
    			min = Integer.MAX_VALUE;
    			blocksOfCriticalPath(r_sol).forEach((b) -> { 
    					neighbors(b).forEach((s)-> {
    						if(sTaboo[s.machine][s.t1][s.t2]<= k) {
    							s.applyOn(r);
    							Schedule s_current = r.toSchedule(); //verifie si on a une solution valide après swap
    							if(s_current != null ) {
    								int makespan = s_current.makespan();
    								if(makespan < min) {
    									sol = s_current;
    									swap = s;
    									min = makespan;
    								}
    							}
    						s.applyOn(r); //reapplique le swap pour revenir a la solution initiale
    						}
    					});
    			});
    			
    			//ajoute sol a sTaboo
    			sTaboo[swap.machine][swap.t2][swap.t1] = this.dureeTaboo + k;
    			
    			//update meilleure solution
    			if(min < best.makespan()) {
    				best = sol;
    			}
    			
    			k++;
    		}
    	}
        
    	
    	return new Result(instance, best, Result.ExitCause.Blocked);
    }



}