package hk.ust.lpxz.LockSynthesis.transformer;

import hk.ust.lpxz.IO.Reader;
import hk.ust.lpxz.fixing.DconPropertyManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class DconMonitor {
    // for node instrumentation, the defalut parameter list is empty.
	static Lock l = new ReentrantLock();
	public static void beginCountingEdges()
	{
		//System.out.println("locking... ");
		l.lock();
	}
	// for edge instrumentation, the last two parameters are reserved as the source/target of the edge.
	public static void countEdges(String stmt, String successor)
	{
		System.out.println("haha");
	}
	public static void reportEdges()
	{
		//System.out.println("unlocking..");
		l.unlock();
	}
	
	
	//==================real part:============================
	static HashMap<Integer, Semaphore> lockID2Sem = new HashMap<Integer, Semaphore>();
	public static void lockInitialize()//load from file
	{// must run from the Dcon home, as eclipse does. otherwise, the file cannot be found.
		//"/output/lockID2Tokens_" + projectname
		String lockfile =DconPropertyManager.file_lockID2Tokens;
		if(lockfile.endsWith("Fixed"))
		{
			lockfile=lockfile.substring(0, lockfile.length()-5);
		}
		


		HashMap<Integer, Integer> arg = (HashMap<Integer, Integer>)Reader.load(lockfile);
		Iterator<Integer> keyIt = arg.keySet().iterator();
		while (keyIt.hasNext()) {
			Integer integer = (Integer) keyIt.next();			
			 lockID2Sem.put(integer, new Semaphore(arg.get(integer)));
		}		
	}
	public static void acquire(int number, int lockID)
	{
		
	    try {
	    	System.out.println("lock:" + lockID + " acquire " + number);	
			lockID2Sem.get(lockID).acquire(number);
	    	
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}	
	}
	public static void release(int number , int lockID)
	{
		System.out.println("lock:" + lockID + " release " + number);
		lockID2Sem.get(lockID).release(number);
		
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
