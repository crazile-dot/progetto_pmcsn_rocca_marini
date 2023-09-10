import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class TEMP {

    public static void main (String args[]) throws IOException {
        ffResponse();
        nResponse();
    }


    public static void nResponse() throws IOException {
        ArrayList<Double> ressponseBiglN = new ArrayList<>();
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\valori\\responseBiglietteriaN.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                ressponseBiglN.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }

        ArrayList<Double> ressponseCheckN = new ArrayList<>();
        line = "";
        br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\valori\\responseCheckinN.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                ressponseCheckN.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }

        ArrayList<Double> ressponseScannN = new ArrayList<>();
        line = "";
        br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\valori\\responseScannerN.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                ressponseScannN.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }

        ArrayList<Double> ressponseSecN = new ArrayList<>();
        line = "";
        br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\valori\\responseSecurityN.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                ressponseSecN.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }

        ArrayList<Double> ressponseSec2N = new ArrayList<>();
        line = "";
        br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\valori\\responseSecurity2N.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                ressponseSec2N.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }

        ArrayList<Double> ressponseImbN = new ArrayList<>();
        line = "";
        br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\valori\\responseImbarcoN.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                ressponseImbN.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }

        double[] responses = new double[InfiniteHorizonBatchSimulation.batchSize * InfiniteHorizonBatchSimulation.numBatches];
        for (int i = 0; i < responses.length; i++) {
            responses[i] = -1;
        }
        for (int i = 0; i < ressponseBiglN.size(); i++) {
            responses[i] = ressponseBiglN.get(i);
        }
        for (int i = 0; i < ressponseCheckN.size(); i++) {
            if (responses[i] != -1)
                responses[i] += ressponseCheckN.get(i);
            else
                responses[i] = ressponseCheckN.get(i);
        }
        for (int i = 0; i < ressponseScannN.size(); i++) {
            if (responses[i] != -1)
                responses[i] += ressponseScannN.get(i);
            else
                responses[i] = ressponseScannN.get(i);
        }
        for (int i = 0; i < ressponseSecN.size(); i++) {
            if (responses[i] != -1)
                responses[i] += ressponseSecN.get(i);
            else
                responses[i] = ressponseSecN.get(i);
        }
        for (int i = 0; i < ressponseSec2N.size(); i++) {
            if (responses[i] != -1)
                responses[i] += ressponseSec2N.get(i);
            else
                responses[i] = ressponseSec2N.get(i);
        }
        for (int i = 0; i < ressponseImbN.size(); i++) {
            if (responses[i] != -1)
                responses[i] += ressponseImbN.get(i);
            else
                responses[i] = ressponseImbN.get(i);
        }

        for (int i = 0; i < responses.length; i++) {
            if (responses[i] == -1) {
                responses[i] = responses[i/2 - 100];
            }
        }

        System.out.println(responses);

        Path responseN = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseN.txt");
        String out = "";
        for (double elem: responses) {
            out += elem + "\n";
        }
        Files.writeString(responseN, out);
    }

    public static void ffResponse() throws IOException {
        ArrayList<Double> ressponseBiglFF = new ArrayList<>();
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\valori\\responseBiglietteriaFF.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                ressponseBiglFF.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }

        ArrayList<Double> ressponseCheckFF = new ArrayList<>();
        line = "";
        br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\valori\\responseCheckinFF.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                ressponseCheckFF.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }

        ArrayList<Double> ressponseScannFF = new ArrayList<>();
        line = "";
        br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\valori\\responseScannerFF.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                ressponseScannFF.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }

        ArrayList<Double> ressponseSecFF = new ArrayList<>();
        line = "";
        br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\valori\\responseSecurityFF.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                ressponseSecFF.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }
        System.out.println(ressponseSecFF.size());

        ArrayList<Double> ressponseSec2FF = new ArrayList<>();
        line = "";
        br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\valori\\responseSecurity2FF.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                ressponseSec2FF.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }

        ArrayList<Double> ressponseImbFF = new ArrayList<>();
        line = "";
        br = new BufferedReader(new FileReader("C:\\Users\\Ilenia\\Desktop\\valori\\responseImbarcoFF.txt"));
        line = br.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                ressponseImbFF.add(Double.parseDouble(tokenizer.nextToken()));
            }
            line = br.readLine();
        }

        double[] responses = new double[InfiniteHorizonBatchSimulation.batchSize * InfiniteHorizonBatchSimulation.numBatches];
        for (int i = 0; i < responses.length; i++) {
            responses[i] = -1;
        }
        for (int i = 0; i < ressponseBiglFF.size(); i++) {
            responses[i] = ressponseBiglFF.get(i);
        }
        for (int i = 0; i < ressponseCheckFF.size(); i++) {
            if (responses[i] != -1)
                responses[i] += ressponseCheckFF.get(i);
            else
                responses[i] = ressponseCheckFF.get(i);
        }
        for (int i = 0; i < ressponseScannFF.size(); i++) {
            System.out.println(ressponseScannFF.size());
            System.out.println(responses.length);
            if (responses[i] != -1)
                responses[i] += ressponseScannFF.get(i);
            else
                responses[i] = ressponseScannFF.get(i);
        }
        for (int i = 0; i < ressponseSecFF.size(); i++) {
            if (responses[i] != -1)
                responses[i] += ressponseSecFF.get(i);
            else
                responses[i] = ressponseSecFF.get(i);
        }
        for (int i = 0; i < ressponseSec2FF.size(); i++) {
            if (responses[i] != -1)
                responses[i] += ressponseSec2FF.get(i);
            else
                responses[i] = ressponseSec2FF.get(i);
        }
        for (int i = 0; i < ressponseImbFF.size(); i++) {
            if (responses[i] != -1)
                responses[i] += ressponseImbFF.get(i);
            else
                responses[i] = ressponseImbFF.get(i);
        }

        /*for (int i = 0; i < responses.length; i++) {
            if (responses[i] == -1) {
                responses[i] = responses[i/2 - 100];
            }
        }*/

        Path responseFF = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseFF.txt");
        String out = "";
        for (double elem: responses) {
            if (elem != -1)
                out += elem + "\n";
        }
        Files.writeString(responseFF, out);
    }

}
