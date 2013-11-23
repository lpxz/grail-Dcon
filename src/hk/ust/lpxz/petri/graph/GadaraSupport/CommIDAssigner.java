package hk.ust.lpxz.petri.graph.GadaraSupport;

import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.graph.criticalsection.PetriCriticalSection;
import hk.ust.lpxz.petri.graph.violation.Violation;
import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.Transition;

import java.util.HashMap;

public class CommIDAssigner {

	public static int placeCounter = 0;
	public static HashMap<Place, Integer> place2serID = new HashMap<Place, Integer>();

	public static int getPlaceSerialNo(Place p) {
		Integer val = place2serID.get(p);
		if (val == null) {
			place2serID.put(p, placeCounter);
			placeCounter++;
			val = place2serID.get(p);
		}
		return val.intValue();
	}

	public static int transCounter = 0;
	public static HashMap<Transition, Integer> transition2ID = new HashMap<Transition, Integer>();

	public static int getTransSerialNO(Transition t) {
		Integer val = transition2ID.get(t);
		if (val == null) {
			transition2ID.put(t, transCounter);
			transCounter++;
			val = transition2ID.get(t);
		}
		return val.intValue();
	}

	public static int arcCounter = 0;
	public static HashMap<Arc, Integer> arc2ID = new HashMap<Arc, Integer>();

	public static int getArcSerialNO(Arc arc) {
		Integer val = arc2ID.get(arc);
		if (val == null) {
			arc2ID.put(arc, arcCounter);
			arcCounter++;
			val = arc2ID.get(arc);
		}
		return val.intValue();
	}

	public static int PetriMCounter = 0;
	public static HashMap<PetriMethod, Integer> pm2ID = new HashMap<PetriMethod, Integer>();

	public static int getPetriMSerialNO(PetriMethod young) {
	
		Integer val = pm2ID.get(young);
		if (val == null) {
			pm2ID.put(young, PetriMCounter);
			PetriMCounter++;
			val = pm2ID.get(young);
		}
		return val.intValue();
	}

	public static int VioCounter = 0;
	public static HashMap<Violation, Integer> vio2ID = new HashMap<Violation, Integer>();

	public static int getVioSerialNO(Violation vio) {
		Integer val = vio2ID.get(vio);
		if (val == null) {
			vio2ID.put(vio, VioCounter);
			VioCounter++;
			val = vio2ID.get(vio);
		}
		return val.intValue();
	}

	public static int placeCSCounter = 0;
	public static HashMap<PetriCriticalSection, Integer> pcs2ID = new HashMap<PetriCriticalSection, Integer>();

	public static int getPlaceCSNO(PetriCriticalSection pcs)
	{
		Integer val = pcs2ID.get(pcs);
		if(val==null)
		{
			pcs2ID.put(pcs, placeCSCounter);
			placeCSCounter++;
			val = pcs2ID.get(pcs);
				
		}
		return val.intValue();
		
	}
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
