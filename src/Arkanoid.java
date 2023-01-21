import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class Arkanoid extends JPanel implements Runnable, KeyListener {
    private static final int WIDTH = 1280; // WIDTH is equal to width of background image
    private static final int HEIGHT = 720; // HEIGHT is equal to height of background image

    private static final double UPS = 60.0D;
    private static final double FPS = 120.0D;

    private static final boolean[] KEYS = new boolean[65535];

    private static final BufferedImage[] BLOCKS = new BufferedImage[8];
    private static final BufferedImage BLOCKS_ATLAS = Arkanoid.loadImage("/blocks.png");
    private static final BufferedImage BALL = Arkanoid.loadImage("/ball.png");
    private static final BufferedImage PADDLE = Arkanoid.loadImage("/paddle.png");
    private static final BufferedImage BACKGROUND = Arkanoid.loadImage("/background.png");

    private static final int PADDLE_WIDTH = Arkanoid.PADDLE.getWidth();
    private static final int PADDLE_HEIGHT = Arkanoid.PADDLE.getHeight();
    private static final int PADDLE_START_X = (Arkanoid.WIDTH / 2) - (Arkanoid.PADDLE_WIDTH / 2);
    private static final int PADDLE_HEIGHT_OFFSET = 28;
    private static final int PADDLE_SPEED = 7;

    private static final int BALL_SIZE = Arkanoid.BALL.getHeight();
    private static final int BALL_START_X = (Arkanoid.WIDTH / 2) - (Arkanoid.BALL_SIZE / 2);
    private static final int BALL_START_Y = Arkanoid.HEIGHT - Arkanoid.PADDLE_HEIGHT_OFFSET - Arkanoid.BALL_SIZE;
    private static int BALL_SPEED_X = 4;
    private static int BALL_SPEED_Y = 4;

    private static final int BLOCK_WIDTH = 61;
    private static final int BLOCK_HEIGHT = 28;
    private static final int BLOCKS_IN_WIDTH = 18;
    private static final int BLOCKS_IN_HEIGHT = 10;
    private static final int BLOCKS_OFFSET_X = (Arkanoid.WIDTH / 2) - ((Arkanoid.BLOCKS_IN_WIDTH * Arkanoid.BLOCK_WIDTH) / 2);
    private static final int BLOCKS_OFFSET_Y = Arkanoid.BLOCK_WIDTH;
    private static final Block[] BLOCKS_ARR = new Block[Arkanoid.BLOCKS_IN_WIDTH * Arkanoid.BLOCKS_IN_HEIGHT];

    private final Rectangle paddle;
    private final Rectangle ball;

    private boolean gameStarted;

    public Arkanoid() {
        super(true);

        this.setPreferredSize(new Dimension(Arkanoid.WIDTH, Arkanoid.HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this);

        Random random = new Random();

        for(int i = 0; i < 8; i++) {
            Arkanoid.BLOCKS[i] = Arkanoid.BLOCKS_ATLAS.getSubimage(i * Arkanoid.BLOCK_WIDTH, 0,
                    Arkanoid.BLOCK_WIDTH, Arkanoid.BLOCK_HEIGHT);
        }

        for(int i = 0; i < Arkanoid.BLOCKS_ARR.length; i++) {
            Arkanoid.BLOCKS_ARR[i] = new Block(Arkanoid.BLOCKS[random.nextInt(Arkanoid.BLOCKS.length)], null);
        }
        this.placeBlocks();


        this.paddle = new Rectangle((Arkanoid.WIDTH / 2) - (Arkanoid.PADDLE_WIDTH / 2),
                Arkanoid.HEIGHT - Arkanoid.PADDLE_HEIGHT_OFFSET, Arkanoid.PADDLE_WIDTH, Arkanoid.PADDLE_HEIGHT);
        this.ball = new Rectangle(Arkanoid.BALL_START_X, Arkanoid.BALL_START_Y, Arkanoid.BALL_SIZE, Arkanoid.BALL_SIZE);
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double timeU = 1000000000 / Arkanoid.UPS;
        final double timeF = 1000000000 / Arkanoid.FPS;
        double deltaU = 0, deltaF = 0;
        int ticks = 0, frames = 0;
        long timer = System.currentTimeMillis();

        while(true) {
            long currentTime = System.nanoTime();
            deltaU += (currentTime - lastTime) / timeU;
            deltaF += (currentTime - lastTime) / timeF;
            lastTime = currentTime;

            if(deltaU >= 1) {
                this.update();
                ticks++;
                deltaU--;
            }

            if(deltaF >= 1) {
                this.repaint(0, 0, Arkanoid.WIDTH, Arkanoid.HEIGHT);
                frames++;
                deltaF--;
            }

            if(System.currentTimeMillis() - timer > 1000) {
                System.out.println("UPS: " + ticks + ", FPS: " + frames);
                ticks = 0;
                frames = 0;
                timer += 1000;
            }

            // This prevents game from eating cpu
            try {
                Thread.sleep(2L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Arkanoid.KEYS[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Arkanoid.KEYS[e.getKeyCode()] = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.drawImage(Arkanoid.BACKGROUND, 0, 0, Arkanoid.WIDTH, Arkanoid.HEIGHT, null);

        g2.drawImage(Arkanoid.PADDLE, this.paddle.x, this.paddle.y, null);

        g2.drawImage(Arkanoid.BALL, this.ball.x, this.ball.y, null);

        for(Block block : Arkanoid.BLOCKS_ARR) {
            if(!block.isDestroyed()) {
                Rectangle bb = block.getBoundingBox();
                g2.drawImage(block.getTexture(), bb.x, bb.y, null);
            }
        }

        g2.dispose();
    }

    private void update() {
        if(Arkanoid.KEYS[KeyEvent.VK_SPACE]) {
            this.gameStarted = true;
        }

        if(Arkanoid.KEYS[KeyEvent.VK_A] || Arkanoid.KEYS[KeyEvent.VK_LEFT]) {
            if(this.paddle.x > 0) {
                this.paddle.x -= Arkanoid.PADDLE_SPEED;
            }
        }
        if(Arkanoid.KEYS[KeyEvent.VK_D] || Arkanoid.KEYS[KeyEvent.VK_RIGHT]) {
            if(this.paddle.x + Arkanoid.PADDLE_WIDTH < Arkanoid.WIDTH) {
                this.paddle.x += Arkanoid.PADDLE_SPEED;
            }
        }

        if(this.gameStarted) {
            this.ball.x += Arkanoid.BALL_SPEED_X;
            this.ball.y += Arkanoid.BALL_SPEED_Y;

            if(this.ball.x < 0) {
                Arkanoid.BALL_SPEED_X *= -1;
            }
            if(this.ball.x + Arkanoid.BALL_SIZE > Arkanoid.WIDTH) {
                Arkanoid.BALL_SPEED_X *= -1;
            }
            if(this.ball.y < 0) {
                Arkanoid.BALL_SPEED_Y *= -1;
            }
            if(this.ball.y + Arkanoid.BALL_SIZE > Arkanoid.HEIGHT) {
                Arkanoid.BALL_SPEED_Y *= -1;
            }

            if(this.ball.y + Arkanoid.BALL_SIZE > this.paddle.y) {
                if(this.ball.x > this.paddle.x && this.ball.x + Arkanoid.BALL_SIZE < this.paddle.x + Arkanoid.PADDLE_WIDTH) {
                    Arkanoid.BALL_SPEED_Y *= -1;
                }
            }

            for(Block block : Arkanoid.BLOCKS_ARR) {
                if(!block.isDestroyed()) {
                    Rectangle bb = block.getBoundingBox();
                    if(bb.contains(this.ball.x, this.ball.y)) {
                        if(this.ball.x > this.ball.y) {
                            Arkanoid.BALL_SPEED_Y *= -1;
                        } else {
                            Arkanoid.BALL_SPEED_X *= -1;
                        }
                        block.setDestroyed(true);
                    } else if(bb.contains(this.ball.x + Arkanoid.BALL_SIZE, this.ball.y + Arkanoid.BALL_SIZE)) {
                        if(this.ball.x < this.ball.y) {
                            Arkanoid.BALL_SPEED_X *= -1;
                        } else {
                            Arkanoid.BALL_SPEED_Y *= -1;
                        }
                        block.setDestroyed(true);
                    } else if(bb.contains(this.ball.x, this.ball.y + Arkanoid.BALL_SIZE)) {
                        if(this.ball.x < this.ball.y) {
                            Arkanoid.BALL_SPEED_Y *= -1;
                        } else {
                            Arkanoid.BALL_SPEED_X *= -1;
                        }
                        block.setDestroyed(true);
                    }
                }
            }
        }
    }

    private void placeBlocks() {
        for(int y = 0; y < Arkanoid.BLOCKS_IN_HEIGHT; y++) {
            for(int x = 0; x < Arkanoid.BLOCKS_IN_WIDTH; x++) {
                Block block = Arkanoid.BLOCKS_ARR[y * Arkanoid.BLOCKS_IN_WIDTH + x];
                Rectangle bb = new Rectangle(
                        x * Arkanoid.BLOCK_WIDTH + Arkanoid.BLOCKS_OFFSET_X,
                        y * Arkanoid.BLOCK_HEIGHT + Arkanoid.BLOCKS_OFFSET_Y,
                        Arkanoid.BLOCK_WIDTH,
                        Arkanoid.BLOCK_HEIGHT
                );
                block.setBoundingBox(bb);
            }
        }
    }

    private void start() {
        new Thread(this).start();
    }

    private static BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(Objects.requireNonNull(Arkanoid.class.getResourceAsStream(path)));
        } catch (IOException e) {
            throw new RuntimeException(e); // If the image is not loaded, there is no point in working further.
        }
    }

    private static void printCredits() {
        System.out.println("Credits:");
        System.out.println("    - Breakout (Arkanoid) Asset Pack by Joyrider3774");
        System.out.println("      Download: https://joyrider3774.itch.io/basic-breakout-asset-pack");
        System.out.println("----------------------------------------------------------------------");
    }

    public static void main(String[] args) {
        Arkanoid.printCredits();

        Arkanoid arkanoid = new Arkanoid();
        JFrame frame = new JFrame("Arkanoid");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(arkanoid);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        arkanoid.start();
    }
}
