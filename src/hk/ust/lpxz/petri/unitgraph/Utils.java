package hk.ust.lpxz.petri.unitgraph;



import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pldi.region.HRG;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.JimpleBody;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.visitor.Visitor;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.IRegion;
import soot.toolkits.graph.pdg.PDGNode;
import soot.util.Chain;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphConstants;

public class Utils {
	
	public static final String DOTGRAPHDIR = "dotgraph";
	protected static final String BODYPOSTFIX = ".body";
	private static final String NAME2NODEPOSTFIX = ".name2node";
	private static final String REGION = "Region";
	private static final String BLOCK = "Block";
	private static final String DirectGraphPostFix = ".directg";
	private static final String REGIONGRAPHPOSTFIX = ".regiong";
	private static final String DEPGRAPHPOSTFIX = "depg";

	public static int counter=0;
	public static Map visited = new HashMap();



	public static void drawDirectedGraphNBody(DirectedGraph graph, Body bb, String filename)
	{
		drawDirectedGraph( graph,  filename);
		 Utils.generateBody(bb, "./"+Utils.DOTGRAPHDIR+"/"+ filename+ Utils.BODYPOSTFIX);
	}
	
	public static void drawDirectedGraph(DirectedGraph graph, String filename)
	{		
		String plot2 = "./"+Utils.DOTGRAPHDIR+"/"+ filename+ Utils.DirectGraphPostFix;
		DirectedGraphToDotGraph dg2dg = new  DirectedGraphToDotGraph();
		DotGraph dg =dg2dg.drawDG(graph, plot2);// interceptor
		dg2dg.generateName2Node("./"+DOTGRAPHDIR+"/"+ filename+ NAME2NODEPOSTFIX);// store the mapping
		dg.plot(plot2);
//		dg.generateName2Node("./dotgraph/"+ filename+".name2node");
	}
	
	

	
	//===============================================
	
	// draw: control flow edge + control depedency graph
	public static void drawDepGraphNBody(HashMutablePDG pdg, Body bb, String filename) {
		//		System.out.println("plaese compare!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		drawDepGraph(pdg,filename) ;
		Utils.generateBody(bb, "./"+Utils.DOTGRAPHDIR+"/"+ filename+ Utils.BODYPOSTFIX);		
	}

	// the following two are same...use 2, it is my version
	public static void drawDepGraph(HashMutablePDG pdg, String filename) {
		String plot2 ="./"+Utils.DOTGRAPHDIR+"/"+ filename+ Utils.DEPGRAPHPOSTFIX;
		DirectedGraphToDotGraph dg2dg = new  DirectedGraphToDotGraph();
		DotGraph dg =dg2dg.drawDepGraph(pdg,plot2);
		dg2dg.generateName2Node("./"+DOTGRAPHDIR+"/"+ filename+ NAME2NODEPOSTFIX);// store the mapping
		dg.plot(plot2);
	}
	//=================================
	public static void drawRegionGraphNBody(HashMutablePDG pdg, Body bb, String filename) {
		//		System.out.println("plaese compare!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		drawRegionGraph(pdg,filename) ;
		generateName2Node("./"+DOTGRAPHDIR+"/"+ filename+ NAME2NODEPOSTFIX);
		Utils.generateBody(bb, "./"+Utils.DOTGRAPHDIR+"/"+ filename+ Utils.BODYPOSTFIX);		
	}


	private static void generateName2Node(String filename) {
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			
			//Iterator keyIt =name2node.keySet().iterator();
			Iterator keyIt=  visited.keySet().iterator();
			while (keyIt.hasNext()) {
				Object node = (Object) keyIt.next();
                String name = getNameOfRegionBlock(node);
                
				out.write("name: "+ name + "\n");
				out.write(node.toString());
				
				out.write("\n\n\n");
				
			}
			
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}

	public static void drawRegionGraph(HashMutablePDG pdg, String filename) {
	    String plot2 = "./"+Utils.DOTGRAPHDIR+"/"+ filename+ Utils.REGIONGRAPHPOSTFIX;
	    DotGraph canvas = new DotGraph(plot2);
	    canvas.setGraphLabel(plot2);
	    boolean onePage =true;
	    if (!onePage) {
	      canvas.setPageSize(8.5, 11.0);
	    }
	    boolean isBrief =true;
	    if (isBrief) {
	      canvas.setNodeShape(DotGraphConstants.NODE_SHAPE_CIRCLE);
	    } else {
	      canvas.setNodeShape(DotGraphConstants.NODE_SHAPE_BOX);
	    }
	    visited.clear();
	   List nodes= pdg.getNodes();
	   
	   for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
		   PDGNode start = (PDGNode) iterator.next();	
			if(start.m_type == PDGNode.Type.REGION)
			{
				IRegion startr=(IRegion)start.m_node;
				if(!visited.containsKey(startr))
				{
					visited.put(startr, "");
					dfs( startr, canvas);//forest style
			
				}
			}
			else if(start.m_type == PDGNode.Type.CFGNODE){
				Block bb = (Block)start.m_node;
				String name = BLOCK+  bb.getIndexInMethod();
				canvas.drawNode(name);
				// throw new RuntimeException();
				//return;// no children, no need to deep into it
			}
			else {
				throw new RuntimeException();
			}
			
		}	    
	  
	   canvas.plot(plot2);	
	}
     
	//==================================
	
	




	private static void dfs(Object start, DotGraph canvas) {
		// recursiion relatinoship changes
		//List l =pdg.getSuccsOf(start);
		List l= new  ArrayList();
        if(start instanceof IRegion)// if block, no need to expand, draw at the children side
        {
         	IRegion  istart = (IRegion) start;
         	l.addAll(istart.getChildRegions());
         	l.addAll(istart.getBlocks());
        }
        
		String startName= getNameOfRegionBlock(start);
		canvas.drawNode(startName);
		
		for (Iterator iterator = l.iterator(); iterator.hasNext();) {
			Object child =  iterator.next();
			if(!visited.keySet().contains(child))
			{
				visited.put(child, "");
				String childName= getNameOfRegionBlock(child);
				canvas.drawNode(childName);
			//	if(startName.contains(REGION) )
				{
					System.out.println(startName + "->" + childName);
					canvas.drawEdge(startName, childName);
				}
				
				dfs( child, canvas);
			}
			else { // multipel edge!, although the endpoint visigted
				String childName= getNameOfRegionBlock(child);
				canvas.drawNode(childName);
			//	if(startName.contains(REGION))
				{
					System.out.println(startName + "->" + childName);
					canvas.drawEdge(startName, childName);
				}
			}
		}
		
		
	}

	private static String getNameOfRegionBlock(Object node) {
		

    	if(node instanceof IRegion)
    	{
    		
    			return REGION+  ((IRegion)node).getID();
    		
    		
    	}
    	else if (node instanceof Block) {
    		
    			return  BLOCK+  ((Block)node).getIndexInMethod();
    		
		}
    	else {
			throw new RuntimeException();
		}
    	
//      Integer index = (Integer)this.get(node);
//      if (index == null) {
//	index = new Integer(nodecount++);
//	this.put(node, index);
//      }
//      return index.toString();
    
	}

	public static Set getLocals(String classfilter, String methodfilter, String  varfilter)
	{
		Set<Local> localS= new HashSet<Local>();
		Chain<SootClass> classes = Scene.v().getApplicationClasses();
		Iterator<SootClass> classesIt = classes.iterator();
		while (classesIt.hasNext()) {
			SootClass sootClass = (SootClass) classesIt.next();
		    if(classfilter!=null && !sootClass.toString().contains(classfilter))
		    {
		    	continue;		    	
		    }
			List<SootMethod> methods = sootClass.getMethods();
			Iterator<SootMethod> methodIt = methods.iterator();
			while (methodIt.hasNext()) {
				SootMethod sootMethod = (SootMethod) methodIt.next();
				if(methodfilter!=null && !sootMethod.toString().contains(methodfilter))
				{
					continue;
				}
				if (sootMethod.isAbstract())
					continue;
				if (sootMethod.isNative())
					continue;
				
			    Visitor.thisClass = sootMethod.getDeclaringClass();
			    if(!sootMethod.hasActiveBody()) continue;
				Body body = sootMethod.getActiveBody();
				Chain units = body.getUnits();

				
				Iterator stmtIt = units.snapshotIterator();
				while (stmtIt.hasNext()) {
					Stmt s = (Stmt) stmtIt.next();
					List useanddefL =s.getUseAndDefBoxes();
				    for (Iterator iterator = useanddefL.iterator(); iterator
							.hasNext();) {
						ValueBox object = (ValueBox) iterator.next();
						Value v = object.getValue();
						if(varfilter!=null && !v.toString().equals(varfilter))
						{
							continue;
						}
						else {
							if(v instanceof Local)
							{
								localS.add((Local)v);
								System.out.println(sootClass.getName()+ sootMethod.getName()+ s);
							}
							
						}
						
					}
				}
			
				

			}
		}
		return localS;

	}
	
	
	
	public static  Stmt getFirstNonIdentityStmt(JimpleBody body)
	{
		return body.getFirstNonIdentityStmt();
		
	}
    public static Stmt getStmtBeforeRet(JimpleBody body)
    {
    	Iterator<Unit> it = body.getUnits().iterator();
        Object o = null;
        Unit last = null;
        Unit cur=null;
        while (it.hasNext()) {
        	 last= cur;
			 cur = it.next();
			 if(cur instanceof ReturnStmt || cur instanceof ReturnVoidStmt)
			 {
				 break;
			 }
			
		}
        
        if (last == null)
            throw new RuntimeException("the one before return is null?!");
        return (Stmt)last;
    }
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}









	public static void generateBody(Body bb, String filename) {

		  
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			
			out.write(bb.toString());
	         
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}


















	
}
