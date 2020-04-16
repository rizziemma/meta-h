package jobshop.encodings;

public class TaskDate implements Comparable<TaskDate> {
	public int start;
	public Task task;
	public int machine;
	public TaskDate(int start, Task task, int machine)  {
		super();
		this.start = start;
		this.task = task;
		this.machine = machine;
	}
	
	@Override
	public int compareTo(TaskDate o) {
		return Integer.compare(this.start, o.start);
	}
}