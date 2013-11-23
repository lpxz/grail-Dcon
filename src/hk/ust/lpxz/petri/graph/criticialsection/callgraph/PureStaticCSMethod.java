package hk.ust.lpxz.petri.graph.criticialsection.callgraph;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;

public class PureStaticCSMethod {

	public List ctxts = null;//new ArrayList();
	public SootMethod sm  = null;
	
	
	public List getCtxts() {
		return ctxts;
	}


	public void setCtxts(List ctxts) {
		this.ctxts = ctxts;
	}


	public SootMethod getSm() {
		return sm;
	}


	public void setSm(SootMethod sm) {
		this.sm = sm;
	}


	public PureStaticCSMethod(List ctxts, SootMethod sm) {		
		this.ctxts = ctxts;
		this.sm = sm;
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
