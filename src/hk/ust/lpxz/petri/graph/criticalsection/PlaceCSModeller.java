package hk.ust.lpxz.petri.graph.criticalsection;

import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.graph.ICFGPetriBuilder;
import hk.ust.lpxz.petri.graph.ICFGLowLevelPetriBuilder;
import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.graph.PetriMethodManager;
import hk.ust.lpxz.petri.graph.ICFGPetriReachable;
import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.ArcCall;
import hk.ust.lpxz.petri.unit.ArcFromResource;
import hk.ust.lpxz.petri.unit.ArcLocal;
import hk.ust.lpxz.petri.unit.ArcToResource;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
import hk.ust.lpxz.petri.unit.PlaceResource;
import hk.ust.lpxz.petri.unit.Transition;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.stringtemplate.v4.compiler.CodeGenerator.includeExpr_return;



import pldi.locking.CriticalSection;
import soot.ArrayType;
import soot.Body;
import soot.PatchingChain;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.MonitorStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.Pair;

public class PlaceCSModeller {

	static Set<Place> placesInMethod = new HashSet<Place>();
	public static void modelPlaceCSs(PetriMethod ypm,
			Petri youngPetri) {
		List<CriticalSection> csList =PetriCriticalSectionManager.getCriticalSections(ypm.getBb());
		placesInMethod.clear();
		ICFGPetriReachable.placesInMethod(youngPetri,  ypm.getEntry(), ypm.getExit(),placesInMethod); 
		
		for(CriticalSection cs : csList)
		{
			PetriCriticalSection petriCS =cs2PetriCS(cs , ypm, youngPetri, placesInMethod);
			addResourcePlace2Petri(ypm, youngPetri, petriCS);
			PetriCriticalSectionManager.registerPetriCS( petriCS);	
		}	
		
		if(ypm.getBb().getMethod().isSynchronized())
		{
			PetriCriticalSection placeCS =syncM2PlaceCS( ypm, youngPetri, placesInMethod);		
			if(placeCS==null) return; // it did not pass the tailchecking (no tails).
			addResourcePlace2Petri(ypm, youngPetri, placeCS);
			PetriCriticalSectionManager.registerPetriCS( placeCS);	
		}		
	}

	static Set<Place> toRemove = new HashSet<Place>();
	private static void onePassRemovePlace4Monitor(PetriMethod ypm,
			Petri youngPetri,Set<Place> pSet ) {
		toRemove.clear();
		for(Place p: pSet)
		{
			if(p instanceof PlaceCommonLocal)
			{
				PlaceCommonLocal tmp = (PlaceCommonLocal )p;
				if(tmp.getJimpleStmt() instanceof MonitorStmt)
				{
					toRemove.add(tmp);
				}
			}
		}
		
		for(Place p : toRemove)
		{
			List precs = youngPetri.getLocalPrecs(p);
			List succs  =youngPetri.getLocalSuccs(p);
			
			for(Object  o1 : precs)
			{
				Place pre = (Place) o1;
				for(Object o2 : succs)
				{
					Place succ = (Place) o2;
					ICFGLowLevelPetriBuilder.connectPlace2Place(youngPetri, pre, succ);
				}
			}
			youngPetri.coreG.removeVertex(p);
			
			ypm.cancelAssociateForPlace(p);
		}	
	}
	private static void addResourcePlace2Petri(PetriMethod ypm,
			Petri youngPetri, PetriCriticalSection placeCS) {// no transitions yet.
		PlaceResource resource = placeCS.getresPlace();
		youngPetri.coreG.addVertex(resource);
		
		Set<Place> ctredPlaces =placeCS.getCtredPlaces();
		Set<Place> obsedPlaces =placeCS.getObsedPlaces();
		
		for(Place ctred : ctredPlaces)
		{
			if(!youngPetri.coreG.containsEdge(resource, ctred))// single-edge between two nodes!, due to the normal head and the exceptional head, exitmethod->exitThread may be added twice.
			{
				youngPetri.coreG.addEdge_edgetype_lpxz(resource, ctred, ArcFromResource.class);
			}	
		}
		
		for(Place obsed : obsedPlaces)
		{
			if(!youngPetri.coreG.containsEdge(obsed, resource))// 
			{
				youngPetri.coreG.addEdge_edgetype_lpxz(obsed, resource, ArcToResource.class);	
			}
			
		}
	}
	
	public static void removeConnectWithResourcePlace(PetriCriticalSection placeCS) {
				
		PlaceResource resource = placeCS.getresPlace();
		Petri youngPetri = Petri.getPetri();
		
		Set<Place> ctredPlaces =placeCS.getCtredPlaces();
		Set<Place> obsedPlaces =placeCS.getObsedPlaces();
		
		for(Place ctred : ctredPlaces)
		{
			youngPetri.coreG.removeEdge(resource, ctred);
		}
		
		for(Place obsed : obsedPlaces)
		{
			youngPetri.coreG.removeEdge(obsed, resource);	
		}
	}
	
	






	static List<Pair<Stmt, Stmt>> endPairs  = new ArrayList<Pair<Stmt,Stmt>>();
	
	private static PetriCriticalSection cs2PetriCS(CriticalSection cs,
		PetriMethod ypm, Petri youngPetri, Set<Place> placesInMethod) {
		PetriCriticalSection petriCS = new PetriCriticalSection();
		petriCS.setPetriMethod(ypm);// the ctxts is in ypm, 
		petriCS.setCS(cs);
		petriCS.resPlace = ResourcePlaceManager.get_create_PlaceResource(ypm, cs.origLock, false); // maybe array =false
		Place controlledPlace = findControlledPlace(placesInMethod, cs);	    
		petriCS.controlledPlaces.add(controlledPlace);
		Set<Place> observedPlaces = findObservedPlaces(placesInMethod, cs);
		petriCS.obsedPlaces.addAll(observedPlaces);
		return petriCS;	
	}

	static Set<Place> tmp4findObservedPlaces = new HashSet<Place>();
	// level: suspicious
	private static Set<Place> findObservedPlaces(Set<Place> placesInMethod2,
			CriticalSection cs) {
		tmp4findObservedPlaces.clear();

		endPairs.clear();
		endPairs.addAll(cs.earlyEnds);
		endPairs.add(cs.end);// pair.getO1 returns the one after the exitMonitor. exit then goto, for example.
		for(Pair<Stmt, Stmt> pair : endPairs)
		{
			if(pair==null)
			{
				continue;
			}
			Stmt exitMonitor = pair.getO2();
			Place exitMonitorPlace = findPlace4Stmt(placesInMethod2 , exitMonitor);
			List<Place> predecessors= Petri._petri.getLocalPrecs(exitMonitorPlace);
			for(Place predecessor : predecessors)// complete & correct
			{
				if(!(predecessor instanceof PlaceCommonLocal)) continue; 
				Stmt stmt = ((PlaceCommonLocal)predecessor).jimpleStmt;
				if(PetriCriticalSectionManager.getUnits(cs).contains(stmt))// do not use cs.units, soot makes a bug there.
				  tmp4findObservedPlaces.add(predecessor);
			}			
		}
		
		if(tmp4findObservedPlaces.size()==0) throw new RuntimeException("what is up?");
		return tmp4findObservedPlaces;
	}

	static Set testset  = new HashSet();
	private static Place findControlledPlace(Set<Place> placesInMethod2,
			CriticalSection cs) {
		Stmt begin =cs.entermonitor;
		Place beginPlace = findPlace4Stmt(placesInMethod2, begin);
		List<Place> successors= Petri._petri.getLocalSuccs(beginPlace);
		
		testset.clear();
		testset.addAll(successors);		
		if(testset.size()!=1) throw new RuntimeException("there should one and only one successor for entermonitor");
		
		Place controlledPlace =(Place) successors.get(0);
		return controlledPlace;
	}

	private static PetriCriticalSection syncM2PlaceCS(PetriMethod ypm, Petri youngPetri, Set<Place> pSet) {		
		Body bb = ypm.getBb();		
		BriefUnitGraph uGraph = new BriefUnitGraph(bb);
		if(!tailCheckPass(uGraph)) return null;// no tails are present!
		
		PetriCriticalSection petriCriticalSection = new PetriCriticalSection();
		petriCriticalSection.setPetriMethod(ypm);// note that, We do not need to clone the ctxts again, share the reference
		petriCriticalSection.setMethod(ypm.getBb().getMethod());
		
		if(bb.getMethod().isStatic())
		{
			SootClass sc = bb.getMethod().getDeclaringClass();			
			petriCriticalSection.resPlace = ResourcePlaceManager.get_create_PlaceResource(ypm,sc.getType(),false);
		}
		else {
			petriCriticalSection.resPlace = ResourcePlaceManager.get_create_PlaceResource(ypm, bb.getThisLocal(), false);
		}
		
		Stmt begin = bb.getFirstNonIdentityStmt();
		Place beginPlace = findPlace4Stmt(pSet , begin);
		petriCriticalSection.controlledPlaces.add(beginPlace);
		
		for(Unit unit : uGraph.getTails())
		{
			if (!(unit instanceof soot.jimple.ReturnStmt ||
					unit instanceof soot.jimple.ReturnVoidStmt ||
					unit instanceof soot.baf.ReturnInst ||
					unit instanceof soot.baf.ReturnVoidInst)) {
		    	continue;
		    }        	        			
			Place endPlace = findPlace4Stmt(pSet , (Stmt)unit);
			if(endPlace==null) throw new RuntimeException("how can you pass the tail checking");
			petriCriticalSection.obsedPlaces.add(endPlace);
		}
		return petriCriticalSection;	
	}

	private static boolean tailCheckPass(BriefUnitGraph uGraph) {
		for(Unit u : uGraph.getTails())
		{
		    if (u instanceof soot.jimple.ReturnStmt ||
		    		u instanceof soot.jimple.ReturnVoidStmt ||
		    		u instanceof soot.baf.ReturnInst ||
		    		u instanceof soot.baf.ReturnVoidInst) {
		    	return true;
		    }
        	
		}
		return false;
	}
	private static Place findPlace4Stmt(Set<Place> pSet, Stmt begin) {
		for(Place p: pSet)
		{
			if(p instanceof PlaceCommonLocal)
			{
				Stmt jStmt = ((PlaceCommonLocal)p).getJimpleStmt();

				if(jStmt.equals(begin))// nonono, toString is really bad, a bug was raised here.
				{
					return p;
				}
			}
		}		
		return null;
	}

	public static void modelAllPlaceCSs(Petri youngPetri) {
		Set<PetriMethod> ypms = PetriMethodManager.getAllPetriMethods();
		for(PetriMethod ypm : ypms)
		{			
			modelPlaceCSs(ypm, youngPetri);	
		}
	    // handle recursive lock at marking enumeration time, do not worry now!
	}

	
	static Set<ArcFromResource> arcFromR4Switch = new HashSet<ArcFromResource>();
	static Set<ArcToResource> arcToR4Switch = new HashSet<ArcToResource>();
	public static void switchToFinestLocks() {
		Petri youngPetri = Petri.getPetri();
		ResourcePlaceManager.switchedPeriod = true;
		Set<PetriCriticalSection> placeCSs =PetriCriticalSectionManager.getAllPetriCSs();
		for(PetriCriticalSection pCs : placeCSs)
		{
            // create PR, register substitute
			PlaceResource newPR = ResourcePlaceManager.justCreatePR();
			ResourcePlaceManager.registerSub(newPR, pCs.getresPlace());
			// substitute the edges
			arcFromR4Switch.clear();
			
			Set<ArcFromResource> arcFromResources =pCs.getArcFromResources();
			for(ArcFromResource arc : arcFromResources)
			{
				Transition ctrled = (Transition) arc.getTarget();
				if(!youngPetri.coreG.containsVertex(newPR)) youngPetri.coreG.addVertex(newPR);
				Arc gen =youngPetri.coreG.addEdge_edgetype_lpxz(newPR, ctrled, ArcFromResource.class);
				arcFromR4Switch.add((ArcFromResource)gen);
				youngPetri.coreG.removeEdge(arc);				
			}
			arcToR4Switch.clear();
			Set<ArcToResource> arcToResources =pCs.getArcToResources();
			for(ArcToResource arc: arcToResources)
			{
				Transition obPlace = (Transition) arc.getSource();
				if(!youngPetri.coreG.containsVertex(newPR)) youngPetri.coreG.addVertex(newPR);
				Arc gen = youngPetri.coreG.addEdge_edgetype_lpxz(obPlace, newPR, ArcToResource.class);
				arcToR4Switch.add((ArcToResource)gen);
				youngPetri.coreG.removeEdge(arc);
				
			}
			// update the pcs internal
			pCs.setresPlace(newPR);
			pCs.getArcFromResources().clear();
			pCs.getArcFromResources().addAll(arcFromR4Switch);
			
			pCs.getArcToResources().clear();
			pCs.getArcToResources().addAll(arcToR4Switch);
			
			
		}
		
	}
	
	

	public static void recoverToOrigLocks() {
		// TODO Auto-generated method stub
		

		Petri youngPetri = Petri.getPetri();
		ResourcePlaceManager.switchedPeriod = true;
		Set<PetriCriticalSection> placeCSs =PetriCriticalSectionManager.getAllPetriCSs();
		for(PetriCriticalSection pCs : placeCSs)
		{
            // create PR, register substitute
			PlaceResource nowPR = pCs.getresPlace();
			PlaceResource oldPR = ResourcePlaceManager.getRegisteredOld(nowPR);
			ResourcePlaceManager.disregisterSub(nowPR);
			// substitute the edges
			arcFromR4Switch.clear();
			
			Set<ArcFromResource> arcFromResources =pCs.getArcFromResources();
			for(ArcFromResource arc : arcFromResources)
			{
				Transition ctrled = (Transition) arc.getTarget();
				if(!youngPetri.coreG.containsVertex(oldPR)) youngPetri.coreG.addVertex(oldPR);
				Arc gen =youngPetri.coreG.addEdge_edgetype_lpxz(oldPR, ctrled, ArcFromResource.class);
				arcFromR4Switch.add((ArcFromResource)gen);
				youngPetri.coreG.removeEdge(arc);				
			}
			arcToR4Switch.clear();
			Set<ArcToResource> arcToResources =pCs.getArcToResources();
			for(ArcToResource arc: arcToResources)
			{
				Transition obPlace = (Transition) arc.getSource();
				if(!youngPetri.coreG.containsVertex(oldPR)) youngPetri.coreG.addVertex(oldPR);
				Arc gen = youngPetri.coreG.addEdge_edgetype_lpxz(obPlace, oldPR, ArcToResource.class);
				arcToR4Switch.add((ArcToResource)gen);
				youngPetri.coreG.removeEdge(arc);
				
			}
			// update the pcs internal
			pCs.setresPlace(oldPR);
			pCs.getArcFromResources().clear();
			pCs.getArcFromResources().addAll(arcFromR4Switch);
			
			pCs.getArcToResources().clear();
			pCs.getArcToResources().addAll(arcToR4Switch);
			
			
		}
		ResourcePlaceManager.switchedPeriod = false;
	
		
	}

	



	
	

}
