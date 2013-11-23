package hk.ust.lpxz.SBPI;

import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.Petri;
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
import java.util.List;
import java.util.Set;

import Jama.Matrix;

public class HumanReadable {


    static PrintStream out = null; 
    public static void printHumanInfo(Matrix dc, Matrix uc, Petri youngPetri, Set<State> states) throws FileNotFoundException {
		//===================== prepare the header:
    	 FileOutputStream os = new FileOutputStream(DconPropertyManager.humanFile);
    	 out = new PrintStream(os);
    	 
    	 List<Place> places_humanRead=StateVectorGenerator.placeTemplate;
    	 List<Transition> transitions_humanRead = StateVectorGenerator.transtemplate;

    	//======================get the header ready now.
    	
    	if(uc.getRowDimension() != dc.getRowDimension())
    		throw new RuntimeException("how many controllers are there?");
    	for(int i=0; i< dc.getRowDimension(); i++)
    	{
    		out.println("=========controller" + i+ "====");
    	   double[] rowDc = dc.getArray()[i];
    	   double[] rowUc = uc.getArray()[i];
    	   
    	   out.println("it contains " + rowUc[0] + " tokens initially" );
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
                       
                    //   System.out.println("\n" + beforeT.getEnclosingM().getBb().toString());
    			       String toprint = "gives " + Math.abs(tmp) + " tokens to T between " + beforeT.getJimpleStmt() + " and " + afterT.getJimpleStmt();
    			    //   System.out.println(toprint);
    				   out.println(toprint );
    				   //printControlledPlace(t);

    			       
    			       
    			   }else {
    				   Transition t =transitions_humanRead.get(j);
    				   if(youngPetri.getLocalSuccs(t).size()>1 || youngPetri.getLocalPrecs(t).size()>1) throw new RuntimeException("check the PN");
    				   PlaceCommonLocal afterT= (PlaceCommonLocal)youngPetri.getLocalSuccs(t).get(0);    				   
                       PlaceCommonLocal beforeT= (PlaceCommonLocal)youngPetri.getLocalPrecs(t).get(0);
                  //     System.out.println("\n" + beforeT.getEnclosingM().getBb().toString());
                       String toprint = "takes " + Math.abs(tmp) + " tokens from T between " + beforeT.getJimpleStmt() + " and " + afterT.getJimpleStmt();
    			   //    System.out.println(toprint);
    				   out.println(toprint );
    				   // printObservedPlace(t);
    			   }
    		   }
    	   }			  
    	   out.println("==========================\n");
    	      	   
    	}
    	
    	
    	
		
	}
	
	private static void printControlledPlace(Transition t) {

		
	   Petri youngPetri =Petri.getPetri();
	  
	   List<Place> ss = youngPetri.getAllSuccs(t);
	 
	   for(Place p: ss)
	   {
		   if(!(p instanceof PlaceResource) && !(p instanceof PlaceControl))
		   {
              if(p instanceof PlaceCommonLocal)
              {
            	  String msigStmtLine = ((PlaceCommonLocal) p).getMsigStmtInLoading();             	
                  out.print(" " +p.getPetriName() + " " + msigStmtLine);// sometimes, the later one, often the goto, has a wrong line!
                  
              }
              else if(p instanceof PlaceMethodEntry){
  				out.print("entry@" + p.getEnclosingM().getMsig());
  			  }else if (p instanceof PlaceMethodExit) {
  				  out.print("exit@" + p.getEnclosingM().getMsig());
  			  }else {
  				 throw new RuntimeException("what type ?" + p.getClass());
  			  }
		   }
		  
	   }
	   out.println();
	
		
	}
	
	private static void printObservedPlace(Transition t) {

	
	   Petri youngPetri =Petri.getPetri();
	   List<Place> ps = youngPetri.getAllPrecs(t);
	  
	   for(Place p: ps)
	   {
		   if(!(p instanceof PlaceResource) && !(p instanceof PlaceControl))
		   {
              if(p instanceof PlaceCommonLocal)
              {
            	  String msigStmtLine = ((PlaceCommonLocal) p).getMsigStmtInLoading();             	
                  out.print(" " +p.getPetriName() + " " + msigStmtLine);// sometimes, the later one, often the goto, has a wrong line!
              }
              else if(p instanceof PlaceMethodEntry){
				out.print("entry@" + p.getEnclosingM().getMsig());
			  }else if (p instanceof PlaceMethodExit) {
				  out.print("exit@" + p.getEnclosingM().getMsig());
			  }else {
				 throw new RuntimeException("what type ?" + p.getClass());
			  }  
		   }
		  
	   }
	   
	
	   out.println();
	
		
	}

	
	
//	  public static void printHumanInfo(Matrix dc, Matrix uc, YoungPetri youngPetri, Set<State> states) throws FileNotFoundException {
//			//===================== prepare the header:
//	    	 FileOutputStream os = new FileOutputStream(Option.humanFile);
//	    	 out = new PrintStream(os);
//	    	places_humanRead.clear(); 
//	    	ToMatrix.preparePlaceTemplate(youngPetri, states, places_humanRead);
//	    	Set<Transition> involvedTs = new HashSet<Transition>();// small trick, according to SBPI, we do not need to care about some transitions, which have nothing to do the above places. the controller will not connect to them
//	    	for(Place p:places_humanRead)
//	    	{
//	    		involvedTs.addAll(youngPetri.getAllPrecs(p));
//	    		involvedTs.addAll(youngPetri.getAllSuccs(p));
//	    	}
//	    	transitions_humanRead.clear();
//	    	ToMatrix.prepareTranTemplate(youngPetri, involvedTs, transitions_humanRead);
//	    	//======================get the header ready now.
//	    	
//	    	if(uc.getRowDimension() != dc.getRowDimension())
//	    		throw new RuntimeException("how many controllers are there?");
//	    	for(int i=0; i< dc.getRowDimension(); i++)
//	    	{
//	    		out.println("=========controller" + i+ "====");
//	    	   double[] rowDc = dc.getArray()[i];
//	    	   double[] rowUc = uc.getArray()[i];
//	    	   
//	    	   out.println("it contains " + rowUc[0] + " tokens initially" );
//	    	   for(int j= 0; j<rowDc.length; j++)
//	    	   {
//	    		   if(rowDc[j]!=0)
//	    		   {
//	    			   double tmp = rowDc[j];
//	    			   if(tmp<0)
//	    			   {
//	    				   Transition t =transitions_humanRead.get(j);
//	    				   out.println(t.getPetriName() + " is controlled by controller" + i + ", it takes " + Math.abs(tmp)+ " tokens from controller" + i);
//	    			       printDetailsOfT(t);
//	    			   }else {
//	    				   Transition t =transitions_humanRead.get(j);
//	    				   out.println(t.getPetriName()  + " is observed by controller" + i + ", it replenishes " +  Math.abs(tmp)+ " tokens to controller" + i );
//	    				   printDetailsOfT(t);
//	    			   }
//	    		   }
//	    	   }
//	    	   
//	    	   
//				  
//	    	   out.println("==========================\n");
//	    	   
//	    	   
//	    	   
//	    	}
//	    	
//	    	
//	    	
//			
//		}
//		private static void printDetailsOfT(Transition t) {
//			out.println(" position of " + t.getPetriName() + ":");
//		   YoungPetri youngPetri =YoungPetri.getYoungPetri();
//		   List<Place> ps = youngPetri.getAllPrecs(t);
//		   out.print("  between ");
//		   for(Place p: ps)
//		   {
//			   if(!(p instanceof PlaceResource))
//			   {
//	              if(p instanceof PlaceCommonLocal)
//	              {
//	            	 Stmt jimStmt = ((PlaceCommonLocal) p).getJimpleStmt();
//	            	 int lineNo = getLineNum(jimStmt);
//	                 out.print(" " +  p.getMsig() + "@" + lineNo);//
//	              }
//	              else if(p instanceof PlaceMethodEntry){
//					out.print("entry@" + p.getMsig());
//				  }else if (p instanceof PlaceMethodExit) {
//					  out.print("exit@" + p.getMsig());
//				  }else {
//					 throw new RuntimeException("what type ?" + p.getClass());
//				  }  
//			   }
//			  
//		   }
//		   
//		   List<Place> ss = youngPetri.getAllSuccs(t);
//		   out.print(" and ");// one prec place, one succ place, domain knowledge
//		   for(Place p: ss)
//		   {
//			   if(!(p instanceof PlaceResource))
//			   {
//	              if(p instanceof PlaceCommonLocal)
//	              {
//	            	  Stmt jimStmt = ((PlaceCommonLocal) p).getJimpleStmt();
//	             	 int lineNo = getLineNum(jimStmt);
//	                  out.print(" " +  "@" + lineNo);// sometimes, the later one, often the goto, has a wrong line!
//	                  
//	              }
//	              else if(p instanceof PlaceMethodEntry){
//	  				out.print("entry@" + p.getMsig());
//	  			  }else if (p instanceof PlaceMethodExit) {
//	  				  out.print("exit@" + p.getMsig());
//	  			  }else {
//	  				 throw new RuntimeException("what type ?" + p.getClass());
//	  			  }
//			   }
//			  
//		   }
//		   out.println();
//		}
//	public static int getLineNum(Host h) {
//		
//        if (h.hasTag("LineNumberTag")) {
//            return ((LineNumberTag) h.getTag("LineNumberTag")).getLineNumber();
//        }
//        if (h.hasTag("SourceLineNumberTag")) {
//            return ((SourceLineNumberTag) h.getTag("SourceLineNumberTag")).getLineNumber();
//        }
//        if (h.hasTag("SourceLnPosTag")) {
//            return ((SourceLnPosTag) h.getTag("SourceLnPosTag")).startLn();
//        }
//        return -1;
//    }
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
