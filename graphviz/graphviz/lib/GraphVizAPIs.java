package graphviz.lib;

import java.io.File;

public class GraphVizAPIs
{
	public static void main(String[] args)
	{
		GraphVizAPIs p = new GraphVizAPIs();
		p.renderFile("./sample/simple.dot");
	}

	public static  void renderFile(String inputfile) {
		String outfile = start_with_file(inputfile);
		ShowImage.showImageAPI(outfile);			
		// sleep and System.exit(1);
		
	}

	/**
	 * Construct a DOT graph in memory, convert it
	 * to image and store the image in the file system.
	 */
	private void start_from_scratch()
	{
		GraphViz gv = new GraphViz();
		gv.addln(gv.start_graph());
		gv.addln("A -> B;");
		gv.addln("A -> C;");
		gv.addln(gv.end_graph());
		System.out.println(gv.getDotSource());

		gv.increaseDpi();   // 106 dpi

		String type = "gif";
		//      String type = "dot";
		//      String type = "fig";    // open with xfig
		//      String type = "pdf";
		//      String type = "ps";
		//      String type = "svg";    // open with inkscape
		//      String type = "png";
		//      String type = "plain";
		File out = new File("./tmp/out"+gv.getImageDpi()+"."+ type);   // Linux
		//      File out = new File("c:/eclipse.ws/graphviz-java-api/out." + type);    // Windows
		gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
	}

	/**
	 * Read the DOT source from a file,
	 * convert to image and store the image in the file system.
	 */
	private static  String start_with_file(String input)
	{
		
		//String input = "./sample/simple.dot";
		//	   String input = "c:/eclipse.ws/graphviz-java-api/sample/simple.dot";    // Windows

		GraphViz gv = new GraphViz();
		gv.readSource(input);
		//System.out.println(gv.getDotSource());

		String type = "gif";
		//    String type = "dot";
		//    String type = "fig";    // open with xfig
		//    String type = "pdf";
		//    String type = "ps";
		//    String type = "svg";    // open with inkscape
		//    String type = "png";
		//      String type = "plain";
		String outfile = input + "." + type;
		File out = new File(input+ "."+ type);   // Linux
		//	   File out = new File("c:/eclipse.ws/graphviz-java-api/tmp/simple." + type);   // Windows
		gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
		return outfile;
	}
}
