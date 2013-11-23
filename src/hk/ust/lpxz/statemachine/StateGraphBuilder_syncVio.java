package hk.ust.lpxz.statemachine;

import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.PetriReachable;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.graph.PetriVisualizer;
import hk.ust.lpxz.petri.graph.GadaraSupport.GadaraNamer;
import hk.ust.lpxz.petri.graph.criticalsection.PetriCriticalSection;
import hk.ust.lpxz.petri.graph.criticalsection.PetriCriticalSectionManager;
import hk.ust.lpxz.petri.graph.criticalsection.PlaceCSModeller;
import hk.ust.lpxz.petri.graph.criticalsection.ResourcePlaceManager;
import hk.ust.lpxz.petri.graph.violation.Violation;
import hk.ust.lpxz.petri.graph.violation.ViolationManager;
import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.ArcFromResource;
import hk.ust.lpxz.petri.unit.ArcLocal;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceResource;
import hk.ust.lpxz.petri.unit.Transition;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;




import soot.JastAddJ.ThisAccess;
import soot.toolkits.scalar.Pair;





public class StateGraphBuilder_syncVio {


	
	

	public static HashMap<State, State> uncontrollable = new HashMap<State, State>();
	public static Set<State> next(State s,Set<Transition> entries, 
			Set<Place> scope) { 
		Set<State> result =	new HashSet<State>();		
		Set<Place> containTokenPlaces = s.containTokenPlaces();
		Petri youngPetri = Petri.getPetri();		
		/* another thread enters a critical section */
		for (Transition t : entries) {
			if(isTriggerable(t,s, scope)) 
			{
				State  newState =trigger(t, s, scope);// update s and s' resource.
				if(!t.isGadara_controllable()) uncontrollable.put(s, newState);
				if(newState.twoTokenPlaces.isEmpty())// we ignore racing case unless specified explicitly by human.
					result.add(newState);
			}		
		}		
		/* existing thread making progress */
		for (Place containTokenPlace : containTokenPlaces) {
			List transitions =youngPetri.getAllSuccs(containTokenPlace);
			for(Object tObject : transitions)
			{
				Transition t = (Transition)tObject;
				if(isTriggerable(t,s,scope)) 
				{
					State  newState =trigger(t, s, scope);
					if(!t.isGadara_controllable())  uncontrollable.put(s, newState);
					if(newState.twoTokenPlaces.isEmpty())
						result.add(newState);
				}
			}	
		}
		return result;
	}
	
	


    public static Set<PlaceResource> inputResourcePlaces = new HashSet<PlaceResource>();
    public static Set<Place> inputNormalPlaces = new HashSet<Place>();
  	private static  boolean isTriggerable(Transition t, State s,Set<Place> scope) {
		inputResourcePlaces.clear();
		inputNormalPlaces.clear();		
		//for places out of scope, we assume they contain infinite tokens conservatively.		
		inputPlaces(t, scope , inputResourcePlaces, inputNormalPlaces);	//inputNormalPlaces may be empty. 
		boolean canProvideResource = s.canProvideResources(inputResourcePlaces)|| PetriCriticalSectionManager.isReentrantLockTran(t); // no need for resource.
		if(canProvideResource)
		{			
 			if(s.containTokenPlaces().containsAll(inputNormalPlaces))// cover them, 1token is enough.
			{
				return true;
			}
			else {
				return false;
			}			
		}
		else {
			return false;
		}	
	}

	private static void inputPlaces(Transition t,
			Set<Place> scope, Set<PlaceResource> inputResourcePlaces2,
			Set<Place> inputNormalPlaces2) {	
		Petri youngPetri = Petri.getPetri();
		List arcs =youngPetri.getAllPrecEdges(t);		
		for(Object o : arcs)
		{
			if(o instanceof ArcFromResource)
			{
				ArcFromResource afr = (ArcFromResource)o;
				Object pr =afr.getSource();
				inputResourcePlaces2.add((PlaceResource)pr);
			}else if(o instanceof ArcLocal){
				Arc afr = (Arc)o;
				Object pr =afr.getSource();
				if(scope.contains(pr)) // in the scope of interest.
				   inputNormalPlaces2.add((Place)pr);
			}
			else {
				//arccall, ignore it.
				//throw new RuntimeException("what type is it?" + o.getClass());
			}
		}		
	}

	



	


    // Semantic : 
	// t's incoming places lose one token
	// t's outgoing places get one token for each.
	// We only consider the places inside the scope of interest.
	static Set<Place> aSimpleSet = new HashSet<Place>(); // do not want the list, may have repeating
	private static State trigger(Transition t, State s, Set<Place> scope) {
		State sNext = s.deepClone();

		Petri youngPetri = Petri.getPetri();
		List<Place> precs =youngPetri.getAllPrecs(t); // may contain the placeResource
		List<Place> succs = youngPetri.getAllSuccs(t); // may contain some places out of the cs
			
		// precs remove a token
		aSimpleSet.clear();
		aSimpleSet.addAll(precs);
		for(Place prec:aSimpleSet)// generally, only one prec, one succ
		{
			
			if((prec instanceof PlaceResource)) 
			{
				if(!PetriCriticalSectionManager.isReentrantLockTran(t))
				    sNext.acqResourcePlace((PlaceResource)prec);
			}
			else {		
				if(scope.contains(prec)){
					boolean ok = sNext.removePlace(prec); 
					if(!ok){
						// it is ok, because the prec is just a method exit, which connects to here by arcreturn.
						// ignore it.
						//throw new RuntimeException("how can you pass triggerable checking?" +   prec.getClass());
					}
				}	
			}		
		}
		// succs add a token
		aSimpleSet.clear();
		aSimpleSet.addAll(succs);
		for(Place succ:aSimpleSet)
		{
			if((succ instanceof PlaceResource) ) 
			{
				if(! PetriCriticalSectionManager.isReentrantUnlockTran(t))
				  sNext.relResourcePlace((PlaceResource)succ);
			}
			else {			
				if(scope.contains(succ))
				  sNext.addPlace_success(succ); 
			}
		}
		return sNext;
	}


	// essentially simulate the thread.starting
	// we start with the outermost CS!	
	public static Set<Transition> getGeneralEntryTransitions() {
		Set<Place> tmpSet = new HashSet<Place>();
		//the outermost CS!
		Set<PetriCriticalSection> outerCSs =PetriCriticalSectionManager.getOuterPetriCSs();		
		for(PetriCriticalSection placeCS: outerCSs)
		{
			tmpSet.addAll(placeCS.getCtredPlaces());
		}
		
		Petri youngPetri = Petri.getPetri();
		Set<Transition> toret = new HashSet<Transition>();
		for(Place place : tmpSet)
		{
			List precTrans = youngPetri.getAllPrecs(place);
			for(Object precT : precTrans)
			{
				Transition tt = (Transition)precT;
				if(hasPRArcIncoming(tt))
				{
					toret.add(tt);
				}
			
			}
			if(toret.size()==0)
				throw new RuntimeException("is it a controlled place?" + GadaraNamer.getGadaraName(place));
		}
		
		
		//===============violations:
		if(!DconPropertyManager.onlyDA)
		{
			tmpSet.clear();
			Set<Violation> violations =ViolationManager.getAllViolations();
			for(Violation vio : violations)
			{
				Place candidate1 =vio.getPplace();
				Place candidate2 = vio.getrPlace();
				tmpSet.add(candidate1); //no matter it is included in outerCS.
				tmpSet.add(candidate2);					
			}	
		}
		
		for(Place place : tmpSet)
		{
			List precTrans = youngPetri.getAllPrecs(place);
			for(Object precT : precTrans)
			{
				Transition tt = (Transition)precT;
				toret.add(tt);
			}
		}
		return toret;
	}
	
	
	public static boolean isContainedBy(Place place ,Set<PetriCriticalSection> outerCSs) {
		boolean isContainedBy = false;
		for(PetriCriticalSection placeCS : outerCSs)
		{
			if(isContainedBy(place, placeCS))
			{
				isContainedBy = true; // by this one
			}
		}		
		return isContainedBy;
	}
	static Set<Place> includedPlaces = new HashSet<Place>();
	static Set reachables4ICB= new HashSet();
	private static boolean isContainedBy(Place place, PetriCriticalSection placeCS) {
		includedPlaces.clear();
		reachables4ICB.clear();
		placeCS.includedPlaces(includedPlaces);
		Petri youngPetri = Petri.getPetri();
		
		 PetriReachable.all_reachablePlaces_noPassing_PRs_domain(youngPetri, includedPlaces, reachables4ICB);
		if(reachables4ICB.contains(place))
		{
			return true;
		}
		else {
			return false;
		}
		
		
	}



	private static boolean hasPRArcIncoming(Transition tt) {
		Petri youngPetri = Petri.getPetri();
		List arcs =youngPetri.getAllPrecEdges(tt);
		boolean existPREdge= false;
		for(Object o : arcs)
		{
			if(o instanceof ArcFromResource)
			{
				existPREdge =true;
				return existPREdge;
			}
		}		
		return existPREdge;
	}
	
	
	private static void IncomingPRs(Transition tt,  Set<PlaceResource> rets) {
		
		Petri youngPetri = Petri.getPetri();
		List arcs =youngPetri.getAllPrecEdges(tt);
		
		for(Object o : arcs)
		{
			if(o instanceof ArcFromResource)
			{
				ArcFromResource afr = (ArcFromResource)o;
				Object pr =afr.getSource();
			    if(pr instanceof PlaceResource)
			    {
			    	rets.add((PlaceResource)pr);
			    }
			    else {
					throw new RuntimeException("it is hte source of the arcFromResource, right?");
				}
			}
		}		
		
	}
	
private static void IncomingNonPRs(Transition tt,  Set<Place> rets) {
		
		Petri youngPetri = Petri.getPetri();
		List arcs =youngPetri.getAllPrecEdges(tt);
		
		for(Object o : arcs)
		{
			if(!(o instanceof ArcFromResource))
			{
				Arc afr = (Arc)o;
				Object pr =afr.getSource();
			   // if(pr instanceof PlaceResource)
			    {
			    	rets.add((Place)pr);
			    }

			}
		}		
		
	}

    // scope for interesting places
	public static Set<Place> getScope() {
		Set<Place> intraScope = new HashSet<Place>(); 
		Set<PetriCriticalSection> placeCSs =  PetriCriticalSectionManager.getAllPetriCSs();// you can also use Outermost ones, the same
		for(PetriCriticalSection pcs : placeCSs)
		{
			pcs.includedPlaces(intraScope);
		}
		if(!DconPropertyManager.onlyDA)
		{
		Set<Violation> violations = ViolationManager.getAllViolations();
		for(Violation vio :violations)
		{
			vio.includedPlaces(intraScope);
		}
		}
		
		Petri youngPetri = Petri.getPetri();
		Set<Place> interScope = new HashSet<Place>();
		PetriReachable.all_reachablePlaces_noPassing_PRs_domain(youngPetri, intraScope, interScope);
		return interScope;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	//============supcon, uncon: 
//	public static int supcon(StateGraph sg ) {
//		return supcon1(sg).size();
//	}

	/**
	 * Iterative procedure to calculate controllable non-blocking safe region
	 * 
	 * @return unsafe states, i.e., states that may reach deadlocked states unavoidably
	 */
	
	static Set<State> newUnsafe_notReach = new HashSet<State>();
	static Set<State> newUnsafe_unevitable = new HashSet<State>(); 
	public  static Set<State> deadlockUnsafe(StateGraph sg,Set<State> unsafeVios_auto_reachable) {
		

		int i = 0;		
		
	
	    Set<State> currentKnownUnsafe = new HashSet<State>();
	    currentKnownUnsafe.addAll(unsafeVios_auto_reachable);// unsafeVios_auto_reachable is not touched
	    
	    Set<State> newFoundUnsafeInTotal = new HashSet<State>();
	    
		while (true) {
			// as unsafe is increasing, the traversal on the graph will meet more enemies, and get less visited node
			// i.e., more nodes are turning to unsafe, you can think the unsafe breaking certain bridges conceptully.
			
			newUnsafe_notReach.clear();
			newUnsafe_blockbyUsafe(currentKnownUnsafe, sg, newUnsafe_notReach);// increasing unsafes-> more unsafes.
			if (newUnsafe_notReach.isEmpty()) break;
//			System.out.println(" new reach unsafe " + newUnsafe_notReach.size());
			
			
			currentKnownUnsafe.addAll(newUnsafe_notReach); //increasing unsafes
			newFoundUnsafeInTotal.addAll(newUnsafe_notReach);// toret
				 
			
			newUnsafe_unevitable.clear();
			newUnsafe_unevitable(newUnsafe_notReach, sg, newUnsafe_unevitable);
		
			currentKnownUnsafe.addAll(newUnsafe_unevitable);// increasing unsafes
			newFoundUnsafeInTotal.addAll(newUnsafe_unevitable);// toret
		}
		

		return newFoundUnsafeInTotal;//toret
	}


    
    public static Set<State> knownUnsafe() {
    	Set<State> toret= new HashSet<State>();
    	 Set<Violation> violations= ViolationManager.getAllViolations();
 	    for(Violation vio : violations)
 	    {
 	    	tmp3conVio.clear();
 	    	vio.includedPlaces_betweenPC(tmp3conVio);
 	    	Petri youngPetri = Petri.getPetri();
 	    	tmpVarInContainVio.clear();
 			PetriReachable.all_reachablePlaces_noPassing_PRs_domain(youngPetri, tmp3conVio, tmpVarInContainVio);// complete, may need to reduce the YPG inter-procedurally
 			
 	    	Place rPlace = vio.getrPlace();
 	    	for(Place place : tmpVarInContainVio)
 	    	{
 	    		State vioState = new State();
 	    		vioState.addPlace_success(place);
 	    		vioState.addPlace_success(rPlace);
 	    		toret.add(vioState); 	    		
 	    	}
	    }	
 	    return toret;
	}





	
	//@ This one makes use of the synchonrization as the ar.
    // specific domain knowledge.
    // so, the only violation, i.e., cutting line is between r and some node between pc
	static Set<Place> tmp3conVio = new HashSet<Place>();
	static Set<Place> tmpVioPair = new HashSet<Place>();  
	static Set<Place> tmpVarInContainVio = new HashSet<Place>();// be used always, worthy to not use cache.
    



	/**
	 * Backtracking from the initial (start) state, states not reached (blocking) are returned
	 * 
	 * @param unsafe set of unsafe states that should be avoided during the backtracking
	 * @return the set of (unsafe) states not reached, excluding "unsafe"
	 */
	static Set<State> backSafeStates = new HashSet<State>();
	static Set<State> safeStates = new HashSet<State>();
	protected static void newUnsafe_blockbyUsafe(Set<State> unsafe, StateGraph sg, Set<State> newUnsafe) {
		backSafeStates.clear();
		safeStates.clear();

		// start from empty
		State initial = sg.getRootOrEnd();
		
		// unsafe take effects here, it blocks the traversal when meeting a unsafe
		StateReachable.reachableSafe(sg, initial, unsafe, safeStates);// unsafe children, do not consider them
	    StateReachable.backreachableSafe(sg, initial, unsafe,backSafeStates );// unsafe children, do not consider them
	    
		Set<State> allStates = sg.allStates();		
		// originally safe, now not erachable, either back or forward
		for (State s : allStates) 
			if (!unsafe.contains(s) && (!safeStates.contains(s) || !backSafeStates.contains(s))) // new unsafe, not included by old unsafes
				newUnsafe.add(s);
		
	}
	
	/**
	 * Calculate states that can reach one of the given set of unsafe states unavoidably
	 * through a sequence of uncontrollable transitions
	 * 
	 * @param tmp 
	 * @return a set of states that reach "unsafe" unavoidably, including "unsafe"
	 */
	protected static Set<State> newUnsafe_unevitable(Set<State> tmp, StateGraph sg,  Set<State> newUnsafe_unevitable) {
         StateReachable.backreachableAlongUncon(sg, tmp, newUnsafe_unevitable);        
        // you want the new ones, i.e., the delta?
        newUnsafe_unevitable.removeAll(tmp);		
		return newUnsafe_unevitable;
	}

	// structure decides
	public static Set<State> exploreStateSpace(StateGraph sg , Set<State> initialUnsafe ) {
		HashSet<State> visited = new HashSet<State>();
		HashSet<State> exploredUnsafe = new HashSet<State>();// empty
		
		Set<Transition> entries = getGeneralEntryTransitions();//starting transitions
		Set<Place> scope =getScope();// limiting scope.	
		
		/* Depth-First Search to find all reachable states */
		Stack<State> stack = new Stack<State>();
		State initialState = new State();
		ResourceState rState = new ResourceState(ResourcePlaceManager.getAllPR());
		initialState.setResourceState(rState);
		sg.setRoot(initialState);
		
		// begin DFS:
		stack.push(initialState);
		visited.add(initialState);		
		while (!stack.isEmpty()) {
			State s = stack.pop();					
			Set<State> tmp =null;
			//if(DconPropertyManager.boundaryOpt) // mandatory optimization.			
			if(!s.twoTokenPlaces.isEmpty())  throw new RuntimeException("we ignore 200 marking");
			if(StateReachable.more_equal_Unsafe(s, initialUnsafe)) continue; //ceasing exploration when unsafer. //lpxz: important, scalability
			tmp =next(s,entries, scope);		
						
			for (State sNext : tmp) {
				if(StateReachable.more_equal_Unsafe(sNext, initialUnsafe)) continue; //ceasing exploration when unsafer. //lpxz, scalability
				//System.out.println(sNext);
				if(!sg.coreG.containsVertex(s)) sg.coreG.addVertex(s);
			    if(!sg.coreG.containsVertex(sNext)) sg.coreG.addVertex(sNext);// maybe bad, we need it, the boundary bad.
			    if(sg.coreG.containsEdge(s, sNext)) continue; 	    	
				sg.coreG.addEdge_edgetype_lpxz(s, sNext, StateTransition.class);
				setUncontrollable(sg, s, sNext);				
				debug(s, sNext);
				if (!visited.contains(sNext)) {					
					stack.push(sNext);
					visited.add(sNext);
				}				
			}
		}	
		return exploredUnsafe;
	}

	private static void debug(State s, State sNext) {
		if(DconPropertyManager.debugEnumerateMarkings)
	    {
	    	System.out.println(s.toString() + " ->  " + sNext.toString());
	    }
	}

	private static void setUncontrollable(StateGraph sg, State s, State sNext) {
		//set the controllable attribute, info is stored in the map already
		if(uncontrollable.get(s)==sNext)
		{
			sg.coreG.getEdge(s, sNext).setControllable(false); // setting it!
		}
		else {
			sg.coreG.getEdge(s, sNext).setControllable(true); // setting it!
		}
	}




	static Set<State> safe_withFinestlock = new HashSet<State>();
	static Set<State> unsafe_withFinestlock = new HashSet<State>();
	static Set<State> safe_withCoarselock = new HashSet<State>();
	static Set<State> unsafe_withCoarselock = new HashSet<State>();
	
	public static void enumerateMarkings(
			Set<State> safe, Set<State> unsafe) {
		// create your own sg, and return your own states/unsafe
		safe_withFinestlock.clear();
		unsafe_withFinestlock.clear();
		safe_withCoarselock.clear();
		unsafe_withCoarselock.clear();

		if(DconPropertyManager.twoPhaseEnumeration)
		{
			// switch to finest lock
			PlaceCSModeller.switchToFinestLocks();
			
			enumerateMarkingsOnce( safe_withFinestlock, unsafe_withFinestlock);
			
			PlaceCSModeller.recoverToOrigLocks();
				
			PetriCriticalSectionManager.detectReentrantLockTransitions();// auto enabling the reentrant mechanism
			
			enumerateMarkingsOnce( safe_withCoarselock, unsafe_withCoarselock);	
			//assertContain(safe_withFinestlock, safe_withCoarselock);		

			safe.addAll(minus(safe_withFinestlock, unsafe_withCoarselock));
			unsafe.addAll(add(unsafe_withFinestlock,unsafe_withCoarselock)); 
		}
		else {
			PetriCriticalSectionManager.detectReentrantLockTransitions();// auto enabling the reentrant mechanism
			enumerateMarkingsOnce( safe_withCoarselock, unsafe_withCoarselock);	// 002 is also unsafe! stop continuing.
			safe.addAll(safe_withCoarselock);			
			unsafe.addAll(unsafe_withCoarselock);			
		}	
		safe_withFinestlock.clear();
		unsafe_withFinestlock.clear();
		safe_withCoarselock.clear();
		unsafe_withCoarselock.clear();
		
	}





	private static void assertContain(Set<State> safe_withFinestlockPara,
			Set<State> safe_withCoarselockPara) {
	      for(State coarse : safe_withCoarselockPara)
	      {
	    	  if(!safe_withFinestlockPara.contains(coarse))
	    	  {
	    		  System.out.println("you do not have:" + coarse);
	    		  
	    	//	  throw new RuntimeException("you should contain");
	    	  }
	      }
		
		
	}

	private static void enumerateMarkingsOnce( 
			Set<State> goodstates, Set<State> badstates) {
		StateGraph sg = new StateGraph();			
		Set<State> knownUnsafe = knownUnsafe();		
		exploreStateSpace(sg, knownUnsafe);        
		Set<State> deadlockUnsafe= deadlockUnsafe(sg, knownUnsafe);  
		
		badstates.addAll(knownUnsafe); 
        badstates.addAll(deadlockUnsafe);              	   
	    goodstates.addAll(minus(sg.allStates(), badstates));    
	    
	    sg=null; 
	    System.gc();
	    
//	    sg.coreG.vertexSet().clear();// unmodified.
//	    sg.coreG.edgeSet().clear();
//	    System.gc();
	    
	}

	static Set<State> tmp4Add = new HashSet<State>();
	private static Set<State> add(Set<State> allStates, Set<State> unsafe) {
		// TODO Auto-generated method stub
		tmp4Add.clear();
		tmp4Add.addAll(allStates);
		tmp4Add.addAll(unsafe);
		return tmp4Add;
	}




	static Set<State> tmp4Minus = new HashSet<State>();
	private static Set<State> minus(Set<State> allStates, Set<State> unsafe) {
		tmp4Minus.clear();
		
		for(State s : allStates)
		{
			if(!unsafe.contains(s))
			{
				tmp4Minus.add(s);
			}
		}
		
		return tmp4Minus;
	}

	private static boolean isEqual(Set<State> states, Set<State> sgStates) {
	    for(State s : states)
	    {
	    	if(!sgStates.contains(s))
	    		return false;
	    }
	    
	    for(State s : sgStates)
	    {
	    	if(!states.contains(s))
	    		return false;
	    }
		
	    
	    return true;
	}


}
