//package hk.ust.lpxz.linearprogramming;
//
//
//import hk.ust.lpxz.IO.Reader;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.Writer;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.io.*;
//
//import net.sf.javailp.Constraint;
//import net.sf.javailp.Linear;
//import net.sf.javailp.OptType;
//import net.sf.javailp.Problem;
//import net.sf.javailp.Result;
//import net.sf.javailp.Solver;
//import net.sf.javailp.SolverFactory;
//import net.sf.javailp.SolverFactoryLpSolve;
//import lpsolve.*;
//
//public class SpyroJavailp {
//	public static double empsilon = 0.01;
//	public static int   M = 100;
//  public static void main(String[] args) {
//
//	  constraintSolve("/home/lpxz/eclipse/workspace/pecan/pecan-monitor/src/linearprogramming/markings");
////	  Linear linear = new Linear();
////	  linear.add(143, "x");
////	  linear.add(60, "y");
////
////	  problem.setObjective(linear, OptType.MAX);
//
////	  linear = new Linear();
////	  linear.add(120, "x");
////	  linear.add(210, "y");
////
////	  problem.add(linear, "<=", 15000);
////
////	  linear = new Linear();
////	  linear.add(110, "x");
////	  linear.add(30, "y");
////
////	  problem.add(linear, "<=", 4000);
////
////	  linear = new Linear();
////	  linear.add(1, "x");
////	  linear.add(1, "y");
////
////	  problem.add(linear, "<=", 75);
//
////	  problem.setVarType("x", Integer.class);
////	  problem.setVarType("y", Integer.class);
//
//
//	 // System.out.println(result.get("x11"));
//
//	  /**
//	  * Extend the problem with x <= 16 and solve it again
//	  */
////	  problem.setVarUpperBound("x", 16);
////
////	  solver = factory.get();
////	  result = solver.solve(problem);
////
////	  System.out.println(result);
//	  
////	 genConstraints("/home/lpxz/eclipse/workspace/pecan/pecan-monitor/src/linearprogramming/markings");
////	  
////	  solveConstraints();
//  }
//  
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
//			}
//		    int extra =sb.lastIndexOf("+");
//		    String tmp = sb.substring(0, extra) + " <= b" + i + ";";
//		    sb.delete(0, sb.length());
//		    cons4good.add(tmp);
//		}
//		 
//		return cons4good;
//	}
//
//	
//	 
//	public static void constraintSolve( String filename)
//	{
//		  SolverFactory factory = new SolverFactoryLpSolve(); // use lp_solve
//		  factory.setParameter(Solver.VERBOSE, 0);
//		 // factory.setParameter(Solver.TIMEOUT, 100); // set timeout to 100 seconds
//		  
//		  
//		  Problem problem = new Problem();
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
//				genConstraints4Good(problem,total, good);
//			}
//			// section 2: add bad, and also the theta equation and binary declaration
//			int badId4DisTheta =0;
//			for(String bad:bads)
//			{
//				badId4DisTheta++;
//				genConstraints4Bad(problem,total, bad, badId4DisTheta);
//			}
//			
//			// section3: add Aij range, i.e., 0<=xij<=z and z is binary
//			genConstraints4Indicator(problem,total, token_no);
//			System.out.println("==========constraints============");
//			for(Constraint cc :problem.getConstraints())
//			{
//				System.out.println(cc);
//			}
//			System.out.println("=================================");
//			
//			genGoal(problem,total);
//
//			  Solver solver = factory.get(); // you should use this solver only once for one problem
//			  Result result = solver.solve(problem);
//
//			 // System.out.println(result);
//			  formatResult(total, token_no,result);
//			  System.out.println("goal:" + result.getObjective());
//			
//		}catch (Exception e) {
//		 e.printStackTrace();
//		}
//	}
//	
//	private static ArrayList<String> formatResult(int total, int token_no, Result result) {
//		ArrayList<String> cons4good = new ArrayList<String>();	
//		
//		StringBuilder sb = new StringBuilder();
//		for(int  i=1; i<=total; i++)
//		{
//			for(int j=1; j<=token_no;j++)
//			{
//				String tokenj = "p"+j;
//				sb.append(result.get("x"+i+""+j) +"*" + tokenj+ " +");
//			}
//		    int extra =sb.lastIndexOf("+");
//		    String tmp = sb.substring(0, extra) + " <= " + result.get("b"+i) + ";";
//		    sb.delete(0, sb.length());
//		    System.out.println(tmp);
//		    cons4good.add(tmp);
//		}
//		 
//		return cons4good;
//	}
//
//
//	private static void genGoal(Problem problem, int total) {
//	
//		
//		 Linear linear = new Linear();
//		for(int  i=1; i<=total; i++)
//		{
//			linear.add(1, "z"+ i);
//			 		   
//		}
//		 problem.setObjective(linear, OptType.MIN);
//	}
//
//	private static void genConstraints4Indicator(
//			Problem problem, int total, int tokenNo) {
//		ArrayList<String> cons4Indi = new ArrayList<String>();
//		StringBuilder sb = new StringBuilder();
//		for(int  i=1; i<=total; i++)
//		{
//			
//			for(int j=1; j<=tokenNo;j++)
//			{
//				Linear linear = new Linear();
//				linear.add(1, "x"+i+""+j);
//				problem.add(linear, ">=", 0);
//				
//				Linear linear2 = new Linear();
//				linear2.add(1, "x"+i+""+j);
//				linear2.add(-1, "z" + i);
//				problem.add(linear2, "<=", 0);			
//			}
//
//		}
//		
//		Linear linear = new Linear();
//		for(int  i=1; i<=total; i++)
//		{
//			linear.add(1, "z"+ i);
//			 
//		}
//		problem.add(linear, ">=", empsilon);			
//
//		
//		
//			for(int  i=1; i<=total; i++)
//			{
//				problem.setVarType("z"+ i, Boolean.class);
//				 		   
//			}
//			
//
//	}
//
//	private static void genConstraints4Good(Problem problem, int total, String good) {
//		
//		String[] tokens = good.split(" ");
//		
//		StringBuilder sb = new StringBuilder();
//		for(int  i=1; i<=total; i++)
//		{
//			Linear linear = new Linear();
//             for(int j=1; j<=tokens.length;j++)
//			{
//				
//				String tokenj = tokens[j-1];
//				linear.add(Integer.parseInt(tokenj), "x"+i+""+j);
//				
//			}
//			linear.add(-1, "b" + i);
//			 problem.add(linear, "<=", 0);
//		   
//		}
//		
//		 
//		
//	}
//	
//
//	 private static void genConstraints4Bad(Problem problem, int total, String bad, int badId4DisTheta) {
//		
//            
//		//	ArrayList<String> cons4bad = new ArrayList<String>();
//			String[] tokens = bad.split(" ");
//			
//		//	StringBuilder sb = new StringBuilder();
//			for(int  i=1; i<=total; i++)
//			{
//				Linear linear = new Linear();
//				for(int j=1; j<=tokens.length;j++)
//				{
//					String tokenj = tokens[j-1];
//					linear.add(Integer.parseInt(tokenj), "x"+i+""+j);
//				}
//				linear.add(M, "y"+ i + "badNo" + badId4DisTheta);
//				linear.add(-1, "b" + i);
//				problem.add(linear, ">=", empsilon);
//				
//			   
//			}
//			Linear linear = new Linear();
//			for(int  i=1; i<=total; i++)
//			{
//				linear.add(1, "y"+ i+ "badNo" + badId4DisTheta);
//						   
//			}
//			problem.add(linear, "<=", total-1);
//			
////			Linear linear2 = new Linear();
////			for(int  i=1; i<=total; i++)
////			{
////				linear2.add(1, "y"+ i+ "badNo" + badId4DisTheta);
////						   
////			}
////			problem.add(linear2, ">=", empsilon);
//            // no need, just bounding M is enough
//
//			 
//			 
//			
//				for(int  i=1; i<=total; i++)
//				{
//					problem.setVarType("y"+ i+ "badNo" + badId4DisTheta, Boolean.class);
//								   
//				}
//				
//
//		
//		
//	}
//
//
//}

