# discounted-cfr-poker-solver

This repository contains Java code for finding approximate nash equilibrium strategies for poker turn and river subgames using discouted counterfactual regret minimization (DCFR). The code will run several hundred iterations of DCFR and print the best response EVs and exploitability every 100 iterations. No function is written yet to print the average strategies.

The hand evaluator requires a 124MB lookup table that can be found here: https://github.com/christophschmalhofer/poker/blob/master/XPokerEval/XPokerEval.TwoPlusTwo/HandRanks.dat

To change the game tree, modify the code in the functions testTurn() and testRiver() in Main.java.

On my machine (i7 4790k), 500 cfr iterations for testTurn() take 20s and 1000 cfr iterations for testRiver() takes <1s.

At one point I had CFR+ and PCS (Public Chance Sampling) implemented, which is why the strategy and factory design patterns are used.

The code could easily be modified for flop subgames.
