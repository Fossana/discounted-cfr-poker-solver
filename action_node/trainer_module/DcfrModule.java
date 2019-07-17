package action_node.trainer_module;

import application.ActionNode;

public class DcfrModule extends TrainerModule
{
	private float[] cumulativeRegret;
	private float[] cumulativeStrategy;
	
	int numHands;
	int numActions;
	
	public int current;
	
	public DcfrModule(ActionNode node)
	{
		numHands = node.getNumHands();
		numActions = node.getNumActions();
		current = node.player;
		
		cumulativeRegret = new float[numHands * numActions];
		cumulativeStrategy = new float[numHands *numActions];
	}
	
	@Override
	public float[] getAverageStrategy()
	{
		float[] averageStrategy = new float[numHands * numActions];
		
		for (int hand = 0; hand < numHands; hand++)
		{
			float total = 0;
			
			for (int action = 0; action < numActions; action++)
				total += cumulativeStrategy[hand + action*numHands];
			
			if (total > 0)
			{
				for (int action = 0; action < numActions; action++)
					averageStrategy[hand + action*numHands] = cumulativeStrategy[hand + action*numHands] / total;
			}
			else
			{
				for (int action = 0; action < numActions; action++)
					averageStrategy[hand + action*numHands] = 1.0f / numActions;
			}
		}
		
		return averageStrategy;
	}
	
	@Override
	public float[] getCurrentStrategy()
	{	
		float[] currentStrategy = new float[numHands * numActions];
		
		for (int hand = 0; hand < numHands; hand++)
		{
			float positiveRegretSum = 0;
			
			for (int action = 0; action < numActions; action++)
				if (cumulativeRegret[hand + action*numHands] > 0)
					positiveRegretSum += cumulativeRegret[hand + action*numHands];
			
			if (positiveRegretSum > 0)
			{
				for (int action = 0; action < numActions; action++)
					if (cumulativeRegret[hand + action*numHands] > 0)
						currentStrategy[hand + action*numHands] = cumulativeRegret[hand + action*numHands] / positiveRegretSum;
					else
						currentStrategy[hand + action*numHands] = 0;
			}
			else
			{
				for (int action = 0; action < numActions; action++)
					currentStrategy[hand + action*numHands] = 1.0f / numActions;
			}
		}
		
		return currentStrategy;
	}
	
	public void updateCumulativeRegretPartOne(float[] actionUtilities, int actionIndex)
	{
		for (int hand = 0; hand < numHands; hand++)
			cumulativeRegret[hand + actionIndex*numHands] += actionUtilities[hand];
	}
	
	public void updateCumulativeRegretPartTwo(float[] utilities, int iterationCount)
	{
		float x = (float) Math.pow((double) iterationCount, 1.5f);
		x = x / (x + 1);
		
		for (int action = 0; action < numActions; action++)
		{
			for (int hand = 0; hand < numHands; hand++)
			{
				cumulativeRegret[hand + action*numHands] -= utilities[hand];
				if (cumulativeRegret[hand + action*numHands] > 0)
					cumulativeRegret[hand + action*numHands] *= x;
				else if (cumulativeRegret[hand + action*numHands] < 0)
					cumulativeRegret[hand + action*numHands] *= 0.5f;
			}
				
		}
	}
	
	public void updateCumulativeStrategy(float[] strategy, float[] reachProbs, int iterationCount)
	{
		float x = (float) Math.pow(((double) iterationCount / (iterationCount+1)), 2);
		
		for (int action = 0; action < numActions; action++)
		{
			for (int hand = 0; hand < numHands; hand++)
			{
				cumulativeStrategy[hand + action*numHands] += strategy[hand + action*numHands] * reachProbs[hand];
				cumulativeStrategy[hand + action*numHands] *= x;
			}	
		}
	}
}
