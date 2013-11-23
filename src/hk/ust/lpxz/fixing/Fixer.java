package hk.ust.lpxz.fixing;

import edu.hkust.clap.organize.CSMethod;
import edu.hkust.clap.organize.CSMethodPair;
import edu.hkust.clap.organize.SaveLoad;
import graphviz.lib.GraphVizAPIs;
import hk.ust.lpxz.LockSynthesis.transformer.DconMonitorInjecter;
import hk.ust.lpxz.SBPI.SBPI;
import hk.ust.lpxz.linearprogramming.SpyroHeuristic;
import hk.ust.lpxz.petri.graph.ICFGPetriBuilder;
import hk.ust.lpxz.petri.graph.ICFGPetriStandardizer;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.graph.PetriMethodManager;
import hk.ust.lpxz.petri.graph.PetriVisualizer;
import hk.ust.lpxz.petri.graph.simplify.ICFGPetriShrinker;
import hk.ust.lpxz.statemachine.State;
import hk.ust.lpxz.statemachine.StateGraphBuilder_syncVio;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map;

import soot.PackManager;
import soot.SceneTransformer;
import soot.Transform;
import soot.tagkit.Host;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.SourceLnPosTag;





public class Fixer {

/// it will not generate the arcFromResources for critical section, placeCS.
	// as a result, during the patching, placeCS.getArcFromResources(0 will return empty.
	// as a result, the switching to fine locks fails.
	// I want to use this class previously to work for C programs.
	// rollback.
	
	public static void main(String[] args) {	
		DconPropertyManager.singleLock = false;
		DconPropertyManager.subsume_removal_opt = true;
		
		
		DconPropertyManager.initialize(args[0]);
		System.out.println(DconPropertyManager.projectname);
		
		final List bugList =loadBugs(DconPropertyManager.projectname);

  
        
//-w -app -p jb use-original-names:false -p cg.spark enabled:true -pp -cp .:/home/lpxz/pool/jdk1.6.0_13/jre/lib/jsse.jar:null -main-class null null -d /home/lpxz/eclipse/workspacegrail/Dcon/output/Test_loopfy -x org.apache.log4j. -x jrockit. -x java. -x javax. -x xjava. -x COM. -x com. -x cryptix. -x sun. -x sunw. -x junit. -x org.junit. -x org.xmlpull. -x edu.hkust.clap. -x org.apache.commons.logging. -x org.apache.xalan. -x org.apache.xpath. -x org.springframework. -x org.jboss. -x jrockit. -x edu. -x checkers. -x org.codehaus.spice.jndikit. -x EDU.oswego.cs.dl.util.concurrent.WaiterPreferenceSemaphore -x soot. -x aTSE. -x pldi. -x popl. -x beaver. -x org.jgrapht -x ca.pfv.spmf. -x japa.parser. -x polyglot. -x jasmin. -x Jama. -x jas. -x java_cup.
//Warning: Phase wjtp.tnlp is not a standard Soot phase listed in XML files.
		SootAgent4Fixing.prepareSoot(DconPropertyManager.classOutputFolder, bugList);	

       //LPXZ: commenting the code makes soot run fast but may be dangerous (trial usage).
		 PackManager.v().getPack("wjtp").add(new Transform("wjtp.fixer", new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName, Map options) {
				List avList = siftAVs(bugList);
				List asetvList = siftASetV(bugList);
		        System.out.println("we fix #bugs: " + avList.size());
				Petri csGraph =buildPetriNet(avList);		
				
				
		       visualPetriNet(csGraph); // optional, may be slow 
				
				Set<State> safe = new HashSet<State>();
				Set<State> unsafe = new HashSet<State>();
				StateGraphBuilder_syncVio.enumerateMarkings(safe, unsafe);	
				Reporter.reportDirectly("Allowed: ", safe.size());
				Reporter.reportDirectly("Forbidden ", unsafe.size());
		        
		        if (DconPropertyManager.minUnsafeMaxSafeOpt) {
		            SBPI.maxSafeFilter(safe); // remove some redundant numeric rep!
		            SBPI.minUnsafeFilter(unsafe);
		          }
		        
				SBPI.solve(safe, unsafe);
			    DconMonitorInjecter.solveLockInitializeTask();
			    DconMonitorInjecter.solveLockOperationTasks();

		       
			}			 
		 }));	    
	    PackManager.v().runPacks();// 1
			    
		PackManager.v().writeOutput();
		SootAgent4Fixing.sootDestroyNecessary();
    	    
		// release the panel resource.
System.out.println("output classes to: " + DconPropertyManager.classOutputFolder);
Reporter.flush();
		 System.exit(0);
	}


	public static List siftASetV(List bugList) {
		List ret = new LinkedList();
		for (Object elem : bugList) {
	            CSMethodPair pair = (CSMethodPair) elem;
	            CSMethod pcCS = pair.getO1(); // must be pc!
	            CSMethod rCS = pair.getO2();
	            if(rCS.getrAnc()==null)
	            {
	            	ret.add(pair);
	            }
		 }
		return ret;
	}


	public static List siftAVs(List bugList) {
		List ret = new LinkedList();
		for (Object elem : bugList) {
	            CSMethodPair pair = (CSMethodPair) elem;
	            CSMethod pcCS = pair.getO1(); // must be pc!
	            CSMethod rCS = pair.getO2();
//	            System.out.println("rcs:" + rCS.getrAnc());
	            
	            if(rCS.getrAnc()!=null)
	            {
	            	ret.add(pair);
	            }
		 }
		return ret;
}


	public static void visualPetriNet(Petri csGraph) {
		PetriVisualizer.visualize(csGraph, DconPropertyManager.PNdotFile); 
		GraphVizAPIs.renderFile(DconPropertyManager.PNdotFile);
	}

	private static Petri buildPetriNet(List bugList) {
		Petri csGraph = ICFGPetriBuilder.getICFGPetri(bugList);// CFG node-> place, edge->arc.		
		if(DconPropertyManager.shrinkPetri) ICFGPetriShrinker.shrinkYoungPetri();	// 
		ICFGPetriStandardizer.standardize(csGraph);	// ICFG petri - > real Petri
		Reporter.reportDirectly(" containing methods ", PetriMethodManager.containingMethods.size());
	//Reporter.store("Petri CSs", ICFGPetriStandardizer.petriCSs.size());
		Reporter.reportDirectly("Petri methods ", PetriMethodManager.getAllPetriMethods().size());
		Reporter.reportDirectly("Petri places ", csGraph.getPlaces().size());
	    Reporter.reportDirectly("Petri transitions ", csGraph.getTransitions().size());
		Reporter.reportDirectly("lock places", ICFGPetriStandardizer.lockplaces.size());
		Reporter.reportDirectly("arcs from resource", ICFGPetriStandardizer.arcFromResource.size());
		Reporter.reportDirectly("arcs to resource", ICFGPetriStandardizer.arcToResource.size());		
		
       
        
		return csGraph;
	}

	private static List loadBugs(String projectname) {
		List list =null;
		if(DconPropertyManager.applyFilteringBadApples) 
        {
			System.out.println("load bugs from " + SaveLoad.default_MCPairList_afterFiltering(projectname));
        	list = (List)SaveLoad.load( SaveLoad.default_MCPairList_afterFiltering(projectname) );
        }
        else
        {
        	System.out.println("load bugs from " + SaveLoad.default_MCPairList(projectname));
        	list = (List)SaveLoad.load(SaveLoad.default_MCPairList(projectname));
        }	
		return list;
	}



	static HashMap<String, String> orig2wildcard = new HashMap<String, String>();
	static HashMap<String, String> new2old_s = new HashMap<String, String>();

	public static int getLineNum(Host h) {
		if (h.hasTag("LineNumberTag")) {
			return ((LineNumberTag) h.getTag("LineNumberTag")).getLineNumber();
		}
		if (h.hasTag("SourceLineNumberTag")) {
			return ((SourceLineNumberTag) h.getTag("SourceLineNumberTag"))
					.getLineNumber();
		}
		if (h.hasTag("SourceLnPosTag")) {
			return ((SourceLnPosTag) h.getTag("SourceLnPosTag")).startLn();
		}
		return -1;
	}


}