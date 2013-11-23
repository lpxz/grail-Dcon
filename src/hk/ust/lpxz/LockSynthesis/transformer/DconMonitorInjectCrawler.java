package hk.ust.lpxz.LockSynthesis.transformer;

import hk.ust.lpxz.fixing.SootAgent4Fixing;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;



import Drivers.Setup;
import soot.Body;
import soot.BodyTransformer;
import soot.EntryPoints;
import soot.EscapeAnalysis;
import soot.G;
import soot.Local;
import soot.Pack;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Transform;
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.jimple.internal.RValueBox;
import soot.jimple.toolkits.thread.ThreadLocalObjectsAnalysis;
import soot.jimple.toolkits.visitor.RecursiveVisitor;
import soot.jimple.toolkits.visitor.Visitor;
import soot.jimple.toolkits.visitor.VisitorForActiveTesting;
import soot.jimple.toolkits.visitor.VisitorForPrinting;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.TrapUnitGraph;
import soot.util.Chain;

public class DconMonitorInjectCrawler {
	protected static final String injectedMethodName = "countEdges";
	public static RecursiveVisitor rv = null;
	 public static Visitor solidVisitor = null; 
	 
	public static String[] classMethodPair = new String[2];

	public static String syncKeyWord = "entermonitor";
	public static BufferedWriter bWriter = null;

	public static void main(String[] args) throws IOException { // wjtp.tn
		// @link LockProducer

		String bootclasspath = System.getProperty("sun.boot.class.path");
    	String argsOfToyW = "-f c -pp -cp .:/home/lpxz/eclipse/workspace/Dcon/bin hk.ust.lpxz.LockSynthesis.transformer.Test"; // java.lang.Math
	
		String interString = argsOfToyW;
		String[] finalArgs = interString.split(" ");
		classMethodPair[0]= "hk.ust.lpxz.LockSynthesis.transformer.Test";//"aTest.Teacher";
		classMethodPair[1]= "main"; //"callee";
		
		soot.Main.v().processCmdLine(finalArgs);
//		Setup.setupPatchOptions();

		Visitor.setObserverClass("hk.ust.lpxz.LockSynthesis.transformer.DconMonitor");
		Scene.v().loadClassAndSupport(Visitor.observerClass);//insert something here
		
		Scene.v().loadNecessaryClasses();
		 
		Pack jtp = PackManager.v().getPack("jtp");
		addVisitorPackToJtp(jtp);


		PackManager.v().runPacks();// 1
		PackManager.v().writeOutput();
		G.reset();
	}



	private static void addVisitorPackToJtp(Pack jtp) {

		jtp.add(new Transform("jtp.visitor", new BodyTransformer() {
			// insert chocalate before the returning statements

			@Override
			protected void internalTransform(Body b, String phaseName,
					Map options) {
				SootMethod sm =b.getMethod();
				SootClass sc = sm.getDeclaringClass();
				if(sc.getName().equals(classMethodPair[0]) && sm.getName().equals(classMethodPair[1]))
				{
					PatchingChain<Unit> units = b.getUnits();					
					
					
					System.out.println(b.toString());
					//===============================================================
					// feasibility checking, eug is too conservative, 					
					ExceptionalUnitGraph eug = new ExceptionalUnitGraph(b); // feasibility checking
					if(b.getMethod().getExceptions().size()!=0)	return; // 1 implicit escaping via "throws", runtime can handle, we cannot. quit!
					// 2 we also assume that programmers are clear about the implicit exceptions such as "a/0" and catch them properly.
					// otherwise, such exceptions simply make the software crash with or without locks added.
					// 3 in the following, we consider only explicit exceptions such as try-catch and throw new Exception().
					
					if(eug.explicitEscapeExceptionTails().size()==0 && eug.normalTails().size()==0) return; 
					// the method contains an infinite loop, if fine-grain fails, coarse-grain may malfunction, ignore it.
								
					//===============================================================
					// degeneration checking.
                     // 1 the edge shares source and target with others.
					//  if(source.branch() && !((source instanceof JIfStmt)|| (source instanceof JGotoStmt))) // or you check the degree of source/target.
					 // 2 the edge has one point in the exceptional block.
					// eug.mightThrowToIntraproceduralCatcher(source||target);
					
					determineTokensToAcquire4Locks(b);
					
					//===============================================================					
					BriefUnitGraph uGraph = new BriefUnitGraph(b);
					List<Unit> heads = uGraph.getHeads();// transform edges
					List<Value> argList = new ArrayList<Value>();
					for(Unit head: heads)
					{
						if(SootAgent4Fixing.isExceptionalHead(head)) continue;  
						argList.clear();
					    boolean succeed = instrumentCFGEdges(uGraph, units, head, true,"hk.ust.lpxz.LockSynthesis.transformer.DconMonitor", "countEdges", argList);
					    
					}
					//===============================================================
					// transform heads, i.e., firstNonIdentityStatements
					// NOTE: do not move it before the edge instrumentation, it will change the unitgraph so that head.successor()!=original_successor.
					Stmt firstNonIdentity =b.getFirstNonIdentityStmt();
					List<Value> headArgList = new ArrayList<Value>();
					insertStaticInvoke(units, firstNonIdentity, true,"hk.ust.lpxz.LockSynthesis.transformer.DconMonitor", "beginCountingEdges", headArgList);					
					//===============================================================
					List<Value> tailArgList = new ArrayList<Value>();
					for(Unit normalTail : eug.normalTails())// transform tails
					{
						tailArgList.clear();
						insertStaticInvoke(units, normalTail, true,"hk.ust.lpxz.LockSynthesis.transformer.DconMonitor", "reportEdges", tailArgList);
					}			
					for(Unit explicitEscapeExceptionTail : eug.explicitEscapeExceptionTails())// transform tails
					{
						tailArgList.clear();
						insertStaticInvoke(units, explicitEscapeExceptionTail, true,"hk.ust.lpxz.LockSynthesis.transformer.DconMonitor", "reportEdges", tailArgList);
					}	
					
					//===============================================================
					System.out.println(b.toString());
					b.validateLP();
				}
				
			}

			
			
			
		}));

	}


	private static void determineTokensToAcquire4Locks(
			Body bb) {
		HashMap<Integer, Integer> recentLocks2Tokens = new HashMap<Integer, Integer>();
		BriefUnitGraph uGraph = new BriefUnitGraph(bb);
		List<Unit> heads = uGraph.getHeads();// transform edges
		List<Value> argList = new ArrayList<Value>();
		for(Unit head: heads)
		{
			if(SootAgent4Fixing.isExceptionalHead(head)) continue; 
			visitedNew.clear();
			 int maxAcquire =maxAcquire(uGraph, head, 0);	
			 System.out.println("look at this: " + maxAcquire); 
		}		
		
	}
	
	


    static Set<Unit> visitedNew = new HashSet<Unit>();
	private static int maxAcquire(
			BriefUnitGraph uGraph, Unit head, int acquired) {
		// if head acquire 3, 
		int max = acquired; 
		int newAcquired = update(head,acquired);
		if(newAcquired > max) max = newAcquired; 
		List<Unit> children =uGraph.getSuccsOf(head);
		for(Unit child : children)
		{
			if(!visitedNew.contains(child))
			{
				visitedNew.add(child);
				int maxAcquiredByChild = maxAcquire(uGraph,child, newAcquired);
				if(maxAcquiredByChild>max) max= maxAcquiredByChild;
			}
		}
		return max;
		
	}





	private static int update(Unit head, int accumulated) {
		if(head instanceof InvokeStmt )
		{
			InvokeExpr ie = ((Stmt)head).getInvokeExpr();
			if(head.toString().contains("acquire"))
			{
			   	IntConstant lockID = (IntConstant)ie.getArg(1);
			   	IntConstant tokens = (IntConstant)ie.getArg(0);
			   	return (accumulated + tokens.value);
			   	
			}else if(head.toString().contains("release")){
				IntConstant lockID = (IntConstant)ie.getArg(1);
			   	IntConstant tokens = (IntConstant)ie.getArg(0);
			   	return (accumulated - tokens.value);
			}
		}
		return accumulated;
	}




	public static Stack systemStack = new Stack();
    public static Set visited = new HashSet();   
    //customize the "insertStaticInvoke" method in this class for your own transformation.
    //set the name of observer class/method and load them during class loading of soot.
    // insert before or not.
    public static boolean instrumentCFGEdges(BriefUnitGraph ug, PatchingChain<Unit> units,Unit unit, boolean before, String classname, String methodName, List<Value> argList) {				
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
					boolean ret = instrumentCFGEdge(units, (Stmt)pop , (Stmt)child, before, classname, methodName, argList);
			    	if(!ret)
			    	{
			    	 		System.err.println("Seems to not work, try the coarse-grain locking");
			    	        return false;
			    	}
			    	ExceptionalUnitGraph eug = new ExceptionalUnitGraph(ug.getBody()); // feasibility checking
			    	if(eug.inBusinessException((Stmt)pop) || eug.inBusinessException((Stmt)child))
			    	{
			    	//	System.out.println(ug.getBody());
			    		
			    	//	throw new RuntimeException(""+ pop + " "+   child);
			    	}
			    		
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
	private static boolean  instrumentCFGEdge(PatchingChain<Unit> units, Unit stmt, Unit successor, boolean before, String classname, String methodName, List<Value> argList) {
		// singlularity checking
		if(!stmt.branches())
		{
             if(units.getSuccOf(stmt)==successor)
             {
            	 privateArgList.clear();// do not add to the argList directly as it is shared by "edges".
            	 privateArgList.addAll(argList);
            	 privateArgList.add(StringConstant.v(stmt.toString()));
            	 privateArgList.add(StringConstant.v(successor.toString()));
            	 insertStaticInvoke(units, successor, before, classname, methodName, privateArgList);
             }else {
				 throw new RuntimeException("non-branch?");
			}
		}
		else {
			 if(stmt instanceof JIfStmt)
			   {
				   JIfStmt aif = (JIfStmt)stmt;
				   if(aif.getTarget()==successor)
				   {
					   // branch = false
					     privateArgList.clear();// do not add to the argList directly as it is shared by "edges".
                    	 privateArgList.addAll(argList);
                    	 privateArgList.add(StringConstant.v(stmt.toString()));
                    	 privateArgList.add(StringConstant.v(successor.toString()));
                    	 insertStaticInvoke(units, successor, before, classname, methodName, privateArgList);
				   }else {
					   // branch = true;
					     privateArgList.clear();// do not add to the argList directly as it is shared by "edges".
                    	 privateArgList.addAll(argList);
                    	 privateArgList.add(StringConstant.v(stmt.toString()));
                    	 privateArgList.add(StringConstant.v(successor.toString()));
                    	 insertStaticInvoke(units, successor, before, classname, methodName, privateArgList);
						  
				   }				   
			   }
			  else if(stmt instanceof JGotoStmt)
			   {
				   JGotoStmt agoto = (JGotoStmt)stmt;
				   if(agoto.getTarget()==successor)
				   {
					   // yes, insert before goto without redirection.
					   privateArgList.clear();// do not add to the argList directly as it is shared by "edges".
					   privateArgList.addAll(argList);
					   privateArgList.add(StringConstant.v(stmt.toString()));
					   privateArgList.add(StringConstant.v(successor.toString()));
					   insertStaticInvoke(units, agoto, before, classname, methodName, privateArgList); 
						 
					   // goto
				   }else {
				       throw new RuntimeException(" is it a real CFG edge? ");
				   }
			   }else {
				   // singlularity checking fails.
			        return false;
			   }
     	}
		
		return true;
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

}
