package hk.ust.lpxz.petri.graph.criticialsection.callgraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.MonitorStmt;
import soot.jimple.Stmt;

public class SynchIdentifier {

	public static List locking = new ArrayList();
	public static List unlocking = new ArrayList();
	static 
	{
		locking.add("lock");
		unlocking.add("unlock");
	}
	public static boolean containSync(SootMethod sm)
	{
		if(sm.isSynchronized()) return true;
		if(!sm.hasActiveBody()) return false;
		Body bb = sm.getActiveBody();
		Iterator<Unit> it =bb.getUnits().iterator();
		while (it.hasNext()) {
			Unit unit = (Unit) it.next();
			if(unit instanceof MonitorStmt)
				return true;
		    Stmt caseted = (Stmt)unit;
		    if(caseted.containsInvokeExpr() )
		    {
		    	SootMethod castedM  =  caseted.getInvokeExpr().getMethod();
		    	if(castedM.getDeclaringClass().getName().contains("lockMeta"))
		    	{
		    		if(locking.contains(castedM.getName()) || unlocking.contains(castedM.getName().equals("unlock")))
		    		{
		    			return true;
		    		}
		    	}
		    }
			
		}
		return false;
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
