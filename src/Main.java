import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

class Ising{
    int [][] lattice;
    int N, iCurrent, jCurrent;
    double h, J, delta;
    Random rnd;
    Ising(int n, double j_, double h_, double positivePercentage){
        lattice = new int[n][n];
        N = n;
        h = h_;
        J = j_;
        rnd = new Random();
        generateLattice(positivePercentage);
        iCurrent = rnd.nextInt(N);
        jCurrent = rnd.nextInt(N);
    }
    private void generateLattice(double positivePercentage){
        for (int i = 0; i < N; i++){
            for (int j = 0; j < N; j++){
                if (rnd.nextDouble() < positivePercentage) lattice[i][j] = 1;
                else lattice[i][j] = -1;
            }
        }
    }
    static double getProbability(double delta, double T){
        return Math.exp(-delta / T);
    }
    private double E (int i, int j){
        double neighbors;
        neighbors = lattice[(i - 1 + N) % N][j] + lattice[(i + 1) % N][j] +
                lattice[i][(j + 1) % N] + lattice[i][(j - 1 + N) % N];
        return -J * lattice[i][j] * neighbors - h * lattice[i][j];
    }
    double Hamiltonian (){
        double H = 0;
        for (int i = 0; i < N; i++){
            for (int j = 0; j < N; j++){
                H += E(i, j);
            }
        }
        return H;
    }
    void copyLattice(Ising src){
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++) lattice[i][j] = src.lattice[i][j];
    }
    Ising newState(){
        Ising upd = new Ising(N, J, h, 0.5);
        upd.copyLattice(this);
        int neighbor = rnd.nextInt(4);
        switch (neighbor){
            case 0:
                iCurrent = (iCurrent - 1 + N) % N;
                break;
            case 1:
                iCurrent = (iCurrent + 1) % N;
                break;
            case 2:
                jCurrent = (jCurrent - 1 + N) % N;
                break;
            case 3:
                jCurrent = (jCurrent + 1) % N;
                break;
        }
        upd.lattice[iCurrent][jCurrent] *= -1;
        delta = -2 * E(iCurrent, jCurrent);
        return upd;
    }
}

class Gui {
    private Ising M;
    private int N = 100, width, height;
    private JFrame frame;
    private JPanel mainPanel;
    private boolean toggleSimulation;
    class SpacebarListener implements KeyListener{
        public void keyTyped(KeyEvent e){}
        public void keyReleased(KeyEvent e){}
        public void keyPressed(KeyEvent e){
            if(e.getKeyCode() == KeyEvent.VK_SPACE) toggleSimulation = !toggleSimulation;
        }
    }
    class DisplayGraphics extends JComponent {
        DisplayGraphics(){
            width = mainPanel.getWidth();
            height = mainPanel.getHeight();
            setPreferredSize(new Dimension(width, height));
        }
        public void paintComponent(Graphics g) {
            int scale = (int)Math.round(Math.sqrt((double)(width * height / (N * N))));
            width = height = scale * N;
            mainPanel.setSize(new Dimension(width, height));
            setPreferredSize(new Dimension(width, height));
            frame.setSize(new Dimension(width, height));
            super.paintComponent(g);
            g.setColor(new Color(198, 200, 238));
            g.fillRect(0, 0, width, height);
            for(int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    g.setColor(new Color(154, 149, 183));
                    if(M.lattice[i][j] == 1) g.fillRect(i * scale, j * scale, scale, scale);
                }
            }
            double Emax = (4 + Math.abs(M.h)) * N * N, Ecurrent = M.Hamiltonian(), Enormalized = Ecurrent / Emax;
            int alpha = 200;
            Color textbg = new Color(163, 11, 55, alpha);
            g.setColor(textbg);
            g.fillRect(0, height - 70, width, 50);
            g.setColor(new Color(252, 252, 255));
            g.setFont(new Font("Helvetica Neue", Font.BOLD, 16));
            g.drawString("H: " + Enormalized,10,height - 40);
        }
    }

    void start() {
        frame = new JFrame();
        mainPanel = new JPanel(new GridLayout());
        mainPanel.setSize(new Dimension(500, 500));
        mainPanel.add(new DisplayGraphics());
        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
// Start simulation
        int J = 1;
        double T = 1.0, delta, H = 0;
        M = new Ising(N, J, H, 0.5);
        Ising tempState = new Ising(N, J, H, 0.5);
        toggleSimulation = true;
        frame.addKeyListener(new SpacebarListener());
        while (true) {
            while (!toggleSimulation){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (toggleSimulation) break;
            }
            delta = M.delta;
            if (delta < 0) M.copyLattice(tempState);
            else if (M.rnd.nextDouble() < Ising.getProbability(delta, T)) M.copyLattice(tempState);
            tempState.copyLattice(M.newState());
            frame.repaint();
        }
    }
}

class Main{
    public static void main(String[] args) {
        Gui GUI = new Gui();
        GUI.start();
    }
}