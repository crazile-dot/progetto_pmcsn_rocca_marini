import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

public class FiniteHorizonSimulation {

    private static int jobNum = 10;
    static final double LOC = 0.95;    /* level of confidence,        */


    public static void main(String[] args) throws IOException {
        String dataFilePath = "C:\\Users\\Ilenia\\Desktop\\prova.txt"; // Sostituisci con il percorso del tuo file di dati

        Queue<Double> responseTimes = readResponseTimesFromFile(dataFilePath);
        ArrayList<Double> data = new ArrayList<>();
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\prova.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                data.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }

        double [] replica = new double[jobNum];
        for (int i = 0; i < jobNum; i++) {
            replica[i] = data.get(i);
        }
        estimate(replica);
    }

    // Funzione per leggere i tempi di risposta da un file
    public static Queue<Double> readResponseTimesFromFile(String filePath) {
        Queue<Double> responseTimes = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    double responseTime = Double.parseDouble(line);
                    responseTimes.add(responseTime);
                } catch (NumberFormatException e) {
                    // Ignora le righe non valide nel file
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseTimes;
    }

    public static void estimate(double[] dataList) {
        long   n    = 0;                     /* counts data points */
        double sum  = 0.0;
        double mean = 0.0;
        double stdev;
        double u, t, w;
        double diff;

        String line = "";

        Rvms rvms = new Rvms();

        for (double data: dataList) {
            n++;                 /* and standard deviation        */
            //System.out.println(n);

            diff  = data - mean;
            sum  += diff * diff * (n - 1.0) / n;
            mean += diff / n;
        }
        stdev  = Math.sqrt(sum / n);

        DecimalFormat df = new DecimalFormat("###0.00");

        if (n > 1) {
            u = 1.0 - 0.5 * (1.0 - LOC);              /* interval parameter  */
            t = rvms.idfStudent(n - 1, u);            /* critical value of t */
            w = t * stdev / Math.sqrt(n - 1);         /* interval half width */

            System.out.print("\nbased upon " + n + " data points");
            System.out.print(" and with " + (int) (100.0 * LOC + 0.5) +
                    "% confidence\n");
            System.out.print("the expected value is in the interval ");
            System.out.print( df.format(mean) + " +/- " + df.format(w) + "\n");
        }
        else{
            System.out.print("ERROR - insufficient data\n");
        }
    }
}
