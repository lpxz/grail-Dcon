package hk.ust.lpxz.petri.graph.criticialsection.callgraph;

import hk.ust.lpxz.fixing.SootAgent4Fixing;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.graph.ICFGPetriBuilder;
import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.graph.PetriMethodManager;
import hk.ust.lpxz.petri.unit.PlaceMethodEntry;
import hk.ust.lpxz.petri.unit.Place;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import pldi.locking.CriticalSection;
import pldi.locking.SynchronizedRegionFinder;

import aTSE.CG.CGTraverse;

import edu.hkust.clap.organize.CSMethod;


import soot.G;
import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.spark.solver.SCCCollapser;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.ContextSensitiveCallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.visitor.jpaul.Graphs.SCComponent;
import soot.toolkits.graph.DirectedGraph;


import Drivers.Setup;

public class CallGraphTraversal {
// limitation:
	//1 if there is a jdk call or the uninteresting call, the subgraph is not visited further
	// 2 recursive is flattened.
	// optimization:
	// pruning if the children do not contain sync at all!

	public static boolean syncOpt = true;
	public static List emptyList = new ArrayList();
	public static Stack systemStack = new Stack();
	public static Stack runningStack = new Stack();
    public static Set visited = new HashSet();   
	
	public static Stack naivesystemStack = new Stack();
	public static Stack naiverunningStack = new Stack();
    public static Set naivevisited = new HashSet();   

    public static void main(String[] args) throws IOException { // wjtp.tn
		// @link LockProducer

		String bootclasspath = System.getProperty("sun.boot.class.path");
		String argsOfEasyLib = "-f J -cp "
				+ bootclasspath
				+ ":/home/lpxz/eclipse/workspace/Playground/bin -process-dir /home/lpxz/eclipse/workspace/Playground/bin"; // java.lang.Math

		// /home/lpxz/eclipse/workspace/Playground/src/HasnextTest.java
		String argsOfJavaLib = "-f J -cp "
				+ bootclasspath
				+ ":/home/lpxz/java_standard/jre/lib/rt.jar -process-dir /home/lpxz/work/soot/subjects/rt"; // java.lang.Math
		String argsOfGoogleLib = "-f J -cp "
				+ bootclasspath
				+ ":/home/lpxz/work/soot/subjects/google/google-collect-1.0/bin:/home/lpxz/work/soot/subjects/google/google-collect-1.0/bin/com/google/jsr-305-read-only/ri/build/classes:/home/lpxz/work/soot/subjects/google/google-collect-1.0/bin/com/google/jsr-305-read-only/ri/build/jsr305.jar:/home/lpxz/work/soot/subjects/google/google-collect-1.0/google-collect-1.0.jar"
				+ " -process-dir /home/lpxz/work/soot/subjects/google/google-collect-1.0/bin/separateDir";// com.google.common.collect.TreeMultiset";// -process-dir /home/lpxz/work/soot/subjects/google/google-collect-1.0/bin";
		// //java.lang.Math
		String argsOfToy = "-f J -pp -cp /home/lpxz/eclipse/workspace/Playground/bin:/home/lpxz/eclipse/workspace/soot24/bin Toy"; // java.lang.Math
		String argsOfToyW = "-f J -pp -cp /home/lpxz/eclipse/workspace/Playground/bin --app TestRegular.Freak"; // java.lang.Math
		String argsOfBayes = "-f J -pp -cp /home/lpxz/eclipse/workspace/simple/bin --app example.Example"; // java.lang.Math

		// /home/lpxz/javapool/jdk1.3.1_20/jre/lib/rt.jar

		String interString = argsOfBayes;
		String[] finalArgs = interString.split(" ");

		soot.Main.v().processCmdLine(finalArgs);
		Setup.setupPatchOptions();

		Scene.v().loadNecessaryClasses();
		// Setup.setPhaseOptionsForPaddleWork();
		Setup.setPhaseOptionsForSparkWork();

		Pack wjtp = PackManager.v().getPack("wjtp");

		wjtp.add(new Transform("wjtp.cganalyer", new SceneTransformer() {
			// Scene.v().getPointsToAnalysis();
			@Override
			protected void internalTransform(String phaseName, Map options) {
		
	               CallGraph cg =   Scene.v().getCallGraph();
	               SootMethod root = Scene.v().getMainMethod();
					//CGTraverse.cgvisit_root(cg, root );// please go inside and set the proper analysis
					PureStaticCSMethod rootM = new PureStaticCSMethod(new ArrayList(), root);
	              //. dfs_naive(cg, root);
					List sccList =sccList(Scene.v().getEntryPoints().iterator(), cg);
					// modify the cg a bit, for reducing the recusion
					dfs_cs(cg, rootM, sccList);
				//	SynchronizedRegionFinder
			}
		}));

		PackManager.v().runPacks();// 1
		PackManager.v().writeOutput();
		G.reset();
	}
    public static List sccList(Iterator<SootMethod> entryIt, CallGraph cg)
    {
    	List toret = new ArrayList();
    	SCC scc = new SCC( entryIt, cg);
		//System.out.println(scc.getSccList().size());;
		for(List sList : scc.getSccList())
		{
			//if(sList.size()<=1) continue;
			if(sList.size()==0) continue;
			if(sList.size() ==1)// ==1, is not simple actually
			{
			
				SootMethod sm =(SootMethod)sList.get(0);
			    if(getAllSucc(cg, sm).contains(sm))// recursion
			    {
			    	
			    }else {
					continue;
				}
				
			}
			toret.add(sList);
//			System.out.println("\nscc:");
//			for(Object o : sList)
//			{
//				System.out.println(o);
//			}
		}
		return toret;
    }
    private static Set dfs_naive_reachable(CallGraph csG, Object rootM) {
    	Set toretSet = new HashSet();
    	
        naivesystemStack.clear();
        naivevisited.clear();
        naivesystemStack.push(rootM);	
    	if(!naivevisited.contains(rootM))
    	{
    		naivevisited.add(rootM);
    	}
	
		while(!naivesystemStack.isEmpty())
		{
		    Object pop =naivesystemStack.pop();	
		   // System.out.println("pop:" + pop.toString());
		    
		    //==========maintain 
		    //  maintainRunningStack(runningStack, pop);
//		    if(!runningStack.contains(pop))
//		    {
//		    	System.out.println("enter:" + pop.toString());
//		    	runningStack.push(pop);// first pop, enter
//		    	systemStack.push(pop);	
//		    }else {
//		    	//  you have entered this running stack before, 
//		    	Object peekObject =runningStack.peek();
//		    	if(peekObject != pop)
//		    	{
//		    		throw new RuntimeException();
//		    	}
//		    	// ==
//				runningStack.pop();// secon pop, exit
//				System.out.println("exit:" + pop.toString());
//			   
//		     }
		    //==============================
		   
		    List children =getAllSucc(csG, pop);// csG.getAllSuccs(pop);// ug.getSuccsOf(pop);
		    for(int i = children.size()-1; i>=0; i--)
		    {
		    	Object child = children.get(i);	
		    		
		    	 
		    	if(!naivevisited.contains(child))
		    	{
		    		naivevisited.add(child);
		    	    naivesystemStack.push(child);				    		
		    	}
		    }
		    
		}
		toretSet.addAll(naivevisited);
		return toretSet;
	}
    
  
	public static List getAllSucc(CallGraph csG, Object pop) {
		List ret = new ArrayList();
	
		Iterator<Edge> edgeIT= csG.edgesOutOf((SootMethod)pop);
		while (edgeIT.hasNext()) {
			Edge edge = (Edge) edgeIT.next();

			SootMethod target = (SootMethod)edge.getTgt();
			if(SootAgent4Fixing.shouldInstruThis_ClassFilter(target.getDeclaringClass().getName()))
			{
			//	if(target.getName().equals("<init>"))
				{
					if(!target.getName().equals("<clinit>"))
					{
					//	System.out.println(target);
						ret.add(target);
					}
				}
			}
			
		}
		return ret;
	}

	
	public static List getAllPrev(CallGraph csG, Object pop) {
		List ret = new ArrayList();
	
		Iterator<Edge> edgeIT= csG.edgesInto((SootMethod)pop);
		while (edgeIT.hasNext()) {
			Edge edge = (Edge) edgeIT.next();

			SootMethod src = (SootMethod)edge.getSrc();
			if(SootAgent4Fixing.shouldInstruThis_ClassFilter(src.getDeclaringClass().getName()))
			{
			//	if(target.getName().equals("<init>"))
				{
					if(!src.getName().equals("<clinit>"))
					{
						ret.add(src);
					}
				}
			}
			
		}
		return ret;
	}
	
	//==========
	// note that, the root method "pure" do not contain its corresponding invoke, so the ctxts is empty.
	// you can insert a placeHolder.invoke(main) or placeHolder.invoke(run), as in the dfs_cs_expand().
	// @alpha
	public static Set dfs_cs(CallGraph csG, PureStaticCSMethod pure, List sccList) {
    	Set toretSet = new HashSet();
    	runningStack.clear();
        systemStack.clear();
        visited.clear();
		systemStack.push(pure);	
    	if(!containsPure(visited,pure))//visited.contains(pure)
    	{
    	    visited.add(pure);
    	}
	
		while(!systemStack.isEmpty())
		{
			PureStaticCSMethod pop =(PureStaticCSMethod)systemStack.pop();	
		   // System.out.println("pop:" + pop.toString());
		    
		    //==========maintain 
			// running stack is used for maintaining the stack, system stack is for the dfs only
		    //  maintainRunningStack(runningStack, pop);
		    if(!containsPure(runningStack,pop)) //runningStack.contains(pop)
		    {
		    	//System.out.println("enter:" + pop.getSm().toString());
		    	runningStack.push(pop);// first pop, enter
		    	systemStack.push(pop);	
		    	
		    	List<PureStaticCSMethod> children =getPureAllSucc(csG, pop);// csG.getAllSuccs(pop);// ug.getSuccsOf(pop);
			    for(int i = children.size()-1; i>=0; i--)
			    {
			    	PureStaticCSMethod child = children.get(i);	
			    		
			    	///###########HERE, add the edge to the contextGraph, similar to before! special for scc methods
			    	if(!containsPure(visited,child))
			    	{			    		
			    		if(tryGetSCC(child, sccList)==null)
			    		{
			    			//System.out.println("adding edge:" + pop.getSm().getName() + " -> " + child.getSm().getName());
			    			 visited.add(child);
					    	 systemStack.push(child);
			    		}
			    		else {// no need to add to stack, otherwise, recursion
			    			// build artificial call relations for all transitive callees (FLATTEN) contained in the "child"
			    			Set<SootMethod> reachableMs= dfs_naive_reachable(csG, child.getSm());
			    			if(syncOpt)
			    			{
			    				 reachableMs=syncFilter(reachableMs);
			    			}			    		   
			    			for(SootMethod reachableM: reachableMs)
			    			{
			    				// they share the contexts as the child!
			    				//System.out.println("adding edge:" + pop.getSm().getName() + " -> " +reachableM.getName());
			    			}
			    		}
			    	   				    		
			    	}
			    }
		    }else {
		    	//  you have entered this running stack before, and the subgraph too!
		    	Object peekObject =runningStack.peek();
		    	if(peekObject != pop)
		    	{
		    		throw new RuntimeException();
		    	}
		    	// ==
				runningStack.pop();// secon pop, exit
			//	System.out.println("exit:" + pop.getSm().toString());
			   
		     }
		   
		    
		}
		toretSet.addAll(visited);
		return toretSet;
	}
	
	//TOFIX
	
	public static Set dfs_cs_expandUnderBound(CallGraph csG, PureStaticCSMethod pure, List sccList, Petri youngPetri,
			SootMethod boundM , Collection<Unit> bound) {
		//CriticalSection cs
		throw new RuntimeException("to fix");
		
//    	Set toretSet = new HashSet();
//    	runningStack.clear();
//        systemStack.clear();
//        visited.clear();
//		systemStack.push(pure);	
//    	if(!containsPure(visited,pure))//visited.contains(pure)
//    	{
//    	    visited.add(pure);
//    	}
//	
//		while(!systemStack.isEmpty())
//		{
//			PureStaticCSMethod pop =(PureStaticCSMethod)systemStack.pop();	
//		   // System.out.println("pop:" + pop.toString());
//		    
//		    //==========maintain 
//			// running stack is used for maintaining the stack, system stack is for the dfs only
//		    //  maintainRunningStack(runningStack, pop);
//		    if(!containsPure(runningStack,pop)) //runningStack.contains(pop)
//		    {
//		    	//System.out.println("enter:" + pop.getSm().toString());
//		    	runningStack.push(pop);// first pop, enter
//		    	systemStack.push(pop);	
//		    	
//		    	List<PureStaticCSMethod> children =getPureAllSuccUnderBound(csG, pop, boundM, bound);// csG.getAllSuccs(pop);// ug.getSuccsOf(pop);
//			    for(int i = children.size()-1; i>=0; i--)
//			    {
//			    	PureStaticCSMethod child = children.get(i);	
//			    		
//			    	///###########HERE, add the edge to the contextGraph, similar to before! special for scc methods
//			    	if(!containsPure(visited,child))
//			    	{			    		
//			    		if(tryGetSCC(child, sccList)==null)
//			    		{
//			    			//System.out.println("adding edge:" + pop.getSm().getName() + " -> " + child.getSm().getName());
//			    			 List<Stmt> ctxts = child.getCtxts();
//			    			 Stmt  iStmt =ctxts.get(ctxts.size()-1);
//			    		
//			    			 YoungPetriBuilder.growGraphWithOneStmt(youngPetri,  iStmt, pop,  child , ctxts);	// yes the callee method is set up
//			    			 visited.add(child);
//					    	 systemStack.push(child);
//			    		}
//			    		else {// no need to add to stack, otherwise, recursion
//			    			// build artificial call relations for all transitive callees (FLATTEN) contained in the "child"
//			    			Set<SootMethod> reachableMs= dfs_naive_reachable(csG, child.getSm());
//			    			if(syncOpt)
//			    			{
//			    				 reachableMs=syncFilter(reachableMs);
//			    			}			    		   
//			    			for(SootMethod reachableM: reachableMs)
//			    			{
//			    				//TOFIX, 
//			    				//according to richard, the application recursive calls are not too much, ignroe now
//			    				// they share the contexts as the child!
//			    				//System.out.println("adding edge:" + pop.getSm().getName() + " -> " +reachableM.getName());
//			    			}
//			    		}
//			    	   				    		
//			    	}
//			    }
//		    }else {
//		    	//  you have entered this running stack before, and the subgraph too!
//		    	Object peekObject =runningStack.peek();
//		    	if(peekObject != pop)
//		    	{
//		    		throw new RuntimeException();
//		    	}
//		    	// ==
//				runningStack.pop();// secon pop, exit
//			//	System.out.println("exit:" + pop.getSm().toString());
//			   
//		     }
//		   
//		    
//		}
//		toretSet.addAll(visited);
//		return toretSet;
	}
	static Set<SootMethod> tmp = new HashSet<SootMethod>();
	private static Set<SootMethod> syncFilter(Set<SootMethod> reachableMs) {
		tmp.clear();
		for(SootMethod sm : reachableMs)
		{
			if(!SynchIdentifier.containSync(sm))
			{
				tmp.add(sm);
			}
		}
		reachableMs.removeAll(tmp);
		
		return reachableMs;
	}
	private static List tryGetSCC(PureStaticCSMethod child, List sccList) {
		for(Object lObject: sccList)
		{
			List scc = (List)lObject;
		    if(scc.contains(child.getSm()))
		    {
		    	return scc;
		    }
		}
		
		return null;
	}

	private static List<PureStaticCSMethod> getPureAllSucc(CallGraph csG,
			PureStaticCSMethod pop) {
		List ret = new ArrayList();
		List curctxt = pop.getCtxts();
		Iterator<Edge> edgeIT= csG.edgesOutOf((SootMethod)pop.getSm());
		while (edgeIT.hasNext()) {
			Edge edge = (Edge) edgeIT.next();
            Stmt stmt =edge.srcStmt();
			SootMethod target = (SootMethod)edge.getTgt();
			if(SootAgent4Fixing.shouldInstruThis_ClassFilter(target.getDeclaringClass().getName()))
			{
			//	if(target.getName().equals("<init>"))
				{
					if(!target.getName().equals("<clinit>"))
					{
						if(syncOpt)
						{
							if(hasTSynchCallees(csG,target))
							{
								List list = new ArrayList<Stmt>();
								list.addAll(curctxt);
								list.add(stmt);
								PureStaticCSMethod target2 = new PureStaticCSMethod(list, target);
								
								ret.add(target2);
							}
							
						}
						else {
							List list = new ArrayList<Stmt>();
							list.addAll(curctxt);
							list.add(stmt);
							PureStaticCSMethod target2 = new PureStaticCSMethod(list, target);
							ret.add(target2);
						}
						
					}
				}
			}
			
		}
		return ret;
		
		
	}
	
	private static List<PureStaticCSMethod> getPureAllSuccUnderBound(CallGraph csG,
			PureStaticCSMethod pop, SootMethod boundM , Collection<Unit> bound) {
		List ret = new ArrayList();
		List curctxt = pop.getCtxts();
		Iterator<Edge> edgeIT= csG.edgesOutOf((SootMethod)pop.getSm());
		boolean takeEffect = false;
		if(pop.getSm().equals(boundM))
		{
			takeEffect =true;
		}
		while (edgeIT.hasNext()) {
			Edge edge = (Edge) edgeIT.next();
            Stmt stmt =edge.srcStmt();
            if(takeEffect)
            {
            	if(!bound.contains(stmt))
            	{
            		continue;// consider next one, it is not interesting at all.
            	}
            }
			SootMethod target = (SootMethod)edge.getTgt();
			if(SootAgent4Fixing.shouldInstruThis_ClassFilter(target.getDeclaringClass().getName()))
			{
			//	if(target.getName().equals("<init>"))
				{
					if(!target.getName().equals("<clinit>"))
					{
						if(syncOpt)
						{
							if(hasTSynchCallees(csG,target))
							{
								List list = new ArrayList<Stmt>();
								list.addAll(curctxt);
								list.add(stmt);
								PureStaticCSMethod target2 = new PureStaticCSMethod(list, target);
								
								ret.add(target2);
							}
							
						}
						else {
							List list = new ArrayList<Stmt>();
							list.addAll(curctxt);
							list.add(stmt);
							PureStaticCSMethod target2 = new PureStaticCSMethod(list, target);
							ret.add(target2);
						}
						
					}
				}
			}
			
		}
		return ret;
		
		
	}
	private static boolean hasTSynchCallees(CallGraph csG,SootMethod target) {
        Set transitivesSet= dfs_naive_reachable(csG, target);
        for(Object o : transitivesSet)
        {
        	SootMethod sm = (SootMethod)o;
        	if(SynchIdentifier.containSync(sm))
        		return true;
        }
        return false;
          
	}
	private static boolean containsPure(Collection visited, PureStaticCSMethod purearg) {
	    for(Object item : visited)
	    { 
	    	  PureStaticCSMethod pure = (PureStaticCSMethod) item;
	    	  // mathc or not:
	    	  if(pure.getSm()==purearg.getSm())
	    	  {
	    		  List purectxt = pure.getCtxts();
	    		  List pureargctxt = purearg.getCtxts();
	    		  if(purectxt.size() ==pureargctxt.size())
	    		  {
	    			  boolean match = true;
	    			  for(int i=0;i<=purectxt.size()-1; i++)
	    			  {
	    				  Object purectxtEle = purectxt.get(i);
	    				  Object purectxtargele = pureargctxt.get(i);
	    				  if(purectxtEle != purectxtargele)
	    				  {
	    					  match =false;
	    				  }
	    			  }
	    			  if(match)// great
	    			  {
	    				  return true ;
	    			  }
	    		  }
	    	  }
	    	  
	    	
	    }
		
		return false;
	}
}
