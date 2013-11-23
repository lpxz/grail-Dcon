package hk.ust.lpxz.LockSynthesis.planner;

import soot.SootMethod;
import soot.Unit;
import soot.toolkits.scalar.Pair;



public class LockOperationTask {
	// method, stmt, successor, +5/-5, lockID
	public SootMethod method ;
	public Unit stmt ;
	public Unit successor;
	public int tokensMoved =0;
	public int lockID=-1; // int is sufficient.
	
	public LockOperationTask(SootMethod method, Unit stmt, Unit successor,
			int tokensMoved, int lockID) {		
		this.method = method;
		this.stmt = stmt;
		this.successor = successor;
		this.tokensMoved = tokensMoved;
		this.lockID = lockID;
		
	}
	
	public String toString()
	{
		return method.getName() + " " + stmt.toString() + " " + successor.toString() + " " + tokensMoved + " from/to " + lockID;
	}
	
	public int hashCode() {
    	return method.hashCode() + stmt.hashCode() + successor.hashCode() + 1000* lockID;
    }
    public boolean equals( LockOperationTask other ) {
        if(method==other.method && stmt==other.stmt && successor==other.successor && tokensMoved==other.tokensMoved && lockID==other.lockID)
        	return true;
        return false;
    }
	
	

}
