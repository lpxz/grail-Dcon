
public class Test {

	public static int counter =0;
	
	public static int xxx = 9;
	public static void main(String[] args) throws InterruptedException {
		Thread t1 = new Thread(){
			public void run()
			{
				synchronized (this) {
					counter=10;
					
					
					
					counter=11;
				}
				
			}
		};
		
		Thread t2 = new Thread(){
			public void run()
			{
				synchronized ("xx") {
					counter=22;
					
				
				}
				
//				xxx=22;
			}
		};
		
		Thread t3 = new Thread(){
			public void run()
			{
				synchronized ("yy") {
					counter=31;
					
					counter=32;
					
				
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
