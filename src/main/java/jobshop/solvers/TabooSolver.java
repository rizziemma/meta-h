package jobshop.solvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.Main.arg;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

public class TabooSolver implements Solver {

	
	int maxIter;
	int dureeTaboo;
	int [][][] sTaboo;

	
	//def pour utiliser dans les for each
	int min;
	Swap swap;
	Schedule sol;
	int k;
	
    public int getMaxIter() {
		return maxIter;
	}

	public void setMaxIter(int maxIter) {
		this.maxIter = maxIter;
	}

	static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
    }

    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
            Task t = order.tasksByMachine[this.machine][t1];
            order.tasksByMachine[this.machine][t1] = order.tasksByMachine[this.machine][t2];
            order.tasksByMachine[this.machine][t2] = t;
        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {
    	this.maxIter = 10000;
    	this.dureeTaboo = 20;
    	this.sTaboo = new int[instance.numMachines][instance.numJobs][instance.numJobs]; //init a 0 par defaut
    	
    	sol = new GloutonSolver(arg.SPT).solve(instance, deadline).schedule;
    	Schedule best = sol;
    	k = 0;
    	while(k<= this.maxIter) {
    		if(System.currentTimeMillis() > deadline) {
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

    /** Returns a list of all blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {
        List<Task> critical_path = order.toSchedule().criticalPath();
        List<Block> blocks = new ArrayList<Block>();
        
        int t1 = 0;
        int t2 = 1;
        while(t2<critical_path.size()-1) {
        	int m = order.instance.machine(critical_path.get(t1));
        	while(t2<critical_path.size()-1 && order.instance.machine(critical_path.get(t2)) == m) {
        		t2++;
        	}
        	if(t2-1 > t1) { //au moins 2 taches d'affilée sur la même machine
        		List<Task> l = Arrays.asList(order.tasksByMachine[m]);
        		blocks.add(new Block(m, l.indexOf(critical_path.get(t1)) ,l.indexOf(critical_path.get(t2-1)) ));
        	}
        	t1 = t2; //replace la première tache a comparer
        	t2++;
        }
        
        
        return blocks;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
        List<Swap> swaps = new ArrayList<Swap>();
        if(block.lastTask == block.firstTask+1) { //block de taille 2 => un seul swap
        	swaps.add(new Swap(block.machine, block.firstTask, block.lastTask));
        }else { //block de taille > 2 => 2 swaps
        	swaps.add(new Swap(block.machine, block.firstTask, block.firstTask +1));
        	swaps.add(new Swap(block.machine, block.lastTask-1, block.lastTask));
        }
        
        return swaps;
    }

}