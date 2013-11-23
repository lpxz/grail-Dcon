package hk.ust.lpxz.statemachine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.graph.GadaraSupport.GadaraNamer;
import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.ArcCall;
import hk.ust.lpxz.petri.unit.ArcLocal;
import hk.ust.lpxz.petri.unit.ArcReturn;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
import hk.ust.lpxz.petri.unit.PlaceResource;
import hk.ust.lpxz.petri.unit.Transition;

import org.jgrapht.graph.DirectedPseudograph;

public class StateGraph {

	
	public State root =  null;
	public State getRootOrEnd() {
		return root;
	}

	public void setRoot(State rootOrEnd) {
		this.root = rootOrEnd;
	}
	// the graph wont change!!
	public Set<State> allStates  = null;
	public Set<State> allStates()
	{
		if(allStates !=null)
		{
			return allStates;
		}
		else {
			allStates = new HashSet<State>();
			for(Object o : this.coreG.vertexSet())
			{
				allStates.add((State)o);
			}
			return allStates;
		}
		
	}
	public DirectedPseudograph<Object, StateTransition> coreG = null;
	public StateGraph()
	{
		
		coreG = new DirectedPseudograph<Object, StateTransition>(
				StateTransition.class);
	}
	
	public static void visualize(StateGraph csGraph, String fileName) { 

		File file = new File(fileName);
		File detailsfile = new File(fileName+"_details");
		FileWriter fw;
		FileWriter detailsFW;
		
	
		try {
			fw = new FileWriter(file);	
			detailsFW = new FileWriter(detailsfile);
			export4StateGraph(fw, csGraph, detailsFW);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public static void export4StateGraph(Writer writer, StateGraph g, FileWriter detailsFW)
    {
        PrintWriter out = new PrintWriter(writer);
        PrintWriter detailsOut= new PrintWriter(detailsFW);
 
        
        String indent = "  ";
        String connector;
        

       // if (g instanceof DirectedGraph) {
            out.println("digraph G {");
            connector = " -> ";
//        } else {
//            out.println("graph G {");
//            connector = " -- ";
//        }

        for (Object v : g.coreG.vertexSet()) {
        	String petriNetName =  getNameForState(v);
        	
        	
        	logDetails(detailsOut,v, petriNetName);

            out.print(indent +petriNetName);            
            out.println(";");
        }

        for (Object e : g.coreG.edgeSet()) {
            String source = getNameForState(g.coreG.getEdgeSource((StateTransition)e));
            String target = getNameForState(g.coreG.getEdgeTarget((StateTransition)e));
            out.print(indent + source + connector + target);             
            out.println(";");
        }

        out.println("}");
        out.flush();
        detailsOut.flush();

    }
	
	private static String getNameForState(Object v) {
		if(v instanceof State)
		{
			State state = (State)v;
            return state.toString();
		}
		else {
			return null;
		}
		
		
	}

	private static void logDetails(PrintWriter detailsOut, Object v, String petriNetName) {
    	//if(detailsOut!=null)
		{
    		if(v instanceof State)
    		{
    			
    			detailsOut.println(petriNetName + " ----> " + ((State)v).toString());
    		}
		  
		}
		
	}
	
//	public static StateGraph _smGraph = null;
//	public static StateGraph getStateGraph()// singleton pattern
//	{
//		if(_smGraph==null)
//		{
//			_smGraph = new StateGraph();
//			return _smGraph;
//		}else {
//			return _smGraph;
//		}
//				
//		
//	}

	public List getSuccs(Object pop) {
		List list = new ArrayList();
		Set<StateTransition> edges  =this.coreG.outgoingEdgesOf(pop);
		for(StateTransition edge : edges)
		{
			
			{ list.add(edge.getTarget());}
		}			
		return list;
	}
	
	public List getSuccTrans(Object pop) {
		List list = new ArrayList();
		Set<StateTransition> edges  =this.coreG.outgoingEdgesOf(pop);
		for(StateTransition edge : edges)
		{
			
			{ list.add(edge);}
		}			
		return list;
	}
	

	

	
	public List getPrecs(Object pop)
	{
		List list = new ArrayList();
		Set<StateTransition> edges  =this.coreG.incomingEdgesOf(pop);
		for(StateTransition edge : edges)
		{
			
			{ list.add(edge.getSource());}
		}			
		return list;
	}
	
	public List getPrecArcs(Object pop)
	{
		List list = new ArrayList();
		Set<StateTransition> edges  =this.coreG.incomingEdgesOf(pop);
		for(StateTransition edge : edges)
		{
			
			{ list.add(edge);}
		}			
		return list;
	}
	
	
	public static void main(String[] args) {
		
		
	}

}
