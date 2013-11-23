package hk.ust.lpxz.LockSynthesis.transformer;

import hk.ust.lpxz.fixing.SootAgent4Fixing;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.jimple.internal.RValueBox;
import soot.jimple.toolkits.visitor.RecursiveVisitor;
import soot.jimple.toolkits.visitor.Visitor;
import soot.jimple.toolkits.visitor.VisitorForActiveTesting;
import soot.jimple.toolkits.visitor.VisitorForPrinting;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;

public class ChocalateInjectCrawler {
	protected static final String injectedMethodName = "countEdges";
	public static RecursiveVisitor rv = null;
	 public static Visitor solidVisitor = null; 
	 
	public static String[] classMethodPair = new String[2];

	public static String syncKeyWord = "entermonitor";
	public static BufferedWriter bWriter = null;

	public static void main(String[] args) throws IOException { // wjtp.tn
		// @link LockProducer

		String bootclasspath = System.getProperty("sun.boot.class.path");
		String argsOfEasyLib = "-f J -cp "
				+ bootclasspath
				+ ":/home/lpxz/eclipse/workspace/Playground/bin -process-dir /home/lpxz/eclipse/workspace/Playground/bin"; // java.lang.Math

		String argsOfJavaLib = "-f J -cp "
				+ bootclasspath
				+ ":/home/lpxz/java_standard/jre/lib/rt.jar -process-dir /home/lpxz/work/soot/subjects/rt"; // java.lang.Math
		String argsOfGoogleLib = "-f J -cp "
				+ bootclasspath
				+ ":/home/lpxz/work/soot/subjects/google/google-collect-1.0/bin:/home/lpxz/work/soot/subjects/google/google-collect-1.0/bin/com/google/jsr-305-read-only/ri/build/classes:/home/lpxz/work/soot/subjects/google/google-collect-1.0/bin/com/google/jsr-305-read-only/ri/build/jsr305.jar:/home/lpxz/work/soot/subjects/google/google-collect-1.0/google-collect-1.0.jar"
				+ " -process-dir /home/lpxz/work/soot/subjects/google/google-collect-1.0/bin/separateDir";// com.google.common.collect.TreeMultiset";// -process-dir /home/lpxz/work/soot/subjects/google/google-collect-1.0/bin";
			//		String className = "soot.jimple.toolkits.thread.synchronizationLP.Jimples.HelloWorld";																								// //java.lang.Math
		String argsOfToy2 = "-f J -pp -cp /home/lpxz/eclipse/workspace/Playground/bin:/home/lpxz/eclipse/workspace/soot24/bin Toy2";// soot.jimple.toolkits.thread.synchronizationLP.Jimples.HelloWorld"; // java.lang.Math
		String argsOfToyW = "-f c -pp -cp .:/home/lpxz/eclipse/workspace/Dcon/bin hk.ust.lpxz.transformer.Test"; // java.lang.Math
		String argsOfJimpleHelloWorld = "-f J -pp -cp /home/lpxz/eclipse/workspace/Playground/bin:/home/lpxz/eclipse/workspace/soot24/bin soot.jimple.toolkits.thread.synchronizationLP.Jimples.HelloWorld"; // java.lang.Math
		String argsOfmtrt = "-f J -pp -cp /home/lpxz/eclipse/workspace/mtrt/bin -main-class spec.benchmarks._227_mtrt.Main spec.benchmarks._227_mtrt.Main"; // java.lang.Math
		//String argsOfToyW = "-f J -pp -cp /home/lpxz/eclipse/workspace/Playground/bin -d /home/lpxz/eclipse/workspace/Playground/bin Toy$InnerThread"; // java.lang.Math
		
		// /home/lpxz/javapool/jdk1.3.1_20/jre/lib/rt.jar

		String interString = argsOfToyW;
		String[] finalArgs = interString.split(" ");
		classMethodPair[0]= "hk.ust.lpxz.transformer.Test";//"aTest.Teacher";
		classMethodPair[1]= "main"; //"callee";
		
		soot.Main.v().processCmdLine(finalArgs);
//		Setup.setupPatchOptions();

		Visitor.setObserverClass("hk.ust.lpxz.transformer.Chocalate");
		Scene.v().loadClassAndSupport(Visitor.observerClass);//insert something here
		
		Scene.v().loadNecessaryClasses();



		
		 
		Pack jtp = PackManager.v().getPack("jtp");
		addVisitorPackToJtp(jtp);
		// Pack wjtp = PackManager.v().getPack("wjtp");
		// addVisitorToWjtp(wjtp);

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
					BriefUnitGraph uGraph = new BriefUnitGraph(b);
					
					System.out.println(b.toString());
					//===============================================================
					ExceptionalUnitGraph eug = new ExceptionalUnitGraph(b); // feasibility checking
					if(eug.explicitEscapeExceptionTails().size()>0 || eug.normalTails().size()==0)
					{
						System.err.println("not handleable, ignore the method");
						return;
					}												
					//===============================================================
					// degeneration checking.
                     // 1 the edge shares source and target with others.
					//  if(source.branch() && !((source instanceof JIfStmt)|| (source instanceof JGotoStmt))) // or you check the degree of source/target.
					 // 2 the edge has one point in the exceptional block.
					// eug.mightThrowToIntraproceduralCatcher(source||target);
					//===============================================================
					List<Unit> heads = uGraph.getHeads();// transform edges
					List<Value> argList = new ArrayList<Value>();
					for(Unit head: heads)
					{
						if(SootAgent4Fixing.isExceptionalHead(head)) continue;  
						argList.clear();
					    boolean succeed = instrumentCFGEdges(uGraph, units, head, true,"hk.ust.lpxz.transformer.Chocalate", "countEdges", argList);
					    
					}
					//===============================================================
					// transform heads, i.e., firstNonIdentityStatements
					// NOTE: do not move it before the edge instrumentation, it will change the unitgraph so that head.successor()!=original_successor.
					Stmt firstNonIdentity =b.getFirstNonIdentityStmt();
					List<Value> headArgList = new ArrayList<Value>();
					insertStaticInvoke(units, firstNonIdentity, true,"hk.ust.lpxz.transformer.Chocalate", "beginCountingEdges", headArgList);					
					//===============================================================
					List<Value> tailArgList = new ArrayList<Value>();
					for(Unit normalTail : uGraph.getTails())// transform tails
					{
						tailArgList.clear();
						insertStaticInvoke(units, normalTail, true,"hk.ust.lpxz.transformer.Chocalate", "reportEdges", tailArgList);
					}				
					//===============================================================
					System.out.println(b.toString());
					b.validateLP();
				}
				
			}

			public  Stack systemStack = new Stack();
		    public  Set visited = new HashSet();   
		    //customize the "insertStaticInvoke" method in this class for your own transformation.
		    //set the name of observer class/method and load them during class loading of soot.
		    // insert before or not.
		    private boolean instrumentCFGEdges(BriefUnitGraph ug, PatchingChain<Unit> units,Unit unit, boolean before, String classname, String methodName, List<Value> argList) {				
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
					    		
					    	if(!visited.contains(child))
					    	{
					    	    visited.add(child);
					    		systemStack.push(child);				    		
					    	}
					    }					    
					}	
					return true;
			}
            public List<Value> privateArgList = new ArrayList<Value>();
			private boolean  instrumentCFGEdge(PatchingChain<Unit> units, Unit stmt, Unit successor, boolean before, String classname, String methodName, List<Value> argList) {
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

			private void insertStaticInvoke(PatchingChain<Unit> units, Unit stmt, boolean before, 
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
			
			
		}));

	}



	private static SootClass loadClass(String name, boolean main) {
		SootClass c = Scene.v().loadClassAndSupport(name);
		c.setApplicationClass();
		if (main)
			Scene.v().setMainClass(c);
		return c;
	}

	public void testGetShimpleBody() {
		// fail("Not yet implemented"); // TODO
	}

}
