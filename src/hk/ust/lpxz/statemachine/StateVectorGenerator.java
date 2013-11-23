package hk.ust.lpxz.statemachine;

import hk.ust.lpxz.SBPI.ToMatrix;
import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceResource;
import hk.ust.lpxz.petri.unit.Transition;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class StateVectorGenerator {
    static PrintStream out = null;
    public static List<Place> placeTemplate = new ArrayList<Place>();
    public static List<Transition> transtemplate = new ArrayList<Transition>();
    static Set<State> allStates = new HashSet<State>();
    static Set<List> toremove = new HashSet<List>();

    static List tmpList = new ArrayList(); // you can also create tmoList for each iteration when encessary
    public static void output(Set<State> goodStates, Set<State> unsafeStates,
        String fileName) throws FileNotFoundException {
    	
    	out = new PrintStream(new File(fileName));
       
         for (Place p : placeTemplate) {
          out.print(p.getPetriName() + " ");
         }
         out.println();
    	
        out.println("[good]");
        for (State good : goodStates) {
        	tmpList.clear();
            getNumericRep(good, tmpList);
            printList(tmpList);
        }

        out.println("[bad]");
        for (State bad : unsafeStates) {
        	tmpList.clear();
            getNumericRep(bad, tmpList);
            printList(tmpList);
        }
    }
    
    //lpxz: modular but costly
//    public static void output(Set<State> goodStates, Set<State> unsafeStates,
//            String fileName) throws FileNotFoundException {
//        	File file = new File(fileName);
//        	if(!file.exists())
//        	{
//        		if(!file.getParentFile().exists())
//        		{
//        			file.getParentFile().mkdirs();
//        		}
//        		try {
//    				file.createNewFile();
//    			} catch (IOException e) {
//    				// LPXZ Auto-generated catch block
//    				e.printStackTrace();
//    			}
//        	}
//            FileOutputStream os = new FileOutputStream(file);
//            //wrap stream in "friendly" PrintStream
//            out = new PrintStream(os);
//
//          
//             if (DconPropertyManager.minUnsafeMaxSafeOpt) {
//                maxSafeFilter(goodStates); // remove some redundant numeric rep!
//                minUnsafeFilter(unsafeStates);
//              }
//            
//            List<List> goods = new ArrayList<List>();
//            List<List> bads = new ArrayList<List>();
//
//           
//            for (State good : goodStates) {
//                List tmpList = new ArrayList(); // you can also create tmoList for each iteration when encessary
//                getNumericRep(good, tmpList);
//                goods.add(tmpList);
//            }
//
//            for (State bad : unsafeStates) {
//                List tmpList = new ArrayList();
//                getNumericRep(bad, tmpList);
//                bads.add(tmpList);
//            }
//
////            System.out.println("spyros versionxxx2"); // very slow
////            if (DconPropertyManager.minUnsafeMaxSafeOpt) {
////                maxSafeFilter(goods); // remove some redundant numeric rep!
////                minUnsafeFilter(bads);
////            }
////            System.out.println("spyros versionxxx3");
//            if (DconPropertyManager.showStates) {
//                System.out.println("[good]");
//
//                for (List good : goods) {
//                    System.out.println(getPlaceRep(good));
//                }
//
//                System.out.println("[bad]");
//
//                for (List bad : bads) {
//                    System.out.println(getPlaceRep(bad));
//                }
//            }
//
//            // write it to marking file, and to console
//            if (DconPropertyManager.showMarking) {
//                StateVectorGenerator.printTranTemplate(Petri.getPetri());
//                StateVectorGenerator.printPlaceTemplate(Petri.getPetri());
//                System.out.println("[good]");
//            }
//
//            out.println("[good]");
//
//            for (List tmpList : goods) {
//                printList(tmpList);
//            }
//
//            if (DconPropertyManager.showMarking) {
//                System.out.println("[bad]");
//            }
//
//            out.println("[bad]");
//
//            for (List tmpList : bads) {
//                printList(tmpList);
//            }
//
//           
//            // System.out.println("now: " + "good states:" + goods.size() + " " + "bad states:" + bads.size());
//        }

   

	private static void minUnsafeFilter(List<List> bads) {
        toremove.clear();

        for (int i = 0; i < bads.size(); i++) {
            List bad = bads.get(i);

            for (int j = 0; j < bads.size(); j++) {
                if (j != i) {
                    List other = bads.get(j);

                    if (GE(bad, other)) // bad is definitely not minimal, just remove it
                     {
                        toremove.add(bad);
                    }
                }
            }
        }

        bads.removeAll(toremove);
    }

    private static boolean GE(List bad, List other) { //>=

        if (bad.size() != other.size()) {
            throw new RuntimeException("hwo to compare");
        }

        for (int i = 0; i < bad.size(); i++) {
            int badEntry = ((Integer) bad.get(i)).intValue();
            int otherEntry = ((Integer) other.get(i)).intValue();

            if (badEntry < otherEntry) // of course, no GT
             {
                return false; // falsify
            }
        }

        return true;
    }

    private static void maxSafeFilter(List<List> goods) {
        toremove.clear();

        for (int i = 0; i < goods.size(); i++) {
            List good = goods.get(i);

            for (int j = 0; j < goods.size(); j++) {
                if (j != i) {
                    List other = goods.get(j);

                    if (GE(other, good)) // good is defintely not the maximal, remove it
                     {
                        toremove.add(good);
                    }
                }
            }
        }

        goods.removeAll(toremove);
    }

    private static void printList(List tmpList) {
        for (int i = 0; i < tmpList.size(); i++) {
            out.print(tmpList.get(i));

            if (i != (tmpList.size() - 1)) {
                out.print(" ");
            } else {
                out.println(); // another line
            }
        }
    }

    private static String getPlaceRep(List numList) {
        StringBuilder sb = new StringBuilder();

        for (int pos = 0; pos < numList.size(); pos++) {
            int value = ((Integer) numList.get(pos)).intValue();

            if (value == 1) {
                Place tmp = placeTemplate.get(pos);
                sb.append(tmp.getPetriName() + " ");
            }

            if (value == 2) {
                Place tmp = placeTemplate.get(pos);
                sb.append(tmp.getPetriName() + "' ");
            }
        }

        return sb.append("\n").toString();
    }

    public static void getNumericRep(State good, List numList) {
        if (placeTemplate == null) {
            throw new RuntimeException("intiialize the tmeplate first please");
        }

        fillZeros(numList);

        for (Place p : good.oneTokenPlaces) {
            int pos = lookupPosition(placeTemplate, p);
            numList.set(pos, 1);
        }

        for (Place p : good.twoTokenPlaces) {
            int pos = lookupPosition(placeTemplate, p);
            numList.set(pos, 2);
        }
    }

    private static void fillZeros(List numList) {
        numList.clear();

        //
        int size = placeTemplate.size();

        for (int i = 1; i <= size; i++) // initialize all the list
         {
            numList.add(0);
        }
    }

    private static int lookupPosition(List<Place> templatePara, Place p) {
        int ret = templatePara.indexOf(p);

        if (ret == -1) {
            templatePara.indexOf(p);
            throw new RuntimeException("what happens?" + p.toString());
        }

        return ret;
    }

    //	private static List<Place> prepareTemplate(Set<State> allStates) {
    //		Set<Place> toret = new HashSet<Place>();
    //		for(State state  :allStates)
    //		{
    //			toret.addAll(state.containTokenPlaces());
    //		}
    //		List<Place> pList = new ArrayList<Place>();
    //		for(Place p: toret)
    //		{
    //			pList.add(p);
    //		}		
    //		return pList;// give each a position
    //	}
    public static void main(String[] args) {
        List list = new ArrayList();
        List list2 = new ArrayList();

        list.add(1);
        list.add(1);

        list2.add(1);
        list2.add(0);

        System.out.println(GE(list, list2));
    }

    public static void printTranTemplate(Petri youngPetri) {
        for (Transition t : transtemplate) {
            System.out.print(t.getPetriName() + " ");
        }

        System.out.println();
    }

    public static List<Transition> prepareTranTemplate(Petri youngPetri) {
        //System.out.println("trans templaet:");
        Set<Transition> involvedTs = new HashSet<Transition>(); // small trick, according to SBPI, we do not need to care about some transitions, which have nothing to do the above places. the controller will not connect to them

        for (Place p : placeTemplate) {
            involvedTs.addAll(youngPetri.getAllPrecs(p));
            involvedTs.addAll(youngPetri.getAllSuccs(p));
        }

        for (Transition p : youngPetri.getTransitions()) {
            if (involvedTs.contains(p) && !transtemplate.contains(p)) {
                transtemplate.add(p);
            }
        }

        return transtemplate;

        //System.out.println();
    }

    public static void printPlaceTemplate(Petri youngPetri) {
        for (Place p : placeTemplate) {
            System.out.print(p.getPetriName() + " ");
        }

        System.out.println();
    }

    public static List<Place> preparePlaceTemplate(Petri youngPetri,
        Set<State> allStates) {
        // follow the order
        List<Place> places = youngPetri.getPlaces();

        // get the involved places ready
        Set<Place> involved = new HashSet<Place>();

        if (!DconPropertyManager.useYinSolver) {
            for (State state : allStates) // optimized version
             {
                involved.addAll(state.containTokenPlaces());
            }
        } else { // yinsolver needs to reason about the transition at the boundary, so the outside place should be included.
                 // more efficient version is to just include the places in the optimized version, 
                 //  and the outside places adjacent to them. Implement it 

            for (Place pp : places) // complete version
             {
                if (!(pp instanceof PlaceResource)) {
                    involved.add(pp);
                }
            }
        }

        for (Place p : places) {
            if (involved.contains(p) && !placeTemplate.contains(p)) {
                placeTemplate.add(p);
            }
        }

        return placeTemplate;
    }

    public static List<Place> preparePlaceTemplateNonYin(Petri youngPetri,
            Set<State> allStates) {
            // follow the order
            List<Place> places = youngPetri.getPlaces();

            // get the involved places ready
            Set<Place> involved = new HashSet<Place>();

            {
                for (State state : allStates) // optimized version
                 {
                    involved.addAll(state.containTokenPlaces());
                }
            } 

            for (Place p : places) {
                if (involved.contains(p) && !placeTemplate.contains(p)) {
                    placeTemplate.add(p);
                }
            }

            return placeTemplate;
        }
    
    public static List<Place> preparePlaceTemplateYin(Petri youngPetri,
            Set<State> allStates) {
            // follow the order
            List<Place> places = youngPetri.getPlaces();

            // get the involved places ready
            Set<Place> involved = new HashSet<Place>();

            { // yinsolver needs to reason about the transition at the boundary, so the outside place should be included.
                     // more efficient version is to just include the places in the optimized version, 
                     //  and the outside places adjacent to them. Implement it 

                for (Place pp : places) // complete version
                 {
                    if (!(pp instanceof PlaceResource)) {
                        involved.add(pp);
                    }
                }
            }

            for (Place p : places) {
                if (involved.contains(p) && !placeTemplate.contains(p)) {
                    placeTemplate.add(p);
                }
            }

            return placeTemplate;
        }


//   
}
