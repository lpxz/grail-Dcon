package hk.ust.lpxz.SBPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import soot.toolkits.scalar.Pair;


import Jama.Matrix;

import hk.ust.lpxz.IO.Reader;
import hk.ust.lpxz.LockSynthesis.planner.InstrumentTaskPlanner;
import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.fixing.Reporter;
import hk.ust.lpxz.linearprogramming.Spyro;
import hk.ust.lpxz.linearprogramming.SpyroHeuristic;
import hk.ust.lpxz.linearprogramming.Yin;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.graph.ICFGPetriBuilder;
import hk.ust.lpxz.petri.graph.PetriVisualizer;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.Transition;

import hk.ust.lpxz.statemachine.State;
import hk.ust.lpxz.statemachine.StateGraphBuilder_syncVio;
import hk.ust.lpxz.statemachine.StateVectorGenerator;

public class SBPI {

    public static void solve(String filename )
    {
    	HashMap<String, Object> retMap = new HashMap<String, Object>();
    	try {
    		Reader.readSBPIFile(new File(filename), retMap);
    		Integer b = (Integer) retMap.get("[b]");
    		Matrix l = (Matrix) retMap.get("[l]");
    		Matrix u0 = (Matrix) retMap.get("[u0]");
    		Matrix D = (Matrix) retMap.get("[D]");
    		
    		Matrix Dm = l.times(D).uminus();
    		Matrix tmp = l.times(u0);
    		
    		double um0 = b.intValue() - tmp.get(0, 0);
    		Dm.print(10, 2);
    		System.out.println(um0);    		
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    }
    
    public static HashSet<State> toremoveNotMin = new HashSet<State>();
    public static void minUnsafeFilter(Set<State> unsafeStates) {
    	toremoveNotMin.clear();
		for(State state : unsafeStates)
		{
			for(State  state2:unsafeStates)
			{
				if(state!=state2&& state.hashCode() > state2.hashCode())
				{
					if(toremoveNotMin.contains(state)||toremoveNotMin.contains(state2))
						continue;
					if(state.oneTokenPlaces.containsAll(state2.oneTokenPlaces))
						toremoveNotMin.add(state);
					else if (state2.oneTokenPlaces.containsAll(state.oneTokenPlaces))
						toremoveNotMin.add(state2);
					else
					{
						
					}
				}
			}
		}
		unsafeStates.removeAll(toremoveNotMin);
		
		
	}

    public static HashSet<State> mustgood = new HashSet<State>();
    public static HashSet<State> maynotgood = new HashSet<State>();
    public static HashSet<State> mustnotgood = new HashSet<State>();
	public static void maxSafeFilter(Set<State> goodStates) {
		mustgood.clear(); maynotgood.clear();mustnotgood.clear();
		for(State good: goodStates)
		{
			if(good.oneTokenPlaces.size()>=2)
				mustgood.add(good);
			else
				maynotgood.add(good);
		}
		for(State tmp:maynotgood)
		{
			for(State tmpmustgood:mustgood)
			{
				if(tmpmustgood.oneTokenPlaces.containsAll(tmp.oneTokenPlaces))
					mustnotgood.add(tmp);
				
			}
		}
		
	

		goodStates.removeAll(mustnotgood);
		
	}
	

    public static long MIPtime = -1;
    public static long SBPItime = -1;
	static Set<State> allStates4solve = new HashSet<State>();
    public static void solve(Set<State> goodstates, Set<State> badStates)
    {
    	allStates4solve.clear();
    	allStates4solve.addAll(goodstates);
    	allStates4solve.addAll(badStates);
    	Petri youngPetri =Petri.getPetri();
    	
    	

    Pair<Matrix, Matrix> pair = new Pair(null, null);
	try {

		if(!DconPropertyManager.useYinSolver)
		{
			StateVectorGenerator.preparePlaceTemplateNonYin(youngPetri, allStates4solve);// place indices
	    	StateVectorGenerator.prepareTranTemplate(youngPetri);// T indices    	
			Spyro.constraintSolve(goodstates, badStates, pair);//	SpyroHeuristic.constraintSolve(marking, pair);
			
		}else {
			StateVectorGenerator.preparePlaceTemplateYin(youngPetri, allStates4solve);// place indices
	    	StateVectorGenerator.prepareTranTemplate(youngPetri);// T indices    	
			Yin.constraintSolve( goodstates, badStates,pair);
		}
//		
		Matrix D= ToMatrix.toMatrix();
		Matrix L =pair.getO1();
		Matrix B = pair.getO2();
		Matrix Dc = L.times(D).uminus();
		Matrix Uc = B;// B.minus(L.times(0)); // initial marking of those places are all zero! domain knowledge

		System.out.println("Dc is: ");	     
    	Dc.print(2, 2);
    	if(DconPropertyManager.addController2Graph)
    	{
    		ICFGPetriBuilder.addControlLogic(Petri.getPetri(), Dc, Uc, allStates4solve);
    		PetriVisualizer.visualizeColorFul(Petri.getPetri(), DconPropertyManager.patchedFile);
    	}
		 Reporter.reportDirectly("MIP ", (int)(SBPI.MIPtime));
		HumanReadable.printHumanInfo(Dc, Uc, Petri.getPetri(), allStates4solve);
	    InstrumentTaskPlanner.generateInstrumentationTasks(Dc, Uc, Petri.getPetri(), allStates4solve);
		
		
		
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
    

	public static void main(String[] args) {
		solve("/home/lpxz/eclipse/workspace/Dcon/src/hk/ust/lpxz/SBPI/SBPI.txt");
		
	}

}
