package action_node.trainer_module;

import application.ActionNode;

public abstract class TrainerModule
{
	public ActionNode node;
	
	public abstract float[] getAverageStrategy();
	public abstract float[] getCurrentStrategy();
}
