package hk.ust.lpxz.petri.unit;

import soot.jimple.Stmt;

public class PlaceResource extends Place {

//public Stmt beforePrep = null; // prep is not that intereating in identifying the arrayElem
//	public Stmt getBeforePrep() {
//	return beforePrep;
//}
//public void setBeforePrep(Stmt beforePrep) {
//	this.beforePrep = beforePrep;
//}


	// From jimple view, it is hard to judge.
	// prep looks like this: x=$r;, and its pred is: $r=$r2[$i1];
	// For hashmap etc, it is much more difficult to judge.
	// So, the value is not quite reliable.
	public boolean maybeArrayEle = false;
	public boolean isMaybeArrayEle() {
		return maybeArrayEle;
	}
	public void setMaybeArrayEle(boolean maybeArrayEle) {
		this.maybeArrayEle = maybeArrayEle;
	}
	public PlaceResource()
	{
		
	}
	public PlaceResource(Object lockObjectPara, boolean mayPara)
	{
		lockObject =lockObjectPara;
		maybeArrayEle = mayPara;
	}
	public Object lockObject = null;
	public Object getLockObject() {
		return lockObject;
	}
	public void setLockObject(Object lockObject) {
		this.lockObject = lockObject;
	}
	@Override
	public String getDebugName() {
		// XXX Auto-generated method stub
		return "lockplace" + this.hashCode();
	}
}
