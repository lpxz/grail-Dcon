package hk.ust.lpxz.LockSynthesis.transformer;

import hk.ust.lpxz.IO.Writer;
import hk.ust.lpxz.LockSynthesis.planner.LockInitializeTask;
import hk.ust.lpxz.LockSynthesis.planner.LockOperationTask;
import hk.ust.lpxz.LockSynthesis.planner.LockOperationTasks;
import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.fixing.SootAgent4Fixing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class DconMonitorInjecter {

	public  static void solveLockInitializeTask() { 
		// find main method, and insert after the first non-identity statement.
		SootClass sClass = Scene.v().getSootClass(DconPropertyManager.mainClass);
		SootMethod sMethod= sClass.getMethodByName("main");
		Body bb = sMethod.retrieveActiveBody();
		PatchingChain<Unit> units = bb.getUnits();		
		Stmt stmt = bb.getFirstNonIdentityStmt();
		Writer.save(LockInitializeTask.lockID2Tokens, DconPropertyManager.file_lockID2Tokens);// pass as argument using the file IO
		insertStaticInvoke(units, stmt, false, DconPropertyManager.observerClass,
				DconPropertyManager.injectedmethodname_lockinitialize, new ArrayList<Value>());
	//	System.out.println(bb.toString());
		
	}
	
	public  static void solveLockOperationTasks(){
		Set<SootMethod> involved = new HashSet<SootMethod>();
		for(LockOperationTask lot : LockOperationTasks.getTasks())
		{
          	involved.add(lot.method);
		}
		
		for(SootMethod each : involved)
		{
			Body bb =each.retrieveActiveBody();		
			//===============================================================
			// feasibility checking, eug is too conservative, 					
			ExceptionalUnitGraph eug = new ExceptionalUnitGraph(bb); // feasibility checking
			if(bb.getMethod().getExceptions().size()!=0)	continue; // 1 implicit escaping via "throws", runtime can handle, we cannot. quit!
			// 2 we also assume that programmers are clear about the implicit exceptions such as "a/0" and catch them properly.
			// otherwise, such exceptions simply make the software crash with or without locks added.
			// 3 in the following, we consider only explicit exceptions such as try-catch and throw new Exception().
			
			if(eug.explicitEscapeExceptionTails().size()==0 && eug.normalTails().size()==0) continue; 
			// the method contains an infinite loop, if fine-grain fails, coarse-grain may malfunction, ignore it.
			
			//==============instrument the edges (fine-grain or fine-scope)==========================
            // degeneration checking inside
			Body bakBody = null;
			SootMethod sm = bb.getMethod();
			try {
				bakBody= (Body)bb.clone();
				BriefUnitGraph uGraph = new BriefUnitGraph(bb);
				PatchingChain<Unit> units = bb.getUnits();	
				List<Unit> heads = uGraph.getHeads();// transform edges
				List<Value> argList = new ArrayList<Value>();
				for(Unit head: heads)
				{
					if(SootAgent4Fixing.isExceptionalHead(head)) continue;  
					argList.clear();						
				    instrumentCFGEdges(uGraph, units, head,   LockOperationTasks.getTasks());			    
				}
				//reorder the acq/rel operations.
				
				//reorder(bb);// avoid deadlocks, these operations should be atomic in theory.
				if(DconPropertyManager.showPatchedCode)
				   System.out.println(bb);
				
				continue; // successful in fine-scope locking, no need to apply the following coarse-grain locking any more.
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(" ====================try coarse-scope locking now================> ");
		        sm.setActiveBody(bakBody);
			}
				
			
			//==========degenerated case: (coarse-scope lock)======
			bb = sm.getActiveBody(); // reset it. may be changed.
			PatchingChain<Unit> units = bb.getUnits(); // new body. 	
			List<Integer> lockIDs = LockOperationTasks.getlockIDsAccessedByMethod(bb.getMethod());
			Collections.sort(lockIDs);
	 
			List<Value> headArgList = new ArrayList<Value>();
			List<Value> tailArgList = new ArrayList<Value>();
			for(Integer lockID: lockIDs)
			{
				int maxAcquire = maxAcquire(bb, lockID);	
				if(maxAcquire> LockInitializeTask.lockID2Tokens.get(lockID).intValue()) maxAcquire =1;// exceeds the capacity?
				// transform heads, i.e., firstNonIdentityStatements
				// NOTE: do not move it before the edge instrumentation, it will change the unitgraph so that head.successor()!=original_successor.
				Stmt firstNonIdentity =bb.getFirstNonIdentityStmt();
				headArgList.clear();
				headArgList.add(IntConstant.v(lockID));
				//headArgList.add(IntConstant.v(1));
				headArgList.add(IntConstant.v(maxAcquire));
				insertStaticInvoke(units, firstNonIdentity, false, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_acquire, headArgList);
				
				
				for(Unit normalTail : eug.normalTails())// transform tails
				{
					tailArgList.clear();
					tailArgList.add(IntConstant.v(lockID));
					//tailArgList.add(IntConstant.v(1));
					headArgList.add(IntConstant.v(maxAcquire));
					insertStaticInvoke(units, firstNonIdentity, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_release, tailArgList);
				}
				for(Unit explicitEscapeExceptionTail : eug.explicitEscapeExceptionTails())// transform tails
				{
					tailArgList.clear();
					tailArgList.add(IntConstant.v(lockID));
					//tailArgList.add(IntConstant.v(1));
					headArgList.add(IntConstant.v(maxAcquire));
					insertStaticInvoke(units, firstNonIdentity, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_release, tailArgList);
				}	
				
			}
				
			
		}
		
		// find methods, traverse to insert function calls to cover the operations.
	}
	
	private static int maxAcquire(Body bb, int lockID ) {		
		BriefUnitGraph uGraph = new BriefUnitGraph(bb);
		Unit head = bb.getFirstNonIdentityStmt(); 
		if(SootAgent4Fixing.isExceptionalHead(head)) throw new RuntimeException("check it"); 
		visitedNew.clear();
		int maxAcquire =maxAcquire(uGraph, lockID, head, 0);	
		return maxAcquire;	
		
	}
	
	
 
    //DFS is correct, no need for Data flow analysis. Key: loops wont increase the number after each iteration.
    static Set<Unit> visitedNew = new HashSet<Unit>();
	private static int maxAcquire(
			BriefUnitGraph uGraph, int lockID, Unit head, int acquired) {		
		int max = acquired; 
		int newAcquired = update(lockID, head,acquired);
		if(newAcquired > max) max = newAcquired; 
		List<Unit> children =uGraph.getSuccsOf(head);
		for(Unit child : children)
		{
			if(!visitedNew.contains(child))
			{
				visitedNew.add(child);
				int maxAcquiredByChild = maxAcquire(uGraph, lockID, child, newAcquired);
				if(maxAcquiredByChild>max) max= maxAcquiredByChild;
			}
		}
		return max;
		
	}





	private static int update(int lockID2, Unit head, int accumulated) {
		if(head instanceof InvokeStmt )
		{
			InvokeExpr ie = ((Stmt)head).getInvokeExpr();
			if(head.toString().contains(DconPropertyManager.acquireSignature))
			{
			   	IntConstant lockID = (IntConstant)ie.getArg(1);
			   	if(lockID.value==lockID2)// filter
			   	{
			   		IntConstant tokens = (IntConstant)ie.getArg(0);
				   	return (accumulated + tokens.value);
			   	}
			   	
			   	
			}else if(head.toString().contains(DconPropertyManager.releaseSignature)){
				IntConstant lockID = (IntConstant)ie.getArg(1);
				if(lockID.value==lockID2)
				{
					IntConstant tokens = (IntConstant)ie.getArg(0);
				   	return (accumulated - tokens.value);
				}
			   	
			}
		}
		return accumulated;
	}

	static List<Unit> snippetList_releases = new  ArrayList<Unit>();
	static List<Unit> snippetList_acquires = new  ArrayList<Unit>();


	static Set<Unit> processed = new HashSet<Unit>();
	private static void reorder(Body bb) {
		processed.clear();
		PatchingChain<Unit> units = bb.getUnits();	
		Iterator<Unit> it =units.snapshotIterator();
	     while (it.hasNext()) {
	    	
			Unit unit = (Unit) it.next();
			 if(processed.contains(unit)) continue; 
			//System.out.println(unit);	
			 //staticinvoke <hk.ust.lpxz.LockSynthesis.transformer.DconMonitor: void acquire(int,int)>(20, 0)
			 if(isDconMonitorAPI(unit))
			 {
				 snippetList_acquires.clear(); snippetList_releases.clear();// build the snippetList
				 Unit headSential = units.getPredOf(unit);
				 Unit current = unit;				 
				 addAsAcquireOrRelease(current, snippetList_acquires, snippetList_releases);
				 while(isDconMonitorAPI(units.getSuccOf(current)))
				 {
					 addAsAcquireOrRelease(units.getSuccOf(current), snippetList_acquires, snippetList_releases);
					 current = units.getSuccOf(current);
				 }
				 Unit tailSential = units.getSuccOf(current);// first non-API
                  // remove, register
				 for(Unit tmp: snippetList_acquires)
				 {
					 units.remove(tmp);
					 processed.add(tmp);
				 }
				 for(Unit tmp : snippetList_releases)
				 {
					 units.remove(tmp);
					 processed.add(tmp);
				 }				  
				  //add  release first, release 54321
 				 for(int i=0;i<snippetList_releases.size();i++)
 				 {
 					 units.insertAfter(snippetList_releases.get(i), headSential);
 					 processed.add(snippetList_releases.get(i));
 				 }
 				 // acquire 12345
 				 for(int i=0;i<snippetList_acquires.size(); i++)
 				 {
 					 units.insertBefore(snippetList_acquires.get(i), tailSential);
 					 processed.add(snippetList_acquires.get(i));
 				 }
			 }
			 System.out.println();
			
		}	
	}


	private static void addAsAcquireOrRelease(Unit current,
			List<Unit> snippetListAcquires, List<Unit> snippetListReleases) {
		if(current.toString().contains(DconPropertyManager.acquireSignature))
		{
			//add to acquires
			addInIncreasingOrder(current, snippetListAcquires);
		}
		else if (current.toString().contains(DconPropertyManager.releaseSignature)) {
			// add  to release
			addInIncreasingOrder(current, snippetListReleases);
		}
		else {
			throw new RuntimeException("what  DconMonitorLib method?");
		}
		
	}
    //LPXZ: observer class-specific feature is used.
	private static void addInIncreasingOrder(Unit toadd,
			List<Unit> list) {
		Stmt toAddStmt = (Stmt) toadd;
		IntConstant toaddLockId = (IntConstant)toAddStmt.getInvokeExpr().getArg(1);
		for(int i=0;i<list.size(); i++)
		{
			Stmt visited= (Stmt) list.get(i);
			IntConstant visitedLockId = (IntConstant)visited.getInvokeExpr().getArg(1);
			if(toaddLockId.value>visitedLockId.value) continue;
			else {
			   list.add(i, toadd);	
			   return;
			}
		}
		list.add(toadd);
		return;	
	}

	private static boolean isDconMonitorAPI(Unit unit) {
		return unit.toString().contains(DconPropertyManager.acquireSignature)|| unit.toString().contains(DconPropertyManager.releaseSignature);
	}

	public static Stack systemStack = new Stack();
    public static Set visited = new HashSet();   
    //customize the "insertStaticInvoke" method in this class for your own transformation.
    //set the name of observer class/method and load them during class loading of soot.
    // insert before or not.
    public static boolean instrumentCFGEdges(BriefUnitGraph ug, PatchingChain<Unit> units,Unit unit,  Set<LockOperationTask> lockopTasks) {				
	        systemStack.clear();
	        visited.clear();
			systemStack.push(unit);	
	    	if(!visited.contains(unit))
	    	{
	    	    visited.add(unit);
	    	}
		
			while(!systemStack.isEmpty())
			{
			    Object pop =systemStack.pop();			    
			    List children = ug.getSuccsOf((Unit)pop);
			    for(int i = children.size()-1; i>=0; i--)
			    {
			    	Object child = children.get(i);					    	
			       
			    	//instrument the edge
			    	//System.out.println(pop + "  == > " + child);
			    	Stmt stmt = (Stmt)pop;
			    	if(stmt.branches() && !(stmt instanceof JIfStmt) && !(stmt instanceof JGotoStmt))
			    	{
			    	 	throw new RuntimeException("Instrumentation of one edge fails, try the coarse-scope approach" + "outdegree(source)=" + ug.getSuccsOf(stmt).size() +
			    	 			" indegree(target)=" + ug.getPredsOf((Stmt)child).size());
			    	}
			    	ExceptionalUnitGraph eug = new ExceptionalUnitGraph(ug.getBody()); // feasibility checking
//			    	if(eug.inBusinessException(stmt) || eug.inBusinessException((Stmt)child))
//			    	{
//			    		throw new RuntimeException("the lock region is inside some business exceptional block, we cannot guarantee correct release in case of exceptions now");
//			    	} // too restrictive!
					instrumentCFGEdge(units, (Stmt)pop , (Stmt)child,   lockopTasks);
			    	
			    	
			    		
			    	if(!visited.contains(child))
			    	{
			    	    visited.add(child);
			    		systemStack.push(child);				    		
			    	}
			    }					    
			}	
			return true;
	}
    public static List<Value> privateArgList = new ArrayList<Value>();
	private static void  instrumentCFGEdge(PatchingChain<Unit> units, Unit stmt, Unit successor,  Set<LockOperationTask> lockOperationTasks) {
		// singlularity checking
		if(!stmt.branches())
		{
            // if(units.getSuccOf(stmt)==successor) // the successor may be newly injected monitor code, do not worry.
             {
            	 for(LockOperationTask lot: lockOperationTasks)
			     {
			    	 if(lot.stmt==stmt && lot.successor==successor)
			    	 {
			    		 int lockID = lot.lockID; 
			    		 if(lockID < 0) throw new RuntimeException("lock id <0 ?");
			    		 int tokensMoved= lot.tokensMoved;
			    		 privateArgList.clear();// do not add to the argList directly as it is shared by "edges".
	            		 privateArgList.add(IntConstant.v(Math.abs(tokensMoved)));
	            		 privateArgList.add(IntConstant.v(lockID));
			    		 if(tokensMoved >0)
			          		 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_acquire, privateArgList);
			    		 else if(tokensMoved <0)
			    			 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_release, privateArgList);
			    		 
			    			 // for testing "reorder" 
//			    		 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_acquire, IntConstant.v(Math.abs(tokensMoved)), IntConstant.v(2));
//			    		 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_release, IntConstant.v(Math.abs(tokensMoved)), IntConstant.v(3));
//			    		 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_acquire, IntConstant.v(Math.abs(tokensMoved)), IntConstant.v(4));
//			    		 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_acquire, IntConstant.v(Math.abs(tokensMoved)), IntConstant.v(6));
//			    		 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_release, IntConstant.v(Math.abs(tokensMoved)), IntConstant.v(5));
//			    		 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_acquire, IntConstant.v(Math.abs(tokensMoved)), IntConstant.v(8));
//			    		 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_acquire, IntConstant.v(Math.abs(tokensMoved)), IntConstant.v(10));
//			    		 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_acquire, IntConstant.v(Math.abs(tokensMoved)), IntConstant.v(12));
						    
			    	 }
			    	 
			     }          	 
             }
//             else {
//            	
//				 throw new RuntimeException("non-branch?"+units.getSuccOf(stmt) + " " + successor + "\n " + units);
//			}
		}
		else {
			 if(stmt instanceof JIfStmt)
			   {
				   JIfStmt aif = (JIfStmt)stmt;
				   if(aif.getTarget()==successor)
				   {
					   // branch = false
					    
					     for(LockOperationTask lot: lockOperationTasks)
					     {
					    	 if(lot.stmt==stmt && lot.successor==successor)
					    	 {
					    		 int lockID = lot.lockID; 
					    		 if(lockID < 0) throw new RuntimeException("lock id <0 ?");
					    		 int tokensMoved= lot.tokensMoved;
					    		 privateArgList.clear();// do not add to the argList directly as it is shared by "edges".
			            		 privateArgList.add(IntConstant.v(Math.abs(tokensMoved)));
			            		 privateArgList.add(IntConstant.v(lockID));
					    		 if(tokensMoved >0)
					          		 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_acquire, privateArgList);
					    		 else if(tokensMoved <0)
					    			 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_release, privateArgList);
      				    	 }
					     }
          
				   }else {
					   // branch = true;
					   for(LockOperationTask lot: lockOperationTasks)
					     {
					    	 if(lot.stmt==stmt && lot.successor==successor)
					    	 {
					    		 int lockID = lot.lockID; 
					    		 if(lockID < 0) throw new RuntimeException("lock id <0 ?");
					    		 int tokensMoved= lot.tokensMoved;
					    		 privateArgList.clear();// do not add to the argList directly as it is shared by "edges".
			            		 privateArgList.add(IntConstant.v(Math.abs(tokensMoved)));
			            		 privateArgList.add(IntConstant.v(lockID));
					    		 if(tokensMoved >0)
					          		 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_acquire, privateArgList);
					    		 else if(tokensMoved <0)
					    			 insertStaticInvoke(units, successor, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_release, privateArgList);
    				    	 }
					     }         
						  
				   }				   
			   }
			  else if(stmt instanceof JGotoStmt)
			   {
				   JGotoStmt agoto = (JGotoStmt)stmt;
				   //if(agoto.getTarget()==successor)
				   {
					   // yes, insert before goto without redirection.
					   for(LockOperationTask lot: lockOperationTasks)
					     {
					    	 if(lot.stmt==stmt && lot.successor==successor)
					    	 {
					    		 int lockID = lot.lockID; 
					    		 if(lockID < 0) throw new RuntimeException("lock id <0 ?");
					    		 int tokensMoved= lot.tokensMoved;
					    		 privateArgList.clear();// do not add to the argList directly as it is shared by "edges".
			            		 privateArgList.add(IntConstant.v(Math.abs(tokensMoved)));
			            		 privateArgList.add(IntConstant.v(lockID));
					    		 if(tokensMoved >0)
					          		 insertStaticInvoke(units, agoto, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_acquire, privateArgList);
					    		 else if(tokensMoved <0)
					    			 insertStaticInvoke(units, agoto, true, DconPropertyManager.observerClass, DconPropertyManager.injectedmethodname_release, privateArgList);
    				    	 }
					     }
					  		   
					   // goto
				   }
//				   else {// do not worry, it is a monitoring code.
//				       throw new RuntimeException(" is it a real CFG edge? ");
//				   }
			   }else {
				   // throw new RuntimeException("unreachable code");
			   }
     	}
		
		
	}

	public static void insertStaticInvoke(PatchingChain<Unit> units, Unit stmt, boolean before, 
			String observerClass, String injectedmethodname, List<Value>  arglist) {				
		SootClass injSC = Scene.v().loadClassAndSupport(observerClass);//insert something here
	    SootMethod injSM = injSC.getMethodByName(injectedmethodname);

	    InvokeExpr incExpr= Jimple.v().newStaticInvokeExpr(injSM.makeRef(), arglist);
	    Stmt incStmt = Jimple.v().newInvokeStmt(incExpr);
	    if(before) {
	    	 units.insertBefore(incStmt, stmt);// help me to redirect the labels.
	    }else {
	    	 units.insertAfter(incStmt, stmt);
		}			   
    }
	
	public static void insertStaticInvoke(PatchingChain<Unit> units, Unit stmt, boolean before, 
			String observerClass, String injectedmethodname, Value arg1, Value arg2) {				
		SootClass injSC = Scene.v().loadClassAndSupport(observerClass);//insert something here
	    SootMethod injSM = injSC.getMethodByName(injectedmethodname);

	    InvokeExpr incExpr= Jimple.v().newStaticInvokeExpr(injSM.makeRef(), arg1, arg2);
	    Stmt incStmt = Jimple.v().newInvokeStmt(incExpr);
	    if(before) {
	    	 units.insertBefore(incStmt, stmt);// help me to redirect the labels.
	    }else {
	    	 units.insertAfter(incStmt, stmt);
		}			   
    }
	
	public static void main(String[] args) {
		
	}

}
