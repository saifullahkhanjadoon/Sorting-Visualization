import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class NeonSortStudio extends JFrame {

    // --- CONFIGURATION ---
    private static final int WIDTH = 1300;
    private static final int HEIGHT = 900; // Slightly taller for stats
    private static final int FPS = 60;

    // --- COLOR PALETTE ---
    private static final Color BG_COLOR = new Color(18, 20, 24);
    private static final Color PANEL_COLOR = new Color(25, 28, 33);
    private static final Color BORDER_COLOR = new Color(40, 44, 52);

    private static final Color COL_DEFAULT = new Color(120, 130, 150);
    private static final Color COL_COMPARE = new Color(50, 200, 255); // Cyan
    private static final Color COL_SORTED = new Color(0, 220, 130);   // Green
    private static final Color COL_PIVOT = new Color(255, 80, 120);   // Pink
    private static final Color COL_POINTER = new Color(255, 180, 50); // Orange
    private static final Color COL_TEXT_BRIGHT = new Color(240, 240, 240);

    // --- STATE ---
    private final List<VisualNode> nodes = new CopyOnWriteArrayList<>();

    private JPanel cardPanel;
    private CardLayout cardLayout;
    private CanvasPanel canvas;
    private JScrollPane scrollPane;
    private JButton pauseBtn;

    // STATS LABELS
    private JLabel compLabel;
    private JLabel swapLabel;
    private JLabel complexityLabel;

    private volatile boolean isSorting = false;
    private volatile boolean isPaused = false;
    private final Object pauseLock = new Object();

    private String currentAlgo = "Merge Sort";
    private ViewMode viewMode = ViewMode.NODES;
    private int delayMs = 300;
    private double physicsSpeed = 0.15;

    // Real-time Counters
    private long comparisons = 0;
    private long swaps = 0;

    // Pointers
    private int ptrA = -1;
    private int ptrB = -1;
    private int ptrPivot = -1;
    private String ptrALabel = "";
    private String ptrBLabel = "";

    public NeonSortStudio() {
        setTitle("Sorting Algorithm Visualizer");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        add(cardPanel);

        // 1. Intro Screen
        cardPanel.add(createIntroScreen(), "INTRO");

        // 2. Main App Screen
        JPanel mainApp = new JPanel(new BorderLayout());
        mainApp.add(createControlPanel(), BorderLayout.NORTH);

        canvas = new CanvasPanel();
        canvas.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(BG_COLOR);

        mainApp.add(scrollPane, BorderLayout.CENTER);

        // ADDED STATS PANEL HERE
        mainApp.add(createStatsPanel(), BorderLayout.SOUTH);

        cardPanel.add(mainApp, "APP");

        // Initialize Complexity Text
        updateComplexityText();

        // Animation Loop
        new Timer(1000 / FPS, e -> {
            for (VisualNode n : nodes) n.update();
            canvas.repaint();
            updateStatsUI(); // Update numbers every frame
        }).start();
    }

    private JPanel createStatsPanel() {
        JPanel container = new JPanel(new GridLayout(2, 1)); // 2 Rows
        container.setBackground(PANEL_COLOR);
        container.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        // Row 1: Real-time Counters
        JPanel countersPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 10));
        countersPanel.setOpaque(false);

        compLabel = new JLabel("Comparisons: 0");
        compLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        compLabel.setForeground(COL_COMPARE); // Bright Cyan

        swapLabel = new JLabel("Swaps: 0");
        swapLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        swapLabel.setForeground(COL_PIVOT);   // Bright Pink

        countersPanel.add(compLabel);
        countersPanel.add(swapLabel);

        // Row 2: Complexity Info
        JPanel complexityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        complexityPanel.setOpaque(false);

        complexityLabel = new JLabel("Time Complexity: O(n log n)");
        complexityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        complexityLabel.setForeground(COL_DEFAULT);

        complexityPanel.add(complexityLabel);

        container.add(countersPanel);
        container.add(complexityPanel);

        return container;
    }

    private void updateStatsUI() {
        compLabel.setText("Comparisons: " + comparisons);
        swapLabel.setText("Swaps: " + swaps);
    }

    private void updateComplexityText() {
        String complexity = "";
        switch (currentAlgo) {
            case "Merge Sort":
                complexity = "Time Complexity: Best: O(n log n) | Avg: O(n log n) | Worst: O(n log n)";
                break;
            case "Quick Sort":
                complexity = "Time Complexity: Best: O(n log n) | Avg: O(n log n) | Worst: O(n²)";
                break;
            case "Insertion Sort":
                complexity = "Time Complexity: Best: O(n) | Avg: O(n²) | Worst: O(n²)";
                break;
            case "Selection Sort":
                complexity = "Time Complexity: Best: O(n²) | Avg: O(n²) | Worst: O(n²)";
                break;
            case "Bubble Sort":
                complexity = "Time Complexity: Best: O(n) | Avg: O(n²) | Worst: O(n²)";
                break;
        }
        complexityLabel.setText(complexity);
    }

    private JPanel createIntroScreen() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_COLOR);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 52));
                String title = "SORTING ALGORITHM VISUALIZER";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(title, (getWidth() - fm.stringWidth(title))/2, getHeight()/2 - 60);

                g2.setFont(new Font("Segoe UI Light", Font.PLAIN, 28));
                g2.setColor(COL_COMPARE);
                String mainHeading = "SORTING";
                g2.drawString(mainHeading, (getWidth() - g2.getFontMetrics().stringWidth(mainHeading))/2, getHeight()/2);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                g2.setColor(COL_DEFAULT);
                String presentedBy = "Presented by: Saifullah Khan Jadoon ";
                g2.drawString(presentedBy, (getWidth() - g2.getFontMetrics().stringWidth(presentedBy))/2, getHeight()/2 + 60);
            }
        };
        p.setLayout(null);

        JButton enterBtn = new JButton("START VISUALIZATION");
        styleButton(enterBtn, COL_SORTED);
        enterBtn.setBounds((WIDTH/2)-120, (HEIGHT/2)+120, 240, 50);
        enterBtn.addActionListener(e -> {
            cardLayout.show(cardPanel, "APP");
            addRandomNodes(20);
        });

        p.add(enterBtn);
        return p;
    }

    private JPanel createControlPanel() {
        JPanel container = new JPanel(new GridLayout(2, 1));
        container.setBackground(PANEL_COLOR);
        container.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        row1.setOpaque(false);

        String[] algos = {"Merge Sort", "Quick Sort", "Insertion Sort", "Selection Sort", "Bubble Sort"};
        JComboBox<String> algoBox = new JComboBox<>(algos);
        styleComponent(algoBox);
        algoBox.addActionListener(e -> {
            currentAlgo = (String) algoBox.getSelectedItem();
            updateComplexityText(); // Update info when algo changes
        });

        JSlider speedSlider = new JSlider(10, 1000, 300);
        speedSlider.setOpaque(false);
        speedSlider.setPreferredSize(new Dimension(150, 30));
        speedSlider.addChangeListener(e -> delayMs = 1010 - speedSlider.getValue());

        JToggleButton viewBtn = new JToggleButton("View: NODES");
        styleButton(viewBtn, COL_COMPARE);
        viewBtn.addActionListener(e -> {
            viewMode = viewBtn.isSelected() ? ViewMode.BARS : ViewMode.NODES;
            viewBtn.setText(viewBtn.isSelected() ? "View: BARS" : "View: NODES");
            arrangeNodesLinearly();
        });

        JButton startBtn = new JButton("Play");
        styleButton(startBtn, COL_SORTED);
        startBtn.addActionListener(e -> startSorting());

        pauseBtn = new JButton("Pause");
        styleButton(pauseBtn, new Color(255, 160, 60));
        pauseBtn.addActionListener(e -> togglePause());

        JButton resetBtn = new JButton("Reset");
        styleButton(resetBtn, new Color(230, 80, 80));
        resetBtn.addActionListener(e -> fullReset());

        row1.add(new JLabel("Algorithm:") {{ setForeground(COL_DEFAULT); }});
        row1.add(algoBox);
        row1.add(new JLabel(" Speed:") {{ setForeground(COL_DEFAULT); }});
        row1.add(speedSlider);
        row1.add(Box.createHorizontalStrut(15));
        row1.add(viewBtn);
        row1.add(Box.createHorizontalStrut(15));
        row1.add(startBtn);
        row1.add(pauseBtn);
        row1.add(resetBtn);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        row2.setOpaque(false);
        row2.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JTextField inputField = new JTextField(15);
        styleComponent(inputField);
        inputField.setToolTipText("e.g. 10, 50, 33");

        JButton addBtn = new JButton("+ Add");
        styleButton(addBtn, COL_DEFAULT);
        addBtn.addActionListener(e -> parseAndAddInput(inputField.getText()));

        JSpinner randCountSpin = new JSpinner(new SpinnerNumberModel(20, 5, 200, 1));
        styleComponent((JComponent) randCountSpin.getEditor());
        ((JSpinner.DefaultEditor)randCountSpin.getEditor()).getTextField().setBackground(PANEL_COLOR.darker());
        ((JSpinner.DefaultEditor)randCountSpin.getEditor()).getTextField().setForeground(Color.WHITE);
        randCountSpin.setPreferredSize(new Dimension(60, 25));

        JButton randBtn = new JButton("Randomize");
        styleButton(randBtn, COL_DEFAULT);
        randBtn.addActionListener(e -> {
            if(!isSorting) {
                nodes.clear();
                addRandomNodes((Integer) randCountSpin.getValue());
            }
        });

        JButton clearBtn = new JButton("Clear");
        styleButton(clearBtn, COL_DEFAULT);
        clearBtn.addActionListener(e -> {
            if(!isSorting) {
                nodes.clear();
                canvas.repaint();
                arrangeNodesLinearly();
            }
        });

        row2.add(new JLabel("Input:") {{ setForeground(COL_DEFAULT); }});
        row2.add(inputField);
        row2.add(addBtn);
        row2.add(Box.createHorizontalStrut(20));
        row2.add(new JLabel("Size:") {{ setForeground(COL_DEFAULT); }});
        row2.add(randCountSpin);
        row2.add(randBtn);
        row2.add(clearBtn);

        container.add(row1);
        container.add(row2);
        return container;
    }

    private void parseAndAddInput(String text) {
        if(isSorting) return;
        if(text.trim().isEmpty()) return;
        try {
            String[] parts = text.split(",");
            boolean added = false;
            for(String p : parts) {
                String clean = p.trim();
                if(!clean.isEmpty()) {
                    int val = Integer.parseInt(clean);
                    nodes.add(new VisualNode(val, WIDTH/2.0, HEIGHT/2.0));
                    added = true;
                }
            }
            if(added) arrangeNodesLinearly();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter numbers separated by commas.");
        }
    }

    private void addRandomNodes(int count) {
        Random r = new Random();
        for(int i=0; i<count; i++) {
            nodes.add(new VisualNode(r.nextInt(95)+5, WIDTH/2.0, HEIGHT/2.0));
        }
        arrangeNodesLinearly();
    }

    private void arrangeNodesLinearly() {
        int margin = 60;
        int minSlotWidth = 50;
        int totalReqWidth = margin * 2 + (nodes.size() * minSlotWidth);
        int viewWidth = Math.max(scrollPane != null ? scrollPane.getWidth() : WIDTH, totalReqWidth);
        if(viewWidth < totalReqWidth) viewWidth = totalReqWidth;

        int canvasH = (currentAlgo.equals("Merge Sort")) ? 1500 : HEIGHT;
        if(canvas != null) {
            canvas.setPreferredSize(new Dimension(viewWidth, canvasH));
            canvas.revalidate();
        }

        double spacing = (double)(viewWidth - (margin*2)) / Math.max(1, nodes.size());
        double startX = margin + (spacing / 2.0);
        double startY = (viewMode == ViewMode.NODES) ? 150 : 600;

        for(int i=0; i<nodes.size(); i++) {
            VisualNode n = nodes.get(i);
            n.targetX = startX + (i * spacing);
            n.targetY = startY;
        }
    }

    private void fullReset() {
        isSorting = false;
        isPaused = false;
        pauseBtn.setText("Pause");
        synchronized(pauseLock) { pauseLock.notifyAll(); }

        clearPointers();
        comparisons = 0;
        swaps = 0;

        for(VisualNode n : nodes) n.color = COL_DEFAULT;
        arrangeNodesLinearly();
        canvas.repaint();
    }

    private void togglePause() {
        if(!isSorting) return;
        isPaused = !isPaused;
        pauseBtn.setText(isPaused ? "Resume" : "Pause");
        if(!isPaused) {
            synchronized(pauseLock) { pauseLock.notifyAll(); }
        }
    }

    private void checkPaused() {
        synchronized(pauseLock) {
            while(isPaused) {
                try { pauseLock.wait(); } catch(InterruptedException e) {}
            }
        }
    }

    private void startSorting() {
        if(isSorting || nodes.isEmpty()) return;
        isSorting = true;
        isPaused = false;
        pauseBtn.setText("Pause");

        comparisons = 0;
        swaps = 0;

        for(VisualNode n : nodes) n.color = COL_DEFAULT;

        new Thread(() -> {
            try {
                switch(currentAlgo) {
                    case "Merge Sort" -> runMergeSort();
                    case "Quick Sort" -> runQuickSort();
                    case "Insertion Sort" -> runInsertionSort();
                    case "Selection Sort" -> runSelectionSort();
                    case "Bubble Sort" -> runBubbleSort();
                }
                clearPointers();
                arrangeNodesLinearly();
                sleepSafe(500);
                for(VisualNode n : nodes) {
                    n.color = COL_SORTED;
                    sleepSafe(30);
                }
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                isSorting = false;
            }
        }).start();
    }

    private void runMergeSort() {
        arrangeNodesLinearly();
        mergeSortRec(0, nodes.size()-1, 1);
    }

    private void mergeSortRec(int l, int r, int depth) {
        if(l >= r || !isSorting) return;
        checkPaused();

        int mid = (l + r) / 2;

        double levelY;
        if(viewMode == ViewMode.NODES) {
            levelY = 150 + (depth * 130);
        } else {
            if(depth == 1) { for(VisualNode n : nodes) n.targetY = 250; waitForPhysics(); }
            levelY = 250 + (depth * 130);
        }

        for(int i=l; i<=r; i++) {
            VisualNode n = nodes.get(i);
            n.targetY = levelY;
            if(i <= mid) n.targetX -= 15;
            else n.targetX += 15;
        }
        waitForPhysics();

        mergeSortRec(l, mid, depth+1);
        mergeSortRec(mid+1, r, depth+1);
        merge(l, mid, r, depth);
    }

    private void merge(int l, int mid, int r, int depth) {
        checkPaused();
        List<VisualNode> temp = new ArrayList<>();
        int i = l, j = mid + 1;

        ptrA = i; ptrALabel = "L";
        ptrB = j; ptrBLabel = "R";

        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        for(int k=l; k<=r; k++) {
            minX = Math.min(minX, nodes.get(k).targetX);
            maxX = Math.max(maxX, nodes.get(k).targetX);
        }
        double step = (maxX - minX) / (Math.max(1, r - l));

        while(i <= mid && j <= r) {
            checkPaused();
            ptrA = i; ptrB = j;
            comparisons++;
            nodes.get(i).color = COL_COMPARE;
            nodes.get(j).color = COL_COMPARE;
            sleepSafe(delayMs);

            if(nodes.get(i).value <= nodes.get(j).value) {
                temp.add(nodes.get(i));
                nodes.get(i).color = COL_DEFAULT;
                i++;
            } else {
                temp.add(nodes.get(j));
                nodes.get(j).color = COL_DEFAULT;
                j++;
            }
        }
        while(i <= mid) temp.add(nodes.get(i++));
        while(j <= r) temp.add(nodes.get(j++));

        double targetY = (viewMode == ViewMode.NODES) ? 150 + ((depth-1)*130) : 250 + ((depth-1)*130);

        for(int k=0; k<temp.size(); k++) {
            VisualNode n = temp.get(k);
            nodes.set(l + k, n);
            swaps++;
            n.color = COL_SORTED;
            n.targetY = targetY;
            n.targetX = minX + (k * step);
        }
        waitForPhysics();
        clearPointers();
    }

    private void runQuickSort() {
        quickSortRec(0, nodes.size()-1);
    }

    private void quickSortRec(int low, int high) {
        if(!isSorting) return;
        if(low < high) {
            int pi = partition(low, high);
            quickSortRec(low, pi-1);
            quickSortRec(pi+1, high);
        } else if (low == high) {
            nodes.get(low).color = COL_SORTED;
        }
    }

    private int partition(int low, int high) {
        checkPaused();
        VisualNode pivot = nodes.get(high);
        pivot.color = COL_PIVOT;
        ptrPivot = high;

        pivot.targetY -= 50;
        waitForPhysics();

        int i = low - 1;
        ptrALabel = "i"; ptrBLabel = "j";

        for(int j=low; j<high; j++) {
            checkPaused();
            ptrA = i; ptrB = j;
            comparisons++;
            nodes.get(j).color = COL_COMPARE;
            sleepSafe(delayMs/2);

            if(nodes.get(j).value < pivot.value) {
                i++;
                swap(i, j);
            }
            if(nodes.get(j) != pivot) nodes.get(j).color = COL_DEFAULT;
        }
        swap(i+1, high);

        nodes.get(i+1).targetY += 50;
        nodes.get(i+1).color = COL_SORTED;
        ptrPivot = -1;
        waitForPhysics();
        return i+1;
    }

    private void runInsertionSort() {
        for(int i=1; i<nodes.size(); i++) {
            if(!isSorting) return;
            checkPaused();

            VisualNode key = nodes.get(i);
            int keyVal = key.value;
            int j = i - 1;

            ptrPivot = i;
            key.color = COL_PIVOT;
            key.targetY += 60;
            waitForPhysics();

            while(j >= 0 && nodes.get(j).value > keyVal) {
                checkPaused();
                ptrA = j; ptrALabel = "scan";
                comparisons++;
                nodes.get(j).color = COL_COMPARE;
                sleepSafe(delayMs);

                VisualNode shiftNode = nodes.get(j);
                double spacing = (nodes.size()>1) ? (nodes.get(1).targetX - nodes.get(0).targetX) : 50;
                shiftNode.targetX += spacing;

                nodes.set(j+1, shiftNode);
                swaps++;
                nodes.get(j).color = COL_DEFAULT;
                j--;
            }
            nodes.set(j+1, key);
            swaps++;

            arrangeNodesLinearly();
            key.targetY -= 60;
            key.color = COL_SORTED;
            waitForPhysics();
        }
    }

    private void runSelectionSort() {
        for (int i = 0; i < nodes.size() - 1; i++) {
            if (!isSorting) return;
            checkPaused();
            ptrA = i;
            ptrALabel = "current";

            for (int j = i + 1; j < nodes.size(); j++) {
                checkPaused();
                ptrB = j;
                ptrBLabel = "?";
                comparisons++;
                nodes.get(i).color = COL_COMPARE;
                nodes.get(j).color = COL_COMPARE;
                sleepSafe(delayMs);

                if (nodes.get(j).value < nodes.get(i).value) {
                    swap(i, j);
                }

                nodes.get(j).color = COL_DEFAULT;
                nodes.get(i).color = COL_DEFAULT;
            }
            nodes.get(i).color = COL_SORTED;
        }
        nodes.get(nodes.size() - 1).color = COL_SORTED;
    }

    private void runBubbleSort() {
        for(int i=0; i<nodes.size()-1; i++) {
            if(!isSorting) return;
            for(int j=0; j<nodes.size()-i-1; j++) {
                checkPaused();
                ptrA = j; ptrALabel = "j";
                ptrB = j+1; ptrBLabel = "j+1";

                nodes.get(j).color = COL_COMPARE;
                nodes.get(j+1).color = COL_COMPARE;
                comparisons++;
                sleepSafe(delayMs);

                if(nodes.get(j).value > nodes.get(j+1).value) {
                    swap(j, j+1);
                }

                nodes.get(j).color = COL_DEFAULT;
                nodes.get(j+1).color = COL_DEFAULT;
            }
            nodes.get(nodes.size()-i-1).color = COL_SORTED;
        }
        nodes.get(0).color = COL_SORTED;
    }

    private void swap(int i, int j) {
        if(i == j) return;
        VisualNode n1 = nodes.get(i);
        VisualNode n2 = nodes.get(j);

        swaps++;

        double tx = n1.targetX;
        n1.targetX = n2.targetX;
        n2.targetX = tx;

        nodes.set(i, n2);
        nodes.set(j, n1);
        waitForPhysics();
    }

    private void waitForPhysics() {
        boolean moving = true;
        while(moving) {
            checkPaused();
            moving = false;
            for(VisualNode n : nodes) {
                if(Math.abs(n.x - n.targetX) > 1 || Math.abs(n.y - n.targetY) > 1) {
                    moving = true; break;
                }
            }
            try { Thread.sleep(10); } catch(Exception e){}
        }
    }

    private void sleepSafe(int ms) {
        try { Thread.sleep(ms); } catch(Exception e){}
    }

    private void clearPointers() {
        ptrA = -1; ptrB = -1; ptrPivot = -1;
    }

    private void styleButton(AbstractButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(new CompoundBorder(new LineBorder(bg.darker(), 1), new EmptyBorder(8, 20, 8, 20)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleButton(JToggleButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(new LineBorder(bg.darker(), 1), new EmptyBorder(8, 20, 8, 20)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleComponent(JComponent c) {
        c.setBackground(Color.WHITE);
        c.setForeground(Color.BLACK);
        c.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    enum ViewMode { NODES, BARS }

    class VisualNode {
        int value;
        double x, y;
        double targetX, targetY;
        Color color;

        VisualNode(int v, double x, double y) {
            this.value = v;
            this.x = targetX = x;
            this.y = targetY = y;
            this.color = COL_DEFAULT;
        }

        void update() {
            x += (targetX - x) * physicsSpeed;
            y += (targetY - y) * physicsSpeed;
        }
    }

    class CanvasPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(BG_COLOR);
            g2.fillRect(0,0,getWidth(), getHeight());

            if(nodes.isEmpty()) return;

            drawPtr(g2, ptrA, ptrALabel, COL_POINTER);
            drawPtr(g2, ptrB, ptrBLabel, COL_POINTER);
            drawPtr(g2, ptrPivot, "KEY", COL_PIVOT);

            for(VisualNode n : nodes) {
                if(viewMode == ViewMode.NODES) {
                    int size = 42;
                    g2.setColor(new Color(0,0,0,50));
                    g2.fillOval((int)n.x - size/2 + 2, (int)n.y - size/2 + 2, size, size);

                    g2.setColor(n.color);
                    g2.fillOval((int)n.x - size/2, (int)n.y - size/2, size, size);

                    g2.setColor(new Color(255,255,255,150));
                    g2.setStroke(new BasicStroke(1));
                    g2.drawOval((int)n.x - size/2, (int)n.y - size/2, size, size);

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    String s = String.valueOf(n.value);
                    g2.drawString(s, (int)n.x - g2.getFontMetrics().stringWidth(s)/2, (int)n.y + 5);
                } else {
                    int w = Math.max(10, (getWidth()-100)/nodes.size() - 5);
                    int h = Math.min(400, n.value * 4);

                    g2.setColor(n.color);
                    g2.fillRoundRect((int)n.x - w/2, (int)n.y - h, w, h, 6, 6);

                    g2.setColor(new Color(255,255,255,50));
                    g2.setFont(new Font("Monospaced", Font.BOLD, 10));
                    String s = String.valueOf(n.value);
                    g2.drawString(s, (int)n.x - g2.getFontMetrics().stringWidth(s)/2, (int)n.y + 15);
                }
            }
        }

        private void drawPtr(Graphics2D g, int idx, String label, Color c) {
            if(idx < 0 || idx >= nodes.size()) return;
            VisualNode n = nodes.get(idx);

            int x = (int) n.x;
            int yTop = (viewMode==ViewMode.NODES) ? (int)n.y - 35 : (int)n.y - Math.min(400, n.value*4) - 10;
            int yBot = yTop - 25;

            g.setColor(c);
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{5}, 0));
            g.drawLine(x, yTop, x, yBot);

            int[] px = {x, x-5, x+5};
            int[] py = {yTop, yTop-8, yTop-8};
            g.fillPolygon(px, py, 3);

            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g.drawString(label, x - g.getFontMetrics().stringWidth(label)/2, yBot - 5);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception e){}
        SwingUtilities.invokeLater(() -> new NeonSortStudio().setVisible(true));
    }
}