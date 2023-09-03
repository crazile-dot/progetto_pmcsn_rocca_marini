import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Autocorrelation {

    private static int batchSize = 32;

    public static void main(String args[]) throws IOException {
        double[] data;
        String line1 = "";
        String line2 = "";

        int[] lags = new int[11];
        for (int l = 0; l < 11; l++) {
            lags[l] = l;
        }

        try {
            BufferedReader br1 = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\prova.txt"));
            line1 = br1.readLine();
            int j = 0;
            while (line1 != null) {        /* use Welford's one-pass method */
                StringTokenizer tokenizer = new StringTokenizer(line1);
                if (tokenizer.hasMoreTokens()) {
                    j++;
                }
                line1 = br1.readLine();
            }

            data = new double[j];

            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\prova.txt"));
            line2 = br.readLine();
            int q = 0;
            while (line2 != null) {         /* use Welford's one-pass method */
                StringTokenizer tokenizer = new StringTokenizer(line2);
                if (tokenizer.hasMoreTokens()) {
                    data[q] = Double.parseDouble(tokenizer.nextToken());
                    q++;

                }
                line2 = br.readLine();
            }


            double[] autocorrelation = calculateAutocorrelation(data, batchSize);
            System.out.println("Batch size = " + batchSize);
            for (double elem : autocorrelation)
                System.out.println(elem);
            while (autocorrelation[1] > 0.2) {
                batchSize = batchSize * 2;
                autocorrelation = calculateAutocorrelation(data, batchSize);
                System.out.println("\n");
                System.out.println("Batch size = "  + batchSize);
                for (double elem : autocorrelation)
                    System.out.println(elem);
            }


            // Pre-allocate autocorrelation table
            /*double[] acorr = new double[lags.length];

            // Mean
            double sum = 0;
            for (double value : data) {
                sum += value;
            }
            double mean = sum / data.length;

            // Variance
            double var = 0;
            for (double value : data) {
                var += Math.pow(value - mean, 2);
            }
            var /= data.length;

            // Normalized data
            double[] ndata = new double[data.length];
            for (int i = 0; i < data.length; i++) {
                ndata[i] = data[i] - mean;
            }

            // Go through lag components one-by-one
            for (int l = 0; l < lags.length; l++) {
                double c = 1; // Self correlation

                if (lags[l] > 0) {
                    double[] tmp = new double[data.length - l];
                    for (int i = 0; i < data.length - l; i++) {
                        tmp[i] = ndata[i + l] * ndata[i];
                    }

                    double sumTmp = 0;
                    for (double value : tmp) {
                        sumTmp += value;
                    }
                    c = sumTmp / data.length / var;
                }

                acorr[l] = c;
            }
            for (double elem : acorr)
                System.out.println(elem);*/

        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

    }

    public static double[] calculateAutocorrelation(double[] data, int batchSize) {
        int dataSize = data.length;
        int numBatches = dataSize / batchSize;
        int remainder = dataSize % batchSize;

        // Pre-allocate autocorrelation table
        double[] autocorrelation = new double[batchSize];

        // Calculate mean and variance for the entire data
        double mean = calculateMean(data);
        double variance = calculateVariance(data, mean);

        for (int lag = 0; lag < 11; lag++) {
            double c = 0;

            for (int batch = 0; batch < numBatches; batch++) {
                for (int i = 0; i < batchSize - lag; i++) {
                    c += (data[batch * batchSize + i] - mean) * (data[batch * batchSize + i + lag] - mean);
                }
            }

            // Handle remainder data if present
            if (remainder > 0) {
                for (int i = 0; i < remainder - lag; i++) {
                    c += (data[numBatches * batchSize + i] - mean) * (data[numBatches * batchSize + i + lag] - mean);
                }
            }

            autocorrelation[lag] = c / (dataSize * variance);
        }

        return autocorrelation;
    }

    public static double calculateMean(double[] data) {
        double sum = 0;
        for (double value : data) {
            sum += value;
        }
        return sum / data.length;
    }

    public static double calculateVariance(double[] data, double mean) {
        double variance = 0;
        for (double value : data) {
            variance += Math.pow(value - mean, 2);
        }
        return variance / data.length;
    }
}


        /*String line1 = "";
        String line2 = "";
        double mean;
        double var;
        double sum = 0.0;
        double sum2 = 0.0;
        double c = 0.0;

        int [] lags = new int[11];
        for (int l = 0; l < 11; l++) {
            lags[l] = l;
        }
        double [] data;
        double [] acorr = new double[lags.length];
        for (double i : acorr) {
            i = 0;
        }
        double [] ndata;

        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\prova.txt"));
        try {
            line1 = br.readLine();
            int j = 0;
            while (line1 != null) {  */       /* use Welford's one-pass method */
        /*   StringTokenizer tokenizer = new StringTokenizer(line1);
                if (tokenizer.hasMoreTokens()) {
                    j++;
                }
                line1 = br.readLine();
            }

            System.out.println("Stampa 1");
            data = new double[j];
            line2 = br.readLine();
            int i = 0;
            while (line2 != null) { */        /* use Welford's one-pass method */
              /*  StringTokenizer tokenizer = new StringTokenizer(line2);
                if (tokenizer.hasMoreTokens()) {
                    data[j] = Double.parseDouble(tokenizer.nextToken());
                    i++;

                }
                line2 = br.readLine();
            }


            sum = Arrays.stream(data).sum();
            mean = sum/data.length;
            System.out.println("Stampa 2");


            for (double d : data) {
                double tmp = Math.pow((d - mean), 2);
                sum2 += tmp;
            }
            var = sum2/data.length;

            int k = 0;
            ndata = new double[data.length];
            for (double d : data) {
                double val = d - mean;
                ndata[k] = val;
                k++;
            }

            for (int l : lags) {
                c = 1.0;
                if (l > 0) {
                    double [] ttmmpp = new double[data.length - l];
                    for (int n = 0; n < data.length - l; n++) {
                        double tmp = ndata[l + n] * ndata[ndata.length - l + n];
                        ttmmpp[n] = tmp;
                    }
                    c = Arrays.stream(ttmmpp).sum() / data.length / var;

                }
                acorr[l] = c;
            }

            System.out.println(acorr);

        } catch(IOException e){
            System.err.println(e);
            System.exit(1);

        }*/



