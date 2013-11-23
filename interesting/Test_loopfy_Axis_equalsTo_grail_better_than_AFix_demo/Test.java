// for jigsaw's first 80 bugs:
// Asn.toHead(): it does not sync on the parameter, so the code may be invoked on different this objects but access the same parameter object.
// NewRef.lock(): itself is well sync, but it forms the violation with Asn.toHead().
// Resource.markModified(): itself is well sync, and it is well sync with NewRef.lock(),  but it forms the violation with Asn.toHead().

// I simplify Asn.Tohead() as two statements, you can add more, which directly lead to more bugs.

// See the markingFile to see the good states and bad states.
public class Test {

	public static int counter =0;
	
	public static Object realLockObject = new Object();
	public static int xxx = 9;
	public static int yyy = 9;
	public static void main(String[] args) throws InterruptedException {
		Thread t1 = new Thread(){
			public void run()
			{
				synchronized (""+System.currentTimeMillis()) {
					counter=10;
					
					
					counter =11;
					
				}
				
			}
		};
		
		Thread t2 = new Thread(){
			public void run()
			{
				synchronized (realLockObject) {
					
					counter=22;
					
					
					counter =25;
				
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
