package hk.ust.lpxz.petri.unit;

import org.jgrapht.graph.DefaultEdge;


public abstract class Arc extends DefaultEdge {
    //==========for supporting gadara
    public int weight = -1;

    //============
    public String petriName = "";

    public boolean equals(Object arcpara) {
        if (!(arcpara instanceof Arc)) {
            return false;
        } else {
            Arc arcparatmp = (Arc) arcpara;

            return this.getSource().equals(arcparatmp.getSource()) &&
            this.getTarget().equals(arcparatmp.getTarget());
        }
    }

    public void intialize(int w) {
        weight = w;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getPetriName() {
        return petriName;
    }

    public void setPetriName(String petriName) {
        this.petriName = petriName;
    }
}
