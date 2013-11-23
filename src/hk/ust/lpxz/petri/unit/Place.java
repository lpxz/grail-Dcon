package hk.ust.lpxz.petri.unit;

import hk.ust.lpxz.petri.graph.PetriMethod;
import hk.ust.lpxz.petri.graph.GadaraSupport.CommNamer;
import hk.ust.lpxz.petri.graph.criticalsection.PetriCriticalSection;

import java.util.List;
import java.util.Set;

import edu.hkust.clap.lpxz.context.ContextMethod;





public abstract class Place {
	public String specifiedName = null;
	public PetriMethod enclosingM = null;
	 
    
	public PetriMethod getEnclosingM() {
		return enclosingM;
	}
	public void setEnclosingM(PetriMethod enclosingM) {
		this.enclosingM = enclosingM;
	}
	
	
	public String toString()
	{
		if(specifiedName!=null) return specifiedName;
		
		return getPetriName();
	}
	//===========for supporting gadara 
	public String gadaraType = null;
	public int gadaratokens = -1;
	public void intialize(String gType , int gToken)
	{
		gadaraType = gType;
		gadaratokens = gToken;
		
	}
	public String getGadaraType() {
		return gadaraType;
	}

	public void setGadaraType(String gadaraType) {
		this.gadaraType = gadaraType;
	}

	public int getGadaratokens() {
		return gadaratokens;
	}

	public void setGadaratokens(int gadaratokens) {
		this.gadaratokens = gadaratokens;
	}
	
	//============================
	
	
	



	//=========== for easier debugging only.:  AND, for dump to the gadara format

	 public abstract String getDebugName() ;
	 
	 public String getPetriName() {
		return CommNamer.getPetriNetName(this);
	}

	
	//===========

//	List<MethodItsCallSitePair> ctxts  ;
//	 
//    public List<MethodItsCallSitePair> getCtxts() {
//		return ctxts;
//	}
//
//	public void setCtxts(List<MethodItsCallSitePair> ctxtsarg) {
//		this.ctxts = ctxtsarg;
//	}
//
//
//	public String getMsig() {
//		return msig;
//	}
//
//	public void setMsig(String msig) {
//		this.msig = msig;
//	}
//
//	String msig ;
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


}
