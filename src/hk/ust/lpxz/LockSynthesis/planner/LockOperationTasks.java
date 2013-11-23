package hk.ust.lpxz.LockSynthesis.planner;

import hk.ust.lpxz.fixing.DconPropertyManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.SootMethod;

public class LockOperationTasks {
	public static Set<LockOperationTask> tasks = new HashSet<LockOperationTask>();
	public static Set<LockOperationTask> getTasks()
	{
		return tasks;
	}
	public static void registerTask(LockOperationTask task)
	{
		if(DconPropertyManager.showLockOperationTasks)
			System.out.println(task.toString());
		tasks.add(task);
	}
	// no duplication
	public static List<Integer> getlockIDsAccessedByMethod(SootMethod method) {
		List<Integer>  toret = new ArrayList<Integer>(); 
		for(LockOperationTask lot: tasks)
		 {
			 if(lot.method==method)
			 {
				 if(!toret.contains(lot.lockID))
				     toret.add(lot.lockID);
			 }
		 }
		return toret;
	}

}
