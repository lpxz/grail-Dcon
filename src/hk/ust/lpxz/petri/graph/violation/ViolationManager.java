package hk.ust.lpxz.petri.graph.violation;



import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.graph.PetriMethodManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// tofix
public class ViolationManager {
   // one method may correspond to multiple violations
	public static HashMap<PetriMethod, Set<Violation>> ypm2vio = new HashMap<PetriMethod, Set<Violation>>();
	
	public static void mapPetriMethod2Violations(PetriMethod ypm,  Violation vio)
	{
		Set<Violation> vios =ypm2vio.get(ypm);
		if(vios==null)
		{
			vios = new HashSet<Violation>();
			ypm2vio.put(ypm, vios);
		}
		vios.add(vio);
		
	}
	public static Set<Violation> allViolations = new HashSet<Violation>();
	public static  Set<Violation> getAllViolations()
	{
		    allViolations.clear();
			Set<Violation> ypms = new HashSet<Violation>();
			
			Iterator<Set<Violation>> it =ypm2vio.values().iterator();
			while (it.hasNext()) {
				HashSet<Violation> hashSet = (HashSet<Violation>) it
						.next();
				ypms.addAll(hashSet);			
			}
			allViolations = ypms;
			return allViolations;
		
	    
	}

	
	
	
}
