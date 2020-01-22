import javax.swing.*;
import java.awt.*;
import java.text.CollationKey;
import java.util.Random;

class Ising{
    int [][] lattice;
    int N, iCurrent, jCurrent;
    double h, J;
    Random rnd;
    public Ising(int n, double j_, double h_, double positivePercentage){
        lattice = new int[n][n];
        N = n;
        h = h_;
        J = j_;
        rnd = new Random();
        generateLattice(positivePercentage);
        iCurrent = rnd.nextInt(N);
        jCurrent = rnd.nextInt(N);
    }
    void generateLattice(double positivePercentage){
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
    double E (int i, int j){
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
                upd.lattice[iCurrent][jCurrent] *= -1;
                break;
            case 1:
                iCurrent = (iCurrent + 1) % N;
                upd.lattice[iCurrent][jCurrent] *= -1;
                break;
            case 2:
                jCurrent = (jCurrent - 1 + N) % N;
                upd.lattice[iCurrent][jCurrent] *= -1;
                break;
            case 3:
                jCurrent = (jCurrent + 1) % N;
                upd.lattice[iCurrent][jCurrent] *= -1;
                break;
        }
        //upd.lattice[rnd.nextInt(N)][rnd.nextInt(N)] *= -1;
        return upd;
    }
}

class Gui {
    Ising M, tempState;
    int N = 100, J = 1, scale, width, height;
    double H = 0;
    JFrame frame;
    class DisplayGraphics extends JComponent {
        public void paintComponent(Graphics g) {
            width = frame.getWidth();
            height = frame.getHeight();
            scale = (int)Math.round(Math.sqrt((double)width * height / (N * N)) + 0.5);
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

    public void start() {
        frame = new JFrame();
        JPanel mainPanel = new JPanel(new FlowLayout());
        DisplayGraphics displayState = new DisplayGraphics();
        mainPanel.add(displayState);
        frame.setSize(500, 500);
        //frame.setResizable(false);
        frame.add(new DisplayGraphics());
        frame.setVisible(true);
// Start simulation
        M = new Ising(N, J, H, 0.5);
        tempState = new Ising(N, J, H, 0.5);
        double T = 1.0, delta, ECurrent;
        while (true) {
            ECurrent = M.Hamiltonian();
            delta = tempState.Hamiltonian() - ECurrent;
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