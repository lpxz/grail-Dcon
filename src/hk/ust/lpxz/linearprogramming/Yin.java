package hk.ust.lpxz.linearprogramming;

import hk.ust.lpxz.IO.Reader;
import hk.ust.lpxz.SBPI.ToMatrix;
import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.petri.graph.Petri;
import hk.ust.lpxz.petri.unit.Place;
import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
import hk.ust.lpxz.petri.unit.PlaceResource;
import hk.ust.lpxz.petri.unit.Transition;
import hk.ust.lpxz.statemachine.State;
import hk.ust.lpxz.statemachine.StateVectorGenerator;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.io.*;



import Jama.Matrix;

import soot.toolkits.scalar.Pair;

import lpsolve.*;

public class Yin {
	public static double empsilon = 0.1;
	public static int   M = 100;
  public static void main(String[] args) {
	  System.out.println(System.getProperty("java.library.path"));
//	 constraintSolve("/home/lpxz/eclipse/workspace/Dcon/src/hk/ust/lpxz/linearprogramming/markings");	  
	// constraintSolve("/home/lpxz/eclipse/workspace/Dcon/src/hk/ust/lpxz/linearprogramming/markings", new ArrayList<List>());	  
	
  }
  


    
	 
//	public static void constraintSolve(String filename)
//	{
//		
//	//	String testfile = "/home/lpxz/eclipse/workspace/pecan/pecan-monitor/src/linearprogramming/markings";
//		List<String> goods = new ArrayList<String>();
//		List<String> bads = new ArrayList<String>();
//		ArrayList<String> constraints = new ArrayList<String>();
//		try {
//			Reader.readMarkingFile(new File(filename), goods, bads);
//
////			System.out.println(goods.size());
////			System.out.println(bads.size());
//			int total = goods.size() + bads.size();
//			int token_no =-1;
//			// note : I change Aij to xij, theta to y, z is z.
//			// section 1: add good
//			for(String good : goods)
//			{
//				token_no= good.split(" ").length;
//				constraints.addAll(genConstraints4Good(total, good));
//			}
//			// section 2: add bad, and also the theta equation and binary declaration
//			int badId4DisTheta =0;
//			for(String bad:bads)
//			{
//				badId4DisTheta++;
//				constraints.addAll(genConstraints4Bad(total, bad, badId4DisTheta));
//			}
//			
//			// section3: add Aij range, i.e., 0<=xij<=z and z is binary
//			constraints.addAll(genConstraints4Indicator(total, token_no));
//			
//			
//			
//			
//			// get the goal function:
//			FileWriter fstream = new FileWriter("/home/lpxz/eclipse/workspace/Dcon/src/hk/ust/lpxz/linearprogramming/constraints");
//			  BufferedWriter out = new BufferedWriter(fstream);
//			  
//			  String goal = genGoal(total);
//			//	System.out.println(goal);
//			  out.write(goal+"\n" );
//            
//			  List<String> bincache = new ArrayList<String>();
//			for(String tmp:constraints)
//			{
//				// cache it, it should be placed at the bottom, so,,
//				if(tmp.startsWith("bin "))
//				{
//					bincache.add(tmp);
//				}
//				else {
//			//		System.out.println(tmp);
//					 out.write(tmp+"\n");
//				}
//			
//			}
//			
//			for(String bin:bincache)
//			{
//			//	System.out.println(bin);
//				 out.write(bin+"\n");
//			}
//			
//			out.flush();
//			out.close();
//			
//			
//			//============================================solve:
//			 LpSolve solver =   LpSolve.readLp("/home/lpxz/eclipse/workspace/Dcon/src/hk/ust/lpxz/linearprogramming/constraints", 0, null);
//		      // add constraints
//		
//			 solver.setTimeout(DconPropertyManager.timeout);
//			
//		      // solve the problem
//		      solver.solve();
//
//		      // print solution
//
//		      solver.getPtrPrimalSolution();
//
//		    double[] argsss= new  double[total];
//		    HashMap<String, Double> results = new HashMap<String, Double>();
//		    argsss =solver.getPtrVariables();
//		    for(int i=1;i<=argsss.length; i++)
//		    {
//		    	results.put(solver.getColName(i), argsss[i-1]);
//		    }
//		    formatResult(total, token_no, results);
//		    
//		      
//		      solver.deleteLp();
//			
//			
//		}catch (Exception e) {
//		 e.printStackTrace();
//		}		
//	}

	
	public static void  constraintSolve(Set<State> goodstates, Set<State> badstates,Pair<Matrix, Matrix> pair)
	{
	//	String testfile = "/home/lpxz/eclipse/workspace/pecan/pecan-monitor/src/linearprogramming/markings";
		List<String> goods = new ArrayList<String>();
		List<String> bads = new ArrayList<String>();
		ArrayList<String> constraints = new ArrayList<String>();
		try {
			String marking= DconPropertyManager.markingFile;
			StateVectorGenerator.output(goodstates, badstates ,marking);	
			Reader.readMarkingFile(new File(marking), goods, bads);// just reuse the following code. I do not want to re-produce the constraints from states.
			

			int total = goods.size() + bads.size();
			int placeNo =-1;
			// note : I change Aij to xij, theta to y, z is z.
			// section 1: add good
			for(String good : goods)
			{
				placeNo= good.split(" ").length;
				constraints.addAll(genConstraints4Good(total, good));
			}
			// section 2: add bad, and also the theta equation and binary declaration
			int badId4DisTheta =0;
			for(String bad:bads)
			{
				badId4DisTheta++;
				constraints.addAll(genConstraints4Bad(total, bad, badId4DisTheta));
			}
			
			// section3: l[t.]-l[.t] + M Rt,i >0
			// Rt,i = 0 or 1.
			constraints.addAll(getConstraints4LockAcqs(total));
			
			
			
			
			// get the goal function:
			FileWriter fstream = new FileWriter(DconPropertyManager.spyrosFile);
			  BufferedWriter out = new BufferedWriter(fstream);
			  
			  String goal = genGoal(total);
			 
			  out.write(goal+"\n" );
            
			  List<String> bincache = new ArrayList<String>();
			for(String tmp:constraints)
			{
				// cache it, it should be placed at the bottom, so,,
				if(tmp.startsWith("bin "))
				{
					bincache.add(tmp);
				}
				else {
					
					 out.write(tmp+"\n");
				}
			
			}
			
			for(String bin:bincache)
			{
								
				 out.write(bin+"\n");
			}
			
			out.flush();
			out.close();
			
			//============================================solve:
			 LpSolve solver =   LpSolve.readLp(DconPropertyManager.spyrosFile, 0, null);
		      // add constraints
			
			 solver.setTimeout(DconPropertyManager.timeout);
			
		      // solve the problem
		      solver.solve();
		     
		    

		      // print solution

		      solver.getPtrPrimalSolution();

		    double[] argsss= new  double[total];
		    HashMap<String, Double> results = new HashMap<String, Double>();
		    argsss =solver.getPtrVariables();
		    for(int i=1;i<=argsss.length; i++)
		    {
		    	results.put(solver.getColName(i), argsss[i-1]);
		    }
		    
		    
		  //==================build list:
		  
			
			List<List> rawL = new ArrayList<List>();
			List<List> rawB = new ArrayList<List>();
			for(int  i=1; i<=total; i++)
			{
				List lb = new ArrayList();
				for(int j=1; j<=placeNo;j++)
				{					
					lb.add(mult10Round(results.get("x"+i+""+j)) );		//mult10Round		
				}
				List lb2 = new ArrayList();
				lb2.add(mult10Round(results.get("b"+i)));//mult10Round
			   if(!allzero(lb)) // nothing meaningful
			   {
				   rawL.add(lb);
				   rawB.add(lb2);
			   }			  
			}
			 
			Matrix L =ToMatrix.toMatrix(rawL);
			Matrix B = ToMatrix.toMatrix(rawB);
			
			
			if(DconPropertyManager.showConstraint)
			{
				System.out.println("L:");
			    L.print(2, 2);    
				System.out.println("");
				System.out.println("B:");
			    B.print(2, 2); 
			}
			
			pair.setO1(L);
			pair.setO2(B);
			
			
		      
		      solver.deleteLp();
			
			
		}catch (Exception e) {
		 e.printStackTrace();
		}		
	}


	static HashSet<String> goalItems = new HashSet<String>();
	private static List<String> getConstraints4LockAcqs(
			int total) {
		goalItems.clear();
		ArrayList<String> cons4bad = new ArrayList<String>();
		List<Transition> trans =StateVectorGenerator.transtemplate;
		List<Place> places =StateVectorGenerator.placeTemplate;
		
		for(Transition tran:trans)	
		{
			for(int  i=1; i<=total; i++) // i means the ith constraint
			{
				StringBuilder sb = new StringBuilder();
				List precs =Petri.getPetri().getAllPrecs(tran);
				List succs =Petri.getPetri().getAllSuccs(tran);
				Place prec =(Place) precs.get(0);
				Place succ =(Place) succs.get(0);
				if(prec instanceof PlaceResource)
				{					
					prec= (Place) precs.get(1);
					if(prec instanceof PlaceResource)
						throw new RuntimeException("erro");
					
				}
				if(succ instanceof PlaceResource)
				{
					succ= (Place) succs.get(1);
					if(succ instanceof PlaceResource)
					{
						 throw new RuntimeException("wrong");
					}			
					  
				}
				int precIndex = places.indexOf(prec);// +1, starting with 1
				int succIndex = places.indexOf(succ);
				if(precIndex==-1 || succIndex==-1)
					throw new RuntimeException("all are here");// the place is not included, its coefficient is 0 by nature. must be smaller, trivial
				sb.append( "x"+i+""+(succIndex+1) + " - " + "x"+i+""+(precIndex+1) + " + " + M + " " + "R"+  (trans.indexOf(tran)+1)+ ""+i + " >= " + empsilon + ";");
				cons4bad.add(sb.toString());
				cons4bad.add("bin " + "R"+  (trans.indexOf(tran)+1)+ ""+i + ";");
				goalItems.add("R"+  (trans.indexOf(tran)+1)+ ""+i);
			}
		}	
		return cons4bad;
	}





	private static boolean allzero(List lb) {
		for(Object o : lb)
		{
			Double long1 = (Double)o;
			if(long1.doubleValue()!=0)
			{
				return false;
			}
		}
		
		return true;
	}




	private static ArrayList<String> formatResult(int total, int token_no, HashMap result) {
		ArrayList<String> cons4good = new ArrayList<String>();	
		
		StringBuilder sb = new StringBuilder();
		for(int  i=1; i<=total; i++)
		{
			for(int j=1; j<=token_no;j++)
			{
				String tokenj = "p"+j;
				sb.append(mult10Round(result.get("x"+i+""+j)) +"*" + tokenj+ " +");
			}
		    int extra =sb.lastIndexOf("+");
		    String tmp = sb.substring(0, extra) + " <= " + mult10Round(result.get("b"+i)) + ";";
		    sb.delete(0, sb.length());
		    System.out.println(tmp);
		    cons4good.add(tmp);
		}
		 
		return cons4good;
	}
	
	public static double mult10Round(Object o)
	{
		String str = o.toString();
		double dd = Double.parseDouble(str);
		return Math.round(dd*DconPropertyManager.multipleWhat);
	}
	private static String genGoal(int total) {
		String tmp = "max: ";
		ArrayList<String> cons4bad = new ArrayList<String>();
		List<Transition> trans =StateVectorGenerator.transtemplate;
		List<Place> places =StateVectorGenerator.placeTemplate;
		StringBuilder sb = new StringBuilder();
		for(String item : goalItems)
		{
			sb.append(item + " + ");			
		}
		
		String sbstring = sb.toString();
		
		int extra =sbstring.lastIndexOf("+");
		 tmp = tmp + sbstring.substring(0, extra) + ";";
		return tmp;
	}


	private static ArrayList<String> genConstraints4Indicator(
			int total, int tokenNo) {
		ArrayList<String> cons4Indi = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		for(int  i=1; i<=total; i++)
		{
			for(int j=1; j<=tokenNo;j++)
			{
				
				sb.append( "0"+ " <= " + "x"+i+""+j +";"); // ,+ zi // + " <= " + "z" + i + 
				sb.append(  "x"+i+""+j + " - " +  "z" + i + " <= 0;" );
			}
		   
		    String tmp = sb.toString();
		    sb.delete(0, sb.length());
		    cons4Indi.add(tmp);
		}
		
		String tmp2 = "";
		for(int  i=1; i<=total; i++)
		{
			   tmp2 += ("z"+ i) + " +";				   
		}
		
		int extra2 =tmp2.lastIndexOf("+");
		 tmp2 = tmp2.substring(0, extra2) +" >="+empsilon+";";
		 cons4Indi.add(tmp2);
		 
		
		String tmp = "bin ";
			for(int  i=1; i<=total; i++)
			{
				   tmp += ("z"+ i) + ", ";				   
			}
			
			int extra =tmp.lastIndexOf(",");
			 tmp = tmp.substring(0, extra) + ";";// avoid the bug of lpsolver. otherwise, it does not find a solution
			 cons4Indi.add(tmp);
			 
		return cons4Indi;
	}

	private static ArrayList<String> genConstraints4Good(int total, String good) {
		ArrayList<String> cons4good = new ArrayList<String>();
		String[] tokens = good.split(" ");
		
		StringBuilder sb = new StringBuilder();
		for(int  i=1; i<=total; i++)
		{
			for(int j=1; j<=tokens.length;j++)
			{
				String tokenj = tokens[j-1];
				sb.append(tokenj + " " + "x"+i+""+j + " +");
			}
		    int extra =sb.lastIndexOf("+");
		    String tmp = sb.substring(0, extra) + " <= b" + i + ";";
		    sb.delete(0, sb.length());
		    cons4good.add(tmp);
		}
		 
		return cons4good;
	}
	
	
	 private static ArrayList<String> genConstraints4Bad(int total, String bad, int badId4DisTheta) {
		
            
			ArrayList<String> cons4bad = new ArrayList<String>();
			String[] tokens = bad.split(" ");
			
			StringBuilder sb = new StringBuilder();
			for(int  i=1; i<=total; i++)
			{
				for(int j=1; j<=tokens.length;j++)
				{
					String tokenj = tokens[j-1];
					sb.append(tokenj + " " + "x"+i+""+j + " +");
				}
			    int extra =sb.lastIndexOf("+");
			    String tmp = sb.substring(0, extra) + "+" + M + " "+ ("y"+ i + "badNo" + badId4DisTheta) + " >= b" + i + " +" + empsilon + ";";
			    sb.delete(0, sb.length());
			    cons4bad.add(tmp);
			    
			}
			String tmp = "";
			for(int  i=1; i<=total; i++)
			{
				   tmp += ("y"+ i+ "badNo" + badId4DisTheta) + " +" ;				   
			}
			 int extra =tmp.lastIndexOf("+");
			 tmp = tmp.substring(0, extra) + " <= " + total + "-1" + ";";
			 cons4bad.add(tmp);
			 
//				String tmp2 = "";
//				for(int  i=1; i<=total; i++)
//				{
//					   tmp2 += ("y"+ i+ "badNo" + badId4DisTheta) + " +" ;				   
//				}
//				 int extra2 =tmp2.lastIndexOf("+");
//				 tmp2 = tmp2.substring(0, extra2) + " >= " + empsilon+ ";";
//				 cons4bad.add(tmp2);
				 // no need, just bound the M is enough
				 
				 tmp = "bin ";
				for(int  i=1; i<=total; i++)
				{
					   tmp += ("y"+ i+ "badNo" + badId4DisTheta) + ", ";				   
				}
				
				 extra =tmp.lastIndexOf(",");
				 tmp = tmp.substring(0, extra) + ";";
				 cons4bad.add(tmp);
			 
			return cons4bad;
		
		
	}


}

