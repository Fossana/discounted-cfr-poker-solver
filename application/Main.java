package application;
	
import java.io.IOException;
import java.util.Arrays;
import trainer.Trainer;
import trainer.TrainerFactory;
import trainer.TrainerType;

public class Main {
	
	static void testTurn() throws IOException
	{
		PreflopRange p1PreflopRange = new PreflopRange("AA,KK,QQ,JJ,TT,99,88,77,66,55,44,33,22,AK,AQ,AJ,AT,A9,A8,A7,A6,A5,A4,A3,A2,KQ,KJ,KT,K9,K8,K7,K6,K5,K4,K3,K2,QJ,QT,Q9,Q8,Q7,Q6,Q5,Q4,Q3,Q2,JT,J9,J8,J7,J6,J5,J4,J3,J2,T9,T8,T7,T6,T5,T4,T3,T2,98,97,96,95,94,93,92,87,86,85,84,83,82,76,75,74,73,72,65,64,63,62,54,53,52,43,42,32");
		PreflopRange p2PreflopRange = new PreflopRange("AA,KK,QQ,JJ,TT,99,88,77,66,55,44,33,22,AK,AQ,AJ,AT,A9,A8,A7,A6,A5,A4,A3,A2,KQ,KJ,KT,K9,K8,K7,K6,K5,K4,K3,K2,QJ,QT,Q9,Q8,Q7,Q6,Q5,Q4,Q3,Q2,JT,J9,J8,J7,J6,J5,J4,J3,J2,T9,T8,T7,T6,T5,T4,T3,T2,98,97,96,95,94,93,92,87,86,85,84,83,82,76,75,74,73,72,65,64,63,62,54,53,52,43,42,32");
		int inPositionPlayer = 2;
		
		Street initialStreet = Street.TURN;
		int[] initialBoard = new int[]{CardUtility.cardFromString("Kd"), CardUtility.cardFromString("Jd"), CardUtility.cardFromString("Td"), CardUtility.cardFromString("5s")};
		int initialPot = 400;
		int startingStack = 800;
		
		BetSettings p1BetSettings = new BetSettings();
		BetSettings p2BetSettings = new BetSettings();
		
		p1BetSettings.flopBetSizes = Arrays.asList(1.0f);
		p1BetSettings.turnBetSizes = Arrays.asList(0.5f, 1.0f);
		p1BetSettings.riverBetSizes = Arrays.asList(0.5f, 1.0f);
		
		p1BetSettings.flopRaiseSizes = Arrays.asList(1.0f);
		p1BetSettings.turnRaiseSizes = Arrays.asList(0.5f);
		p1BetSettings.riverRaiseSizes = Arrays.asList(0.5f);
		
		p2BetSettings.flopBetSizes = Arrays.asList(1.0f);
		p2BetSettings.turnBetSizes = Arrays.asList(0.5f, 1.0f);
		p2BetSettings.riverBetSizes = Arrays.asList(0.5f, 1.0f);
		
		p2BetSettings.flopRaiseSizes = Arrays.asList(1.0f);
		p2BetSettings.turnRaiseSizes = Arrays.asList(0.5f);
		p2BetSettings.riverRaiseSizes = Arrays.asList(0.5f);
		
		int minimumBetSize = 10;
		float allinThreshold = 0.67f;
		
		TreeBuildSettings treeBuildSettings = new TreeBuildSettings(
			p1PreflopRange,
			p2PreflopRange,
			inPositionPlayer,
			initialStreet,
			initialBoard,
			initialPot,
			startingStack,
			p1BetSettings,
			p2BetSettings,
			minimumBetSize,
			allinThreshold
		);
		
		GameTree gameTree = new GameTree(treeBuildSettings);
		
		Node root = gameTree.build();
		gameTree.printTree(root, 0);
		System.out.println("");
		
		TrainerFactory trainerFactory = new TrainerFactory();
		Trainer trainer = trainerFactory.getTrainer(TrainerType.PARALLEL_DCFR, new PreflopRangeManager(p1PreflopRange.getPreflopCombos(), p2PreflopRange.getPreflopCombos(), initialBoard), initialBoard, initialPot, inPositionPlayer);
		
		trainer.train(root, 500);
	}
	
	static void testRiver() throws IOException
	{
		PreflopRange p1PreflopRange = new PreflopRange("AA,KK,QQ,JJ,TT,99,88,77,66,55,44,33,22,AK,AQ,AJ,AT,A9,A8,A7,A6,A5,A4,A3,A2,KQ,KJ,KT,K9,K8,K7,K6,K5,K4,K3,K2,QJ,QT,Q9,Q8,Q7,Q6,Q5,Q4,Q3,Q2,JT,J9,J8,J7,J6,J5,J4,J3,J2,T9,T8,T7,T6,T5,T4,T3,T2,98,97,96,95,94,93,92,87,86,85,84,83,82,76,75,74,73,72,65,64,63,62,54,53,52,43,42,32");
		PreflopRange p2PreflopRange = new PreflopRange("AA,KK,QQ,JJ,TT,99,88,77,66,55,44,33,22,AK,AQ,AJ,AT,A9,A8,A7,A6,A5,A4,A3,A2,KQ,KJ,KT,K9,K8,K7,K6,K5,K4,K3,K2,QJ,QT,Q9,Q8,Q7,Q6,Q5,Q4,Q3,Q2,JT,J9,J8,J7,J6,J5,J4,J3,J2,T9,T8,T7,T6,T5,T4,T3,T2,98,97,96,95,94,93,92,87,86,85,84,83,82,76,75,74,73,72,65,64,63,62,54,53,52,43,42,32");
		int inPositionPlayer = 2;
		
		Street initialStreet = Street.RIVER;
		int[] initialBoard = new int[]{CardUtility.cardFromString("Kd"), CardUtility.cardFromString("Jd"), CardUtility.cardFromString("Td"), CardUtility.cardFromString("5s"), CardUtility.cardFromString("8s")};
		int initialPot = 400;
		int startingStack = 800;
		
		BetSettings p1BetSettings = new BetSettings();
		BetSettings p2BetSettings = new BetSettings();
		
		p1BetSettings.flopBetSizes = Arrays.asList(1.0f);
		p1BetSettings.turnBetSizes = Arrays.asList(1.0f);
		p1BetSettings.riverBetSizes = Arrays.asList(1.0f);
		
		p1BetSettings.flopRaiseSizes = Arrays.asList(1.0f);
		p1BetSettings.turnRaiseSizes = Arrays.asList(1.0f);
		p1BetSettings.riverRaiseSizes = Arrays.asList(1.0f);
		
		p2BetSettings.flopBetSizes = Arrays.asList(1.0f);
		p2BetSettings.turnBetSizes = Arrays.asList(1.0f);
		p2BetSettings.riverBetSizes = Arrays.asList(1.0f);
		
		p2BetSettings.flopRaiseSizes = Arrays.asList(1.0f);
		p2BetSettings.turnRaiseSizes = Arrays.asList(1.0f);
		p2BetSettings.riverRaiseSizes = Arrays.asList(1.0f);
		
		int minimumBetSize = 10;
		float allinThreshold = 0.67f;
		
		TreeBuildSettings treeBuildSettings = new TreeBuildSettings(
			p1PreflopRange,
			p2PreflopRange,
			inPositionPlayer,
			initialStreet,
			initialBoard,
			initialPot,
			startingStack,
			p1BetSettings,
			p2BetSettings,
			minimumBetSize,
			allinThreshold
		);
		
		GameTree gameTree = new GameTree(treeBuildSettings);
		
		Node root = gameTree.build();
		gameTree.printTree(root, 0);
		System.out.println("");
		
		TrainerFactory trainerFactory = new TrainerFactory();
		Trainer trainer = trainerFactory.getTrainer(TrainerType.PARALLEL_DCFR, new PreflopRangeManager(p1PreflopRange.getPreflopCombos(), p2PreflopRange.getPreflopCombos(), initialBoard), initialBoard, initialPot, inPositionPlayer);
		
		trainer.train(root, 1000);
	}
	
	public static void main(String[] args) throws IOException
	{
		testTurn();
	}
}
