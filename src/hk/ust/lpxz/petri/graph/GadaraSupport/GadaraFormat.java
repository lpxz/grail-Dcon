package hk.ust.lpxz.petri.graph.GadaraSupport;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hk.ust.lpxz.IO.Reader;
import hk.ust.lpxz.IO.Writer;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.ArcCall;
import hk.ust.lpxz.petri.unit.ArcFromResource;
import hk.ust.lpxz.petri.unit.ArcLocal;
import hk.ust.lpxz.petri.unit.ArcToResource;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
import hk.ust.lpxz.petri.unit.PlaceResource;
import hk.ust.lpxz.petri.unit.Transition;

public class GadaraFormat {

	// two things to do: gadara input, dcon output place name """""
	public static void toGadara(Petri g , String filename )
	{
		if(!filename.endsWith(".pn"))
		{
			throw new RuntimeException("Gadara requires .pn file!");
		}
		String togadara = toGadara(g);
		Writer.write2File(togadara, filename);		
	}

	public static String toGadara(Petri g)
	{
		if(!g.initialized)
		{
			throw new RuntimeException("it is not a good time to dump the gadara, initialize it first");
		}
		StringBuffer sb = new StringBuffer();

		
	    for (Object v : g.coreG.vertexSet()) {
        	
        	if(v instanceof Place)// maybe transition
        	{
        		Place tmp = (Place)v;
        		String name = tmp.getPetriName();
        		String typeName = tmp.getGadaraType();
        		int intialTokens = tmp.getGadaratokens();
        		//System.out.println(name);
        		sb.append("P" + " "+  name + " " + typeName + " " + intialTokens + "\n");            	
        	}
        	else if(v instanceof Transition){
        		Transition tmp = (Transition)v;
        		String name = tmp.getPetriName();
        		boolean controllable = tmp.isGadara_controllable();
        		boolean observable = tmp.isGadara_observable();
        		sb.append("T" + " "+name + " " + controllable + " " + observable + "\n" );       		
			}
        }

        for (Object e : g.coreG.edgeSet()) {
        	Arc arc = (Arc)e;
        	String name = arc.getPetriName();
        	
        	int weight = arc.getWeight(); // initially, all set as 1      	
        	
        	
        	String fromnode =  getPetriName(g.coreG.getEdgeSource((Arc)e));
        	String tonode = getPetriName(g.coreG.getEdgeTarget((Arc)e));        	
        	sb.append("A" + " "+ fromnode + " " + tonode + " " + weight+"\n");
        	

        }     
        return sb.toString();		
	}
	
	
	private static String getPetriName(Object tmp) {
		if(tmp instanceof Place)
		{
			return ((Place)tmp).getPetriName();
		}
		else if ( tmp instanceof Transition) {
			return ((Transition)tmp).getPetriName();
		}
		else {
			throw new RuntimeException("what type is ti?");
		}
		
		
	}
	

	


	
	static HashMap<String, Object> name2instance = new HashMap<String, Object>();
	
	// help checking the toGadara's branch handling capability!, may modify the gadara file to addd the branch
//	public static YoungPetri fromGadara(String filename ) 
//	{
//		System.err.println("Warning:");
//		System.err.println(" 1 lose the context information, method info, jstmt, of course, the methodEntry, methodExit places");
//		System.err.println("2 for the lockPlace, lose we lose the lockobject and array attribute,");
//		System.err.println("3 we lose the arcCall and arcReturn edges.  i.e., we lose almost all the program information");
//
//		name2instance.clear();
//		YoungPetri graph = new YoungPetri();
//		File file = new File(filename);
//		List<String> goods = new ArrayList<String>();
//	     try {
//			Reader.readLineByLine(file, goods);
//			
//			for(String line:goods)
//			{
//				if(line.startsWith("P"))
//				{
//					Place cls =createPlace(graph,line);
//					name2instance.put(cls.getPetriName(), cls);
//					graph.coreG.addVertex(cls);
//				}
//				else if (line.startsWith("T")) {
//					Transition t = createTransition(graph,line);
//					name2instance.put(t.getPetriName(), t);
//					graph.coreG.addVertex(t);
//				}
//				else if(line.startsWith("A")){
//					createArc(graph,line);// add to G inside
//				}
//				else {
//					throw new RuntimeException("what is this?");
//				}			
//				
//			}
//			
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return graph;
//	}

//	private static void createArc(YoungPetri graph, String line) {
//		//arc:        A      from_node  to_node weight 
//		 String tt[] = line.split("\\s{1,}"); 
//		 String fromString = tt[1];
//		 String toString = tt[2];
//		 String weiString = tt[3];
//		 
//		 Object from = name2instance.get(fromString);
//		 Object to = name2instance.get(toString);
//		 
//		 if(from instanceof PlaceResource)
//		 {
//			 graph.coreG.addEdge_edgetype_lpxz(from, to, ArcFromResource.class);
//		 }
//		 else if (to instanceof PlaceResource) {
//			 graph.coreG.addEdge_edgetype_lpxz(from, to, ArcToResource.class);
//		 }
//		 else {
//			 graph.coreG.addEdge_edgetype_lpxz(from, to, ArcLocal.class);
//		}		 
//		 
//		// return graph.coreG.getEdge(from, to) ; 
//	}
//
//	private static Transition createTransition(YoungPetri graph, String line) {
//		//	trans:      T      transition_name  controllable?  observable?
//		 String tt[] = line.split("\\s{1,}"); 
//		 String name = tt[1];
//		 String contString = tt[2];
//		 String obsString = tt[3];
//		 boolean controllable = contString.equals("true");
//		 boolean observable = obsString.equals("false");
//		 
//		 Transition t = new Transition();
//		 t.setPetriName(name);
//		 t.intialize(controllable, observable);
//		 
//		 return t ;
//		 
//		 
//	}
//
//	// 1 lose the context information, method info, jstmt, of course, the methodEntry, methodExit places
//	// 2 for the lockPlace, we lose the lockobject and array attribute,
//	// 3 we lose the arcCall and arcReturn edges. 
//	// i.e., we lose almost all the program information
//	
//
//	private static Place createPlace(YoungPetri graph, String line) {
//		//place:      P      place_name  type  init_num_tokens
//	    String tt[] = line.split("\\s{1,}"); 
//		String name = tt[1];
//		String type = tt[2];
//		String numberOftokens = tt[3];
//		int tokeNum = Integer.parseInt(numberOftokens);
//		
//		Place toret  = null;
//		if(type.equals(GadaraNotation.LockPlaceType))
//		{
//			toret  = new PlaceResource();
//		}
//		else {
//			toret = new PlaceCommonLocal();
//		}
//
////		toret.setCtxts(null);
////		toret.setMsig(null);
////		toret.setJimpleStmt(null);	
//		toret.setPetriName(name);
//		toret.intialize(type, tokeNum);
//		
//		return toret;
//	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//           String string  = "aa    aa b";
//       	String tt[] = string.split("\\s{1,}"); 
//       	for(String str:tt)
//       	{
//       		System.out.println(str);
//       	}
		
		//String gadaraFile = "/home/lpxz/eclipse/workspace/Dcon/src/hk/ust/lpxz/petri/graph/GadaraSupport/test";
          System.out.println("xx".hashCode());
          String jiakuohao =  "\"" + "xx" + "\"";
          System.out.println(jiakuohao.hashCode());
		
	}

}
