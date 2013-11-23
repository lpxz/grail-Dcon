
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
					
					
					if(xxx>5)
					{
						counter =11;
					}
					
				}
				
			}
		};
		
		Thread t2 = new Thread(){
			public void run()
			{
				synchronized (new Object()) {
					
					counter=22;
					
					if(yyy>5)
					{
						counter =21;
					}

				
				}
				
//				xxx=22;
			}
		};
		
		Thread t3 = new Thread(){
			public void run()
			{
				
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
