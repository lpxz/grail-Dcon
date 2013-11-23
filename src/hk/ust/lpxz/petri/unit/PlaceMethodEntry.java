package hk.ust.lpxz.petri.unit;



public class PlaceMethodEntry extends Place{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDebugName() {
		
		return "entry of " + enclosingM.methodName;
	}

}
