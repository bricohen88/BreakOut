package sample;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Controller {

    @FXML
    Group mainPane;
    Text mainDisplay;

    int level = 1;

    BreakOutAnimator animator;

    int lives = 3;
    Text livesText;
    SimpleIntegerProperty score = new SimpleIntegerProperty(this,"value",0);
    Text scoreText;
    int combo = 0;
    boolean play = false;
    Line aim;

    int speed = 2;
    double angle = 0.875;
    double dirX = 1;
    double dirY = -1;
    double ballX = 300;
    double ballY = 520;
    final int ballR = 10;
    Circle ball;

    int paddleX = 260;
    final int paddleL = 80;
    String friction = "none";
    Rectangle paddle;

    int[][] map;
    Map<Rectangle,Integer> brickList;
    int brickH;
    int brickW;
    int brickCol;
    int brickRow;

    public void initialize() {
        //Load and create map
        loadMap();
        createBricks();

        //Add lives and score
        livesText = new Text("Lives: " + lives);
        livesText.setX(500);
        livesText.setY(30);
        livesText.setFont(Font.font("verdana",FontWeight.BLACK, 20));
        mainPane.getChildren().add(livesText);
        scoreText = new Text();
        scoreText.setX(20);
        scoreText.setY(30);
        scoreText.textProperty().bind(Bindings.convert(score));
        scoreText.setFont(Font.font("verdana",FontWeight.BLACK, 20));
        mainPane.getChildren().add(scoreText);

        //Set paddle and ball position
        paddle = new Rectangle(paddleL,10);
        paddle.setX(paddleX);
        paddle.setY(550);
        paddle.setFill(Color.BLUE);
        mainPane.getChildren().add(paddle);
        ball = new Circle(ballR);
        ball.setCenterX(ballX);
        ball.setCenterY(ballY);
        ball.setFill(Color.BLUE);
        mainPane.getChildren().add(ball);

        //Add aiming line
        aim = new Line();
        aim.setStartX(ballX);
        aim.setStartY(ballY);
        aim.setEndX(ballX + Math.sqrt(2) * Math.sin(angle) * 30);
        aim.setEndY(ballY - Math.sqrt(2) * Math.cos(angle) * 30);
        mainPane.getChildren().add(aim);

        //Display start instructions
        mainDisplay = new Text();
        mainDisplay.setText("Press Space to Start");
        mainDisplay.setX(100);
        mainDisplay.setTextAlignment(TextAlignment.CENTER);
        mainDisplay.setWrappingWidth(400);
        mainDisplay.setY(300);
        mainDisplay.setFont(Font.font("verdana",FontWeight.BOLD,30));
        mainPane.getChildren().add(mainDisplay);

        paddle.setFocusTraversable(true);
        paddle.requestFocus();

        //Begin animator
        animator = new BreakOutAnimator();

        //Handle start game with space and handle paddle movement
        paddle.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (!play) {
                    if (keyEvent.getCode() == KeyCode.SPACE) {
                        refresh();
                        dirY = -Math.abs(dirY);
                        animator.start();
                    } else if (keyEvent.getCode() == KeyCode.LEFT) {
                        if (angle > -1.4) {
                            angle -= 0.175;
                        }
                        dirX = Math.sqrt(2) * Math.sin(angle);
                        dirY = -Math.sqrt(2) * Math.cos(angle);
                        aim.setEndX(300 + dirX * 30);
                        aim.setEndY(520 + dirY * 30);
                    } else if (keyEvent.getCode() == KeyCode.RIGHT) {
                        if (angle < 1.4) {
                            angle += 0.175;
                        }
                        dirX = Math.sqrt(2) * Math.sin(angle);
                        dirY = -Math.sqrt(2) * Math.cos(angle);
                        aim.setEndX(300 + dirX * 30);
                        aim.setEndY(520 + dirY * 30);
                    }
                } else if (keyEvent.getCode() == KeyCode.LEFT) {
                    if (paddleX > 0) {
                        paddleX -= 5;
                        friction = "left";
                    }
                } else if (keyEvent.getCode() == KeyCode.RIGHT) {
                    if (paddleX + paddleL < 600) {
                        paddleX += 5;
                        friction = "right";
                    }
                }
            }
        });

        paddle.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                friction = "none";
            }
        });

        paddle.requestFocus();

    }

    public void loadMap() {
        File file = new File("src\\sample\\levels\\level" + level + ".txt");
        System.out.println(file.getAbsolutePath());
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line = in.readLine();
            System.out.println(line);
            brickCol = Integer.parseInt(line.substring(0,1));
            System.out.println(Integer.valueOf(line.substring(0,1)));
            brickRow = Integer.parseInt(line.substring(2,3));
            System.out.println(line.charAt(2));
            map = new int[brickRow][brickCol];
            for (int i = 0; i < brickRow; i++) {
                line = in.readLine();
                String[] sArray = line.split(" ");
                for(int j = 0; j < brickCol; j++) {
                    map[i][j] = Integer.parseInt(sArray[j]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createBricks() {
        brickH = 200 / brickRow;
        brickW = 500 / brickCol;
        brickList = new HashMap<>();

        for (int i = 0; i < brickRow; i++) {
            for (int j = 0; j < brickCol; j++) {
                if (map[i][j] > 0) {
                    Rectangle rect = new Rectangle(brickW, brickH);
                    rect.setX(50 + j * brickW);
                    rect.setY(50 + i * brickH);
                    rect.setFill(setColor(map[i][j]));
                    rect.setStroke(Color.WHITE);
                    rect.setStrokeWidth(3);
                    brickList.put(rect, map[i][j]);
                    mainPane.getChildren().add(rect);
                }
            }
        }
        System.out.println(brickList.size());
    }

    class BreakOutAnimator extends AnimationTimer {

            @Override
            public void handle(long l) {
                // Move Ball
                ballX += speed * dirX;
                ballY += speed * dirY;
                ball.setCenterX(ballX);
                ball.setCenterY(ballY);

                //Update position of paddle
                paddle.setX(paddleX);

                //Check intersection boundaries
                if (ballX >= 590) {
                    dirX = -Math.abs(dirX);
                } else if (ballX <= 10) {
                    dirX = Math.abs(dirX);
                }
                if (ballY <= 10) {
                    dirY = Math.abs(dirY);
                    System.out.println("Hit ceiling " + dirY);
                }
                if (ballY > 600) {
                    //Restart
                    animator.stop();
                    play = false;
                    lives--;
                    livesText.setText("Lives: " + lives);
                    dirX = Math.sqrt(2) * Math.sin(angle);
                    dirY = Math.sqrt(2) * Math.cos(angle);
                    aim.setEndX(300 + Math.sqrt(2) * Math.sin(angle) * 30);
                    aim.setEndY(520 - Math.sqrt(2) * Math.cos(angle) * 30);
                    ball.setCenterX(300);
                    ball.setCenterY(520);
                    if (lives <= 0) {
                        level = 1;
                        loadMap();
                        lives = 3;
                        score.setValue(0);
                        System.out.println("Game Over");
                        mainDisplay.setText("Game Over\nPress Space to Restart");

                    } else {
                        mainDisplay.setText("Press Space to Continue");

                    }
                    mainPane.getChildren().add(aim);
                    mainPane.getChildren().add(mainDisplay);

                }

                //Intersect paddle and apply paddle friction to ball
                if (collide(paddle, ball)) {
                    combo = 0;
                    if ((friction.equals("left") && dirX <= 0) || (friction.equals("right") && dirX >= 0)) {
                        dirY = 0.8 * dirY;
                        dirX = Math.sqrt(2 - dirY * dirY) * dirX / Math.abs(dirX);
                    } else if (!friction.equals("none")) {
                        dirX = 0.8 * dirX;
                        dirY = Math.sqrt(2 - dirX * dirX) * dirY / Math.abs(dirY);
                    }
            } else if (ballY < 280) {

                //Check Intersections with bricks
                Rectangle rect = null;
                for (Map.Entry<Rectangle, Integer> entry : brickList.entrySet()) {

                    if (collide(entry.getKey(),ball)) {
                        score.set(score.getValue() + 10 + combo);
                        combo++;
                        // Remove rect set to 0 and remove from list
                        entry.setValue(entry.getValue() - 1);
                        entry.getKey().setFill(setColor(entry.getValue()));
                        if (entry.getValue() <= 0) {
                            mainPane.getChildren().remove(entry.getKey());
                            rect = entry.getKey();
                        }

                        break;
                    }
                }
                if (rect != null) {
                    brickList.remove(rect);
                    if(brickList.isEmpty()) {
                        File directory = new File ("src\\sample\\levels");
                        if (level >= directory.list().length) {
                            mainDisplay.setText("You beat the game\nwith a score of " + score.getValue() + "\nPress Space To Restart");
                            level = 1;
                            score.setValue(0);
                            lives = 3;
                        } else {
                            level++;
                            mainDisplay.setText("You Beat This Level\nPress Space to Continue");
                        }
                        loadMap();
                        mainPane.getChildren().add(mainDisplay);
                        mainPane.getChildren().add(aim);
                        play = false;
                        animator.stop();

                    }
                }
            }
        }
    }

    //Checks for intersection between ball and rectangle then updates direction of ball
    public boolean collide(Rectangle r, Circle c) {
        if (r.intersects(c.getBoundsInParent())) {
            if (c.getCenterY() > r.getY() + r.getHeight()) {
                dirY = Math.abs(dirY);
            } else if (c.getCenterY() -5 < r.getY()) {
                dirY = -Math.abs(dirY);
            }
            if (c.getCenterX() > r.getX() + r.getWidth()) {
                dirX = Math.abs(dirX);
            } else if (c.getCenterX() < r.getX()) {
                dirX = -Math.abs(dirX);
            }
            return true;
        }
        return false;
    }

    //Updates bricks, lives and score between deaths and games
    public void refresh() {
        if (lives <= 0) {
            for (Map.Entry<Rectangle, Integer> entry : brickList.entrySet()) {
                mainPane.getChildren().remove(entry.getKey());
            }
            brickList.clear();
        }
        if (brickList.isEmpty()) {
            createBricks();
            livesText.setText("Lives: " + lives);
        }
        mainPane.getChildren().remove(mainDisplay);
        mainPane.getChildren().remove(aim);
        paddleX = 600/2 -40;
        paddle.setX(paddleX);
        ballX = 300;
        ball.setCenterX(ballX);
        ballY = 500;
        ball.setCenterY(520);
        play = true;
        paddle.requestFocus();
    }

    public Color setColor(int i) {
        switch (i) {
            case 0:
                return Color.WHITE;
            case 1:
                return Color.VIOLET;
            case 2:
                return Color.INDIGO;
            case 3:
                return Color.BLUE;
            case 4:
                return Color.GREEN;
            case 5:
                return Color.YELLOW;
            case 6:
                return Color.ORANGE;
            default:
                return Color.RED;
        }
    }
}
