import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable{

    static final int GAME_WIDTH = 1000;
    static final int GAME_HEIGHT = (int)(GAME_WIDTH * (0.5555));
    static final Dimension SCREEN_SIZE = new Dimension(GAME_WIDTH,GAME_HEIGHT);
    static final int BALL_DIAMETER = 20;
    static final int PADDLE_WIDTH = 25;
    static final int PADDLE_HEIGHT = 100;
    static final int WINNING_SCORE = 10;
    Thread gameThread;
    Image image;
    Graphics graphics;
    Random random;
    Paddle paddle1;
    Paddle paddle2;
    Ball ball;
    Score score;
    boolean gameStarted = false;
    boolean gameOver = false;
    int currentLevel = 1;
    Rectangle playButton;
    Rectangle nextLevelButton;
    Font titleFont;
    Font buttonFont;

    GamePanel(){
        newPaddles();
        newBall();
        score = new Score(GAME_WIDTH,GAME_HEIGHT);
        this.setFocusable(true);
        this.addKeyListener(new AL());
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!gameStarted && playButton.contains(e.getPoint())) {
                    gameStarted = true;
                    gameThread = new Thread(GamePanel.this);
                    gameThread.start();
                }
                if (gameOver && nextLevelButton.contains(e.getPoint())) {
                    gameOver = false;
                    currentLevel = 2;
                    score.player1 = 0;
                    score.player2 = 0;
                    newPaddles();
                    newBall();
                    gameThread = new Thread(GamePanel.this);
                    gameThread.start();
                }
            }
        });
        this.setPreferredSize(SCREEN_SIZE);
        
        // Inicializace fontů
        titleFont = new Font("Arial", Font.BOLD, 60);
        buttonFont = new Font("Arial", Font.BOLD, 30);
        
        // Vytvoření tlačítek
        int buttonWidth = 200;
        int buttonHeight = 60;
        playButton = new Rectangle(GAME_WIDTH/2 - buttonWidth/2, GAME_HEIGHT/2, buttonWidth, buttonHeight);
        nextLevelButton = new Rectangle(GAME_WIDTH/2 - buttonWidth/2, GAME_HEIGHT/2 + 80, buttonWidth, buttonHeight);
    }

    public void newBall() {
        random = new Random();
        ball = new Ball((GAME_WIDTH/2)-(BALL_DIAMETER/2),random.nextInt(GAME_HEIGHT-BALL_DIAMETER),BALL_DIAMETER,BALL_DIAMETER);
        // Nastavení rychlosti podle levelu
        if (currentLevel == 1) {
            ball.setXDirection(2);
            ball.setYDirection(2);
        } else {
            ball.setXDirection(3);
            ball.setYDirection(3);
        }
    }
    public void newPaddles() {
        paddle1 = new Paddle(0,(GAME_HEIGHT/2)-(PADDLE_HEIGHT/2),PADDLE_WIDTH,PADDLE_HEIGHT,1);
        paddle2 = new Paddle(GAME_WIDTH-PADDLE_WIDTH,(GAME_HEIGHT/2)-(PADDLE_HEIGHT/2),PADDLE_WIDTH,PADDLE_HEIGHT,2);
    }
    public void paint(Graphics g) {
        image = createImage(getWidth(),getHeight());
        graphics = image.getGraphics();
        draw(graphics);
        g.drawImage(image,0,0,this);
    }
    public void draw(Graphics g) {
        FontMetrics buttonMetrics = g.getFontMetrics(buttonFont);
        
        if (!gameStarted) {
            // Vykreslení menu
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
            
            // Vykreslení názvu Pong
            g.setColor(Color.WHITE);
            g.setFont(titleFont);
            FontMetrics titleMetrics = g.getFontMetrics(titleFont);
            String title = "PONG";
            g.drawString(title, GAME_WIDTH/2 - titleMetrics.stringWidth(title)/2, GAME_HEIGHT/3);
            
            // Vykreslení tlačítka Play
            g.setColor(Color.WHITE);
            g.fillRect(playButton.x, playButton.y, playButton.width, playButton.height);
            g.setColor(Color.BLACK);
            g.setFont(buttonFont);
            String playText = "PLAY";
            g.drawString(playText, 
                playButton.x + (playButton.width - buttonMetrics.stringWidth(playText))/2,
                playButton.y + (playButton.height + buttonMetrics.getAscent())/2);
        } else if (gameOver) {
            // Vykreslení obrazovky konce hry
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
            
            g.setColor(Color.WHITE);
            g.setFont(titleFont);
            FontMetrics titleMetrics = g.getFontMetrics(titleFont);
            String winner = score.player1 >= WINNING_SCORE ? "Player 1 Wins!" : "Player 2 Wins!";
            g.drawString(winner, GAME_WIDTH/2 - titleMetrics.stringWidth(winner)/2, GAME_HEIGHT/3);
            
            if (currentLevel == 1) {
                // Vykreslení tlačítka pro další level
                g.setColor(Color.WHITE);
                g.fillRect(nextLevelButton.x, nextLevelButton.y, nextLevelButton.width, nextLevelButton.height);
                g.setColor(Color.BLACK);
                g.setFont(buttonFont);
                String nextLevelText = "Next Level";
                g.drawString(nextLevelText, 
                    nextLevelButton.x + (nextLevelButton.width - buttonMetrics.stringWidth(nextLevelText))/2,
                    nextLevelButton.y + (nextLevelButton.height + buttonMetrics.getAscent())/2);
            }
        } else {
            // Vykreslení hry
            paddle1.draw(g);
            paddle2.draw(g);
            ball.draw(g);
            score.draw(g);
            
            // Vykreslení aktuálního levelu
            g.setColor(Color.WHITE);
            g.setFont(buttonFont);
            String levelText = "Level: " + currentLevel;
            g.drawString(levelText, 20, 40);
        }
        Toolkit.getDefaultToolkit().sync();
    }
    public void move() {
        paddle1.move();
        paddle2.move();
        ball.move();
    }
    public void checkCollision() {

        //bounce ball off top & bottom window edges
        if(ball.y <=0) {
            ball.setYDirection(-ball.yVelocity);
        }
        if(ball.y >= GAME_HEIGHT-BALL_DIAMETER) {
            ball.setYDirection(-ball.yVelocity);
        }
        //bounce ball off paddles
        if(ball.intersects(paddle1)) {
            ball.xVelocity = Math.abs(ball.xVelocity);
            ball.xVelocity++; //optional for more difficulty
            if(ball.yVelocity>0)
                ball.yVelocity++; //optional for more difficulty
            else
                ball.yVelocity--;
            ball.setXDirection(ball.xVelocity);
            ball.setYDirection(ball.yVelocity);
        }
        if(ball.intersects(paddle2)) {
            ball.xVelocity = Math.abs(ball.xVelocity);
            ball.xVelocity++; //optional for more difficulty
            if(ball.yVelocity>0)
                ball.yVelocity++; //optional for more difficulty
            else
                ball.yVelocity--;
            ball.setXDirection(-ball.xVelocity);
            ball.setYDirection(ball.yVelocity);
        }
        //stops paddles at window edges
        if(paddle1.y<=0)
            paddle1.y=0;
        if(paddle1.y >= (GAME_HEIGHT-PADDLE_HEIGHT))
            paddle1.y = GAME_HEIGHT-PADDLE_HEIGHT;
        if(paddle2.y<=0)
            paddle2.y=0;
        if(paddle2.y >= (GAME_HEIGHT-PADDLE_HEIGHT))
            paddle2.y = GAME_HEIGHT-PADDLE_HEIGHT;
        //give a player 1 point and creates new paddles & ball
        if(ball.x <=0) {
            score.player2++;
            if (score.player2 >= WINNING_SCORE) {
                gameOver = true;
                return;
            }
            newPaddles();
            newBall();
            System.out.println("Player 2: "+score.player2);
        }
        if(ball.x >= GAME_WIDTH-BALL_DIAMETER) {
            score.player1++;
            if (score.player1 >= WINNING_SCORE) {
                gameOver = true;
                return;
            }
            newPaddles();
            newBall();
            System.out.println("Player 1: "+score.player1);
        }
    }
    public void run() {
        if (!gameStarted) return;
        
        //game loop
        long lastTime = System.nanoTime();
        double amountOfTicks =60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        while(true) {
            long now = System.nanoTime();
            delta += (now -lastTime)/ns;
            lastTime = now;
            if(delta >=1) {
                move();
                checkCollision();
                repaint();
                delta--;
            }
        }
    }
    public class AL extends KeyAdapter{
        public void keyPressed(KeyEvent e) {
            paddle1.keyPressed(e);
            paddle2.keyPressed(e);
        }
        public void keyReleased(KeyEvent e) {
            paddle1.keyReleased(e);
            paddle2.keyReleased(e);
        }
    }
}