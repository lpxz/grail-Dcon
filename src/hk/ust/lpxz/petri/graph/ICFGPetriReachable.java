package hk.ust.lpxz.petri.graph;

import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.ArcFromResource;
import hk.ust.lpxz.petri.unit.PlaceMethodEntry;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceMethodExit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.sun.org.apache.bcel.internal.generic.NEW;

import edu.hkust.clap.lpxz.context.ContextMethod;
import edu.hkust.clap.organize.CSMethod;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;

public class ICFGPetriReachable {


	public static List emptyList = new ArrayList();
	public static Stack systemStack = new Stack();
    public static Set visited = new HashSet();   

  
	//==============================================================
	
	// now comes the CSGraph
	// better change the name ! unit is for unit, here, it is the node,,
	
	public static Set allInBetween(Petri csG, Place pnode , Place cnode)
    {  // check carefully for the exceptional branches
        Set reachable =all_reachable_no_cross(csG, pnode, cnode);
        Set backreachable = all_backreachable_no_cross(csG, cnode, pnode);
        Set intersectSet = new HashSet();
        intersect(reachable, backreachable, intersectSet);
        intersectSet.add(pnode);
        intersectSet.add(cnode);
        return intersectSet;    	
    }
	
   static Set reachable = new HashSet();
   static Set backreachable = new HashSet();
   static Set intersectSet  = new HashSet();
   
   
   //warning: method exit node is not reachable, for example in openjms, org.exolab.jms.common.uuid.Clock.run()
   // while-loop. for methods, we can avoid the problem by forwarding only. no backwarding.   
	public static void localAllInBetween(Petri csG, Place pnode , Place cnode, Set<Place> includedPlaces)
    {  // check carefully for the exceptional branches

        reachable.clear();
        backreachable.clear();
        intersectSet.clear();
        
        local_reachable_no_cross(csG, pnode, cnode ,reachable);
        local_backreachable_no_cross(csG, cnode, pnode ,backreachable);
        if((cnode instanceof PlaceMethodExit)&&backreachable.contains(cnode) && backreachable.size()==1)// a bug happens, see the prologue of this method
        {
        	intersectSet.addAll(reachable);// ignore the backrechable. for fixing a bug
        }else
        {
        	intersect(reachable, backreachable, intersectSet);
        }
        
        
        for(Object o : intersectSet)// may contain transition, for the mature petri net. for youngpetri, no problem
        {
        	if(o instanceof Place)
        	{
                includedPlaces.add((Place)o);// transfer the data

        	}
        }
        includedPlaces.add(pnode);
        includedPlaces.add(cnode);

       
    }
	
	
	
	
	// exit node may not be reachable because of the infinite loop.
	public static void placesInMethod(Petri csG, Place pnode , Place cnode, Set<Place> toret)
    { 
        reachable.clear();        
        local_reachable_no_cross(csG, pnode, cnode ,reachable);         
        for(Object o : reachable)
        {
        	if(o instanceof Place)
        	{
                toret.add((Place)o); // do not include transitions.
        	}
        }
        toret.add(pnode);
        toret.add(cnode);       
    }
	
	static Set inside = new HashSet();
	public static Set<Arc> localAllEdgesInBetween(Petri csG, Place pnode , Place cnode)
    {  // check carefully for the exceptional branches
		
		inside.clear();
		Set<Arc> ret = new HashSet<Arc>();
		localAllInBetween(csG, pnode, cnode, inside);
		for(Object node : inside)
		{
			List predEdges =csG.getLocalPrecArcs(node);
			List succEdges = csG.getLocalSuccArcs(node);
			
			for(Object predEdge : predEdges)
			{
				Arc predArc = (Arc) predEdge;
				if(inside.contains(predArc.getSource())&& inside.contains(predArc.getTarget()))// maybe redundant, as the edges are all local
				{
				  ret.add(predArc);						 
				}
			}
			
			for(Object succEdge : succEdges )
			{
				Arc succArc = (Arc) succEdge;
				if(inside.contains(succArc.getSource()) && inside.contains(succArc.getTarget()))
				{
					ret.add(succArc);
				}
			}
			
		}
		
		return ret ;
		
	
    }
	
	public static Set<Arc> ArcsInMethod(Petri csG, Place pnode , Place cnode)// entry, exit
    {  // check carefully for the exceptional branches		
		inside.clear();
		Set<Arc> ret = new HashSet<Arc>();
		placesInMethod(csG, pnode, cnode, inside);
		for(Object node : inside)
		{
			List predEdges =csG.getLocalPrecArcs(node);
			List succEdges = csG.getLocalSuccArcs(node);
			
			for(Object predEdge : predEdges)
			{
				Arc predArc = (Arc) predEdge;
				if(inside.contains(predArc.getSource())&& inside.contains(predArc.getTarget()))// maybe redundant, as the edges are all local
				{
				  ret.add(predArc);						 
				}
			}
			
			for(Object succEdge : succEdges )
			{
				Arc succArc = (Arc) succEdge;
				if(inside.contains(succArc.getSource()) && inside.contains(succArc.getTarget()))
				{
					ret.add(succArc);
				}
			}
			
		}
		
		return ret ;
		
	
    }
	// now comes the CSGraph
	// better change the name ! unit is for unit, here, it is the node,,
	public static Set local_reachable(Petri csG, Object unit) {
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
		    List children =csG.getLocalSuccs(pop);// ug.getSuccsOf(pop);
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
	
	public static Set local_backreachable(Petri csG, Object unit) {
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
		    List children =csG.getLocalPrecs(pop);//simplify it
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

	
	public static void local_reachable_no_cross(Petri csG, Object unit, Object bound, Set toretSet) {
    	
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
		    List children =getLocalSuccs_awareof_bound(csG, pop, bound);//csG.getLocalSuccs(pop);// ug.getSuccsOf(pop);
		    for(int i = children.size()-1; i>=0; i--)
		    {
		    	Object child = children.get(i);	
		    		    	
		    	
		    	if(!visited.contains(child))
		    	{
		    		//System.out.println(((Place)pop).petriName + "->" + ((Place)child).petriName );		    		
		    	    visited.add(child);
		    		systemStack.push(child);				    		
		    	}
		    }
		    
		}
		toretSet.addAll(visited);
		
	}
	public static List getLocalSuccs_awareof_bound(Petri csG, Object pop,
			Object bound) {
		if(pop!=bound)
		{
			return csG.getLocalSuccs(pop);
		}
		else {
			return emptyList;
		}	
	}
	
	public static void local_backreachable_no_cross(Petri csG, Object unit, Object bound, Set toretSet) {
    	
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
		    List children =getLocalPrecs_awareof_bound(csG, pop, bound);//csG.getLocalSuccs(pop);// ug.getSuccsOf(pop);
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
		
	}

	public static List getLocalPrecs_awareof_bound(Petri csG, Object pop,
			Object bound) {
		if(pop !=bound)
		{
			return csG.getLocalPrecs(pop);
		}
		else {
			return emptyList;
		}
	}
	
	//================================================global 
	public static Set all_reachable(Petri csG, Object unit) {
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
		    List children =csG.getAllSuccs(pop);// ug.getSuccsOf(pop);
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

	
	public static Set all_backreachable(Petri csG, Object unit) {
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
		    List children =csG.getAllPrecs(pop);//simplify it
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

	
	public static Set all_reachable_no_cross(Petri csG, Object unit, Object bound) {
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
		    List children =getAllSuccs_awareof_bound(csG, pop, bound);//csG.getLocalSuccs(pop);// ug.getSuccsOf(pop);
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
	public static List getAllSuccs_awareof_bound(Petri csG, Object pop,
			Object bound) {
		if(pop!=bound)
		{
			return csG.getAllSuccs(pop);
		}
		else {
			return emptyList;
		}	
	}
	
	public static Set all_backreachable_no_cross(Petri csG, Object unit, Object bound) {
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
		    List children =getAllPrecs_awareof_bound(csG, pop, bound);//csG.getLocalSuccs(pop);// ug.getSuccsOf(pop);
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

	public static List getAllPrecs_awareof_bound(Petri csG, Object pop,
			Object bound) {
		if(pop !=bound)
		{
			return csG.getAllPrecs(pop);
		}
		else {
			return emptyList;
		}
	}
	
	//=======================================================================

	
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
	
	public static void  intersect(Set list1, Set list2 ,Set ret)
	{
		
		for(Object o  : list1)
		{
			if (list2.contains(o))
			{
				ret.add(o);
			}
		}
		
	
	}
	public static void main(String[] args) {
		

	}

	public static Set<Place> interProtectedStatements(Petri csGraph,CSMethod pcCS) {
		if(pcCS.isPC())
		{
			
            // for histroy reason, the last one in the ctxts is the current method, remove it for searching!
			List<ContextMethod> ctxts = pcCS.getContexts();	
			//@@test, .subList(0, ctxts.size()-1) or ctxts!!!!!!
			PetriMethod graphPCM = PetriMethodManager.retrievePetriMethod(pcCS.getMsig(), ctxts);// the secondmethod's ctxt : 0-> (size-2)
			SootMethod pcm =graphPCM.getBb().getMethod();// it must be processed, just now or earlier
			
			Place pnode =graphPCM.getUnit2Place().get(pcCS.getPunit());
			Place cnode =graphPCM.getUnit2Place().get(pcCS.getCunit());
			
			// pnode and cnode may be invokeStatement
//			if(!csGraph.coreG.containsVertex(pnode))
//			{ 
//				pnode =YoungPetriLowLevelBuilder.invoke2begin.get(pnode);
//				
//			}
//			if(!csGraph.coreG.containsVertex(cnode))
//			{
//				cnode = YoungPetriLowLevelBuilder.invoke2end.get(cnode);
//			}
			
			Set<Place> inPC =ICFGPetriReachable.allInBetween(csGraph, pnode, cnode);
			return inPC;
		}
		else if(pcCS.isR()){
			Set<Place> toret = new HashSet<Place>();
			List<ContextMethod> ctxts = pcCS.getContexts();
			PetriMethod graphRM = PetriMethodManager.retrievePetriMethod(pcCS.getMsig(), ctxts);// the secondmethod's ctxt : 0-> (size-2)
			SootMethod rm =graphRM.getBb().getMethod();// it must be processed, just now or earlier
		   
			Stmt runit = (Stmt)pcCS.getRunit();
			Place rnode =graphRM.getUnit2Place().get(runit);
			toret.add(rnode);			


			return toret;
		}
		else {
			throw new RuntimeException();
		}
		
		
	}

}
