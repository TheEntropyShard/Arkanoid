import java.awt.*;
import java.awt.image.BufferedImage;

public class Block {
    private final BufferedImage texture;
    private Rectangle boundingBox;
    private boolean destroyed;

    public Block(BufferedImage texture, Rectangle boundingBox) {
        this.texture = texture;
        this.boundingBox = boundingBox;
    }

    public BufferedImage getTexture() {
        return this.texture;
    }

    public Rectangle getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(Rectangle boundingBox) {
        this.boundingBox = boundingBox;
    }

    public boolean isDestroyed() {
        return this.destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
}
