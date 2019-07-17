package application;

public class Action
{
	public enum Type
	{
		FOLD,
		CHECK,
		CALL,
		BET,
		RAISE
	}
	
	Type type;
	int amount;
	
	public Action(Type type, int amount)
	{
		this.type = type;
		this.amount = amount;
	}
	
	public static boolean isValidAction(Action action, int stack, int wager, int callAmount, int minimumRaiseSize)
	{
		switch(action.type)
		{
			case FOLD:
				return callAmount > 0; // don't allow folding if checking is an option
			case CHECK:
				return callAmount == 0;
			case CALL:
				return callAmount > 0
					&& ((action.amount == callAmount && action.amount <= stack)
					|| action.amount == stack);
			case BET:
				return callAmount == 0
					&& ((action.amount > minimumRaiseSize && action.amount <= stack)
					|| (action.amount > 0 && action.amount == stack));
			case RAISE:
				int raiseSize = action.amount - callAmount - wager;
				return callAmount > 0
					&& ((raiseSize >= minimumRaiseSize && action.amount <= (stack + wager))
					|| (raiseSize > 0 && action.amount == (stack + wager)));
			default:
				return false;
		}
	}
	
	@Override
    public boolean equals(Object o)
	{ 
        if (o == this)
        { 
            return true; 
        } 
  
        if (!(o instanceof Action))
        { 
            return false; 
        } 
          
        Action c = (Action) o; 
         
        return c.type == type && c.amount == amount;
    } 
}
