import javax.swing.*;
import java.awt.*;
import java.util.Random;

class Ising{
    int [][] lattice;
    int N;
    double h, J;
    Random rnd;
    public Ising(int n, double j_, double h_){
        lattice = new int[n][n];
        N = n;
        h = h_;
        J = j_;
        rnd = new Random();
        generateLattice(0.5);
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
        return -J * lattice[i][j] * neighbors / 2 - h * lattice[i][j];
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
        Ising upd = new Ising(N, J, h);
        upd.copyLattice(this);
        upd.lattice[rnd.nextInt(N)][rnd.nextInt(N)] *= -1;
        return upd;
    }
}

class Gui {
    Ising M, tempState;
    int N = 100, J = 1, scale = 5;
    double H = 0;
    class DisplayGraphics extends JComponent {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.WHITE);
            for(int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if(M.lattice[i][j] == 1) g.fillRect(i * scale, j * scale, scale, scale);
                }
            }
        }
    }

    public void start() {
        final JFrame frame = new JFrame();
        JPanel mainPanel = new JPanel(new FlowLayout());
        DisplayGraphics displayState = new DisplayGraphics();
        mainPanel.add(displayState);
        frame.setSize(500, 520);
        frame.add(new DisplayGraphics());
        frame.setVisible(true);
// Start simulation
        M = new Ising(N, J, H);
        tempState = new Ising(N, J, H);
        double T = 1.0, delta, ECurrent = 0;
        while (true) {
            ECurrent = M.Hamiltonian();
            delta = tempState.Hamiltonian() - ECurrent;
            if (delta < 0) M.copyLattice(tempState);
            else if (M.rnd.nextDouble() < M.getProbability(delta, T))
                M.copyLattice(tempState);
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