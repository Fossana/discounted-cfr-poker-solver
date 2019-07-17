package application;

public class PlayerState
{
	
	private boolean hasPosition;
	private boolean hasFolded;
	
	private int stack;
	private int wager;
	private int id;
	
	public PlayerState(int id, boolean hasPosition, int stackSize)
	{
		this.id = id;
		this.hasPosition = hasPosition;
		this.stack = stackSize;
		hasFolded = false;
		wager = 0;
	}
	
	public boolean hasPosition()
	{
		return hasPosition;
	}
	
	public PlayerState(PlayerState playerState)
	{
		id = playerState.id;
		hasPosition = playerState.hasPosition;
		hasFolded = playerState.hasFolded;
		stack = playerState.stack;
		wager = playerState.wager;
		hasPosition = playerState.hasPosition;
	}
	
	public int getStack()
	{
		return stack;
	}
	
	public int getId()
	{
		return id;
	}

	
	@Override
    public boolean equals(Object obj)
	{ 
		if (obj == null)
		{
            return false;
        }

        if (!PlayerState.class.isAssignableFrom(obj.getClass()))
        {
            return false;
        }

        final PlayerState other = (PlayerState) obj;
        
        return this.id == other.id;
    }
	
	@Override
    public int hashCode()
	{
        return id;
    }
	
	public boolean hasFolded()
	{
		return hasFolded;
	}
	
	public int getWager()
	{
		return wager;
	}
	
	public void resetWager()
	{
		wager = 0;
	}
	
	public boolean isAllin()
	{
		return stack == 0;
	}
	
	public void fold()
	{
		hasFolded = true;
	}
	
	public void commitChips(int amount)
	{
		stack -= amount;
		wager += amount;
	}
	
	public void uncommitChips(int amount)
	{
		wager -= amount;
		stack += amount;
	}
}
