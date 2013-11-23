package hk.ust.lpxz.petri.graph.violation;

import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;

public class ViolationModeller {

	public static Violation modelViolation(Place pplace, PetriMethod ypm1, Place cplace, PetriMethod ypm12, Place rplace, PetriMethod ypm2) {
		Violation violation = new Violation();	       
		
		violation.setPPlace(pplace);
        violation.setPPetriMethod(ypm1);
        
        violation.setCPlace(cplace);
        violation.setCPetriMethod(ypm1);
                
        violation.setRPlace(rplace);        
        violation.setRPetriMethod(ypm2);    
		 		
		ViolationManager.mapPetriMethod2Violations(ypm1, violation);
		ViolationManager.mapPetriMethod2Violations(ypm2, violation);
		return violation;
	}
}
