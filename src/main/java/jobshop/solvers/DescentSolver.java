package jobshop.solvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jobshop.Instance;
import jobshop.Main.arg;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

public class DescentSolver implements Solver {

	//def pour utiliser dans les for each
	Schedule sol;
	boolean update;
    
	
	/** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
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

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
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
    	sol = new GloutonSolver(arg.EST_SPT).solve(instance, deadline).schedule;
    	
    	update = true;
    	while(update) {
    		if(deadline - System.currentTimeMillis() <= 1) {
    			return new Result(instance, sol, Result.ExitCause.Timeout);
    		}else {
    			update = false;
    			
    			//liste toutes les solutions voisines
    			ResourceOrder r_sol = new ResourceOrder(sol);
    			ResourceOrder r = r_sol.copy();
    			blocksOfCriticalPath(r_sol).forEach((b) -> { 
    					neighbors(b).forEach((s)-> {
    						s.applyOn(r);
    						Schedule s_current = r.toSchedule(); //verifie si on a une solution valide après swap
    						if(s_current != null && s_current.makespan() < sol.makespan()) {
    							  sol = s_current; //meilleure solution
    							  update = true;
    						}
    						s.applyOn(r);  //reapplique le swap pour revenir a la solution initiale
    						
    					});
    			});
    					
    		}
    	}
        
    	
    	return new Result(instance, sol, Result.ExitCause.Blocked);
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