package trainer;

import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;

import action_node.trainer_module.DcfrModule;
import application.BestResponse;
import application.CardUtility;
import application.ChanceNode;
import application.ActionNode;
import application.Node;
import application.PreflopCombo;
import application.PreflopRangeManager;
import application.RiverCombo;
import application.RiverRangeManager;
import application.TerminalNode;

public class ParallelDcfr implements Trainer
{
	PreflopRangeManager prm;
	RiverRangeManager rrm = RiverRangeManager.getInstance();
	
	int[] initialBoard;
	int initialPot;
	int inPositionPlayer;
	
	ForkJoinPool forkJoinPool;
	BestResponse br;
	
	public ParallelDcfr(PreflopRangeManager prm, int[] initialBoard, int initialPot, int inPositionPlayer)
	{
		this.prm = prm;
		this.inPositionPlayer = inPositionPlayer;
		this.initialBoard = initialBoard;
		this.initialPot = initialPot;
	}
	
	void loadTrainerModules(Node root)
	{
		if (root instanceof TerminalNode)
		{
			return;
		}
		else if (root instanceof ChanceNode)
		{
			ChanceNode chanceNode = (ChanceNode) root;
			for (Node child: chanceNode.getChildren())
				if (child != null)
					loadTrainerModules(child);
		}
		else
		{
			ActionNode actionNode = (ActionNode) root;
			actionNode.setTrainerModule(new DcfrModule(actionNode));
			
			for (int action = 0; action < actionNode.getNumActions(); action++)
				loadTrainerModules(actionNode.getChild(action));
		}
	}
	
	@Override
	public void train(Node root, int numberOfIterations)
	{
		loadTrainerModules(root);
		
		br = new BestResponse(prm);
		
		br.printExploitability(root, 0, initialBoard, initialPot, inPositionPlayer);
		System.out.println("");

		int nThreads = Runtime.getRuntime().availableProcessors();
		forkJoinPool = new ForkJoinPool(nThreads);
		
		long start = System.currentTimeMillis();
		long timeElapsed = 0;
		
		for (int i = 1; i <= numberOfIterations; i++)
		{
			cfr(1, 2, root, i);
			cfr(2, 1, root, i);
			
			if (i % 100 == 0)
			{
				timeElapsed += System.currentTimeMillis() - start;

				System.out.println(i + " cfr iterations took: " + timeElapsed + "ms");
				br.printExploitability(root, i, initialBoard, initialPot, inPositionPlayer);
				System.out.println("");
				
				start = System.currentTimeMillis();
			}
		}
		
		long finish = System.currentTimeMillis();
		timeElapsed += finish - start;
		
		forkJoinPool.shutdown();
	}
	
	float[] cfr(int hero, int villain, Node root, int iterationCount)
	{
		PreflopCombo[] heroPreflopCombos = prm.getPreflopCombos(hero);
		PreflopCombo[] villainPreflopCombos = prm.getPreflopCombos(villain);
		
		float[] heroReachProbs;
		float[] villainReachProbs;
		
		if (hero == 1)
		{
			heroReachProbs = prm.getInitialReachProbs(1, initialBoard);
			villainReachProbs = prm.getInitialReachProbs(2, initialBoard);
		}
		else
		{
			heroReachProbs = prm.getInitialReachProbs(2, initialBoard);
			villainReachProbs = prm.getInitialReachProbs(1, initialBoard);
		}
		
		return forkJoinPool.invoke(new CfrHelper(null,
				root,
				hero,
				villain,
				heroPreflopCombos,
				villainPreflopCombos,
				heroReachProbs,
				villainReachProbs,
				initialBoard,
				iterationCount));
	}
	
	enum ResultType
	{
		CHANCE_NODE,
		ACTION_NODE
	}
	
	class CfrHelper extends CountedCompleter<float[]>
	{
		int hero;
		int villain;
		Node node;
		float[] heroReachProbs;
		float[] villainReachProbs;
		int[] board;
		PreflopCombo[] heroPreflopCombos;
		PreflopCombo[] villainPreflopCombos;
		int numHeroHands;
		int numVillainHands;
		int iterationCount;
		float[] result = null;
		CfrHelper[] cfrHelpers = null;
		DcfrModule trainerModule = null;
		ResultType resultType;
		
		CfrHelper(CountedCompleter<?> p, Node node,
				int heroId, int villainId,
				PreflopCombo[] heroPreflopCombos,
				PreflopCombo[] villainPreflopCombos,
				float[] heroReachPr,
				float[] villainReachPr,
				int[] board,
				int iterationCount)
		{
			super(p);
			this.hero = heroId;
			this.villain = villainId;
			this.node = node;
			this.heroReachProbs = heroReachPr;
			this.villainReachProbs = villainReachPr;
			this.heroPreflopCombos = heroPreflopCombos;
			this.villainPreflopCombos = villainPreflopCombos;
			this.board = board;
			this.iterationCount = iterationCount;
			numHeroHands = heroPreflopCombos.length;
			numVillainHands = villainPreflopCombos.length;
		}
		
		@Override
		public void compute()
		{
			if (node instanceof ChanceNode)
			{
				resultType = ResultType.CHANCE_NODE;
				chanceNodeUtility(this, (ChanceNode) node, heroReachProbs, villainReachProbs, board);
				tryComplete();
				return;
			}
			
			if (node instanceof TerminalNode)
			{
				result = terminalNodeUtility((TerminalNode) node, villainReachProbs, board);
				tryComplete();
				return;
			}
			
			resultType = ResultType.ACTION_NODE;
			
			ActionNode actionNode = (ActionNode) node;
			trainerModule = (DcfrModule) actionNode.getTrainerModule();
			
			int actionCount = actionNode.getNumActions();
			float[] strategy = actionNode.getCurrentStrategy();
			
			if (actionNode.player == hero)
			{
				cfrHelpers = new CfrHelper[actionCount];
				setPendingCount(actionCount-1);
				
				for (int action = 0; action < actionCount; action++)
				{
					float[] newHeroReachProbs = new float[numHeroHands];
					for (int hand = 0; hand < numHeroHands; hand++)
						newHeroReachProbs[hand] = strategy[hand + action*numHeroHands] * heroReachProbs[hand];
					
					CfrHelper cfrHelper = new CfrHelper(this, actionNode.getChild(action), hero, villain, heroPreflopCombos, villainPreflopCombos, newHeroReachProbs, villainReachProbs, board, iterationCount);
					cfrHelpers[action] = cfrHelper;
					if (action == actionCount - 1)
						cfrHelper.compute();
					else
						cfrHelper.fork();
				}
			}
			else
			{
				cfrHelpers = new CfrHelper[actionCount];
				setPendingCount(actionCount-1);
				
				for (int action = 0; action < actionCount; action++)
				{
					float[] newVillainReachProbs = new float[numVillainHands];
					for (int hand = 0; hand < numVillainHands; hand++)
						newVillainReachProbs[hand] = strategy[hand + action*numVillainHands] * villainReachProbs[hand];
					
					CfrHelper cfrHelper = new CfrHelper(this, actionNode.getChild(action), hero, villain, heroPreflopCombos, villainPreflopCombos, heroReachProbs, newVillainReachProbs, board, iterationCount);
					cfrHelpers[action] = cfrHelper;
					if (action == actionCount - 1)
						cfrHelper.compute();
					else
						cfrHelper.fork();
				}
			}
		}
		
		@Override
		public void onCompletion(CountedCompleter<?> caller)
		{
			if (caller != this)
			{
				if (resultType == ResultType.CHANCE_NODE)
				{
					result = new float[numHeroHands];
					float[] subgameUtilities;
					
					for (int action = 0; action < cfrHelpers.length; action++)
					{
						if (cfrHelpers[action] == null)
							continue;
						
						subgameUtilities = cfrHelpers[action].result;
						for (int hand = 0; hand < numHeroHands; hand++)
							result[hand] += subgameUtilities[hand] / 44;
					}
				}
				else if (resultType == ResultType.ACTION_NODE)
				{
					if (trainerModule.current == hero)
					{
						float[] currentStrategy = trainerModule.getCurrentStrategy();
						result = new float[numHeroHands];
						float[] actionUtilities;
						
						for (int action = 0; action < cfrHelpers.length; action++)
						{
							if (cfrHelpers[action] == null)
								continue;
							
							actionUtilities = cfrHelpers[action].result;
							trainerModule.updateCumulativeRegretPartOne(actionUtilities, action);
							for (int hand = 0; hand < numHeroHands; hand++)
								result[hand] += actionUtilities[hand] * currentStrategy[hand + action*numHeroHands];
						}
						
						trainerModule.updateCumulativeRegretPartTwo(result, iterationCount);
						trainerModule.updateCumulativeStrategy(currentStrategy, heroReachProbs, iterationCount);
					}
					else
					{
						result = new float[numHeroHands];
						float[] actionUtilities;
						
						for (int action = 0; action < cfrHelpers.length; action++)
						{
							if (cfrHelpers[action] == null)
								continue;
							
							actionUtilities = cfrHelpers[action].result;
							for (int hand = 0; hand < numHeroHands; hand++)
								result[hand] += actionUtilities[hand];
						}
					}
				}
			}
		}
		
		void chanceNodeUtility(CountedCompleter<?> p, ChanceNode node, float[] heroReachProbs, float[] villainReachProbs, int[] board)
		{
			cfrHelpers = new CfrHelper[node.getChildCount()];
			
			float[] cardWeights = getCardWeights(villainReachProbs, board);
			
			int count = 0;
			for (int card = 0; card < 52; card++)
			{
				Node child = node.getChild(card);
				if (child == null)
					continue;
				
				addToPendingCount(1);
				
				int[] newBoard = new int[board.length + 1];
				for (int i = 0; i < board.length; i++)
					newBoard[i] = board[i];
				newBoard[board.length] = card;
				
				float[] newHeroReachProbs = new float[numHeroHands];
				
				for (int hand = 0; hand < numHeroHands; hand++)
					if (!CardUtility.overlap(heroPreflopCombos[hand], card))
						newHeroReachProbs[hand] = heroReachProbs[hand] * cardWeights[hand + card*numHeroHands];

				float[] newVillainReachProbs = new float[numVillainHands];
				
				for (int hand = 0; hand < numVillainHands; hand++)
					if (!CardUtility.overlap(villainPreflopCombos[hand], card))
						newVillainReachProbs[hand] = villainReachProbs[hand];
				
				CfrHelper cfrHelper = new CfrHelper(p, child, hero, villain, heroPreflopCombos, villainPreflopCombos, newHeroReachProbs, newVillainReachProbs, newBoard, iterationCount);
				cfrHelpers[count++] = cfrHelper;
			}
			
			for (int i = 0; i < cfrHelpers.length; i++)
				if (i == cfrHelpers.length-1)
					cfrHelpers[i].compute();
				else
					cfrHelpers[i].fork();
		}
		
		float[] getCardWeights(float[] villainReachProbs, int[] board)
		{
			float[] cardWeights = new float[numHeroHands * 52];
			
			float villainReachSum = 0;
			float[] villainCardReachSum = new float[52];
			
			for (int hand = 0; hand < numVillainHands; hand++)
			{
				villainCardReachSum[villainPreflopCombos[hand].card1] += villainReachProbs[hand];
				villainCardReachSum[villainPreflopCombos[hand].card2] += villainReachProbs[hand];
				
				villainReachSum += villainReachProbs[hand];
			}
			
			for (int hand = 0; hand < heroPreflopCombos.length; hand++)
			{
				if (CardUtility.overlap(heroPreflopCombos[hand], board))
					continue;
				
				float totalWeight = 0;
				
				for (int card = 0; card < 52; card++)
				{
					if (CardUtility.overlap(card, board) || CardUtility.overlap(heroPreflopCombos[hand], card))
						continue;
					
					float weight = villainReachSum 
							- villainCardReachSum[card] 
							- villainCardReachSum[heroPreflopCombos[hand].card1]
							- villainCardReachSum[heroPreflopCombos[hand].card2]
							+ villainReachProbs[hand];
					
					cardWeights[hand + card*numHeroHands] = weight;
					
					totalWeight += weight;
				}
				
				for (int card = 0; card < 52; card++)
					if (!CardUtility.overlap(card, board) && totalWeight > 0 && cardWeights[hand + card*numHeroHands] > 0)
						cardWeights[hand + card*numHeroHands] /= totalWeight;	
			}
			
			return cardWeights;
		}
		
		float[] terminalNodeUtility(TerminalNode node, float[] villainReachProbs, int[] board)
		{
			if (node.type == TerminalNode.Type.ALLIN)
				return getAllinUtilities(node, villainReachProbs, board);
			if (node.type == TerminalNode.Type.UNCONTESTED)
				return getUncontestedUtilities(node, villainReachProbs, board);
			else
				return getShowdownUtilities(node, villainReachProbs, board);
		}
		
		//assumes that both players got allin on the turn
		float[] getAllinUtilities(TerminalNode node, float[] villainReachProbs, int[] board)
		{
			float[] preflopComboEvs = new float[numHeroHands];
			
			PreflopCombo[] villainPreflopCombos = prm.getPreflopCombos(villain);

			for (int card = 0; card < 52; card++)
			{
				if (CardUtility.overlap(card, board))
					continue;
				
				int newBoard[] = new int[board.length + 1];
				for (int j = 0; j < board.length; j++)
					newBoard[j] = board[j];
				newBoard[board.length] = card;
				
				float[] newVillainReachProbs = new float[numVillainHands];
				
				for (int hand = 0; hand < numVillainHands; hand++)
				{
					if (!CardUtility.overlap(villainPreflopCombos[hand], card))
						newVillainReachProbs[hand] = villainReachProbs[hand];
				}
				
				float subgameEvs[] = getShowdownUtilities(node, newVillainReachProbs, newBoard);
				
				for (int hand = 0; hand < numHeroHands; hand++)
					preflopComboEvs[hand] += subgameEvs[hand] / 44;
			}
			
			return preflopComboEvs;
		}
		
		float[] getShowdownUtilities(TerminalNode node, float[] villainReachProbs, int[] board)
		{	
			RiverCombo[] heroRiverCombos = rrm.getRiverCombos(hero, heroPreflopCombos, board);
			RiverCombo[] villainRiverCombos = rrm.getRiverCombos(villain, villainPreflopCombos, board);
					
			float[] utilities =  new float[numHeroHands];
			
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
				
				utilities[heroCombo.reachProbsIndex] = (winSum
						- cardWinSum[heroCombo.card1]
						- cardWinSum[heroCombo.card2])
						* value;
			}
			
			float loseSum = 0;
			float[] cardLoseSum = new float[52];
			
			j = villainRiverCombos.length - 1;
			for (int i = heroRiverCombos.length - 1; i >= 0; i--)
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
				
				utilities[heroCombo.reachProbsIndex] -= (loseSum
						- cardLoseSum[heroCombo.card1]
						- cardLoseSum[heroCombo.card2])
						* value;
			}
			
			return utilities;
		}
		
		float[] getUncontestedUtilities(TerminalNode node, float[] villainReachProbs, int[] board)
		{
			float villainReachSum = 0;
			float[] sumIncludingCard = new float[52];
			
			for (int hand = 0; hand < numVillainHands; hand++)
			{
				sumIncludingCard[villainPreflopCombos[hand].card1] += villainReachProbs[hand];
				sumIncludingCard[villainPreflopCombos[hand].card2] += villainReachProbs[hand];
				
				villainReachSum += villainReachProbs[hand];
			}
			
			float value = (hero == node.lastToAct) ? (-node.pot / 2.0f) : (node.pot / 2.0f);
			
			float[] utilities = new float[numHeroHands];
			
			for (int hand = 0; hand < numHeroHands; hand++)
			{	
				if (CardUtility.overlap(heroPreflopCombos[hand], board))
					continue;
				
				utilities[hand] = value * (villainReachSum
						- sumIncludingCard[heroPreflopCombos[hand].card1]
						- sumIncludingCard[heroPreflopCombos[hand].card2]
						+ villainReachProbs[hand]);	
			}
			
			return utilities;
		}
	}
}
