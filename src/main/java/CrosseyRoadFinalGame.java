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
    private static int PLAYER_SPEED = 10;
    private static final int PROJECTILE_SPEED = 10;

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
    
    //Car-crashing/collision clip
    private Clip carCrashClip;
    //Train clip for level 2
    private Clip trainClip;
    //Highway traffic clip for level 1
    private Clip highwayTrafficClip;

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

    private BufferedImage roadBackground, trainBackground, neighborhoodBackground;

    // Powerups(health and speedboost)
    private BufferedImage healthPowerUpImage;
    private BufferedImage speedBoostImage;
    private List<PowerUp> powerUps = new ArrayList<>();

    // Speedboost Powerup
    private int speedBoostTimeLeft = 5; // 5 seconds countdown
    private boolean showSpeedBoostTimer = false; // Tracks when to display timer
    private long speedBoostStartTime;

    // Health regeneration message
    private String healthMessage = ""; // Empty by default
    private long healthMessageStartTime; // Time when message appears
    private static final int HEALTH_MESSAGE_DURATION = 2000; // Show for 2 seconds


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
        loadBackgroundImages(); //Ensure background images are loaded
        loadCrashSound();       //loads the car crash sound
        loadTrainSound();       //loads train sound for level 2
        loadHighwayTrafficSound(); //loads highway traffic sound for level 1
        loadPowerUpImage();       // Loads health powerup image
        loadSpeedBoostImage();     // Loads speedboosts powerup image

        // Set up the game panel with custom painting
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGame(g);  // Draw everything
                // Draws health powerup
                for (PowerUp powerUp : powerUps) {
                    if (healthPowerUpImage != null) {
                        g.drawImage(healthPowerUpImage, powerUp.x, powerUp.y, PowerUp.SIZE, PowerUp.SIZE, null);

                    }
                    // Draws speedboosts
                    else if (powerUp.type.equals("speed") && speedBoostImage != null) {
                        g.drawImage(speedBoostImage, powerUp.x, powerUp.y, PowerUp.SIZE, PowerUp.SIZE, null);
                    }
                    if (showSpeedBoostTimer) {
                        g.setColor(Color.RED);
                        g.setFont(new Font("Arial", Font.BOLD, 20));
                        g.drawString("Speed Boost: " + speedBoostTimeLeft + "s", WIDTH - -100 , 20);
                    }
                    if (!healthMessage.isEmpty() && System.currentTimeMillis() - healthMessageStartTime < HEALTH_MESSAGE_DURATION) {
                        g.setColor(Color.GREEN);
                        g.setFont(new Font("Arial", Font.BOLD, 15));
                        int messageX = (800 / 2) - 60; // Centered in window width
                        int messageY = 50; // Positioned near the top
                        g.drawString(healthMessage, messageX, messageY);

                    } else {
                        healthMessage = ""; // Clear message when time expires
                    }

                }
            }
        };

        gamePanel.setLayout(null);  // Set custom layout for the game panel
        add(gamePanel);

        // Add UI labels including score, health, and choosing character prompt.
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setBounds(10, 10, 100, 20);  // Position score label
        gamePanel.add(scoreLabel);

        healthLabel = new JLabel("Health: 3");
        healthLabel.setForeground(Color.WHITE);
        healthLabel.setBounds(10, 40, 100, 20);  // Position health label
        gamePanel.add(healthLabel);

        catDescriptionLabel = new JLabel("Choose your cat! Press UP key");
        catDescriptionLabel.setForeground(Color.WHITE);
        catDescriptionLabel.setBounds(10, 70, 400, 20);  // Position cat description label
        gamePanel.add(catDescriptionLabel);
        timerLabel = new JLabel("Time: 60");
        timerLabel.setForeground(Color.WHITE);
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
            String[] meowFiles = {"Yoda2.0.wav", "Alpha2.0.wav", "Explorer2.0.wav"};
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
     * Load car crash sounds for collision with level 1.
     */
    private void loadCrashSound() {
        try {
            AudioInputStream crashStream = AudioSystem.getAudioInputStream(new File("carcrashing.wav").getAbsoluteFile());
            carCrashClip = AudioSystem.getClip();
            carCrashClip.open(crashStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Load the train sound after the entering level 2 .
     */
    private void loadTrainSound() {
        try {
            AudioInputStream trainStream = AudioSystem.getAudioInputStream(new File("train.wav").getAbsoluteFile());
            trainClip = AudioSystem.getClip();
            trainClip.open(trainStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Load the highway traffic sound after the entering level 1 .
     */
    private void loadHighwayTrafficSound() {
        try {
            AudioInputStream highwayStream = AudioSystem.getAudioInputStream(new File("highwaytraffic2.0.wav").getAbsoluteFile());
            highwayTrafficClip = AudioSystem.getClip();
            highwayTrafficClip.open(highwayStream);
        } catch (Exception e) {
            e.printStackTrace();
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
    private void loadBackgroundImages() {
        try {
            roadBackground = ImageIO.read(new File("road_background.jpg"));
            trainBackground = ImageIO.read(new File("train_background.jpg"));
            neighborhoodBackground = ImageIO.read(new File("neighborhood_background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawGame(Graphics g) {
        if (hasWon) {
            int imgWidth = 400;
            int imgHeight = 300;
            int x = WIDTH / 2 - imgWidth / 2;
            int y = HEIGHT / 2 - imgHeight / 2;


            if (winImage != null) {
                g.drawImage(winImage, x, y, imgWidth, imgHeight, null);
            }

            // Then draw the "You Win!" text on top
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            String winText = "WELCOME HOME KITTY!";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(winText);
            int textX = WIDTH / 2 - textWidth / 2;
            int textY = y + imgHeight + 50;  // Position below the image

            g.drawString(winText, textX, textY);

            return;
        }

        if (level == 1) {
            g.drawImage(roadBackground, 0, 0, WIDTH, HEIGHT, null);
            drawRoadLevel(g);
        } else if (level == 2) {
            g.drawImage(trainBackground, 0, 0, WIDTH, HEIGHT, null);
            drawTrainLevel(g);
        } else if (level == 3) {
            g.drawImage(neighborhoodBackground, 0, 0, WIDTH, HEIGHT, null);
            drawNeighborhoodLevel(g);
        }

        if (catImages[selectedCat] != null) {
            g.drawImage(catImages[selectedCat], playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);
        } else {
            g.setColor(Color.ORANGE);
            g.fillRect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        }

        g.setColor(Color.RED);
        for (Rectangle obstacle : obstacles) {
            g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }

        if (isProjectileVisible) {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, 5, 10);
        }

        if (isShieldActive()) {
            g.setColor(new Color(0, 255, 255, 100));
            g.fillOval(playerX - 10, playerY - 10, PLAYER_WIDTH + 20, PLAYER_HEIGHT + 20);
        }

        if (isGameOver) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Game Over!", WIDTH / 2 - 100, HEIGHT / 2);
        }
    }

    private void drawRoadLevel(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        int laneHeight = 60;
        int laneCount = 7;
        int startY = 120;

        for (int i = 0; i < laneCount; i++) {
            int y = startY + i * laneHeight;

            // Translucent dark gray lane
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, y, getWidth(), laneHeight);

            // Solid white dashed center lines
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2d.setColor(Color.WHITE);
            for (int x = 0; x < getWidth(); x += 40) {
                g2d.fillRect(x, y + laneHeight / 2 - 2, 20, 4);
            }
        }

        g2d.dispose();
    }

    private void drawTrainLevel(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        int laneHeight = 60;
        int laneCount = 7;
        int startY = 120;

        for (int i = 0; i < laneCount; i++) {
            int y = startY + i * laneHeight;

            // Translucent dark gray lane
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, y, getWidth(), laneHeight);

            // Solid white dashed center lines
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2d.setColor(Color.WHITE);
            for (int x = 0; x < getWidth(); x += 40) {
                g2d.fillRect(x, y + laneHeight / 2 - 2, 20, 4);
            }
        }

        g2d.dispose();
    }

    private void drawNeighborhoodLevel(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        int laneHeight = 60;
        int laneCount = 7;
        int startY = 120;

        for (int i = 0; i < laneCount; i++) {
            int y = startY + i * laneHeight;

            // Translucent dark gray lane
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, y, getWidth(), laneHeight);

            // Solid white dashed center lines
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2d.setColor(Color.WHITE);
            for (int x = 0; x < getWidth(); x += 40) {
                g2d.fillRect(x, y + laneHeight / 2 - 2, 20, 4);
            }
        }

        g2d.dispose();
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
        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        // Play highway traffic sound during level 1
        if (level == 1 && highwayTrafficClip != null && !highwayTrafficClip.isRunning()) {
            highwayTrafficClip.setFramePosition(0);
            highwayTrafficClip.loop(Clip.LOOP_CONTINUOUSLY);
        }

        // Stop highway traffic sound when moving to level 2 or beyond
        if (level > 1 && highwayTrafficClip != null && highwayTrafficClip.isRunning()) {
            highwayTrafficClip.stop();
        }

        // Detect when player collects a power-up
        for (int i = 0; i < powerUps.size(); i++) {
            Rectangle powerUpRect = new Rectangle(powerUps.get(i).x, powerUps.get(i).y, PowerUp.SIZE, PowerUp.SIZE);


                if (playerRect.intersects(powerUpRect)) {
                    // If player collects the health power-up, increase health
                    if (powerUps.get(i).type.equals("health")) {
                        health = Math.min(health + 1, 3); // Ensure health doesn't exceed max
                        healthLabel.setText("Health: " + health);

                        // Show health message
                        healthMessage = "Health Restored!";
                        healthMessageStartTime = System.currentTimeMillis();



                } else if (powerUps.get(i).type.equals("speed")) {
                        PLAYER_SPEED *= 2; // Double speed
                        showSpeedBoostTimer = true; // Ensure timer is visible
                        speedBoostTimeLeft = 5; // Start at 5 seconds
                        speedBoostStartTime = System.currentTimeMillis(); // Track time

                        // Count down and repaint UI each second
                        Timer countdownTimer = new Timer(1000, new ActionListener() {
                            int countdown = 5;
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                speedBoostTimeLeft = countdown--;
                                gamePanel.repaint(); // Ensure screen updates

                                // Stop timer when it reaches 0
                                if (countdown < 0) {
                                    ((Timer) e.getSource()).stop(); // Stop countdown
                                    PLAYER_SPEED /= 2; // Reset speed
                                    showSpeedBoostTimer = false; // Hide timer
                                    gamePanel.repaint(); // Final refresh
                                }
                            }
                        });
                        countdownTimer.start(); // Start the countdown
                    }
                    powerUps.remove(i); // Remove collected power-up
                    break;
                }
        }

        // Check for player collisions with obstacles
        for (Rectangle obstacle : obstacles) {
            if (playerRect.intersects(obstacle)) {
                if (isShieldActive()) {
                    return;
                } else {
                    health--;
                    healthLabel.setText("Health: " + health);

                    // Play crash sound
                    if (carCrashClip != null) {
                        carCrashClip.setFramePosition(0);
                        carCrashClip.start();
                    }

                    resetPlayerPosition();
                    if (health <= 0) {
                        isGameOver = true;
                        timer.stop();
                    }
                    return;
                }
            }
        }

        // Check for projectile hitting obstacles
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

        // Check if player leveled up
        if (playerY < 0) {
            level++;

            // Stop highway traffic sound when leaving level 1
            if (level == 2 && highwayTrafficClip != null) {
                highwayTrafficClip.stop();
            }

            // Start train sound in level 2
            if (level == 2 && trainClip != null) {
                trainClip.setFramePosition(0);
                trainClip.loop(Clip.LOOP_CONTINUOUSLY);
            }

            // Stop train sound in level 3
            if (level == 3 && trainClip != null) {
                trainClip.stop();
            }

            // Game win condition
            if (level > 3) {
                hasWon = true;
                timer.stop();
                countdownTimer.stop();
                gamePanel.repaint();
                return;
            }

            resetPlayerPosition();
            createObstacles();
            createPowerUps();
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

    // Health Power up class
    class PowerUp {
        int x, y; // Position
        static final int SIZE = 30; // Size of power-up
        String type; // Type of power-up (e.g., "shield", "score boost")

        public PowerUp(int x, int y, String type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }
    private void loadPowerUpImage() {
        try {
            healthPowerUpImage = ImageIO.read(new File("fish_treat.png")); // Replace with actual image file name
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void createPowerUps() {
        powerUps.clear(); // Remove previous power-ups
        int minX = 100;  // Minimum X position
        int maxX = 600;  // Maximum X position
        int minY = 300;  // Minimum Y position
        int maxY = 500;  // Maximum Y position


        // Generate health-restoring power-ups at random positions
        for (int i = 0; i < 2; i++) {  // Spawning two health power-ups per level
            int x = rand.nextInt(WIDTH - PowerUp.SIZE);
            int y = rand.nextInt(HEIGHT - PowerUp.SIZE);
            powerUps.add(new PowerUp(x, y, "health")); // Set type to "health"
            powerUps.add(new PowerUp(x, y, "speed"));

        }
    }
    // Loads speedboost image
    private void loadSpeedBoostImage() {
        try {
            speedBoostImage = ImageIO.read(new File("fish_treat.png")); // Replace with actual image file name
        } catch (IOException e) {
            e.printStackTrace();
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
