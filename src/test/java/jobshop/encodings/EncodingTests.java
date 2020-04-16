package jobshop.encodings;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.solvers.BasicSolver;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class EncodingTests {

    @Test
    public void testJobNumbers() throws IOException {
    	System.out.print("testJobNumbers \n");
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // numéro de jobs : 1 2 2 1 1 2 (cf exercices)
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();

        System.out.println(sched);
        assert sched.isValid();
        assert sched.makespan() == 12;



        // numéro de jobs : 1 1 2 2 1 2
        enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        sched = enc.toSchedule();
        System.out.println(sched);
        assert sched.isValid();
        assert sched.makespan() == 14;
    }


    @Test
    public void testResourceOrder() throws IOException {
    	System.out.print("testResourceOrder \n");
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        ResourceOrder enc = new ResourceOrder(instance);
        enc.resources[enc.nextToSetList++] = new Task[] {new Task(0,0), new Task(1,1)};
        enc.resources[enc.nextToSetList++] = new Task[] {new Task(1,0), new Task(0,1)};
        enc.resources[enc.nextToSetList++] = new Task[] {new Task(0,2), new Task(1,2)};


        Schedule sched = enc.toSchedule();

        System.out.println(sched);
        assert sched.isValid();
        assert sched.makespan() == 12;



        enc = new ResourceOrder(instance);
        enc.resources[enc.nextToSetList++] = new Task[] {new Task(1,1), new Task(0,0)};
        enc.resources[enc.nextToSetList++] = new Task[] {new Task(1,0), new Task(0,1)};
        enc.resources[enc.nextToSetList++] = new Task[] {new Task(0,2), new Task(1,2)};


        sched = enc.toSchedule();
        System.out.println(sched);
        assert sched.isValid();
        assert sched.makespan() == 16;
    }

    @Test
    public void testFromScheduleResourceOrder() throws IOException {
    	System.out.print("testFromScheduleResourceOrder \n");
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        ResourceOrder enc1 = new ResourceOrder(instance);
        enc1.resources[enc1.nextToSetList++] = new Task[] {new Task(0,0), new Task(1,1)};
        enc1.resources[enc1.nextToSetList++] = new Task[] {new Task(1,0), new Task(0,1)};
        enc1.resources[enc1.nextToSetList++] = new Task[] {new Task(0,2), new Task(1,2)};

        Schedule sched1 = enc1.toSchedule();
        System.out.println(sched1);       
        
        ResourceOrder enc2 = new ResourceOrder(instance);
        enc2.fromSchedule(sched1);
        System.out.println(enc2.toSchedule());
        assert enc1.equals(enc2);   

    }
    
    @Test
    public void testFromScheduleJobNumbers() throws IOException {
    	System.out.print("testFromScheduleJobNumbers \n");
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));
        
        JobNumbers enc1 = new JobNumbers(instance);
        enc1.jobs[enc1.nextToSet++] = 0;
        enc1.jobs[enc1.nextToSet++] = 1;
        enc1.jobs[enc1.nextToSet++] = 1;
        enc1.jobs[enc1.nextToSet++] = 0;
        enc1.jobs[enc1.nextToSet++] = 0;
        enc1.jobs[enc1.nextToSet++] = 1;
        Schedule sched1 = enc1.toSchedule();
        System.out.println(sched1);
        
        JobNumbers enc2 = new JobNumbers(instance);
        enc2.fromSchedule(sched1);
        Schedule sched2 = enc2.toSchedule();
        System.out.println(sched2);
        assert sched1.equals(sched2);   

    }
    
    @Test
    public void testBasicSolver() throws IOException {
    	System.out.print("testBasicSolver \n");
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // build a solution that should be equal to the result of BasicSolver
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();
        assert sched.isValid();
        assert sched.makespan() == 12;

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isValid();
        assert result.schedule.makespan() == sched.makespan(); // should have the same makespan
    }

}
