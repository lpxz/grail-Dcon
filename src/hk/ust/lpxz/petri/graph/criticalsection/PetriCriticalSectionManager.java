package hk.ust.lpxz.petri.graph.criticalsection;


import com.sun.corba.se.spi.orb.StringPair;

import edu.hkust.clap.lpxz.context.ContextMethod;

import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.graph.PetriMethodManager;
import hk.ust.lpxz.petri.graph.PetriReachable;
import hk.ust.lpxz.petri.graph.violation.Violation;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceResource;
import hk.ust.lpxz.petri.unit.Transition;
import hk.ust.lpxz.petri.unitgraph.LocalUnitGraphReachable;

import pldi.locking.CriticalSection;
import pldi.locking.SynchronizedRegionFinder;
import pldi.locking.SynchronizedRegionFlowPair;


import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import soot.jimple.Stmt;

import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.Pair;

import soot.util.Chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class PetriCriticalSectionManager {
    public static HashMap<Body, List<CriticalSection>> body2CSs = new HashMap<Body, List<CriticalSection>>();

    // public static HashMap<Object, HashSet<PlaceCS>> CSorSyncM2PCSs = new HashMap<Object, HashSet<PlaceCS>>();
    public static HashSet<PetriCriticalSection> petriCS = new HashSet<PetriCriticalSection>();
    public static List<Pair<Stmt, Stmt>> endPairs = new ArrayList<Pair<Stmt, Stmt>>();
    public static Set<PetriCriticalSection> allPlaceCSs = new HashSet<PetriCriticalSection>();
    static Set<PetriCriticalSection> outerMost = null;

    // tested
    static Set<Place> includedPlaces1 = new HashSet<Place>();
    static Set reachables4ICB1 = new HashSet();
    static Set<Place> includedPlaces2 = new HashSet<Place>();
    static Set reachables4ICB2 = new HashSet();

    public static void registerPetriCS(PetriCriticalSection newOne) {
        petriCS.add(newOne);
    }

    static boolean isHealthy(CriticalSection cs) {
        if ((cs.entermonitor != null) && (cs.origLock != null) &&
                (cs.prepStmt != null) && (cs.exceptionalEnd != null)) {
            return true;
        }
        return false;
    }

    public static List<CriticalSection> getCriticalSections(Body b) {
        List ret = body2CSs.get(b);

        if (ret != null) {
            return ret;
        }

        List list = new ArrayList();

        {
            //	Body clone = (Body)b.clone();// for recovering
            UnitGraph eug = new ExceptionalUnitGraph( // this oen would add nop to the body, sign!!
                    b);
            SynchronizedRegionFinder ta = new SynchronizedRegionFinder(eug, b,
                    true);
            Chain units = b.getUnits();
            Unit lastUnit = (Unit) units.getLast();
            FlowSet fs = (FlowSet) ta.getFlowBefore(lastUnit);

            // all tns are here
            if (fs != null) {
                for (Iterator iterator = fs.iterator(); iterator.hasNext();) {
                    SynchronizedRegionFlowPair srfp = (SynchronizedRegionFlowPair) iterator.next();
                    CriticalSection cSection = (srfp).tn;

                    if ((cSection.origLock == null) &&
                            (cSection.entermonitor == null)) {
                        continue; // this is the buggy output CS due to the nop generated in the EnhancedUnitGraph!, avoid it temporarily.
                    }

                    // I hope to get the correct cSection instead of the wrong one! soot returns wrong one.
                    // units!
                    fixSootBug(cSection);

                    list.add(cSection);

                    //Value orig =cSection.origLock;
                }
            }

            body2CSs.put(b, list); // store it , do not create it next time for the same body
        }

        return list;
    }

    private static void fixSootBug(CriticalSection cSection) { // soot misses some regular stmts!!!
        cSection.units.clear();

        endPairs.clear();

        endPairs.addAll(cSection.earlyEnds);
        endPairs.add(cSection.end); // pair.getO1 returns the one after the exitMonitor. exit then goto, for example.
                                    // ignore the exceptional ends

        UnitGraph unitGraph = new BriefUnitGraph(cSection.method.getActiveBody());

        for (Pair<Stmt, Stmt> pair : endPairs) {
            if (pair != null) {
                //	System.out.println(cSection.method.getActiveBody());
                Set protectedUnits = LocalUnitGraphReachable.protectedSet(unitGraph,
                        cSection.entermonitor, pair.getO2());
                cSection.units.addAll(protectedUnits);
            }
        }
    }
    
	public static Set<Unit> getUnits(CriticalSection cSection) { // soot misses some regular stmts!!!
	    cSection.units.clear();
	
	    PetriCriticalSectionManager.endPairs.clear();
	
	    PetriCriticalSectionManager.endPairs.addAll(cSection.earlyEnds);
	    PetriCriticalSectionManager.endPairs.add(cSection.end); // pair.getO1 returns the one after the exitMonitor. exit then goto, for example.
	                                // ignore the exceptional ends
	
	    UnitGraph unitGraph = new BriefUnitGraph(cSection.method.getActiveBody());
	
	    for (Pair<Stmt, Stmt> pair : PetriCriticalSectionManager.endPairs) {
	        if (pair != null) {
	            //	System.out.println(cSection.method.getActiveBody());
	            Set protectedUnits = LocalUnitGraphReachable.protectedSet(unitGraph,
	                    cSection.entermonitor, pair.getO2());
	            cSection.units.addAll(protectedUnits);
	        }
	    }
	    return cSection.units;
	}
    
    public static Set<PetriCriticalSection> getAllPetriCSs() // computation based
     {
        allPlaceCSs.clear();
        allPlaceCSs.addAll(petriCS);
        return allPlaceCSs;
    }

    public static boolean isReentrantLockTran(Transition arg)
    {
    	return reentrantLockTrans.contains(arg);
    }
    public static boolean isReentrantUnlockTran(Transition arg)
    {
    	return reentrantUnlockTrans.contains(arg);
    }
    
	static Set<Transition> reentrantLockTrans = new HashSet<Transition>();
	static Set<Transition> reentrantUnlockTrans = new HashSet<Transition>();
	public static void detectReentrantLockTransitions() {
		reentrantLockTrans.clear();
		 Set<PetriCriticalSection> allCSs = getAllPetriCSs();

         for (PetriCriticalSection placeCS : allCSs) {
             if (!hasNoDomination(placeCS, allCSs, true)) { // true means "checking the lock place equality"
            	 reentrantLockTrans.addAll(placeCS.computeCtredTransitions());
            	 reentrantUnlockTrans.addAll(placeCS.computeObsedTransitions());
             }
         }	
	}
	
    public static Set<PetriCriticalSection> getOuterPetriCSs() {
        if (outerMost != null) // caching
         {
            return outerMost;
        } else {
            outerMost = new HashSet<PetriCriticalSection>();

            Set<PetriCriticalSection> allCSs = getAllPetriCSs();

            for (PetriCriticalSection placeCS : allCSs) {
                if (hasNoDomination(placeCS, allCSs, false)) {
                    outerMost.add(placeCS);
                }
            }

            return outerMost;
        }
    }

    private static boolean hasNoDomination(PetriCriticalSection placeCS,
        Set<PetriCriticalSection> allCSs, boolean samelockConstraint) {
        boolean noDom = true;

        for (PetriCriticalSection tmp : allCSs) {
            if (tmp != placeCS) // never take himself into account
             {
                if (isDomedBy(placeCS, tmp, samelockConstraint)) {
                    noDom = false;

                    return noDom; // you fail to be an outermost
                }
            }
        }

        return noDom;
    }

    private static boolean isDomedBy(PetriCriticalSection placeCS,
        PetriCriticalSection tmp, boolean samelockConstraint) {
        // simple now..
        includedPlaces1.clear();
        reachables4ICB1.clear();
        placeCS.includedPlaces(includedPlaces1);

        Petri youngPetri = Petri.getPetri();
        PetriReachable.all_reachablePlaces_noPassing_PRs_domain(youngPetri,
            includedPlaces1, reachables4ICB1);

        includedPlaces2.clear();
        reachables4ICB2.clear();
        tmp.includedPlaces(includedPlaces2);
        PetriReachable.all_reachablePlaces_noPassing_PRs_domain(youngPetri,
            includedPlaces2, reachables4ICB2);

        if (reachables4ICB2.containsAll(reachables4ICB1)) {
            if (samelockConstraint) {
                return tmp.getresPlace() == placeCS.getresPlace();
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    //passed test
    private static int firstDiff(List<ContextMethod> ctxt1,
        List<ContextMethod> ctxt2) {
        int firstDIff = -1;

        for (int i = 0; (i < ctxt1.size()) && (i < ctxt2.size()); i++) // must be executed
         {
            ContextMethod s1 = ctxt1.get(i);
            ContextMethod s2 = ctxt2.get(i);

            if (s1 != s2) {
                firstDIff = i;

                break;
            } else { // still the same, but one or two reach the endpoints

                if (i == (ctxt1.size() - 1)) {
                    firstDIff = i + 1;

                    break;
                } else if (i == (ctxt2.size() - 1)) {
                    firstDIff = i + 1;

                    break;
                }
            }
        }

        return firstDIff;
    }

    public static void removeRecursiveCS() {
        HashSet<PetriCriticalSection> toremove = new HashSet<PetriCriticalSection>();
        Set<PetriCriticalSection> allCSs = getAllPetriCSs();

        for (PetriCriticalSection placeCS : allCSs) {
            if (hasNoDomination(placeCS, allCSs, true)) // consider the lock constraint
             {
            } else {
                toremove.add(placeCS);
            }
        }

        //remove also the mapping stored.:
        for (PetriCriticalSection pcs : toremove) {
            PlaceCSModeller.removeConnectWithResourcePlace(pcs); // remove connection
                                                                 // disregisterPCS(pcs);// keep them, otherwise, the simplification will kill the statements
                                                                 // but the arcs already removed, i.e., the solid thing is done
        }
    }
}
