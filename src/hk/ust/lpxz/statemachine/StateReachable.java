package hk.ust.lpxz.statemachine;

import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.graph.PetriMethodManager;
import hk.ust.lpxz.petri.graph.ICFGPetriReachable;
import hk.ust.lpxz.petri.unit.Place;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;



import soot.SootMethod;
import soot.jimple.Stmt;
import edu.hkust.clap.organize.CSMethod;


// collective entries, or single entry
// simple traversal, or bounded traversal

public class StateReachable {
	public static List emptyList = new ArrayList();
	public static Stack systemStack = new Stack();
    public static Set visited = new HashSet();   
    
	   static Set reachable = new HashSet();
	   static Set backreachable = new HashSet();
	   static Set intersectSet  = new HashSet();
	   // 
	
	
	

	
	

	
		private static void safetyCheck(Object pop, Set unsafe) {
			 if(!safe(pop, unsafe))
			 {
				 throw new RuntimeException("the parent is even unsafe.");
			 }
		}
		public static boolean safe(Object o, Set unsafe) {
			 State ss = (State)o;
			 // RACE
//			 if(DconPropertyManager.race_to_be_fixed) // otherwise, we skip this branch code.
//			 {
//				 if(!ss.twoTokenPlaces.isEmpty())// races
//				 {
//					 return false;
//				 } 
//			 }			
			 
			 for(Object tmp: unsafe)
			{
				State un = (State)tmp;
				if(more_equal_Unsafe(ss, un))
				{
					return false;
				}					
			}			 
			 return true;			
		}
	
		public static boolean more_equal_Unsafe(Object o, Set unsafe) {
		//	110 is unsafe, 111 is unsafer
			 State ss = (State)o;	 
			 for(Object tmp: unsafe)
			{
				State un = (State)tmp;
				if(more_equal_Unsafe(ss, un))
				{
					return true;
				}					
			}			 
			 
			 if(ss.twoTokenPlaces.size()>=1 ||ss.oneTokenPlaces.size()>=3) //TODO lpxz
			{
				 //System.out.println(ss);
				 
				 return true;// no concurrent execution on three places!
			}
			 return false;			
		}
	
	private static boolean more_equal_Unsafe(State ss, State un) {
		  boolean cond1 = ss.oneTokenPlaces.containsAll(un.oneTokenPlaces);
		   boolean cond2 = ss.twoTokenPlaces.containsAll(un.twoTokenPlaces);		
			return cond1 && cond2;
		}
	//=======================the following are for searching safe states.
	// boundary optimization can be easily implemented by extendign the isSafe function!
	public static void reachableSafe(StateGraph csG, Object unit, Set unsafe , Set toretSet) {
		
        systemStack.clear();
        visited.clear();
		
    	if(!visited.contains(unit)&& safe(unit, unsafe))
    	{
    		systemStack.push(unit);	
    	    visited.add(unit);
    	}
	
		while(!systemStack.isEmpty())
		{
		    Object pop =systemStack.pop();	
		    safetyCheck(pop, unsafe);
		    List children =getSafeSuccs(csG, pop ,unsafe);//csG.getLocalSuccs(pop);// ug.getSuccsOf(pop);
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



	public static List getSafeSuccs(StateGraph csG, Object pop, Set unsafe) {
		//if(pop!=bound)
		List list = emptyList;// new ArrayList();
		for(Object o : csG.getSuccs(pop))
		{
			if(safe(o, unsafe))
			{
				if(list.equals(emptyList))// the first time, do it
				{
					list = new ArrayList();// lazy initialization, for memory
				}
				
				list.add(o);
			}
		}
		return list;
		
		
	}
	

	public static void backreachableSafe(StateGraph csG, Object unit, Set unsafe, Set toretSet) {
		
        systemStack.clear();
        visited.clear();
		
    	if(!visited.contains(unit) && safe(unit, unsafe))
    	{
    		systemStack.push(unit);	
    	    visited.add(unit);
    	}
	
		while(!systemStack.isEmpty())
		{
		    Object pop =systemStack.pop();	
		    safetyCheck(pop, unsafe);
		    List children =getSafePrecs(csG, pop, unsafe);//csG.getLocalSuccs(pop);// ug.getSuccsOf(pop);
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

	public static List getSafePrecs(StateGraph csG, Object pop,
			Set unsafe) {
		List list = emptyList;// new ArrayList();
		for(Object o : csG.getPrecs(pop))
		{
			if(safe(o, unsafe))
			{
				if(list.equals(emptyList))// the first time, do it
				{
					list = new ArrayList();// lazy initialization, for memory
				}
				
				list.add(o);
			}
		}
		return list;

	}
	
	public static List getUnconPrecs(StateGraph csG, Object pop) {
		List list = emptyList;// new ArrayList();
		List arcs =csG.getPrecArcs(pop);
		for(Object o : arcs)
		{
			StateTransition st = (StateTransition)o;
			if(!st.isControllable())// not controllable, good, need you
			{
				State prec = (State) st.getSource();
				if(list.equals(emptyList))// the first time, do it
				{
					list = new ArrayList();// lazy initialization, for memory
				}
				
				list.add(prec);
				
			}

		}
		return list;

	}
	
	

public static void backreachableAlongUncon(StateGraph csG, Set units,  Set toretSet) {

    systemStack.clear();
    visited.clear();
    for(Object unit : units)
    {
    	systemStack.push(unit);	
    	if(!visited.contains(unit) )
    	{
    	    visited.add(unit);
    	}
    }


	while(!systemStack.isEmpty())
	{
	    Object pop =systemStack.pop();	
	   
	    List children =getUnconPrecs(csG, pop);//csG.getLocalSuccs(pop);// ug.getSuccsOf(pop);
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
	//================================================global 
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
