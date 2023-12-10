package dev.wjteo.progressindicator;

import dev.wjteo.progressindicator.component.ProgressBar;
import dev.wjteo.progressindicator.component.ProgressCircle;
import dev.wjteo.progressindicator.component.ProgressComponent;
import dev.wjteo.progressindicator.helper.ProgressStage;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ProgressIndicator extends JPanel {
    private static final int BASE_DIMENSION = 60;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ExecutorService queueExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService updateExecutor = Executors.newSingleThreadExecutor();
    private final Runnable onComplete;

    private final List<ProgressComponent> components = new ArrayList<>();
    private final List<Boolean> queue = new ArrayList<>();
    private final ProgressStage[] stages;
    private final int scaledDimension;
    private final int scaledTotalDimension;

    private final JLabel descriptionLabel = new JLabel("", SwingConstants.LEFT);

    private int currentIndex;
    private boolean disposed = false;

    @SuppressWarnings("WrapperTypeMayBePrimitive")
    public ProgressIndicator(final ProgressStage[] stages, final double scale, final Runnable onComplete) {
        if (stages.length < 1) {
            throw new IllegalStateException("# Stages cannot be less than 1.");
        }

        this.stages = stages;
        this.currentIndex = 0;
        this.onComplete = onComplete;

        Double scaledDimension = scale * BASE_DIMENSION;
        if (scaledDimension < 16) scaledDimension = 16d;
        this.scaledDimension = scaledDimension.intValue();
        this.scaledTotalDimension = (stages.length + (stages.length - 1)) * this.scaledDimension;

        initContentPane();
        initComponentSet();
        startExecutor();
        updateProgress(true);
    }

    private void initContentPane() {
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        add(panel, BorderLayout.NORTH);

        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(14f));
        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.BOLD));
        descriptionLabel.setText(" ");
        panel.add(descriptionLabel);
        panel.add(Box.createVerticalStrut(16));
    }

    private void initComponentSet() {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        add(panel, BorderLayout.CENTER);

        final Dimension labelDimension = new Dimension(scaledDimension, 32);
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy += 1;

        for (int i = 0; i < stages.length; i++) {
            final ProgressStage stage = stages[i];
            constraints.gridy = 1;

            if (i > 0) {
                final ProgressBar progressBar = new ProgressBar(scaledDimension);
                components.add(progressBar);
                constraints.gridx += 1;
                panel.add(progressBar, constraints);
            }

            final ProgressCircle circle = new ProgressCircle(scaledDimension);
            components.add(circle);
            constraints.gridx += 1;
            panel.add(circle, constraints);

            final JLabel label = new JLabel(stage.getShortDescription(), SwingConstants.CENTER);
            label.setSize(labelDimension);
            label.setPreferredSize(labelDimension);
            constraints.gridy += 1;
            panel.add(label, constraints);
        }
    }

    private void setLongDescription(final int currentIndex) {
        if (currentIndex == -1) {
            descriptionLabel.setText("Speech delivery successful.");
            return;
        }

        if (currentIndex == -2) {
            descriptionLabel.setText("Speech delivery failed.");
            return;
        }

        final int stageIndex = currentIndex / 2;

        if (stageIndex < 0 || stageIndex >= stages.length) {
            descriptionLabel.setText("Something went wrong.");
            return;
        }

        final ProgressStage stage = stages[stageIndex];
        String description = stage.getLongDescription();
        if (!description.endsWith(".")) description += "...";
        descriptionLabel.setText(description);
    }

    @SuppressWarnings("BusyWait")
    private void startExecutor() {
        Runnable r = () -> {
            while (!disposed) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException ignored) {
                }
                processQueue();
                repaint();
            }
        };

        queueExecutor.submit(r);
    }

    public void shutdown(boolean forced) {
        this.disposed = true;
        updateExecutor.shutdown();
        queueExecutor.shutdownNow();
        queue.clear();
        if (!forced) onComplete.run();
    }

    public int getScaledTotalDimension() {
        return scaledTotalDimension;
    }

    public synchronized void updateProgress(boolean success) {
        if (disposed) return;

        Runnable r = () -> {
            boolean l = lock.writeLock().tryLock();

            while (!l) {
                try {
                    Thread.sleep(5);
                    l = lock.writeLock().tryLock();
                } catch (InterruptedException ignored) {
                }
            }

            queue.add(success);
            lock.writeLock().unlock();
        };

        updateExecutor.submit(r);
    }

    public void processQueue() {
        if (lock.writeLock().tryLock()) {
            try {
                if (currentIndex >= components.size()) return;
                final ProgressComponent component = components.get(currentIndex);

                if (component.isFailed()) {
                    setLongDescription(-2);
                    shutdown(false);
                    return;
                }

                if (component.isCompleted()) {
                    currentIndex++;

                    if (currentIndex >= components.size()) {
                        setLongDescription(-1);
                        shutdown(false);
                    }

                    return;
                }

                if (component.isIdle() && currentIndex > 0) {
                    setLongDescription(currentIndex);
                    component.updateProgress(true);
                    return;
                }

                if (component.isProgress() && currentIndex % 2 != 0) {
                    component.updateProgress(true);
                    return;
                }

                if (queue.isEmpty()) return;
                final boolean update = queue.get(0);
                final boolean success = component.updateProgress(update);
                if (!success) return;
                setLongDescription(currentIndex);
                queue.remove(0);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
}
