package hk.ust.lpxz.petri.graph;

import hk.ust.lpxz.petri.unit.Arc;
import hk.ust.lpxz.petri.unit.ArcFromResource;
import hk.ust.lpxz.petri.unit.ArcLocal;
import hk.ust.lpxz.petri.unit.ArcToResource;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedPseudograph;


public class Petri {
    // additional tags..
    public static Petri _petri = null;
    public boolean initialized = false;
    public List<Place> places = new ArrayList<Place>();
    public List<Transition> transitions = new ArrayList<Transition>(); // 
    public Set<Arc> snapshot = new HashSet<Arc>();
    public HashMap<Arc, Arc> revertedArc2Arc = new HashMap<Arc, Arc>();

    // =========================
    public HashSet<PetriMethod> threadRoots;
    public DirectedPseudograph<Object, Arc> coreG = null;

    public Petri() {
        coreG = new DirectedPseudograph<Object, Arc>(Arc.class);
        threadRoots = new HashSet<PetriMethod>();
    }

    public void revertArcs() {
        Set<Arc> arcs = coreG.edgeSet();
        snapshot.clear();
        snapshot.addAll(arcs);

        for (Arc arc : snapshot) {
            Class newclazz = null;

            if (arc.getClass().equals(ArcFromResource.class)) {
                newclazz = ArcToResource.class;
            } else if (arc.getClass().equals(ArcToResource.class)) {
                newclazz = ArcFromResource.class;
            } else {
                newclazz = arc.getClass();
            }

            Arc revertedArc = coreG.addEdge_edgetype_lpxz(arc.getTarget(),
                    arc.getSource(), newclazz);
            revertedArc2Arc.put(revertedArc, arc);
        }

        coreG.removeAllEdges(snapshot); 
    }

    public void revertBackArcs() {
        Set<Arc> revertedarcs = coreG.edgeSet();
        snapshot.clear();
        snapshot.addAll(revertedarcs);

        for (Arc revertedarc : snapshot) {
            Arc arc = revertedArc2Arc.get(revertedarc);
            coreG.addEdge(revertedarc.getTarget(), revertedarc.getSource(), arc);
        }

        coreG.removeAllEdges(snapshot);
    }

    public List<Place> getPlaces() {
        if (!isPTready()) {
            getPTready();
        }

        return places;
    }

    public List<Transition> getTransitions() {
        if (!isPTready()) {
            getPTready();
        }

        return transitions;
    }

    public boolean isPTready() {
        return !(places.isEmpty() || transitions.isEmpty());
    }

    public void getPTready() {
        for (Object o : this.coreG.vertexSet()) {
            if (o instanceof Place) {
                if (!places.contains(o)) {
                    places.add((Place) o);
                }
            } else if (o instanceof Transition) {
                if (!transitions.contains(o)) {
                    transitions.add((Transition) o);
                }
            } else {
                throw new RuntimeException("what kind?");
            }
        }
    }

    public HashSet<PetriMethod> getThreadRoots() {
        return threadRoots;
    }

    public void setThreadRoots(HashSet<PetriMethod> threadRoots) {
        this.threadRoots = threadRoots;
    }

    public static Petri getPetri() {
        if (_petri == null) {
            throw new RuntimeException("not ready yet");
        }

        return _petri;
    }

    public List getLocalSuccs(Object pop) {
        List list = new ArrayList();
        Set<Arc> edges = this.coreG.outgoingEdgesOf(pop);

        for (Arc edge : edges) {
            if (edge instanceof ArcLocal) {
                list.add(edge.getTarget());
            }
        }
        return list;
    }

    public List getLocalSuccArcs(Object pop) {
        List list = new ArrayList();
        Set<Arc> edges = this.coreG.outgoingEdgesOf(pop);

        for (Arc edge : edges) {
            if (edge instanceof ArcLocal) {
                list.add(edge);
            }
        }

        return list;
    }

    public List getAllSuccs(Object pop) {
        List list = new ArrayList();
        Set<Arc> edges = this.coreG.outgoingEdgesOf(pop);

        for (Arc edge : edges) // commonly, one transition for one place. The
                               // join/start is not explicitly modeled
         {
            list.add(edge.getTarget());
        }

        return list;
    }

    public List getAllSuccEdges(Object pop) {
        List list = new ArrayList();
        Set<Arc> edges = this.coreG.outgoingEdgesOf(pop);

        for (Arc edge : edges) {
            list.add(edge);
        }

        return list;
    }

    public List getLocalPrecs(Object pop) {
        List list = new ArrayList();
        Set<Arc> edges = this.coreG.incomingEdgesOf(pop);

        for (Arc edge : edges) {
            if (edge instanceof ArcLocal) {
                list.add(edge.getSource());
            }
        }

        return list;
    }

    public List getLocalPrecArcs(Object pop) {
        List list = new ArrayList();
        Set<Arc> edges = this.coreG.incomingEdgesOf(pop);

        for (Arc edge : edges) {
            if (edge instanceof ArcLocal) {
                list.add(edge);
            }
        }

        return list;
    }

    public List getAllPrecs(Object pop) {
        List list = new ArrayList();
        Set<Arc> edges = this.coreG.incomingEdgesOf(pop);

        for (Arc edge : edges) {
            list.add(edge.getSource());
        }

        return list;
    }

    public List getAllPrecEdges(Object pop) {
        List list = new ArrayList();
        Set<Arc> edges = this.coreG.incomingEdgesOf(pop);

        for (Arc edge : edges) {
            list.add(edge);
        }

        return list;
    }

    public static Petri instantiate() {
        Petri._petri = new Petri();

        return Petri._petri;
    }

    public static boolean isReady() {
        return _petri != null;
    }

    public static Petri instance() {
        return _petri;
    }
    
    public static Petri rebuild() {
       _petri =null;
       Petri._petri = new Petri();
       return _petri;
    }
}
