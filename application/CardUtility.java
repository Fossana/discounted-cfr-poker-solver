package application;

public class CardUtility
{
	public static int cardFromString(String str)
	{
		char rank = str.charAt(0);
		char suit = str.charAt(1);
		
		return (rankToInt(rank) - 2) * 4 + suitToInt(suit);
	}
	
	public static int cardFromRankAndSuit(char rank, int suit)
	{	
		return (rankToInt(rank) - 2) * 4 + suit;
	}
	
	public static String cardToString(int card)
	{
		int rank = card / 4 + 2;
		int suit = card - (rank-2)*4;
		return rankToString(rank) + suitToString(suit);
	}
	
	static String suitToString(int suit)
	{
		switch(suit)
        {
            case 0: return "c";
			case 1: return "d";
			case 2: return "h";
			case 3: return "s";
			default: return "c";
        }
	}
	
	static String rankToString(int rank)
	{
		switch(rank)
        {
            case 2: return "2";
			case 3: return "3";
			case 4: return "4";
			case 5: return "5";
			case 6: return "6";
			case 7: return "7";
			case 8: return "8";
			case 9: return "9";
			case 10: return "T";
			case 11: return "J";
			case 12: return "Q";
			case 13: return "K";
			case 14: return "A";
			default: return "2";
        }
	}
	
	static int rankToInt(char rank)
    {
        switch(rank)
        {
            case '2': return 2;
			case '3': return 3;
			case '4': return 4;
			case '5': return 5;
			case '6': return 6;
			case '7': return 7;
			case '8': return 8;
			case '9': return 9;
			case 'T': return 10;
			case 'J': return 11;
			case 'Q': return 12;
			case 'K': return 13;
			case 'A': return 14;
			default: return 2;
        }
    }
	
	static int suitToInt(char suit)
    {
        switch(suit)
        {
            case 'c': return 0;
			case 'd': return 1;
			case 'h': return 2;
			case 's': return 3;
			default: return 0;
        }
    }
	
	static public boolean overlap(PreflopCombo combo1, PreflopCombo combo2)
	{
		if (combo1.card1 == combo2.card1 || combo1.card1 == combo2.card2)
			return true;
		
		if (combo1.card2 == combo2.card1 || combo1.card2 == combo2.card2)
			return true;
		
		return false;
	}
    
    static public int boardToKey(int[] board)
    {
    	if (board.length == 3)
    		return 100000000 * board[0] + 1000000 * board[1] + 10000 * board[2];
    	else if (board.length == 4)
    		return 100000000 * board[0] + 1000000 * board[1] + 10000 * board[2] + 100 * board[3];
    	else
    		return 100000000 * board[0] + 1000000 * board[1] + 10000 * board[2] + 100 * board[3] + board[4];
    }
	
	static public boolean overlap(PreflopCombo combo, int card)
	{
		if (combo.card1 == card || combo.card2 == card)
			return true;
		return false;
	}
	
	static public boolean overlap(int card, int[] board)
	{
		for (int i = 0; i < board.length; i++)
			if (card == board[i])
				return true;
		return false;
	}
	
	static public boolean overlap(PreflopCombo combo, int[] board)
	{
		for (int i = 0; i < board.length; i++)
				if (combo.card1 == board[i] || combo.card2 == board[i])
					return true;
		return false;
	}
}

