package hk.ust.lpxz.fixing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;




public class DconPropertyManager {
	public static boolean nuwaStyle =false;
	public static boolean lockCallStyle = false;
	
	public static String propertyFile = "./properties";
	public static Properties props ;
	static{
		
		props = loadProperties(propertyFile);
	}
	
	
    public static Properties loadProperties(String filename)
    {
    	Properties properties = new Properties();
    	try {
    	    properties.load(new FileInputStream(filename));
    	} catch (IOException e) {
    	}
    	return properties;
    }	
    public static void initialize(String project)
    {
    	 projectname =project;
        
        
    	 origAnalyzedFolder = props.getProperty("originalAnalyzedFolder"); //"/home/lpxz/eclipse/workspace/app/bin";////"/home/lpxz/eclipse/workspace/openjms/bin";
    	mainClass= props.getProperty(projectname +"_trans_mainClass"); //"benchmarks.dstest.MTLinkedListInfiniteLoop";//System.getProperty("mainclass");//"driver.OpenJMSDriver";
         preTransArg = props.getProperty(projectname+"_pretrans_arg"); //"/h
     ClassPath = props.getProperty(projectname+"_trans_classpath"); //"/home/lpxz/eclipse/workspace/app/bin";////"/home/lpxz/eclipse/workspace/openjms/bin";
        excludeString = props.getProperty(projectname+"_trans_excludelist");
        includeString = props.getProperty(projectname+"_trans_includelist");
        outputform = props.getProperty(projectname+"_trans_outputform");
        
        String outputDir = System.getProperty("user.dir")+"/output/"+projectname;
        if(!new File(outputDir).exists())
        {
        	new  File(outputDir).mkdirs();
        }
        
        humanFile = System.getProperty("user.dir")+"/output/"+projectname+"/humanReadable" ;
    	 gadaraFileName = System.getProperty("user.dir") +"/output/"+projectname+"/commPN" ;
    	 stateGraphFile =System.getProperty("user.dir")+ "/output/"+projectname+"/stategraphDot";
    	 markingFile = System.getProperty("user.dir")+ "/output/"+projectname+"/markings";
    	 spyrosFile = System.getProperty("user.dir")+ "/output/"+projectname+"/spyrosTmpFile";;	
    	 PNdotFile =System.getProperty("user.dir")+ "/output/"+projectname+"/PetriNetDot" ;
    	constraint =System.getProperty("user.dir") +"/output/"+projectname+"/constraints";
    	patchedFile = System.getProperty("user.dir") +"/output/"+projectname+"/patchedDot";
    	 gadarafile_statements_postfix = ".statements";
    	 gadarafile_signatures_postfix = ".methodsignatures";
    	classOutputFolder = System.getProperty("user.dir")+ "/output/"+ projectname + "/outputClasses";
    	 observerClass = "hk.ust.lpxz.LockSynthesis.transformer.DconMonitor";
    	file_lockID2Tokens = System.getProperty("user.dir") +"/output/"+projectname+"/lockID2Tokens"  ; 
    	//private String observerClass = "AVfix.icse.fixing.lockwrapperStyle.lockMeta";;

    	
    }
	//must set the project name, always
	public static String projectname = null;
	public static String origAnalyzedFolder = null;// props.getProperty("originalAnalyzedFolder"); //"/home/lpxz/eclipse/workspace/app/bin";////"/home/lpxz/eclipse/workspace/openjms/bin";
	public static String mainClass=  null;// props.getProperty(projectname +"_trans_mainClass"); //"benchmarks.dstest.MTLinkedListInfiniteLoop";//System.getProperty("mainclass");//"driver.OpenJMSDriver";
    public static String preTransArg = null;//  props.getProperty(projectname+"_pretrans_arg"); //"/h
	public static String ClassPath = null;//  props.getProperty(projectname+"_trans_classpath"); //"/home/lpxz/eclipse/workspace/app/bin";////"/home/lpxz/eclipse/workspace/openjms/bin";
    public static String excludeString =  null;// props.getProperty(projectname+"_trans_excludelist");
    public static String includeString =  null;// props.getProperty(projectname+"_trans_includelist");
    public static String outputform =  null;// props.getProperty(projectname+"_trans_outputform");




    
	public static void main(String[] args) {	
		String userdir =System.getProperty("user.dir");
	    System.out.println(userdir);
	}
	
	//===============Dcon options:============================
	public static boolean applyFilteringBadApples= false;
	public static boolean findIntersectingVios_on_2Vars = false;
	 public static boolean rInPC_reduct_opt = false;// it is not correct, r in PC, you still need to protect pc,right?
	  public static boolean subsume_removal_opt = true;
	  public static boolean merge_of_jin_opt = true;

	  
	  public static boolean removeSynch = false;
	   public static boolean removeWait = false;
	   public static boolean report = true;;
	   
	   
	  public static String localizedPrefix = "localizedLock_";
	  public static String enforceClass  = "enforceRefreshClassLP";
		
		public static boolean singleLock = false;
	//	public static SootField globalLockObj =null;
		
	// we do not consider the AV lying in the exceptional block.
    // Therefore, we do not model the exceptional block.
	// But, exceptional block may be (completely) in the additive lock region. 
	// consider source transformation?  
	//public static boolean ignoreException = true; 

	
	public static boolean construct4unitgraph =true;// Petri net built for unitgraph or blockgraph?
	
	
	
	public static boolean visualizePic = true;
	public static boolean reportMetric = true;
	public static boolean simplify = true;
	
	
	
	
	
	
	
	
	
	
	public static boolean considerUnderBound =false;
	public static boolean twoPhaseEnumeration =true; // one phase: alias are modeled as the same place, second phase: modeled as different. Combination.
	public static int multipleWhat =100;
	
	public static boolean addController2Graph = true;
	public static long timeout= 10;//ms
	// exploring time:
	
	
	
	//====================state exploration:
//	public static boolean boundaryOpt = true; // hardcode as a mandatory option
	public static boolean minUnsafeMaxSafeOpt = true;// works if the space is linearly separatable.
	
	// unsafe reachable time:
	public static boolean onlyDA  =false;
	// the following field must be true. Otherwise, we can enumerate 200, 300, 400, infinite...
	public static final boolean race_to_be_fixed = true; // no matter whether race is bad, I have to fix it. No appraoches can fix violation while allowing the race.

	//=====================================
	
	//==============naming system==========
   public static boolean afterLoading= false;
   public static boolean useCommNamer = true;// typically, whne after laoding, we use CommNamer
	//=====================================
   
   //=================solver system=====
   public static boolean showMarking = false;// marking vectors are hardly readable.
   public static boolean showStates = false;
   public static boolean showConstraint = true;
   public static boolean debugEnumerateMarkings = false;
   public static boolean useYinSolver = false;// it does not work as well as expected.
	
    //===================files =========
	public static String humanFile = null;// System.getProperty("user.dir")+"/output/humanReadableOf" + projectname;
	public static String gadaraFileName = null;// System.getProperty("user.dir") +"/output/commPNOf" + projectname;
	public static String stateGraphFile =null;// System.getProperty("user.dir")+ "/output/stategraphDotOf" + projectname;
	public static String markingFile = null;// System.getProperty("user.dir")+ "/output/markingsOf"+ projectname;
	public static String spyrosFile = null;// System.getProperty("user.dir")+ "/output/spyrosTmpFileOf"+ projectname;;	
	public static String PNdotFile =null;// System.getProperty("user.dir")+ "/output/PetriNetDotOf" + projectname;
	public static String constraint =null;// System.getProperty("user.dir") +"/output/PetriNetDotOf" + projectname;
	public static String patchedFile =null;//  System.getProperty("user.dir") +"/output/patchedDotOf" + projectname;
	public static String gadarafile_statements_postfix = null;// ".statements";
	public static String gadarafile_signatures_postfix = null;// ".methodsignatures";
	public static String classOutputFolder = null;// System.getProperty("user.dir")+ "/output/"+ projectname + "";
	public static  String observerClass =null;//  "hk.ust.lpxz.LockSynthesis.transformer.DconMonitor";
	public static String file_lockID2Tokens = null;// System.getProperty("user.dir") +"/output/lockID2Tokens_" + projectname;; 
	//private String observerClass = "AVfix.icse.fixing.lockwrapperStyle.lockMeta";;

	public static boolean trialPatcher= false;
	public static boolean shrinkPetri =true;// shrinking may complicate the mapping from control logic to code. 	
	public static boolean showLockOperationTasks =false;
	public static String injectedmethodname_lockinitialize = "lockInitialize";// hardcoded
	public static String injectedmethodname_acquire="acquire";
	public static String injectedmethodname_release = "release";
	public static boolean showPatchedCode = false;
	public static String acquireSignature ="<hk.ust.lpxz.LockSynthesis.transformer.DconMonitor: void acquire(int,int)>";
	public static String releaseSignature= "<hk.ust.lpxz.LockSynthesis.transformer.DconMonitor: void release(int,int)>";

	
	
	

}
