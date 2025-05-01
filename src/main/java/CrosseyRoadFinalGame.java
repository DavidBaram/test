/**
 * Project: Group project Team 1 - Crossey Road Game
 * Purpose Details: Using what we learned in IST 242 to create and build a Crossey Road Game that works
 * Course: IST 242: Intermediate & Object-Oriented Application Development
 * Author: David Michael Baram, Liya Aji, Yusuf I Baksh, Ulrich Kevin Commodore-Armah, Jerry Peter Alexander
 * Date Developed: 04/28/25
 * Last Date Changed: 05//25
 * Revision: 1

 */
// Import necessary libraries for GUI, event handling, multimedia, and file operations
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

/**
 * Main class for the Crossey Roads final game.
 * Implements multiple levels, player health,
 * obstacles, sound effects, and basic animation.
 */
public class CrosseyRoadFinalGame extends JFrame implements KeyListener {

    // Constants for window and object dimensions
    private static final int WIDTH = 800, HEIGHT = 600;
    private static final int PLAYER_WIDTH = 40, PLAYER_HEIGHT = 40;
    private static final int OBSTACLE_WIDTH = 40, OBSTACLE_HEIGHT = 30;
    private static final int PLAYER_SPEED = 10, PROJECTILE_SPEED = 10;

    // Game state variables
    private int level = 1;  // Current game level
    private int health = 3;  // Player's health
    private int score = 0;  // Player's score
    private boolean isGameOver = false;  // Flag to indicate if the game is over
    private boolean isProjectileVisible = false;  // Flag to indicate if the projectile is visible
    private boolean isFiring = false;  // Flag to prevent firing multiple projectiles at once

    // Player and projectile position (Starting position of the player within each level)
    private int playerX = WIDTH / 2;  // Initial player X position
    private int playerY = HEIGHT - 60;  // Initial player Y position
    private int projectileX, projectileY;  // Projectile's coordinates

    // Cat selection (Alpha, Explorer, and Yoda)
    private int selectedCat = 0;  // Tracks which cat is selected (0: Yoda, 1: Alpha, 2: Explorer)
    private Random rand = new Random();  // Random number generator for obstacle placement

    // Lists to manage dynamic game elements
    private List<Rectangle> obstacles = new ArrayList<>();  // List to store obstacles

    // Cat images and meowing sound clips
    private BufferedImage[] catImages = new BufferedImage[3];  // Array to hold cat images
    private Clip[] meowingClips = new Clip[3];  // Array to hold meowing sound clips

    //ending image for when the player wins
    private BufferedImage winImage;
    private boolean hasWon = false;

    // Shield variables for activating, starting and the duration of shield
    private boolean shieldActive = false;
    private int shieldDuration = 5000; // Shield lasts for 5 seconds
    private long shieldStartTime;

    // Cat descriptions displayed on-screen to tell the user each cat's details
    private final String[] catDescriptions = {
            "Yoda: long haired with yellow and green eyes",
            "Alpha: husky, meaty and dominant",
            "Explorer: short haired, always angry and claws are always ready"
    };

    // UI Components within the game
    private JPanel gamePanel;  // Game display panel
    private JLabel scoreLabel;  // Score label
    private JLabel healthLabel;  // Health label
    private JLabel catDescriptionLabel;  // Label to show selected cat's description
    private JLabel timerLabel;  // Timer label to show countdown
    private int remainingTime = 60;  // 60-second countdown
    private Timer countdownTimer;  // Timer for countdown

    // Timer for game loop
    private Timer timer;  // Timer to control the game loop and timing of movements

    /**
     * Constructor to set up the game window and initialize components.
     */
    public CrosseyRoadFinalGame() {
        setTitle("Crossey Roads - Final Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        loadCatImages();       // Load player sprites
        loadMeowingSounds();   // Load cat sound effects
        loadWinImage();        //loads the win image

        // Set up the game panel with custom painting
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGame(g);  // Draw everything
            }
        };

        gamePanel.setLayout(null);  // Set custom layout for the game panel
        add(gamePanel);

        // Add UI labels including score, health, and choosing character prompt.
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.BLACK);
        scoreLabel.setBounds(10, 10, 100, 20);  // Position score label
        gamePanel.add(scoreLabel);

        healthLabel = new JLabel("Health: 3");
        healthLabel.setForeground(Color.BLACK);
        healthLabel.setBounds(10, 40, 100, 20);  // Position health label
        gamePanel.add(healthLabel);

        catDescriptionLabel = new JLabel("Choose your cat! Press UP key");
        catDescriptionLabel.setForeground(Color.BLACK);
        catDescriptionLabel.setBounds(10, 70, 400, 20);  // Position cat description label
        gamePanel.add(catDescriptionLabel);
        timerLabel = new JLabel("Time: 60");
        timerLabel.setForeground(Color.BLACK);
        timerLabel.setBounds(10, 100, 100, 20);  // Position timer label
        gamePanel.add(timerLabel);


        // Enable keyboard input
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        startObstacleMovement(); // Begin game loop and spawn initial obstacles
        // Still inside the constructor, at the end, after startObstacleMovement();
        startCountdownTimer();   // Begin countdown timer
    }

    /**
     * Load images for each cat sprite.
     */
    private void loadCatImages() {
        try {
            catImages[0] = ImageIO.read(new File("cat1.png"));
            catImages[1] = ImageIO.read(new File("cat2.png"));
            catImages[2] = ImageIO.read(new File("cat3.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load timer to countdown
     */
    private void startCountdownTimer() {
        countdownTimer = new Timer(1000, e -> {
            if (!isGameOver) {
                remainingTime--;
                timerLabel.setText("Time: " + remainingTime);
                if (remainingTime <= 0) {
                    isGameOver = true;
                    timer.stop();
                    countdownTimer.stop();
                    gamePanel.repaint();
                    JOptionPane.showMessageDialog(this, "Time's up!");
                }
            }
        });
        countdownTimer.start();
    }
    /**
     * Load sound clips for each cat's meow.
     */
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
    
    /**
     * Load the winner image after the player wins.
     */
    private void loadWinImage() {
        try {
            winImage = ImageIO.read(new File("winimage.png")); // Replace with your actual image file name
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main game rendering function that handles drawing all game components.
     */
    private void drawGame(Graphics g) {
        //draws out the winning image if the player wins
        if (hasWon) {
            int imgWidth = 400;
            int imgHeight = 300;
            int x = WIDTH / 2 - imgWidth / 2;
            int y = HEIGHT / 2 - imgHeight / 2;

            // Draw the image first
            if (winImage != null) {
                g.drawImage(winImage, x, y, imgWidth, imgHeight, null);
            }

            // Then draw the "You Win!" text on top
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            String winText = "You Win!";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(winText);
            int textX = WIDTH / 2 - textWidth / 2;
            int textY = y + imgHeight + 50;  // Position below the image

            g.drawString(winText, textX, textY);

            return;
        }
        
        if (level == 1) drawRoadLevel(g);  // Draw road level
        else if (level == 2) drawTrainLevel(g);  // Draw train level
        else if (level == 3) drawNeighborhoodLevel(g);  // Draw neighborhood level

        // Draw the selected cat at the player's position
        if (catImages[selectedCat] != null) {
            g.drawImage(catImages[selectedCat], playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);
        } else {
            g.setColor(Color.ORANGE);  // Default fallback color if no image is loaded
            g.fillRect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);  // Draw a rectangle
        }

        // Draw all obstacles
        g.setColor(Color.RED);
        for (Rectangle obstacle : obstacles) {
            g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }

        // Draw the projectile if visible
        if (isProjectileVisible) {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, 5, 10);  // Draw the projectile
        }
        //Draws the semi-transparent shield around the player
        if (isShieldActive()) {
            g.setColor(new Color(0, 255, 255, 100)); // Semi-transparent cyan
            g.fillOval(playerX - 10, playerY - 10, PLAYER_WIDTH + 20, PLAYER_HEIGHT + 20); // Glow around the player
        }

        // Display Game Over message if the game is over
        if (isGameOver) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Game Over!", WIDTH / 2 - 100, HEIGHT / 2);
        }
    }

    /**
     * Draw the road level background and obstacles.
     */
    private void drawRoadLevel(Graphics g) {
        gamePanel.setBackground(Color.GRAY);  // Set background color for road level
        g.setColor(Color.WHITE);  // Set obstacle color
        for (int i = 50; i < getWidth(); i += 60) {
            g.fillRect(i, 200, 50, 10);  // Draw road obstacles
            g.fillRect(i, 300, 50, 10);
            g.fillRect(i, 400, 50, 10);
        }
    }

    /**
     * Draw the train level background and obstacles.
     */
    private void drawTrainLevel(Graphics g) {
        gamePanel.setBackground(Color.LIGHT_GRAY);  // Set background color for train level
        g.setColor(Color.DARK_GRAY);  // Set obstacle color
        for (int i = 150; i < 600; i += 60) {
            g.fillRect(0, i, getWidth(), 20);  // Draw train track obstacles
        }
    }

    /**
     * Draw the neighborhood level background and obstacles.
     */
    private void drawNeighborhoodLevel(Graphics g) {
        gamePanel.setBackground(new Color(12, 196, 12));  // Set background color for neighborhood level
        g.setColor(Color.YELLOW);  // Set obstacle color
        g.fillRect(600, 50, 100, 100);  // Draw a neighborhood house
    }

    /**
     * Starts the timer for the game loop to handle movement and game state updates.
     */
    private void startObstacleMovement() {
        timer = new Timer(20, e -> {
            if (!isGameOver) {
                moveObstacles();
                moveProjectile();
                checkCollisions();

                // Auto-deactivate shield
                if (shieldActive && (System.currentTimeMillis() - shieldStartTime) >= shieldDuration) {
                    deactivateShield();
                }

                gamePanel.repaint();
            }
        });
        timer.start();  // Start the game loop
        createObstacles();  // Create initial obstacles
    }

    /**
     * Moves obstacles across the screen based on game level.
     */
    private void moveObstacles() {
        int speed = (level == 2) ? 6 : 4;  // Set speed based on level
        for (Rectangle obstacle : obstacles) {
            obstacle.x += speed;  // Move each obstacle
            if (obstacle.x > WIDTH) {  // Reset obstacle position if it moves off-screen
                obstacle.x = -rand.nextInt(400);
            }
        }
    }

    /**
     * Moves the projectile upward when fired.
     */
    private void moveProjectile() {
        if (isProjectileVisible) {
            projectileY -= PROJECTILE_SPEED;  // Move the projectile up
            if (projectileY < 0) isProjectileVisible = false;  // Hide projectile when it moves off-screen
        }
    }
    /**
     * method to activate the shield
     */
    private void activateShield() {
        shieldActive = true;
        shieldStartTime = System.currentTimeMillis();
    }
    /**
     * method to deactivate the shield
     */
    private void deactivateShield() {
        shieldActive = false;
    }
    /**
     * method to check whether the shield is active
     */
    private boolean isShieldActive() {
        return shieldActive && (System.currentTimeMillis() - shieldStartTime) < shieldDuration;
    }

    /**
     * Creates new obstacles at random positions.
     */
    private void createObstacles() {
        obstacles.clear();  // Clear existing obstacles
        if (level == 2) {  // Train level
            int[] trackY = {150, 210, 270, 330, 390, 450, 510};  // Y positions for obstacles
            for (int i = 0; i < 5; i++) {
                int y = trackY[rand.nextInt(trackY.length)];  // Randomize obstacle Y position
                int x = -rand.nextInt(WIDTH);  // Randomize obstacle X position
                obstacles.add(new Rectangle(x, y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT));  // Add obstacle
            }
        } else {  // Other levels
            for (int i = 0; i < 15; i++) {
                int x = rand.nextInt(WIDTH);  // Random X position
                int y = 150 + rand.nextInt(300);  // Random Y position
                obstacles.add(new Rectangle(x, y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT));  // Add obstacle
            }
        }
    }

    /**
     * Checks for collisions between player, obstacles, and projectiles.
     */
    private void checkCollisions() {
        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);  // Player's rectangle

        // Check for player collisions with obstacles
        for (Rectangle obstacle : obstacles) {
            if (playerRect.intersects(obstacle)) {
                if (isShieldActive()) {
                    // Shield blocks the collision
                    return;
                } else {
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

        }

        // Check for collisions between projectile and obstacles
        if (isProjectileVisible) {
            Rectangle projectileRect = new Rectangle(projectileX, projectileY, 5, 10);  // Projectile's rectangle
            for (int i = 0; i < obstacles.size(); i++) {
                if (projectileRect.intersects(obstacles.get(i))) {  // Check if projectile hits obstacle
                    obstacles.remove(i);  // Remove obstacle
                    score += 10;  // Increase score
                    scoreLabel.setText("Score: " + score);  // Update score label
                    isProjectileVisible = false;  // Hide the projectile
                    break;
                }
            }
        }

         // Check if player has reached the top of the screen and level up
        if (playerY < 0) {
            level++;  // Increase level
            if (level > 3) {
                hasWon = true;
                timer.stop();
                countdownTimer.stop();  // Stop the countdown too
                gamePanel.repaint();  // Trigger repaint so the win image shows
                return;
            }

            resetPlayerPosition();  // Reset player position
            createObstacles();  // Create new obstacles for the next level
        }
    }

    /**
     * Resets the player's position to the starting point.
     */
    private void resetPlayerPosition() {
        playerX = WIDTH / 2;
        playerY = HEIGHT - 60;
    }

    // KeyListener methods to handle player input
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W && playerY > -10) playerY -= PLAYER_SPEED;  // Move up
        if (key == KeyEvent.VK_A && playerX > 0) playerX -= PLAYER_SPEED;  // Move left
        if (key == KeyEvent.VK_D && playerX + PLAYER_WIDTH < WIDTH) playerX += PLAYER_SPEED;  // Move right
        if (key == KeyEvent.VK_S) {
            activateShield();
        }


        // Fire projectile if the spacebar is pressed
        if (key == KeyEvent.VK_SPACE && !isFiring) {
            projectileX = playerX + PLAYER_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;  // Show projectile
            isFiring = true;  // Set firing flag to true
            new Thread(() -> {
                try {
                    Thread.sleep(500);  // Prevent multiple firings
                    isFiring = false;  // Reset firing flag after half a second
                } catch (InterruptedException ignored) {}
            }).start();
        }

        // Cycle through cats when the UP key is pressed
        if (key == KeyEvent.VK_UP) {
            selectedCat = (selectedCat + 1) % 3;  // Cycle cat selection
            catDescriptionLabel.setText(catDescriptions[selectedCat]);  // Update cat description label
            playMeowingSound(selectedCat);  // Play selected cat's meow sound
        }
    }

    /**
     * Play the selected cat's meowing sound.
     */
    private void playMeowingSound(int catIndex) {
        if (meowingClips[catIndex] != null) {
            meowingClips[catIndex].setFramePosition(0);  // Reset sound to start
            meowingClips[catIndex].start();  // Play the meow
        }
    }

    @Override public void keyReleased(KeyEvent e) {}  // Not used
    @Override public void keyTyped(KeyEvent e) {}  // Not used

    /**
     * Main method to start the game.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CrosseyRoadFinalGame().setVisible(true));
    }
}
