package application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreflopRange
{
	private PreflopCombo[] preflopCombos;
	
	public PreflopRange(String rangeString) throws IOException
	{
		List<PreflopCombo> startingCombosList = new ArrayList<PreflopCombo>();
		
        List<String> holeCardComboStrings = Arrays.asList(rangeString.split(","));
        
		for (String holeCardComboString : holeCardComboStrings)
		{
			char firstCardRank = holeCardComboString.charAt(0);
    		char secondCardRank = holeCardComboString.charAt(1);
    		float weight = 1.0f;
    		
        	if (holeCardComboString.length() == 2) // unweighted. not specified if suited or offsuit.
        	{
    			for (int suit1 = 0; suit1 < 4; suit1++)
    			{
    				for (int suit2 = 0; suit2 < 4; suit2++)
        			{
    					if (firstCardRank == secondCardRank && suit1 >= suit2)
    						continue;
    					
    					int card1 = CardUtility.cardFromRankAndSuit(firstCardRank, suit1);
    					int card2 = CardUtility.cardFromRankAndSuit(secondCardRank, suit2);
    					
    					PreflopCombo preflopCombo = new PreflopCombo(card1, card2, weight);
    					startingCombosList.add(preflopCombo);
        			}
    			}
        	}
        	else if (holeCardComboString.charAt(2) == ':') // pair with weight
        	{
        		weight = Float.valueOf(holeCardComboString.substring(3, holeCardComboString.length()));
        		for (int suit1 = 0; suit1 < 4; suit1++)
    			{
    				for (int suit2 = suit1+1; suit2 < 4; suit2++)
        			{		
        				int card1 = CardUtility.cardFromRankAndSuit(firstCardRank, suit1);
    					int card2 = CardUtility.cardFromRankAndSuit(secondCardRank, suit2);
    					
    					PreflopCombo preflopCombo = new PreflopCombo(card1, card2, weight);
    					startingCombosList.add(preflopCombo);
        			}
    			}
        	}
        	else if (holeCardComboString.charAt(2) == 's') //suited hand
        	{
        		if (holeCardComboString.length() > 4) // weighted
        			weight = Float.valueOf(holeCardComboString.substring(4, holeCardComboString.length()));
        		
        		for (int suit = 0; suit < 4; suit++)
    			{
        			int card1 = CardUtility.cardFromRankAndSuit(firstCardRank, suit);
					int card2 = CardUtility.cardFromRankAndSuit(secondCardRank, suit);
					
					PreflopCombo preflopCombo = new PreflopCombo(card1, card2, weight);
					startingCombosList.add(preflopCombo);
    			}
        	}
        	else if (holeCardComboString.charAt(2) == 'o') // offsuit hand
        	{
    			if (holeCardComboString.length() > 4) // weighted
    				weight = Float.valueOf(holeCardComboString.substring(4, holeCardComboString.length()));
    			
    			for (int suit1 = 0; suit1 < 4; suit1++)
    			{
    				for (int suit2 = 0; suit2 < 4; suit2++)
        			{
        				if (suit1 == suit2)
        					continue;
        				
        				int card1 = CardUtility.cardFromRankAndSuit(firstCardRank, suit1);
    					int card2 = CardUtility.cardFromRankAndSuit(secondCardRank, suit2);

    					PreflopCombo preflopCombo = new PreflopCombo(card1, card2, weight);
    					startingCombosList.add(preflopCombo);
        			}
    			}
        	}
		}
		
		preflopCombos = new PreflopCombo[startingCombosList.size()];
		
		for (int i = 0; i < preflopCombos.length; i++)
			preflopCombos[i] = startingCombosList.get(i);
	}
	
	public PreflopCombo[] getPreflopCombos()
	{
		return preflopCombos;
	}
	
	public int getNumHands()
	{
		return preflopCombos.length;
	}
}
