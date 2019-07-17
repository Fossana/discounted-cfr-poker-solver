package application;

public class ChanceNode extends Node
{
	public enum Type
	{
		DEAL_TURN,
		DEAL_RIVER
	}
	
	public ChanceNode(Node parent, Type type)
	{
		super(parent);
		this.type = type;
		children = new Node[52];
	}
	
	private Node[] children;
	private int childCount = 0;
	
	public Type type;
	
	public void addChild(Node node, int card)
	{
		children[card] = node;
		childCount++;
	}
	
	public Node[] getChildren()
	{
		return children;
	}
	
	public int getChildCount()
	{
		return childCount;
	}
	
	public Node getChild(int card)
	{
		return children[card];
	}
}
