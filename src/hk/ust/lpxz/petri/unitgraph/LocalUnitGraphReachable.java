package hk.ust.lpxz.petri.unitgraph;





import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.Pair;
import edu.hkust.clap.organize.CSMethod;


public class LocalUnitGraphReachable {

	public static Stack systemStack = new Stack();
    public static Set visited = new HashSet();   
    
    
   //public static HashMap<Pair, Set> pc2Protected= new HashMap<Pair, Set>();
   
   public static HashMap<Set, Set> protected2Xedges = new HashMap<Set, Set>();
     


	public static Set protectedSet(DirectedGraph ug, Object pnode , Object cnode)
    {  // check carefully for the exceptional branches
        Set reachable =reachable_no_cross(ug, pnode, cnode);
        Set backreachable = back_reachable_no_cross(ug, cnode, pnode);
        return intersect(reachable, backreachable);    	
    }
    

	private static Set reachable(DirectedGraph ug, Object unit) {
    	Set toretSet = new HashSet();
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
  	
		    	
		    	if(!visited.contains(child))
		    	{
		    	    visited.add(child);
		    		systemStack.push(child);				    		
		    	}
		    }
		    
		}
		toretSet.addAll(visited);
		return toretSet;
	}
    
    private static Set back_reachable(DirectedGraph ug, Object unit) {
    	Set toretSet = new HashSet();
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
		    List children = getSuccsOnReversedGraph(ug,pop);//ug.getSuccsOf(pop);
		    for(int i = children.size()-1; i>=0; i--)
		    {
		    	Object child = children.get(i);	
		    	
		    	
		    	if(!visited.contains(child))
		    	{
		    	    visited.add(child);
		    		systemStack.push(child);				    		
		    	}
		    }
		    
		}
		toretSet.addAll(visited);
		return toretSet;
	}
    
	private static List getSuccsOnReversedGraph(DirectedGraph ug, Object pop) {		
		return ug.getPredsOf(pop);
	}
	
	
	private static Set reachable_no_cross(DirectedGraph ug, Object unit, Object bound) {
    	Set toretSet = new HashSet();
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
		    List children = getSuccs_awareof_bound(ug, pop, bound);//ug.getSuccsOf(pop);
		    for(int i = children.size()-1; i>=0; i--)
		    {
		    	Object child = children.get(i);	
		    	
		    	if(!visited.contains(child))
		    	{
		    	    visited.add(child);
//		    	    if(child==null)
//		    	    {
//		    	    	System.out.println("what happened?");
//		    	    	
//		    	    }
		    		systemStack.push(child);				    		
		    	}
		    }
		    
		}
		toretSet.addAll(visited);
		return toretSet;
	}
	

	public static List emptyList = new ArrayList();
	private static List getSuccs_awareof_bound(DirectedGraph ug, Object pop,
			Object bound) {
		if(pop!=bound)
			return ug.getSuccsOf(pop);
		else
			return emptyList;
	}

	
	private static Set back_reachable_no_cross(DirectedGraph ug, Object unit, Object bound) {
    	Set toretSet = new HashSet();
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
		    List children = getSuccs_onReversedGraph_awareof_bound(ug, pop, bound);//ug.getSuccsOf(pop);
		    for(int i = children.size()-1; i>=0; i--)
		    {
		    	Object child = children.get(i);	
		    	
		    	
		    	if(!visited.contains(child))
		    	{
		    	    visited.add(child);
		    		systemStack.push(child);				    		
		    	}
		    }
		    
		}
		toretSet.addAll(visited);
		return toretSet;
	}
	
	private static List getSuccs_onReversedGraph_awareof_bound(
			DirectedGraph ug, Object pop, Object bound) {
		if(pop!=bound)
			return ug.getPredsOf(pop);
		else
			return emptyList;
	}
	
	
	public static Set  intersect(List list1, List list2)
	{
		Set ret= new HashSet();
		for(Object o  : list1)
		{
			if (list2.contains(o))
			{
				ret.add(o);
			}
		}
		
		return ret;
	}
	
	public static Set  intersect(Set list1, Set list2)
	{
		Set ret= new HashSet();
		for(Object o  : list1)
		{
			if (list2.contains(o))
			{
				ret.add(o);
			}
		}
		
		return ret;
	}
	
	


	// List entryPoints=EntryPoints.v().methodsOfApplicationClasses();
	// List mainEntries = new ArrayList();
	// for(int i=0;i< entryPoints.size(); i++)
	// {
	// if(entryPoints.get(i).toString().contains("main"))
	// {
	// mainEntries.add(entryPoints.get(i));
	// }
	// }
	//
	// soot.Scene.v().setEntryPoints(mainEntries); // process : app and its
	// reachable methods.

	public static Set protectedUnitsSet(CSMethod csMethod)
	{// should be available after conputing the Xedges
		if(csMethod.method_type.equals(CSMethod.RMethod))
		{
			Set protectedSet = new HashSet();
			if(csMethod.getRunit()==null)
				throw new RuntimeException();
			protectedSet.add(csMethod.getRunit());
			return protectedSet;
		}
		else {
			Unit pnode= csMethod.getPunit();
			 Unit cnode =csMethod.getCunit();
		     Pair tmp = new Pair(pnode, cnode);
		    // for safety, I am not sure whetehr the underlying body would be reconstructed
		     SootMethod pcm= Scene.v().getMethod(csMethod.getMsig());
				UnitGraph pcug = new BriefUnitGraph(pcm.getActiveBody());// give u one chance
		     Set protectedSet = protectedSet =protectedSet(pcug, pnode, cnode);;//pc2Protected.get(tmp);
		     if(protectedSet==null)
		    	 throw new RuntimeException();
		     return protectedSet;
		}
	    
	}




}
