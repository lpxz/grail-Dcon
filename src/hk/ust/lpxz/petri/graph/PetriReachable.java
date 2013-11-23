package hk.ust.lpxz.petri.graph;

import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.ArcCall;
import hk.ust.lpxz.petri.unit.ArcReturn;
import hk.ust.lpxz.petri.unit.ArcToResource;
import hk.ust.lpxz.petri.unit.Place;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class PetriReachable {
// this petri net is the mature one, after the lock resource is corerctly modeled
	// and the transitions are all added.

	public static List emptyList = new ArrayList();
	public static Stack systemStack = new Stack();
    public static Set visited = new HashSet();   

    
	
	
	static Set<Place> onlyPlaces = new HashSet<Place>();
	public static void all_reachablePlaces_noPassing_PRs_domain(Petri csG, Set units, Set toretSet)
	{
		onlyPlaces.clear();
		if(toretSet.size()>=1) throw new RuntimeException("not the right way to use");
		all_reachable_noPassing_PRs_domain( csG,  units,  toretSet);
		for(Object obj : toretSet)
		{
			if(obj instanceof Place)
			{
				onlyPlaces.add((Place)obj);
			}
		}
		toretSet.clear();
		toretSet.addAll(onlyPlaces);
	}

	// the nodes called and the shallow ones in teh units.
	static Set directCallees = new HashSet();
	public static void all_reachable_noPassing_PRs_domain(Petri csG, Set units, Set toretSet) {
    	//Set toretSet = new HashSet();
		directCallees.clear();
		directCallees(csG, units, directCallees);
		// just make sure no going beyond the cs
		// from directCallees!, otherwise, the node just outside the cs locally is included
        systemStack.clear();
        visited.clear();
        for(Object unit : directCallees)
        {
    		systemStack.push(unit);	
        	if(!visited.contains(unit))
        	{
        	    visited.add(unit);// direct callees added
        	}
        }

	
		while(!systemStack.isEmpty())
		{
		    Object pop =systemStack.pop();			    
			List childArcs =csG.getAllSuccEdges(pop);// ug.getSuccsOf(pop);
		    for(int i = childArcs.size()-1; i>=0; i--)
		    {
		    	Arc childArc = (Arc)childArcs.get(i);	
		    		
		    	// go directly, do not back (if back, it may come to the first level and go beyond the cs), do not diverge to some other functions using the lock.
		    	if(!(childArc instanceof ArcToResource) && !(childArc instanceof ArcReturn))// filter of children
		    	{
		    		Object child = childArc.getTarget();
			    	if(!visited.contains(child))
			    	{
			    		
			    		
				    	    visited.add(child);
				    		systemStack.push(child);// transitive callees added 
			    		
					    		
			    	}
		    	}

		    }
		    
		}
		toretSet.addAll(visited);
		toretSet.addAll(units);// units themselves, shallow ndoes
		
	}


//	public static Set all_reachable(YoungPetri csG, Object unit) {
//    	Set toretSet = new HashSet();
//        systemStack.clear();
//        visited.clear();
//		systemStack.push(unit);	
//    	if(!visited.contains(unit))
//    	{
//    	    visited.add(unit);
//    	}
//	
//		while(!systemStack.isEmpty())
//		{
//		    Object pop =systemStack.pop();			    
//		    List children =csG.getAllSuccs(pop);// ug.getSuccsOf(pop);
//		    for(int i = children.size()-1; i>=0; i--)
//		    {
//		    	Object child = children.get(i);	
//		    		
//		    	
//		    	if(!visited.contains(child))
//		    	{
//		    	    visited.add(child);
//		    		systemStack.push(child);				    		
//		    	}
//		    }
//		    
//		}
//		toretSet.addAll(visited);
//		return toretSet;
//	}

	

	
	//=======================================================================

	
	private static void directCallees(Petri csG, Set units,
			Set directCallees) {
		for(Object unit:units)
		{
			List childArcs =csG.getAllSuccEdges(unit);// ug.getSuccsOf(pop);
		    for(int i = childArcs.size()-1; i>=0; i--)
		    {
		    	Object childArc = childArcs.get(i);	
		    		
		    	if((childArc instanceof ArcCall))// filter of children
		    	{
		    		directCallees.add(((ArcCall) childArc).getTarget());
		    	}

		    }
			
		}
		
		
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

	
}
