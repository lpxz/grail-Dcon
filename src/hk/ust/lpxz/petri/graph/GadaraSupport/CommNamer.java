package hk.ust.lpxz.petri.graph.GadaraSupport;

import java.util.HashMap;
import java.util.Iterator;



import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.graph.criticalsection.PetriCriticalSection;
import hk.ust.lpxz.petri.graph.violation.Violation;
import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
import hk.ust.lpxz.petri.unit.Transition;

public class CommNamer {

	public static final String equalsTo = "=";

	private static final String placePrefix = "p";
	private static final String transPrefix = "t";
	private static final String arcPrefix = "a";
	private static final String pmPrefix = "pm";
	private static final String vioPrefix = "v";
	private static final String pcsPrefix = "pcs";

	//public static final String arctype = "Arc";
	public static final String pmtype = "PetriMethod";
	public static final String viotype = "Violation";
	public static final String pcstype = "PetriCS";

	public static String RESOURCENAME = "resource";

	public static String CTREDPLACE = "ctredplace";

	public static String OBSEDPLACES = "obsedplaces";

	public static String PLACES = "places";

	public static String PPLACE = "pplace";

	public static String CPLACE = "cplace";

	public static String RPLACE = "rplace";

	public static String METHODSIG = "methodsig";

	public static String ENTRY = "entry";

	public static String EXIT = "exit";

	public static String WEIGHT = "weight";

	public static String CONTROLLABLE = "controllable";

	public static String OBSERVABLE = "observable";

	public static String STMT = "stmt";

	public static String TOKEN = "token";

	public static String ENCLOSINGM = "enclosingPM";

	public static String getNameInGenerating(Object p) {
		if (p instanceof Place) {
			return placePrefix + CommIDAssigner.getPlaceSerialNo((Place) p);
		} else if (p instanceof Transition) {
			return transPrefix
					+ CommIDAssigner.getTransSerialNO((Transition) p);
		} else if (p instanceof Arc) {
			return arcPrefix + CommIDAssigner.getArcSerialNO((Arc) p);
		} else if (p instanceof PetriMethod) {
			return pmPrefix
					+ CommIDAssigner.getPetriMSerialNO((PetriMethod) p);
		} else if (p instanceof Violation) {
			return vioPrefix + CommIDAssigner.getVioSerialNO((Violation) p);
		} else if (p instanceof PetriCriticalSection) {
			return pcsPrefix + CommIDAssigner.getPlaceCSNO((PetriCriticalSection) p);
		} else {
			throw new RuntimeException("what type??");
		}
	}

	// ==============================
	// store the Name for each object in laoding
	public static HashMap<Object, String> object2name = new HashMap<Object, String>();

	public static String PLACECOMMONLOCAL = "PlaceCommonLocal";

	public static String PLACERESOURCE = "PlaceResource";

	public static String TRANSITION = "Transition";

	public static String ARCLOCAL ="ArcLocal";

	public static String ARCCALL= "ArcCall";
	public static String ARCRETURN= "ArcReturn";
	public static String ARCFromResource= "ArcFromResource";
	public static String ARCToResource= "ArcToResource";
	public static String ARCFromController= "ArcFromController";
	public static String ARCToController= "ArcToController";

	public static String PLACEENTRY ="PlaceMethodEntry";
	public static String PLACEEXIT ="PlaceMethodExit";

	public static void registerNameInLoading(Object oo, String name) {
		//System.out.println("register:" + oo + " " + name);
		
		object2name.put(oo, name);
	}

	public static String getNameInLoading(Object oo) {
		return object2name.get(oo);
	}

	public static Object getObjectInLoading(String name)
	{
		Iterator it=object2name.keySet().iterator();
		while(it.hasNext())
		{
			Object key = it.next();
			String value = object2name.get(key);
			if(value.equals(name))
			{
				return key;
			}
		}
		return null;
		
	}

	public static String getPetriNetName(Object v) {
		
		    if(DconPropertyManager.trialPatcher)
		    {
		    	if(v instanceof PlaceCommonLocal)
		    	return v.toString();
		    	if(v instanceof Transition)
		    		return v.toString();
		    	
		    }
		    
		    
		    if(DconPropertyManager.useCommNamer)
			{
				return getNameInGenerating(v);
			}
			else
			{
				return GadaraNamer.getGadaraName(v);
			}
			
			
			
		
	
	}
}
