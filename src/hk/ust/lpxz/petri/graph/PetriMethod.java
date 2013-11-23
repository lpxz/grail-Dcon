package hk.ust.lpxz.petri.graph;

import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceMethodEntry;
import hk.ust.lpxz.petri.unit.PlaceMethodExit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import soot.Body;
import soot.Unit;
import edu.hkust.clap.lpxz.context.ContextMethod;



public class PetriMethod {
	// guan neng tuan
	// it is an organ of the graph
	
	
//	public HashMap<Unit, Integer> u2LineNO ;
//	
//	public HashMap<Unit, Integer> getU2LineNO() {
//		return u2LineNO;
//	}
//	public void setU2LineNO(HashMap<Unit, Integer> u2LineNO) {
//		this.u2LineNO = u2LineNO;
//	}

	


	public String classname ;
	public String methodName; 
	
	
	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	

	
	
	
	public String getMsig() {
	 return bb.getMethod().getSignature();
	}
	




	public PlaceMethodEntry entry ;

	public PlaceMethodExit exit ;

	public Body bb ;
	public HashMap<Unit, Place> unit2place ;
	
	public PlaceMethodEntry getEntry() {
		return entry;
	}
	public void setEntry(PlaceMethodEntry entry) {
		this.entry = entry;
	}
	public PlaceMethodExit getExit() {
		return exit;
	}
	public void setExit(PlaceMethodExit exit) {
		this.exit = exit;
	}

	public Body getBb() {
		return bb;
	}
	public void setJimpleBody(Body bb) {
		this.bb = bb;
	}
	public HashMap<Unit, Place> getUnit2Place() {
		return unit2place;
	}
	public void setUnit2Place(HashMap<Unit, Place> unit2placeArg) {
		unit2place = unit2placeArg;
	}

	
	List<ContextMethod> ctxts;
	

	 public List<ContextMethod> getCtxts() {
		return ctxts;
	}
	public void setContexts(List<ContextMethod> ctxtsarg) {
		this.ctxts = ctxtsarg;
	}





		
		public void cancelAssociateForPlace(Place p) {
			   Iterator<Entry<Unit, Place>> it = unit2place.entrySet().iterator();
			   Object key = null;
			   
			   while (it.hasNext()) {
				Entry<Unit,Place> entry = (Entry<Unit,Place>) it.next();
				if(entry.getValue().equals(p))
				{
					key= entry.getKey();
				}
				
			   }
			   if(key!=null)
			   {
				   unit2place.remove(key);
//				   u2LineNO.remove(key);
			   }			
		}
		
		
		
		
		
		
		
		
		
		
		
		///=========================================
		public String msigInLoading = ""; // it helps recovering everything

		public String getMsigInLoading() {
			return msigInLoading;
		}
		public void setMsigInLoading(String msigInLoading) {
			this.msigInLoading = msigInLoading;
		}
		Set<Place> cacheRet = new HashSet<Place>();
		public Set<Place> containedPlaces_intra() {
			cacheRet.clear();
			 ICFGPetriReachable.localAllInBetween(Petri.getPetri(), getEntry(), getExit(),cacheRet);
			 return cacheRet;
		}
		public Place getPlace(Unit unit) {
			return getUnit2Place().get(unit);
		}
		public HashMap<Unit, Place> getUnit2Place_or_create() {
			HashMap<Unit, Place> unit2placeLocal= getUnit2Place();// later on, set the u2n contents..
	    	if(unit2placeLocal==null )
	    	{
	    		unit2placeLocal= new HashMap<Unit, Place>();
	    		setUnit2Place(unit2placeLocal);
	    	}
	    	return unit2placeLocal;
		}
		
		
		
}
