import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;

public class InfiniteHorizonBatchSimulation {

    static final double LOC = 0.95;    /* level of confidence,        */
    static int batchSize = 32;   //b
    static int numBatches = 64;   //k
    static Path batchMeansFile = Path.of("C:\\Users\\Adriano\\Desktop\\valori\\batches.txt");


    public static void main(String[] args) throws IOException {
      /*  String dataFilePath = "C:\\Users\\Adriano\\Desktop\\prova.txt"; // Sostituisci con il percorso del tuo file di dati

        Queue<Double> responseTimes = readResponseTimesFromFile(dataFilePath);

        String out = "";

        //generateRandomValuesAndSaveToFile(batchSize*numBatches, dataFilePath);
        ArrayList<Double> data = new ArrayList<>();
        String line = "";
        //BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Adriano\\Desktop\\output.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                data.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }
        // Inizia la simulazione a orizzonte infinito
        int size = data.size();
        int batchIndex = 0;
        int numBatches = size/batchSize;
        int i = 0;
        double [] batch;
        double [][] batches = new double[numBatches][batchSize];
        while (i < size) {
            batch = new double[batchSize];
            for (int j = 0; j < batchSize; j++) {
                if (!responseTimes.isEmpty()) {
                    batch[j] = responseTimes.poll();
                } else {
                    System.out.println("Nessun altro dato disponibile. Attendo nuovi dati...");
                    break;
                }

                // Qui puoi inserire la logica per elaborare il batch, ad esempio calcolare la media
                //double batchMean = calculateBatchMean(batch);
            }
            /*System.out.println("\n");
            System.out.println("Lunghezza batch: " + batch.length);
            int count = 0;
            for (double elem: batch) {
                System.out.println("Batch[" + count + "]: " + elem);
                count++;
            }*//*
            batches[batchIndex] = batch;
            batchIndex++;
            i += batchSize;

            // Pausa tra le iterazioni (adattare il ritmo di elaborazione ai tuoi dati)
            /*try {
                Thread.sleep(1000); // Attendi per 1 secondo tra le elaborazioni
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        /*System.out.println("Numero di batch: " + batches.length);
        System.out.println("\n");*/
/*
        double [] batchMeans = new double[numBatches];
        // parto da k=1 perché scarto il primo batch perché questo è influenzato dalle condizioni iniziali
        for (int k = 1; k < batches.length; k++) {
            double mean = calculateAverage(batches[k]);
            batchMeans[k] = mean;
            out += mean+"\n";
        }
        Files.writeString(batchMeansFile, out);

        autocorrelation(batchMeans);*/
        //System.out.println("numBatches = " + numBatches);
        //estimate(batchMeans);*/
    }

    public static double calculateAverage(double[] values) {
        if (values.length == 0) {
            return 0.0; // Gestione dell'array vuoto (puoi scegliere un altro valore di default)
        }

        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }

        return sum / values.length;
    }


    public static void generateRandomValuesAndSaveToFile(int n, String fileName) {
        Random rand = new Random();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < n; i++) {
                double randomValue = 1 + (rand.nextDouble() * 0.5); // Genera un valore tra 1 e 1.5
                writer.write(String.valueOf(randomValue));
                writer.newLine(); // Aggiungi una nuova riga per ciascun valore
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Valori casuali generati e salvati in " + fileName);
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

    // Funzione per calcolare la media di un batch
    public static double calculateBatchMean(double[] batch) {
        double sum = 0;
        for (double responseTime : batch) {
            sum += responseTime;
        }
        return sum / batch.length;
    }

    public static void estimate(double[] batchMeans) {
        long   n    = 0;                     /* counts data points */
        double sum  = 0.0;
        double mean = 0.0;
        double stdev;
        double u, t, w;
        double diff;

        String line = "";

        Rvms rvms = new Rvms();

        for (double data: batchMeans) {
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

    public static void autocorrelation(double[] data) {
        int[] lags = new int[11];
        for (int l = 0; l < 11; l++) {
            lags[l] = l;
        }
        double[] autocorrelation = calculateAutocorrelation(data, batchSize, lags);
        System.out.println("Batch size = " + batchSize);
        //autocorrelation[1] = 0.4;
        for (double elem : autocorrelation)
            System.out.println(elem);
        while (autocorrelation[1] > 0.2) {
            batchSize = batchSize * 2;
            autocorrelation = calculateAutocorrelation(data, batchSize, lags);
            System.out.println("\n");
            System.out.println("Batch size = "  + batchSize);
            for (double elem : autocorrelation)
                System.out.println(elem);
        }
    }

    public static double[] calculateAutocorrelation(double[] data, int batchSize, int[] lags) {
        int dataSize = data.length;
        int numBatches = dataSize / batchSize;
        int remainder = dataSize % batchSize;

        // Pre-allocate autocorrelation table
        double[] autocorrelation = new double[lags.length];

        // Calculate mean and variance for the entire data
        double mean = calculateMean(data);
        double variance = calculateVariance(data, mean);

        for (int lag = 0; lag < lags.length; lag++) {
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


