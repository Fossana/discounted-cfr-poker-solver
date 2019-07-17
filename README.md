# discounted-cfr-poker-solver

This repository contains Java code for finding approximate nash equilibrium strategies for poker turn and river subgames using discouted counterfactual regret minimization (DCFR). The solution is multithreaded. At one point I had CFR+ and PCS (Public Chance Sampling) implemented, which is why the strategy and factory pattern are used. The code could easily be modified for flop subgames.
