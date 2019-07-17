package application;

public class TerminalNode extends Node
{
	public enum Type
	{
		ALLIN,
		UNCONTESTED,
		SHOWDOWN
	}
	
	public TerminalNode(Node parent, Type type)
	{
		super(parent);
		pot = 0;
		this.type = type;
	}
	
	public Type type;
	
	public int lastToAct;
	public int pot;
}
