package hk.ust.lpxz.petri.unit;

import soot.jimple.Stmt;

public class PlaceCommonLocal extends Place{
	public PlaceCommonLocal(String specName)
	{
		specifiedName = specName;
		
	}
	public PlaceCommonLocal()
	{
		
		
	}
	public Stmt jimpleStmt  ;

	public Stmt getJimpleStmt() {
		return jimpleStmt;
	}

	public void setJimpleStmt(Stmt jimpleStmt) {
		this.jimpleStmt = jimpleStmt;
	}
	
	public int jimpleStmtLine = -1;
	
	public int getJimpleStmtLine() {
		return jimpleStmtLine;
	}

	public void setJimpleStmtLine(int jimpleStmtLine) {
		this.jimpleStmtLine = jimpleStmtLine;
	}

	public  boolean isInvoke()
	{
		return jimpleStmt.containsInvokeExpr();
	}
//	public String toString()
//	{
//		return jimpleStmt.toString();
//	}
	
	//
	public String msigStmtInLoading = "";

	public String getMsigStmtInLoading() {
		return msigStmtInLoading;
	}

	public void setMsigStmtLineInLoading(String msigStmtInLoading) {
		this.msigStmtInLoading = msigStmtInLoading;
	}

	@Override
	public String getDebugName() {
		// XXX Auto-generated method stub
		return jimpleStmt + ""+jimpleStmtLine ;
	}
	
}
