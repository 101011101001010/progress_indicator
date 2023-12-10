package dev.wjteo.progressindicator.component;

import dev.wjteo.progressindicator.helper.ProgressState;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

public class ProgressCircle extends ProgressComponent {
    private int previousRed = 255;

    public ProgressCircle(final int scaledDimension) {
        super(scaledDimension);
    }

    @Override
    protected void initSize() {
        final Dimension dimension = new Dimension(scaledDimension, scaledDimension);
        setSize(dimension);
        setPreferredSize(dimension);
        setMinimumSize(dimension);
        setMaximumSize(dimension);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        final Graphics2D graphics = (Graphics2D) g;
        final Shape circle = new Ellipse2D.Double(0, 0, scaledDimension, scaledDimension);
        final ProgressState state = this.state;
        graphics.setColor(getColor(state));
        graphics.fill(circle);

        if (state == ProgressState.IDLE || state == ProgressState.FAILED || state == ProgressState.COMPLETED) return;
        drawAnimation(graphics, state);
    }

    private void drawAnimation(Graphics2D graphics, ProgressState state) {
        calculateArcParameters();
        if (extent == 0) return;
        final Arc2D arc = new Arc2D.Double(0, 0, scaledDimension, scaledDimension, -start, -extent, Arc2D.PIE);
        graphics.setColor(getAnimationColor(state, extent));
        graphics.fill(arc);
    }

    private Color getAnimationColor(ProgressState state, int extent) {
        final int previousRed = this.previousRed;

        if (state != ProgressState.LAST || flipped) {
            this.previousRed = 255;
            return Color.ORANGE;
        }

        final int extentDelta = 360 - extent;
        if (extentDelta <= 0) return COMPLETED_COLOR;

        final int step = previousRed / extentDelta;
        final int newRed = previousRed - step;
        this.previousRed = newRed;
        return new Color(newRed, 200, 0);
    }

    @SuppressWarnings("WrapperTypeMayBePrimitive")
    protected void calculateArcParameters() {
        final ProgressState state = this.state;
        if (state == ProgressState.COMPLETED) return;

        if (extent >= 360 && state == ProgressState.LAST) {
            manualLock = false;
            this.state = ProgressState.COMPLETED;
            return;
        }

        if (state == ProgressState.IDLE) {
            start = 0;
            extent = 0;
            return;
        }

        if (flipped) {
            if (extent > 0) {
                start += 20;
                extent -= 15;
            }
        } else {
            if (state == ProgressState.PROGRESS) start += 5;
            if (state == ProgressState.LAST) {
                if (extent < 180) {
                    extent += 20;
                } else {
                    Double change = (180.0 / extent) * 20;
                    if (change < 2) change = 2d;
                    extent += change.intValue();
                }
            } else {
                extent += 20;
            }
        }

        if (start >= 360) start -= 360;
        if (start < 0) start = 0;
        if (extent >= 330 && state == ProgressState.PROGRESS) extent = 330;
        if (extent < 0) extent = 0;
        if (!flipped && state == ProgressState.LAST) return;

        if (!flipped && extent == 330) {
            ticks++;
        } else if (flipped && extent == 0) {
            ticks++;
        }

        if (ticks >= 10) {
            ticks = 0;
            flipped = !flipped;
        }
    }
}
