//package hk.ust.lpxz.statemachine;
//
//import hk.ust.lpxz.petri.graph.RealPetriReachable;
//import hk.ust.lpxz.petri.graph.YoungPetri;
//import hk.ust.lpxz.petri.graph.criticalsection.PlaceCS;
//import hk.ust.lpxz.petri.graph.criticalsection.PlaceCSManager;
//import hk.ust.lpxz.petri.graph.criticalsection.ResourcePlaceManager;
//import hk.ust.lpxz.petri.unit.Arc;
//import hk.ust.lpxz.petri.unit.ArcFromResource;
//import hk.ust.lpxz.petri.unit.Place;
//import hk.ust.lpxz.petri.unit.PlaceResource;
//import hk.ust.lpxz.petri.unit.Transition;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.Stack;
//
//import soot.JastAddJ.ThisAccess;
//
//
//
//
//
//public class StateGraphBuilder_onlyDeadlock {
//
//
//	public static HashMap<State, State> uncontrollable = new HashMap<State, State>();
//	// structure decides
//	public static Set<State> exploreStateSpace(StateGraph sg , Set<State> unsafe) {
//		HashSet<State> visited = new HashSet<State>();
//		
//		// fill in entries
//		Set<Transition> entries = getEntryTransitions();// new ArrayList<Transition>();
//		Set<Place> critical =getCriticalSection();
//		
//		
//		
//		/* Depth-First Search to find all reachable states */
//		Stack<State> stack = new Stack<State>();
//		State initialState = new State();
//		ResourceState rState = new ResourceState(ResourcePlaceManager.getAllPR());
//		initialState.setResourceState(rState);
//		sg.setRootOrEnd(initialState);
//		
//		stack.push(initialState);
//		visited.add(initialState);
//		
//		while (!stack.isEmpty()) {
//			State s = stack.pop();		
//		//	System.out.println("\n" + s.toString());
//			Set<State> tmp = next(s,entries, critical, unsafe);	
//			// set the uncontrollable transitions between states already
//			
//			for (State sNext : tmp) {
//				/* add the transition from "s" to "t" */ // always add the edge no matter whether the child has been visited, this edge is not added
//			    if(!sg.coreG.containsVertex(s)) sg.coreG.addVertex(s);
//			    if(!sg.coreG.containsVertex(sNext)) sg.coreG.addVertex(sNext);
//			    if(sg.coreG.containsEdge(s, sNext)) throw new RuntimeException("invariant violated");			    	
//				sg.coreG.addEdge_edgetype_lpxz(s, sNext, StateTransition.class);
//				
//				//set the controllable attribute, info is stored in the map already
//				if(uncontrollable.get(s)==sNext)
//				{
//					sg.coreG.getEdge(s, sNext).setControllable(false); // setting it!
//				}
//				else {
//					sg.coreG.getEdge(s, sNext).setControllable(true); // setting it!
//				}
//			
//				//System.out.println("child: " + sNext.toString());
//				// continue the recursion!
//				if (!visited.contains(sNext)) {
//					
//					stack.push(sNext);
//					visited.add(sNext);
//					
//
//				}
//				
//			}
//		}
//		
//       
//		return visited;
//	}
//
//	
//	
//	public static Set<State> next(State s,Set<Transition> entries, 
//			Set<Place> critical, Set<State> unsafe) {
//		Set<State> result =	new HashSet<State>();
//		
//		Set<Place> containTokenPlaces = s.containTokenPlaces();// ok, s's places are immutatble, if change, the other state.
//		YoungPetri youngPetri = YoungPetri.getYoungPetri();
//		
//		
//		/* another thread enters a critical section */
//		for (Transition t : entries) {
//			// I purposely split the isT and trigger() for clear logic.
//			if(isTriggerable(t,s, critical)) // for places outof cs, we do not consider them and assume conservatively there are many tokens
//			{
//				// for place outof cs: we do not consider them in s. 
//				State  newState =trigger(t, s, critical);// remember update s and s.resource|| if out of cs, do not add the place!
//				if(unsafe!=null && !unsafe.contains(newState))
//				{
//					// help setting the controllable atrtibute:
//					if(!t.isGadara_controllable())
//					{
//						uncontrollable.put(s, newState);// newState is got via t, which is uncontrollable
//					}
//					result.add(newState);
//				}
//			}		
////			AbstractPlace[] newPlaces = cloneAdd(places, t.getTo());
////			if (unsafe == null || !unsafe.contains(new SetState(newPlaces)))
////				result.put(new SetState(newPlaces), (int[])newAvl);
//		}
//		
//		/* existing thread making progress */
//		for (Place containTokenPlace : containTokenPlaces) {
//			List transitions =youngPetri.getAllSuccs(containTokenPlace);
//			for(Object tObject : transitions)
//			{
//				Transition t = (Transition)tObject;
//				if(isTriggerable(t,s,critical)) // 
//				{
//					State  newState =trigger(t, s, critical);// remember update s and s.resource ||if out of cs, do not add the place!
//					if(unsafe!=null && !unsafe.contains(newState))
//					{
//						// help setting the controllable atrtibute:
//						if(!t.isGadara_controllable())
//						{
//							uncontrollable.put(s, newState);// newState is got via t, which is uncontrollable
//						}
//						result.add(newState);
//					}
//				}
//			}		
////			AbstractPlace[] newPlaces = cloneDel(places, p);
////			for (Transition t : p.outTrans()) {
////				Object newAvl = t.execute(resList, resAvl);
////				if (newAvl instanceof Resource) continue;
////				SetState newstate = new SetState(critical.contains(t.getTo()) ? 
////						cloneAdd(newPlaces, t.getTo()) : newPlaces);
////				if (unsafe == null || !unsafe.contains(newstate))
////					result.put(newstate, (int[])newAvl);
////			}
//		}
//		return result;
//	}
//	
//	
//
//
//    public static Set<PlaceResource> requiredByT = new HashSet<PlaceResource>();
//    public static Set<Place> requiredByT2 = new HashSet<Place>();
//     public static Set<Place> tmp4triggerable = new HashSet<Place>();
//	private static  boolean isTriggerable(Transition t, State s,Set<Place> critical) {
//		requiredByT.clear();
//		requiredByT2.clear();
//		tmp4triggerable.clear();
//		IncomingPRs(t,requiredByT);
//		IncomingNonPRs(t,requiredByT2);
//		
//		if(canProvideResources(s, requiredByT))
//		{
//			//return true; // most time, engouh
//			// I am recommended by an incoming place, if the t only has one incoming place
//			// that is it, no need to check
//			// if there are multiple incoming places, I need to check whether they are all present
//			// like the joining.
//			// acutally, in current impl, join is not explicitly modeled, no need to consider them at all
//			// for complete, I implement it.
//			if(requiredByT2.size()==0) throw new RuntimeException("impossible");
//			csFilter(requiredByT2, tmp4triggerable, critical);
//			if(s.containTokenPlaces().containsAll(tmp4triggerable))
//			{
//				return true;
//			}
//			else {
//				return false;
//			}
//			
////			if (requiredByT2.size()==1) {
////				return true; //==1, it is the recommending place, it may be the place outside the cs
////			}
////			else //if(requiredByT2.size()>=2) // 
////			{
////				
////			}
//			
//			
//		}
//		else {
//			return false;
//		}
//
//		
//	}
//
//	private static void csFilter(Set<Place> requiredByT22,
//			Set<Place> tmp4triggerable2 ,Set<Place> critical) {
//		for(Place place: requiredByT22)
//		{
//			if(critical.contains(place))
//			{
//				tmp4triggerable2.add(place);
//			}
//		}
//		
//	}
//
//
//
//	private static boolean canProvideResources(State s,
//			Set<PlaceResource> wants) {
//		
//        if(wants.size()==0)
//        	return true; // I do not need any reousrces
//        else {
//        	Set<PlaceResource> haves = s.getResourceState().getAvailablePlaces();
//			if(haves.containsAll(wants))
//			{
//				return true;
//			}
//			else {
//				return false;
//			}
//		}	
//	}
//
//
//    // the semantic : t's incoming places lose one token
//	// t's outgoing places get one token for each.
//	// We only consider the inside places of the critiical section!
//	static Set<Place> safetySet = new HashSet<Place>(); 
//	private static State trigger(Transition t, State s, Set<Place> critical) {
//		// create the next State: sNext.// update its state and resource state!
//		// note not to add the nodes outside the critical section!
//		
//		State sNext = s.deepClone();
//		// operate on sNext directly
//		ResourceState sNextRS = sNext.getResourceState();
//		YoungPetri youngPetri = YoungPetri.getYoungPetri();
//		List<Place> precs =youngPetri.getAllPrecs(t); // may contain the placeResource
//		List<Place> succs = youngPetri.getAllSuccs(t); // may contain some places out of the cs
//		// may also contain the PR place!
//		
//		// remove precs
//		safetySet.clear();
//		safetySet.addAll(precs);
//		for(Place prec:safetySet)// generally, only one prec, one succ, special case: start/join
//		{
//			if(!(prec instanceof PlaceResource))
//			{
//				if(!critical.contains(prec))// originally, due to the soot bug, some normal stmt are missed, the logic flow is broken then
//				{
//					// we think many tokens are tehre in the outer places.
//					// autofill, (autoignore later)
//				}
//				else {
//					boolean succ = sNext.removePlace(prec); 
//					if(!succ)
//						throw new RuntimeException("invariant violated");
//				}
//
//			}
//			else {
//				sNextRS.placeDisabled((PlaceResource)prec);
//			}		
//		}
//		// add succs
//		safetySet.clear();
//		safetySet.addAll(succs);
//		for(Place succ:safetySet)
//		{
//			if(!(succ instanceof PlaceResource))
//			{
//				if(!critical.contains(succ))
//				{
//					// autoignore
//				}		
//				else {
//					sNext.addPlace_success(succ); 
//				}
//				
//			}
//			else {
//				sNextRS.placeEnabled((PlaceResource)succ);
//			}
//		}
//		return sNext;
//	}
//
//
//	// essentially simulate the thread.starting
//	// we start with the outermost CS!
//	static public Set<Place> tmp4EntryTrans = new HashSet<Place>();
//	private static Set<Transition> getEntryTransitions() {
//		tmp4EntryTrans.clear();
//		// to get the outermost CS!
//		Set<PlaceCS> outerCSs =PlaceCSManager.getOutermostCSs();
//		Set<Transition> trans = new HashSet<Transition>();
//		
//		for(PlaceCS placeCS: outerCSs)
//		{
//			tmp4EntryTrans.addAll(placeCS.getCtredPlaces());
//		}
//		
//		YoungPetri youngPetri = YoungPetri.getYoungPetri();
//		for(Place place : tmp4EntryTrans)
//		{
//			List precTrans = youngPetri.getAllPrecs(place);
//			for(Object precT : precTrans)
//			{
//				Transition tt = (Transition)precT;
//				if(!hasPRArcIncoming(tt))
//				{
//					throw new RuntimeException(" not reasonable, it is the controlling transition, right?");
//				}
//				trans.add(tt);// it must be a transitin
//			}
//		}
//		return trans;
//	}
//	
//	private static boolean hasPRArcIncoming(Transition tt) {
//		YoungPetri youngPetri = YoungPetri.getYoungPetri();
//		List arcs =youngPetri.getAllPrecEdges(tt);
//		boolean existPREdge= false;
//		for(Object o : arcs)
//		{
//			if(o instanceof ArcFromResource)
//			{
//				existPREdge =true;
//				return existPREdge;
//			}
//		}		
//		return existPREdge;
//	}
//	
//	
//	private static void IncomingPRs(Transition tt,  Set<PlaceResource> rets) {
//		
//		YoungPetri youngPetri = YoungPetri.getYoungPetri();
//		List arcs =youngPetri.getAllPrecEdges(tt);
//		
//		for(Object o : arcs)
//		{
//			if(o instanceof ArcFromResource)
//			{
//				ArcFromResource afr = (ArcFromResource)o;
//				Object pr =afr.getSource();
//			    if(pr instanceof PlaceResource)
//			    {
//			    	rets.add((PlaceResource)pr);
//			    }
//			    else {
//					throw new RuntimeException("it is hte source of the arcFromResource, right?");
//				}
//			}
//		}		
//		
//	}
//	
//private static void IncomingNonPRs(Transition tt,  Set<Place> rets) {
//		
//		YoungPetri youngPetri = YoungPetri.getYoungPetri();
//		List arcs =youngPetri.getAllPrecEdges(tt);
//		
//		for(Object o : arcs)
//		{
//			if(!(o instanceof ArcFromResource))
//			{
//				Arc afr = (Arc)o;
//				Object pr =afr.getSource();
//			   // if(pr instanceof PlaceResource)
//			    {
//			    	rets.add((Place)pr);
//			    }
//
//			}
//		}		
//		
//	}
//
//	private static Set<Place> getCriticalSection() {
//		Set<Place> critical = new HashSet<Place>();
//		Set<PlaceCS> placeCSs =  PlaceCSManager.getAllPlaceCSs();
//		for(PlaceCS pcs : placeCSs)
//		{
//			pcs.includedPlaces(critical);
//		}
//		
//		YoungPetri youngPetri = YoungPetri.getYoungPetri();
//		Set<Place> ret = new HashSet<Place>();// be used always, worthy to not use cache.
//		RealPetriReachable.all_reachablePlaces_noPassing_PRs_domain(youngPetri, critical, ret);
//		
//		
////        for(Object o : ret)
////        {
////        	System.out.println(o);
////        }
//		
//		
//		
//		
//		////return critical;
//		return ret;
//	
//	}
//
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//	}
//	
//	//============supcon, uncon: 
////	public static int supcon(StateGraph sg ) {
////		return supcon1(sg).size();
////	}
//
//	/**
//	 * Iterative procedure to calculate controllable non-blocking safe region
//	 * 
//	 * @return unsafe states, i.e., states that may reach deadlocked states unavoidably
//	 */
//	static Set<State> newUnsafe_notReach = new HashSet<State>();
//	static Set<State> newUnsafe_unevitable = new HashSet<State>(); 
//	public  static Set<State> unsafe(StateGraph sg) {
//		
//		Set<State> unsafe = new HashSet<State>();// you can also set some initial unsafe
//		int i = 0;
//		
//		while (true) {
//			// as unsafe is increasing, the traversal on the graph will meet more enemies, and get less visited node
//			// i.e., more nodes are turning to unsafe, you can think the unsafe breaking certain bridges conceptully.
//			
//			newUnsafe_notReach.clear();
//			newUnsafe_blockbyUsafe(unsafe, sg, newUnsafe_notReach);
//			unsafe.addAll(newUnsafe_notReach);
//			
//			if (newUnsafe_notReach.isEmpty()) break;			 
//			
//			newUnsafe_unevitable.clear();
//			newUnsafe_unevitable(newUnsafe_notReach, sg, newUnsafe_unevitable);
//			unsafe.addAll(newUnsafe_unevitable);// not contain the newUnsafe_blockByUsafe states, make things clear
//			System.out.println("# DEBUG Supcon iteration: " + ++i + ", unsafe states " + unsafe.size());
//		}
//		return unsafe;
//	}
//
//// more general, you can enter the initial unsafe!
//   
//
//	
//	
//	/**
//	 * Backtracking from the initial (start) state, states not reached (blocking) are returned
//	 * 
//	 * @param unsafe set of unsafe states that should be avoided during the backtracking
//	 * @return the set of (unsafe) states not reached, excluding "unsafe"
//	 */
//	static Set<State> backSafeStates = new HashSet<State>();
//	static Set<State> safeStates = new HashSet<State>();
//	protected static void newUnsafe_blockbyUsafe(Set<State> unsafe, StateGraph sg, Set<State> newUnsafe) {
//		backSafeStates.clear();
//		safeStates.clear();
//
//		// start from empty
//		State initial = sg.getRootOrEnd();
//		
//		// unsafe take effects here, it blocks the traversal when meeting a unsafe
//		StateReachable.backreachableSafe(sg, initial, unsafe, backSafeStates);// unsafe children, do not consider them
//	    StateReachable.backreachableSafe(sg, initial, unsafe, safeStates);// unsafe children, do not consider them
//		
//		Set<State> allStates = sg.allStates();		
//		// originally safe, now not erachable, either back or forward
//		for (State s : allStates) 
//			if (!unsafe.contains(s) && (!safeStates.contains(s) || !backSafeStates.contains(s))) 
//				newUnsafe.add(s);
//		
//	}
//	
//	/**
//	 * Calculate states that can reach one of the given set of unsafe states unavoidably
//	 * through a sequence of uncontrollable transitions
//	 * 
//	 * @param tmp 
//	 * @return a set of states that reach "unsafe" unavoidably, including "unsafe"
//	 */
//	protected static Set<State> newUnsafe_unevitable(Set<State> tmp, StateGraph sg,  Set<State> newUnsafe_unevitable) {
//         StateReachable.backreachableAlongUncon(sg, tmp, newUnsafe_unevitable);        
//        // you want the new ones, i.e., the delta?
//        newUnsafe_unevitable.removeAll(tmp);		
//		return newUnsafe_unevitable;
//	}
//
//
//}
