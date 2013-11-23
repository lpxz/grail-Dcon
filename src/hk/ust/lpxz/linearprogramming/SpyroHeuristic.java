package hk.ust.lpxz.linearprogramming;

import hk.ust.lpxz.IO.Reader;
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

import org.antlr.grammar.v3.ANTLRParser.label_return;
import org.antlr.works.visualization.serializable.SSerializable;



import Jama.Matrix;

import soot.toolkits.scalar.Pair;

import lpsolve.*;

public class SpyroHeuristic {
	public static double empsilon = 0.1;
	public static int M = 100;

	public static void main(String[] args) {
//redirect output failed for native printing
		
//		PrintStream fileStream 	= null;
//		try {
//			fileStream = new PrintStream(new FileOutputStream("out.txt",true));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		PrintStream oldout = System.out;
//		PrintStream olderr = System.err;
//		System.setOut(fileStream);		
//		System.setErr(fileStream);

		
//		 constraintSolve("/home/lpxz/eclipse/workspace/Dcon/src/hk/ust/lpxz/linearprogramming/markings",
//		 new Pair<Matrix, Matrix>(null, null)
//		 );
		

	}

//	public static List<List> constraintSolve(String filename) {
//		List<List> toret = new ArrayList<List>();
//		// String testfile =
//		// "/home/lpxz/eclipse/workspace/pecan/pecan-monitor/src/linearprogramming/markings";
//		List<String> goods = new ArrayList<String>();
//		List<String> bads = new ArrayList<String>();
//		ArrayList<String> constraints = new ArrayList<String>();
//		try {
//			String source = Reader.readMarkingFile(new File(filename), goods,
//					bads);
//			
//			
//			
//			int total = goods.size() + bads.size();
//			int token_no = -1;
//			// note : I change Aij to xij, theta to y, z is z.
//			// section 1: add good
//
//			toret = iterativeFixing(goods, bads);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return toret;
//	}
	
	public static List<List> constraintSolve(Set<State> goodstates, Set<State> badstates) {
	List<List> toret = new ArrayList<List>();
	// String testfile =
	// "/home/lpxz/eclipse/workspace/pecan/pecan-monitor/src/linearprogramming/markings";
//	List<String> goods = new ArrayList<String>();
//	List<String> bads = new ArrayList<String>();
	ArrayList<String> constraints = new ArrayList<String>();
	try {
//		String source = Reader.readMarkingFile(new File(filename), goods,
//				bads);
		
		
		
		int total = goodstates.size() + badstates.size();
		int token_no = -1;
		// note : I change Aij to xij, theta to y, z is z.
		// section 1: add good

		toret = iterativeFixing(goodstates, badstates);
	} catch (Exception e) {
		e.printStackTrace();
	}
	return toret;
}

	static Set badPoints = new HashSet();
	static Set toremove = new HashSet();


//	private static List<List> iterativeFixing(List<String> goods,
//			List<String> bads) throws Exception {
//		badPoints.clear();
//		toremove.clear();
//		badPoints.addAll(bads);
//		List<List> resultantsList = new ArrayList<List>();
//		while (true) {
//			if (!badPoints.isEmpty()) {
//				toremove.clear();
//				fixBad_oneIter(goods, badPoints, toremove, resultantsList);
//				badPoints.removeAll(toremove);
//				// update the badPoints!
//			} else {
////				if(DconPropertyManager.showConstraint)
////				{
////					System.out.print("The constraints are :");
////					for (Object o : resultantsList) {
////						System.out.print(o + " ");
////					}
////				}
//				
//				break;
//			}
//		}
//
//		return resultantsList;
//
//	}
	
	
	private static List<List> iterativeFixing(	Set<State> goodstates, Set<State> badstates) throws Exception {
		badPoints.clear();
		toremove.clear();
		badPoints.addAll(badstates);
		List<List> resultantsList = new ArrayList<List>();
		int iteration=0;
		while (true) {
			if (!badPoints.isEmpty()) {
				toremove.clear();
				System.out.println("iteration: " + iteration);
				System.gc();
				fixBad_oneIter(goodstates, badPoints, toremove, resultantsList);
				badPoints.removeAll(toremove);
				// update the badPoints!
			} else {
//				if(DconPropertyManager.showConstraint)
//				{
//					System.out.print("The constraints are :");
//					for (Object o : resultantsList) {
//						System.out.print(o + " ");
//					}
//				}
				
				break;
			}
		}

		return resultantsList;

	}

	public static int getTokenNo(List<String> goods) {
		for (String good : goods) {
			int token_no = good.split(" ").length;
			return token_no;
		}
		return -1;
	}
	
	
//	private static void fixBad_oneIter(List<String> goods, List<String> bads,
//			Set<String> toremovepara, List<List> resultantsList)
//			throws Exception {
//		dumpSpyrosEquas(goods, bads);
//		List resultant = solveSpyrosEquas(goods.size() + bads.size(),
//				getTokenNo(goods));
//		resultantsList.add(resultant);
//		for (String bad : bads) {
//			if (canBeRemovedByConstraint(bad, resultant)) {
//				toremovepara.add(bad);
//			}
//		}
//
//	}

	private static void fixBad_oneIter(Set<State> goods, Set<State> bads,
			Set<State> toremovepara, List<List> resultantsList)
			throws Exception {
		System.out.println("before dumping");
		dumpSpyrosEquas(goods, bads);
		System.out.println("after dumping");
		List resultant = solveSpyrosEquas(goods.size() + bads.size(),
				token_no);
		System.out.println("after solving");
		resultantsList.add(resultant);
		for (State bad : bads) {
			if (canBeRemovedByConstraint(bad, resultant)) {
				toremovepara.add(bad);
			}
		}

	}
	
	
//	private static boolean canBeRemovedByConstraint(String bad, List resultant) {
//		List doubleList = toDouble(bad);
//		if (resultant.size() - doubleList.size() != 1) {
//			throw new RuntimeException();
//
//		}
//		double b = ((Double) resultant.get(resultant.size() - 1)).doubleValue();
//		double sum = 0;
//		for (int i = 0; i < doubleList.size(); i++) {
//			double token = ((Double) doubleList.get(i)).doubleValue();
//			double li = ((Double) resultant.get(i)).doubleValue();
//			sum += (token * li);
//		}
//
//		if (sum >= (b + empsilon))// already sepeareted
//		{
//			return true;
//		} else {
//			return false;
//		}
//
//	}
	
	
	private static boolean canBeRemovedByConstraint(State bad, List resultant) {
	//	List doubleList = toDouble(bad);
		tmpList.clear();
		StateVectorGenerator.getNumericRep(bad, tmpList);
		
		if (resultant.size() - tmpList.size() != 1) {
			throw new RuntimeException();

		}
		double b = ((Double) resultant.get(resultant.size() - 1)).doubleValue();
		double sum = 0;
		for (int i = 0; i < tmpList.size(); i++) {
			int token = ((Integer) tmpList.get(i)).intValue();
			double li = ((Double) resultant.get(i)).doubleValue();
			sum += (token * li);
		}

		if (sum >= (b + empsilon))// already sepeareted
		{
			return true;
		} else {
			return false;
		}

	}

	private static List toDouble(String bad) {
		List toret = new ArrayList();
		String[] tokens = bad.split(" ");
		for (String token : tokens) {
			toret.add((double) Integer.parseInt(token));
		}
		return toret;
	}

	private static List solveSpyrosEquas(int total, int token_no)
			throws LpSolveException {
		List toret = new ArrayList();
		// ============================================solve:
		LpSolve solver = LpSolve
				.readLp(DconPropertyManager.spyrosFile,
						0, null);// 0 stands for the level verbosity
		
	
		

		// solve the problem   
         ///************************
		solver.solve();
		///*************************

		
		// print solution

		solver.getPtrPrimalSolution();

		double[] argsss = new double[total];
		HashMap<String, Double> results = new HashMap<String, Double>();
		argsss = solver.getPtrVariables();
		for (int i = 1; i <= argsss.length; i++) {
			results.put(solver.getColName(i), argsss[i - 1]);
		}

		for (int j = 1; j <= token_no; j++) {
			toret.add(mult10Round(results.get("x" + "" + j))); // mult10Round
		}
		toret.add(mult10Round(results.get("b")));// mult10Round

		solver.deleteLp();

		return toret;

	}

//	public static Set<String> varStrings =new HashSet<String>();
//	public static Set<String> constraintStrings =new HashSet<String>();
//	private static void dumpSpyrosEquas(List<String> goods, List<String> bads)
//			throws IOException {
//		int token_no = -1;
//		int total = goods.size() + bads.size();
//		ArrayList<String> constraints = new ArrayList<String>();
//		
//		// section 1: add good
//		for (String good : goods) {
//			token_no = good.split(" ").length;
//			constraints.addAll(genConstraints4Good(total, good));
//		}
//		// section 2: add bad, and also the theta equation and binary
//		// declaration
//		int badId4DisTheta = 0;
//		for (String bad : bads) {
//			badId4DisTheta++;
//			constraints.addAll(genConstraints4Bad(total, bad, badId4DisTheta));
//		}
//
//		// section3: add Aij range, i.e., 0<=xij<=z and z is binary
//		constraints.addAll(genConstraints4Coefficient(total, token_no));
//		constraintStrings.addAll(constraints);
//        
//        
//		
//		String goal = genGoal(bads);
//
//		// get the goal function:
//		FileWriter fstream = new FileWriter(
//				DconPropertyManager.spyrosFile);
//		BufferedWriter out = new BufferedWriter(fstream);
//
//		out.write(goal + "\n");
//
//		List<String> bincache = new ArrayList<String>();
//		for (String tmp : constraints) {
//			// cache it, it should be placed at the bottom, so,,
//			if (tmp.startsWith("bin ")) {
//				bincache.add(tmp);
//			} else {
//				if(DconPropertyManager.showSpyrosEquation)
//				{
//					System.out.println(tmp);
//				}
//				
//				out.write(tmp + "\n");
//			}
//
//		}
//
//		for (String bin : bincache) {
//			if(DconPropertyManager.showSpyrosEquation)
//			{
//		    	System.out.println(bin);
//			}
//			out.write(bin + "\n");
//		}
//
//		out.flush();
//		out.close();
//
//	}

	public static int token_no = -1;
	public static List tmpList = new  LinkedList();
	private static void dumpSpyrosEquas(Set<State> goods, Set<State> bads)
	throws IOException {

	int total = goods.size() + bads.size();
	
	
	// section 1: add good
	


	
	
	
	String goal = genGoal(bads);
	
	// get the goal function:
	FileWriter fstream = new FileWriter(
			DconPropertyManager.spyrosFile);
	BufferedWriter out = new BufferedWriter(fstream);
	
	out.write(goal + "\n");
	
	
	for (State good : goods) {
		tmpList.clear();
		 StateVectorGenerator.getNumericRep(good, tmpList);
		token_no =tmpList.size();
		genConstraints4Good(total, tmpList,out);
	}
	// section 2: add bad, and also the theta equation and binary
	// declaration
	List<String> bincache = new ArrayList<String>();
	int badId4DisTheta = 0;
	for (State bad : bads) {
		badId4DisTheta++;
		tmpList.clear();
		StateVectorGenerator.getNumericRep(bad, tmpList);
		bincache.addAll(genConstraints4Bad(total, tmpList, badId4DisTheta,out));
	}
	
	// section3: add Aij range, i.e., 0<=xij<=z and z is binary
	genConstraints4Coefficient(total, token_no,out);
	

for (String bin : bincache) {

	out.write(bin + "\n");
}

out.flush();
out.close();

}
//	public static void constraintSolve(String filename,
//			Pair<Matrix, Matrix> pair) {
//		List<List> resultantList = constraintSolve(filename);
//
//		List<List> rawL = new ArrayList<List>();
//		List<List> rawB = new ArrayList<List>();
//		for (int i = 0; i < resultantList.size(); i++) {
//			List result = resultantList.get(i);
//			List lb = new ArrayList();
//
//			for (int j = 0; j < result.size() - 1; j++) {
//				lb.add(result.get(j));
//			}
//
//			List lb2 = new ArrayList();
//			lb2.add(result.get(result.size() - 1));// mult10Round
//			if (!allzero(lb)) // nothing meaningful
//			{
//				rawL.add(lb);
//				rawB.add(lb2);
//			}
//		}
//
//		Matrix L = ToMatrix.toMatrix(rawL);
//		Matrix B = ToMatrix.toMatrix(rawB);
//		if(DconPropertyManager.showConstraint)
//		{
//			System.out.println("L:");
//			L.print(2, 2);
//			System.out.println("");
//			System.out.println("B:");
//			B.print(2, 2);
//		}
//		
//		pair.setO1(L);
//		pair.setO2(B);
//
//	}
	
	
	
	
	public static void constraintSolve(Set<State> goodstates, Set<State> badstates,
			Pair<Matrix, Matrix> pair) {
		List<List> resultantList = constraintSolve( goodstates, badstates);

		List<List> rawL = new ArrayList<List>();
		List<List> rawB = new ArrayList<List>();
		for (int i = 0; i < resultantList.size(); i++) {
			List result = resultantList.get(i);
			List lb = new ArrayList();

			for (int j = 0; j < result.size() - 1; j++) {
				lb.add(result.get(j));
			}

			List lb2 = new ArrayList();
			lb2.add(result.get(result.size() - 1));// mult10Round
			if (!allzero(lb)) // nothing meaningful
			{
				rawL.add(lb);
				rawB.add(lb2);
			}
		}

		Matrix L = ToMatrix.toMatrix(rawL);
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

	}

	private static boolean allzero(List lb) {
		for (Object o : lb) {
			Double long1 = (Double) o;
			if (long1.doubleValue() != 0) {
				return false;
			}
		}

		return true;
	}

	private static ArrayList<String> formatResult(int total, int token_no,
			HashMap result) {
		ArrayList<String> cons4good = new ArrayList<String>();

		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= total; i++) {
			for (int j = 1; j <= token_no; j++) {
				String tokenj = "p" + j;
				sb.append(mult10Round(result.get("x" + i + "" + j)) + "*"
						+ tokenj + " +");
			}
			int extra = sb.lastIndexOf("+");
			String tmp = sb.substring(0, extra) + " <= "
					+ mult10Round(result.get("b" + i)) + ";";
			sb.delete(0, sb.length());
			System.out.println(tmp);
			cons4good.add(tmp);
		}

		return cons4good;
	}

	public static double mult10Round(Object o) {
		String str = o.toString();
		double dd = Double.parseDouble(str);
		return Math.round(dd * DconPropertyManager.multipleWhat);
	}

//	private static String genGoal(List<String> bads) {
//
//		String tmp = "min: ";
//		// for(int i=1; i<=total; i++)
//		int badId4DisTheta = 0;
//		for (String bad : bads) {
//			badId4DisTheta++;
//			tmp += ("ybadNo" + badId4DisTheta) + "+ ";
//		}
//
//		int extra = tmp.lastIndexOf("+");
//		tmp = tmp.substring(0, extra) + ";";
//		return tmp;
//	}
	
	private static String genGoal(Set<State> bads) {

		String tmp = "min: ";
		// for(int i=1; i<=total; i++)
		int badId4DisTheta = 0;
		for (State bad : bads) {
			badId4DisTheta++;
			tmp += ("ybadNo" + badId4DisTheta) + "+ ";
		}

		int extra = tmp.lastIndexOf("+");
		tmp = tmp.substring(0, extra) + ";";
		return tmp;
	}

	private static void genConstraints4Coefficient(int total,
			int tokenNo , BufferedWriter out) {
//		/ArrayList<String> cons4Indi = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		// for(int i=1; i<=total; i++)
		{
			for (int j = 1; j <= tokenNo; j++) {

				sb.append("0" + " <= " + "x" + "" + j + ";"); // ,+ zi // +
																// " <= " + "z"
																// + i +
				sb.append("x" + "" + j + " <= 1;");
			}			
			
			String tmp = sb.toString();
			sb.delete(0, sb.length());
			try {
				out.write(tmp+ "\n");
			} catch (IOException e) {
				// XXX Auto-generated catch block
				e.printStackTrace();
			}
		}

		// String tmp2 = "";
		// for(int i=1; i<=total; i++)
		// {
		// tmp2 += ("z"+ i) + " +";
		// }
		//
		// int extra2 =tmp2.lastIndexOf("+");
		// tmp2 = tmp2.substring(0, extra2) +" >="+empsilon+";";
		// cons4Indi.add(tmp2);

		// String tmp = "bin ";
		// for(int i=1; i<=total; i++)
		// {
		// tmp += ("z"+ i) + ", ";
		// }
		//
		// int extra =tmp.lastIndexOf(",");
		// tmp = tmp.substring(0, extra) + ";";// avoid the bug of lpsolver.
		// otherwise, it does not find a solution
		// cons4Indi.add(tmp);

		//return cons4Indi;
	}

	public static StringBuilder sb = new StringBuilder();
	private static void genConstraints4Good(int total, List good,BufferedWriter out) {
		//ArrayList<String> cons4good = new ArrayList<String>();
	//	String[] tokens = good.split(" ");

		
		sb.delete(0, sb.length());
		// for(int i=1; i<=total; i++)
		
			for (int j = 1; j <= good.size(); j++) {
				Integer tokenj = (Integer) good.get(j - 1);
				//sb.append(tokenj + " " + "x" + "" + j + " +");
				sb.append(tokenj);
				sb.append(" x");
				sb.append(j);
				if(j!=good.size())
				   sb.append(" +");
			
			}
			
		//	String tmp = sb + " <= b" + ";";
			sb.append(" <=b;");
			sb.append("\n");
			
			
			//cons4good.add(tmp);
		
		try {
			out.write(sb.toString());
		} catch (IOException e) {
			// XXX Auto-generated catch block
			e.printStackTrace();
		}

		//return cons4good;
	}

	private static ArrayList<String> genConstraints4Bad(int total, List bad,
			int badId4DisTheta, BufferedWriter out) {

		
		
		sb.delete(0, sb.length());
		// for(int i=1; i<=total; i++)
		
			for (int j = 1; j <= bad.size(); j++) {
				Integer tokenj = (Integer)bad.get(j - 1);
				sb.append(tokenj + " " + "x" + "" + j + " +");
			
			}
			int extra = sb.lastIndexOf("+");
			String tmp = sb.substring(0, extra) + "+"
					+ (M+" " + " ybadNo" + badId4DisTheta) + " >= b" + " +"
					+ empsilon + ";";
			
			sb.delete(0, sb.length());
			try {
				out.write(tmp+"\n");
			} catch (IOException e) {
				// XXX Auto-generated catch block
				e.printStackTrace();
			}



		 tmp = "bin ";
		// for(int i=1; i<=total; i++)
		{
			tmp += ("y" + "badNo" + badId4DisTheta) + "; ";
		}

		ArrayList<String> cons4bad = new ArrayList<String>();
		cons4bad.add(tmp);
		return cons4bad;

	}
	
	
	
//	private static ArrayList<String> genConstraints4Good(int total, String good) {
//		ArrayList<String> cons4good = new ArrayList<String>();
//		String[] tokens = good.split(" ");
//
//		StringBuilder sb = new StringBuilder();
//		// for(int i=1; i<=total; i++)
//		{
//			for (int j = 1; j <= tokens.length; j++) {
//				String tokenj = tokens[j - 1];
//				sb.append(tokenj + " " + "x" + "" + j + " +");
//				varStrings.add("x"+j);
//			}
//			int extra = sb.lastIndexOf("+");
//			String tmp = sb.substring(0, extra) + " <= b" + ";";
//			sb.delete(0, sb.length());
//			cons4good.add(tmp);
//		}
//
//		return cons4good;
//	}
//
//	private static ArrayList<String> genConstraints4Bad(int total, String bad,
//			int badId4DisTheta) {
//
//		ArrayList<String> cons4bad = new ArrayList<String>();
//		String[] tokens = bad.split(" ");
//
//		StringBuilder sb = new StringBuilder();
//		// for(int i=1; i<=total; i++)
//		{
//			for (int j = 1; j <= tokens.length; j++) {
//				String tokenj = tokens[j - 1];
//				sb.append(tokenj + " " + "x" + "" + j + " +");
//				varStrings.add("x"+j);
//			}
//			int extra = sb.lastIndexOf("+");
//			String tmp = sb.substring(0, extra) + "+"
//					+ (M+" " + " ybadNo" + badId4DisTheta) + " >= b" + " +"
//					+ empsilon + ";";
//			varStrings.add("ybadNo" + badId4DisTheta);
//			sb.delete(0, sb.length());
//			cons4bad.add(tmp);
//
//		}
//		// String tmp = "";
//		// // for(int i=1; i<=total; i++)
//		// {
//		// tmp += ("y"+ i+ "badNo" + badId4DisTheta) + " +" ;
//		// }
//		// int extra =tmp.lastIndexOf("+");
//		// tmp = tmp.substring(0, extra) + " <= " + total + "-1" + ";";
//		// cons4bad.add(tmp);
//
//		// String tmp2 = "";
//		// for(int i=1; i<=total; i++)
//		// {
//		// tmp2 += ("y"+ i+ "badNo" + badId4DisTheta) + " +" ;
//		// }
//		// int extra2 =tmp2.lastIndexOf("+");
//		// tmp2 = tmp2.substring(0, extra2) + " >= " + empsilon+ ";";
//		// cons4bad.add(tmp2);
//		// no need, just bound the M is enough
//
//		String tmp = "bin ";
//		// for(int i=1; i<=total; i++)
//		{
//			tmp += ("y" + "badNo" + badId4DisTheta) + "; ";
//		}
//
//		cons4bad.add(tmp);
//
//		return cons4bad;
//
//	}
	
	

}
