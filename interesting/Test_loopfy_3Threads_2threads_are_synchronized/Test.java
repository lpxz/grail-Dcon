
public class Test {

	public static int counter =0;
	
	public static Object realLockObject = new Object();
	public static int xxx = 9;
	public static int yyy = 9;
	public static void main(String[] args) throws InterruptedException {
		Thread t1 = new Thread(){
			public void run()
			{
				synchronized (realLockObject) {
					counter=10;
					
					
					counter =11;
					
				}
				
			}
		};
		
		Thread t2 = new Thread(){
			public void run()
			{
				synchronized ("xxx") {
					
					counter=22;
					
					

				
				}
				
//				xxx=22;
			}
		};
		
		Thread t3 = new Thread(){
			public void run()
			{
				synchronized (realLockObject) {
					counter =33;
					
					counter =35;
				}
				
			}
		};
		
		t1.start();
		t2.start();
		t3.start();
		
		t1.join();
		t2.join();
		t3.join();
		
		
		
	}

}
