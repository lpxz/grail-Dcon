package hk.ust.lpxz.petri.graph.GadaraSupport;

import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.ArcCall;
import hk.ust.lpxz.petri.unit.ArcReturn;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
import hk.ust.lpxz.petri.unit.PlaceControl;
import hk.ust.lpxz.petri.unit.PlaceMethodEntry;
import hk.ust.lpxz.petri.unit.PlaceMethodExit;
import hk.ust.lpxz.petri.unit.PlaceResource;
import hk.ust.lpxz.petri.unit.Transition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;

import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;

public class GadaraNamer {
//BE consistent with @YoungPetriVisualizer
//@Depreciated!

  
	
	public static void assignName( Petri g)
    {
		
        for (Object v : g.coreG.vertexSet()) {
        	
        	String petriNetName =  getGadaraName(v);
        	
        	

//            if(g.getThreadRoots().contains(v))
//            {
//            	
//            }
        }

        for (Object e : g.coreG.edgeSet()) {
        	Arc arc = (Arc)e;
        	String petriNetName =  getGadaraName(arc);
        	arc.setPetriName(petriNetName);
        	
        	
//            String source = getPetriNetName(g.coreG.getEdgeSource((Arc)e));
//            String target = getPetriNetName(g.coreG.getEdgeTarget((Arc)e));

        }

      

    }

	
	public static int curCounter = 0;
	public static HashMap<Integer, Integer> hashcode2counter =new HashMap<Integer, Integer>();
	public static  int fakeHashCode(int hashcode)
	{
		if(hashcode2counter.get(hashcode)==null)
		{
			hashcode2counter.put(hashcode, curCounter++);
		}
		return hashcode2counter.get(hashcode).intValue();
	}

	// for all kinds of objects in the petri net
	public static String getGadaraName(Object v) {
		
		String core = "";
		// do not use the jimple code directly, ``return'' would be shared
	if(v instanceof PlaceMethodEntry)
	{
		String msig =((PlaceMethodEntry)v).getEnclosingM().getMsig();
		String mname =getmName(msig);
		//System.out.println(mname);
		core="Entry_" + mname + fakeHashCode(v.hashCode());//v.hashCode(); "Entry_" +removeYinhao(msig); //
	}
	else if(v instanceof PlaceMethodExit)
	{
		String msig =((PlaceMethodExit)v).getEnclosingM().getMsig();
		String mname =getmName(msig);
		core = "Exit_" + mname+fakeHashCode(v.hashCode()); //"Exit_" +removeYinhao(msig);//
	}
	

	else if(v instanceof PlaceCommonLocal){		
		// the same jimple may have mulitple places under different contexts.
		// do not merge because of just the name!
		Stmt st = ((PlaceCommonLocal)v).getJimpleStmt();
		core =   "S" + fakeHashCode((st.hashCode()+v.hashCode()));//removeYinhao(st.toString())  ;// ;// I am not interested of the type
	}
    else if ( v instanceof PlaceResource) {
    	//PlaceResource pr = new PlaceResource(origLock ,arrayEle);
    	// use the identity of the 
    	// Do not use the JimpleLocal's hashcode, it is fixed for all locals.
	   int x =((PlaceResource)v).hashCode();
	   core =  "Lock" + fakeHashCode(x);//removeYinhao( ((ConcreteStatement)v).getJimpleStmt().toString());//

	}
	else if (v instanceof Transition) {
		core = "T" +  fakeHashCode(v.hashCode());;
	}
	else if(v instanceof Arc) {
		core = "Arc" + fakeHashCode(v.hashCode());
	}
	else if(v instanceof PlaceControl){
		 int x =((PlaceControl)v).hashCode();
		   core =  "Con_" + fakeHashCode(x) + "_" + ((PlaceControl)v).getGadaratokens();//removeYinhao( ((ConcreteStatement)v).getJimpleStmt().toString());//
	}	
	else
	{
		throw new RuntimeException("what type?");
	}
	
		if(core.contains("$"))
		{
			return core.replace("$", "_dao_");
			
		}
		else
			return core;
	}


	private static String removeYinhao(String string) {
		return string.replace('"', ' ');

	}


	private static String getmName(String msig) {
		//<example.ExampleThread: void run()>
		int maohao =msig.indexOf(':');
		int lbrace = msig.indexOf('(');
		msig = msig.substring(maohao+1, lbrace);
		int lastBlank = msig.lastIndexOf(' ');		
		
		return msig.substring(lastBlank+1);
	}
		
	


	public static void main(String[] args) {
	 if("ss$_".contains("$"))
	 {
		System.out.println("ss$_".replace("$", "_dao_"));
	 }

	}
	
	

}
