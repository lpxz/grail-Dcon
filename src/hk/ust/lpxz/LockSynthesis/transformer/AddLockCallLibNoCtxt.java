package hk.ust.lpxz.LockSynthesis.transformer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Unit;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.TrapUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

public class AddLockCallLibNoCtxt {


	public static  String observerClass = "AVfix.icse.fixing.lockwrapperStyle.lockMeta";


	static int serialNo =0;


	public static  void addLockCalls(SootMethod sm,  int bugID) {
		SootClass sc = sm.getDeclaringClass();
		if (sm.getName().contains("<clinit>"))			
		{
		return;
		}
		Body b = sm.retrieveActiveBody();
		
        UnitGraph eug = new ExceptionalUnitGraph(
				b);// yes, this graph is pretty great, no bugs for the syncFinder any more
        PatchingChain<Unit> units = b.getUnits();
         
     
		
		//NO IDEA WHY THIS
		//To enable insert tid
		if(sm.isStatic()&&sm.getParameterCount()==0)
		{					
			Stmt nop=Jimple.v().newNopStmt();				
			units.insertBefore(nop, units.getFirst());
		}
		
        Iterator stmtIt = units.snapshotIterator();    	       
        while (stmtIt.hasNext()) 
        {
            Stmt s = (Stmt) stmtIt.next();
            if(s instanceof ThrowStmt)
            {
            	visitStmtThrow(sm,units, (ThrowStmt)s,  bugID);
            }
            else if (s instanceof ReturnStmt)
            {
            	visitStmtReturn(sm,units, (ReturnStmt)s,  bugID);
            }else if (s instanceof ReturnVoidStmt)
            {
            	visitStmtReturnVoid(sm,units, (ReturnVoidStmt )s,  bugID );
            }             
        }  
        addCallMethodEntry(sm,units,  bugID);		    	
    	b.validate();
	}
	
	 public static void visitStmtThrow(SootMethod sm, Chain units, ThrowStmt throwStmt, int bugID) {
	    boolean dominated = dominatedByRet(sm, units, throwStmt);
	    	       if(dominated)
	    	       {
	    	    	   return;
	    	       }
	    	       addCallMethodExit(sm,units, throwStmt, bugID);
	    }

//	    public static boolean doubleExiting_throw_ret(SootMethod sm, Chain units,
//				ThrowStmt throwStmt) {
//
//	    	ExceptionalUnitGraph ug = new ExceptionalUnitGraph(sm.getActiveBody());
//	    	HashSet<Unit> visited  = new  HashSet<Unit>();
//	    	Stack<Unit> stack = new  Stack<Unit>();
//	    	stack.push(throwStmt);
//	    	visited.add(throwStmt);
//	    	while(stack.size()!=0)
//	    	{
//	    		Unit  top = stack.pop();
//	    		List<Unit> childrenChain =ug.getSuccsOf(top);
//	    		
//	    		for(Unit child : childrenChain)
//	    		{
//	    			if(!visited.contains(child))
//	    			{
//	    				visited.add(child);
//	    				stack.push(child); 
//	    				 if(child instanceof ReturnStmt || child instanceof ReturnVoidStmt)
//	    		    	 { 
//	    		    		 System.err.println(sm.getDeclaringClass().getName() + " " + sm.getName() + " this one contains double exiting...");
//	    		    		 
//	    		    		 return false;
//	    		    	 
//	    		    	 }
//	    			}
//	    			else {
//						// visited node...
//					}
//	    		}
//	    	}
//
//			return true;
//		}

	 
	 private static boolean dominatedByRet(SootMethod sm, Chain units,
				ThrowStmt throwStmt) {
	    	if(sm.getDeclaringClass().toString().contains("EDU.oswego.cs.dl.util.concurrent.PooledExecutor") &&
	    			sm.getName().contains("run"))
	    	{
	    		System.out.println();
	    	}
	    	// TrapUnitGraph suits you!
	    	TrapUnitGraph ug = new TrapUnitGraph(sm.getActiveBody());
	    //	 Utils.drawDirectedGraphNBody(ug, sm.getActiveBody(), "lpxz");
	    	HashSet<Unit> visited  = new  HashSet<Unit>();
	    	Stack<Unit> stack = new  Stack<Unit>();
	    	stack.push(throwStmt);
	    	visited.add(throwStmt);
	    	while(stack.size()!=0)
	    	{
	    		Unit  top = stack.pop();
	    		List<Unit> childrenChain =ug.getSuccsOf(top);
	    		
	    		for(Unit child : childrenChain)
	    		{
	    			if(!visited.contains(child))
	    			{
	    				visited.add(child);
	    				stack.push(child); 
	    				 if(child instanceof ReturnStmt || child instanceof ReturnVoidStmt
	    						 || child instanceof ThrowStmt)
	    		    	 { 
	    		    		 System.err.println(sm.getDeclaringClass().getName() + " " + sm.getName() + " this one is dominated by ret..");
	    		    		 
	    		    		 return true;
	    		    	 
	    		    	 }
	    			}
	    			else {
						// visited node...
					}
	    		}
	    	}

			return false;
		}
		public static void visitStmtReturnVoid(SootMethod sm, Chain units, ReturnVoidStmt returnVoidStmt,  int bugID) {
			addCallMethodExit(sm,units, returnVoidStmt,  bugID);		    	
	    }/*
	     * ReturnStmt ::= 'return' LocalOrConstant@ReturnContext
	     */

	    public static void visitStmtReturn(SootMethod sm, Chain units, ReturnStmt returnStmt,  int bugID) {
	        		addCallMethodExit(sm,units, returnStmt,  bugID);
	    }/*
	     * MonitorStmt ::=  EnterMonitorStmt | ExitMonitorStmt
	     */

	 
	public static  void addCallMethodEntry(SootMethod sm, Chain units,  int bugID) {			  	
    	String methodname = sm.getDeclaringClass().getName()+"."+sm.getName(); 
      
    	
    	Local ctxtListLocal = Jimple.v().newLocal("ctxtList"+bugID,
				RefType.v("java.util.List"));
		;

		Body body = sm.getActiveBody();
		body.getLocals().add(ctxtListLocal);
		Unit firstNon = sm.getActiveBody().getFirstNonIdentityStmt();
		// assign new object to lock obj
		Stmt newStmt = Jimple.v().newAssignStmt(ctxtListLocal,
				NullConstant.v());
		units.insertBefore(newStmt,firstNon );
		
		System.err.println("locking..." + bugID + "for this method:" + sm.getName());
		LinkedList args = new LinkedList();
        args.add(ctxtListLocal);
        args.add(IntConstant.v(bugID));
        
    	
        SootMethodRef mr_private = Scene.v().getMethod("<" + observerClass + ": void " + "locking" + "(java.util.List,int)>").makeRef();
         Unit s  = null; 
        units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr_private, args)),firstNon );
        
    }
    public static  void addCallMethodExit(SootMethod sm, Chain units, Stmt s,   int bugID) {		      	
    	String methodname = sm.getDeclaringClass().getName()+"."+sm.getName();    	

    	
    	Local ctxtListLocal = Jimple.v().newLocal("ctxtList"+bugID,
				RefType.v("java.util.List"));
		;

		System.err.println("unlocking..." + bugID + "for this method:" + sm.getName());
		Body body = sm.getActiveBody();
		body.getLocals().add(ctxtListLocal);
		
		// assign new object to lock obj
		Stmt newStmt = Jimple.v().newAssignStmt(ctxtListLocal,
				NullConstant.v());
		units.insertBefore(newStmt,s );
		
    	LinkedList args = new LinkedList();
        args.add(ctxtListLocal);
        args.add(IntConstant.v(bugID));
        
//        SootClass sc = Scene.v().getSootClass(observerClass);
//       System.out.println(sc.getMethodByName("unlocking").getSignature());
       
       SootMethodRef mr_private = Scene.v().getMethod("<" + observerClass  + ": void " + "unlocking" + "(java.util.List,int)>").makeRef();
       units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr_private, args)), s);
    
    }
 }
