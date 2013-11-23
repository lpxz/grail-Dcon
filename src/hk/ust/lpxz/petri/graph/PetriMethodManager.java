package hk.ust.lpxz.petri.graph;

import hk.ust.lpxz.fixing.SootAgent4Fixing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import edu.hkust.clap.lpxz.context.ContextMethod;


public class PetriMethodManager {
	public static HashMap<String, HashSet<PetriMethod>> methodName2PetriMethods = new HashMap<String, HashSet<PetriMethod>>();

	public static Set<PetriMethod> allCSMs = null;
	public static  Set<PetriMethod> getAllPetriMethods()
	{
		if(allCSMs !=null)// cahce
		{
			return allCSMs;
		}
		else {
			Set<PetriMethod> ypms = new HashSet<PetriMethod>();
			
			Iterator<HashSet<PetriMethod>> it =PetriMethodManager.methodName2PetriMethods.values().iterator();
			while (it.hasNext()) {
				HashSet<PetriMethod> hashSet = (HashSet<PetriMethod>) it
						.next();
				ypms.addAll(hashSet);			
			}
			allCSMs = ypms;
			return allCSMs;
		}
	    
	}
	
	public static HashSet<String> containingMethods = new HashSet<String>();
	public static PetriMethod getCurrentPetriMethod(
			List<ContextMethod> contexts) {
		ContextMethod currentMethod = contexts.get(contexts.size()-1); // the last one of the contexts
		String currentMethodName = currentMethod.getCurMsig();
		
		
		
		return retrievePetriMethod(currentMethodName, contexts);		
	}
	
	public static PetriMethod retrievePetriMethod(String methodName,
			List<ContextMethod> ctxtStack) {
		HashSet<PetriMethod> gcss =methodName2PetriMethods.get(methodName);
		if(gcss==null)
			 return null;
		for(PetriMethod gcs:gcss)
		{
			List<ContextMethod> stes =gcs.getCtxts();
			if(ctxtsMatch(stes,ctxtStack)){
				//// main has a special ctxt: empty, 
				//the methods with different parameters can not be invoked under the same ctxt!				
				return gcs;
			}
		}
		return null;
	}


	public static boolean ctxtsMatch(List<ContextMethod> stes,
			List<ContextMethod> ctxtStack) {
		if(stes.size() != ctxtStack.size()) return false;
		boolean toret = true;
		for(int i=0; i < stes.size(); i++)
		{
			ContextMethod ste = stes.get(i);
			ContextMethod ste2 = ctxtStack.get(i);
			if(!ste.equals(ste2)) // actually compare the two fields
			{
				toret =false;
			}
		}
		
		return toret;
	}

	public static void registerPetriMethod(String methodName,
			PetriMethod newOne) {
		HashSet<PetriMethod> entrySs =methodName2PetriMethods.get(methodName);
		if(entrySs==null)
		{
		  entrySs = new HashSet<PetriMethod>();
		  methodName2PetriMethods.put(methodName, entrySs);
		}
		entrySs.add(newOne);
	}
	
	
	
	//========================
	
	//after load PN from the file. The PM name can be used as a unique identifier.
	static HashMap<String, PetriMethod> pmname2PM = new HashMap<String, PetriMethod>();
	public static void registerInLoading(String pmname, PetriMethod pm)
	{
		   pmname2PM.put(pmname, pm);
	}
	
	public static PetriMethod getPMInLoading(String pmname)
	{
		return pmname2PM.get(pmname);
	}


	public static PetriMethod getPetriMethod4PCR(
			List<ContextMethod> contexts) {
		ContextMethod ste = contexts.get(contexts.size() - 1);
		PetriMethod ypm1 = PetriMethodManager
		.retrievePetriMethod(ste.getCurMsig(),
				contexts);
		return ypm1;
	}


	public static Unit getInvokeUnit(List<ContextMethod> ctxtStack) {
		ContextMethod currentMethod = ctxtStack.get(ctxtStack.size()-1); // 
		ContextMethod callerMethod = ctxtStack.get(ctxtStack.size()-2);
		String invStr  = currentMethod.getTheCallsite();
		int invLine = currentMethod.getLineOfCallSite();
		SootMethod secondM =Scene.v().getMethod(callerMethod.getCurMsig());
		secondM.retrieveActiveBody();		
		Body bb =secondM.getActiveBody();
		Iterator<Unit> it= bb.getUnits().iterator();
		while (it.hasNext()) {
			Unit unit = (Unit) it.next();
			if(unit.toString().equals(invStr) && SootAgent4Fixing.getLineNum(unit)==invLine)
			{
				return  unit ;
			}			
		}
		
		return null;
	}

	public static PetriMethod getCallerPetriMethod(List<ContextMethod> ctxtStack) {
		List<ContextMethod> contexts4callerMethod = ctxtStack.subList(0, ctxtStack.size() - 1); // trim: remove the last method from the contexts.
		PetriMethod callerMethod = PetriMethodManager.getCurrentPetriMethod(contexts4callerMethod);
		return callerMethod;
	}

}
