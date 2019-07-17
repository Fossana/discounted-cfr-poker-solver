package application;

public class TreeBuildSettings
{
	private PreflopRange p1Range;
	private PreflopRange p2Range;
	
	private int inPositionPlayer;
	
	private Street initialStreet;
	private int[] initialBoard;
	private int initialPot;
	private int startingStack;
	
	private BetSettings p1BetSettings;
	private BetSettings p2BetSettings;
	
	private int minimumBetSize;
	private float allinThreshold;
	
	public TreeBuildSettings(PreflopRange p1Range,
			PreflopRange p2Range,
			int inPositionPlayerId,
			Street initialStreet,
			int[] initialBoard,
			int initialPotSize,
			int effectiveStartingStackSize,
			BetSettings p1BetSettings,
			BetSettings p2BetSettings,
			int minimumBetSize,
			float allinThreshold)
	{
		this.p1Range = p1Range;
		this.p2Range = p2Range;
		this.inPositionPlayer = inPositionPlayerId;
		this.initialStreet = initialStreet;
		this.initialBoard = initialBoard;
		this.initialPot = initialPotSize;
		this.startingStack = effectiveStartingStackSize;
		this.p1BetSettings = p1BetSettings;
		this.p2BetSettings = p2BetSettings;
		this.minimumBetSize = minimumBetSize;
		this.allinThreshold = allinThreshold;
	}

	public PreflopRange getP1Range()
	{
		return p1Range;
	}

	public PreflopRange getP2Range()
	{
		return p2Range;
	}

	public int getInPositionPlayer()
	{
		return inPositionPlayer;
	}

	public int[] getInitialBoard()
	{
		return initialBoard;
	}
	
	public Street getInitialStreet()
	{
		return initialStreet;
	}
	
	public void setInitialStreet(Street street)
	{
		initialStreet = street;
	}

	public int getInitialPot()
	{
		return initialPot;
	}

	public int getStartingStack()
	{
		return startingStack;
	}

	public BetSettings getP1BetSettings()
	{
		return p1BetSettings;
	}

	public BetSettings getP2BetSettings()
	{
		return p2BetSettings;
	}

	public int getMinimumBetSize()
	{
		return minimumBetSize;
	}

	public float getAllinThreshold()
	{
		return allinThreshold;
	}
}
