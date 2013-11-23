package hk.ust.lpxz.LockSynthesis.transformer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;




public class lockMeta {	
	static boolean debug = false;
	static boolean ctxtsensitive = false;
	static ReentrantLock[] locks = new ReentrantLock[80];// statically allocated, enough?
	static // initialization
	{
		
		for(int i=0;i<locks.length;i++)
		{

			ReentrantLock k  = new ReentrantLock();;
			locks[i] = k;
			
//			if(i <=28)
//			{
//				ReentrantLock k  = new ReentrantLock();;
//				locks[i] = k;
//			}
//			else
//			{
//				locks[i] = locks[i%28];
//			}
			
		}
	}

	// this lock and the next unlock is like a pipl, once you enter it, you have to exit it and exit once!
	 public static void locking(List ctxts, int bugID )// yes, the ctxts is instrumented as a static field, do not touch it, it will affect other ones. read-only
	 {
		//System.out.println(bugID);
		 // later is in the later part: ctxts
		System.out.println(Thread.currentThread().getName() + " tolock:" + bugID);
		StackTraceElement[] elems =Thread.currentThread().getStackTrace();
		System.out.println(elems[2]);
		 if(debug)
		 {
			 if(ctxtsensitive)
			 {
				 reportCtxt(ctxts);
			 }
			
			 System.out.println(Thread.currentThread().getName() + " locking lockID:" + bugID);
			// return;
		 }
		 if(ctxtsensitive)
		 {
			 List tmpList = new  ArrayList();
				
			 tmpList.addAll(ctxts);
			 StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			 // the later is in the beginning
			 int last = 0;
			 while(tmpList !=null && tmpList.size()!=0)// once the elements are not fully matched
			 {
				 String pop = (String)tmpList.remove(tmpList.size()-1);
				 int index = searchFromLast(stackTrace,last, pop);
				 if(index == -1)
				 {
					 return;// not matching
				 }
				 else {
					 last = index +1;
				}
				 			 
			 }
		 }
	     
		 // match
		// System.out.println("locking:" + bugID);
		ReentrantLock lock = locks[bugID];
		lock.lock(); 	
		System.out.println(Thread.currentThread().getName() + " locked:" + bugID);
		System.out.println(elems[2]);
	 }
	 


	public static void unlocking(List ctxts, int bugID )
	 {

		
		 if(debug)
		 {
			 if(ctxtsensitive){
				 reportCtxt(ctxts); 
			 }
			 
			 System.out.println(Thread.currentThread().getName() + " unlocking lockID:" + bugID);
			// return;
		 }
		 if(ctxtsensitive)
		 {
			 List tmpList = new  ArrayList();
				
			 tmpList.addAll(ctxts);
			 // later is in the later part: ctxts
			 StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			 // the later is in the beginning
			 int last = 0;
			 while(tmpList !=null && tmpList.size()!=0)
			 {
				 String pop = (String)tmpList.remove(tmpList.size()-1);
				 int index = searchFromLast(stackTrace,last, pop);
				 if(index == -1)
				 {
					 return;// not matching
				 }
				 else {
					 last = index +1;
				}
							 
			 } 
		 }
	     
		// System.out.println("unlocking:" + bugID);
		 // match
		 System.out.println(Thread.currentThread().getName() + " unlocked:" + bugID);
			StackTraceElement[] elems =Thread.currentThread().getStackTrace();
			System.out.println(elems[2]);
		ReentrantLock lock = locks[bugID];
		lock.unlock(); 
		
	 }
	 
	 private static int searchFromLast(StackTraceElement[] stackTrace, int last,
				String pop) {
			if(last >= stackTrace.length)
				return -1;
			for(int i = last ; i< stackTrace.length; i++)
			{
				StackTraceElement ste = stackTrace[i];
				String methodStr  = ste.getClassName() + "."+ ste.getMethodName();
				if(methodStr.equals(pop))// yes, first match, consider the method1()->method1(true) sequence, it is still correct, we do not have argument info here!
				{
					return i;
				}
			}
			 // find no match
			return -1;
		}
	 
	 private static void reportCtxt(List ctxts)
	 {
		 System.out.println();
		 System.out.println("report ctxts:");
		 for(int i=0; i< ctxts.size(); i++)
		 {
			 System.out.println(ctxts.get(i));
		 }
	 }
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
