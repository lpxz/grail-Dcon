
public class Test {

	public static int counter =0;
	
	public static int xxx = 9;
	public static int yyy = 9;
	public static void main(String[] args) throws InterruptedException {
		Thread t1 = new Thread(){
			public void run()
			{
				synchronized (this) {
					counter=10;
					
					xxx=10;
					
					counter=11;
				}
				
			}
		};
		
		Thread t2 = new Thread(){
			public void run()
			{
				synchronized (new Object()) {
					
					counter=22;
					
					xxx=222;
					counter=24;
					

				
				}
				
//				xxx=22;
			}
		};
		
		Thread t3 = new Thread(){
			public void run()
			{
				synchronized ("xxxx") {
					
					xxx=31;
					
					xxx=35;
					
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
