package application;

public class RiverCombo implements Comparable<RiverCombo>
{
	public int card1;
	public int card2;
	public int rank;
	public int reachProbsIndex;
	public float probability;
	
	RiverCombo(int card1, int card2, float probability, int reachProbsIndex)
	{
		this.card1 = card1;
		this.card2 = card2;
		this.probability = probability;
		this.reachProbsIndex = reachProbsIndex;
		rank = 0;
	}

	@Override
	public int compareTo(RiverCombo o)
	{
		if (this.rank > o.rank)
    		return 1;
    	if (this.rank < o.rank)
    		return -1;
        return 0;
	}
}
