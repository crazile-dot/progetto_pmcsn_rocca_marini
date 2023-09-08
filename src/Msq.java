import java.io.IOException;
import java.lang.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.*;


class MsqT {
    double current;                   /* current time                       */
    double next;                      /* next (most imminent) event time    */
}

class MsqSum {                      /* accumulated sums of                */
    double service;                   /*   service times                    */
    long   served;                    /*   number served                    */
}

class MsqEvent{                     /* the next-event list    */
    double t;                         /*   next event time      */
    int    x;                         /*   event status, 0 or 1 */
    int priority;                   // frequent flyer= 1 ; normale=0
    int passenger_type;
}


class Msq {
    static double START   = 0.0;            /* initial (open the door)        */
    static double STOP    = 20.0;        /* terminal (close the door) time  20000.0; */
    static int    SERVERS = 4;              /* number of servers              */

    static double LAMBDA;
    static double SERVICE;
    static double fasciaOraria = MMValues.fasciaOraria1;

    static int SERVER_DEDICATO=1; /* SERVER DEDICATO per i frequent flyers */
    static double sarrival = START;
    static int[] number_queues={0,0};
    static int[] number_queues_checkin={0,0};
    static int[] number_queues_security={0,0};
    static int[] number_queues_imbarco={0,0,0};
    static int number_queues_security_app=0;
    static long[] number_nodes={0,0,0,0,0,0,0,0,0,0,0};
    static long   number = 0;
    public static void main(String[] args) throws IOException {

        /*= l(t)     number in the node     */
        int    e;                      /* next event index                   */
        int    s;                      /* server index                       */
        long   index  = 0;             /* used to count processed jobs       */
        double area   = 0.0;           /* time integrated number in the node */
        double areaBiglietteria = 0.0;
        double areaBiglietteriaQueue = 0.0;
        double areaBiglietteriaDedicata=0.0;
        double areaCheckin = 0.0;
        double areaCheckinQueue = 0.0;
        double areaScansione1 = 0.0;
        double areaScansione1Queue = 0.0;
        double areaScansione2 = 0.0;
        double areaScansione2Queue = 0.0;
        double areaScansione3 = 0.0;
        double areaScansione3Queue = 0.0;
        double areaScansione4 = 0.0;
        double areaScansione4Queue = 0.0;
        double areaScansioneDedicata1 = 0.0;
        double areaScandioneDedicataQueue1=0.0;
        double areaScansioneDedicata2 = 0.0;
        double areaScandioneDedicataQueue2=0.0;
        double areaSecurity = 0.0;
        double areaControlliApprofonditi = 0.0;
        double areaImbarco = 0.0;
        double service;

        double tBigliett = 0.0;
        double tBigliettD = 0.0;
        double tCheckIn = 0.0;
        double tCheckInD = 0.0;
        double tScann = 0.0;
        double tScannD = 0.0;
        double tSecur = 0.0;
        double tSecurD = 0.0;
        double tSecur2 = 0.0;
        double tSecur2D = 0.0;
        double tGate = 0.0;
        double tGateD = 0.0;


        Msq m = new Msq();
        Rngs r = new Rngs();
        r.plantSeeds(123456789);

        for(int mo=0;mo<2;mo++){
            number_queues[mo]=0;
        }
        MsqEvent [] event = new MsqEvent [1000000];
        MsqSum [] sum = new MsqSum [1000000];
        for (s = 0; s < 1000000; s++) {
            event[s] = new MsqEvent();
            sum [s]  = new MsqSum();
        }

        MsqT t = new MsqT();

        Path fileName = Path.of(
                "C:\\Users\\Adriano\\Desktop\\output.txt");
        String out = "";

        t.current    = START;
        event[0].t   = getArrival(r);
        event[0].x   = 1;
        for (s = 1; s <= 100000; s++) {
            event[s].t     = START;          /* this value is arbitrary because */
            event[s].x     = 0;              /* all servers are initially idle  */
            sum[s].service = 0.0;
            sum[s].served  = 0;
        }
        MultiQueue Queues_security=new MultiQueue(2);
        MultiQueue Queues= new MultiQueue(2);
        MultiQueue Queues_checkin=new MultiQueue(2);
        MultiQueue Queues_imbarco=new MultiQueue(3);
        MultiQueue Queues_security_app=new MultiQueue(1);

        while ((event[0].x != 0) || (number != 0)) {
            System.out.println("***** IL NUMERO DEI JOB NEL SISTEMA E': "+number);
           /* System.out.println("Lista eventi:");
            System.out.println("arrivo:"+event[0].x);
            System.out.println("server biglietteria 1:"+event[1].x);
            System.out.println("server biglietteria 2:"+event[2].x);
            System.out.println("server biglietteria 3:"+event[3].x);
            System.out.println("server biglietteria 4:"+event[4].x);
            System.out.println("server biglietteria dedicato:"+event [5].x);
            System.out.println("server coda check in:"+number_queues[0]);
            System.out.println("server coda check ff in:"+number_queues[1]);
            System.out.println("arrivo:"+event[6].x);
            System.out.println("server check in 1:"+event[7].x);
            System.out.println("server check in 2:"+event[8].x);
            System.out.println("server check in 3:"+event[9].x);
            System.out.println("server check in 4:"+event[10].x);
            System.out.println("server check dedicato:"+event[11].x);
            System.out.println("server coda check in:"+number_queues_checkin[0]);
            System.out.println("server coda check in:"+number_queues_checkin[1]);
            System.out.println("arrivo N 1:"+event[12].x);
            System.out.println("arrivo N 2:"+event[13].x);
            System.out.println("arrivo N 3:"+event[14].x);
            System.out.println("arrivo N 4: "+event[15].x);
            System.out.println("arrivo FF:"+event[16].x);
            System.out.println("arrivo FF:"+event[17].x);
            System.out.println("server carta 1:"+event[19].x);
            System.out.println("server carta in 2:"+event[20].x);
            System.out.println("server carta in 3:"+event[21].x);
            System.out.println("server carta in 4:"+event[22].x);
            System.out.println("server carta dedicato 1:"+event[23].x);
            System.out.println("server carta dedicato 1:"+event[24].x);
            System.out.println("coda  1:"+(number_nodes[6]-1));
            System.out.println("coda  2:"+(number_nodes[7]-1));
            System.out.println("arrivi security:"+event[25].x);
            System.out.println("server security:"+event[26].x);
            System.out.println("server security 2:"+event[27].x);
            System.out.println("server security3:"+event[28].x);
            System.out.println("server security 4:"+event[29].x);
            System.out.println("server security dedicato:"+event[30].x);
            System.out.println("coda security N:" + number_queues_security[0]);
            System.out.println("coda security FF:" + number_queues_security[1]);
            System.out.println("arrivi security:"+event[32].x);
            System.out.println("server imbarco:"+event[33].x);
            System.out.println("server imbarco 2:"+event[34].x);
            System.out.println("server imbarco 3:"+event[35].x);
            System.out.println("server imbarco 4:"+event[36].x);
            System.out.println("server imbarco dedicato:"+event[37].x);
            System.out.println("server imbarco coda ff:"+number_queues_imbarco[1]);
            System.out.println("server coda N2:"+number_queues_imbarco[2]);
            System.out.println("server coda N:"+number_queues_imbarco[0]);*/
            int q=1;
            for (s = 1; s <= MMValues.SERVER_BIGLIETTERIA; s++) {
                System.out.println("server biglietteria  "+q+":"+event[s].x);
                q++;
            }
            System.out.println("coda biglietteria:"+number_queues[0]);
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO; s++) {
                System.out.println("server biglietteria dedicato "+q+":"+event[s].x);
                q++;
            }
            System.out.println("coda biglietteria dedicato:"+number_queues[1]);
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+2; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN; s++) {
                System.out.println("server check in "+q+":"+event[s].x);
                q++;
            }
            System.out.println("coda check in:"+number_queues_checkin[0]);
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO; s++) {
                System.out.println("server check in dedicato "+q+":"+event[s].x);
                q++;
            }
            System.out.println("coda check in dedicato:"+number_queues_checkin[1]);
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+8; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO; s++) {
                System.out.println("server carta imbarco "+q+":"+event[s].x);
                q++;
            }
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO; s++) {
                System.out.println("server carta imbarco dedicato "+q+":"+event[s].x);
                q++;
            }
            System.out.println("coda carta:"+number_nodes[2]);
            System.out.println("coda carta:"+number_nodes[3]);
            System.out.println("coda carta:"+number_nodes[4]);
            System.out.println("coda carta:"+number_nodes[5]);
            System.out.println("coda carta dedicato:"+number_nodes[6]);
            System.out.println("coda carta dedicato:"+number_nodes[7]);

            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY; s++) {
                System.out.println("server security  "+q+":"+event[s].x);
                q++;
            }
            System.out.println("coda security:"+number_queues_security[0]);
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO; s++) {
                System.out.println("server security dedicato  "+q+":"+event[s].x);
                q++;
            }
            System.out.println("coda security dedicato:"+number_queues_security[1]);

            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+3; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO; s++) {

                System.out.println("server imbarco  "+q+":"+event[s].x);

                q++;
            }
            System.out.println("coda imbarco:"+number_queues_imbarco[0]);
            System.out.println("coda imbarco:"+number_queues_imbarco[2]);
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO; s++) {
                System.out.println("server imbarco dedicato "+q+":"+event[s].x);

                q++;

            }
            System.out.println("coda imbarco:"+number_queues_imbarco[1]);
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO+MMValues.SERVER_CONT_APP; s++) {
                System.out.println("server controllo app:"+event[s].x);
            }
            System.out.println("coda controllo app:"+number_queues_security_app);
            //print sul fle
            out+="\n***** IL NUMERO DEI JOB NEL SISTEMA E': "+number;
             q=1;
            for (s = 1; s <= MMValues.SERVER_BIGLIETTERIA; s++) {
                out+="\nserver biglietteria "+q+":"+event[s].x;
                q++;
            }
            out+="\ncoda biglietteria:"+number_queues[0];
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO; s++) {
                out+="\nserver biglietteria dedicato "+q+":"+event[s].x;
                q++;
            }
            out+="\ncoda biglietteria dedicato:"+number_queues[1];
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+2; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN; s++) {
                out+="\nserver check in "+q+":"+event[s].x;
                q++;
            }
            out+="\ncoda check in:"+number_queues_checkin[0];
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO; s++) {
                out+="\nserver check in dedicato "+q+":"+event[s].x;
                q++;
            }
            out+="\ncoda check in dedicato:"+number_queues_checkin[1];
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+8; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO; s++) {
                out+="\nserver carta imbarco "+q+":"+event[s].x;
                q++;
            }
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO; s++) {
                out+="\nserver carta imbarco dedicato "+q+":"+event[s].x;
                q++;
            }
            out+="\ncoda carta:"+number_nodes[2];
            out+="\ncoda carta:"+number_nodes[3];
            out+="\ncoda carta:"+number_nodes[4];
            out+="\ncoda carta:"+number_nodes[5];
            out+="\ncoda carta:"+number_nodes[6];
            out+="\ncoda carta dedicato:"+number_nodes[7];
            out+="\ncoda carta dedicato:"+number_nodes[8];
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY; s++) {
                out+="\nserver security  "+q+":"+event[s].x;
                q++;
            }
            out+="\ncoda security:"+number_queues_security[0];
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO; s++) {
                out+="\nserver security dedicato  "+q+":"+event[s].x;
                q++;
            }
            out+="\ncoda security dedicato:"+number_queues_security[1];

            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+3; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO; s++) {

                out+="\nserver imbarco  "+q+":"+event[s].x;

                q++;
            }
            out+="\ncoda imbarco:"+number_queues_imbarco[0];
            out+="\ncoda imbarco:"+number_queues_imbarco[2];
            q=1;
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO; s++) {
                out+="\nserver imbarco dedicato "+q+":"+event[s].x;

                q++;

            }
            out+=("coda imbarco:"+number_queues_imbarco[1]);
            for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO+MMValues.SERVER_CONT_APP; s++) {
                out+=("\nserver controllo app:"+event[s].x);
            }
            out+=("\ncoda controllo app:"+number_queues_security_app);
            /* out = out + "***** IL NUMERO DEI JOB NEL SISTEMA E': "+number + "\n" +
                     //"          Sto processando l'evento " + e +
                    "            Lista eventi:\n" +
                    "            arrivo: "+event[0].x + "\n" +
                    "            server biglietteria 1: "+event[1].x +"\n" +
                    "            server biglietteria 2: "+event[2].x + "\n" +
                    "            server biglietteria 3: "+event[3].x + "\n" +
                    "            server biglietteria 4: "+event[4].x + "\n" +
                    "            server biglietteria dedicato: "+event [5].x + "\n" +
                    "            server coda check in: "+number_queues[0] + "\n" +
                    "            server coda check ff in: "+number_queues[1] + "\n" +
                    "            arrivo: "+event[6].x + "\n" +
                    "            server check in 1: "+event[7].x + "\n" +
                    "            server check in 2: "+event[8].x + "\n" +
                    "            server check in 3: "+event[9].x + "\n" +
                    "            server check in 4: "+event[10].x + "\n" +
                    "            server check dedicato: "+event[11].x + "\n" +
                    "            server coda check in: "+number_queues_checkin[0] + "\n" +
                    "            server coda check in: "+number_queues_checkin[1] + "\n" +
                    "            arrivo N 1: "+event[12].x + "\n" +
                    "            arrivo N 2: "+event[13].x + "\n" +
                    "            arrivo N 3: "+event[14].x + "\n" +
                    "            arrivo N 4: "+event[15].x + "\n" +
                    "            arrivo FF: "+event[16].x + "\n" +
                    "            arrivo FF: "+event[17].x + "\n" +
                    "            server carta 1: "+event[19].x + "\n" +
                    "            server carta in 2: "+event[20].x + "\n" +
                    "            server carta in 3: "+event[21].x + "\n" +
                    "            server carta in 4: "+event[22].x + "\n" +
                    "            server carta dedicato 1: "+event[23].x + "\n" +
                    "            server carta dedicato 1: "+event[24].x + "\n" +
                    "            coda  1: "+(number_nodes[6]-1) + "\n" +
                    "            coda  2: "+(number_nodes[7]-1) + "\n" +
                    "            arrivi security: "+event[25].x + "\n" +
                    "            server security: "+event[26].x + "\n" +
                    "            server check in 2: "+event[27].x + "\n" +
                    "            server check in 3: "+event[28].x + "\n" +
                    "            server check in 4: "+event[29].x + "\n" +
                    "            server check dedicato: "+event[30].x + "\n" +
                    "            arrivi security: "+event[32].x + "\n" +
                    "            server imbarco: "+event[33].x + "\n" +
                    "            server imbarco 2: "+event[34].x + "\n" +
                    "            server imbarco 3: "+event[35].x + "\n" +
                    "            server imbarco 4: "+event[36].x + "\n" +
                    "            server imbarco dedicato: "+event[37].x + "\n" +
                    "            server imbarco coda ff: "+number_queues_imbarco[1] + "\n" +
                    "            server coda N2: "+number_queues_imbarco[2] + "\n" +
                    "            server coda N:\"+number_queues_imbarco[0]);\n" +
                    "            \n";

*/
            // Writing into the file
           // Files.writeString(fileName, out);



            e         = m.nextEvent(event);                /* next ev1ent index */

            t.next    = event[e].t;                         /* next event time  */

            area     += (t.next - t.current) * number;     /* update integral  */
            areaBiglietteria+=(t.next - t.current) * number_nodes[0];
            if (number_nodes[0]>MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO){
                areaBiglietteriaQueue+=(t.next - t.current) * number_nodes[0];
            }
            areaCheckin+=(t.next - t.current) * number_nodes[1];
            if (number_nodes[1]>SERVERS+SERVER_DEDICATO){
                areaCheckinQueue+=(t.next - t.current) * number_nodes[1];
            }
            areaScansione1+=(t.next - t.current) * number_nodes[2];
            if (number_nodes[2]>1){
                areaScansione1Queue+=(t.next - t.current) * number_nodes[2];
            }
            areaScansione2+=(t.next - t.current) * number_nodes[3];
            if (number_nodes[3]>1){
                areaScansione2Queue+=(t.next - t.current) * number_nodes[3];
            }
            areaScansione3+=(t.next - t.current) * number_nodes[4];
            if (number_nodes[4]>1){
                areaScansione3Queue+=(t.next - t.current) * number_nodes[4];
            }
            areaScansione4+=(t.next - t.current) * number_nodes[5];
            if (number_nodes[5]>1){
                areaScansione4Queue+=(t.next - t.current) * number_nodes[5];
            }

            areaScansioneDedicata1+=(t.next - t.current) * number_nodes[6];
            if (number_nodes[6]>1){
                areaScandioneDedicataQueue1+=(t.next - t.current) * number_nodes[6];
            }
            areaScansioneDedicata2+=(t.next - t.current) * number_nodes[7];
            if (number_nodes[7]>1){
                areaScandioneDedicataQueue2+=(t.next - t.current) * number_nodes[7];
            }
            areaSecurity+=(t.next - t.current) * number_nodes[8];
            if (number_nodes[8]>SERVERS+SERVER_DEDICATO){
                areaCheckinQueue+=(t.next - t.current) * number_nodes[8];
            }
           /* areaImbarco+=(t.next - t.current) * number_nodes[9];
            if (number_nodes[9]>SERVERS+SERVER_DEDICATO){
                areaImbarco+=(t.next - t.current) * number_nodes[9];
            }*/
            t.current = t.next;                            /* advance the clock*/

            if (e == 0) {   /* process an arrival*/
                number++;
                event[0].t = getArrival(r);
                if (event[0].t > STOP)
                    event[0].x = 0;
                r.selectStream(10);
                double rndComp = r.random();
                //System.out.println("Valore Random company: " + rndComp);
                /*if (rndComp > MMValues.singleCompanyPerc) {
                    r.selectStream(2);
                    double rndFF = r.random();
                    if (rndFF > MMValues.FFPercentage) {
                        int i = find_best_node();
                        event[i].x = 1;
                        event[i].t = t.current;
                        event[i].priority = 0;
                        event[i].passenger_type = 0;
                    } else {
                        int i = findOne_controllo_dedicato();
                        event[i].x = 1;
                        event[i].t = t.current;
                        event[i].priority = 1;
                        event[i].passenger_type = 1;
                    }
                } else {*/
                    r.selectStream(4);
                    double rndB = r.random();
                    if (rndB > MMValues.noTktNPercentage + MMValues.noTktFFPercentage) {
                        r.selectStream(8);
                        double rndC = r.random();
                        if (rndC > MMValues.noChkinNPercentage + MMValues.noChkinFFPercentage) {
                            r.selectStream(2);
                            double rndFF = r.random();
                            if (rndFF > MMValues.FFPercentage) {
                                int i = find_best_node();
                                event[i].x = 1;
                                event[i].t = t.current;
                                event[i].priority = 0;
                                event[i].passenger_type = 0;
                            } else {
                                int i = findOne_controllo_dedicato();
                                event[i].x = 1;
                                event[i].t = t.current;
                                event[i].priority = 1;
                                event[i].passenger_type = 1;
                            }
                        } else {
                            r.selectStream(2);
                            double rndFF = r.random();
                            if (rndFF > MMValues.FFPercentage) {
                                event[MMValues.SERVER_BIGLIETTERIA + MMValues.SERVER_BIGLIETTERIA_DEDICATO + 1].x = 1;
                                event[MMValues.SERVER_BIGLIETTERIA + MMValues.SERVER_BIGLIETTERIA_DEDICATO + 1].t = t.current;
                                event[MMValues.SERVER_BIGLIETTERIA + MMValues.SERVER_BIGLIETTERIA_DEDICATO + 1].priority = 0;
                                event[MMValues.SERVER_BIGLIETTERIA + MMValues.SERVER_BIGLIETTERIA_DEDICATO + 1].passenger_type = 0;
                            } else {
                                event[MMValues.SERVER_BIGLIETTERIA + MMValues.SERVER_BIGLIETTERIA_DEDICATO + 1].x = 1;
                                event[MMValues.SERVER_BIGLIETTERIA + MMValues.SERVER_BIGLIETTERIA_DEDICATO + 1].t = t.current;
                                event[MMValues.SERVER_BIGLIETTERIA + MMValues.SERVER_BIGLIETTERIA_DEDICATO + 1].priority = 1;
                                event[MMValues.SERVER_BIGLIETTERIA + MMValues.SERVER_BIGLIETTERIA_DEDICATO + 1].passenger_type = 1;
                            }
                        }
                    } else {
                        System.out.println("\n\n****Sto processando un arrivo*****");
                        r.selectStream(2);
                        double rnd = r.random();                        // Prendo un numero casuale da Rngs
                        System.out.println("\nil valore di rnd è:" + rnd);
                        if (rnd > MMValues.FFPercentage) {
                            event[0].passenger_type = 0;
                            event[0].priority = 0;
                        } else {
                            event[0].passenger_type = 1;
                            event[0].priority = 1;
                        }
                        System.out.println("il tipo di passeggero è:" + event[0].passenger_type);
                        Block block = new Block(event[0]);

                        Queues.enqueue(block.priority, block);  // enqueue the current arrival into the respective queue defined by 'priority'
                        number_queues[event[0].priority] += 1;// incremento temporaneamente il numero dei job nella coda di appartenenza
                        //number++; // incremento numero dei job nel sistema
                        number_nodes[0]++;
                        /*event[0].t = getArrival(r);
                        if (event[0].t > STOP)
                            event[0].x = 0;*/
                       // if (number_nodes[0] <= MMValues.SERVER_BIGLIETTERIA + MMValues.SERVER_BIGLIETTERIA_DEDICATO) {
                            int l = 0;
                            for (int z = MMValues.SERVER_BIGLIETTERIA + 1; z <= MMValues.SERVER_BIGLIETTERIA + MMValues.SERVER_BIGLIETTERIA_DEDICATO; z++) {
                                if (event[z].x == 0) {
                                    l = 1;
                                }
                            }
                            if (block.priority == 1 && l == 1) {
                                areaBiglietteriaDedicata += (t.next - t.current) * number;
                                Block passenger_served = Queues.dequeue(block.priority);
                                s=findOne(event, MMValues.SERVER_BIGLIETTERIA + 1,MMValues.SERVER_BIGLIETTERIA + MMValues.SERVER_BIGLIETTERIA_DEDICATO);
                                service = getServiceBigl(r);
                                sum[s].service += service;
                                sum[s].served++;
                                event[s].t = t.current + service;
                                event[s].x = 1;
                                event[s].priority = block.priority;
                                event[s].passenger_type = block.type;
                                number_queues[event[s].priority] -= 1;

                            } else {
                                l = 0;
                                for (int z = 1; z <= MMValues.SERVER_BIGLIETTERIA; z++) {
                                    if (event[z].x == 0) {
                                        l = 1;
                                    }
                                }
                                if (l == 1) {
                                    System.out.println("\n***sono dentro else***");
                                    Block passenger_served = Queues.dequeue(block.priority);
                                    service = getServiceBigl(r);
                                    s = m.findOne(event,1,MMValues.SERVER_BIGLIETTERIA);
                                    System.out.println("SCELGO IL SERVER NUMERO:"+s);
                                    out+="\nSCELGO IL SERVER NUMERO:"+s;
                                    sum[s].service += service;
                                    System.out.println("il servizio è"+service);
                                    out+="\nil servizio è"+service;
                                    sum[s].served++;
                                    event[s].t = t.current + service;
                                    event[s].x = 1;
                                    event[s].priority = block.priority;
                                    event[s].passenger_type = block.type;
                                    number_queues[event[s].priority] -= 1;
                                }
                            }
                        //}
                    }
                //}
            } else if(e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1){ /* process an arrival at check-in*/
                event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].x=0; //6
                System.out.println("\n *** Sto processando un arrivo al check in ***");
                System.out.println("il tipo di passeggero è:"+event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].passenger_type);
                Block block=new Block(event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1]);

                Queues_checkin.enqueue(block.priority,block);  // enqueue the current arrival into the respective queue defined by 'priority'
                number_queues_checkin[event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].priority] += 1;// incremento temporaneamente il numero dei job nella coda di appartenenza
                //number++; // incremento numero dei job nel sistema
                number_nodes[1]++;
               // if (number_nodes[1] <= MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO) {
                    int l=0;
                    for(int z=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+1+1;z<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1;z++){
                        if(event[z].x==0){
                            l=1;
                        }
                    }
                    if (block.priority==1 && l==1){
                        Block passenger_served = Queues_checkin.dequeue(block.priority);
                        service         = getServiceChk(r);
                        s=findOne(event,MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+1+1,MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+1+MMValues.SERVER_CHECK_DEDICATO);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t      = t.current + service;
                        event[s].x      = 1;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_checkin[event[s].priority] -= 1;

                    }
                    else {
                        l=0;
                        for(int z=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+1;z<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+1+MMValues.SERVER_CHECK_IN;z++){
                            if(event[z].x==0){
                                l=1;
                            }
                        }
                        if( l==1 ){
                            System.out.println("\n***sono dentro else***");
                            Block passenger_served = Queues_checkin.dequeue(block.priority);
                            service = getServiceChk(r);
                            s = m.findOne_check_in(event);
                            sum[s].service += service;
                            sum[s].served++;
                            event[s].t = t.current + service;
                            event[s].x = 1;
                            event[s].priority = block.priority;
                            event[s].passenger_type = block.type;
                            number_queues_checkin[event[s].priority] -= 1;
                        }
                    }
                //}


            }
            else if(e>=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1 && e<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO){
                event[e].x=0;
                System.out.println("\n *** Sto processando un arrivo al controllo ***");
                System.out.println("il tipo di passeggero è:"+event[e].passenger_type);
                Block block=new Block(event[e]);


                // number++; // incremento numero dei job nel sistema
                int k = findNode(e);
                number_nodes[k]++;
                if (number_nodes[k] <= 1) {

                    if( event[e].x==0  ){


                        service = getServiceScann(r);
                        s = m.findOne_controllo(e);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].x = 1;
                        event[s].priority = block.priority;
                        event[s].passenger_type = block.type;

                    }

                }
            }
            else if (e>=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+1 && e<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO){
                event[e].x=0;

                System.out.println("\n *** Sto processando un arrivo al controllo ***");
                System.out.println("il tipo di passeggero è:"+event[e].passenger_type);
                Block block=new Block(event[e]);


                //number++; // incremento numero dei job nel sistema
                int k = findNode(e);
                number_nodes[k]++;
                if (number_nodes[k] <= 1) {

                    if( event[e].x==0  ){


                        service = getServiceScann(r);
                        s = m.findOne_controllo(e);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].x = 1;
                        event[s].priority = block.priority;
                        event[s].passenger_type = block.type;

                    }

                }
            }
            else if (e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2) {
                event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2].x=0;
                System.out.println("\n *** Sto processando un arrivo alla security ***");
                System.out.println("La prio del passeggero è: "+event[e].priority);
                Block block=new Block(event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2]);

                Queues_security.enqueue(block.priority,block);  // enqueue the current arrival into the respective queue defined by 'priority'
                System.out.println("La prio del passeggero è block: "+block.priority);
                number_queues_security[event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2].priority] += 1;// incremento temporaneamente il numero dei job nella coda di appartenenza
                //number++; // incremento numero dei job nel sistema
                ;
                number_nodes[8]++;
               // if (number_nodes[8] <= MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO) {
                    int l=0;
                    for(int z=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+1;z<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO;z++){
                        if(event[z].x==0){
                            l=1;
                        }
                    }
                    if (block.priority==1 && l==1){
                        Block passenger_served = Queues_security.dequeue(block.priority);
                        service         = getServiceSec(r);

                        s=findOne(event,MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+1,MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t      = t.current + service;
                        event[s].x      = 1;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_security[event[s].priority] -= 1;

                    }
                    else {
                        l=0;
                        for(int z=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+3;
                            z<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY;z++){
                            if(event[z].x==0){
                                l=1;
                            }
                        }
                        if( l==1){

                            Block passenger_served = Queues_security.dequeue(block.priority);
                            service = m.getServiceSec(r);
                            s = m.findOne_security(event);
                            sum[s].service += service;
                            sum[s].served++;
                            event[s].t = t.current + service;
                            event[s].x = 1;
                            event[s].priority = block.priority;
                            event[s].passenger_type = block.type;
                            number_queues_security[event[s].priority] -= 1;
                        }
                    }
               // }
            }
            else if ( e== MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2){
                event[ MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2].x=0;
                System.out.println("\n *** Sto processando un arrivo all'imbarco***");
                //32
                System.out.println("il tipo di passeggero è:"+event[ MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2].passenger_type+"\nla prio è:"+event[ MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2].priority);
                Block block=new Block(event[e]);
                System.out.println("\nla priority del blocco è:"+block.priority);
                System.out.println(block.priority);
                Queues_imbarco.enqueue(block.priority,block);  // enqueue the current arrival into the respective queue defined by 'priority'

                number_queues_imbarco[event[ MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2].priority] += 1;// incremento temporaneamente il numero dei job nella coda di appartenenza
                //number++; // incremento numero dei job nel sistema
                ;
                number_nodes[9]++;
                System.out.println("IL NUMERO DEL NODO è :"+number_nodes[9]);
                //if (number_nodes[9] <= MMValues.SERVER_IMBARCO_DEDICATO+MMValues.SERVER_IMBARCO) {
                    int  l=0;
                    for(int z=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+MMValues.SERVER_IMBARCO+1+1+1;
                        z<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO;z++){
                        if(event[z].x==0){
                            l=1;
                        }
                    }
                    int poi=0;
                    if (block.priority==1 && l==1){
                        Block passenger_served = Queues_imbarco.dequeue(block.priority);
                        service         = m.getServiceGate(r);
                        //37
                        s=findOne(event,MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+MMValues.SERVER_IMBARCO+1+1+1,MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO);
                        sum[s].service += service; //MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO
                        sum[s].served++;
                        event[s].t      = t.current + service;
                        event[s].x      = 1;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_imbarco[event[s].priority] -= 1;
                        poi=1;
                    }
                      l=0;
                    for(int z=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+3;
                        z<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO;z++){
                        if(event[z].x==0){
                            l=1;
                        }
                    }
                    //SSSS
                    if (block.priority==2 && (l==1)){
                        System.out.println("\nsono nell'if");
                        Block passenger_served = Queues_imbarco.dequeue(block.priority);
                        service = m.getServiceGate(r);
                        s = m.findOne_imbarco(event);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].x = 1;
                        event[s].priority = block.priority;
                        event[s].passenger_type = block.type;
                        System.out.println("la queue in considerazione è:"+number_queues_imbarco[event[s].priority]);
                        number_queues_imbarco[event[s].priority] -= 1;
                    }
                    else if( poi==0 && l==1){

                        Block passenger_served = Queues_imbarco.dequeue(block.priority);
                        service = m.getServiceGate(r);
                        s = m.findOne_imbarco(event);
                        System.out.println(s);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].x = 1;
                        event[s].priority = block.priority;
                        event[s].passenger_type = block.type;
                        number_queues_imbarco[event[s].priority] -= 1;
                    }

                //}
            }
            else if (e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+1){
                event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+1].x=0;
                System.out.println("\n *** Sto processando un arrivo security app***");
                //System.out.println("il tipo di passeggero è:"+event[31].passenger_type+"\nla prio è:"+event[31].priority);
                Block block=new Block(event[e]);
                Queues_security_app.enqueue(0,block); // enqueue the current arrival into the respective queue defined by 'priority'

                number_queues_security_app += 1;// incremento temporaneamente il numero dei job nella coda di appartenenza


                number_nodes[10]++;
                if (number_nodes[10] <= MMValues.SERVER_CONT_APP){
                    Block passenger_served = Queues_security_app.dequeue(0);
                    service = m.getServiceSec2(r);
                    s=findOne(event, MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+
                            MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO+1,
                            MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO+MMValues.SERVER_CONT_APP);
                    sum[ s].service += service;
                    sum[s].served++;
                    event[ s].t = t.current + service;
                    event[s].x = 1;
                    event[s].priority = block.priority;
                    event[s].passenger_type = block.type;
                    number_queues_security_app-= 1;
                }
            }
            else {                                         /* process a departure */

                index++;                                     /* from server s       */
                // number--;
                s = e;

                if(s>=MMValues.SERVER_BIGLIETTERIA+1 &&s<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO){
                    number_nodes[0]--;
                    System.out.println("\n***Sto processando la departure del server dedicato***");
                    System.out.println(number_queues[1]);
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].x=1;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].t=t.current;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].priority=1;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].passenger_type=1;

                    if(number_queues[1]>0 ) {

                        Block block = new Block(Queues.dequeue(1));
                        service = m.getServiceBigl(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues[1] -= 1;
                    }
                    else{
                        event[s].x      = 0;
                    }
                }
                else if(s>=1+1+MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN && s<=1+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN){ //11
                    number_nodes[1]--;
                    System.out.println("\n***Sto processando la departure del server dedicato check_in ***");
                    System.out.println(number_queues_checkin[1]);
                    int p=findOne_controllo_dedicato();
                    event[p].x=1;
                    event[p].t=t.current;
                    event[p].priority=1;
                    event[p].passenger_type=1;

                    /* inserire codice per controllo */
                    if(number_queues_checkin[1]>0 ) {

                        Block block = new Block(Queues_checkin.dequeue(1));
                        service = m.getServiceChk(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_checkin[1] -= 1;
                    }
                    else{
                        event[s].x      = 0;
                    }
                }
                else if (s>0 && s<= MMValues.SERVER_BIGLIETTERIA) {
                    number_nodes[0]--;
                    System.out.println("\n***Sto processando la departure del server***");
                    System.out.println(number_queues[0]);
                    if(event[s].priority==1){


                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].x=1;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].t=t.current;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].priority=1;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].passenger_type=1;

                    } else if (event[s].priority==0) {


                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].x=1;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].t=t.current;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].priority=0;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].passenger_type=0;

                    }


                    if(number_queues[1]>0 ) {

                        Block block = new Block(Queues.dequeue(1));
                        service = m.getServiceBigl(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues[1] -= 1;
                    }
                    else if (number_queues[0]>0){
                        Block block = new Block(Queues.dequeue(0));
                        service = m.getServiceBigl(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues[0] -= 1;
                    }
                    else{
                        event[s].x=0;
                    }
                }
                else if (s>MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1 && s<2+MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN) {
                    System.out.println("\n***Sto processando la departure del server check in***");
                    System.out.println(number_queues_checkin[0]);
                    number_nodes[1]--;

                    if(event[s].priority==1){
                        int k= findOne_controllo_dedicato();
                        System.out.println(k);
                        event[k].x=1;
                        event[k].t=t.current;
                        event[k].priority=1;
                        event[k].passenger_type=1;



                    } else if (event[s].priority==0) {

                        int k= find_best_node();
                        System.out.println(k);
                        event[k].x=1;
                        event[k].t=t.current;
                        event[k].priority=0;
                        event[k].passenger_type=0;

                    }

                    if(number_queues_checkin[1]>0 ) {

                        Block block = new Block(Queues_checkin.dequeue(1));
                        service = m.getServiceChk(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_checkin[1] -= 1;
                    }
                    else if (number_queues_checkin[0]>0){
                        Block block = new Block(Queues_checkin.dequeue(0));
                        service = m.getServiceChk(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_checkin[0] -= 1;
                    }
                    else{
                        event[s].x=0;
                    }
                }
                else if (s>=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2&& s<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2 ) {//19 22
                    // number--;
                    System.out.println("\n**** Sto processando una departure dal server carta imbarco****");
                    System.out.println("\n"+number);
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3].x=1;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3].t=t.current;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3].priority=0;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3].passenger_type=0;
                    int k =findNode(s);
                    number_nodes[k]--;
                    if(number_nodes[k]>0 ) {

                        Block block = new Block(event[s]);
                        service = m.getServiceScann(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;

                    }
                    else{
                        event[s].x      = 0;
                    }


                }
                else if (s==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2 ||s==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+1) {
                    // number--;
                    System.out.println("\n**** Sto processando una departure dal server dedicato carta imbarco prioritario ****");
                    System.out.println("\n"+number);
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 2].x=1;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 2].t=t.current;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 2].priority=1;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 2].passenger_type=1;
                    int k =findNode(s);
                    number_nodes[k]--;
                    if(number_nodes[k]>0 ) {

                        Block block = new Block(event[s]);
                        service = m.getServiceScann(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;

                    }
                    else{
                        event[s].x=0;
                    }
                }
                else if(s>=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 4 && s<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY){
                    System.out.println("\n**** Sto processando una departure dal server Security***");
                    // number--;
                    number_nodes[8]--;
                    r.selectStream(0 );
                    double rnd = r.random();
                    if(rnd<0.99){
                        r.selectStream(10 );
                        double rnd1 = r.random();
                        if (event[s].priority==1){
                            event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].x = 1;
                            event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].t = t.current;
                            event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].priority = event[s].priority;
                            event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].passenger_type = event[s].passenger_type;
                        }
                        else if(rnd1<0.50) {
                            event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].x = 1;
                            event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].t = t.current;
                            event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].priority = event[s].priority;
                            event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].passenger_type = event[s].passenger_type;
                        }
                        else{
                            event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].x = 1;
                            event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].t = t.current;
                            event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].priority = 2;
                            event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].passenger_type = 2;
                        }
                    }
                    else {
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+1].x=1;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+1].t=t.current;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+1].priority=event[s].priority;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+1].passenger_type=event[s].passenger_type;
                    }
                    if(number_queues_security[1]>0 ) {

                        Block block = new Block(Queues_security.dequeue(1));
                        service = m.getServiceSec(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_security[1] -= 1;
                    }
                    else if (number_queues_security[0]>0){
                        Block block = new Block(Queues_security.dequeue(0));
                        service = m.getServiceSec(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_security[0] -= 1;
                    }
                    else {
                        event[s].x=0;
                    }
                }
                else if(s>=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 2+MMValues.SERVER_SECURITY+1 && s<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 2+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO){
                    System.out.println("\n**** Sto processando una departure dal server dedicato Security***");
                    //number--;
                    number_nodes[8]--;
                    r.selectStream(0 );
                    double rnd = r.random();
                    if(rnd<0.99){
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].x=1;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].t=t.current;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].priority=event[s].priority;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].passenger_type=event[s].passenger_type;
                    }
                    else {
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+1].x=1;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+1].t=t.current;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+1].priority=event[s].priority;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+1].passenger_type=event[s].passenger_type;
                    }
                    if(number_queues_security[1]>0 ) {

                        Block block = new Block(Queues_security.dequeue(1));
                        service = m.getServiceSec(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_security[1] -= 1;
                    }
                    else {
                        event[s].x=0;
                    }
                }
                else if(s>=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+3 && s<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO){
                    number--;
                    number_nodes[9]--;
                    System.out.println("\n**** Sto processando una departure dal server imbarco****");
                    if(number_queues_imbarco[1]>0 ) {

                        Block block = new Block(Queues_imbarco.dequeue(1));
                        service = m.getServiceGate(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_imbarco[1] -= 1;
                    }
                    else if (number_queues_imbarco[2]>0){
                        Block block = new Block(Queues_imbarco.dequeue(2));
                        service = m.getServiceGate(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_imbarco[2] -= 1;
                    }
                    else if (number_queues_imbarco[0]>0){
                        Block block = new Block(Queues_imbarco.dequeue(0));
                        service = m.getServiceGate(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_imbarco[0] -= 1;
                    }
                    else{
                        event[s].x=0;
                    }
                }
                else if(s>=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 2+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+1 && s<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO){
                    number--;
                    number_nodes[9]--;
                    System.out.println("******Sto processando una departure dal server dedicato********");
                    if(number_queues_imbarco[1]>MMValues.SERVER_IMBARCO_DEDICATO ) {

                        Block block = new Block(Queues_imbarco.dequeue(1));
                        service = m.getServiceGate(r);
                        s=findOne(event,MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+1,MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_imbarco[1] -= 1;
                    }
                    else {
                        event[s].x=0;
                    }
                }
                else if(s>=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 2+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+1+MMValues.SERVER_IMBARCO_DEDICATO && s<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 2+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO +MMValues.SERVER_CONT_APP ){
//38
                    number_nodes[10]--;
                    System.out.println("*******Sto processando una departure dal server Controllo approfondito*********");
                    r.selectStream(0 );
                    double rnd = r.random();
                    if (rnd>0.70){
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].x=1;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].t=t.current;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].priority=event[s].priority;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2].passenger_type=event[s].passenger_type;
                    }
                    else{
                        number--;

                    }
                    if(number_queues_security_app>0 ) {

                        Block block=new Block(Queues_security_app.dequeue(0));
                        service = m.getServiceSec2(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.priority;
                        number_queues_security_app -= 1;
                    }
                    else {
                        event[s].x=0;
                    }

                }
                else {
                    event[s].x=0;
                }

            }
            System.out.println("TEMPO iterazione:"+t.next+"\nTeMPO event:"+event[e].t);
        }

        DecimalFormat f = new DecimalFormat("###0.00");
        DecimalFormat g = new DecimalFormat("###0.000");

        System.out.println("\nfor " + index + " jobs the service node statistics are:\n");
        System.out.println("  avg interarrivals .. =   " + f.format(event[0].t / index));
        System.out.println("  avg wait ........... =   " + f.format(area / index));
        System.out.println("  avg # in node ...... =   " + f.format(area / t.current));


        for (s = 1; s <= MMValues.SERVER_BIGLIETTERIA; s++) {
            if (event[s].t > tBigliett) {
                tBigliett = event[s].t;
            }
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO; s++) {
            if (event[s].t > tBigliettD) {
                tBigliettD = event[s].t;
            }
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+2; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN; s++) {
            if (event[s].t > tCheckIn) {
                tCheckIn = event[s].t;
            }
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO; s++) {
            if (event[s].t > tCheckInD) {
                tCheckInD = event[s].t;
            }
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+8; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO; s++) {
            if (event[s].t > tScann) {
                tScann = event[s].t;
            }
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO; s++) {
            if (event[s].t > tScannD) {
                tScannD = event[s].t;
            }
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY; s++) {
            if (event[s].t > tSecur) {
                tSecur = event[s].t;
            }
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO; s++) {
            if (event[s].t > tSecurD) {
                tSecurD = event[s].t;
            }
        }
        System.out.println(tSecurD);
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+3; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO; s++) {
            if (event[s].t > tGate) {
                tGate = event[s].t;
            }
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO; s++) {
            if (event[s].t > tGateD) {
                tGateD = event[s].t;
            }
        }
        System.out.println(t);
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO+MMValues.SERVER_CONT_APP; s++) {
            if (event[s].t > tSecur2) {
                tSecur2 = event[s].t;
            }
        }

        for (s = 1; s <= SERVERS+SERVER_DEDICATO; s++)          /* adjust area to calculate */
            area -= sum[s].service;              /* averages for the queue   */

        System.out.println("  avg delay .......... =   " + f.format(area / index));
        System.out.println("  avg # in queue ..... =   " + f.format(area / t.current));
        System.out.println("\nthe server statistics are:\n");
        System.out.println("    server     utilization     avg service      share");

        for (s = 1; s <= MMValues.SERVER_BIGLIETTERIA; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tBigliett) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index)+ "         "+tBigliett);
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tBigliettD) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+2; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tCheckIn) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tCheckInD) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+8; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tScann) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tScannD) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tSecur) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tSecurD) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+3; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tGate) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tGateD) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }
        for (s = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO+1; s <= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+1+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO+MMValues.SERVER_CONT_APP; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tSecur2) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }

        /*for (s = 1; s <= 45; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / t.current) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }*/
        /*for (s = 7; s < 7+SERVERS+SERVER_DEDICATO; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / t.current) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }

        for (s = 19; s <= 24; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / t.current) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }

        for (s = 26; s <= 31; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / t.current) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }
        for (s =33; s <= 37; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / t.current) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }*/
        System.out.println("");



    }


    private static int findOne_controllo_dedicato() {
        int i=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CARTA_IMBARCO+1;
        int current=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CARTA_IMBARCO+1;

        if (number_nodes[findNode(current)]>number_nodes[findNode(i+1)]){
            current=i+1;
        }


        return current;
    }

    private static int find_best_node() {
        int i=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+2;
        int current=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+2;
        while (i<MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO){
            if (number_nodes[findNode(current)]>number_nodes[findNode(i+1)]){
                current=i+1;
            }
            i++;
        }
        return current;
    }

    private static int findNode(int e) {

        if(e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+2 || e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2){
            return 2;
        }
        if (e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+3 || e== MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+1){
            return 3;
        }
        if (e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+4 || e== MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+2) return 4;
        if(e== MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+3 || e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+5) return 5;
        if(e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+2 || e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+4){
            return 6;
        }
        if(e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+1+MMValues.SERVER_CARTA_IMBARCO_DEDICATO|| e== MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+5){
            return 7;
        }
        if(e== MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+6){
            return 8;
        }
        return -1;
    }


    public static double exponential(double m, Rngs r) {
        /* ---------------------------------------------------
         * generate an Exponential random variate, use m > 0.0
         * ---------------------------------------------------
         */
        return (-m * Math.log(1.0 - r.random()));
    }

    double uniform(double a, double b, Rngs r) {
        /* --------------------------------------------
         * generate a Uniform random variate, use a < b
         * --------------------------------------------
         */
        return (a + (b - a) * r.random());
    }

    public static double getArrival(Rngs r) {

        if (fasciaOraria == MMValues.fasciaOraria1)
            LAMBDA = MMValues.arrivalFascia1;
        if (fasciaOraria == MMValues.fasciaOraria2)
            LAMBDA = MMValues.arrivalFascia2;
        if (fasciaOraria == MMValues.fasciaOraria3)
            LAMBDA = MMValues.arrivalFascia3;
        if (fasciaOraria == MMValues.fasciaOraria4)
            LAMBDA = MMValues.arrivalFascia4;
        if (fasciaOraria == MMValues.fasciaOraria5)
            LAMBDA = MMValues.arrivalFascia5;

        r.selectStream(0);
        sarrival += exponential(1/LAMBDA, r);
        return (sarrival);
    }


    public static double getServiceBigl(Rngs r) {
        SERVICE = MMValues.biglService;
        return getService(r);
    }

    public static double getServiceChk(Rngs r) {
        SERVICE = MMValues.chckinService;
        return getService(r);
    }

    public static double getServiceScann(Rngs r) {
        SERVICE = MMValues.scannService;
        return getService(r);
    }

    public static double getServiceSec(Rngs r) {
        SERVICE = MMValues.securService;
        return getService(r);
    }

    public static double getServiceSec2(Rngs r) {
        SERVICE = MMValues.secur2Service;
        return getService(r);
    }

    public static double getServiceGate(Rngs r) {
        SERVICE = MMValues.gateService;
        return getService(r);
    }

    public static double getService(Rngs r) {
        /* ------------------------------
         * generate the next service time, with rate 1/6
         * ------------------------------
         */
        System.out.println("SERVICE = " + SERVICE);
        r.selectStream(1);
        return (exponential(SERVICE, r));
    }


    int nextEvent(MsqEvent [] event) {
        /* ---------------------------------------
         * return the index of the next event type
         * ---------------------------------------
         */
        int e;
        int i = 0;

        while (event[i].x == 0)       /* find the index of the first 'active' */
            i++;                        /* element in the event list            */
        e = i;
        while (i <100) {         /* now, check the others to find which  */
            i++;                        /* event type is most imminent          */
            if ((event[i].x == 1) && (event[i].t < event[e].t))
                e = i;
        }
        return (e);
    }

    int findOne(MsqEvent [] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i = 1;

        while (event[i].x == 1 && i<=MMValues.SERVER_BIGLIETTERIA)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < MMValues.SERVER_BIGLIETTERIA) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
    int findOne_check_in(MsqEvent [] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+1;

        while (event[i].x == 1 && i<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
    int findOne_security(MsqEvent [] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i = MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+2*MMValues.SERVER_CARTA_IMBARCO+2*MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+1; //26

        while (event[i].x == 1 && i<= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+2*MMValues.SERVER_CARTA_IMBARCO+2*MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+ MMValues.SERVER_SECURITY)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i <  MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+2*MMValues.SERVER_CARTA_IMBARCO+2*MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
    int findOne_imbarco(MsqEvent [] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i =  MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+2*MMValues.SERVER_CARTA_IMBARCO+2*MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+1;//33

        while (event[i].x == 1 && i<= MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+2*MMValues.SERVER_CARTA_IMBARCO+2*MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+2*MMValues.SERVER_CARTA_IMBARCO+2*MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
    int findOne_controllo(int e) {
        /* -----------------------------------------------------
         * return the number of the server
         * -----------------------------------------------------
         */

        if (e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+2){//12
            return MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2;//19
        }
        if (e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+3) return MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+1;
        if (e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+4) return MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+2;
        if (e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+5) return MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+3;
        if (e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+6)return MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+4;
        if (e==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+7)return MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO*2+5;
        return -1;

    }
    public static int findOne(MsqEvent [] event, int min, int max) {

        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i = min;

        while (event[i].x == 1 && i<=max )       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < max) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
}
