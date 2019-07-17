package application;

import java.util.List;

import action_node.trainer_module.TrainerModule;

public class ActionNode extends Node
{
	private TrainerModule trainerModule;
	
	public TrainerModule getTrainerModule()
	{
		return trainerModule;
	}
	
	public void setTrainerModule(TrainerModule trainerModule)
	{
		this.trainerModule = trainerModule;
		trainerModule.node = this;
	}
	
	Node[] children;
	Action[] actions;
	
	private int numActions;
	private int numHands;
	
	public int player;
	
	public ActionNode(Node parent, int player)
	{
		super(parent);
		this.player = player;
	}
	
	public void initialize(List<Node> nodes, List<Action> actions, int numHands)
	{
		this.numHands = numHands;
		initializeChildren(nodes);
		initializeActions(actions);
	}
	
	public void initializeChildren(List<Node> nodes)
	{
		children = new Node[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
			children[i] = nodes.get(i);
	}
	
	public void initializeActions(List<Action> actions)
	{
		numActions = actions.size();
		this.actions = new Action[numActions];
		for (int i = 0; i < numActions; i++)
			this.actions[i] = actions.get(i);
	}
	
	public int getChildCount()
	{
		return children.length;
	}

	public Node getChild(int index)
	{
		return children[index];
	}
	
	public Action getAction(int index)
	{
		return actions[index];
	}
	
	public float[] getAverageStrategy()
	{
		return trainerModule.getAverageStrategy();
	}
	
	public float[] getCurrentStrategy()
	{
		return trainerModule.getCurrentStrategy();
	}

	public int getNumActions()
	{
		return numActions;
	}

	public int getNumHands()
	{
		return numHands;
	}
}
