# fruits-game
A game-playing algorithm to capture maximum fruits on a N X N board using minimax and alpha beta pruning in Java.

Input-format:
Value of n for the n x n board
Types of Fruits
Total remaining time

Scoring: You score square of the fruits you capture.
Goal:
Capture maximum fruits and print your move (Ex:B1 means second column, first row) and the next state of the board after you have made your move.
The time is the time remaining for the entire game for your player. You lose the game if you run out of time or the opponent scores more than you. The initial configuratio of the board is given where * denotes empty positions and after your move the spots captured become empty and are replaced by fruits on top of them(gravity effect).

Sample Input:
10
4
1.276
3102322310
0121232013
3021111113
0221031132
0230011012
0323321010
2003022012
2202200021
0130000020
2200022231

One possible output:
G8
31******10
010*****13
3022322*13
0221232*32
0221111*12
0331031310
2020011012
2203321121
0103022120
2232222231
