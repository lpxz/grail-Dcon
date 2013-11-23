package hk.ust.lpxz.petri.graph;

import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.GadaraSupport.GadaraNamer;
import hk.ust.lpxz.petri.graph.GadaraSupport.GadaraNotation;
import hk.ust.lpxz.petri.graph.criticalsection.PetriCriticalSection;
import hk.ust.lpxz.petri.graph.criticalsection.PetriCriticalSectionManager;
import hk.ust.lpxz.petri.graph.criticalsection.ResourcePlaceManager;
import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.ArcCall;
import hk.ust.lpxz.petri.unit.ArcFromResource;
import hk.ust.lpxz.petri.unit.ArcLocal;
import hk.ust.lpxz.petri.unit.ArcReturn;
import hk.ust.lpxz.petri.unit.ArcToResource;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
import hk.ust.lpxz.petri.unit.PlaceMethodEntry;
import hk.ust.lpxz.petri.unit.PlaceResource;
import hk.ust.lpxz.petri.unit.Transition;

import org.antlr.grammar.v3.ANTLRParser.throwsSpec_return;



import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;


public class ICFGPetriStandardizer {
 
   
    static Set toaddArcsFromResource = new HashSet(); // DO not use hashmap, you know, only one value is associted with one key!
    static Set toaddArcsToResource = new HashSet(); //HashMap<Transition, Place>
    static Set<Arc> toremoveFakeResourceArcs = new HashSet<Arc>();
    static Set<Place> tmp4IsCrossing = new HashSet<Place>();
    static Set<ArcToResource> cache4getTmpAToR = new HashSet<ArcToResource>();
    static Set<ArcFromResource> cache4getTmpAFromR = new HashSet<ArcFromResource>();
    static Set tmpForControllable = new HashSet();
    static Set<Place> tmp4GetPlaceType = new HashSet<Place>();
    public static Stack systemStack = new Stack();
    public static Set visited = new HashSet();

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
    }

    public static void standardize(Petri csGraph) {
        HashSet<PetriMethod> roots = csGraph.getThreadRoots();
        systemStack.clear();
        visited.clear(); // clear it here [not inside petrify0()], otherwise, some nodes are added twice, what is worse, their children, which may be transitions
                         // are taken as places [It should not happen].

        for (PetriMethod root : roots) {
            PlaceMethodEntry entry = root.getEntry();
            standardize0(csGraph, entry);

            if (systemStack.size() >= 1) // assertion
             {
                throw new RuntimeException("empty now");
            }
        }

        handleArcsFromToResource(csGraph);
        initialize(csGraph); // initialize tokens/controllable.  
        petrinetCheck(csGraph);
    }

    private static void petrinetCheck(Petri csGraph) {
        Set<Arc> arcs = csGraph.coreG.edgeSet();

        for (Arc arc : arcs) {
            Object source = arc.getSource();
            Object target = arc.getTarget();

            if (source instanceof Place && target instanceof Place) {
                throw new RuntimeException();
            }

            if (source instanceof Transition && target instanceof Transition) {
                throw new RuntimeException();
            }
        }
    }

    private static void handleArcsFromToResource(Petri csGraph) {
        checkArcToResource(csGraph);
        toremoveFakeResourceArcs.clear(); // all together at last
        toaddArcsFromResource.clear();
        toaddArcsToResource.clear();
     

        Set<PetriCriticalSection> placeCSs = PetriCriticalSectionManager.getAllPetriCSs();
        for (PetriCriticalSection petriCS : placeCSs) {
            Set<ArcFromResource> fakeArcsFromResource = computeFakeArcsFromResource(csGraph, petriCS);
            for (Arc fakeArcFromResource : fakeArcsFromResource) {
            	toremoveFakeResourceArcs.add(fakeArcFromResource);
            	toaddArcsFromResource.addAll(toaddArcsFromResource(fakeArcFromResource, csGraph, petriCS ));                
            }

            Set<ArcToResource> fakeArcsToResource = computeFakeArcsToResource(csGraph, petriCS);
            for (Arc fakeArcToResource : fakeArcsToResource) {
            	 toremoveFakeResourceArcs.add(fakeArcToResource);
            	toaddArcsToResource.addAll(toaddArcsToResource(fakeArcToResource, csGraph, petriCS));
            }
        }
       
        // remove old     
        for (Arc toremove : toremoveFakeResourceArcs) {
            csGraph.coreG.removeEdge(toremove);
        }
        
        // add new edge, and store in PetriCS.
        for (Object o : toaddArcsFromResource) {
        	addArcsFromResource(o, csGraph);
        }

        for (Object o : toaddArcsToResource) {
        	addArcsToResource(o,csGraph);            
        }


    }

    
    private static void addArcsToResource(Object o, Petri csGraph) {
    	Object[] obsTlockP = (Object[]) o;
        Transition obj = (Transition) obsTlockP[0];
        Place lockPlace = (Place) obsTlockP[1];
        PetriCriticalSection placeCS = (PetriCriticalSection) obsTlockP[2];

        if (csGraph.coreG.getEdge(obj, lockPlace) != null) {
            throw new RuntimeException(
                "the arc, between the lockplace and the corresponding place of the transition, is already processed (each arc is processed for once)");
        }

        Arc edge = csGraph.coreG.addEdge_edgetype_lpxz(obj, lockPlace,
                ArcToResource.class);
        placeCS.getArcToResources().add((ArcToResource) edge);
        petriCSs.add(placeCS);
        lockplaces.add(lockPlace);
        arcToResource.add(edge);
	}

    public static HashSet petriCSs = new HashSet();
    public static HashSet lockplaces = new HashSet();
    public static HashSet arcFromResource = new HashSet();
    public static HashSet arcToResource = new HashSet();
	private static void addArcsFromResource(Object o, Petri csGraph) {
    	 Object[] lockPContT = (Object[]) o;
         Place lockPlace = (Place) lockPContT[0];
         Transition trans = (Transition) lockPContT[1];
         PetriCriticalSection petriCS = (PetriCriticalSection) lockPContT[2];

         if (csGraph.coreG.getEdge(lockPlace, trans) != null) {
             throw new RuntimeException(
                 "the arc is processed multiple times?");
         }

         Arc edge = csGraph.coreG.addEdge_edgetype_lpxz(lockPlace, trans,
                 ArcFromResource.class);
         
         petriCS.getArcFromResources().add((ArcFromResource) edge);
         petriCSs.add(petriCS);
         lockplaces.add(lockPlace);
         arcFromResource.add(edge);
	}

	private static Set toaddArcsToResource(Arc arcToResource,
			Petri csGraph, PetriCriticalSection petriCS) {
    	Set toret = new HashSet();
        Place lockPlace = (Place) arcToResource.getTarget();
        Place observedP = (Place) arcToResource.getSource();
        List succs = csGraph.getLocalSuccs(observedP); 
        Set afterObservedPlaceSet = new HashSet();
        afterObservedPlaceSet.addAll(succs);
        for (Object obj : afterObservedPlaceSet) {
            if (obj instanceof Transition) {
                if (checkCrossing(csGraph, obj, petriCS)) {
                    Object[] obsTlockP = new Object[3];
                    obsTlockP[0] = obj;
                    obsTlockP[1] = lockPlace;
                    obsTlockP[2] = petriCS;
                    toret.add(obsTlockP);
                }
            }
        }
        return toret;
	}

	private static Set toaddArcsFromResource(Arc arc, Petri csGraph,
			PetriCriticalSection petriCS) {
    	Set toret = new HashSet();
    	Place lockPlace = (Place) arc.getSource();// place-place arc still.
        Place controlledP = (Place) arc.getTarget();
        List beforeControlledP = csGraph.getLocalPrecs(controlledP);
        Set beforeControlledPlaceSet = new HashSet();
        beforeControlledPlaceSet.addAll(beforeControlledP);
        for (Object trans : beforeControlledPlaceSet) {
            if (trans instanceof Transition) {
                if (checkCrossing(csGraph, trans, petriCS)) // really crossing?
                 {
                    Object[] lockPContT = new Object[3];
                    lockPContT[0] = lockPlace;
                    lockPContT[1] = trans;
                    lockPContT[2] = petriCS;
                    toret.add(lockPContT);
                }
            }
        }
        return toret ;
        
	}

	private static void checkArcToResource(Petri csGraph) {
        // no need to check arcFrom, it is simpler
        Set<ArcToResource> allTO = new HashSet<ArcToResource>();
        Set<Arc> arcs = csGraph.coreG.edgeSet();

        for (Arc arc : arcs) {
            if (arc instanceof ArcToResource) {
                allTO.add((ArcToResource) arc);
            }
        }

        Set<ArcToResource> allTO2 = new HashSet<ArcToResource>();
        Set<PetriCriticalSection> placeCSs2 = PetriCriticalSectionManager.getAllPetriCSs();

        for (PetriCriticalSection pCs : placeCSs2) {
            Set<ArcToResource> tmpArcTos = computeFakeArcsToResource(csGraph, pCs);
            allTO2.addAll(tmpArcTos);
        }

        for (ArcToResource arc : allTO) {
            if (!allTO2.contains(arc)) {
                throw new RuntimeException("should equal");
            }
        }

        for (ArcToResource arc : allTO2) {
            if (!allTO.contains(arc)) {
                throw new RuntimeException("should equal");
            }
        }
    }

    private static boolean checkCrossing(Petri csGraph, Object trans,
        PetriCriticalSection pCs) {
        tmp4IsCrossing.clear();
        pCs.includedPlaces(tmp4IsCrossing);

        List precPlaces = csGraph.getLocalPrecs(trans);
        List succPlaces = csGraph.getLocalSuccs(trans);

        if ((precPlaces.size() != 1) || (succPlaces.size() != 1)) {
            throw new RuntimeException(
                "every transition connects with one input and one output");
        }

        boolean precIn = tmp4IsCrossing.contains(precPlaces.get(0));
        boolean sucIn = tmp4IsCrossing.contains(succPlaces.get(0));

        return (precIn != sucIn);
    }

    private static Set<ArcToResource> computeFakeArcsToResource(Petri csGraph,
        PetriCriticalSection pCs) {
        cache4getTmpAToR.clear();

        Set<Place> obsPlaces = pCs.getObsedPlaces();

        for (Place ob : obsPlaces) {
            List succs = csGraph.getAllSuccEdges(ob);

            for (Object suc : succs) {
                if (suc instanceof ArcToResource) {
                    cache4getTmpAToR.add((ArcToResource) suc);
                }
            }
        }

        return cache4getTmpAToR;
    }

    private static Set<ArcFromResource> computeFakeArcsFromResource(Petri csGraph,
        PetriCriticalSection pCs) {
        cache4getTmpAFromR.clear();

        Set<Place> ctrleds = pCs.getCtredPlaces();

        for (Place ctrled : ctrleds) {
            List precs = csGraph.getAllPrecEdges(ctrled);

            for (Object o : precs) {
                if (o instanceof ArcFromResource) {
                    cache4getTmpAFromR.add((ArcFromResource) o);
                }
            }
        }

        return cache4getTmpAFromR;
    }

    private static void initialize(Petri g) {
        GadaraNamer.assignName(g);

        for (Object v : g.coreG.vertexSet()) {
            if (v instanceof Place) // maybe transition
             {
                Place tmp = (Place) v;
                String name = getPetriName(tmp);
                String typeName = getPlaceType(g, tmp);
                int intialTokens = decideInitialTokens(typeName);
                tmp.intialize(typeName, intialTokens);
            } else if (v instanceof Transition) {
                Transition tmp = (Transition) v;
                String name = getPetriName(tmp);
                boolean controllable = isConstrollable(g, tmp);
                boolean observable = true;
                tmp.intialize(controllable, observable);
            }
        }

        for (Object e : g.coreG.edgeSet()) {
            Arc arc = (Arc) e;
            String name = arc.getPetriName();
            String fromnode = getPetriName(g.coreG.getEdgeSource((Arc) e));
            String tonode = getPetriName(g.coreG.getEdgeTarget((Arc) e));
            int weight = 1; // initially, all set as 1
            arc.intialize(weight);
        }

        g.initialized = true;
    }

    private static boolean isConstrollable(Petri g, Transition tmp) {
        tmpForControllable.clear();

        // if it is the child transition of in a branch, return false
        List precs = g.getLocalPrecs(tmp);
        for (Object prec : precs) {
            if (!(prec instanceof PlaceResource)) {
                tmpForControllable.add(prec);
            }
        }
        if (tmpForControllable.size() > 1) // ==0 is impossible, after the petrinet checking!
         {
            throw new RuntimeException(
                "how can a transition have two parent places? we do not model start/join explicitly. ");
        }
        if (tmpForControllable.size() == 0) {
            return true; //the transition is  arcCall arcReturn
        }

        Place p = (Place) precs.get(0);// branch header place
        List localsuccs = g.getLocalSuccs(p);
        tmpForControllable.clear();
        tmpForControllable.addAll(localsuccs);
        if (tmpForControllable.size() >= 2) {
            return false;
        }

        return true;
    }

    private static int decideInitialTokens(String typeName) {
        if (typeName.equals(GadaraNotation.LockPlaceType)) {
            return 1;
        } else {
            return 0;
        }
    }

    private static String getPlaceType(Petri g, Place tmp) {
        if (tmp instanceof PlaceResource) {
            return GadaraNotation.LockPlaceType;
        }

        if (g.getAllPrecs(tmp).size() == 0) {
            // //assertion: it acts the methodEntry of the threadroot methdos
            tmp4GetPlaceType.clear();

            for (PetriMethod ypm : g.getThreadRoots()) {
                tmp4GetPlaceType.add(ypm.getEntry());
            }

            if (!tmp4GetPlaceType.contains(tmp)) {
                return GadaraNotation.FinishPlaceType; // sometimes, there is really no return, like stupid net.sf.cache4j.CacheCleaner.run(-1)
                                                       //  throw new RuntimeException();  
            }

            return GadaraNotation.InitialPlaceType;
        }

        if (g.getAllSuccs(tmp).size() == 0) {
            return GadaraNotation.FinishPlaceType;
        }
        ////if(tmp instanceof PlaceCommonLocal)// common place, MethodEntry, MethodExit
        {
            return GadaraNotation.SubnetPlaceType;
        }
    }

    private static String getPetriName(Object tmp) {
        if (tmp instanceof Place) {
            return ((Place) tmp).getPetriName();
        } else if (tmp instanceof Transition) {
            return ((Transition) tmp).getPetriName();
        } else {
            throw new RuntimeException("what type is ti?");
        }
    }

    private static void standardize0(Petri csGraph, PlaceMethodEntry entry) {
        if (!visited.contains(entry)) {
            visited.add(entry);
            systemStack.push(entry);
        }

        while (!systemStack.isEmpty()) {
            Object pop = systemStack.pop();

            List children = csGraph.getAllSuccs(pop); // ug.getSuccsOf(pop);

            for (int i = children.size() - 1; i >= 0; i--) {
                Object child = children.get(i);

                // here, visit the edge, do not visit inside the following branch, it would miss some unvisited edges with the target nodes  visited.
                if (pop instanceof PlaceResource ||
                        child instanceof PlaceResource) {
                    // DO not handle the arcs with the PlaceResource as the source or targets!
                    // we will handle them in special function.
                } else {
                    petrify_an_edge(csGraph, (Place) pop, (Place) child); // done
                }

                if (!visited.contains(child)) {
                    //System.out.println("added:" + ((Place)child).petriName);
                    visited.add(child);
                    systemStack.push(child);
                }
            }
        }
    }

    private static void petrify_an_edge(Petri csGraph, Place pop, Place child) {
        // add newtransition to the graph first
        Transition newone = new Transition();
        csGraph.coreG.addVertex(newone);

        Arc edge = csGraph.coreG.getEdge(pop, child);

        if (edge instanceof ArcLocal) {
            csGraph.coreG.addEdge_edgetype_lpxz(pop, newone, ArcLocal.class);
            csGraph.coreG.addEdge_edgetype_lpxz(newone, child, ArcLocal.class);
            csGraph.coreG.removeEdge(pop, child);
        } else if (edge instanceof ArcCall) {
            csGraph.coreG.addEdge_edgetype_lpxz(pop, newone, ArcCall.class);
            csGraph.coreG.addEdge_edgetype_lpxz(newone, child, ArcCall.class);
            csGraph.coreG.removeEdge(pop, child);
        } else if (edge instanceof ArcReturn) {
            csGraph.coreG.addEdge_edgetype_lpxz(pop, newone, ArcReturn.class);
            csGraph.coreG.addEdge_edgetype_lpxz(newone, child, ArcReturn.class);
            csGraph.coreG.removeEdge(pop, child);
        } else if (edge instanceof ArcFromResource) {
            csGraph.coreG.addEdge_edgetype_lpxz(pop, newone,
                ArcFromResource.class);
            csGraph.coreG.addEdge_edgetype_lpxz(newone, child,
                ArcFromResource.class);
            csGraph.coreG.removeEdge(pop, child);
        } else if (edge instanceof ArcToResource) {
            csGraph.coreG.addEdge_edgetype_lpxz(pop, newone, ArcToResource.class);
            csGraph.coreG.addEdge_edgetype_lpxz(newone, child,
                ArcToResource.class);
            csGraph.coreG.removeEdge(pop, child);
        }
        else {
            throw new RuntimeException("impossible type..");
        }

        //		HashSet<Place> places = csGraph.getPlaces();// not complete.
        //		HashSet<Transition> transitions = csGraph.getTransitions();
        //		places.add(pop); places.add(child);		
        //		transitions.add(newone);	
    }
}
