package dev.wjteo;

import dev.wjteo.progressindicator.ProgressIndicator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProgressDialog extends JDialog {
    private final MainFrame frame;
    private final ProgressIndicator progressIndicator;
    private final JPanel testPanel = new JPanel();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ProgressDialog(MainFrame frame) {
        super(frame);
        this.frame = frame;
        this.progressIndicator = new ProgressIndicator(TProgressStage.values(), 1, this::onComplete);
        frame.setDimmed(true);

        initDialog();
        initContent();
        initSize();
        startExecutor();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    private void initDialog() {
        Dimension dimension = new Dimension(progressIndicator.getScaledTotalDimension() + 32, 0);
        setTitle("Speech Delivery In Progress");
        setLayout(new BorderLayout());
        setMinimumSize(dimension);
        setModal(false);
        setUndecorated(false);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(16, 32, 16, 32));
        panel.add(progressIndicator, BorderLayout.CENTER);

        JButton button = new JButton("TEST");
        JButton button2 = new JButton("TEST");
        button.addActionListener(e -> updateP());
        button2.addActionListener(e -> updateP2());

        testPanel.setPreferredSize(new Dimension(getPreferredSize().width, 400));
        testPanel.setVisible(false);
        panel.add(testPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private void initSize() {
        final int width = getPreferredSize().width;
        final int height = getPreferredSize().height + 28;
        setSize(new Dimension(width, height));
    }

    @SuppressWarnings("BusyWait")
    private void startExecutor() {
        Runnable r = () -> {
            final Random random = new Random();

            for (int i = 0; i < TProgressStage.values().length; i++) {
                long wait = (random.nextInt(4) + 2) * 1000;

                try {
                    Thread.sleep(wait);
                } catch (InterruptedException ignored) {
                }

                int count = 0;
                for (int j = 0; j < 6; j++) if (random.nextBoolean()) count++;
                progressIndicator.updateProgress(count > 2);
            }
        };

        executor.submit(r);
    }

    private void updateP() {
        progressIndicator.updateProgress(true);
    }

    private void updateP2() {
        progressIndicator.updateProgress(false);
    }

    private void onComplete() {
        System.out.println("COMPLETED.");
        testPanel.setVisible(true);
        setSize(getPreferredSize());
    }

    @Override
    public void dispose() {
        super.dispose();
        progressIndicator.shutdown(true);
        frame.setDimmed(false);
    }
}
