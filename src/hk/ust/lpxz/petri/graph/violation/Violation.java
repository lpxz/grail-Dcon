package hk.ust.lpxz.petri.graph.violation;

import java.util.HashMap;
import java.util.Set;

import soot.jimple.FieldRef;
import soot.jimple.Stmt;

import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.graph.ICFGPetriReachable;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;

public class Violation { // an tag , deadlock is also a bug
	// essentailly a 6-entry tuple
	public Place pplace = null;
	public Place cPlace = null; 
	public Place rPlace =null;
	
	//=======not important: temp var
	public Set<Place> tempIncludedPlaces = null;
	
	public Set<Place> getTempIncludedPlaces() {
		return tempIncludedPlaces;
	}
	public void setTempIncludedPlaces(Set<Place> tempIncludedPlaces) {
		this.tempIncludedPlaces = tempIncludedPlaces;
	}
	
	public FieldRef accessedField()
	{
		PlaceCommonLocal pcl = (PlaceCommonLocal)rPlace;
		Stmt stmt =pcl.getJimpleStmt();
		if(stmt.containsFieldRef())
		{
			return stmt.getFieldRef();
		}
		return null;
		
	}

	
	public  String toString()
	{
		return "P: " +pplace.toString() + " PM:" + pYPM.classname+ "."+ pYPM.methodName + 
		  "\n R:" + rPlace.toString() + rYPM.classname + "." + rYPM.methodName +
		  "\n C:" + cPlace.toString() +  cYPM.classname + "." + cYPM.methodName ;
	}
	
	public PetriMethod pYPM =null;
	public PetriMethod cYPM  = null;
	public PetriMethod rYPM = null;
	public Place getPplace() {
		return pplace;
	}
	public void setPPlace(Place pplace) {
		this.pplace = pplace;
	}
	public Place getcPlace() {
		return cPlace;
	}
	public void setCPlace(Place cPlace) {
		this.cPlace = cPlace;
	}
	public Place getrPlace() {
		return rPlace;
	}
	public void setRPlace(Place rPlace) {
		this.rPlace = rPlace;
	}
	public PetriMethod getpYPM() {
		return pYPM;
	}
	public void setPPetriMethod(PetriMethod pYPM) {
		this.pYPM = pYPM;
	}
	public PetriMethod getcYPM() {
		return cYPM;
	}
	public void setCPetriMethod(PetriMethod cYPM) {
		this.cYPM = cYPM;
	}
	public PetriMethod getrYPM() {
		return rYPM;
	}
	public void setRPetriMethod(PetriMethod rYPM) {
		this.rYPM = rYPM;
	}
	
	

	
	public void includedPlaces(Set<Place> includedPlaces)
	{
		// both the beforeLoading and afterLoading can use it!
		// do not use the cache mechanism, it changes from time to time
		try {
			includedPlaces_betweenPC(includedPlaces);		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		includedPlaces.add(rPlace);

	}
	public void includedPlaces_betweenPC(Set<Place> includedPlaces) {
		Petri csG = Petri.getPetri();
		// no need to create a new set, because it is already created inside by the intersection
		ICFGPetriReachable.localAllInBetween(csG, pplace, cPlace,includedPlaces);
		
	}
	
	
//	public static HashMap<Place, YoungPetriMethod> place2ypm = new HashMap<Place, YoungPetriMethod>();
//	public static YoungPetriMethod getYPM(Place p )
//	{
//		return place2ypm.get(p);// may be null
//		
//	}
//	
//	public static void putYPM(Place p , YoungPetriMethod ypm)
//	{
//		place2ypm.put(p, ypm);
//	}
//	
	

}
