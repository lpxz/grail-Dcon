package hk.ust.lpxz.IO;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



import Jama.Matrix;

public class Reader {

	public static Object load(String filename)
	{
		Object obj = null;
		ObjectInputStream in =null;
		try
    	{
			
			File file = new File(filename);
			in = new ObjectInputStream(
					new FileInputStream(file));
			obj = in.readObject();
			System.err.println("load object from " + filename);
			
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}finally
    	{
    		try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return obj;
    	}
		
	}
	
	public static void readLineByLine(File file, List<String> goods)
	throws IOException
	{

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		try {			

			String line;
//			StringBuffer buffer = new StringBuffer();
//			  buffer.delete(0, buffer.length());
			while (true) {
				line =nextNonCommentLine(reader);	
//				System.out.println(line);
				if(line == null) break;
				goods.add(line);

				
				
			}
			
		} finally {
			reader.close();
		}

		
	}
	
//    [echo] sleep:0
//    [java] 106.821175
//    [java] 141.66425
//    [java] 127.386215
//    [java] 108.703995
//    [java] 106.62601
//    [java] 92.89062
//    [java] 74.13767
//    [java] 91.178116
//    [java] 98.10141
//    [java] 119.70029
//    [java] 112.55434
//    [java] 64.744354
//    [echo] sleep:10
    
//	2600.0
//	86.62114749999999
//	88.95846075
//	51.905319
//
//	2800.0
//	81.03748000000002
//	69.34439325
//	98.04153375
	public static void readConsole(String file)
	throws IOException
	{

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		try {			

			String line;
//			StringBuffer buffer = new StringBuffer();
//			  buffer.delete(0, buffer.length());
			List<List<Double>> dataLines = new ArrayList<List<Double>>();
			while (true) {
				line =nextNonCommentLine(reader);	
				if(line == null) break;
				
				double sleeptime =getSleeptime(line);
				
//				System.out.println(line);
			
				double v11 = getDouble(nextNonCommentLine(reader));	
				double v12 = getDouble(nextNonCommentLine(reader));	
				double v13 = getDouble(nextNonCommentLine(reader));	
				double v14 = getDouble(nextNonCommentLine(reader));	
				double v1 = (v11+ v12+v13+v14)/4;
				//System.out.print(v1);
				
				double v21 = getDouble(nextNonCommentLine(reader));	
				double v22 = getDouble(nextNonCommentLine(reader));	
				double v23 = getDouble(nextNonCommentLine(reader));	
				double v24 = getDouble(nextNonCommentLine(reader));	
				double v2 = (v21+ v22+v23+v24)/4;
				//System.out.print(v2);
				
				double v31 = getDouble(nextNonCommentLine(reader));	
				double v32 = getDouble(nextNonCommentLine(reader));	
				double v33 = getDouble(nextNonCommentLine(reader));	
				double v34 = getDouble(nextNonCommentLine(reader));	
				double v3= (v31+ v32+v33+v34)/4;
				//System.out.print(v3);
				
				List<Double> dataLine = new ArrayList<Double>();
				if(v2>v3)
				{
					//System.out.println(sleeptime+" "+ v1 + " " + v3 + " " + v2);
					dataLine.add(sleeptime);
					dataLine.add(v1);
					dataLine.add(v3);
					dataLine.add(v2);
				}
				else {
					//System.out.println(sleeptime+" "+v1 + " " + v2 + " " + v3);
					dataLine.add(sleeptime);
					dataLine.add(v1);
					dataLine.add(v2);
					dataLine.add(v3);
				}
				
				dataLines.add(dataLine);
		
				
			}
			for(int i=0; i<7;i++)
			NiheAll(dataLines);
			

			

			
			for(int i=0; i<dataLines.size(); i++)
			{
				List dataLine = dataLines.get(i);
				Double dd1= (Double)dataLine.get(1);
				Double dd2= (Double)dataLine.get(2);
				Double dd3= (Double)dataLine.get(3);
				double base= dd1.doubleValue();
				
				System.out.println(dataLine.get(0)+" "+ 1 + " " + dd2/dd1 + 
						" " + dd3/dd1);
			}
			
		} finally {
			reader.close();
		}

		
	}
	
	private static void NiheAll(List<List<Double>> dataLines) {
		for(int i=1; i< dataLines.size()-1; i++)
		{
			List dataLinePrec = dataLines.get(i-1);				
			List dataLine = dataLines.get(i);
			List dataLineSucc = dataLines.get(i+1);
			
            Nihe(dataLinePrec, dataLine, dataLineSucc);
        
		}
		
	}

	private static void Nihe(List dataLinePrec, List dataLine, List dataLineSucc) {
		
		nihe_vi(dataLinePrec, dataLine, dataLineSucc, 1);
		nihe_vi(dataLinePrec, dataLine, dataLineSucc, 2);
		nihe_vi(dataLinePrec, dataLine, dataLineSucc, 3);
		
		
		
		
		
	}

	private static void nihe_vi(List dataLinePrec, List dataLine,
			List dataLineSucc, int i) {
		double v1Prec = ((Double)dataLinePrec.get(i)).doubleValue();
		double v1 = ((Double)dataLine.get(i)).doubleValue();
		double v1Succ = ((Double)dataLineSucc.get(i)).doubleValue();
		if((v1-v1Prec)*(v1-v1Succ)>0)
		{
			v1 = (v1Prec + v1Succ)/2;
			dataLine.set(i, v1);
		}
		
	}

	private static double getSleeptime(String line) {

	    int index =line.indexOf(':');
		String str =line.substring(index+1).trim();
	 
		return Double.parseDouble(str);
	
		
	}

	private static double getDouble(String nextNonCommentLine) {
		String[] strs = nextNonCommentLine.split(" ");
		for(int i=0; i<strs.length; i++)
		{
			if(strs[i].equals("[java]"))
			{
				return Double.parseDouble(strs[i+1]);
			}
		}
		
		throw new RuntimeException("do not come here");
	}

	public static boolean readMarkingFile(File file, List<String> goods, List<String> bads)
	throws IOException {
BufferedReader reader = new BufferedReader(new InputStreamReader(
		new FileInputStream(file)));
try {
	boolean goodflag = false;
	boolean badflag = false;
	StringBuilder ret = new StringBuilder();
	String line;
	while ((line = reader.readLine()) != null) {
		if(line.isEmpty())
			continue;
		if(line.contains("[good]"))
		{
			goodflag =true;
			badflag=false;
		}
		if(line.contains("[bad]"))
		{
			goodflag =false;
			badflag=true;
		}
		if(goodflag && !line.contains("[good]") && !line.contains("[bad]"))
		{
			goods.add(line);
		}
		else if (badflag && !line.contains("[good]") && !line.contains("[bad]")){
			bads.add(line);
		}else {
			ret.append(line);
		}
		
	}
	  return ret.toString().contains("hijacking")?true:false;
	  
} finally {
	reader.close();
}
}
	// 
	// retMap: [b] Integer
	//         [l] matrix
	//         similar to u0 and D
	public static void readSBPIFile(File file, HashMap<String, Object> retMap)
	throws IOException {
BufferedReader reader = new BufferedReader(new InputStreamReader(
		new FileInputStream(file)));
try {
	

	String line;
//	StringBuffer buffer = new StringBuffer();
//	  buffer.delete(0, buffer.length());
	while (true) {
		line =nextNonCommentLine(reader);	
//		System.out.println(line);
		if(line == null) break;
		if(line.contains("[b]"))
		{ 	
			line =nextNonCommentLine(reader);		
			int value =Integer.parseInt(line);
			retMap.put("[b]", value);
		}
		else if(line.contains("[l]"))
		{	
			Matrix sectionVal = lookahead(reader);	
			retMap.put("[l]", sectionVal);
			
		}
		else if(line.contains("[u0]"))
		{ 
			Matrix sectionVal = lookahead(reader);
			retMap.put("[u0]", sectionVal);
			
		}
		else if (line.contains("[D]")) {	
			Matrix sectionVal = lookahead(reader);
			retMap.put("[D]", sectionVal);
			
		}
		

		
		
	}
	
} finally {
	reader.close();
}
}
	
	

	private static String nextNonCommentLine(BufferedReader reader) {
		String line = "";
		try {
			line = reader.readLine();
			while(line !=null && (line.startsWith("#"))) // # is the commenting symbol
			{
				line = reader.readLine();
			}
			return line ;
		} catch (IOException e) {
			
			e.printStackTrace();
			return null;
		}	
		
		
	}
	private static Matrix lookahead(BufferedReader reader) {
		String line;	
		List<List> rows = new ArrayList<List>();
		while((line = nextNonCommentLine(reader)) !=null && !(line).isEmpty())
		{
			List aRow = new ArrayList();
			String[] elements =line.split(" ");
			for(String str : elements) // "1 0  0" is partitioned as 1, 0, "", 0
			{
				if(!str.equals(""))
					aRow.add(str);
				}			
			rows.add(aRow);
		}
		int columnsize = rows.get(0).size();
		Matrix mm = new Matrix(rows.size(), columnsize);
		for(int i = 0;i<=rows.size()-1; i++)
		{
			List rowI = rows.get(i);
			for(int j=0; j<= columnsize-1; j ++)
			{
				String tmpString = (String) rowI.get(j);
				
				mm.set(i, j, Integer.parseInt(tmpString));
			}
		}
		
	
		return mm;
		    
		
	}

	public static void main(String[] args) {
    try {
		readConsole("/home/lpxz/console");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}

}