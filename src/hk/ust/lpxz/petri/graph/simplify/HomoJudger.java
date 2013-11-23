package hk.ust.lpxz.petri.graph.simplify;

import java.util.HashSet;
import java.util.Set;

import hk.ust.lpxz.petri.graph.criticalsection.PetriCriticalSection;
import hk.ust.lpxz.petri.graph.criticalsection.PetriCriticalSectionManager;
import hk.ust.lpxz.petri.graph.violation.Violation;
import hk.ust.lpxz.petri.graph.violation.ViolationManager;
import hk.ust.lpxz.petri.unit.Place;

public class HomoJudger {

// at the same side of the boundary nodes of the violaiton or cs
	public static void main(String[] args) {		
		

	}
	
	static Set<Place> included = new HashSet<Place>();
	

	public static boolean homo(Place p1 , Place  p2)
	{
		boolean agree = true;
		Set<Violation> violations =	ViolationManager.getAllViolations();
		Set<PetriCriticalSection> placeCSs =  PetriCriticalSectionManager.getAllPetriCSs();
			for(Violation violation : violations)
		{
			included.clear();
			violation.includedPlaces(included);
			boolean tmp1  = included.contains(p1);
			boolean tmp2 = included.contains(p2);
			if(tmp1 != tmp2)// one in one out
			{
				agree = false;
				return agree;
			}
		}
		
		for(PetriCriticalSection placeCS : placeCSs)
		{
			included.clear();
			placeCS.includedPlaces(included);
			boolean tmp1 = included.contains(p1);
			boolean tmp2 = included.contains(p2);
			if(tmp1 != tmp2)
			{
				agree = false; // one in one out
				return  agree;
			}
		}
		return agree;		
	}

}
