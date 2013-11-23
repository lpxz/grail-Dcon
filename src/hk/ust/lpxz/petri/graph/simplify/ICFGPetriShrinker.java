package hk.ust.lpxz.petri.graph.simplify;

import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.graph.ICFGLowLevelPetriBuilder;
import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.graph.PetriMethodManager;
import hk.ust.lpxz.petri.graph.ICFGPetriReachable;
import hk.ust.lpxz.petri.graph.PetriVisualizer;
import hk.ust.lpxz.petri.graph.criticalsection.PetriCriticalSection;
import hk.ust.lpxz.petri.graph.criticalsection.PetriCriticalSectionManager;
import hk.ust.lpxz.petri.graph.violation.Violation;
import hk.ust.lpxz.petri.graph.violation.ViolationManager;
import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.ArcCall;
import hk.ust.lpxz.petri.unit.ArcLocal;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



import soot.jimple.MonitorStmt;

public class ICFGPetriShrinker {
   // eatable, some nodes are not eatable, and those which are not homo are not eatable
	// eat
	
	
	public static void shrinkYoungPetri()
	{
		System.out.println("size : " + Petri._petri.coreG.vertexSet().size());
		if(DconPropertyManager.simplify)
		{
			Set<PetriMethod> ypms =PetriMethodManager.getAllPetriMethods();
			for(PetriMethod ypm : ypms)
			{
				try
				{
				shrinkYoungPetriMethodLocally(ypm);		
				}
				catch(Exception e)
				{
					// ignore..
				}
			}	
		}
			
		System.out.println("size 2 : " + Petri._petri.coreG.vertexSet().size());
	}
	// need testing before further developing
	static Set<Place> stones0 = new HashSet<Place>();
	static Set<Place> stones1 = new HashSet<Place>();
	static Set<Place> stones2 = new HashSet<Place>();
	private static void shrinkYoungPetriMethodLocally(PetriMethod ypm) {
        // determine the stones, i.e., unEATable places.
		stones0.clear();
		stones1.clear();
		stones2.clear();
		
		Set<Place> stones = new HashSet<Place>();
		stones.add(ypm.getEntry()); stones.add(ypm.getExit());
		callerStones(ypm, stones0);		
		specialPlaceStones(ypm,stones1);
		crossingRegionStones(ypm,stones2); 
		stones.addAll(stones0);
		stones.addAll(stones1);
		stones.addAll(stones2);			
		structurePreservingEat(ypm, stones);
}
	

	static Set<Place> placesInMethod = new HashSet<Place>();
	private static void callerStones(PetriMethod ypm,
			Set<Place> stones0) {			
		Petri petri = Petri.getPetri();
		placesInMethod.clear();
		ICFGPetriReachable.placesInMethod(petri,  ypm.getEntry(), ypm.getExit(),placesInMethod); 		
		for(Place p:placesInMethod)
		{	
			List succEdges = petri.getAllSuccEdges(p);
			for(Object tmp: succEdges)
			{
			  if(tmp instanceof ArcCall)
			  {
				  stones0.add(p);
			  }
			}
		}
	}
	static HashMap<Place, Set<Place>> eatenBy = new HashMap<Place, Set<Place>>();
	// update the eatenBy
	static void eater2eatees(Place eater, Place eatee)
	{
		 Set<Place> eatees  = eatenBy.get(eater);
		 if(eatees==null)
		 {
			 eatees = new HashSet<Place>();
			 eatenBy.put(eater, eatees);
		 }
		 eatees.add(eatee);		
	}
	
	static Set<Place> getEatees(Place eater)
	{
		return eatenBy.get(eater);
	}
	
	
	static void getAllEatees(Set<Place> allEatees)
	{
		allEatees.clear();
		Iterator<Set<Place>> it = eatenBy.values().iterator();
		while (it.hasNext()) {
			Set<Place> valueSet = (Set<Place>) it.next();
			allEatees.addAll(valueSet);		
		}
		
	}
	
	
	
	static Set<Place> allEatees = new HashSet<Place>();
	static Set<Place> pset4Eat = new HashSet<Place>();
	private static void structurePreservingEat(PetriMethod ypm, Set<Place> stones) {		
		Petri youngPetri = Petri.getPetri();
		pset4Eat.clear();
		ICFGPetriReachable.placesInMethod(youngPetri, ypm.getEntry(), ypm.getExit(), pset4Eat);		
		int iteration =0;
		boolean changeInLastIteration = true;
		while(true)//add termination condition
		{
			iteration++;
			if(!changeInLastIteration)
			{
				break; // stable now.
			}
			changeInLastIteration =false;// reset the variable
			for(Place eater : pset4Eat)
			{		
				allEatees.clear();
				getAllEatees(allEatees);// compute on-demand
				if(allEatees.contains(eater))
				{
					continue;// you already die.
				}
				
				List precs = youngPetri.getLocalPrecs(eater);
				for(Object o1:precs)
				{
					Place prec = (Place)o1;
					boolean touchable = (!stones.contains(prec));
					boolean homo = HomoJudger.homo(eater, prec);
					boolean sPsC = singleParentSingleChild(prec);// to preserve structure. agressive pruning may also be possible.
					if(touchable && homo && sPsC) // judge eatable
					{
						eatParent(ypm, eater, prec);
						eater2eatees(eater, prec);
						changeInLastIteration= true;						
					}					
				}
				
				List succs  =youngPetri.getLocalSuccs(eater);
				for(Object o2: succs)
				{
					Place succ = (Place)o2;
					boolean touchable = (!stones.contains(succ));
					boolean homo = HomoJudger.homo(eater, succ);
					boolean sPsC = singleParentSingleChild(succ);// 
					if(touchable && homo && sPsC) // judge eatable
					{
						eatChild(ypm, eater, succ);						
						eater2eatees(eater, succ);
						changeInLastIteration= true;
					}
				}
			}		
		}
		
		
		
		
		
	}
	private static void eatChild(PetriMethod ypm, Place stone, Place succ) {
		 Petri youngPetri = Petri.getPetri();
		    // remove the prec node, it is useless now, check whether there are some other edges connecting to it.
			// TOFIX
		    if(!checkNoSideEffect(youngPetri,succ))
		    {
		    	//throw new RuntimeException();// what is up
		    }
		    
			// like the removal of an element in the linked list
		    Place succOfsucc = getChild(succ);
		    if(succOfsucc!=stone)
		    {
		    	ICFGLowLevelPetriBuilder.connectPlace2Place(youngPetri,  stone, succOfsucc);    
		    }
		    else {
				// do not add, otherwise, self-loop
			}
		   
	        
		    
		    youngPetri.coreG.removeVertex(succ);		
			ypm.cancelAssociateForPlace(succ);	
		
		
	}
	private static Place getChild(Place succ) {
		tmp.clear();
		Petri youngPetri = Petri.getPetri();		
		tmp.addAll(youngPetri.getLocalSuccs(succ));
		
        if(tmp.size()!=1) throw new RuntimeException();
		
		Place toret = null; 
		for(Place p: tmp)
		{
			toret = p;// yes, return the first one
		}
		return toret;

	}
	private static void eatParent(PetriMethod ypm, Place stone, Place prec) {
		 Petri youngPetri = Petri.getPetri();
	    // remove the prec node, it is useless now, check whether there are some other edges connecting to it.
		// TOFIX
	    if(!checkNoSideEffect(youngPetri,prec))
	    {
	    	throw new RuntimeException();// what is up
	    }
	    
		// like the removal of an element in the linked list
	    Place precOfprec = getParent(prec);
	    if(precOfprec!=stone)
	    {
	    	 ICFGLowLevelPetriBuilder.connectPlace2Place(youngPetri, precOfprec, stone);
	    }
	    else {
			// do nto add, otherwise, self-loop
		}
	   
	       
	    
	    youngPetri.coreG.removeVertex(prec);		
		ypm.cancelAssociateForPlace(prec);	
	}
	private static boolean checkNoSideEffect(Petri youngPetri, Place prec) {
		List precEdges =youngPetri.getAllPrecEdges(prec);
		for(Object o : precEdges)
		{
			
			if(!(o instanceof ArcLocal))
			{
				return false;// some thing wrong with the node to remove? it is not trivial?
			}
		}
		
		List succEdges =youngPetri.getAllSuccEdges(prec);
		for(Object o : succEdges)
		{
			
			if(!(o instanceof ArcLocal))
			{
				return false;// some thing wrong with the node to remove? it is not trivial?
			}
		}
		
		return true;
	}
	private static Place getParent(Place prec) {
		tmp.clear();
		Petri youngPetri = Petri.getPetri();
		tmp.addAll(youngPetri.getLocalPrecs(prec));
		
		if(tmp.size()!=1) throw new RuntimeException();
		
		Place toret = null; 
		for(Place p: tmp)
		{
			toret = p;// yes, return the first one
		}
		return toret;
		
	}
	static Set<Place> tmp = new HashSet<Place>();
	private static boolean singleParentSingleChild(Place prec) {
		// I use the set to avoid the duplication problem
		tmp.clear();
		Petri youngPetri = Petri.getPetri();
		tmp.addAll(youngPetri.getLocalPrecs(prec));
		boolean singleParent = (tmp.size() ==1);
		
		tmp.clear();
		tmp.addAll(youngPetri.getLocalSuccs(prec));
		boolean singleChild = (tmp.size()==1);
		
		return singleParent&&singleChild;
	}
	
	private static boolean singleParent(Place prec) {
		// I use the set to avoid the duplication problem
		tmp.clear();
		Petri youngPetri = Petri.getPetri();
		tmp.addAll(youngPetri.getLocalPrecs(prec));
		boolean singleParent = (tmp.size() ==1);
		
		return singleParent;
	}
	
	private static boolean singleChild(Place prec) {
		
		tmp.clear();
		Petri youngPetri = Petri.getPetri();
		tmp.addAll(youngPetri.getLocalSuccs(prec));
		boolean singleChild = (tmp.size()==1);
		
		return singleChild;
	}
//	static Set<Place> toremove = new HashSet<Place>();
	private static void specialPlaceStones(PetriMethod ypm, Set<Place> pSet) {
		placesInMethod.clear();
		Set<Place> allBoundaryStones = allBoundaryStones();
		Petri youngPetri = Petri.getPetri();		
		ICFGPetriReachable.placesInMethod(youngPetri, ypm.getEntry(), ypm.getExit(),placesInMethod);
		for(Place p : placesInMethod)
		{
			if(allBoundaryStones.contains(p))
				pSet.add(p);
		}		
	}
	
	private static Set<Place> allBoundaryStones = null;
	private static Set<Place> allBoundaryStones() {
		if(allBoundaryStones!=null)
			return allBoundaryStones;
		Set<Place> toret = new HashSet<Place>();
		Set<Violation> violations =	ViolationManager.getAllViolations();
		Set<PetriCriticalSection> placeCSs =  PetriCriticalSectionManager.getAllPetriCSs();
		for(Violation violation: violations)
		{
			toret.add(violation.getPplace());
			toret.add(violation.getcPlace());
			toret.add(violation.getrPlace());
		}
		
		for(PetriCriticalSection placeCS: placeCSs)
		{
			toret.addAll(placeCS.getCtredPlaces());
			toret.addAll(placeCS.getObsedPlaces());
		}
		allBoundaryStones = toret;		
		return toret;
	}
	
	private static void crossingRegionStones(PetriMethod ypm, Set<Place> ret) {	
		Petri youngPetri = Petri.getPetri();
		 Set<Arc> arcs =ICFGPetriReachable.ArcsInMethod(youngPetri, ypm.getEntry(), ypm.getExit());
		 for(Arc arc : arcs)
		 {
			 Place src = (Place) arc.getSource();
			 Place tgt = (Place)arc.getTarget();
			 
			 if(!HomoJudger.homo(src, tgt))// put them two in the ret set
			 {	
				 ret.add(src);
				 ret.add(tgt);
			 }
		 }	
	
	}

	
	
}
