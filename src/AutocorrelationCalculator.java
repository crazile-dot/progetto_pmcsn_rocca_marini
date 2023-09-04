import java.util.Arrays;

public class AutocorrelationCalculator {
    public static void main(String[] args) {
        // Definisci i dati su cui calcolare l'autocorrelazione (questo è solo un esempio).
        double[] data = {1.55859859, 1.084953838, 1.47836829, 1.093583748, 1.23245464, 1.12302934, 0.999778390, 1.234232245, 1.002324233, 1.12132342, 1.082939234, 1.1231434253, 1.343324131, 1.00093227382, 1.012131423, 1.21113223, 1.122324235, 1.05893002, 1.3434221, 1.322498889};

        // Definisci il lag massimo e il batch size.
        int maxLag = 10;
        int batchSize = 32;
        double[] autcors = new double[maxLag + 1];
        autcors[1] = 10000;

        while (autcors[1] > 0.2) {
            // Calcola l'autocorrelazione per ogni lag.
            for (int lag = 0; lag <= maxLag; lag++) {
                double[] subData1 = Arrays.copyOfRange(data, 0, batchSize);
                double[] subData2 = Arrays.copyOfRange(data, lag, lag + batchSize);

                // Calcola l'autocorrelazione per il lag corrente.
                double autocorrelation = calculateAutocorrelation(subData1, subData2);
                autcors[lag] = autocorrelation;

                //System.out.println("Autocorrelation at lag " + lag + ": " + autocorrelation);
            }
            System.out.println("\n");
            System.out.println("Batch size = " + batchSize);
            for (double elem : autcors)
                System.out.println(elem);

            batchSize = batchSize * 2;
        }
    }

    // Funzione per calcolare l'autocorrelazione tra due serie di dati.
    private static double calculateAutocorrelation(double[] data1, double[] data2) {
        // Implementa il calcolo dell'autocorrelazione qui.
        // Questa è solo una rappresentazione di base.
        double result = 0.0;
        int n = data1.length;

        for (int i = 0; i < n; i++) {
            result += data1[i] * data2[i];
        }

        return result;
    }
}
