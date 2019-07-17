package application;

public class PreflopRangeManager
{
	PreflopCombo[] p1PreflopCombos;
	PreflopCombo[] p2PreflopCombos;
	
	PreflopRangeManager(PreflopCombo[] p1PreflopCombos, PreflopCombo[] p2PreflopCombos, int[] initialBoard)
	{
		this.p1PreflopCombos = p1PreflopCombos;
		this.p2PreflopCombos = p2PreflopCombos;
		
		setRelativeProbabilities(initialBoard);
	}
	
	public int getNumHands(int playerId)
	{
		if (playerId == 1)
			return p1PreflopCombos.length;
		return p2PreflopCombos.length;
	}
	
	public PreflopCombo[] getPreflopCombos(int playerId)
	{
		if (playerId == 1)
			return p1PreflopCombos;
		return p2PreflopCombos;
	}
	
	public float[] getInitialReachProbs(int player, int[] board)
	{
		PreflopCombo[] preflopCombos = getPreflopCombos(player);
		
		float[] reachProbs = new float[preflopCombos.length];
		
		for (int i = 0; i < preflopCombos.length; i++)
		{
			if (CardUtility.overlap(preflopCombos[i], board))
				reachProbs[i] = 0;
			else
				reachProbs[i] = preflopCombos[i].probability;
		}
		
		return reachProbs;
	}
	
	public void setRelativeProbabilities(int[] initialBoard)
	{
		for (int p = 1; p <= 2; p++)
		{
			PreflopCombo[] heroPreflopCombos;
			PreflopCombo[] villainPreflopCombos;
			
			if (p == 1)
			{
				heroPreflopCombos = p1PreflopCombos;
				villainPreflopCombos = p2PreflopCombos;
			}
			else
			{
				heroPreflopCombos = p2PreflopCombos;
				villainPreflopCombos = p1PreflopCombos;
			}
			
			float relativeSum = 0;
				
			for (int heroHand = 0; heroHand < heroPreflopCombos.length; heroHand++)
			{
				PreflopCombo heroCombo = heroPreflopCombos[heroHand];
				
				if (CardUtility.overlap(heroCombo, initialBoard))
				{
					heroCombo.relativeProbability = 0;
					continue;
				}
					
				float villainSum = 0;

				for (int villainHand = 0; villainHand < villainPreflopCombos.length; villainHand++)
				{
					if (CardUtility.overlap(villainPreflopCombos[villainHand], initialBoard))
						continue;
					if (CardUtility.overlap(heroCombo, villainPreflopCombos[villainHand]))
						continue;
					
					villainSum += villainPreflopCombos[villainHand].probability;
				}

				heroCombo.relativeProbability = villainSum * heroCombo.probability;
				relativeSum += heroCombo.relativeProbability;
			}

			for (int heroHand = 0; heroHand < heroPreflopCombos.length; heroHand++)
				heroPreflopCombos[heroHand].relativeProbability /= relativeSum;
		}
	}
}
