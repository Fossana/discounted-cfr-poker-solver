package application;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class GameTree
{
	SplittableRandom rand = new SplittableRandom();
	
	TreeBuildSettings treeBuildSettings;
	
	int p1NumHands;
	int p2NumHands;
	
	int flopActionNodeCount = 0;
	int turnActionNodeCount = 0;
	int riverActionNodeCount = 0;
	int chanceNodeCount = 0;
	int terminalNodeCount = 0;
	
	void printTree(Node node, int tabCount)
	{
	    if (node instanceof ActionNode)
	    {
	        ActionNode actionNode = (ActionNode) node;

	        for (int i = 0; i < actionNode.getNumActions(); i++)
	        {
	        	Action action = actionNode.getAction(i);
	            Node child = actionNode.getChild(i);

	            for (int j = 0; j < tabCount; j++)
	            	System.out.print("    ");    

	            if (action.type == Action.Type.FOLD || action.type == Action.Type.CHECK)
	            	System.out.print("p" + actionNode.player + ": " + action.type);
	            else
	            	System.out.print("p" + actionNode.player + ": " + action.type + " " + action.amount);
	            
	            System.out.println("");

	            printTree(child, tabCount+1);
	        }
	    }
	    else if (node instanceof ChanceNode)
	    {
	    	for (int j = 0; j < tabCount; j++)
            	System.out.print("    ");

	        ChanceNode chanceNode = (ChanceNode) node;
	        System.out.print(chanceNode.type);
	        
	        System.out.println("");

	        int index = 0;
	        Node child = chanceNode.getChild(index);
	        while (child == null)
	        	child = chanceNode.getChild(++index);
	        
	        printTree(child, tabCount+1);
	    }
	    else if (node instanceof TerminalNode)
	    {
	    	for (int j = 0; j < tabCount; j++)
            	System.out.print("    ");

	        TerminalNode terminalNode = (TerminalNode) node;
	        System.out.print(terminalNode.type + ": POT: " + terminalNode.pot + " " + "LAST_TO_ACT: p" + terminalNode.lastToAct);
	        System.out.println("");
	    }
	}
    
    GameTree(TreeBuildSettings treeBuildSettings)
    {
    	this.treeBuildSettings = treeBuildSettings;
    	p1NumHands = treeBuildSettings.getP1Range().getNumHands();
    	p2NumHands = treeBuildSettings.getP2Range().getNumHands();
    }
    
    State getInitialState()
    {
    	State state = new State();

		state.setStreet(treeBuildSettings.getInitialStreet());
		state.setBoard(treeBuildSettings.getInitialBoard());
		state.setPot(treeBuildSettings.getInitialPot());
		
		state.setMinimumBetSize(treeBuildSettings.getMinimumBetSize());
		state.setMinimumRaiseSize(treeBuildSettings.getMinimumBetSize());
		
		PlayerState p1 = new PlayerState(1, treeBuildSettings.getInPositionPlayer() == 1, treeBuildSettings.getStartingStack());
		PlayerState p2 = new PlayerState(2, treeBuildSettings.getInPositionPlayer() == 2, treeBuildSettings.getStartingStack());
		
		state.setP1(p1);
		state.setP2(p2);
		
		state.initializeCurrent();
		state.initializeLastToAct();
		
		return state;
    }
	
	public Node build()
	{
		Node root = buildActionNodes(null, getInitialState());
		root.parent = root;
		
		System.out.println("flop action node count: " + flopActionNodeCount);
		System.out.println("turn action node count: " + turnActionNodeCount);
		System.out.println("river action node count: " + riverActionNodeCount);
		System.out.println("chance node count: " + chanceNodeCount);
		System.out.println("terminal node count: " + terminalNodeCount);
		
		return root;
	}
	
	void buildAction(ActionNode node, State state, Action action, List<Node> children, List<Action> actions)
	{
		if (Action.isValidAction(action, state.getCurrent().getStack(), state.getCurrent().getWager(), state.getCallAmount(), state.getMinimumRaiseSize()))
		{
			Node child = null;
			State nextState = new State(state);
			boolean betsSettled = nextState.applyPlayerAction(action);
			if (betsSettled)
			{
				if (nextState.isUncontested() || nextState.bothPlayersAreAllin() || state.getStreet() == Street.RIVER)
					child = buildTerminalNodes(node, nextState);
				else
					child = buildChanceNodes(node, nextState);
			}
			else
			{
				child = buildActionNodes(node, nextState);
			}
			children.add(child);
			actions.add(action);
		}
	}
	
	Node buildActionNodes(Node parent, State state)
	{
		ActionNode actionNode = new ActionNode(parent, state.getCurrent().getId());
		
		List<Node> children = new ArrayList<>();
		List<Action> actions = new ArrayList<>();
		
		if (state.getStreet() == Street.FLOP)
			flopActionNodeCount++;
		else if (state.getStreet() == Street.TURN)
			turnActionNodeCount++;
		else if (state.getStreet() == Street.RIVER)
			riverActionNodeCount++;
		
		List<Float> betSizes = null;
		List<Float> raiseSizes = null;
		
		BetSettings p1BetSettings = treeBuildSettings.getP1BetSettings();
		BetSettings p2BetSettings = treeBuildSettings.getP2BetSettings();
		
		if (state.getStreet() == Street.FLOP)
		{
			if (state.getCurrent().getId() == 1)
			{
				betSizes = p1BetSettings.flopBetSizes;
				raiseSizes = p1BetSettings.flopRaiseSizes;
			}
			else
			{
				betSizes = p2BetSettings.flopBetSizes;
				raiseSizes = p2BetSettings.flopRaiseSizes;
			}
		}
		else if (state.getStreet() == Street.TURN)
		{
			if (state.getCurrent().getId() == 1)
			{
				betSizes = p1BetSettings.turnBetSizes;
				raiseSizes = p1BetSettings.turnRaiseSizes;
			}
			else
			{
				betSizes = p2BetSettings.turnBetSizes;
				raiseSizes = p2BetSettings.turnRaiseSizes;
			}
		}
		else if (state.getStreet() == Street.RIVER)
		{
			if (state.getCurrent().getId() == 1)
			{
				betSizes = p1BetSettings.riverBetSizes;
				raiseSizes = p1BetSettings.riverRaiseSizes;
			}
			else
			{
				betSizes = p2BetSettings.riverBetSizes;
				raiseSizes = p2BetSettings.riverRaiseSizes;
			}
		}
		
		for (Action.Type actionType : Action.Type.values())
		{
			if (actionType == Action.Type.FOLD)
			{
				Action action = new Action(Action.Type.FOLD, 0);
				buildAction(actionNode, state, action, children, actions);
			}
			else if (actionType == Action.Type.CHECK)
			{
				Action action = new Action(Action.Type.CHECK, 0);
				buildAction(actionNode, state, action, children, actions);
			}	
			else if (actionType == Action.Type.CALL)
			{
				Action action = new Action(Action.Type.CALL, state.getCallAmount());
				buildAction(actionNode, state, action, children, actions);
			}
			else if (actionType == Action.Type.BET)
			{
				for (float betSize : betSizes)
				{
					int betAmount = (int) (betSize * state.getPot());
					betAmount = Math.min(betAmount, state.getCurrent().getStack());
					if (((float) betAmount + state.getCurrent().getWager()) / (state.getCurrent().getStack() + state.getCurrent().getWager()) >= treeBuildSettings.getAllinThreshold())
					{
						betAmount = state.getCurrent().getStack();
						Action action = new Action(Action.Type.BET, betAmount);
						buildAction(actionNode, state, action, children, actions);
						break;
					}
					else
					{
						Action action = new Action(Action.Type.BET, betAmount);
						buildAction(actionNode, state, action, children, actions);
					}	
				}
			}
			else if (actionType == Action.Type.RAISE)
			{
				for (float raiseSize : raiseSizes)
				{
					int raiseAmount = (int) (state.getCurrent().getWager() + state.getCallAmount() + raiseSize * (state.getPot() + state.getCallAmount()));
					raiseAmount = Math.min(raiseAmount, state.getCurrent().getWager() + state.getCurrent().getStack());
					if (((float) raiseAmount / (state.getCurrent().getStack() + state.getCurrent().getWager())) >= treeBuildSettings.getAllinThreshold())
					{
						raiseAmount = state.getCurrent().getStack() + state.getCurrent().getWager();
						Action action = new Action(Action.Type.RAISE, raiseAmount);
						buildAction(actionNode, state, action, children, actions);
						break;
					}
					else
					{
						Action action = new Action(Action.Type.RAISE, raiseAmount);
						buildAction(actionNode, state, action, children, actions);
					}
				}
			}
		}
		
		if (state.getCurrent().getId() == 1)
			actionNode.initialize(children, actions, p1NumHands);
		else
			actionNode.initialize(children, actions, p2NumHands);
		
		return actionNode;
	}
	
	Node buildChanceNodes(Node parent, State state)
	{
		ChanceNode chanceNode = null;
		
		Street currentStreet = state.getStreet();
		
		if (currentStreet == Street.FLOP)
			chanceNode = new ChanceNode(parent, ChanceNode.Type.DEAL_TURN);
		else if (currentStreet == Street.TURN)
			chanceNode = new ChanceNode(parent, ChanceNode.Type.DEAL_RIVER);
			
		chanceNodeCount++;
		
		int[] board = state.getBoard();
		
		for (int card = 0; card < 52; card++)
		{
			if (CardUtility.overlap(card, board))
				continue;

			State nextState = new State(state);
			
			if (currentStreet == Street.TURN)
				nextState.setTurn(card);
			else if (currentStreet == Street.RIVER)
				nextState.setRiver(card);
			
			nextState.goToNextStreet();
			
			Node actionNode = buildActionNodes(chanceNode, nextState);
			chanceNode.addChild(actionNode, card);
		}
		
		return chanceNode;
	}
	
	Node buildTerminalNodes(Node parent, State state)
	{
		terminalNodeCount++;
		
		TerminalNode terminalNode = null;
		
		if (state.bothPlayersAreAllin() && state.getStreet() != Street.RIVER)
			terminalNode = new TerminalNode(parent, TerminalNode.Type.ALLIN);
		else if (state.isUncontested())
			terminalNode = new TerminalNode(parent, TerminalNode.Type.UNCONTESTED);
		else
			terminalNode = new TerminalNode(parent, TerminalNode.Type.SHOWDOWN);
		
		terminalNode.pot = state.getPot();
		
		Node node = parent;
		while (!(node instanceof ActionNode))
		{
			node = node.parent;
		}
		
		terminalNode.lastToAct = ((ActionNode) node).player;
		
		return terminalNode;
	}
}
