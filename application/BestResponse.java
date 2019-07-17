package application;

import java.text.NumberFormat;

public class BestResponse
{
	PreflopRangeManager prm;
	RiverRangeManager rrm = RiverRangeManager.getInstance();
	
	PreflopCombo[] heroPreflopCombos;
	PreflopCombo[] villainPreflopCombos;
	int hero;
	int villain;
	int numHeroHands;
	int numVillainHands;
	
	public BestResponse(PreflopRangeManager prm)
	{
		this.prm = prm;
	}
	
	public float getBestReponseEv(Node node, int hero, int villain, PreflopCombo[] heroCombos, PreflopCombo[] villainCombos, int[] initialBoard)
	{
		this.hero = hero;
		this.villain = villain;
		heroPreflopCombos = heroCombos;
		villainPreflopCombos = villainCombos;
		numHeroHands = heroCombos.length;
		numVillainHands = villainCombos.length;
		
		float ev = 0;
		
		float[] preflopComboEvs = bestResponse(node, prm.getInitialReachProbs(villain, initialBoard), initialBoard);
		
		float[] unblockedComboCounts = getUnblockedComboCounts(heroCombos, villainCombos, initialBoard);
		
		for (int i = 0; i < numHeroHands; i++)
			if (!CardUtility.overlap(heroCombos[i], initialBoard))
			{
				ev += preflopComboEvs[i] / unblockedComboCounts[i] * heroCombos[i].relativeProbability;
				//System.out.println(heroCombos[i].toString() + ": " + (preflopComboEvs[i] / unblockedComboCounts[i] + 400) + " " + unblockedComboCounts[i] + " " + heroCombos[i].relativeProbability);
			}
				
		
		return ev;
	}
	
	public void printExploitability(Node root, int iterationCount, int[] initialBoard, int initialPot, int inPositionPlayer)
	{
		NumberFormat numberFormat = NumberFormat.getInstance();
		numberFormat.setMinimumFractionDigits(3);
		numberFormat.setMaximumFractionDigits(3);
		
		float oopEv;
		float ipEv;
		
		if (inPositionPlayer == 2)
		{
			oopEv = getBestReponseEv(root, 1, 2, prm.getPreflopCombos(1), prm.getPreflopCombos(2), initialBoard);
			ipEv = getBestReponseEv(root, 2, 1, prm.getPreflopCombos(2), prm.getPreflopCombos(1), initialBoard);
		}
		else
		{
			oopEv = getBestReponseEv(root, 2, 1, prm.getPreflopCombos(2), prm.getPreflopCombos(1), initialBoard);
			ipEv = getBestReponseEv(root, 1, 2, prm.getPreflopCombos(1), prm.getPreflopCombos(2), initialBoard);
		}
		
		float exploitability = (oopEv + ipEv) / 2 / initialPot * 100;
		
		System.out.println("OOP BR EV: " + numberFormat.format(oopEv));
		System.out.println("IP BR EV: " + numberFormat.format(ipEv));
		System.out.println("Exploitability after " + iterationCount + " iterations: " + numberFormat.format(exploitability) + "% of the pot per hand");
	}
	
	public float[] getUnblockedComboCounts(PreflopCombo[] heroCombos, PreflopCombo[] villainCombos, int[] initialBoard)
	{
		float[] comboCounts = new float[heroCombos.length];
		
		for (int heroHand = 0; heroHand < heroCombos.length; heroHand++)
		{
			PreflopCombo heroCombo = heroCombos[heroHand];
			
			if (CardUtility.overlap(heroCombo, initialBoard))
				continue;
			
			float sum = 0;
			
			for (PreflopCombo villainCombo : villainCombos)
				if (!CardUtility.overlap(heroCombo, villainCombo) && !CardUtility.overlap(villainCombo, initialBoard))
					sum += villainCombo.probability;
			
			comboCounts[heroHand] = sum;
		}
		return comboCounts;
	}
	
	public float[] bestResponse(Node node, float[] villainReachProbs, int[] board)
	{
		if (node instanceof ActionNode)
			return actionBestResponse((ActionNode) node, villainReachProbs, board);
		else if (node instanceof ChanceNode)
			return chanceBestReponse((ChanceNode) node, villainReachProbs, board);
		else // terminal node
			return terminalBestReponse((TerminalNode) node, villainReachProbs, board);
	}
	
	public float[] actionBestResponse(ActionNode node, float[] villainReachProbs, int[] board)
	{
		if (hero == node.player)
		{
			float[] maxActionEvs = new float[numHeroHands];
			
			for (int action = 0; action < node.getNumActions(); action++)
			{
				float[] actionEvs = bestResponse(node.getChild(action), villainReachProbs, board);
							
				for (int hand = 0; hand < numHeroHands; hand++)
				{
					if (action == 0 || actionEvs[hand] > maxActionEvs[hand])
						maxActionEvs[hand] = actionEvs[hand];
				}
			}

			return maxActionEvs;
		}
		else
		{
			float[] cumSubgameEvs = new float[numHeroHands];
					
			float[] avgStrategy = node.getAverageStrategy();
			
			for (int action = 0; action < node.getNumActions(); action++)
			{
				float[] newVillainReachProbs = new float[numVillainHands];
				
				for (int hand = 0; hand < numVillainHands; hand++)
					newVillainReachProbs[hand] = avgStrategy[hand + action*numVillainHands] * villainReachProbs[hand];

				float[] subgameEvs = bestResponse(node.getChild(action), newVillainReachProbs, board);
				
				for (int hand = 0; hand < numHeroHands; hand++)
					cumSubgameEvs[hand] += subgameEvs[hand];
			}

			return cumSubgameEvs;
		}
	}
	
	public float[] chanceBestReponse(ChanceNode node, float[] villainReachProbs, int[] board)
	{
		float[] preflopComboEvs = new float[numHeroHands];

		for (int card = 0; card < 52; card++)
		{
			Node child = node.getChild(card);
			
			if (child == null)
				continue;
			
			int newBoard[] = new int[board.length + 1];
			for (int j = 0; j < board.length; j++)
				newBoard[j] = board[j];
			
			if (node.type == ChanceNode.Type.DEAL_TURN)
				newBoard[3] = card;
			else if (node.type == ChanceNode.Type.DEAL_RIVER)
				newBoard[4] = card;
			
			float[] newVillainReachProbs = new float[numVillainHands];
			
			for (int hand = 0; hand < numVillainHands; hand++)
			{
				if (!CardUtility.overlap(villainPreflopCombos[hand], card))
					newVillainReachProbs[hand] = villainReachProbs[hand];
			}
			
			float subgameEvs[] = bestResponse(child, newVillainReachProbs, newBoard);
			
			for (int hand = 0; hand < numHeroHands; hand++)
				preflopComboEvs[hand] += subgameEvs[hand] / 44;
		}
		
		return preflopComboEvs;
	}
	
	public float[] terminalBestReponse(TerminalNode node, float[] villainReachProbs, int[] board)
	{
		if (node.type == TerminalNode.Type.ALLIN)
			return allinBestResponse(node, villainReachProbs, board);
		if (node.type == TerminalNode.Type.UNCONTESTED)
			return uncontestedBestResponse(node, villainReachProbs, board);
		else
			return showdownBestResponse(node, villainReachProbs, board);
	}
	
	//assumes that both players got allin on the turn
	float[] allinBestResponse(TerminalNode node, float[] villainReachProbs, int[] board)
	{
		float[] preflopComboEvs = new float[numHeroHands];

		for (int card = 0; card < 52; card++)
		{
			if (CardUtility.overlap(card, board))
				continue;
			
			int newBoard[] = new int[board.length + 1];
			for (int j = 0; j < board.length; j++)
				newBoard[j] = board[j];
			newBoard[4] = card;
			
			float[] newVillainReachProbs = new float[numVillainHands];
			
			for (int hand = 0; hand < numVillainHands; hand++)
			{
				if (!CardUtility.overlap(villainPreflopCombos[hand], card))
					newVillainReachProbs[hand] = villainReachProbs[hand];
			}
			
			float subgameEvs[] = showdownBestResponse(node, newVillainReachProbs, newBoard);
			
			for (int hand = 0; hand < numHeroHands; hand++)
				preflopComboEvs[hand] += subgameEvs[hand] / 44;
		}
		
		return preflopComboEvs;
	}
	
	float[] showdownBestResponse(TerminalNode node, float[] villainReachProbs, int[] board)
	{	
		RiverCombo[] heroRiverCombos = rrm.getRiverCombos(hero, heroPreflopCombos, board);
		RiverCombo[] villainRiverCombos = rrm.getRiverCombos(villain, villainPreflopCombos, board);

		float[] preflopComboEvs = new float[numHeroHands];
		
		float winSum = 0;
		float[] cardWinSum = new float[52];

		float value = node.pot / 2.0f;
		
		int j = 0;
		for (int i = 0; i < heroRiverCombos.length; i++)
		{
			RiverCombo heroCombo = heroRiverCombos[i];
			
			while (heroCombo.rank > villainRiverCombos[j].rank)
			{
				RiverCombo villainCombo = villainRiverCombos[j];
				
				winSum += villainReachProbs[villainCombo.reachProbsIndex];
				
				cardWinSum[villainCombo.card1] += villainReachProbs[villainCombo.reachProbsIndex];
				cardWinSum[villainCombo.card2] += villainReachProbs[villainCombo.reachProbsIndex];
				
				j++;
			}

			preflopComboEvs[heroCombo.reachProbsIndex] = (winSum
									- cardWinSum[heroCombo.card1]
									- cardWinSum[heroCombo.card2])
									* value;
		}
	
		float loseSum = 0;
		float[] cardLoseSum = new float[52];
		
		j = villainRiverCombos.length-1;
		for (int i = heroRiverCombos.length-1; i >= 0; i--)
		{
			RiverCombo heroCombo = heroRiverCombos[i];
			
			while (heroCombo.rank < villainRiverCombos[j].rank)
			{
				RiverCombo villainCombo = villainRiverCombos[j];
				
				loseSum += villainReachProbs[villainCombo.reachProbsIndex];
				
				cardLoseSum[villainCombo.card1] += villainReachProbs[villainCombo.reachProbsIndex];
				cardLoseSum[villainCombo.card2] += villainReachProbs[villainCombo.reachProbsIndex];
				
				j--;
			}

			preflopComboEvs[heroCombo.reachProbsIndex] -= (loseSum
									- cardLoseSum[heroCombo.card1]
									- cardLoseSum[heroCombo.card2])
									* value;
		}

		return preflopComboEvs;
	}
	
	float[] uncontestedBestResponse(TerminalNode node, float[] villainReachProbs, int[] board)
	{	
		float villainSum = 0;
		float[] villainCardSum = new float[52];
		
		for (int hand = 0; hand < numVillainHands; hand++)
		{
			if (CardUtility.overlap(villainPreflopCombos[hand], board))
				continue;
			
			int card1 = villainPreflopCombos[hand].card1;
			int card2 = villainPreflopCombos[hand].card2;
			
			villainSum += villainReachProbs[hand];
			
			villainCardSum[card1] += villainReachProbs[hand];
			villainCardSum[card2] += villainReachProbs[hand];
		}
		
		float value = (hero == node.lastToAct) ? (-node.pot / 2.0f) : (node.pot / 2.0f);
		
		float[] preflopComboEvs = new float[numHeroHands];
		
		for (int hand = 0; hand < numHeroHands; hand++)
		{
			if (CardUtility.overlap(heroPreflopCombos[hand], board))
				continue;
			
			int card1 = heroPreflopCombos[hand].card1;
			int card2 = heroPreflopCombos[hand].card2;
			
			preflopComboEvs[hand] = (villainSum
					- villainCardSum[card1]
					- villainCardSum[card2]
					+ villainReachProbs[hand])
					* value;
		}

		return preflopComboEvs;
	}
}
