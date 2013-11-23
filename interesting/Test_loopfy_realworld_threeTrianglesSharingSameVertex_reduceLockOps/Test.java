
public class Test {

	
	public static Object realLockObject = new Object();

	public static int queue = 0; 
	public static int last = 0;
	public static int event = 0;
	
	public static void main(String[] args) throws InterruptedException {
		Thread t1 = new Thread(){
			public void run()
			{
				synchronized (""+System.currentTimeMillis()) {
					
					queue = 10;
					
					
					event =12;
					
					
					
					
					
					last =13;
				}
				
			}
		};
		
		Thread t2 = new Thread(){
			public void run()
			{
				synchronized (realLockObject) {
					
					setValue();
					
					postChange();
					
			 	
				}
				
//				xxx=22;
			}
		};
		
		Thread t3 = new Thread(){
			public void run()
			{
//				synchronized (new Integer(5)) {
//				
//					setValue();
//					
//					postChange();
//				}
//				
			}
		};
		
		t1.start();
		t2.start();
		t3.start();
		
		t1.join();
		t2.join();
		t3.join();
		
		
		
	}

	protected static void postChange() {

queue = 34;
		
		
		event =34;
		
		
		
		
		
		last =35;
	
	}

	protected static void setValue() {
		queue = 30;
		
		
		event =32;
		
		
		
		
		
		last =33;
		
	}


}
