import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class CrosseyRoadFinalGame extends JFrame implements KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_WIDTH = 40;
    private static final int PLAYER_HEIGHT = 40;
    private static final int OBSTACLE_WIDTH = 40;
    private static final int OBSTACLE_HEIGHT = 30;
    private static final int PLAYER_SPEED = 10;
    private static final int PROJECTILE_SPEED = 10;
    private static final int OBSTACLE_SPEED = 4;

    private int level = 1;
    private int health = 3;
    private int score = 0;
    private boolean isGameOver = false;
    private boolean isProjectileVisible = false;
    private boolean isFiring = false;
    private int playerX = WIDTH / 2;
    private int playerY = HEIGHT - 60;
    private int projectileX, projectileY;
    private int selectedCat = 0;
    private Random rand = new Random();

    private List<Rectangle> obstacles = new ArrayList<>();
    //This is an array which holds all 3 of the cat sprite images
    private BufferedImage[] catImages = new BufferedImage[3];
    //This is a Clip array for all 3 of the meowing sounds for each cat
    private Clip[] meowingClips = new Clip[3];
    //Shows the descriptions of each cat
    private final String[] catDescriptions = {
            "Yoda: long haired with yellow and green eyes",
            "Alpha: husky, meaty and dominant",
            "Explorer: short haired, always angry and claws are always ready"
    };

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private JLabel catDescriptionLabel;
    private Timer timer;

    public CrosseyRoadFinalGame() {
        setTitle("Crossey Roads - Combined Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        //the images of cats pop up
        loadCatImages();
        //the meowing sounds of cats pop up
        loadMeowingSounds();

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGame(g);
            }
        };
        gamePanel.setLayout(null);
        add(gamePanel);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.BLACK);
        scoreLabel.setBounds(10, 10, 100, 20);
        gamePanel.add(scoreLabel);

        healthLabel = new JLabel("Health: 3");
        healthLabel.setForeground(Color.BLACK);
        healthLabel.setBounds(10, 40, 100, 20);
        gamePanel.add(healthLabel);

        //Shows you where the description for each cat is and it's color
        catDescriptionLabel = new JLabel("Choose your cat! Press UP key");
        catDescriptionLabel.setForeground(Color.BLACK);
        catDescriptionLabel.setBounds(10, 70, 400, 20);
        gamePanel.add(catDescriptionLabel);

        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        startObstacleMovement();
    }

    private void loadCatImages() {
        try {
            //Each specific cat image is brought in
            catImages[0] = ImageIO.read(new File("cat1.png"));
            catImages[1] = ImageIO.read(new File("cat2.png"));
            catImages[2] = ImageIO.read(new File("cat3.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //The method for all three cats meowing sounds to be brought in
    private void loadMeowingSounds() {
        try {
            String[] meowFiles = {"Yoda.wav", "Alpha.wav", "Explorer.wav"};
            for (int i = 0; i < meowFiles.length; i++) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(meowFiles[i]).getAbsoluteFile());
                meowingClips[i] = AudioSystem.getClip();
                meowingClips[i].open(audioStream);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void drawGame(Graphics g) {
        if (level == 1) {
            drawRoadLevel(g);
        } else if (level == 2) {
            drawTrainLevel(g);
        } else if (level == 3) {
            drawNeighborhoodLevel(g);
        }

        // Draw players which is the 3 cats 
        if (catImages[selectedCat] != null) {
            g.drawImage(catImages[selectedCat], playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);
        } else {
            g.setColor(Color.ORANGE);
            g.fillRect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        }

        // Draw obstacles
        g.setColor(Color.RED);
        for (Rectangle obstacle : obstacles) {
            g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }

        // Draw projectile
        if (isProjectileVisible) {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, 5, 10);
        }

        if (isGameOver) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Game Over!", WIDTH / 2 - 100, HEIGHT / 2);
        }
    }

    private void drawRoadLevel(Graphics g) {
        setBackground(Color.GRAY);
        g.setColor(Color.WHITE);
        for (int i = 50; i < getWidth(); i += 60) {
            g.fillRect(i, 200, 50, 10);
            g.fillRect(i, 300, 50, 10);
            g.fillRect(i, 400, 50, 10);
        }
    }

    private void drawTrainLevel(Graphics g) {
        setBackground(Color.LIGHT_GRAY);
        g.setColor(Color.DARK_GRAY);
        for (int i = 150; i < 600; i += 60) {
            g.fillRect(0, i, getWidth(), 20);
        }
    }

    private void drawNeighborhoodLevel(Graphics g) {
        setBackground(new Color(12, 196, 12));
        g.setColor(Color.YELLOW);
        g.fillRect(600, 50, 100, 100);
    }

    private void startObstacleMovement() {
        timer = new Timer(20, e -> {
            if (!isGameOver) {
                moveObstacles();
                moveProjectile();
                checkCollisions();
                gamePanel.repaint();
            }
        });
        timer.start();
        createObstacles();
    }

    private void moveObstacles() {
        int speed = (level == 2) ? 8 : 4;

        for (Rectangle obstacle : obstacles) {
            obstacle.x += speed;
            if (obstacle.x > WIDTH) {
                obstacle.x = -rand.nextInt(400);
            }
        }
    }

    private void moveProjectile() {
        if (isProjectileVisible) {
            projectileY -= PROJECTILE_SPEED;
            if (projectileY < 0) {
                isProjectileVisible = false;
            }
        }
    }

    private void createObstacles() {
        obstacles.clear();
        int count = (level == 2) ? 5 : 15;
        for (int i = 0; i < count; i++) {
            obstacles.add(new Rectangle(rand.nextInt(WIDTH), 150 + i * 50, OBSTACLE_WIDTH, OBSTACLE_HEIGHT));
        }
    }

    private void checkCollisions() {
        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        for (Rectangle obstacle : obstacles) {
            if (playerRect.intersects(obstacle)) {
                health--;
                healthLabel.setText("Health: " + health);
                resetPlayerPosition();
                if (health <= 0) {
                    isGameOver = true;
                    timer.stop();
                }
                return;
            }
        }

        if (isProjectileVisible) {
            Rectangle projectileRect = new Rectangle(projectileX, projectileY, 5, 10);
            for (int i = 0; i < obstacles.size(); i++) {
                if (projectileRect.intersects(obstacles.get(i))) {
                    obstacles.remove(i);
                    score += 10;
                    scoreLabel.setText("Score: " + score);
                    isProjectileVisible = false;
                    break;
                }
            }
        }

        if (playerY < 0) {
            level++;
            if (level > 3) {
                JOptionPane.showMessageDialog(this, "You Win!");
                System.exit(0);
            }
            resetPlayerPosition();
            createObstacles();
        }
    }

    private void resetPlayerPosition() {
        playerX = WIDTH / 2;
        playerY = HEIGHT - 60;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) playerY -= PLAYER_SPEED;
        if (key == KeyEvent.VK_A) playerX -= PLAYER_SPEED;
        if (key == KeyEvent.VK_D) playerX += PLAYER_SPEED;
        if (key == KeyEvent.VK_SPACE && !isFiring) {
            projectileX = playerX + PLAYER_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;
            isFiring = true;
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    isFiring = false;
                } catch (InterruptedException ignored) {}
            }).start();
        }
        //When you hit the UP key, you can choose a cat
        if (key == KeyEvent.VK_UP) {
            selectedCat = (selectedCat + 1) % 3;
            catDescriptionLabel.setText(catDescriptions[selectedCat]);
            //Each time you hit the UP key, you also hear the different meows of each chosen
            playMeowingSound(selectedCat);
        }
    }
    //The meowing sound method to play for the cats
    private void playMeowingSound(int catIndex) {
        if (meowingClips[catIndex] != null) {
            meowingClips[catIndex].setFramePosition(0);
            meowingClips[catIndex].start();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CrosseyRoadFinalGame().setVisible(true));
    }
}
