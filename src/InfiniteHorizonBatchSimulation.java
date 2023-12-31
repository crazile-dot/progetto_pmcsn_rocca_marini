import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class InfiniteHorizonBatchSimulation {

    static final double LOC = 0.95;    /* level of confidence,        */
    static int batchSize = 32;   //b
    static int numBatches = 64;   //k
    static Path batchMeansFile = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\batches.txt");


    public static void main(String[] args) throws IOException {

        String dataFilePath = "C:\\Users\\Ilenia\\Desktop\\valori\\responseFF.txt"; // Sostituisci con il percorso del tuo file di dati

        Queue<Double> responseTimes = readResponseTimesFromFile(dataFilePath);
        System.out.println("DIMENSIONE QUEUE: " + responseTimes.size());
        /*ArrayList<Double> response = new ArrayList<>();
        for (Double d: responseTimes) {
            if (d == null) {
                System.out.println("NULL");
            } else {
                response.add(d);
            }
        }
        System.out.println("DIMENSIONE ARRAY: " + response.size());*/
        String out = "";

        //generateRandomValuesAndSaveToFile(batchSize*numBatches, dataFilePath);
        /*ArrayList<Double> data = new ArrayList<>();
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\output.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                data.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }*/
        // Inizia la simulazione a orizzonte infinito
        int size = responseTimes.size();
        int batchIndex = 0;
        //int numBatches = size/batchSize;
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

            //System.out.println("\n");
            //System.out.println("Lunghezza batch: " + batch.length);
            //int count = 0;
            //for (double elem: batch) {
                //System.out.println("Batch[" + count + "]: " + elem);
               // count++;
            //}
            batches[batchIndex] = batch;
            batchIndex++;
            i += batchSize;

            // Pausa tra le iterazioni (adattare il ritmo di elaborazione ai tuoi dati)
            /*try {
                Thread.sleep(1000); // Attendi per 1 secondo tra le elaborazioni
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
        /*System.out.println("Numero di batch: " + batches.length);
        System.out.println("\n");*/

        double [] batchMeans = new double[numBatches];
        // parto da k=1 perché scarto il primo batch perché questo è influenzato dalle condizioni iniziali
        for (int k = 1; k < batches.length; k++) {
            double mean = calculateAverage(batches[k]);
            batchMeans[k] = mean;
            out += mean+"\n";
        }
        Files.writeString(batchMeansFile, out);

        /*List<Double> autoc = autocorrelation(Arrays.stream(batchMeans)
                .boxed()
                .collect(Collectors.toList()), batchSize);
        for (double elem: autoc) {
            System.out.println(elem);
        }*/
        //System.out.println("numBatches = " + numBatches);
        //estimate(batchMeans);
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
                    if (line != "") {
                        double responseTime = Double.parseDouble(line);
                        responseTimes.add(responseTime);
                    }
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

    public static List<Double> autocorrelation(List<Double> data, int b) {
        /*int[] lags = new int[11];
        for (int l = 0; l < 11; l++) {
            lags[l] = l;
        }*/
        //double[] autocorrelation = calculateAutocorrelation(data);

        List<Double> autocorrelation = acs(data, b);


        //System.out.println("Batch size = " + batchSize);
        //autocorrelation[1] = 0.4;
        /*for (double elem : autocorrelation)
            System.out.println(elem);
        while (autocorrelation[1] > 0.2) {
            batchSize = batchSize * 2;
            autocorrelation = calculateAutocorrelation(data, batchSize, lags);
            System.out.println("\n");
            System.out.println("Batch size = "  + batchSize);
            for (double elem : autocorrelation)
                System.out.println(elem);
        }*/
        return autocorrelation;
    }

    public static List<Double> acs(List<Double> batchmeansList, int b) {
        int K = 50;  // K is the maximum lag
        int SIZE = K + 1;
        int i = 0;  // data point index
        double sum = 0.0;  // sums x[i]
        int j = 0;  // lag index
        List<Double> hold = new ArrayList<>(SIZE);  // K + 1 most recent data points
        int p = 0;  // points to the head of 'hold'
        double[] cosum = new double[SIZE];  // cosum[j] sums x[i] * x[i+j]

        while (i < SIZE) {  // initialize the hold array with
            double x = batchmeansList.remove(0);  // the first K + 1 data values
            sum += x;
            hold.add(x);
            i++;
        }

        if (!batchmeansList.isEmpty()) {
            double x = batchmeansList.remove(0);
        }

        while (!batchmeansList.isEmpty()) {
            for (j = 0; j < SIZE; j++) {
                cosum[j] += hold.get(p) * hold.get((p + j) % SIZE);
            }
            double x = batchmeansList.remove(0);
            sum += x;
            hold.set(p, x);
            p = (p + 1) % SIZE;
            i++;

            if (batchmeansList.isEmpty()) {
                x = 0.0;
            }
        }
        int n = i;  // the total number of data points

        while (i < n + SIZE) {  // empty the circular array
            for (j = 0; j < SIZE; j++) {
                cosum[j] += hold.get(p) * hold.get((p + j) % SIZE);
            }
            hold.set(p, 0.0);
            p = (p + 1) % SIZE;
            i++;
        }

        double mean = sum / n;
        List<Double> autocorrelations = new ArrayList<>(SIZE);
        for (j = 0; j < K + 1; j++) {
            autocorrelations.add(cosum[j] / cosum[0]);
        }

        return autocorrelations;
    }


    //public static double[] calculateAutocorrelation(double[] data, int batchSize, int[] lags) {
        /*int dataSize = data.length;
        int numBatches = dataSize / batchSize;
        int remainder = dataSize % batchSize;*/

        // Pre-allocate autocorrelation table
       /* double[] autocorrelation = new double[lags.length];

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
    }*/

    public static double[] calculateAutocorrelation(double[] data) {
        int n = data.length;
        double[] autocorrelation = new double[n];

        // Calcola la media dei dati
        double mean = 0.0;
        for (double value : data) {
            mean += value;
        }
        mean /= n;

        // Calcola la varianza dei dati
        double variance = 0.0;
        for (double value : data) {
            variance += Math.pow(value - mean, 2);
        }
        variance /= n;

        // Calcola l'autocorrelazione per ogni lag
        for (int lag = 0; lag < n; lag++) {
            double sum = 0.0;
            for (int i = 0; i < n - lag; i++) {
                sum += (data[i] - mean) * (data[i + lag] - mean);
            }
            autocorrelation[lag] = sum / ((n - lag) * variance);
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


