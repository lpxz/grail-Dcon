package hk.ust.lpxz.SBPI;

import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceResource;
import hk.ust.lpxz.petri.unit.Transition;
import hk.ust.lpxz.statemachine.State;
import hk.ust.lpxz.statemachine.StateVectorGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;


import Jama.Matrix;

public class ToMatrix {

   
    // states act as the mask, it enlists all the placs we need to consider
    // also, we only consider the transitions which connect to the places.
//    static List<Place> places_2Matrix = new ArrayList<Place>();
//    static List<Transition> transitions_2Matrix =new ArrayList<Transition>();
    public static Matrix toMatrix()
    {
    	List<Place> places_2Matrix_para=StateVectorGenerator.placeTemplate;
    	List<Transition> transitions_2Matrix_para=StateVectorGenerator.transtemplate;
    	Matrix toret = new Matrix(places_2Matrix_para.size(), transitions_2Matrix_para.size());
//    	toret.setPlaceTempList(places);
//    	toret.setTranTempList(transitions);
    	Petri youngPetri= Petri.getPetri();
    	
    	for(int i=0; i< places_2Matrix_para.size(); i++)
    	{
    		Object p =  places_2Matrix_para.get(i);
    		List inputs =youngPetri.getAllPrecs(p);
    		List outputs =youngPetri.getAllSuccs(p);
    		for(Object input : inputs)
    		{
    			int inputIndex =transitions_2Matrix_para.indexOf(input);
    		    toret.set(i, inputIndex, 1);
    		}
    		for(Object output : outputs)
    		{
    			int outputIndex =transitions_2Matrix_para.indexOf(output);
    		    toret.set(i, outputIndex, -1);
    		}
    		
    	}  
    	return toret;
    }
    
    
    public static Matrix toMatrix(List<List> conSet)
    {
    	if(conSet.size()==0) throw new RuntimeException("what constraint to guarantee?");
    	List oneList = conSet.get(0);
    	
   
    	Matrix toret = new Matrix(conSet.size(), oneList.size());
    	for(int i=0; i< conSet.size(); i++)
    	{
            List tmpList = conSet.get(i);
    		for(int j=0 ; j<tmpList.size();j++)
    		{
    			Double valueO  = (Double)tmpList.get(j);
    		    double value = valueO.doubleValue();
    		    toret.set(i, j, value);
    		}
    	}  
    	return toret;

    	
    }
    
    
    
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}



}
