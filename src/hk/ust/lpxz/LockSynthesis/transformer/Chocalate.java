package hk.ust.lpxz.LockSynthesis.transformer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Chocalate {
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
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
