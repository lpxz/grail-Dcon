package hk.ust.lpxz.petri.graph;

import Jama.Matrix;

import edu.hkust.clap.lpxz.context.ContextMethod;
import edu.hkust.clap.organize.CSMethod;
import edu.hkust.clap.organize.CSMethodPair;

import hk.ust.lpxz.fixing.Reporter;
import hk.ust.lpxz.fixing.SootAgent4Fixing;
import hk.ust.lpxz.petri.graph.criticalsection.PlaceCSModeller;
import hk.ust.lpxz.petri.graph.violation.Violation;
import hk.ust.lpxz.petri.graph.violation.ViolationManager;
import hk.ust.lpxz.petri.graph.violation.ViolationModeller;
import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.ArcFromController;
import hk.ust.lpxz.petri.unit.ArcToController;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
import hk.ust.lpxz.petri.unit.PlaceControl;
import hk.ust.lpxz.petri.unit.Transition;
import hk.ust.lpxz.statemachine.State;
import hk.ust.lpxz.statemachine.StateVectorGenerator;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.tagkit.Host;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ICFGPetriBuilder {
    private static final int err_threshold = 3; // lineNO may not be reliable.
    public static Local jvmInstance = Jimple.v()
                                            .newLocal("placeholder",
            RefType.v("java.lang.Object"));

    static List<ContextMethod> ctxtStack = new ArrayList();

    public static Petri getICFGPetri(List list) {
        // make sure you run the FilterOut first!
        if (Petri.isReady()) {
            return Petri.instance();
        } else {
            Petri petriNet = Petri.instantiate();
            
            pcr_raw_2_sootIR(list); //

            for (int i = 0; i < list.size(); i++) {
                System.out.println("The " + i + "th AV bug");

                
                	
                Object elem = list.get(i);
                CSMethodPair pair = (CSMethodPair) elem;
                CSMethod PC = pair.getO1();
                CSMethod R = pair.getO2();


                List<ContextMethod> contexts = PC.getContexts();
                ICFGPetriBuilder.ICFG2ICFGPetri(petriNet, contexts); // interprocedural CFG 2 raw Petri.

              //  R.printIt();

                List<ContextMethod> contexts2 = R.getContexts();               
                ICFGPetriBuilder.ICFG2ICFGPetri(petriNet, contexts2);

                PetriMethod pcPetriMethod = PetriMethodManager.getPetriMethod4PCR(contexts);
                PetriMethod rPetriMethod = PetriMethodManager.getPetriMethod4PCR(contexts2);
                PetriMethodManager.containingMethods.add(pcPetriMethod.getMsig());
                PetriMethodManager.containingMethods.add(rPetriMethod.getMsig());
                
                Place pplace = pcPetriMethod.getPlace(PC.getPunit());
                Place cplace = pcPetriMethod.getPlace(PC.getCunit());
                Place rplace = rPetriMethod.getPlace(R.getRunit()); // 
                ViolationModeller.modelViolation(pplace,
                        pcPetriMethod, cplace, pcPetriMethod, rplace, rPetriMethod);
            }

            
            //Reporter.store("Petri transitions ", petriNet.getTransitions().size());
            
            PlaceCSModeller.modelAllPlaceCSs(petriNet);
            return petriNet;
        }
    }

    public static void ICFG2ICFGPetri(Petri youngPetri, List<ContextMethod> stes) {
        Place invokePlace = null;
        ctxtStack.clear();

        for (int i = 0; i <= (stes.size() - 1); i++) {
            ContextMethod ste = stes.get(i);
            ctxtStack.add(ste); // always updating

            if ((PetriMethodManager.getCurrentPetriMethod(ctxtStack) == null)) {
                if (i == 0) { // No parent caller method. fake call site.
                    PetriMethod currentPetriMethod = storeCurrentPetriMethod(youngPetri,
                            ctxtStack);
                    youngPetri.getThreadRoots().add(currentPetriMethod); // entry of PN.
                } else if (i >= 1) {
                    PetriMethod gcsMethod = storeCurrentPetriMethod(youngPetri,
                            ctxtStack); //
                    PetriMethod callerMethod = PetriMethodManager.getCallerPetriMethod(ctxtStack);
                    Unit invoke = PetriMethodManager.getInvokeUnit(ctxtStack);
                    invokePlace = callerMethod.getPlace(invoke);

                    if (invokePlace != null) {
                        ICFGLowLevelPetriBuilder.remoteIssues(youngPetri,
                            (PlaceCommonLocal) invokePlace,
                            gcsMethod.getEntry(), gcsMethod.getExit());
                    } else {
                        throw new RuntimeException(
                            "no caller unit found, maybe it is in the exception part which is not modeled in Soot's briefUnitGraph.");
                    
                    }
                }
            }
        }
    }

    public static Petri getYoungPetri_filterPhase(List list) {
        if (Petri.isReady()) {
            return Petri.instance();
        } else {
            Petri youngPetri = Petri.instantiate();
            pcr_raw_2_sootIR(list); //

            for (int i = 0; i < list.size(); i++) {
                System.out.println("The " + i + "th AV bug");

                Object elem = list.get(i);
                CSMethodPair pair = (CSMethodPair) elem;
                CSMethod PC = pair.getO1();
                CSMethod R = pair.getO2();

               // PC.printIt();

                List<ContextMethod> contexts = PC.getContexts();

                try {
                    ICFGPetriBuilder.ICFG2ICFGPetri(youngPetri, contexts);
                } catch (Exception e) {
                	e.printStackTrace();
                    System.out.println("!!!ignore the violation: " + i);
                    PC.printIt();
                    list.set(i, null); // to be filtered out.

                    continue;
                }

               // R.printIt();

                List<ContextMethod> contexts2 = R.getContexts();

                try {
                    ICFGPetriBuilder.ICFG2ICFGPetri(youngPetri, contexts2);
                } catch (Exception e) {
                	e.printStackTrace();
                    System.out.println("!!!ignore the violation: " + i);
                    R.printIt();
                    list.set(i, null);

                    continue;
                }

                PetriMethod ypm1 = PetriMethodManager.getPetriMethod4PCR(contexts);
                PetriMethod ypm2 = PetriMethodManager.getPetriMethod4PCR(contexts2);

                Place pplace = ypm1.getPlace(PC.getPunit());
                Place cplace = ypm1.getPlace(PC.getCunit());
                Place rplace = ypm2.getPlace(R.getRunit()); // 

                if ((pplace == null) || (cplace == null) || (rplace == null)) {
                    System.out.println("!!!ignore the violation: " + i);
                    list.set(i, null);

                    continue;
                }

                Violation tmpViolation = ViolationModeller.modelViolation(pplace,
                        ypm1, cplace, ypm1, rplace, ypm2);
            }

            PlaceCSModeller.modelAllPlaceCSs(youngPetri);

            return youngPetri;
        }
    }

    private static void findIntersectingVios_on_2Vars() {
        Set<Violation> vios = ViolationManager.getAllViolations();

        for (Violation vio : vios) {
            Set<Place> tmp4decideColor = new HashSet<Place>();
            vio.includedPlaces(tmp4decideColor);
            vio.setTempIncludedPlaces(tmp4decideColor);
        }

        for (Violation vio1 : vios) {
            for (Violation vio2 : vios) {
                if (vio1.accessedField().getField().toString()
                            .equals(vio2.accessedField().getField().toString())) {
                    if (!Collections.disjoint(vio1.getTempIncludedPlaces(),
                                vio2.getTempIncludedPlaces())) {
                        System.out.println(
                            "Vio 1, note the rstmt accesses a different field");
                        System.out.println("" + vio1);

                        System.out.println("Vio 2");
                        System.out.println("" + vio2);
                        System.out.println("\n");
                    }
                }
            }
        }
    }

    // shortcut, account for most cases, efficient, hot path
    public static Body search4MethodBodyInClass(String msig, String className) {
        SootClass sc = Scene.v().loadClassAndSupport(className);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        SootMethod sm = Scene.v().getMethod(msig);

        if (!sm.hasActiveBody()) {
            sm.retrieveActiveBody();
        }

        Body bb = sm.getActiveBody();

        // G.v().reset();
        return bb;
    }

    // reuse the body stored in the secondGCS, if you search with the soot
    // naively, you will get a unit which can not direct you to the wanted
    // Statement.
    // remind that soot generates new jimple unit for the same code in a
    // different run.
    public static Stmt search4CallSiteStmt_reuseGCS(Body bb, String className,
        String mName, String filename, int eleLineNO, String eleLineNoInvoke) {
        // mName is not important, we care about the eleLineNoInvoke only.
        // mName is not necessarily the same as the callsite invocation.
        // YOU need to trust eleLineNoInvoke!absolutely correct.
        // It is taken from the jdk stacktraceelement.
        // the lineNO may not be quite accurate, for example, 137 maybe taken as
        // 138.
        // String secondClass = secondGCS.getClassname();

        // boolean check = couplingCheck(secondClass, filename);
        // if(!check)
        // {
        // System.err.println("seems not matching between the caller and the callee!!");
        //			
        // }
        // if(check)// the second one really matches the caller, instead of the
        // far-away relative
        {
            // Body bb =secondGCS.getBb();// recentMethod!=NULL
            HashSet<Unit> possibleUnits = new HashSet<Unit>();
            Iterator<Unit> it = bb.getUnits().iterator();

            while (it.hasNext()) {
                Stmt unit = (Stmt) it.next();

                if (unit.containsInvokeExpr()) {
                    SootMethodRef invokeM = unit.getInvokeExpr().getMethodRef(); // getMethod
                                                                                 // may
                                                                                 // returns
                                                                                 // null
                                                                                 // as
                                                                                 // the
                                                                                 // method
                                                                                 // is
                                                                                 // collected
                                                                                 // by
                                                                                 // gc

                    String invokeMStr = invokeM.name(); // not necessarily the
                                                        // same,
                                                        // one is Impl.loopup, one is soot-based: interface.lookup!

                    if (eleLineNoInvoke.contains(invokeMStr)) // 
                     {
                        if (getLineNum((Stmt) unit) == eleLineNO) // exactly the
                                                                  // same??
                         {
                            // hit
                            // G.reset();
                            return unit;
                        } else {
                            possibleUnits.add(unit);
                        }
                    } else if (eleLineNoInvoke.contains("<clinit>")) {
                        // special case, there is no statement there containing
                        // the clinint,
                        // just find the calling statements of the static
                        // methods of the class!,
                        // if many, put them into the possibleUnits for further
                        // line-based pruning.
                        int sep = eleLineNoInvoke.lastIndexOf('.');
                        String elelineNoinvoke_class = eleLineNoInvoke.substring(0,
                                sep);

                        if (invokeM.declaringClass().getName()
                                       .equals(elelineNoinvoke_class)) {
                            // hit
                            if (getLineNum((Stmt) unit) == eleLineNO) // exactly
                                                                      // the
                                                                      // same??
                             {
                                // hit
                                // G.reset();
                                return unit;
                            } else {
                                possibleUnits.add(unit);
                            }
                        }
                    }
                }
            }

            if (possibleUnits.size() != 0) {
                // still not return the signature yet, error_tolerance of the
                // line NO!
                // G.reset();
                Unit best = null;
                int bestDis = Integer.MAX_VALUE;

                for (Unit possible : possibleUnits) {
                    int itsLine = getLineNum((Stmt) possible);

                    if (Math.abs(itsLine - eleLineNO) < bestDis) {
                        best = possible;
                        bestDis = Math.abs(itsLine - eleLineNO);
                    }
                }

                if (bestDis <= err_threshold) {
                    return (Stmt) best;
                } else {
                    throw new RuntimeException("double checking " + bestDis);
                }

                // System.out.println("I want " + eleLineNO);
                // for(Unit possible:possibleUnits)
                // {
                // System.out.println("but one possible is :" +
                // getLineNum(possible));
                //	    	    		
                // }
                // System.out.println();

                // throw new
                // RuntimeException("check please!line no is not exactly the same?");
            }
        }

        // G.reset();
        return null;
    }

    public static int getLineNum(Host h) {
        if (h.hasTag("LineNumberTag")) {
            return ((LineNumberTag) h.getTag("LineNumberTag")).getLineNumber();
        }

        if (h.hasTag("SourceLineNumberTag")) {
            return ((SourceLineNumberTag) h.getTag("SourceLineNumberTag")).getLineNumber();
        }

        if (h.hasTag("SourceLnPosTag")) {
            return ((SourceLnPosTag) h.getTag("SourceLnPosTag")).startLn();
        }

        return -1;
    }

    public static PetriMethod storeCurrentPetriMethod(Petri youngPetri,
        List<ContextMethod> ctxtStack) {
        ContextMethod currentMethod = ctxtStack.get(ctxtStack.size() - 1);
        String cursig = currentMethod.getCurMsig(); 
        PetriMethod gcsMethod = null;
        gcsMethod = ICFGLowLevelPetriBuilder.buildPetriMethod(currentMethod,
                youngPetri, ctxtStack);
        PetriMethodManager.registerPetriMethod(cursig, gcsMethod);
        return gcsMethod;
    }

    public static void addControlLogic(Petri youngPetri, Matrix dc, Matrix uc,
        Set<State> states) {
        List<Place> places_humanRead = StateVectorGenerator.placeTemplate; // must be available

        List<Transition> transitions_humanRead = StateVectorGenerator.transtemplate;

        // ======================get the header ready now.
        if (uc.getRowDimension() != dc.getRowDimension()) {
            throw new RuntimeException("how many controllers are there?");
        }

        for (int i = 0; i < dc.getRowDimension(); i++) {
            PlaceControl pc = new PlaceControl();

            double[] rowDc = dc.getArray()[i];
            double[] rowUc = uc.getArray()[i];
            pc.setGadaratokens((int) rowUc[0]);
            youngPetri.coreG.addVertex(pc);

            for (int j = 0; j < rowDc.length; j++) {
                if (rowDc[j] != 0) {
                    double tmp = rowDc[j];

                    if (tmp < 0) { // t gets tokens from lockplace

                        Transition t = transitions_humanRead.get(j);
                        Arc arc = (ArcFromController) youngPetri.coreG.addEdge_edgetype_lpxz(pc,
                                t, ArcFromController.class); // duplication
                                                             // removal?

                        arc.setLabel("" + Math.abs(tmp));
                    } else {
                        Transition t = transitions_humanRead.get(j);
                        Arc arc = youngPetri.coreG.addEdge_edgetype_lpxz(t, pc,
                                ArcToController.class);
                        arc.setLabel("" + Math.abs(tmp));
                    }
                }
            }
        }
    }

    public static void pcr_raw_2_sootIR(List list) {
        List toremove = new ArrayList();

        for (Object elem : list) {
            CSMethodPair pair = (CSMethodPair) elem;
            CSMethod pcCS = pair.getO1(); // must be pc!
            CSMethod rCS = pair.getO2();

            // rCS.printIt();
            if (pcCS.getMethod_type().equals(CSMethod.PCMethod) &&
                    rCS.getMethod_type().equals(CSMethod.RMethod)) {
                String m1sig = pcCS.getMsig();
                String m2sig = rCS.getMsig();
                SootMethod pcm = Scene.v().getMethod(m1sig);
                SootMethod rm = Scene.v().getMethod(m2sig);

                String pString = pcCS.getpAnc(); // the format isis "jcode+ "
                                                 // " +line"

                String cString = pcCS.getcAnc();
                String rString = rCS.getrAnc();

                int pLine = pcCS.getpAncLine();
                int cLine = pcCS.getcAncLine();
                int rLine = rCS.getrAncLine();

                Unit punit = SootAgent4Fixing.getUnit(pcm, pString, pLine);
                Unit cunit = SootAgent4Fixing.getUnit(pcm, cString, cLine);
                Unit runit = SootAgent4Fixing.getUnit(rm, rString, rLine);

                if ((punit == null) || (cunit == null) || (runit == null)) {
                    throw new RuntimeException("check the pcr please");
                }

                pcCS.setPunit(punit);
                pcCS.setCunit(cunit);

                Body pcbb = pcm.getActiveBody();
                UnitGraph pcug = new BriefUnitGraph(pcbb); // give u one chance

                pcCS.setBb(pcbb);
                pcCS.setUg(pcug); // only set the unitref is useless, the
                                  // motherborad should be maintained too

                rCS.setRunit(runit);

                Body rbb = rm.getActiveBody();
                UnitGraph rug = new BriefUnitGraph(rbb);
                rCS.setBb(rbb);
                rCS.setUg(rug);
            } else {
                throw new RuntimeException("checking the type!");
            }
        }
    }
}
