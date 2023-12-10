package dev.wjteo.progressindicator.component;

import dev.wjteo.progressindicator.helper.ProgressState;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class ProgressComponent extends JPanel {
    protected static final Color COMPLETED_COLOR = new Color(0, 200, 0);
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected final int scaledDimension;
    protected short ticks = 0;
    protected int start = 0;
    protected int extent = 0;
    protected boolean flipped = false;
    protected ProgressState state = ProgressState.IDLE;
    protected boolean manualLock = false;

    public ProgressComponent(final int scaledDimension) {
        this.scaledDimension = scaledDimension;
        initSize();
    }

    protected abstract void initSize();

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    protected Color getColor(ProgressState state) {
        switch (state) {
            case COMPLETED:
                return COMPLETED_COLOR;
            case FAILED:
                return Color.RED.darker();
            default:
                return Color.GRAY.brighter();
        }
    }

    public boolean updateProgress(boolean success) {
        if (lock.writeLock().tryLock()) {
            try {
                final ProgressState currentState = this.state;
                if (manualLock) return false;
                if (currentState == ProgressState.COMPLETED || currentState == ProgressState.FAILED || currentState == ProgressState.LAST) return false;

                if (!success) {
                    this.state = ProgressState.FAILED;
                    return true;
                }

                final int newOrdinal = currentState.ordinal() + 1;
                this.state = ProgressState.values()[newOrdinal];
                if (this.state == ProgressState.LAST) manualLock = true;
                return true;
            } finally {
                lock.writeLock().unlock();
            }
        }

        return false;
    }

    public boolean isIdle() {
        return this.state == ProgressState.IDLE;
    }

    public boolean isProgress() {
        return this.state == ProgressState.PROGRESS;
    }

    public boolean isFailed() {
        return this.state == ProgressState.FAILED;
    }

    public boolean isCompleted() {
        return this.state == ProgressState.COMPLETED;
    }
}
