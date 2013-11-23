package hk.ust.lpxz.linearprogramming;

import hk.ust.lpxz.IO.Reader;
import hk.ust.lpxz.SBPI.SBPI;
import hk.ust.lpxz.SBPI.ToMatrix;
import hk.ust.lpxz.fixing.DconPropertyManager;
import hk.ust.lpxz.fixing.Reporter;
import hk.ust.lpxz.statemachine.State;
import hk.ust.lpxz.statemachine.StateVectorGenerator;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.io.*;

import org.antlr.works.visualization.serializable.SSerializable;


import Jama.Matrix;

import soot.toolkits.scalar.Pair;

import lpsolve.*;

public class Spyro {
	public static double empsilon = 0.1;
	public static int   M = 100;
  public static void main(String[] args) {
	//  System.out.println(System.getProperty("java.library.path"));
	// constraintSolve("/home/lpxz/eclipse/workspace/Dcon/src/hk/ust/lpxz/linearprogramming/markings");	  
	// constraintSolve("/home/lpxz/eclipse/workspace/Dcon/src/hk/ust/lpxz/linearprogramming/markings", new ArrayList<List>());	  
	
  }
  


//	public static Set<String> varStrings =new HashSet<String>();
	public static Set<String> constraintStrings =new HashSet<String>();
	 
//	public static void constraintSolve(String filename)
//	{
//		
//	//	String testfile = "/home/lpxz/eclipse/workspace/pecan/pecan-monitor/src/linearprogramming/markings";
//		List<String> goods = new ArrayList<String>();
//		List<String> bads = new ArrayList<String>();
//		ArrayList<String> constraints = new ArrayList<String>();
//		try {
//			String source = Reader.readMarkingFile(new File(filename), goods, bads);
//			System.out.println("======marking====");
//			System.out.println(source);
//			System.out.println("=================");
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
//			constraintStrings.addAll(constraints);
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

	public static int varNo = 0;
	public static int inequalityNo =0;
	public static List tmpList = new ArrayList(); // you can also create tmoList for each iteration when encessary
	public static void  constraintSolve(Set<State> goodstates, Set<State> badstates, Pair<Matrix, Matrix> pair) throws FileNotFoundException
	{
		

		System.out.println("spyros version");
		
		try {
		
			int total = goodstates.size() + badstates.size();
			
			// get the goal function:
			FileWriter fstream = new FileWriter(DconPropertyManager.spyrosFile);
			  BufferedWriter out = new BufferedWriter(fstream);
			  
			  String goal = genGoal(total);
			  out.write(goal+"\n" );
			  
			  List<String> goods = new LinkedList<String>();
			  List<String> bads = new LinkedList<String>();
			  boolean hijacking= Reader.readMarkingFile(new File(DconPropertyManager.markingFile), goods, bads);
            
			  List<String> bincache = new ArrayList<String>();
			  int token_no =-1;
			  if(hijacking)
			  {
					for(String good : goods)
					{
						tmpList.clear();
						String[] tmp = good.split(" ");
						for(String str : tmp)
						{
							tmpList.add(str);
						}
						 token_no= tmpList.size();
						genConstraints4Good(total, tmpList,out);	
					}
					
				
					// section 2: add bad, and also the theta equation and binary declaration
					
					int badId4DisTheta =0;
					for(String bad: bads)
					{
						badId4DisTheta++;
						tmpList.clear(); // you can also create tmoList for each iteration when encessary
						String[] tmp = bad.split(" ");
						for(String str : tmp)
						{
							tmpList.add(str);
						}
						bincache.addAll(genConstraints4Bad(total, tmpList, badId4DisTheta,out));
					}
				  
			  }else {
					String marking= DconPropertyManager.markingFile;
					StateVectorGenerator.output(goodstates, badstates ,marking);	
	   			 
					for(State good : goodstates)
					{				
						tmpList.clear();
						 StateVectorGenerator.getNumericRep(good, tmpList);
						 token_no= tmpList.size();
						 
						genConstraints4Good(total, tmpList,out);
					}
				
					// section 2: add bad, and also the theta equation and binary declaration
					
					int badId4DisTheta =0;
					for(State bad:badstates)
					{
						badId4DisTheta++;
						tmpList.clear(); // you can also create tmoList for each iteration when encessary
						 StateVectorGenerator.getNumericRep(bad, tmpList);
						 bincache.addAll(genConstraints4Bad(total, tmpList, badId4DisTheta,out));
					}
			   }
			
				
				// section3: add Aij range, i.e., 0<=xij<=z and z is binary
				bincache.addAll(genConstraints4Indicator(total, token_no,out));
				
				
				
			  
			
			for(String bin:bincache)
			{
				 out.write(bin+"\n");
			}
			
			out.flush();
			out.close();
			
	    	Reporter.reportDirectly("vars " , varNo);
			Reporter.reportDirectly("inequalities", inequalityNo);
			//============================================solve:
			 LpSolve solver =   LpSolve.readLp(DconPropertyManager.spyrosFile, 0, null);
		      // add constraints
			
			 solver.setTimeout(10000000);
			
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
				for(int j=1; j<=token_no;j++)
				{					
					lb.add(mult10Round(results.get("l"+i+""+j)) );		//mult10Round		
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

	
// lpxz: reduce the inter-module computation.	
//	public static void  constraintSolve(String filename, Pair<Matrix, Matrix> pair)
//	{
//		System.out.println("spyros version");
//		
//	//	String testfile = "/home/lpxz/eclipse/workspace/pecan/pecan-monitor/src/linearprogramming/markings";
//		List<String> goods = new ArrayList<String>();
//		List<String> bads = new ArrayList<String>();
//		ArrayList<String> constraints = new ArrayList<String>();
//		try {
//			 Reader.readMarkingFile(new File(filename), goods, bads);
////			if(DconPropertyManager.showMarking) //lpxz:remove inter-module communication 
////			{
////				System.out.println("======marking====");
////				System.out.println(source);
////				System.out.println("=================");
////			}			
//
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
//			FileWriter fstream = new FileWriter(DconPropertyManager.spyrosFile);
//			  BufferedWriter out = new BufferedWriter(fstream);
//			  
//			  String goal = genGoal(total);
//			if(DconPropertyManager.showSpyrosEquation)
//			{
//				 System.out.println(goal);
//			}
//			 
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
//					if(DconPropertyManager.showSpyrosEquation)
//					{
//						System.out.println(tmp);
//					}
//				
//					 out.write(tmp+"\n");
//				}
//			
//			}
//			
//			for(String bin:bincache)
//			{
//				if(DconPropertyManager.showSpyrosEquation)
//				{
//					System.out.println(bin);
//				}
//				
//				 out.write(bin+"\n");
//			}
//			
//			out.flush();
//			out.close();
//			
//	    	Reporter.reportDirectly("vars " , Spyro.varStrings.size());
//			Reporter.reportDirectly("inequalities", Spyro.constraintStrings.size());
//			//============================================solve:
//			 LpSolve solver =   LpSolve.readLp(DconPropertyManager.spyrosFile, 0, null);
//		      // add constraints
//			
//			 solver.setTimeout(DconPropertyManager.timeout);
//			
//		      // solve the problem
//		      solver.solve();
//		     
//		    
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
//		    
//		    
//		  //==================build list:
//		  
//			
//			List<List> rawL = new ArrayList<List>();
//			List<List> rawB = new ArrayList<List>();
//			for(int  i=1; i<=total; i++)
//			{
//				List lb = new ArrayList();
//				for(int j=1; j<=token_no;j++)
//				{					
//					lb.add(mult10Round(results.get("x"+i+""+j)) );		//mult10Round		
//				}
//				List lb2 = new ArrayList();
//				lb2.add(mult10Round(results.get("b"+i)));//mult10Round
//			   if(!allzero(lb)) // nothing meaningful
//			   {
//				   rawL.add(lb);
//				   rawB.add(lb2);
//			   }			  
//			}
//			 
//			Matrix L =ToMatrix.toMatrix(rawL);
//			Matrix B = ToMatrix.toMatrix(rawB);
//			
//			
//			if(DconPropertyManager.showConstraint)
//			{
//				System.out.println("L:");
//			    L.print(2, 2);    
//				System.out.println("");
//				System.out.println("B:");
//			    B.print(2, 2); 
//			}
//			
//			pair.setO1(L);
//			pair.setO2(B);
//			
//			
//		      
//		      solver.deleteLp();
//			
//			
//		}catch (Exception e) {
//		 e.printStackTrace();
//		}		
//	}


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
		String tmp = "min: ";
		for(int  i=1; i<=total; i++)
		{
			   tmp += ("z"+ i) + "+ ";				   
		}
		
		int extra =tmp.lastIndexOf("+");
		 tmp = tmp.substring(0, extra) + ";";
		return tmp;
	}


	
	private static ArrayList<String> genConstraints4Indicator(
			int total, int tokenNo, BufferedWriter out) throws IOException {
		
		
		for(int  i=1; i<=total; i++)
		{
			sb.delete(0, sb.length());
			for(int j=1; j<=tokenNo;j++)
			{
				
				sb.append( "0"+ " <= " + "l"+i+""+j +";"); // ,+ zi // + " <= " + "z" + i + 
				sb.append(  "l"+i+""+j + " - " +  "z" + i + " <= 0;" );
				inequalityNo++;
				inequalityNo++;				
			}
		   
		    out.write(sb.toString() + "\n");
		}
		
		String tmp2 = "";
		for(int  i=1; i<=total; i++)
		{
			   tmp2 += ("z"+ i) + " +";			
			   varNo++;
		}
		
		int extra2 =tmp2.lastIndexOf("+");
		 tmp2 = tmp2.substring(0, extra2) +" >="+empsilon+";";
		 inequalityNo++;		
		 out.write(tmp2 + "\n");
		 
		 cons4bad.clear();
		String tmp = "bin ";
			for(int  i=1; i<=total; i++)
			{
				   tmp += ("z"+ i) + ", ";				   
			}
			
			int extra =tmp.lastIndexOf(",");
			 tmp = tmp.substring(0, extra) + "; ";// avoid the bug of lpsolver. otherwise, it does not find a solution
			 cons4bad.add(tmp );
			 return cons4bad;
			 
		
	}

//	private static ArrayList<String> genConstraints4Good(int total, String good) {
//		ArrayList<String> cons4good = new ArrayList<String>();
//		String[] tokens = good.split(" ");
//		
//		StringBuilder sb = new StringBuilder();
//		for(int  i=1; i<=total; i++)
//		{
//			for(int j=1; j<=tokens.length;j++)
//			{
//				String tokenj = tokens[j-1];
//				sb.append(tokenj + " " + "x"+i+""+j + " +");
//				varStrings.add("x" + i + "" + j);
//			}
//		    int extra =sb.lastIndexOf("+");
//		    String tmp = sb.substring(0, extra) + " <= b" + i + ";";
//		    sb.delete(0, sb.length());
//		    cons4good.add(tmp);
//		}
//		 
//		return cons4good;
//	}
	
	public static StringBuilder sb = new StringBuilder();

	private static void genConstraints4Good(int total, List good, BufferedWriter out) throws IOException {
		
		sb.delete(0, sb.length());
		for(int  i=1; i<=total; i++) 
		{
			for(int j=1; j<=good.size();j++)
			{
				Object tokenj = (Object)good.get(j-1);
				sb.append(tokenj );
				sb.append(" l" );
				sb.append(i );
				sb.append(j );
				if(j!=good.size())
				  sb.append(" +");
			}
		    sb.append(" <= b");
		    sb.append(i);
		    sb.append(";\n");  // equation 2.
		   
		    out.write(sb.toString());
		   
		    sb.delete(0, sb.length());// clear the sb to save the space.
		}
		 
		
	}

	public static ArrayList<String> cons4bad = new ArrayList<String>();
	 private static ArrayList<String> genConstraints4Bad(int total, List bad, int badId4DisTheta
			 ,BufferedWriter out ) throws IOException {
		 cons4bad.clear();
			for(int  i=1; i<=total; i++)
			{
				 sb.delete(0, sb.length());	
				for(int j=1; j<=bad.size();j++)
				{
					Object tokenj = (Object) bad.get(j-1);
					sb.append(tokenj);
					sb.append(" ");
					sb.append("l");
					sb.append(i);
					sb.append(j);
					if(j!=bad.size())
					   sb.append(" +");
				}
			    String tmp =sb + "+" + M + " "+ ("theta"+ i +  badId4DisTheta) + " >= b" + i + " +" + empsilon + ";\n";
			    inequalityNo++;
			    out.write(tmp);
			   		    
			}
			String tmp = "";
			for(int  i=1; i<=total; i++)
			{
				   tmp += ("theta"+ i + badId4DisTheta) + " +" ;	
				   
			}
			 int extra =tmp.lastIndexOf("+");
			 tmp = tmp.substring(0, extra) + " <= " + total + "-1" + ";";
			 inequalityNo++;
			 out.write(tmp+"\n");
			 

				 
				 tmp = "bin ";
				for(int  i=1; i<=total; i++)
				{
					   tmp += ("theta"+ i + badId4DisTheta) + ", ";				   
				}
				
				 extra =tmp.lastIndexOf(",");
				 tmp = tmp.substring(0, extra) + ";";
				 cons4bad.add(tmp);
			 
			return cons4bad;
		
		
	}
	 
//	 private static ArrayList<String> genConstraints4Bad(int total, String bad, int badId4DisTheta) {
//			
//         
//			ArrayList<String> cons4bad = new ArrayList<String>();
//			String[] tokens = bad.split(" ");
//			
//			StringBuilder sb = new StringBuilder();
//			for(int  i=1; i<=total; i++)
//			{
//				for(int j=1; j<=tokens.length;j++)
//				{
//					String tokenj = tokens[j-1];
//					sb.append(tokenj + " " + "x"+i+""+j + " +");
//					varStrings.add("x" + i + "" + j);
//				}
//			    int extra =sb.lastIndexOf("+");
//			    String tmp = sb.substring(0, extra) + "+" + M + " "+ ("y"+ i + "badNo" + badId4DisTheta) + " >= b" + i + " +" + empsilon + ";";
//			    sb.delete(0, sb.length());
//			    cons4bad.add(tmp);
//			    
//			}
//			String tmp = "";
//			for(int  i=1; i<=total; i++)
//			{
//				   tmp += ("y"+ i+ "badNo" + badId4DisTheta) + " +" ;	
//				   varStrings.add("y"+ i+ "badNo" + badId4DisTheta);
//			}
//			 int extra =tmp.lastIndexOf("+");
//			 tmp = tmp.substring(0, extra) + " <= " + total + "-1" + ";";
//			 cons4bad.add(tmp);
//			 
////				String tmp2 = "";
////				for(int  i=1; i<=total; i++)
////				{
////					   tmp2 += ("y"+ i+ "badNo" + badId4DisTheta) + " +" ;				   
////				}
////				 int extra2 =tmp2.lastIndexOf("+");
////				 tmp2 = tmp2.substring(0, extra2) + " >= " + empsilon+ ";";
////				 cons4bad.add(tmp2);
//				 // no need, just bound the M is enough
//				 
//				 tmp = "bin ";
//				for(int  i=1; i<=total; i++)
//				{
//					   tmp += ("y"+ i+ "badNo" + badId4DisTheta) + ", ";				   
//				}
//				
//				 extra =tmp.lastIndexOf(",");
//				 tmp = tmp.substring(0, extra) + ";";
//				 cons4bad.add(tmp);
//			 
//			return cons4bad;
//		
//		
//	}


}

