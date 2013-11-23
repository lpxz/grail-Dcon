package hk.ust.lpxz.fixing;

import hk.ust.lpxz.petri.graph.ICFGPetriBuilder;
import hk.ust.lpxz.petri.graph.Petri;

import java.util.ArrayList;
import java.util.List;

import edu.hkust.clap.organize.CSMethod;
import edu.hkust.clap.organize.CSMethodPair;
import edu.hkust.clap.organize.SaveLoad;



public class FilterOut {

	public static int[] manualBadIndex = null;
	public static int[] manualGoodIndex = null;//{};// from 0..
	
	
	public static  List nonNullList = new ArrayList();
	

	
	public static List filterOutIndices(List listtmp) {
		if(manualGoodIndex!=null )
		{
			for(int i=0; i<listtmp.size(); i++)
			{
				if(!containedIn(i, manualGoodIndex))
				{
					listtmp.set(i, null);
				}
			}
		}
		
		
		if(manualBadIndex!=null ){
		for(int tmp : manualBadIndex)
		{
			listtmp.set(tmp, null);
		}}
		
		nonNullList.clear();
		for(Object o : listtmp)
		{
			if(o!=null)
			{
				nonNullList.add(o);
			}
		}
		
		
		listtmp.clear();
		for(Object o : nonNullList)
		{
			if(o!=null)
			{
				listtmp.add(o);
			}
		}
		
		return listtmp;
		
		
		
		
	}
	


	public static void setManualBadIndex(int[] manualBadIndex) {
		FilterOut.manualBadIndex = manualBadIndex;
	}


	public static void setManualGoodIndex(int[] manualGoodIndex) {
		FilterOut.manualGoodIndex = manualGoodIndex;
	}

	public static void setManualGoodIndex(String leftrange, String rightrange) {
	  // assume at most 1000 violations
		int left = Integer.parseInt(leftrange);
		int right = Integer.parseInt(rightrange);
		List<Integer> goodList = new  ArrayList<Integer>();
		for(int  i = left; i< right; i++)
		{
			goodList.add(i);
		}
		
		int[] goods = new  int[goodList.size()];
		for(int i=0; i< goods.length; i++)
		{
			goods[i] = goodList.get(i).intValue();
		}
		
		FilterOut.manualGoodIndex = goods;
		
	}
	
	//lp: it is possible that we cannot map some of the buggy pAnc cAnc rAnc to the Petri net.
	// we do not model the exception handling code in the Petri net, therefore, some of the statements which are in the exceptional handling
	// code cannot be modeled.
	public static List filterOutBadApples(List listtmp) {
		
	
		
		nonNullList.clear();
		for(Object o : listtmp)
		{
			if(o!=null)
			{
				nonNullList.add(o);
			}
		}
		//nonNullList = nonNullList.subList(12, 25);
		// auto-filter
	
		SootAgent4Fixing.retrieveAppClasses(nonNullList);		
		SootAgent4Fixing.sootLoadNecessary();		
		Petri csGraph = ICFGPetriBuilder.getYoungPetri_filterPhase(nonNullList);	
		
		// some entries are set as null.
		
		listtmp.clear();
		int i=0;
		for(Object o : nonNullList)
		{
			
			
			i++;
			if(o!=null)
			{
				listtmp.add(o);
				CSMethodPair  csmethodpair = (CSMethodPair)o;
				unsetSootConst4Serialization(csmethodpair);			// destroy the soot stuff, recoer the env					
			}
		}
		

		
		

		SootAgent4Fixing.sootDestroyNecessary();
		
		
		System.out.println("*****************************************");
		System.out.println("********after filtering, remain:"+ listtmp.size()+"********");
		System.out.println("*****************************************");
		
		return listtmp;
	}

	private static void unsetSootConst4Serialization(CSMethodPair csmethodpair) {
		CSMethod cm1 = csmethodpair.getO1();
		CSMethod cm2 = csmethodpair.getO2();
		
		cm1.setPunit(null);
		cm1.setCunit(null);
		cm1.setRunit(null);
		cm1.setXedges(null);
		cm1.setBb(null);
		cm1.setUg(null);
		
		cm2.setPunit(null);
		cm2.setCunit(null);
		cm2.setRunit(null);
		cm2.setXedges(null);
		cm2.setBb(null);
		cm2.setUg(null);
		
		
		
	}



	private static boolean containedIn(int i, int[] manualGoodIndex2) {
		for(int tmp : manualGoodIndex2)
		{
			if(tmp==i) return true;
		}
		return false;
	}

	
	public static void main(String[] args) {
		DconPropertyManager.initialize(args[0]);
		
		if(DconPropertyManager.applyFilteringBadApples)
		{
			Object object2 = SaveLoad.load(SaveLoad.default_MCPairList(DconPropertyManager.projectname));
			List listtmp = ((List) object2);// CSMethodPair List
			
			List list = FilterOut.filterOutBadApples(listtmp);				
			String directto = SaveLoad.default_MCPairList_afterFiltering(DconPropertyManager.projectname) ;
			SaveLoad.save(list, directto);
			
		}
		else
		{
			// do nothing here.
		}
		


	}





	

}
