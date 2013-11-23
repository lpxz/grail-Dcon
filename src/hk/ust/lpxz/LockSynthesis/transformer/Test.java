package hk.ust.lpxz.LockSynthesis.transformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args){
		int x=3;
		int y=0;
	
		acquire(5, 1);
		acquire(6, 1);
		
		
for(int i=0;i<=100;i++){
	
	//	synchronized ("ss") {
			if(x*x<=y)
			{
				
				
				release(3, 1);
				acquire(1, 1);
				
			}
			else {
				y++;
				acquire(1, 1);
			}
			
			release(1, 1);
}	//	}
		return;

	}
	public static void acquire(int arg1, int arg2)
	{
	   //	
	}
	
	public static void release(int arg1, int arg2)
	{
	   //	
	}

}
