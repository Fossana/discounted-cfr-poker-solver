package trainer;

import application.PreflopRangeManager;

public class TrainerFactory
{
	
	public Trainer getTrainer(TrainerType trainerType, PreflopRangeManager prm, int[] initialBoard, int initialPotSize, int inPositionPlayerId)
	{
		if (trainerType == TrainerType.PARALLEL_DCFR)
			return new ParallelDcfr(prm, initialBoard, initialPotSize, inPositionPlayerId);
		return null;
	}
}
