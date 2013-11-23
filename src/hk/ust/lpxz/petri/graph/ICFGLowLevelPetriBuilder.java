package hk.ust.lpxz.petri.graph;

import hk.ust.lpxz.fixing.SootAgent4Fixing;
import hk.ust.lpxz.petri.unit.ArcCall;
import hk.ust.lpxz.petri.unit.ArcLocal;
import hk.ust.lpxz.petri.unit.ArcReturn;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
import hk.ust.lpxz.petri.unit.PlaceMethodEntry;
import hk.ust.lpxz.petri.unit.PlaceMethodExit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.tagkit.Host;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import edu.hkust.clap.lpxz.context.ContextMethod;

public class ICFGLowLevelPetriBuilder {	
	public static PetriMethod buildPetriMethod(ContextMethod curSTE, Petri csGraph,
			List<ContextMethod> ctxtStack) { 	
		Body bb = SootAgent4Fixing.getJimpleBody(curSTE);		
		if(bb!=null)
		{
			PetriMethod petriMethod = new PetriMethod();
			petriMethod.setClassname(bb.getMethod().getDeclaringClass().getName());
			petriMethod.setMethodName(bb.getMethod().getName());		
			petriMethod.setJimpleBody(bb);
			petriMethod.setContexts(snapshot(ctxtStack));
	    	PlaceMethodEntry start = createAndAddEntryPlace(petriMethod, bb, csGraph, ctxtStack);
	    	PlaceMethodExit end = createAndAddExitPlace(petriMethod,bb, csGraph, ctxtStack);    
	    	petriMethod.setEntry(start); // set the artificial entry
	    	petriMethod.setExit(end );// set the artificial exit
	    	HashMap<Unit, Place> unit2place = petriMethod.getUnit2Place_or_create();
	    	
			DirectedGraph ug= SootAgent4Fixing.createCFGGraph(bb);			
			List headsList = ug.getHeads();	  
	    		
	    	for(Object head : headsList)
	    	{
	    		if(SootAgent4Fixing.isExceptionalHead(head)) continue;       		
	        	PlaceCommonLocal headPlace = createAndAddPlace(petriMethod, (Stmt)head,csGraph);    	
	    		unit2place.put((Stmt)head, headPlace);
	    		connectPlace2Place(csGraph,start, headPlace);	    		
	    		buildPetriMethodCore(petriMethod, ug, head, csGraph,  start, end, unit2place); 
	    	}       
	        return petriMethod;	
		}
		else {
			throw new RuntimeException("what is up...");
		}
	}
	
	
	

	public static Body search4MethodBodyInClass(String msig, String className) {
	    SootClass sc = Scene.v().loadClassAndSupport(className);
	    sc.setApplicationClass();
	    Scene.v().loadNecessaryClasses();
	    SootMethod sm =Scene.v().getMethod(msig);
	    if(!sm.hasActiveBody())
              sm.retrieveActiveBody();
	    Body bb  =sm.getActiveBody();
	    return bb;	   
	}
	
	
	public static List<ContextMethod> snapshot(List<ContextMethod> ctxtStack )
	{
		if(ctxtStack==null) return null;
		List<ContextMethod> ret = new ArrayList<ContextMethod>();
		ret.addAll(ctxtStack);
		return ret;
		
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

	public static void connectPlace2Place(Petri csGraph, Place start, Place p) {
		if(csGraph.coreG.containsVertex(start) &&  csGraph.coreG.containsVertex(p))
		{
			if(csGraph.coreG.containsEdge(start, p))// single-edge between two nodes!, due to the normal head and the exceptional head, exitmethod->exitThread may be added twice.
			{
				return;
			}
			csGraph.coreG.addEdge_edgetype_lpxz(start, p, ArcLocal.class);
		}
		else {
			throw new RuntimeException("wrong");
		}		
	}

	public static Stack systemStack = new Stack();
    public static Set visited = new HashSet();   

    
    private static void buildPetriMethodCore(PetriMethod toret, DirectedGraph ug, Object unit, Petri csGraph,
    		PlaceMethodEntry start,PlaceMethodExit end,  HashMap<Unit, Place> tmpu2n) {    	
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
		    List children = ug.getSuccsOf(pop);
		    for(int i = children.size()-1; i>=0; i--)
		    {
		    	Object child = children.get(i);	
		    	Place parentPlace = get_createAndAddPlace(tmpu2n, toret,(Stmt)pop, csGraph);
		    	Place childPlace = get_createAndAddPlace(tmpu2n, toret,(Stmt)child, csGraph);
     	    	connectPlace2Place(csGraph,parentPlace, childPlace);
		    	
		    	if(!visited.contains(child))
		    	{
		    	    visited.add(child);
		    		systemStack.push(child);				    		
		    	}
		    }
		    
		    if(children.size() ==0)
		    {
		    	// tail in infinite loops may not connect to end.
		    	UnitGraph ugg = (UnitGraph)ug;
		    	if(!ugg.getTails().contains(pop)) throw new RuntimeException();
		    	Place parentPlace = get_createAndAddPlace(tmpu2n, toret,(Stmt)pop, csGraph);
		    	connectPlace2Place(csGraph,parentPlace,  end); 
		    }		    
		}					
	}


	private static Place get_createAndAddPlace(HashMap<Unit, Place> tmpu2n,
			PetriMethod petriMethod, Stmt stmt, Petri csGraph) {
		Place place = null; 
		if(tmpu2n.containsKey(stmt))
    	{
    		place = tmpu2n.get(stmt);
    	}else {
			place = createAndAddPlace(petriMethod,(Stmt)stmt, csGraph);				
			tmpu2n.put((Stmt)stmt, place);								
		}
		return place;		
	}




	public static PlaceMethodEntry createAndAddEntryPlace( PetriMethod toret, Body bb, Petri csGraph, List<ContextMethod> ctxtStack) {
        PlaceMethodEntry entry = new PlaceMethodEntry();
        entry.setEnclosingM(toret);
        csGraph.coreG.addVertex(entry);        
	    return entry;
	}
    
	public static PlaceMethodExit  createAndAddExitPlace( PetriMethod toret, Body bb, Petri csGraph, List<ContextMethod> ctxtStack) {
    	PlaceMethodExit exit = new PlaceMethodExit();
    	exit.setEnclosingM(toret);
    	csGraph.coreG.addVertex(exit);
    	return exit;
	}
    
    // under the same context:
    // one stmt-> one place
	public static PlaceCommonLocal createAndAddPlace(PetriMethod toret, Stmt unit,
			Petri csGraph) {
		PlaceCommonLocal cls = new PlaceCommonLocal();
		
		cls.setEnclosingM(toret);
		cls.setJimpleStmt(unit);
		if(unit!=null)
		{
			cls.setJimpleStmtLine(getLineNum(unit));
		}
				
		csGraph.coreG.addVertex(cls);
		return cls;
	}

    public static  void remoteIssues(Petri csGraph, PlaceCommonLocal invokeNode, PlaceMethodEntry entry,
			PlaceMethodExit exit) {
    
		csGraph.coreG.addEdge_edgetype_lpxz(invokeNode, entry, ArcCall.class);
    	csGraph.coreG.addEdge_edgetype_lpxz(exit, invokeNode, ArcReturn.class);
    }

}
