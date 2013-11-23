
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
					event =12;
					
					event =12;
					
					
					
					last =13;
				}
				
			}
		};
		
		Thread t2 = new Thread(){
			public void run()
			{
				synchronized (realLockObject) {
					
					event =22;
					
					last =21;
					
					last =23;
					
			 	
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


		
		
		event =35;
		
	
	}

	protected static void setValue() {

		
		event =32;
		
	}


}
