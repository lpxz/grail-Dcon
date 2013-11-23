package hk.ust.lpxz.statemachine;



import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
import hk.ust.lpxz.petri.unit.PlaceResource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;



import soot.toolkits.scalar.Pair;




public class State {

	// it should be attached with the resource state
	public ResourceState resourceState = null;
	
	public ResourceState getResourceState() {
		return resourceState;
	}

	public void setResourceState(ResourceState resourceState) {
		this.resourceState = resourceState;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		for(Place p:oneTokenPlaces)
		{
			sb.append(p.getPetriName()+" ");
		}
		
		for(Place p:twoTokenPlaces)
		{
			sb.append(p.getPetriName()+"' ");
		}
		if(oneTokenPlaces.size()==0 && twoTokenPlaces.size()==0)
		{
			sb.append("empty");
		}
		
		//sb.append("\n");
		return sb.toString();
	}
    public Set<Place> oneTokenPlaces = new HashSet<Place>();
    public Set<Place> twoTokenPlaces = new HashSet<Place>();
    
    public Set<Place> containTokenPlaces = null;
    
    public Set<Place> containTokenPlaces()// return a complete list
    {
    	if(containTokenPlaces!=null)
    		return containTokenPlaces;
    	else 
    	{
			containTokenPlaces = new HashSet<Place>();
			containTokenPlaces.addAll(oneTokenPlaces);
			containTokenPlaces.addAll(twoTokenPlaces);
			return containTokenPlaces;
		}
    }
    
    //=============================quick add & remove operations
    // return false, meaning that the adding is not successful
    public boolean addPlace_success(Place p)
    {
    	if(twoTokenPlaces.contains(p)) return false;// no, if you add, there will be three tokens, I can not represent it
    	else {
			if(oneTokenPlaces.contains(p))
			{
				oneTokenPlaces.remove(p);
				twoTokenPlaces.add(p);// promoted
				return true;
			}
			else { // even one does not contain it
				oneTokenPlaces.add(p);
				return true;
			}
		}    	
    }
    
    // return false, means that p is not contained at all
    public boolean removePlace(Place p)// 
    {
    	if(oneTokenPlaces.contains(p) && twoTokenPlaces.contains(p))
    	{
    		throw new RuntimeException("impossible");
    	}
    	if(twoTokenPlaces.contains(p))// degrade
    	{
    		twoTokenPlaces.remove(p);
    		oneTokenPlaces.add(p);
    		return true;
    	}else if (oneTokenPlaces.contains(p)) {
			oneTokenPlaces.remove(p);
			return true;
		}
    	else { // both do not contain p;
			return false;
		}
    	
    }
    
    
    
    //==============the following is for hashmap 
    public int hashCode() {// hash is just for speeding up
    	int num  = 0;
    	for(Place p : oneTokenPlaces)
    	{
    		num += (p.hashCode());
    	}
    	
    	for(Place p: twoTokenPlaces)
    	{
    		num += 2*(p.hashCode());    		
    	}
    	return num ;
    	
    }
    @Override
    public boolean equals( Object otherpara ) {
    	if(otherpara instanceof State)
    	{
    		State other = (State)otherpara;
    		 boolean same1 = containSameContent(this.oneTokenPlaces, other.oneTokenPlaces);
    	       boolean same2 = containSameContent(this.twoTokenPlaces, other.twoTokenPlaces);
    	       return same1 && same2;
    	}
    	else {
			 return false;
		}
    	
    	
      
    	
    }
	private boolean containSameContent(Set<Place> oneTokenPlaces1,
			Set<Place> oneTokenPlaces2) {
		if(oneTokenPlaces1.size()!=oneTokenPlaces2.size()) return false;// quick path
		
        boolean include1 = isContainedBy(oneTokenPlaces1, oneTokenPlaces2);
        boolean include2 = isContainedBy(oneTokenPlaces2, oneTokenPlaces1);
        
		
		
		return include1&&include2; // both true, then true
	}
	private boolean isContainedBy(Set<Place> oneTokenPlaces1,
			Set<Place> oneTokenPlaces2) {
		
		for(Place p: oneTokenPlaces1)
		{
			if(!oneTokenPlaces2.contains(p))// if this branch is not executed at all, set1 <=set2
			{
				return false;
			}
		}
		
		return true;
	}
	
	
	
	
	//=================================
    public State deepClone()
    {
    	State clonee = this;
    	State toret = new State();
    	toret.oneTokenPlaces.addAll(clonee.oneTokenPlaces);
    	toret.twoTokenPlaces.addAll(clonee.twoTokenPlaces);
    	
    	ResourceState toretRS =clonee.getResourceState().deepClone();// new ResourceState(clonee.getResourceState().getAvailablePlaces());
    	toret.setResourceState(toretRS); 	
    	
    	
    	return toret;
    	
    	
    }
    
	
	

    
	public static void main(String[] args) {
	// testing
		State s1 = new State();
		State s2 = new State();
		
		Place p1 = new PlaceCommonLocal();
		
		Place p2 = new PlaceCommonLocal();
		
		s1.addPlace_success(p1);
		s1.addPlace_success(p2);
		s1.addPlace_success(p1);
		
//		s1.removePlace(p1);
//		s1.removePlace(p1);
		
		s2.addPlace_success(p1);
		s2.addPlace_success(p2);
		s2.addPlace_success(p1);
		
//		s2.removePlace(p1);
//		s2.removePlace(p1);
		
		HashMap<State, Integer> map = new HashMap<State, Integer>();
		map.put(s1, 0);
		map.put(s2, 1);

		
		

	
		
		System.out.println(map.get(s1));
		
	
	
		
	}

	// dispatching
    public  boolean canProvideResources(Set<PlaceResource> wants) {
		return this.getResourceState().canProvideResources(wants);       
	}
    
	public void acqResourcePlace(PlaceResource prec) {
		
		this.getResourceState().acqResourcePlace(prec);
		
		
	}

	public void relResourcePlace(PlaceResource succ) {
		
		this.getResourceState().relResourcePlace(succ);
		
	}

}
