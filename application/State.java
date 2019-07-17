package application;

public class State
{
	private Street street;
	private int pot;
	private int[] board;
	
	PlayerState p1;
	PlayerState p2;
	
	PlayerState current;
	PlayerState lastToAct;
	
	private int minimumRaiseSize;
	private int minimumBetSize;
	
	public State()
	{
	}
	
	public State(State state)
	{
		street = state.getStreet();
		pot = state.getPot();
		
		board = new int[state.getBoard().length];
		for (int i = 0; i < board.length; i++)
			board[i] = state.getBoard()[i];
		
		p1 = new PlayerState(state.getP1());
		p2 = new PlayerState(state.getP2());
		
		if (state.current.getId() == 1)
			current = p1;
		else
			current = p2;
		
		if (state.lastToAct.getId() == 1)
			lastToAct = p1;
		else
			lastToAct = p2;
		
		minimumRaiseSize = state.getMinimumRaiseSize();
		minimumBetSize = state.getMinimumBetSize();
	}
	
	public PlayerState getP1()
	{
		return p1;
	}
	
	public PlayerState getP2()
	{
		return p2;
	}
	
	public PlayerState getCurrent()
	{
		return current;
	}
	
	public PlayerState getLastToAct()
	{
		return lastToAct;
	}
	
	public void setP1(PlayerState playerState)
	{
		p1 = playerState;
	}
	
	public void setP2(PlayerState playerState)
	{
		p2 = playerState;
	}
	
	public void setTurn(int card)
	{
		int[] newBoard = new int[4];
		
		for (int i = 0; i < 3; i++)
			newBoard[i] = board[i];
		
		newBoard[3] = card;
		board = newBoard;
	}
	
	public void setRiver(int card)
	{
		int[] newBoard = new int[5];
		
		for (int i = 0; i < 4; i++)
			newBoard[i] = board[i];
		
		newBoard[4] = card;
		board = newBoard;
	}

	public Street getStreet()
	{
		return street;
	}

	public void setStreet(Street street)
	{
		this.street = street;
	}

	public int getPot()
	{
		return pot;
	}

	public void setPot(int potSize)
	{
		this.pot = potSize;
	}
	
	public int[] getBoard()
	{
		return board;
	}
	
	public void setBoard(int[] board)
	{
		this.board = board;
	}

	public int getMinimumRaiseSize()
	{
		return minimumRaiseSize;
	}

	public void setMinimumRaiseSize(int minimumRaiseSize)
	{
		this.minimumRaiseSize = minimumRaiseSize;
	}
	
	public int getMinimumBetSize()
	{
		return minimumBetSize;
	}

	public void setMinimumBetSize(int minimumBetSize)
	{
		this.minimumBetSize = minimumBetSize;
	}
	
	public int getHighestWager()
	{
		return Math.max(p1.getWager(), p2.getWager());
	}
	
	public int getCallAmount()
	{
		return getHighestWager() - current.getWager();
	}
	
	boolean isUncontested()
	{
		return p1.hasFolded() || p2.hasFolded();
	}
	
	boolean bothPlayersAreAllin()
	{
		return p1.isAllin() && p2.isAllin();
	}
	
	public boolean applyPlayerAction(Action action)
	{
		if (action.type == Action.Type.FOLD)
		{
			current.fold();
			pot -= getCallAmount();
			return true;
		}
		else if (action.type == Action.Type.CHECK)
		{
			if (current == lastToAct)
				return true;
		}
		else if (action.type == Action.Type.CALL)
		{
			current.commitChips(action.amount);
			pot += action.amount;
			return true;
		}
		else if (action.type == Action.Type.BET)
		{
			current.commitChips(action.amount);
			pot += action.amount;
			minimumRaiseSize = action.amount;
			resetLastToAct();
		}
		else if (action.type == Action.Type.RAISE)
		{
			int chipsToCommit = action.amount - current.getWager();
			current.commitChips(chipsToCommit);
			pot += chipsToCommit;
			int raiseSize = action.amount - getHighestWager();
			if (raiseSize > minimumRaiseSize)
			{
				minimumRaiseSize = raiseSize;
			}
			resetLastToAct();
		}
		updateCurrent();
		return false;
	}
	
	public void goToNextStreet()
	{
		if (street == Street.PREFLOP)
			street = Street.FLOP;
		else if (street == Street.FLOP)
			street = Street.TURN;
		else if (street == Street.TURN)
			street = Street.RIVER;
		
		p1.resetWager();
		p2.resetWager();
		
		initializeCurrent();
		initializeLastToAct();
		
		minimumRaiseSize = minimumBetSize;
	}
	
	void initializeCurrent()
	{
		if (!p1.hasPosition())
			current = p1;
		else
			current = p2;
	}
	
	void initializeLastToAct()
	{
		if (p1.hasPosition())
			lastToAct = p1;
		else
			lastToAct = p2;
	}
	
	public void resetLastToAct()
	{
		if (lastToAct == p1)
			lastToAct = p2;
		else
			lastToAct = p1;
	}
	
	void updateCurrent()
	{
		if (current == p1)
			current = p2;
		else
			current = p1;
	}
}
