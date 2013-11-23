//package hk.ust.lpxz.statemachine;
//
//import hk.ust.lpxz.SBPI.ToMatrix;
//import hk.ust.lpxz.petri.graph.YoungPetri;
//import hk.ust.lpxz.petri.unit.Place;
//import hk.ust.lpxz.petri.unit.Transition;
//
//
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.OutputStream;
//import java.io.PrintStream;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import properties.DconPropertyManager;
//
//
//
//public class StateOutputter {
//
//	static PrintStream out = null; 
//    public static List<Place> template = new ArrayList<Place>();
//     public static List<Transition> transtemplate = new ArrayList<Transition>();
//  //   static Set<State> goodStates = new HashSet<State>();
////	 public static void output(Set<State> allStates , Set<State> unsafeStates, String fileName ) throws FileNotFoundException
////	 {
////		 FileOutputStream os = new FileOutputStream(fileName);
////         //wrap stream in "friendly" PrintStream
////		 out = new PrintStream(os);
////
////		
////		 goodStates.clear();
////		if(template==null)
////		{
////			template = prepareTemplate(allStates);
////		}
////		for(State state : allStates)
////		{
////			if(!unsafeStates.contains(state))// ! unsafe-> safe
////			{
////				goodStates.add(state);
////			}
////		}
////		//printList(template);
////		out.println("[good]");
////		List tmpList = new ArrayList();
////		for(State good : goodStates)
////		{
////			tmpList.clear(); // you can also create tmoList for each iteration when encessary
////		    getNumericRep(good, tmpList);
////			printList(tmpList);
////		}
////		
////		out.println("[bad]");
////		
////		for(State bad: unsafeStates)
////		{
////			tmpList.clear();
////		    getNumericRep(bad, tmpList);
////			printList(tmpList);			
////		}	 
////	 }
//	 
//     static Set<State> allStates = new HashSet<State>();
//	 public static void output(Set<State> goodStates , Set<State> unsafeStates, String fileName) throws FileNotFoundException
//	 {
//		 FileOutputStream os = new FileOutputStream(fileName);
//         //wrap stream in "friendly" PrintStream
//		 out = new PrintStream(os);
//		 allStates.clear();
//		 allStates.addAll(goodStates);
//		 allStates.addAll(unsafeStates);
//		 
//	     ToMatrix.preparePlaceTemplate(YoungPetri.getYoungPetri(), allStates); 
//	    ToMatrix.prepareTranTemplate(YoungPetri.getYoungPetri());
//	    
//	    
//		
//
//
////		printList(template);
////		printList(transtemplate);
//	
//		List<List> goods = new ArrayList<List>();
//		List<List> bads = new ArrayList<List>();
//		
//		for(State good : goodStates)
//		{
//			List tmpList = new ArrayList(); // you can also create tmoList for each iteration when encessary
//		    getNumericRep(good, tmpList);
//		    goods.add(tmpList);
//		    //printList(tmpList);
//		}
//		
//		
//		
//		for(State bad: unsafeStates)
//		{
//			List tmpList = new ArrayList();
//		    getNumericRep(bad, tmpList);
//		    boolean noContainTwo = bad.twoTokenPlaces.isEmpty();
//		    if(DconPropertyManager.removeBad_with2_fromMarkingFile)
//		    {
//		    	if(noContainTwo)
//		    	{	bads.add(tmpList);}
//		    }
//		    else {
//		    	bads.add(tmpList);
//			}
//		    
//		  
//		    
//			//printList(tmpList);			
//		}	
//		
//		
//         if(DconPropertyManager.minUnsafeMaxSafeOpt)
//         {
//        	 maxSafeFilter(goods);// remove some redundant numeric rep!
//        	 minUnsafeFilter(bads);
//         }
//         
//         // write it to marking file, and to console
//         if(DconPropertyManager.showMarking){
//        	 ToMatrix.printTranTemplate(YoungPetri.getYoungPetri()); 
//        	 ToMatrix.printPlaceTemplate(YoungPetri.getYoungPetri());         	 
//        	 System.out.println("[good]");
//		 }
//     	 out.println("[good]");
//         for(List tmpList: goods)
//         {
//        	 printList(tmpList);	
//         }
//         if(DconPropertyManager.showMarking){
//        	 System.out.println("[bad]");
//		 }
//         out.println("[bad]");
//         for(List tmpList: bads)
//         {
//        	 printList(tmpList);	
//         }
//         
//        // System.out.println("now: " + "good states:" + goods.size() + " " + "bad states:" + bads.size());
//         
//	 }
//	
//	 static Set<List> toremove  = new HashSet<List>();
//	private static void minUnsafeFilter(List<List> bads) {
//		toremove.clear();
//		for(int i=0;i<bads.size(); i++)
//		{
//			List bad = bads.get(i);
//			for(int j=0; j< bads.size(); j++)
//			{
//				if(j!=i)
//				{
//					List other = bads.get(j);
//					if(GE(bad, other))// bad is definitely not minimal, just remove it
//					{
//						toremove.add(bad);
//					}
//				}
//				
//				
//			}
//		}
//		
//		bads.removeAll(toremove);
//		
//		
//	}
//	private static boolean GE(List bad, List other) {//>=
//		if(bad.size()!= other.size())
//			throw new RuntimeException("hwo to compare");
//		
//		
//		for(int i=0 ; i < bad.size(); i++)
//		{
//			int badEntry =((Integer) bad.get(i)).intValue();
//			int otherEntry = ((Integer) other.get(i)).intValue();
//			if(badEntry<otherEntry)// of course, no GT
//			{
//				return false; // falsify
//			}
//		
//			
//		}
//	    return true;
//		
//	}
//	private static void maxSafeFilter(List<List> goods) {
//		toremove.clear();
//		for(int i=0 ; i<goods.size(); i++)
//		{
//			List good = goods.get(i);
//			for(int j=0; j< goods.size(); j++)
//			{
//			   if(j!=i)
//			   {
//				   List other = goods.get(j);
//				   if(GE(other, good))// good is defintely not the maximal, remove it
//				   {
//					   toremove.add(good);
//				   }
//			   }
//			}
//		}
//		goods.removeAll(toremove);
//		
//	}
//	private static void printList(List tmpList) {
//        for(int i=0; i< tmpList.size(); i++)
//        {
//        	 if(DconPropertyManager.showMarking){
//        		 System.out.print(tmpList.get(i));
//             	if(i!=tmpList.size()-1)
//             	{
//             		System.out.print(" ");
//             	}
//             	else {
//             		System.out.println();// another line
//     			}
//    		}
//        	 
//        	 
//        	out.print(tmpList.get(i));
//        	if(i!=tmpList.size()-1)
//        	{
//        		out.print(" ");
//        	}
//        	else {
//				out.println();// another line
//			}
//        }
//		
//		
//	}
//	
//	private static void getNumericRep(State good, List numList) {
//		if(template==null) throw new RuntimeException("intiialize the tmeplate first please");
//	    
//		initializeNumericRep(numList);
//			
//		for(Place p:  good.oneTokenPlaces)
//		{
//			int pos =lookupPosition(template, p);
//		    numList.set(pos, 1);
//		}
//		
//		for(Place p: good.twoTokenPlaces)
//		{
//			int pos =lookupPosition(template, p);
//		    numList.set(pos, 2);
//		}	
//	}
//	private static void initializeNumericRep(List numList) {
//		numList.clear();
//		//
//		int size = template.size();
//		for(int i = 1; i<=size; i++)// initialize all the list
//		{
//			numList.add(0);
//		}
//		
//		
//	}
//	private static int lookupPosition(List<Place> templatePara, Place p) {
//		int ret =   templatePara.indexOf(p);
//		if(ret ==-1)
//		{
//			 templatePara.indexOf(p);
//			throw new RuntimeException("what happens?");
//		}
//		return ret;
//	}
////	private static List<Place> prepareTemplate(Set<State> allStates) {
////		Set<Place> toret = new HashSet<Place>();
////		for(State state  :allStates)
////		{
////			toret.addAll(state.containTokenPlaces());
////		}
////		List<Place> pList = new ArrayList<Place>();
////		for(Place p: toret)
////		{
////			pList.add(p);
////		}		
////		return pList;// give each a position
////	}
//	public static void main(String[] args) {
//	    List list = new ArrayList();
//	    List list2 = new ArrayList();
//	    
//	    list.add(1);
//	    list.add(1);
//	    
//	    list2.add(1);
//	    list2.add(0);
//	    
//	    System.out.println(GE(list, list2));
//	}
//
//}
