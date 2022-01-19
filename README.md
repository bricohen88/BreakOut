# BrickBreaker
Classic version of brickbreaker game using Java

All the code to operate the game is contained in the controller.java and the front end is in sample.fxml

Map is loaded from the levels folder which currently contains 3 levels. Each level file starts with 2 digits containing the number of columns and rows and then a series of integers for the strength of each brick. Custom levels can be added easily name needs to match format with level then an incremented number.

The game increments moving using a custom AnimationTimer class to move ball and paddle. After each movement ball is checked for collision against paddle and all 4 walls. Then checked against the bricks. Ball can bounce off the paddle and bricks in 8 directions. If ball goes below bottom lose a life and try again. If ball hits a brick the loses a strength until it disappears. When no bricks are left you go to the next level. If no more files exist you won the game.
