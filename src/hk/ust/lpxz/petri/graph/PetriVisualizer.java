package hk.ust.lpxz.petri.graph;

import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.GadaraSupport.CommNamer;
import hk.ust.lpxz.petri.graph.violation.Violation;
import hk.ust.lpxz.petri.graph.violation.ViolationManager;
import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.ArcCall;
import hk.ust.lpxz.petri.unit.ArcFromController;
import hk.ust.lpxz.petri.unit.ArcReturn;
import hk.ust.lpxz.petri.unit.ArcToController;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
import hk.ust.lpxz.petri.unit.PlaceControl;
import hk.ust.lpxz.petri.unit.Transition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class PetriVisualizer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	

	}

	
	public static void visualize(Petri csGraph, String fileName) { 
		if(DconPropertyManager.visualizePic)			
		{
			File file = new File(fileName);
			if(!file.exists())
			{
				if(!file.getParentFile().exists())
				{file.getParentFile().mkdirs();}
				try {
					file.createNewFile();
				} catch (IOException e) {
					// LPXZ Auto-generated catch block
					e.printStackTrace();
				}
			}
			File detailsfile = new File(fileName+"_details");
			FileWriter fw;
			FileWriter detailsFW;
			
		
			try {
				fw = new FileWriter(file);	
				detailsFW = new FileWriter(detailsfile);
				export4PetriNet(fw, csGraph, detailsFW);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
		
	
	public static void export4PetriNet(Writer writer, Petri g, FileWriter detailsFW)
    {
        PrintWriter out = new PrintWriter(writer);
        PrintWriter detailsOut= new PrintWriter(detailsFW);
        String indent = "  ";
        String connector;
            out.println("digraph G {");
            connector = " -> ";

        for (Object v : g.coreG.vertexSet()) {
        	String petriNetName =  CommNamer.getPetriNetName(v);

        	
        	logDetails(detailsOut,v, petriNetName);

            out.print(indent +petriNetName);            
            if(v instanceof Transition)// two special cases for the node shape or color
            {
            	 out.print(
                         " [shape = box]");
            }
           
           
            
            out.println(";");
        }

        for (Object e : g.coreG.edgeSet()) {
            String source = CommNamer.getPetriNetName(g.coreG.getEdgeSource((Arc)e));
            String target = CommNamer.getPetriNetName(g.coreG.getEdgeTarget((Arc)e));


            out.print(indent + source + connector + target);     
            if(e instanceof ArcCall || e instanceof ArcReturn)// special case for the invoking edge
            {
            	out.print(
                " [style = bold]");
            }
            
            out.println(";");
        }

        out.println("}");
        out.flush();
        detailsOut.flush();

    }

	
		
	
	public static void export4PetriNetColorful(Writer writer, Petri g, FileWriter detailsFW)
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
        	String petriNetName =  CommNamer.getPetriNetName(v);
        	 out.print(indent +petriNetName);      
            if(v instanceof Place)
            {
            	String color=decideColor((Place)v);
            	if(color!=null && !color.equals(""))
            	{            	
            		out.print(
                            " [style=filled,color="+ color+ "]");
            	}            	
            }
        	
        	
        	if(!DconPropertyManager.trialPatcher)
        	   logDetails(detailsOut,v, petriNetName);
        	

                
            if(v instanceof Transition)// two special cases for the node shape or color
            {
            	 out.print(
                         " [shape = box]");
            }
            
            
            out.println(";");
        }

        for (Object e : g.coreG.edgeSet()) {
            String source = CommNamer.getPetriNetName(g.coreG.getEdgeSource((Arc)e));
            String target = CommNamer.getPetriNetName(g.coreG.getEdgeTarget((Arc)e));


            out.print(indent + source + connector + target);     
//            if(e instanceof ArcCall || e instanceof ArcReturn)// special case for the invoking edge
//            {
//            	out.print(
//                " [style = bold]");
//            }
            if(e instanceof ArcFromController || e instanceof ArcToController )
            {
            	Arc arce = (Arc)e;
            	out.print(
                " [style = bold,label = "+arce.getLabel()+ "]");//label="n"
            }
            
            out.println(";");
        }

        out.println("}");
        out.flush();
        detailsOut.flush();

    }

	static String color4PC = "black";
	static String color4Vio = "red";
	static String color4MultipleVio = "blueviolet";
	static Set<Place>  tmp4decideColor = new HashSet<Place>();
	private static String decideColor(Place v) {
		
	    
		
		if(v instanceof PlaceControl)
		{
			return color4PC;
		}
		else
		{
			String toret = null;
			Set<Violation> vios =ViolationManager.getAllViolations();
			for(Violation vio: vios)
			{
				tmp4decideColor.clear();
				vio.includedPlaces(tmp4decideColor);
				if(tmp4decideColor.contains(v))// update color
				{
					if(toret ==null) toret = color4Vio;
					else if(toret.equals(color4Vio))
						toret = color4MultipleVio;
					else
						;
				}
			}
			return toret;
		
		}
		
		
	}


	private static void logDetails(PrintWriter detailsOut, Object v, String petriNetName) {
    	//if(detailsOut!=null)
		{
    		if(v instanceof PlaceCommonLocal)
    		{
    			
    			
    				String jimple  = ((PlaceCommonLocal)v).getJimpleStmt().toString();
        			detailsOut.println(petriNetName + " ----> " + jimple);
    			
    			
    		}
		  
		}
		
	}

	// Do not maintain two set of counters, you will find different names for the same place!
//	public static int curCounter = 0;
//	public static HashMap<Integer, Integer> hashcode2counter =new HashMap<Integer, Integer>();
//	public static  int fakeHashCode(int hashcode)
//	{
//		if(hashcode2counter.get(hashcode)==null)
//		{
//			hashcode2counter.put(hashcode, curCounter++);
//		}
//		return hashcode2counter.get(hashcode).intValue();
//	}

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


	public static void visualizeColorFul(Petri youngPetri, String fileName) {
		 

		File file = new File(fileName);
		File detailsfile = new File(fileName+"_details");
		FileWriter fw;
		FileWriter detailsFW;
		
	
		try {
			fw = new FileWriter(file);	
			detailsFW = new FileWriter(detailsfile);
			export4PetriNetColorful(fw, youngPetri, detailsFW);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}
		
	

}
