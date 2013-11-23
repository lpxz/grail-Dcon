package hk.ust.lpxz.petri.unit;

import hk.ust.lpxz.petri.graph.GadaraSupport.CommNamer;


public class Transition{//detailsOut.flush();
	//============for supporting gaddara
	public String specifiedName = null;
	public Transition()
	{
		
	}
	
	public Transition(String para)
	{
		specifiedName = para;
	}
	public boolean gadara_controllable = false;
	public boolean gadara_observable = false; 
	public void intialize(boolean c , boolean o)
	{
		gadara_controllable = c;
		gadara_observable = o;	
	}
	
	public boolean isGadara_controllable() {
		return gadara_controllable;
	}

	public void setGadara_controllable(boolean gadaraControllable) {
		gadara_controllable = gadaraControllable;
	}

	public boolean isGadara_observable() {
		return gadara_observable;
	}

	public void setGadara_observable(boolean gadaraObservable) {
		gadara_observable = gadaraObservable;
	}

	//==============

	
	public String toString()
	{
		if(specifiedName!=null ) return specifiedName;
		return this.getPetriName();
	}
	 public String getPetriName() {
		return CommNamer.getPetriNetName(this);
		
	}

	

}
