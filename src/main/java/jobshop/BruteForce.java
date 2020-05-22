package jobshop;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

public class BruteForce {
	


	public static ArrayList<JobNumbers> allJobNumbers(Instance instance){
		int n = instance.numJobs * instance.numTasks;
		int [] elements = new int[n];
		int next_to_set = 0;
		for(int i = 0; i<instance.numJobs; i++) {
			for(int j = 0; j<instance.numTasks; j++) {
				elements[next_to_set++] = i;
			}
		}
		
		ArrayList<JobNumbers> result = new ArrayList<JobNumbers>();
		
		//algo de heap iteratif
		//toutes les permutations de jobs possibles
		int[] indexes = new int[n];		 
		result.add(new JobNumbers(instance, elements));
		int i = 0;
		while (i < n) {
		    if (indexes[i] < i) {
		        swap(elements, i % 2 == 0 ?  0: indexes[i], i);
		        JobNumbers jobn = new JobNumbers(instance, elements);
		        if(!result.contains(jobn)) {
		        	result.add(jobn);
		        }
		        indexes[i]++;
		        i = 0;
		    } else {
		        indexes[i] = 0;
		        i++;
		    }
		}
		return result;	
	}
	

	
	public static ArrayList<ResourceOrder> allResourceOrder(Instance instance){
		ArrayList<ResourceOrder> result = new ArrayList<ResourceOrder>();
		
		int [] jobs = new int[instance.numJobs];
		for(int i = 0; i<instance.numJobs; i++) {
			jobs[i] = i;	
		}
		//chaque job utilise une fois chaque machine
		//toutes les permutations de jobs possibles
		ArrayList<int[]> permut_jobs = new ArrayList<int[]>();
		//algo de heap iteratif
		int[] indexes = new int[instance.numJobs];		 
		permut_jobs.add(jobs);
		int i = 0;
		while (i < instance.numJobs) {
		    if (indexes[i] < i) {
		        swap(jobs, i % 2 == 0 ?  0: indexes[i], i);
		        permut_jobs.add(jobs);
		        indexes[i]++;
		        i = 0;
		    } else {
		        indexes[i] = 0;
		        i++;
		    }
		}
		
		int nb_permut = permut_jobs.size();
		
		
		//Combinaisons de permutations
		int [] indexes_m = new int [instance.numMachines];
		int [] start = indexes_m.clone();
		do {
			ResourceOrder r = new ResourceOrder(instance);
			for(int machine = 0; machine<instance.numMachines; machine++) {
				//transforme le tableau d'int en tableau de tasks
				for(int j : permut_jobs.get(indexes_m[machine])) {
					r.tasksByMachine[machine][r.nextFreeSlot[machine]++] = new Task(j, instance.task_with_machine(j, machine));
				}
			}
			result.add(r);
			increment(indexes_m, nb_permut, instance.numMachines);
		}while(!Arrays.equals(indexes_m, start));
		
		
		
		return result;
	}
	
	private static void increment(int[] indexes, int n, int m) {
	    int place = 0;
	    while (place < m) {
	        if (indexes[place]+1 < n) {
	        	indexes[place] ++;
	        	break;
	        }else {
	            indexes[place++] = 0;
	        }
	    }
	}
				
	private static void swap(int[] input, int a, int b) {
			    int tmp = input[a];
			    input[a] = input[b];
			    input[b] = tmp;
	}
	
	public static void main(String[] args) {
		try {
			Instance instance = Instance.fromFile(Paths.get("instances/ft06"));
			//System.out.print("JobNumbers : \n");
			//System.out.print(allJobNumbers(instance).size());
			System.out.print("\nResourceOrder : \n");
			System.out.print(allResourceOrder(instance).size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
