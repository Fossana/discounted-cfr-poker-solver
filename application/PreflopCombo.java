package application;

public class PreflopCombo
{
	public float probability;
	public float relativeProbability;
	
	public int card1;
	public int card2;
	
	PreflopCombo(int card1, int card2, float probability)
	{
		this.card1 = card1;
		this.card2 = card2;
		this.probability = probability;
		relativeProbability = 0.0f;
	}
	
	@Override
    public String toString() { 
        return CardUtility.cardToString(card1) + CardUtility.cardToString(card2);
    } 
}
