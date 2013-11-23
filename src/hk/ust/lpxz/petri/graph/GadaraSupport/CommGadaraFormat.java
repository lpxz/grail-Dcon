//package hk.ust.lpxz.petri.graph.GadaraSupport;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Properties;
//import java.util.Set;
//
//import properties.DconPropertyManager;
//
//import edu.hkust.clap.lpxz.context.MethodItsCallSiteLineTuple;
//
//import hk.ust.lpxz.IO.Reader;
//import hk.ust.lpxz.IO.Writer;
//import hk.ust.lpxz.petri.graph.YoungPetri;
//import hk.ust.lpxz.petri.graph.YoungPetriLowLevelBuilder;
//import hk.ust.lpxz.petri.graph.YoungPetriMethod;
//import hk.ust.lpxz.petri.graph.YoungPetriMethodManager;
//import hk.ust.lpxz.petri.graph.GadaraSupport.parser.PNparser;
//import hk.ust.lpxz.petri.graph.criticalsection.PlaceCS;
//import hk.ust.lpxz.petri.graph.criticalsection.PlaceCSManager;
//import hk.ust.lpxz.petri.graph.criticalsection.PlaceCSModeller;
//import hk.ust.lpxz.petri.graph.criticalsection.ResourcePlaceManager;
//import hk.ust.lpxz.petri.graph.violation.Violation;
//import hk.ust.lpxz.petri.graph.violation.ViolationManager;
//import hk.ust.lpxz.petri.graph.violation.ViolationModeller;
//import hk.ust.lpxz.petri.unit.Arc;
//import hk.ust.lpxz.petri.unit.ArcCall;
//import hk.ust.lpxz.petri.unit.ArcReturn;
//import hk.ust.lpxz.petri.unit.ArcFromResource;
//import hk.ust.lpxz.petri.unit.ArcLocal;
//import hk.ust.lpxz.petri.unit.ArcToResource;
//import hk.ust.lpxz.petri.unit.ArcFromController;
//import hk.ust.lpxz.petri.unit.ArcToController;
//import hk.ust.lpxz.petri.unit.Place;
//import hk.ust.lpxz.petri.unit.PlaceCommonLocal;
//import hk.ust.lpxz.petri.unit.PlaceMethodEntry;
//import hk.ust.lpxz.petri.unit.PlaceMethodExit;
//import hk.ust.lpxz.petri.unit.PlaceResource;
//import hk.ust.lpxz.petri.unit.Transition;
//
//
//
//public class CommGadaraFormat {
//
//	// two things to do: gadara input, dcon output place name """""
//	public static void toGadara(YoungPetri g , String filename )
//	{
//
//		String togadara = toGadara(g);
//		Writer.write2File(togadara, filename);		
//		String stmtId_msigStmtLine_info = num2String(misgstmtLine2ID);
//		Writer.write2File(stmtId_msigStmtLine_info, filename + DconPropertyManager.gadarafile_statements_postfix);
//		
//		String msigId_msig_info = num2String(misg2ID);
//		Writer.write2File(msigId_msig_info, filename + DconPropertyManager.gadarafile_signatures_postfix);			
//	//	public static String gadarafile_statements_postfix = ".statements";
////		public static String gadarafile_signatures_postfix = ".methodsignatures";
//	}
//
//	
//	private static String num2String(HashMap<String, Integer> misg2id2) {
//		StringBuffer sb = new StringBuffer();
//		Iterator<String> it= misg2id2.keySet().iterator();
//		while(it.hasNext())
//		{
//			String key = it.next();
//			Integer value = misg2id2.get(key);
//			sb.append(value.intValue() + "=" + key +"\n");			
//		}
//		return sb.toString();
//	}
//
//
//	static boolean nodesProcessed = false;
//	
//	public static String toGadara(YoungPetri g)
//	{
//		if(!g.tokenAdded_matureNow)
//		{
//			throw new RuntimeException("it is not a good time to dump the gadara, initialize it first");
//		}
//		StringBuffer sb = new StringBuffer();
//         
//		
//		
//	    for (Object v : g.coreG.vertexSet()) {
//	    	nodesProcessed = true;
//        	
//        	if(v instanceof Place)// maybe transition
//        	{
//        		Place tmp = (Place)v;
//        		String placeinfo = placeinfo(tmp);
//        		sb.append(placeinfo + "\n");
//        		   	
//        	}
//        	else if(v instanceof Transition){
//        		Transition tmp = (Transition)v;
//        		String transinfo = transinfo(tmp);
//        		sb.append(transinfo + "\n");
//        				
//			}
//        }
//	    
//	    
//
//        for (Object e : g.coreG.edgeSet()) {
//        	Arc arc = (Arc)e;
//        	String arcinfo = arcinfo(arc);
//        	sb.append(arcinfo + "\n");
//        	}     
//        
//        // make sure the places are already outputed
//        if(!nodesProcessed) {
//        	throw new RuntimeException("I require the order: nodes-> compund construct. It is used as a rule in loading");
//        }
//        Set<YoungPetriMethod> pms =YoungPetriMethodManager.getAllPetriMethods();
//        for(YoungPetriMethod pm:pms)
//        {
//        	
//        	String pminfo = petrimethodinfo(pm);
//        	sb.append(pminfo + "\n");
//        }
//        
//    
//        Set<Violation> vios = ViolationManager.getAllViolations();
//        for(Violation vio: vios)
//        {
//        	String vioinfo = vioinfo(vio);
//        	sb.append(vioinfo + "\n");
//        }
//        
//        Set<PlaceCS> placeCSs =PlaceCSManager.getAllPlaceCSs();
//        for(PlaceCS placeCS: placeCSs)
//        {
//        	String csinfo = csinfo(placeCS);
//        	sb.append(csinfo + "\n");
//        }
//        
//        for (Object e : g.coreG.edgeSet()) {
//        	Arc arc = (Arc)e;
//        	Object src = arc.getSource();
//        	Object target = arc.getTarget();
//        	String sname =CommNamer.getNameInGenerating(src);
//        	String tname =CommNamer.getNameInGenerating(target);
//        	String aname =CommNamer.getNameInGenerating(arc);
//        	sb.append(sname +"->"+ aname + "->" + tname+";" + "\n");
//        	}    
//        
//        return sb.toString();		
//	}
//	
//	public static HashMap<String, Integer> misgstmtLine2ID = new HashMap<String, Integer>();
//	public static int stmtID =0;
//	public static int getStmtID(String msigstmtline)
//	{
//		Integer value =misgstmtLine2ID.get(msigstmtline);
//		if(value ==null)
//		{
//			misgstmtLine2ID.put(msigstmtline,stmtID );
//			stmtID ++;			
//		}
//		value =misgstmtLine2ID.get(msigstmtline);
//		return value.intValue();
//	}
//	
//	public static HashMap<String, Integer> misg2ID = new HashMap<String, Integer>();
//	public static int msigID =0;
//	public static int getMsigID(String msigstmtline)
//	{
//		Integer value =misg2ID.get(msigstmtline);
//		if(value ==null)
//		{
//			misg2ID.put(msigstmtline,msigID );
//			msigID ++;			
//		}
//		value =misg2ID.get(msigstmtline);
//		return value.intValue();
//	}
//	
//	
//	// place's id starts with p.
//	private static String placeinfo(Place tmp) {
//		// Type pid {stmt = id, token =, pmid= } // pmid =-1 if no pm is found
//		String type = tmp.getClass().getName();
//		String shortType = getShortType(type);
//		
//		
//		String pname = CommNamer.getNameInGenerating(tmp);
//		String modeledStmtID = getModeledStmt(tmp);
//		int token = tmp.getGadaratokens();
//		
//		
//		YoungPetriMethod enclose =tmp.getEnclosingM();
//		String pmname ="";
//		if(enclose ==null )//lock
//		{
//			pmname = "";
//		}else {
//			pmname = CommNamer.getNameInGenerating(enclose);
//		}
//		 
//		int stmtId = getStmtID(modeledStmtID);// dump when terminates!!
//		return shortType + " " + pname + " {" + CommNamer.STMT+ CommNamer.equalsTo +stmtId  + 
//		", " + CommNamer.TOKEN +  CommNamer.equalsTo+ token  +
//		", " +  CommNamer.ENCLOSINGM +CommNamer.equalsTo + pmname +  "};";
//	}
//	
//	private static String  transinfo(Transition tmp)
//	{
//		// Type tid {controllable=, observable=}
//		String type = tmp.getClass().getName();
//		String shortType = getShortType(type);
//		
//		
//		String tname = CommNamer.getNameInGenerating(tmp);
//		
//		return shortType + " " + tname+ " {" + CommNamer.CONTROLLABLE+ CommNamer.equalsTo + tmp.isGadara_controllable() + 
//		", " + CommNamer.OBSERVABLE+ CommNamer.equalsTo 
//		+ tmp.isGadara_observable() + "};";
//		
//		
//	}
//	
//	public static String arcinfo(Arc tmp)
//	{  // Arc aid {weight}
//		//String type = CommNamer.arctype; //you can not take care of the type easily, it is not simple, [invoke->trnastion]  ->methodentry.
//		
//		String type = tmp.getClass().getName();
//		String shortType = getShortType(type);
//		String aname = CommNamer.getNameInGenerating(tmp);
//		return shortType + " " + aname+ " {" + CommNamer.WEIGHT+ CommNamer.equalsTo + tmp.getWeight()+ "};";
//		
//	}
//	
//	public static String petrimethodinfo(YoungPetriMethod tmp)
//	{ // PetriMetho pmid {methodsig=,  entry=,  exit=}  // nodes={}, can be calculated in a reversed way from the nodes, 
//		// ctxt=,  can be calculated too based on the graph traversal.
//		String type =CommNamer.pmtype; 
//		String pmname = CommNamer.getNameInGenerating(tmp);
//		
//		String msig = tmp.getMsig();
//		
//		Place entry =tmp.getEntry();
//		Place exit = tmp.getExit();
//		String entryname = CommNamer.getNameInGenerating(entry);
//		String exitname = CommNamer.getNameInGenerating(exit);
//		int  msigId = getMsigID(msig);// dump when terminates!!
//		return type + " " +pmname + " {"+ CommNamer.METHODSIG +CommNamer.equalsTo + msigId + 
//		", "+ CommNamer.ENTRY  +CommNamer.equalsTo +entryname  + 
//		", "+ CommNamer.EXIT + CommNamer.equalsTo +exitname + "};";
//		
//		
//	}
//	
//	public static String vioinfo(Violation tmp)
//	{ // Violation vid {pnode= cnode= rnode=}
//		String type = CommNamer.viotype;
//		String vioname = CommNamer.getNameInGenerating(tmp);
//		
//		Place pnode =tmp.getPplace();
//		Place cnode =tmp.getcPlace();
//		Place rnode =tmp.getrPlace();
//		
//		String pnodename = CommNamer.getNameInGenerating(pnode);
//		String cnodename = CommNamer.getNameInGenerating(cnode);
//		String rnodename = CommNamer.getNameInGenerating(rnode);
//		
//	
//		
//		return type + " " + vioname + " {"+ CommNamer.PPLACE+CommNamer.equalsTo + pnodename +"" +
//				", "+ CommNamer.CPLACE + CommNamer.equalsTo  +cnodename + 
//				", "+   CommNamer.RPLACE +CommNamer.equalsTo +rnodename + "};";
//	}
//
//	
//	public  static String csinfo(PlaceCS tmp)
//	{ // PetriCS csid {resource=, nodes={}} // nodes can be computed from the nodes. Petrim= is not stored, because the nodes may not be in the same method
//		String type = CommNamer.pcstype;;
//		String csname = CommNamer.getNameInGenerating(tmp);
//		
//		Set<Place> places = new  HashSet<Place>();
//		tmp.includedPlaces(places);
//		
//		Set<Place> ctredPlaces =tmp.getCtredPlaces();
//		if(ctredPlaces.size()!=1){
//			throw  new RuntimeException(" only one ctred place !");
//		}
//		Place ctredPlace  =(Place) ctredPlaces.toArray()[0];
//		String ctredname =CommNamer.getNameInGenerating(ctredPlace);
//		
//		String observedString = "";
//		for(Place p: tmp.getObsedPlaces())
//		{
//			observedString+=CommNamer.getNameInGenerating(p) +",";
//		}
//		if(!observedString.isEmpty())
//		{
//			observedString =observedString.substring(0, observedString.length()-1);
//		}
//		observedString = "{" + observedString + "}";
//		
//		
//		
//		
//		String bigString = "";
//		for(Place p: places)
//		{
//			bigString+=CommNamer.getNameInGenerating(p) +",";
//		}
//		if(!bigString.isEmpty())
//		{
//			bigString =bigString.substring(0, bigString.length()-1);
//		}
//		bigString = "{" + bigString + "}";
//		
//		Place resP = tmp.getresPlace();
//		String resourcename =CommNamer.getNameInGenerating(resP);
//		
//		return type + " " + csname+ " {" + CommNamer.RESOURCENAME+CommNamer.equalsTo+ resourcename+", " 
//		+ CommNamer.CTREDPLACE + CommNamer.equalsTo + ctredname + ", " + CommNamer.OBSEDPLACES +  CommNamer.equalsTo + observedString + ", "
//		+CommNamer.PLACES+CommNamer.equalsTo + bigString+ "};"; 		
//	}
//
//	
//	
//	
//
//
//	
//
//
//	
//	private static String getModeledStmt(Place tmp) {
//		if(tmp instanceof PlaceCommonLocal)
//		{
//			PlaceCommonLocal pl = (PlaceCommonLocal)tmp;
//			return tmp.getEnclosingM().getMsig() +":"+ pl.getJimpleStmt().toString() + ":" + pl.getJimpleStmtLine();
//			
//		}
//		else {
//			return "";
//		}
//		
//	}
//
//	private static String getShortType(String type) {
//		int lastDot = type.lastIndexOf('.');
//		if(lastDot ==-1) return  type;
//		else {
//			return type.substring(lastDot+1);
//		}
//		
//		
//	
//	}
//
//
//
//
////====================================================================
//
//
//
//
//
//
//	static HashMap<String, Object> name2instance = new HashMap<String, Object>();
//	
//	// help checking the toGadara's branch handling capability!, may modify the gadara file to addd the branch
//	public static YoungPetri loadGadara(String filename )
//	{
//		try
//        {      
//			PNparser parser = new PNparser(new java.io.FileInputStream(filename));
//            parser.parse();
//            // how to load??
//             
//        }
//        catch(Exception e)
//        {
//                e.printStackTrace();
//        }
//		return null;
//		
//	}
//	static Properties stmtProps =null;
//	static Properties sigProps=null;
//	
//	public static void seeVarDec(String vtype , String vname , HashMap attriTable)
//	{
//		if(YoungPetri._cGraph==null)
//			YoungPetri._cGraph = new YoungPetri();		
//		YoungPetri youngPetri  =YoungPetri._cGraph;
//		
//		
////		System.out.println("===========");
////		System.out.println(vtype);
////		System.out.println(vname);
////		Iterator it =attriTable.keySet().iterator();
////		while (it.hasNext()) {
////			Object attr = (Object) it.next();
////			System.out.println(attr);
////			System.out.println(attriTable.get(attr));			
////		}
//		if(vtype.equals(CommNamer.PLACEENTRY))
//		{  // store and get style change???? see close into it.
//			String pmname= (String)attriTable.get(CommNamer.ENCLOSINGM);
//			YoungPetriMethod pm =YoungPetriMethodManager.getPMInLoading(pmname);
//			if(pm==null)
//			{
//				pm = new YoungPetriMethod();
//				YoungPetriMethodManager.registerInLoading(pmname, pm);
//				CommNamer.registerNameInLoading(pm, pmname);
//			}
//			   // set enclosingPM in this method.
//			PlaceMethodEntry entry =YoungPetriLowLevelBuilder.createAndAddEntryPlace(pm, null, youngPetri, null);
//		 	CommNamer.registerNameInLoading(entry, vname);
//			String tokens= (String)attriTable.get(CommNamer.TOKEN);
//			entry.setGadaratokens(Integer.parseInt(tokens));
//			// no need to set the stmt field, not used
//		}
//		else if(vtype.equals(CommNamer.PLACEEXIT))
//		{  // store and get style change???? see close into it.
//			String pmname= (String)attriTable.get(CommNamer.ENCLOSINGM);
//			YoungPetriMethod pm =YoungPetriMethodManager.getPMInLoading(pmname);
//			if(pm==null)
//			{
//				pm = new YoungPetriMethod();
//				YoungPetriMethodManager.registerInLoading(pmname, pm);
//				CommNamer.registerNameInLoading(pm, pmname);
//			}
//			 // set enclosingPM in this method.
//			PlaceMethodExit exit =YoungPetriLowLevelBuilder.createAndAddExitPlace(pm, null, youngPetri, null);		   
//			CommNamer.registerNameInLoading(exit, vname);
//			String tokens= (String)attriTable.get(CommNamer.TOKEN);
//			exit.setGadaratokens(Integer.parseInt(tokens));
//			// no need to set the stmt field, not used
//		}
//		
//		else if(vtype.equals(CommNamer.PLACECOMMONLOCAL))
//		{  // store and get style change???? see close into it.
//			String pmname= (String)attriTable.get(CommNamer.ENCLOSINGM);
//			YoungPetriMethod pm =YoungPetriMethodManager.getPMInLoading(pmname);
//			if(pm==null)
//			{
//				pm = new YoungPetriMethod();
//				YoungPetriMethodManager.registerInLoading(pmname, pm);
//				CommNamer.registerNameInLoading(pm, pmname);
//			}
//			   // set enclosingPM in this method.
//			PlaceCommonLocal pcl =YoungPetriLowLevelBuilder.createAndAddStmt(pm, null, youngPetri, null);		 
//			CommNamer.registerNameInLoading(pcl, vname);
//			String tokens= (String)attriTable.get(CommNamer.TOKEN);
//			pcl.setGadaratokens(Integer.parseInt(tokens));
//			String  msigStmt = (String)attriTable.get(CommNamer.STMT);
//			// load from property file!
//			if(stmtProps ==null)
//			{
//				stmtProps = DconPropertyManager.loadProperties(DconPropertyManager.gadaraFileName+ DconPropertyManager.gadarafile_statements_postfix);
//				
//			}
//			
//			pcl.setMsigStmtLineInLoading(stmtProps.getProperty(msigStmt));
//					
//		}
//		else if(vtype.equals(CommNamer.PLACERESOURCE))
//		{  // store and get style change???? see close into it.
//
//			PlaceResource pr = new PlaceResource();			// no need for tt and arrayEle	 
//			CommNamer.registerNameInLoading(pr, vname);
//			String tokens= (String)attriTable.get(CommNamer.TOKEN);
//			pr.setGadaratokens(Integer.parseInt(tokens));
//			ResourcePlaceManager.registerAllPRsInLoading(pr);
//			// no need for setting stmt
//		}
//		else if(vtype.equals(CommNamer.TRANSITION))
//		{  // store and get style change???? see close into it.
//
//			Transition newone = new Transition();
//			CommNamer.registerNameInLoading(newone, vname);
//			String controllable = (String)attriTable.get(CommNamer.CONTROLLABLE);
//			newone.setGadara_controllable(controllable.equals("true"));
//			String observable = (String)attriTable.get(CommNamer.OBSERVABLE);
//			newone.setGadara_controllable(observable.equals("true"));
//		
//		}
//		
//		else if(vtype.equals(CommNamer.ARCLOCAL))
//		{  // store and get style change???? see close into it.
//			String weight = (String)attriTable.get(CommNamer.WEIGHT);			
//			ArcLocal arclocal = new ArcLocal();
//			CommNamer.registerNameInLoading(arclocal, vname);
//			arclocal.setWeight(Integer.parseInt(weight));
//		}
//		else if(vtype.equals(CommNamer.ARCCALL))
//		{  // store and get style change???? see close into it.
//			String weight = (String)attriTable.get(CommNamer.WEIGHT);
//			
//			ArcCall arccall = new ArcCall();
//			CommNamer.registerNameInLoading(arccall, vname);
//			arccall.setWeight(Integer.parseInt(weight));
//		}
//		else if(vtype.equals(CommNamer.ARCRETURN))
//		{  // store and get style change???? see close into it.
//			String weight = (String)attriTable.get(CommNamer.WEIGHT);
//			
//			ArcReturn arcreturn = new ArcReturn();
//			CommNamer.registerNameInLoading(arcreturn, vname);
//			arcreturn.setWeight(Integer.parseInt(weight));
//		}
//		else if(vtype.equals(CommNamer.ARCFromResource))
//		{  // store and get style change???? see close into it.
//			String weight = (String)attriTable.get(CommNamer.WEIGHT);
//			
//			ArcFromResource arcfromRes = new ArcFromResource();
//			CommNamer.registerNameInLoading(arcfromRes, vname);
//			arcfromRes.setWeight(Integer.parseInt(weight));
//		}
//		else if(vtype.equals(CommNamer.ARCToResource))
//		{  // store and get style change???? see close into it.
//			String weight = (String)attriTable.get(CommNamer.WEIGHT);
//			
//			ArcToResource arcToRes = new ArcToResource();
//			CommNamer.registerNameInLoading(arcToRes, vname);
//			arcToRes.setWeight(Integer.parseInt(weight));
//		}
//		else if(vtype.equals(CommNamer.ARCFromController))
//		{  // store and get style change???? see close into it.
//			String weight = (String)attriTable.get(CommNamer.WEIGHT);
//			
//			ArcFromController arcFromCon = new ArcFromController();
//			CommNamer.registerNameInLoading(arcFromCon, vname);
//			arcFromCon.setWeight(Integer.parseInt(weight));
//		}
//		else if(vtype.equals(CommNamer.ARCToController))
//		{  // store and get style change???? see close into it.
//			String weight = (String)attriTable.get(CommNamer.WEIGHT);
//			
//			ArcToController arcToCon = new ArcToController();
//			CommNamer.registerNameInLoading(arcToCon, vname);
//			arcToCon.setWeight(Integer.parseInt(weight));
//		}
//		
//		else if(vtype.equals(CommNamer.pmtype))
//		{  // store and get style change???? see close into it.
//			
//			YoungPetriMethod pm =YoungPetriMethodManager.getPMInLoading(vname);
//			if(pm==null)
//			{
//				throw new RuntimeException("already created by one of its containing places");
//			}
//			// load from property file!
//
//			String msig = (String)attriTable.get(CommNamer.METHODSIG);
//			if(sigProps ==null)
//			{
//				sigProps = DconPropertyManager.loadProperties(DconPropertyManager.gadaraFileName+ DconPropertyManager.gadarafile_signatures_postfix);
//				
//			}
//			pm.setMsigInLoading(sigProps.getProperty(msig));
//			String entry = (String)attriTable.get(CommNamer.ENTRY);
//			String exit = (String)attriTable.get(CommNamer.EXIT);
//			PlaceMethodEntry placemethodentry =(PlaceMethodEntry) CommNamer.getObjectInLoading(entry);
//			PlaceMethodExit placemethodexit =(PlaceMethodExit) CommNamer.getObjectInLoading(exit);
//			if(placemethodentry==null || placemethodexit==null) throw new RuntimeException("already loaded, can not be null");
//			pm.setEntry(placemethodentry);
//			pm.setExit(placemethodexit);			
//		}
//		else if(vtype.equals(CommNamer.viotype))
//		{  // store and get style change???? see close into it.
//			String pstring = (String)attriTable.get(CommNamer.PPLACE);
//			String cstring = (String)attriTable.get(CommNamer.CPLACE);
//			String rstring = (String)attriTable.get(CommNamer.RPLACE);
//			PlaceCommonLocal pplace  =(PlaceCommonLocal) CommNamer.getObjectInLoading(pstring);
//			PlaceCommonLocal cplace  =(PlaceCommonLocal) CommNamer.getObjectInLoading(cstring);
//			PlaceCommonLocal rplace  =(PlaceCommonLocal) CommNamer.getObjectInLoading(rstring);
//			// name!!!
//			Violation vio =ViolationModeller.modelViolation(pplace, pplace.getEnclosingM(), cplace,cplace.getEnclosingM(), rplace,rplace.getEnclosingM());		
//			CommNamer.registerNameInLoading(vio, vname);
//			// need to register to Violaation manmager!!
//		}
//		else if(vtype.equals(CommNamer.pcstype))
//		{  // store and get style change???? see close into it.
//			String pr = (String)attriTable.get(CommNamer.RESOURCENAME);
//			String ctredPlaceStr = (String)attriTable.get(CommNamer.CTREDPLACE);
//			Set obsedPlacesSet = (Set)attriTable.get(CommNamer.OBSEDPLACES);
//			Set includedPlacesSet = (Set)attriTable.get(CommNamer.PLACES);
//			
//			PlaceCS placeCS = new PlaceCS();
//			CommNamer.registerNameInLoading(placeCS, vname);
//			
//			PlaceResource resourcePlace =(PlaceResource) CommNamer.getObjectInLoading(pr);
//			placeCS.setresPlace(resourcePlace);
//			
//			PlaceCommonLocal ctredPlace =(PlaceCommonLocal) CommNamer.getObjectInLoading(ctredPlaceStr);
//			placeCS.ctredPlaces.add(ctredPlace);
//			
//			
//			for(Object obs:obsedPlacesSet)
//			{
//				PlaceCommonLocal obsedPlace =(PlaceCommonLocal) CommNamer.getObjectInLoading((String)obs);
//				placeCS.obsedPlaces.add(obsedPlace);
//			}
//			
//		
//			for(Object includeplace:includedPlacesSet)
//			{
//				PlaceCommonLocal includedP =(PlaceCommonLocal) CommNamer.getObjectInLoading((String)includeplace);
//				placeCS.includedInLoading.add(includedP);
//			}
//			
//			
//			PlaceCSManager.registerPCS4CS( placeCS);	
//			 // need to register to Violaation manmager!!
//		}
//	}
//	
//	
//	public static String[] items(String includedPlaces) // {x, y, z}
//	{
//		
//		int left =includedPlaces.indexOf('{');
//		int right = includedPlaces.indexOf('}');
//		String inter = includedPlaces.substring(left+1, right);
//		String[] items =inter.split(",");
//		for(String item : items)
//		{
//			item.trim();
//		}
//		return items;
//		
//	}
//	
//	
//	public static void seelinkDec(String node1 , String node2 , String node3)
//	{
////		System.out.println("===================");
////		System.out.println(node1 + " -> " + node2 + " -> " +node3);
//		Object  n1 = CommNamer.getObjectInLoading(node1);
//		Object  n2 = CommNamer.getObjectInLoading(node2);
//		Object  n3 = CommNamer.getObjectInLoading(node3);
//		YoungPetri csGraph = YoungPetri.getYoungPetri();
//		if(!csGraph.coreG.containsVertex(n1))
//		{
//			csGraph.coreG.addVertex(n1);
//		}
//		if(!csGraph.coreG.containsVertex(n3))
//		{
//			csGraph.coreG.addVertex(n3);
//		}
//	    csGraph.coreG.addEdge(n1, n3, (Arc) n2);
//		
//	}
//	
//	
//
////	private static void createArc(YoungPetri graph, String line) {
////		//arc:        A      from_node  to_node weight 
////		 String tt[] = line.split("\\s{1,}"); 
////		 String fromString = tt[1];
////		 String toString = tt[2];
////		 String weiString = tt[3];
////		 
////		 Object from = name2instance.get(fromString);
////		 Object to = name2instance.get(toString);
////		 
////		 if(from instanceof PlaceResource)
////		 {
////			 graph.coreG.addEdge_edgetype_lpxz(from, to, ArcFromResource.class);
////		 }
////		 else if (to instanceof PlaceResource) {
////			 graph.coreG.addEdge_edgetype_lpxz(from, to, ArcToResource.class);
////		 }
////		 else {
////			 graph.coreG.addEdge_edgetype_lpxz(from, to, ArcLocal.class);
////		}		 
////		 
////		// return graph.coreG.getEdge(from, to) ; 
////	}
////
////	private static Transition createTransition(YoungPetri graph, String line) {
////		//	trans:      T      transition_name  controllable?  observable?
////		 String tt[] = line.split("\\s{1,}"); 
////		 String name = tt[1];
////		 String contString = tt[2];
////		 String obsString = tt[3];
////		 boolean controllable = contString.equals("true");
////		 boolean observable = obsString.equals("false");
////		 
////		 Transition t = new Transition();
////		 t.setPetriName(name);
////		 t.intialize(controllable, observable);
////		 
////		 return t ;
////		 
////		 
////	}
////
////	// 1 lose the context information, method info, jstmt, of course, the methodEntry, methodExit places
////	// 2 for the lockPlace, we lose the lockobject and array attribute,
////	// 3 we lose the arcCall and arcReturn edges. 
////	// i.e., we lose almost all the program information
////	
////
////	private static Place createPlace(YoungPetri graph, String line) {
////		//place:      P      place_name  type  init_num_tokens
////	    String tt[] = line.split("\\s{1,}"); 
////		String name = tt[1];
////		String type = tt[2];
////		String numberOftokens = tt[3];
////		int tokeNum = Integer.parseInt(numberOftokens);
////		
////		Place toret  = null;
////		if(type.equals(GadaraNotation.LockPlaceType))
////		{
////			toret  = new PlaceResource();
////		}
////		else {
////			toret = new PlaceCommonLocal();
////		}
////
//////		toret.setCtxts(null);
//////		toret.setMsig(null);
//////		toret.setJimpleStmt(null);	
////		toret.setPetriName(name);
////		toret.intialize(type, tokeNum);
////		
////		return toret;
////	}
//
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
////           String string  = "aa    aa b";
////       	String tt[] = string.split("\\s{1,}"); 
////       	for(String str:tt)
////       	{
////       		System.out.println(str);
////       	}
//		
//		stmtProps = DconPropertyManager.loadProperties(DconPropertyManager.gadaraFileName+ DconPropertyManager.gadarafile_statements_postfix);
//		System.out.print(stmtProps.getProperty("2"));
//		
////		String gadaraFile = "/home/lpxz/eclipse/workspace/Dcon/output/commPNOfopenjms";
////        
////		loadGadara(gadaraFile);
//		
//	}
//
//}
