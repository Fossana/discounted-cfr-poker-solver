package application;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RiverRangeManager
{
	Map<Integer, RiverCombo[]> p1RiverRanges = new HashMap<Integer, RiverCombo[]>();
	Map<Integer, RiverCombo[]> p2RiverRanges = new HashMap<Integer, RiverCombo[]>();
	
	HandEvaluator handEvaluator = HandEvaluator.getInstance();
	
	private static RiverRangeManager instance = null; 
	 
    public static RiverRangeManager getInstance() 
    { 
        if (instance == null) 
        	instance = new RiverRangeManager(); 
  
        return instance; 
    } 
	
	public RiverCombo[] getRiverCombos(int player, PreflopCombo[] preflopCombos, int[] board)
	{
		Map<Integer, RiverCombo[]> riverRanges;
		
		if (player == 1)
			riverRanges = p1RiverRanges;
		else
			riverRanges = p2RiverRanges;
		
		int key = CardUtility.boardToKey(board);
		
		if (riverRanges.get(key) != null)
			return riverRanges.get(key);
		
		int count = 0;
		
		for (int hand = 0; hand < preflopCombos.length; hand++)
			if (!CardUtility.overlap(preflopCombos[hand], board))
				count++;
		
		int index = 0;
		RiverCombo[] riverCombos = new RiverCombo[count];
		
		for (int hand = 0; hand < preflopCombos.length; hand++)
		{
			PreflopCombo preflopCombo = preflopCombos[hand];
			
			if (CardUtility.overlap(preflopCombo, board))
				continue;
			
			RiverCombo riverCombo = new RiverCombo(preflopCombo.card1, preflopCombo.card2, preflopCombo.probability, hand);
			riverCombo.rank = handEvaluator.getHandValue(riverCombo.card1, riverCombo.card2, board);
			riverCombos[index++] = riverCombo;
		}
		
		Arrays.sort(riverCombos);
		
		riverRanges.put(key, riverCombos);
		
		return riverCombos;
	}
}
