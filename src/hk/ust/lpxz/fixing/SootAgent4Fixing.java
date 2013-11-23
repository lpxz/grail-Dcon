package hk.ust.lpxz.fixing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.EntryPoints;
import soot.G;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.IdentityStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JCaughtExceptionRef;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;
import soot.tagkit.Host;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ZonedBlockGraph;
import edu.hkust.clap.lpxz.context.ContextMethod;
import edu.hkust.clap.organize.CSMethod;
import edu.hkust.clap.organize.CSMethodPair;
//import AVfix.graph.ContextGraphMethod;
//import AVfix.icse.fixing.Domain;

public class SootAgent4Fixing {
private static final int err_threshold = 3;// lineNO may not be reliable.	
	
public static String[] unInstruClasses = {
    "org.apache.log4j.",
	"jrockit.",
	"java.",
	"javax.",
	"xjava.",
	"COM.",
	"com.",
	"cryptix.",
	"sun.",
	"sunw.",
	"junit.",
	"org.junit.",
	"org.xmlpull.",
	"edu.hkust.clap.",
	// the following are copied frpm the transforming options. (commandLine)
	"org.apache.commons.logging.",// annoying, ban it
	"org.apache.xalan.",
	"org.apache.xpath.",
	"org.springframework.",
	"org.jboss.",
	"jrockit.",
	"edu.",				
	"checkers.",
	"org.codehaus.spice.jndikit.",
	"EDU.oswego.cs.dl.util.concurrent.WaiterPreferenceSemaphore",
	"soot.",
	"aTSE.",
	"pldi.",
	"popl.", 
	"beaver.",
	"org.jgrapht",
	"ca.pfv.spmf.","japa.parser.", "polyglot.",
	"jasmin.", "Jama.", "jas.", "java_cup."
};
public static boolean shouldInstruThis_ClassFilter(String scname)
{
	for(int k=0;k<unInstruClasses.length;k++)
	{
		if(scname.startsWith(unInstruClasses[k]))
		{
			return false;
		}
	}
	
	return true;
}

public static boolean shouldInstrumentMethod(SootMethod sm )
{
    return shouldInstruThis_ClassFilter(sm.getDeclaringClass().getName());
	
}
	public static String getClassNameFromSootMSig(String msig)
	{
		//<org.exolab.jms.net.multiplexer.Multiplexer: int getNextChannelId()> 
		int lbrace =msig.indexOf('<');
		int maohao = msig.indexOf(':');
		return msig.substring(lbrace+1, maohao);		
	}

	

	
	

	public static int getLineNum(Host h) {
        if (h.hasTag("LineNumberTag")) {
            return ((LineNumberTag) h.getTag("LineNumberTag")).getLineNumber();
        }
        if (h.hasTag("SourceLineNumberTag")) {
            return ((SourceLineNumberTag) h.getTag("SourceLineNumberTag")).getLineNumber();
        }
        if (h.hasTag("SourceLnPosTag")) {
            return ((SourceLnPosTag) h.getTag("SourceLnPosTag")).startLn();
        }
        return -1;
    }
	
	// this function is very important, make it correct!
	public static Stmt search4CallSiteStmt(String secondClass,
			String secondMethod, String className, String mName,
			String filename, int eleLineNO, String eleLineNoInvoke) {
       // mName is not important, we care about the eleLineNoInvoke only.
		// mName is not necessarily the same as the callsite invocation.
		// YOU need to trust eleLineNoInvoke!absolutely correct.
		// It is taken from the jdk stacktraceelement.
		// the lineNO may not be quite accurate, for example, 137 maybe taken as 138.
		
		{ 		
			    SootClass secondclass = Scene.v().loadClassAndSupport(secondClass);
			    SootClass curclass = Scene.v().loadClassAndSupport(className);
			    secondclass.setApplicationClass();
			    curclass.setApplicationClass();
			    Scene.v().loadNecessaryClasses();
			    
			    
//			    Scene.v().loadNecessaryClasses();
			    List<SootMethod> sms = secondclass.getMethods();
			    List<SootMethod> hit = new ArrayList<SootMethod>();
			    for(SootMethod sm : sms)
			    {
			    	if(sm.getName().equals(secondMethod))
			    	{
			    		hit.add(sm);
			    	}
			    }
			    boolean theParent = false;
			    int recentMethodEntry = Integer.MAX_VALUE;
			    SootMethod recentMethod  = null;
			    for(SootMethod sm:hit)
			    {
			    	sm.retrieveActiveBody(); 		    	
			    	
			    	if(sm.hasActiveBody())
			    	{
			    		Body bb = sm.getActiveBody();
			    	    Stmt stmt  =(Stmt) bb.getUnits().getLast();
			    	    int lineNO = getLineNum(stmt);
			    	    if(eleLineNO <=lineNO)
			    	    {
			    	    	// lineNO is valid for candidates
			    	    	if(lineNO<=recentMethodEntry)// this one is more recent.
			    	    	{
			    	    		recentMethodEntry = lineNO;
			    	    		recentMethod = sm;
			    	    	}
			    	    }
			    	    
			    	}
			    }
			    if(recentMethod==null) 
			    {
			        	return null;
			    }
			    Body bb =recentMethod.getActiveBody();// recentMethod!=NULL

			    HashSet<Unit> possibleUnits  = new HashSet<Unit>();
	    		Iterator<Unit> it =bb.getUnits().iterator();
	    	    while (it.hasNext()) {
					Stmt unit = (Stmt) it.next();
					if(unit.containsInvokeExpr())
					{
						SootMethod invokeM = unit.getInvokeExpr().getMethod();
						String invokeMStr=invokeM.getName();// not necessarily the same, 
						// one is Impl.loopup, one is soot-based: interface.lookup!							

						if(eleLineNoInvoke.contains(invokeMStr))// just use contains for simplicity
						{
							if(getLineNum(unit)==eleLineNO)// exactly the same??
							{
								// hit 
								//G.reset();
								return unit;
							}
							else {
								possibleUnits.add(unit);
							}							
						}
						else if(eleLineNoInvoke.contains("<clinit>"))
						{
							// special case, there is no statement there containing the clinint,
							//just find the calling statements of the static methods of the class!,
							//if many, put them into the possibleUnits for further line-based pruning.
							int sep =eleLineNoInvoke.lastIndexOf('.');
							String elelineNoinvoke_class = eleLineNoInvoke.substring(0, sep);
							if(invokeM.getDeclaringClass().getName().equals(elelineNoinvoke_class))
							{
								// hit
								if(getLineNum(unit)==eleLineNO)// exactly the same??
								{
									// hit 
									//G.reset();
									return unit;
								}
								else {
									possibleUnits.add(unit);
								}	
							}
							
								
						}
						
						
						
					}
				}
	    	    if(possibleUnits.size() !=0)
	    	    {
	    	    	// still not return the signature yet, error_tolerance of the line NO!
	    	    	//G.reset();
	    	    	Unit best =null;
	    	    	int bestDis= Integer.MAX_VALUE;
	    	    	for(Unit possible:possibleUnits)
	    	    	{
	    	    		int itsLine = getLineNum(possible);
	    	    		if(Math.abs(itsLine-eleLineNO) < bestDis)
	    	    		{
	    	    			best = possible;
	    	    			bestDis= Math.abs(itsLine-eleLineNO);
	    	    		}	    	    		
	    	    	}
	    	    	if(bestDis <= err_threshold)
	    	    	{
	    	    		return (Stmt)best;
	    	    	}
	    	    	else {
						throw new RuntimeException("double checking " + bestDis);
					}
    	    	
	    	    }
			    

		}
		
		//G.reset();
		return null;
	}

	



	public static boolean couplingCheck(String secondClass, String filename) {
		
		if(filename.contains(".java"))
		{
			int index =filename.indexOf(".java");
			if(index !=-1)
			{
				String pureName =filename.substring(0, index);
				if(secondClass.contains(pureName))
				{
					return true; //pass
				}
			}
			
		}
		return false;
	}



	




	 
// shortcut, account for most cases, efficient, hot path
	public static Body search4MethodBodyInClass(String msig, String className) {
	    SootClass sc = Scene.v().loadClassAndSupport(className);
	    sc.setApplicationClass();
	    Scene.v().loadNecessaryClasses();
	    SootMethod sm =Scene.v().getMethod(msig);
	    if(!sm.hasActiveBody())
              sm.retrieveActiveBody();
	    Body bb  =sm.getActiveBody();
	//    G.v().reset();
	    return bb;	   
	}

	public static String getFullMethodName(StackTraceElement ste)
	{ 
		String classname = ste.getClassName();
		String methodname = ste.getMethodName();
		return classname + "." + methodname;
		
	}
	


	public static void main(String[] args) throws IOException {}


	private static HashSet<SootMethod> entrysOfRoot(String cgRootClass1) {
		HashSet<SootMethod> ret = new HashSet<SootMethod>();
		SootClass mainclass =Scene.v().loadClassAndSupport(cgRootClass1);	      
        SootClass cls = mainclass;//;Scene.v().getMainClass(); 
        if(cls.declaresMethodByName("main"))
        {
        	  ret.add( cls.getMethodByName("main"));     
        }
        else if(cls.declaresMethodByName("run")){
        	 ret.add( cls.getMethodByName("run"));     
		}	        
        else {
			// do nothing
        	throw new RuntimeException("you are the root class?");
		}
        for (SootMethod clinit : EntryPoints.v().clinitsOf(mainclass)) {
             ret.add(clinit);
        }
        ret.addAll( EntryPoints.v().implicit() );
        return ret;
	}

	public static void sootDestroyNecessary() {
		
		G.v().reset();
		return ;
		
	}

	public static String openjmsCP = "/home/lpxz/eclipse/workspace/soot24/lib/log4j.jar:/home/lpxz/java_standard/jre/lib/jsse.jar:/home/lpxz/eclipse/workspace/openjms/lib/jms-1.1.jar:" +
	"/home/lpxz/eclipse/workspace/openjms/lib/commons-codec-1.3.jar:" +
	"/home/lpxz/eclipse/workspace/openjms/lib/servlet.jar:" +
	"/home/lpxz/eclipse/workspace/openjms/lib/derby-10.1.1.0.jar:" +
	"/home/lpxz/eclipse/workspace/openjms/lib/castor-0.9.5.jar:" +
	"/home/lpxz/eclipse/workspace/openjms/lib/spice-jndikit-1.2.jar:" +
	"/home/lpxz/eclipse/workspace/openjms/lib/antlr-2.7.6.jar:" +
	"/home/lpxz/eclipse/workspace/openjms/lib/oro-2.0.8.jar:" +
	"/home/lpxz/eclipse/workspace/openjms/lib/commons-dbcp-1.2.1.jar:" +
	"/home/lpxz/eclipse/workspace/openjms/lib/xml-apis-1.0.b2.jar:" +
	"/home/lpxz/eclipse/workspace/openjms/lib/xerces-2.4.0.jar:" +
	"/home/lpxz/eclipse/workspace/openjms/lib/commons-pool-1.2.jar:" +
	"/home/lpxz/eclipse/workspace/openjms/lib/commons-collections-2.1.jar:" +
	"/home/lpxz/eclipse/workspace/openjms/lib/commons-logging-1.0.4.jar:";

	public static String outputDir = System.getProperty("user.dir")+"/sootoutput";

	

	public static HashSet<String> applicationClassNames;
	public static String getClassNameFromMSig(String msig)
	{
		//<org.exolab.jms.net.multiplexer.Multiplexer: int getNextChannelId()> 
		int lbrace =msig.indexOf('<');
		int maohao = msig.indexOf(':');
		return msig.substring(lbrace+1, maohao);		
	}

	public static void setPhaseOptionsForSparkWork() {
		PhaseOptions.v().setPhaseOption("jb", "enabled:true");
	    Options.v().set_keep_line_number(true);
	 	PhaseOptions.v().setPhaseOption("jb", "use-original-names:false");
	    Options.v().set_whole_program(true);
	    Options.v().set_app(true);
	    
	   //Enable Spark
	    HashMap<String,String> opt = new HashMap<String,String>();
	    //opt.put("verbose","true");
	    opt.put("propagator","worklist");
	    opt.put("simple-edges-bidirectional","false");
	    opt.put("on-fly-cg","true");
	    opt.put("set-impl","double");
	    opt.put("double-set-old","hybrid");
	    opt.put("double-set-new","hybrid");
	    opt.put("pre_jimplify", "true");
	    SparkTransformer.v().transform("",opt);
	    PhaseOptions.v().setPhaseOption("cg.spark", "enabled:true");
					
			List excludesList= new ArrayList();
			for(String exclude : unInstruClasses)
			{
				excludesList.add(exclude);
			}		
			Options.v().set_exclude(excludesList);
	      
	 
			
		}

	public static void setOutputDir(String outputdirpara)
	{
		outputDir = outputdirpara;
	}
//	" -cp /home/lpxz/java_standard/jre/lib/jce.jar:/home/lpxz/java_standard/jre/lib/rt.jar:" 
	public static void sootLoadNecessary() {
	
		String excludeArgString = "";   
		 String includeArgString = "";    
		 
		 
		 for(String excludeItem : unInstruClasses) // default.
        {
			excludeArgString +=" -x " + excludeItem ;
        } 
		 
		 if(DconPropertyManager.excludeString!=null && DconPropertyManager.excludeString.length()!=0)
		 {
			 String[] excludes =DconPropertyManager.excludeString.split(" ");
			 for(String excludeItem : excludes)
	        {
				excludeArgString +=" -x " + excludeItem ;
	        } 				 
		 }
				 
		 if(DconPropertyManager.includeString!=null && DconPropertyManager.includeString.length()!=0)
		 {
				String[] includes =DconPropertyManager.includeString.split(" ");	     
				for(String includeItem : includes)
		        {
					includeArgString +=" -i " + includeItem ;
		        }
		 }		
       // redirect the input to the Pre.
		    String argsOfToyW = "-w -app -p jb use-original-names:false -p cg.spark enabled:true "+ 
			    "-pp -cp .:/home/lpxz/pool/jdk1.6.0_13/jre/lib/jsse.jar:" + DconPropertyManager.ClassPath + " " + "-main-class "+ DconPropertyManager.mainClass + " " +  DconPropertyManager.mainClass
			    + " -d " + outputDir +  excludeArgString + includeArgString;
	   
	    		    System.out.println(argsOfToyW);
	    		    
			    String interString = argsOfToyW;
				String[] finalArgs = interString.split(" ");
				 soot.Main.v().processCmdLine(finalArgs);	
				 setPhaseOptionsForSparkWork();
		       
			    for(String classname:applicationClassNames)
			    {	    	
			    		SootClass curclass = Scene.v().loadClassAndSupport(classname);	 
			    		 curclass.setApplicationClass();			   
			    }	 
			    SootClass curclass = Scene.v().loadClassAndSupport(DconPropertyManager.observerClass);
			    curclass.setApplicationClass();	
	              Scene.v().loadNecessaryClasses();// at last                
		}

	public static Unit getUnit(SootMethod sm1, String pString, int pLine) {
		// if problems, just collect all possible stmts, and choose the cloest one.
		try {
		if (!sm1.hasActiveBody())
			sm1.retrieveActiveBody();
	} catch (Exception e) {
		System.out.println(sm1.getDeclaringClass().getName()
				+ sm1.getName());
	}
	
	Body b = sm1.getActiveBody();
	
	Iterator it =b.getUnits().iterator();
	 Unit ret = null;
	 while (it.hasNext()) {
	 Unit object = (Unit) it.next();
	 if(object.toString().equals(pString) && getLineNum(object)==pLine)
	 {
		 
	 ret = object;
	 break;
	 }
				
	 }
		if(ret ==null ) throw new RuntimeException("check line NO!");
		return ret;
	//return ret;
	}



	public static void retrieveAppClasses(List list) {
		 applicationClassNames = new HashSet<String>();
			
			for(Object elem : list)
			{
				CSMethodPair pair = (CSMethodPair)elem;
				CSMethod o1 = pair.getO1();// must be pc!
				CSMethod o2 = pair.getO2();	
				
				
				String m1sig =o1.getMsig();
				String m2sig = o2.getMsig();
				String classname = getClassNameFromMSig(m1sig);
				String classname2 = getClassNameFromMSig(m2sig);
				
				
				applicationClassNames.add(classname);
				applicationClassNames.add(classname2);	
				
				Iterator<ContextMethod> it =o1.getContexts().iterator();				
				while (it.hasNext()) {
					ContextMethod methodCallsite = it.next();
					String tmp = getClassNameFromSootMSig(methodCallsite.curMsig);
					
					applicationClassNames.add(tmp);				
				}
				
				
				Iterator<ContextMethod> it2 =o2.getContexts().iterator();
				while (it2.hasNext()) {
					ContextMethod methodCallsite =  it2.next();
					String tmp = getClassNameFromSootMSig(methodCallsite.curMsig);
				
					applicationClassNames.add(tmp);				
				}
				
			}
		    
		
	}

	// set the output, and load classes
	public static void prepareSoot(String classOutputFolder, List list) {
		SootAgent4Fixing.setOutputDir(DconPropertyManager.classOutputFolder);	//Dcon/output/Simple
		SootAgent4Fixing.retrieveAppClasses(list);		
		SootAgent4Fixing.sootLoadNecessary();
		
	}

	public static Body getJimpleBody(ContextMethod curSTE) {
		String msig = curSTE.getCurMsig();
		SootMethod sootMethod = Scene.v().getMethod(msig);	
		sootMethod.retrieveActiveBody(); 	
		Body bb =sootMethod.getActiveBody();
		return bb;
	}

	public static DirectedGraph createCFGGraph(Body bb) {
		DirectedGraph ug= null;
		if(DconPropertyManager.construct4unitgraph)
		{			
			 ug = new BriefUnitGraph(bb);	
		}
		else {
			ug = new ZonedBlockGraph(bb);// it is just blockCFG...
		}	
		return ug;
	}

	public static boolean isExceptionalHead(Object head) {
		if(head instanceof IdentityStmt)
		{
		   IdentityStmt idStmt =	(IdentityStmt) head;
		   if(idStmt.getRightOp() instanceof JCaughtExceptionRef)
		   {
			   return true;// no need to walk along this way.
		   }
 		}
		return false;
	}
}
