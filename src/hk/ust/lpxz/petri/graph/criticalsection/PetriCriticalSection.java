package hk.ust.lpxz.petri.graph.criticalsection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.hkust.clap.lpxz.context.ContextMethod;


import pldi.locking.CriticalSection;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.graph.ICFGPetriReachable;
import hk.ust.lpxz.petri.unit.ArcFromResource;
import hk.ust.lpxz.petri.unit.ArcToResource;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceResource;
import hk.ust.lpxz.petri.unit.Transition;

public class PetriCriticalSection {
	public PetriMethod ypm;
	
	public PetriMethod getYpm() {
		return ypm;
	}
	public void setPetriMethod(PetriMethod ypm) {
		this.ypm = ypm;
	}


	
//public  Set<Place> getPlaces()
//{
//	return  included;
//	}
	
	public void includedPlaces(Set<Place> includedPlaces)
	{			
		Set<Place> ctreds = getCtredPlaces();
		Set<Place> obseds = getObsedPlaces();
		if(ctreds.size()!=1) throw new RuntimeException("one and only one controlled place");
		// syncM is normal, its ctred/obsed is not the entry/exit
		for(Place ctred : ctreds)
		{
			for(Place obsed : obseds)
			{
				ICFGPetriReachable.localAllInBetween(Petri.getPetri(), ctred, obsed, includedPlaces);
			}
		}
	}

	// the controlled palces are essnetially the start fo the cs
	// the observed places are essentially e\the ends of the cs.
	public CriticalSection baseCS = null;// for finding the stmts fast
	public SootMethod baseSynM = null;// for finding the stmts fast
	
	public SootMethod getBaseSynM() {
		return baseSynM;
	}
	public void setMethod(SootMethod baseSynM) {
		this.baseSynM = baseSynM;
	}
	public PlaceResource getresPlace() {
		return resPlace;
	}
	public void setresPlace(PlaceResource rPlace) {
		this.resPlace = rPlace;
	}
	public CriticalSection getBaseCS() {
		return baseCS;
	}
	public void setCS(CriticalSection baseCS) {
		this.baseCS = baseCS;
	}



	 public List<ContextMethod> getCtxts() {
		return ypm.getCtxts();
	}
//	public void setCtxts(List<Stmt> ctxts) {
//		this.ctxts = ctxts;
//	}



	public PlaceResource resPlace = null;
	
	public Set<Place> controlledPlaces = new HashSet<Place>();
	public Set<Place> obsedPlaces = new HashSet<Place>();
	
//	public Set<Transition> ctredTransitions = null;
//	public Set<Transition> obsedTransitions = null;
	
	public Set<ArcFromResource> arcFromResources = new HashSet<ArcFromResource>();
	public Set<ArcToResource> arcToResources = new HashSet<ArcToResource>();
	// after the petrify. it is a shortcut only

	public Set<ArcFromResource> getArcFromResources() {
		return arcFromResources;
	}

	public Set<ArcToResource> getArcToResources() {
		return arcToResources;
	}

	
	public Set<Place> getCtredPlaces() {
		return controlledPlaces;
	}
	
	public void setCtredPlaces(Set<Place> ctredPlaces) {
		this.controlledPlaces = ctredPlaces;
	}

	public Set<Place> getObsedPlaces() {
		return obsedPlaces;
	}
	public void setObsedPlaces(Set<Place> obsedPlaces) {
		this.obsedPlaces = obsedPlaces;
	}
	
	// by seraching the ArcFromResource, and ArcToResource
	public Set<Transition> computeCtredTransitions()// jsut compute them!, on the graph!
	{
		HashSet<Transition> toret = new HashSet<Transition>();
		Set<ArcFromResource> arcs = getArcFromResources();
		for(ArcFromResource arc : arcs)
		{
		   toret.add((Transition)arc.getTarget());
		}
		
		return  toret;
	}
	
	public Set<Transition> computeObsedTransitions()
	{
		HashSet<Transition> toret = new HashSet<Transition>();
		Set<ArcToResource> arcs = getArcToResources();
		for(ArcToResource arc : arcs)
		{
		   toret.add((Transition)arc.getSource());
		}		
		return  toret;
	}
	
	
//=============================================
	public Set<Place> includedInLoading = new HashSet<Place>();// placeholder field, llvm implementation should give the fin!




	
	
	
	
	

}
