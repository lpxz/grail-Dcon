package hk.ust.lpxz.IO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.thoughtworks.xstream.XStream;

import edu.hkust.clap.organize.SaveLoad;

public class Writer {

	public static  void write2File(String towrite, String file) {
		try{
			  // Create file 
			  FileWriter fstream = new FileWriter(file);
			  BufferedWriter out = new BufferedWriter(fstream);
			  out.write(towrite);
			  //Close the output stream
			  out.close();
			  }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
           
	}
	
	public static void save(Object toDump, String filename )
	{
		ObjectOutputStream out = null;
    	try
    	{	
			String path = filename;
			File f = new File(path);
			if(!f.exists()) {
				if(!f.getParentFile().exists())
			       f.getParentFile().mkdirs();
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f);
			out = new ObjectOutputStream(fos);
			System.err.println("write the object to :" + path);
			
			out.writeObject(toDump);
    	}
		catch(Exception e)
    	{
    		e.printStackTrace();
    	}finally
    	{
    		try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}
	


	
	public static void main(String[] args) {
		
	}

}
