package hk.ust.lpxz.petri.graph.criticalsection;
import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.unit.PlaceResource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



import soot.AnySubType;
import soot.Local;
import soot.PointsToAnalysis;
import soot.Scene;
import soot.Type;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.toolkits.scalar.Pair;
import sun.reflect.Reflection;



public class ResourcePlaceManager {
	public static boolean switchedPeriod = false; // initially, false
	public static HashMap<Object, PlaceResource> orig2PR = new HashMap<Object, PlaceResource>(); 
	
	public static HashMap<PlaceResource, PlaceResource> substituteWhat = new HashMap<PlaceResource, PlaceResource>(); 
	public static void registerSub(PlaceResource a, PlaceResource b)// a substitutes b
	{
		substituteWhat.put(a, b);
	}
	
	public static PlaceResource getRegisteredOld(PlaceResource a)
	{
		return substituteWhat.get(a);
	}
	public static void disregisterSub(PlaceResource a) {
		substituteWhat.remove(a);
		
	}

	
	public static Set<PlaceResource> allPRs = new HashSet<PlaceResource>();

	public static Set<PlaceResource> getAllPR()
	{
		
		
			if(!switchedPeriod)
			{
				allPRs.clear();
				for(Object object : orig2PR.values())
				{
					allPRs.add((PlaceResource) object);
				}
				return allPRs;
			}
			else {
				return substituteWhat.keySet();// all are registered here
			}
		
		
		
	}
	
	public static void printIt(List<Stmt> ctxts)
	{
		for(Stmt st : ctxts)
		{
			System.out.println(st);
		}
	}
//	public static PlaceResource  getOrCreatePR(YoungPetriMethod ypm, Value origLock, boolean arrayEle)
//	{
//		// manually do it currently, later on create the thread copies and get the statemetns (thread-sen lock modeling), 
//		//model the lock using paddle anode
//		
//        
//
//		
//		Type tt  = origLock.getType();
//		
//		
//		if(orig2PR.containsKey(tt))
//		{
//			PlaceResource pr =   orig2PR.get(tt);
//			if(arrayEle)
//			{
//				pr.setMaybeArrayEle(true);
//			}// it can be false, many instances of the same type exist
//			
//			
//			return pr;
//		}
//		else {
//			PlaceResource pr = new PlaceResource(origLock ,arrayEle);
//			if(arrayEle)
//			{
//				pr.setMaybeArrayEle(true);
//			}// it can be false, many instances of the same type exist
//			orig2PR.put(tt, pr );// it does not matter what is contained, we only care about the PlaceResource identity
//		    return pr;
//		}
//		
//	}


	public static PlaceResource  get_create_PlaceResource(PetriMethod ypm, Object origLock, boolean arrayEle)
	{
        // one PR per type. you can choose another strategy.
		PlaceResource pr = null;
		Type tt  = null;
		if(origLock instanceof Value)
		{
			tt  = ((Value)origLock).getType();	
		}else if ( origLock instanceof Type) {
			tt = (Type) origLock;
		}else {
			throw new RuntimeException("incorrect invocation of the current method");
		}
			
		if(orig2PR.containsKey(tt))
		{
			pr = orig2PR.get(tt);			
		}
		else {
			pr = new PlaceResource(origLock ,arrayEle);
			orig2PR.put(tt, pr );// it does not matter what is contained, we only care about the PlaceResource identity
		}
		if(arrayEle)
		{
			pr.setMaybeArrayEle(true);
		}
		return pr;
	}
	
	public static PlaceResource  justCreatePR()
	{
		
		PlaceResource pr = new PlaceResource(null ,false);
		return pr;
	}



}
