package hk.ust.lpxz.fixing;

import java.util.HashMap;
import java.util.Iterator;

public class Reporter {

	public static HashMap<String, Integer> property2data = new HashMap<String, Integer>();
	public static void report()
	{
		Iterator<String> iterator =property2data.keySet().iterator();
	    while (iterator.hasNext()) {
			String string = (String) iterator.next();
			System.out.println(string + " " + property2data.get(string));
			
		}
	}
	
	public static void store(String string, Integer intvalue)
	{
		property2data.put(string, intvalue);
	}
	
	public static StringBuffer sb = new StringBuffer();
	public static void reportDirectly(String string, Integer intvalue)
	{
		System.out.println(string + " " + intvalue + "\n");
		
	}
	
	public static void flush()
	{
		System.out.println(sb.toString());
		
	}
	public static void main(String[] args) {
		// LPXZ Auto-generated method stub

	}

}
