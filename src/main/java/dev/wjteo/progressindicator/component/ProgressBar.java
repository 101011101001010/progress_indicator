package dev.wjteo.progressindicator.component;

import dev.wjteo.progressindicator.helper.ProgressState;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class ProgressBar extends ProgressComponent {
    public ProgressBar(final int scaledDimension) {
        super(scaledDimension);
    }

    @Override
    protected void initSize() {
        final Dimension dimension = new Dimension(scaledDimension, 8);
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
        final Shape rectangle = new Rectangle2D.Double(0, 0, scaledDimension, 8);
        final ProgressState state = this.state;
        graphics.setColor(getColor(state));
        graphics.fill(rectangle);

        if (state == ProgressState.IDLE || state == ProgressState.FAILED || state == ProgressState.COMPLETED) return;
        drawAnimation(graphics);
    }

    private void drawAnimation(Graphics2D graphics) {
        calculateParameters();
        final Shape rectangle = new Rectangle2D.Double(0, 0, extent, 8);
        graphics.setColor(COMPLETED_COLOR);
        graphics.fill(rectangle);
    }

    private void calculateParameters() {
        final ProgressState state = this.state;
        if (state == ProgressState.COMPLETED) return;

        if (extent == scaledDimension && state == ProgressState.LAST) {
            manualLock = false;
            this.state = ProgressState.COMPLETED;
            return;
        }

        extent += 12;
        if (extent >= scaledDimension) extent = scaledDimension;
    }
}
