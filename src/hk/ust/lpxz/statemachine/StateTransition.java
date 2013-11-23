package hk.ust.lpxz.statemachine;

import org.jgrapht.graph.DefaultEdge;

import hk.ust.lpxz.petri.unit.Arc;

public class StateTransition extends DefaultEdge{

    boolean controllable = true;

    
	
	public boolean isControllable() {
		return controllable;
	}



	public void setControllable(boolean controllable) {
		this.controllable = controllable;
	}



	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
