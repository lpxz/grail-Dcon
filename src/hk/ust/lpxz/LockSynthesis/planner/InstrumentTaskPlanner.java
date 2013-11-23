package hk.ust.lpxz.LockSynthesis.planner;

import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.ICFGPetriReachable;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.graph.PetriReachable;
import hk.ust.lpxz.petri.graph.GadaraSupport.GadaraFormat;
import hk.ust.lpxz.petri.graph.GadaraSupport.GadaraNamer;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
import hk.ust.lpxz.petri.unit.PlaceControl;
import hk.ust.lpxz.petri.unit.PlaceMethodEntry;
import hk.ust.lpxz.petri.unit.PlaceMethodExit;
import hk.ust.lpxz.petri.unit.PlaceResource;
import hk.ust.lpxz.petri.unit.Transition;

import hk.ust.lpxz.statemachine.State;
import hk.ust.lpxz.statemachine.StateVectorGenerator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import edu.hkust.clap.Util;

import soot.jimple.Stmt;
import soot.tagkit.Host;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.SourceLnPosTag;


import Jama.Matrix;

public class InstrumentTaskPlanner {


   
    public static void generateInstrumentationTasks(Matrix dc, Matrix uc, Petri youngPetri, Set<State> states) throws FileNotFoundException {

    	 
    	 List<Place> places_humanRead=StateVectorGenerator.placeTemplate;
    	 List<Transition> transitions_humanRead = StateVectorGenerator.transtemplate;

    	//======================get the header ready now.
    	
    	if(uc.getRowDimension() != dc.getRowDimension())
    		throw new RuntimeException("how many controllers are there?");
    	for(int i=0; i< dc.getRowDimension(); i++)
    	{
    		//out.println("=========controller" + i+ "====");
    	   double[] rowDc = dc.getArray()[i];
    	   double[] rowUc = uc.getArray()[i];
    	   
    	   //out.println("it contains " +  + " tokens initially" );
    	   LockInitializeTask.lockID2Tokens(i, (int)rowUc[0]);
    	   for(int j= 0; j<rowDc.length; j++)
    	   {
    		   if(rowDc[j]!=0)
    		   { 
    			   double tmp = rowDc[j];
    			   if(tmp<0)
    			   {// t gets tokens from lockplace 
    				   Transition t =transitions_humanRead.get(j);
    				   if(youngPetri.getLocalSuccs(t).size()>1 || youngPetri.getLocalPrecs(t).size()>1) throw new RuntimeException("check the PN");
    				   PlaceCommonLocal afterT= (PlaceCommonLocal)youngPetri.getLocalSuccs(t).get(0);    				   
                       PlaceCommonLocal beforeT= (PlaceCommonLocal)youngPetri.getLocalPrecs(t).get(0);
                       
//                       System.out.println("\n" + beforeT.getEnclosingM().getBb().toString());
//    			       String toprint = "gives " + Math.abs(tmp) + " tokens to T between " + beforeT.getJimpleStmt() + " and " + afterT.getJimpleStmt();
    			       LockOperationTask tmpTask = new LockOperationTask(beforeT.getEnclosingM().getBb().getMethod(),
    			    		   beforeT.getJimpleStmt(), afterT.getJimpleStmt(), (int)Math.abs(tmp), i);
    				  LockOperationTasks.registerTask(tmpTask);
    				      			       
    			       
    			   }else {
    				   Transition t =transitions_humanRead.get(j);
    				   if(youngPetri.getLocalSuccs(t).size()>1 || youngPetri.getLocalPrecs(t).size()>1) throw new RuntimeException("check the PN");
    				   PlaceCommonLocal afterT= (PlaceCommonLocal)youngPetri.getLocalSuccs(t).get(0);    				   
                       PlaceCommonLocal beforeT= (PlaceCommonLocal)youngPetri.getLocalPrecs(t).get(0);
       //                System.out.println("\n" + beforeT.getEnclosingM().getBb().toString());
        //               String toprint = "takes " + Math.abs(tmp) + " tokens from T between " + beforeT.getJimpleStmt() + " and " + afterT.getJimpleStmt();
    			       LockOperationTask tmpTask = new LockOperationTask(beforeT.getEnclosingM().getBb().getMethod(),
    			    		   beforeT.getJimpleStmt(), afterT.getJimpleStmt(), (int)Math.abs(tmp) * (-1), i);
    				    LockOperationTasks.registerTask(tmpTask);
    				 
    			   }
    		   }
    	   }
    	   
    	   
    	   
    	   
    	}
    	
    	
    	
		
	}
	
}
