package hk.ust.lpxz.statemachine;

import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
import hk.ust.lpxz.petri.unit.PlaceResource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import soot.toolkits.scalar.Pair;




public class ResourceState {

	// old version for StateGraphBUilder_onlyDeadlock
	 public boolean placeDisabled(PlaceResource p)
	    {
	    	if(availablePlaces.contains(p))
	    	{
	    		availablePlaces.remove(p);
	    		return true;
	    	}
	    	else {
				return false; // does not exist at all
			}
	    	
	    }
	    
	    public boolean placeEnabled(PlaceResource p)
	    {
	    	if(availablePlaces.contains(p))
	    	{
	    		return false;
	    	}
	    	else {
				availablePlaces.add(p);
				return true;
			}
	    }
	   
	// no, recursion lock should be implemented as the thread-identity based
	    //here, we only have token number, no therad identity at all
//    public HashMap<PlaceResource, Integer> recurLockHistory = null;
//	 //new HashMap<PlaceResource, Integer>();
//
//
//public HashMap<PlaceResource, Integer> getRecursiveLockHashMap() {
//	return recurLockHistory;
//}
//
//public void setRecursiveLockHashMap(
//		HashMap<PlaceResource, Integer> recursiveLockHashMap) {
//	this.recurLockHistory = recursiveLockHashMap;
//}

	public ResourceState(Set<PlaceResource> prs )
	{
		for(PlaceResource pr : prs)
		{
			availablePlaces.add(pr);// yes, not reference, but deep clone, initially, all are avialable
		}
	}
	public String toString()
	{// not important
		StringBuffer sb = new StringBuffer();
		for(Place p:availablePlaces)
		{
			sb.append(p.getPetriName()+"\n");
		}
		return sb.toString();
	}
    public Set<PlaceResource> availablePlaces = new HashSet<PlaceResource>();
  //  public Set<Place> twoTokenPlaces = new HashSet<Place>();
    
    
    public Set<PlaceResource> getAvailablePlaces() {
		return availablePlaces;
	}
	public void setAvailablePlaces(Set<PlaceResource> availablePlaces) {
		this.availablePlaces = availablePlaces;
	}
	//=============================

   
    
    
    
    //==============the following is for hashmap 
    public int hashCode() {// hash is just for speeding up
    	int num  = 0;
    	for(Place p : availablePlaces)
    	{
    		num += (p.hashCode());
    	}
    	
    	
    	return num ;
    	
    }
    @Override
    public boolean equals( Object otherpara ) {
    	// executed ever?
    	System.err.println("executed ever");
    	if(otherpara instanceof ResourceState)
    	{
    		ResourceState other = (ResourceState)otherpara;
    		 boolean same1 = containSameContent(this.availablePlaces, other.availablePlaces);
    	      
    	       return same1 ;
    	}
    	else {
			 return false;
		}
    	
    	
      
    	
    }
	private boolean containSameContent(Set<PlaceResource> oneTokenPlaces1,
			Set<PlaceResource> oneTokenPlaces2) {
		if(oneTokenPlaces1.size()!=oneTokenPlaces2.size()) return false;// quick path
		
        boolean include1 = isContainedBy(oneTokenPlaces1, oneTokenPlaces2);
        boolean include2 = isContainedBy(oneTokenPlaces2, oneTokenPlaces1);
        
		
		
		return include1&&include2; // both true, then true
	}
	private boolean isContainedBy(Set<PlaceResource> oneTokenPlaces1,
			Set<PlaceResource> oneTokenPlaces2) {
		
		for(Place p: oneTokenPlaces1)
		{
			if(!oneTokenPlaces2.contains(p))// if this branch is not executed at all, set1 <=set2
			{
				return false;
			}
		}
		
		return true;
	}
	public static void main(String[] args) {}

	public boolean canProvideResources(Set<PlaceResource> wants) {
		 if(wants.size()==0)
	        	return true; // I do not need any reousrces
	        else {
	        	Set<PlaceResource> haves = this.getAvailablePlaces();
	       //// give a second chance! recursivelock:
	        	
	        	
	        
				if(haves.containsAll(wants))
				{
					return true;
				}
				else {
								
					return false;
				}
			}	
	}

	
	// the following three are very important, in implementing the 2PL unsafe computation
	public boolean acqResourcePlace(PlaceResource prec) {
	    //at the same time, 
        return   placeDisabled(prec);

	}


	public void relResourcePlace(PlaceResource succ) {
        placeEnabled(succ);	
		
	}



	public ResourceState deepClone() {
	    ResourceState ret = new ResourceState(this.availablePlaces);
	   
		return ret;
	}
}
