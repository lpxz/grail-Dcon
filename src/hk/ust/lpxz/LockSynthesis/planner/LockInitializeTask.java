package hk.ust.lpxz.LockSynthesis.planner;

import java.util.HashMap;

public class LockInitializeTask {
	public static HashMap<Integer, Integer> lockID2Tokens = new HashMap<Integer, Integer>();
	public static void lockID2Tokens(int lockID, int tokens)
	{
		if(lockID2Tokens.containsKey(lockID))
		{
			Integer valueInteger  = lockID2Tokens.get(lockID);
		    if(valueInteger.intValue()!=tokens) throw new RuntimeException("recheck the number of tokens for the lock");
		}
		lockID2Tokens.put(lockID, tokens);
	}

}
