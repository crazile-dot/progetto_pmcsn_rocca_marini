import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;

class Node {
    int type;                       /* node=0: FF; node=1: N */
    int number;                     /* numero di job nel singolo nodo */
    int index;
    int area;
}


class Area {
    double node;                    /* time integrated number in the node  */
    double queue;                   /* time integrated number in the queue */
    double service;                 /* time integrated number in service   */

    void initAreaParas() {
        node = 0.0;
        queue = 0.0;
        service = 0.0;
    }
}


public class ModelloIniziale {
    static ArrayList<Double> ResponseTimeFF =new ArrayList<Double>();
    static ArrayList<Double> ResponseTimeN =new ArrayList<Double>();
    static ArrayList<Double> ResponseTimeCheckInFF =new ArrayList<Double>();
    static ArrayList<Double> ResponseTimeCheckInN =new ArrayList<Double>();
    static ArrayList<Double> ResponseTimeCartaFF =new ArrayList<Double>();
    static ArrayList<Double> ResponseTimeCartaN =new ArrayList<Double>();
    static ArrayList<Double> ResponseTimeSecurityN =new ArrayList<Double>();
    static ArrayList<Double> ResponseTimeSecurityFF =new ArrayList<Double>();
    static ArrayList<Double> ResponseTimeImbarcoN =new ArrayList<Double>();
    static ArrayList<Double> ResponseTimeImbarcoFF=new ArrayList<Double>();
    static ArrayList<Double> ResponseTimeSecurityAppN =new ArrayList<Double>();
    static ArrayList<Double> ResponseTimeSecurityAppFF =new ArrayList<Double>();
    static ArrayList<Double> WaitFF =new ArrayList<Double>();
    static ArrayList<Double> WaitN =new ArrayList<Double>();
    static ArrayList<Double> WaitCheckInFF =new ArrayList<Double>();
    static ArrayList<Double> WaitCheckInN =new ArrayList<Double>();
    static ArrayList<Double> WaitCartaFF =new ArrayList<Double>();
    static ArrayList<Double> WaitCartaN =new ArrayList<Double>();
    static ArrayList<Double> WaitSecurityFF =new ArrayList<Double>();
    static ArrayList<Double> WaitSecurityN =new ArrayList<Double>();
    static ArrayList<Double> WaitImbarcoN =new ArrayList<Double>();
    static ArrayList<Double> WaitImbarcoFF=new ArrayList<Double>();
    static ArrayList<Double> WaitSecurityAppFF =new ArrayList<Double>();
    static ArrayList<Double> WaitSecurityAppN =new ArrayList<Double>();
    static int SERVERS_DEDICATO=1;
    static double START = 0.0;              /* initial time                   */
    static double STOP  = 2000.0;          /* terminal (close the door) time */
    static double INFINITY = 1000.0 * STOP;  /* must be much larger than STOP  */

    /* per la simulazione ad orizzonte infinito */
    static int maxArrival = InfiniteHorizonBatchSimulation.batchSize * InfiniteHorizonBatchSimulation.numBatches;
    static int simulation = 0;  /* 0 = simulazione spenta, 1 = simulazione orizzonte infinito, 2 = simulazione orizzonte finito */
    static int jobCounter = 0;
    static int flag = -1;
    static int nJobs = FiniteHorizonSimulation.jobNum;
    //static double[] risposta_globali= new double[InfiniteHorizonBatchSimulation.batchSize*InfiniteHorizonBatchSimulation.numBatches];
    static int job=-1;
    static double LAMBDA;
    static double SERVICE = 2;
    static double fasciaOraria = MMValues.fasciaOraria1;

    static int SERVERS = 4;              /* number of servers */
    static int NODES = 16;
    static int[] number_queues={0,0};
    static double sarrival = START;
    static  Node [] nodes = new Node [NODES];
    static int numb_wait_bigl=0;
    static int numb_wait_biglD=0;
    static int numb_wait_checkin=0;
    static int numb_wait_checkinD=0;
    static int numb_wait_carta=0;
    static int numb_wait_cartaD=0;
    static int numb_wait_security=0;
    static int numb_wait_securityD=0;
    static int numb_wait_security_app=0;
    static int numb_wait_imbarco=0;
    static int numb_wait_imbarcoD=0;
    static int ya=0;
    public static void main(String[] args) throws IOException {
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
        long index = 0;                  /* used to count departed jobs         */
        long number = 0;                  /* number in the node                  */

        int idx = 1;

        Rngs r = new Rngs();
        r.plantSeeds(1965998004);


        //long   numberM = 0;             /* number in the node                 */
        int    e;                      /* next event index                   */
        int    s;                      /* server index                       */
        int    n;                       /* node index */
        //long   indexM  = 0;             /* used to count processed jobs       */
        double area   = 0.0;           /* time integrated number in the node */
        double multiService;
        double singleService;

        double rnd = 0.0;

        MsqEvent [] event = new MsqEvent [1000];  /* tipologie di evento */
        MsqSum [] sum = new MsqSum [1000];
       // Node [] nodes = new Node [NODES];
        Area[] areas = new Area[NODES];
        for (s = 0; s <1000; s++) {
            event[s] = new MsqEvent();
            sum [s]  = new MsqSum();
        }
        for (n = 0; n < NODES; n++) {
            nodes[n] = new Node();
            nodes[n].type = n;
            areas[n] = new Area();
        }

        MsqT t = new MsqT();

        /* inizializzazione del primo arrivo */
        t.current = START;
        event[0].t = getArrival(r);
        event[0].x = 1;
       // event[1].t = getArrival(r);
       // event[1].x = 1;

        for (n = 2; n <1000; n++) {
            event[n].t = START;
            event[n].x = 0;
            sum[n].service = 0.0;
            sum[n].served = 0;
        }
        MultiQueue Queues_security=new MultiQueue(1);
        MultiQueue Queue_security1=new MultiQueue(2);
        MultiQueue Queues_biglietteria= new MultiQueue(1);
        MultiQueue Queues_biglietteriaD= new MultiQueue(1);
        MultiQueue Queues_checkin=new MultiQueue(1);
        MultiQueue Queues_checkinD=new MultiQueue(1);

        MultiQueue Queues_imbarcoD=new MultiQueue(1);

        MultiQueue Queue_carta1=new MultiQueue(1);
        MultiQueue Queue_carta2=new MultiQueue(1);
        MultiQueue Queue_carta3=new MultiQueue(1);
        MultiQueue Queue_carta4=new MultiQueue(1);
        MultiQueue Queue_carta_ded1=new MultiQueue(1);
        MultiQueue Queue_carta_ded2=new MultiQueue(1);
        MultiQueue Queue_securityD=new MultiQueue(1);
        MultiQueue Queue_imbarco1=new MultiQueue(2);
        int iteration = 0;

        while ((event[0].x != 0) || event[1].x != 0 || (number != 0)) {
            int q=0;
            System.out.println("ILNUMERO DEI JOB NEL SISTEMA E':" + number);
            System.out.println("JOBCOUNTER: " + jobCounter);
            /* q = 1;
            for (s = 2; s <= 1+Values.SERVERS_BIGLIETTERIA; s++) {
                System.out.println("server biglietteria  " + q + ":" + event[s].x);
                q++;
            }
            System.out.println("coda biglietteria:" + (nodes[1].number - Values.SERVERS_BIGLIETTERIA));
            q = 1;
            for (s = 1 + Values.SERVERS_BIGLIETTERIA + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA; s++) {
                System.out.println("server biglietteria dedicato " + q + ":" + event[s].x);
                q++;
            }
            System.out.println("coda biglietteria dedicato:" + (nodes[0].number - Values.SERVERS_DEDICATO_BIGLIETTERIA));
            q = 1;
            for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVER_DEDICATO_CHECK_IN + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_CHECK_IN; s++) {
                System.out.println("server check in " + q + ":" + event[s].x);
                q++;
            }
            System.out.println("coda check in:" + (nodes[3].number - Values.SERVERS_CHECK_IN));
            q = 1;
            for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVER_DEDICATO_CHECK_IN; s++) {
                System.out.println("server check in dedicato " + q + ":" + event[s].x);
                q++;
            }
            System.out.println("coda check in dedicato:" + (nodes[2].number - Values.SERVER_DEDICATO_CHECK_IN));
            q = 1;
            for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2; s++) {
                System.out.println("server carta imbarco " + q + ":" + event[s].x);
                q++;
            }
            System.out.println("coda imbarco:" + (nodes[5].number - 1));
            System.out.println("coda imbarco:" + (nodes[6].number - 1));
            System.out.println("coda imbarco:" + (nodes[7].number - 1));
            System.out.println("coda imbarco:" + (nodes[8].number - 1));
            q = 1;
            for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2; s++) {
                System.out.println("server carta imbarco dedicato " + q + ":" + event[s].x);
                q++;
            }
            System.out.println("coda imbarco dedicato 1:" + (nodes[4].number - 1));
            System.out.println("coda imbarco dedicato 2:" + (nodes[9].number - 1));

            q = 1;
            for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_DEDICATI_SECURITY + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_DEDICATI_SECURITY + Values.SERVERS_SECURITY; s++) {
                System.out.println("server security  " + q + ":" + event[s].x);
                q++;
            }
            System.out.println("coda security:" + (nodes[11].number - Values.SERVERS_SECURITY));
            q = 1;
            for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_DEDICATI_SECURITY; s++) {
                System.out.println("server security dedicato  " + q + ":" + event[s].x);
                q++;
            }
            System.out.println("coda security prioritari:" + (nodes[10].number - Values.SERVERS_DEDICATI_SECURITY));

            q = 1;
            for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI; s++) {
                System.out.println("server controllo approfondito  " + q + ":" + event[s].x);
                q++;
            }
            System.out.println("coda controllo app" + (nodes[12].number - Values.SERVERS_CONTROLLI_APPROFODNITI));
            q = 1;
            for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + Values.SERVERS_IMBARCO_DEDICATO; s++) {
                //System.out.println("server  imbarco dedicato: " + q + ":" + event[s].x);
                q++;
            }
            System.out.println("coda imbarco dedicato" + (nodes[13].number - Values.SERVERS_IMBARCO_DEDICATO));
            q = 1;
            for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + Values.SERVERS_IMBARCO_DEDICATO + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + Values.SERVERS_IMBARCO_DEDICATO + Values.SERVERS_IMBARCO; s++) {
                //System.out.println("server  imbarco: " + q + ":" + event[s].x);
                q++;
            }
            System.out.println("coda imbarco" + (nodes[14].number - Values.SERVERS_IMBARCO));
            System.out.println("NUMEROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO"+ya);*/

            e = nextEvent(event);                     /* next event index */
            t.next = event[e].t;                        /* next event time  */
            area += (t.next - t.current) * number;      /* update integral  */

            System.out.println("Tipo evento: " + e + "\n");

            for (int i = 0; i < NODES; i++) {
                nodes[i].area += (t.next - t.current) * nodes[i].number;
                areas[i].node = nodes[i].area;
                if (i == 0) {
                    if (nodes[i].number > 0) {
                        areas[i].queue += (t.next - t.current) * (nodes[i].number - 1);
                        areas[i].service = 1;
                    }
                } else {
                    if (nodes[i].number > SERVERS) {
                        areas[i].queue += (t.next - t.current) * (nodes[i].number - SERVERS);
                        areas[i].service = SERVERS;
                    } else {
                        areas[i].service = nodes[i].number;
                    }
                }

            }

            t.current = t.next;                            /* advance the clock*/

            if (e == 0 || e == 1) {

                job++;
                if (simulation == 0) { } /* simulazione spenta */
                else if (simulation == 1){
                    if (maxArrival > 1) { /* simulazione orizzonte infinito */
                        maxArrival--;
                    }
                    else {
                        flag = 1;
                        event[0].x = 0;
                        event[1].x = 0;
                    }
                } else if (simulation == 2) {  /* simulazione orizzonte finito */
                    if (nJobs > 1) {
                        nJobs--;
                    }
                    else {
                        flag = 1;
                        event[0].x = 0;
                        event[1].x = 0;
                    }
                }
                number++;
                jobCounter++;
                r.selectStream(10 + idx);
                idx++;
                rnd = r.random();
                if (rnd > MMValues.FFPercentage && flag != 1) {
                    event[0].t = getArrival(r);
                    event[0].x=1;
                    event[1].x=0;

                    //System.out.println("Primo arrivo normale: " + event[0].t);
                    if (event[0].t > STOP) {
                        event[0].x = 0;
                    }


                } else if (rnd <= MMValues.FFPercentage && flag != 1){
                    event[1].t = getArrival(r);
                    event[1].x=1;
                    event[0].x=0;
                    //System.out.println("Primo arrivo ff: " + event[1].t);
                    if (event[1].t > STOP) {
                        event[1].x = 0;
                    }
                }
                r.selectStream(4);
                double rndB = r.random();
                if(rndB > MMValues.noTktNPercentage + MMValues.noTktFFPercentage) {
                    r.selectStream(8);
                    double rndC = r.random();
                    if (rndC > MMValues.noChkinNPercentage + MMValues.noChkinFFPercentage) {
                        r.selectStream(2);
                        double rndFF = r.random();
                        if (e==0) {
                            System.out.println("********STO IMPOSTANDO QUESTO VALORE ***********");
                            int i = find_best_server();
                            event[i].x = 1;
                            event[i].t = t.current;
                            event[i].priority = 0;
                            event[i].passenger_type = 0;
                            event[i].job=job;
                        } else {
                            int i = find_best_server_dedicato();
                            System.out.println("********STO IMPOSTANDO QUESTO VALORE DEDICATO ***********");
                            event[i].x = 1;
                            event[i].t = t.current;
                            event[i].priority = 1;
                            event[i].passenger_type = 1;
                            event[i].job=job;
                        }
                    }
                    else{
                        r.selectStream(2);
                        double rndFF = r.random();

                        if (e==0) {
                            System.out.println("********STO IMPOSTANDO QUESTO VALORE  CHECK IN***********");
                            event[1+Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 1].x = 1;
                            event[1+Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 1].t = t.current;
                            event[1+Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 1].priority = 0;
                            event[1+Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 1].passenger_type = 0;
                            event[1+Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 1].job=job;
                        } else {
                            System.out.println("********STO IMPOSTANDO QUESTO VALORE DEDICATO CHECK IN***********");
                            event[1+Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2].x = 1;
                            event[1+Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2].t = t.current;
                            event[1+Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2].priority = 1;
                            event[1+Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2].passenger_type = 1;
                            event[1+Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2].job=job;
                        }
                    }
                }else{
                    System.out.println("\n*** Sto Processando un arrivo ***");
                    Block block;
                    if (e == 0) {
                        nodes[1].number++;
                        block = new Block(event[0]);
                        numb_wait_bigl++;
                        block.job=job;
                        block.arrival_time=t.current;
                        Queues_biglietteria.enqueue(0, block);
                    } else {
                        ya++;
                        nodes[0].number++;
                       block = new Block(event[1]);
                        numb_wait_biglD++;
                        block.job=job;
                        block.arrival_time=t.current;
                        Queues_biglietteriaD.enqueue(0, block);
                    }




                    if (e == 1 && nodes[0].number <= Values.SERVERS_DEDICATO_BIGLIETTERIA) {
                        numb_wait_biglD--;

                        multiService = getServiceBigl(r);
                        Queues_biglietteriaD.dequeue(0);
                        s = findOne(event, 1 + Values.SERVERS_BIGLIETTERIA + 1, 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                        event[s].x = 1;
                        event[s].arrival_time=block.arrival_time;
                        event[s].counter=block.number;
                        event[s].service=multiService;
                        event[s].departure= t.current + multiService;
                        event[s].block=block;
                        event[s].job=job;
                    } else if (e == 0 && nodes[1].number <= Values.SERVERS_BIGLIETTERIA) {
                        numb_wait_bigl--;

                        Queues_biglietteria.dequeue(0);
                        multiService = getServiceBigl(r);
                        s = findOne(event, 2, Values.SERVERS_BIGLIETTERIA + 1);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                        event[s].x = 1;
                        event[s].arrival_time=block.arrival_time;
                        event[s].counter=block.number;
                        event[s].service=multiService;
                        event[s].departure= t.current + multiService;
                        event[s].block=block;
                        event[s].job=job;
                    }



                }
                /* process an arrival*/


               /* System.out.println("\n*** Sto Processando un arrivo ***");
                if (e == 0) {
                    nodes[1].number++;
                } else {
                    nodes[0].number++;
                }




                if (e == 1 && nodes[0].number <= Values.SERVERS_DEDICATO_BIGLIETTERIA) {

                    multiService = getService(r);
                    s = findOne(event, 1 + Values.SERVERS_BIGLIETTERIA + 1, 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                } else if (e == 0 && nodes[1].number <= Values.SERVERS_BIGLIETTERIA) {
                    multiService = getService(r);
                    s = findOne(event, 2, Values.SERVERS_BIGLIETTERIA + 1);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                }

*/


        }
            else if(e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2){ /* server dedicato*/
                System.out.println("*****Sto Processando un arrivo al server dedicato check in**************");
                Block block=new Block(event[e]);
                block.arrival_time=t.current;
                Queues_checkinD.enqueue(0,block);
                event[e].x=0;
                //number++;
                nodes[2].number++;
                //idx++????
                numb_wait_checkinD++;
                if( nodes[2].number<=Values.SERVER_DEDICATO_CHECK_IN){
                    Block block1=new Block(Queues_checkinD.dequeue(0));
                    multiService = getServiceChk(r);
                    numb_wait_checkinD--;
                    s=findOne(event,1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+1,1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                    event[s].arrival_time=block.arrival_time;
                    event[s].counter=block.number;
                    event[s].service=multiService;
                    event[s].departure= t.current + multiService;
                    event[s].job=block.job;


                }

            }
            else if(e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1){
                System.out.println("*****Sto Processando un arrivo al server check in**************");
                event[e].x=0;
               // number++;
                numb_wait_checkin++;

                Block block =new Block(event[e]);
                block.arrival_time=t.current;
                Queues_checkin.enqueue(0,block);
                nodes[3].number++;
                //idx++????
                if(nodes[3].number<=Values.SERVERS_CHECK_IN){
                    numb_wait_checkin--;
                    Block block1=new Block(Queues_checkin.dequeue(0));
                    multiService = getServiceChk(r);
                    s = findOne(event, 1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+1, 1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                    event[s].arrival_time=block.arrival_time;
                    event[s].counter=block.number;
                    event[s].service=multiService;
                    event[s].departure= t.current + multiService;
                    event[s].job=block.job;
                }
            }
            else if(e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+2){
                event[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+2].x=0;
                System.out.println("*****Sto Processando un arrivo al server carta dedicato**************");
                Block block =new Block(event[e]);
                block.arrival_time=t.current;
                Queue_carta_ded2.enqueue(0,block);
                //number++;
                nodes[9].number++;
                numb_wait_cartaD++;

                //idx++????
                if(nodes[9].number==1){
                    Queue_carta_ded2.dequeue(0);
                    numb_wait_cartaD--;

                    singleService = getServiceScann(r);
                    sum[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].service += singleService;
                    sum[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].served++;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].t = t.current + singleService;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].x = 1;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].arrival_time=block.arrival_time;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].counter=block.number;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].service=singleService;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].departure= t.current + singleService;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].departure= t.current + singleService;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].job=block.job;


                }
            }
            else if (e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+1){
                event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+1].x=0;
                //number++;
                System.out.println("*****Sto Processando un arrivo al server carta dedicato**************");
                Block block =new Block(event[e]);
                block.arrival_time=t.current;
                numb_wait_cartaD++;

                Queue_carta_ded1.enqueue(0,block);
                nodes[4].number++;
                //idx++????
                if(nodes[4].number==1){
                    numb_wait_cartaD--;
                    Queue_carta_ded1.dequeue(0);

                    singleService = getServiceScann(r);
                    sum[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].service += singleService;
                    sum[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].served++;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].t = t.current + singleService;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].x = 1;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].arrival_time=block.arrival_time;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].counter=block.number;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].service=singleService;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].departure= t.current + singleService;
                    event[1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].job=block.job;

                }
            }
            else if(e>=1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+1 && e<=1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA){
                event[e].x=0;
                System.out.println("*****Sto Processando un arrivo al server carta imbarco**************");
                Block block =new Block(event[e]);
                block.arrival_time=t.current;
                int ita=0;
                numb_wait_carta++;
                if(e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+1){
                    Queue_carta1.enqueue(0,block);
                    ita=1;
                }
                if(e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+2){
                    Queue_carta2.enqueue(0,block);
                    ita=2;
                }
                if(e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+3){
                    Queue_carta3.enqueue(0,block);
                    ita=3;
                }
                if(e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+4){
                    Queue_carta4.enqueue(0,block);
                    ita=4;
                }
                //number++;
                int p=find_number_node(e);
                nodes[p].number++;
                //idx++????
                if(nodes[p].number==1){
                    numb_wait_carta--;
                    if(ita==1){
                        Queue_carta1.dequeue(0);
                    }
                    if(ita==2){
                        Queue_carta2.dequeue(0);
                    }
                    if(ita==3){
                        Queue_carta3.dequeue(0);
                    }
                    if(ita==4){
                        Queue_carta4.dequeue(0);
                    }
                    singleService = getServiceScann(r);

                    sum[e+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].service += singleService;
                    sum[e+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].served++;
                    event[e+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].t = t.current + singleService;
                    event[e+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].x = 1;
                    event[e+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].arrival_time=block.arrival_time;
                    event[e+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].counter=block.number;
                    event[e+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].service=singleService;
                    event[e+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].departure= t.current + singleService;
                    event[e+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].job=block.job;


                }
            }
            else if (e==1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+1){  /* servers prioritario secuirty*/
                System.out.println("*****Sto Processando un arrivo al server security prioritario**************");
                Block block =new Block(event[e]);
                numb_wait_securityD++;
                block.arrival_time=t.current;
                event[e].x=0;
                // number++;
                Queue_securityD.enqueue(0,block);
                nodes[10].number++;
                //idx++????
                if(nodes[10].number<=Values.SERVERS_DEDICATI_SECURITY){
                    Queue_securityD.dequeue(0);
                    multiService = getServiceSec(r);
                    numb_wait_securityD--;

                    s = findOne(event, 1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+1, 1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_DEDICATI_SECURITY);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                    event[s].arrival_time=block.arrival_time;
                    event[s].counter=block.number;
                    event[s].service=multiService;
                    event[s].departure= t.current + multiService;
                    event[s].job=block.job;

                }
            }
            else if(e==1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2){
                event[e].x=0;
                numb_wait_security++;

                Block block =new Block(event[e]);
                block.arrival_time=t.current;
                Queues_security.enqueue(0,block);
                // number++;
                System.out.println("*****Sto Processando un arrivo al server security **************");
                nodes[11].number++;
                //idx++????
                if(nodes[11].number<=Values.SERVERS_SECURITY){
                    numb_wait_security--;

                    Queues_security.dequeue(0);
                    multiService = getServiceSec(r);
                    s = findOne(event, 1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_DEDICATI_SECURITY+1, 1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_DEDICATI_SECURITY+Values.SERVERS_SECURITY);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                    event[s].arrival_time=block.arrival_time;
                    event[s].counter=block.number;
                    event[s].service=multiService;
                    event[s].departure= t.current + multiService;
                    event[s].job=block.job;

                }
            }
            else if (e==1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1) {
                event[e].x=0;

                numb_wait_security_app++;

                System.out.println("\n *** Sto processando un arrivo al controllo approfondito ***");
                nodes[12].number++;
                Block block=new Block(event[e]);
                block.arrival_time=t.current;
                Queue_security1.enqueue(0,block);  // enqueue the current arrival into the respective queue defined by 'priority'
                if(nodes[12].number<=Values.SERVERS_CONTROLLI_APPROFODNITI){
                    numb_wait_security_app--;

                    Block pa=Queue_security1.dequeue(0);
                    multiService = getServiceSec2(r);
                    s = findOne(event, 1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+1, 1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                    event[s].priority=pa.priority;
                    event[s].arrival_time=block.arrival_time;
                    event[s].counter=block.number;
                    event[s].service=multiService;
                    event[s].departure= t.current + multiService;
                    event[s].job=block.job;

                }

            }
            else if(e==1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+1){
                event[e].x=0;
                System.out.println("\n *** Sto processando un arrivo all'imbarco FF***");
                System.out.println("il tipo di passeggero è:"+event[e].priority);
                nodes[13].number++;
                numb_wait_imbarcoD++;
                Block block =new Block(event[e]);
                block.arrival_time=t.current;
                Queues_imbarcoD.enqueue(0,block);
                if(nodes[13].number<=Values.SERVERS_IMBARCO_DEDICATO){
                    numb_wait_imbarcoD--;
                    Queues_imbarcoD.dequeue(0);
                    multiService = getServiceGate(r);
                    s=findOne(event,e+1+1,e+1+Values.SERVERS_IMBARCO_DEDICATO);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                    event[s].arrival_time=block.arrival_time;
                    event[s].counter=block.number;
                    event[s].service=multiService;
                    event[s].departure= t.current + multiService;
                    event[s].job=block.job;

                }
            }
            else if(e==1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2){
                event[e].x=0;
                nodes[14].number++;
                numb_wait_imbarco++;


                System.out.println("\n *** Sto processando un arrivo all'imbarco ***");
                System.out.println("il tipo di passeggero è:"+event[e].priority);
                Block block=new Block(event[e]);
                block.arrival_time=t.current;
                if(block.priority==2){
                    Queue_imbarco1.enqueue(1, block);
                    number_queues[1]++;
                }
                else{
                    Queue_imbarco1.enqueue(0, block);
                    number_queues[0]++;
                }


                if(nodes[14].number<=Values.SERVERS_IMBARCO){
                    numb_wait_imbarco--;

                    if(block.priority==2){
                        Block b=Queue_imbarco1.dequeue(1);
                        number_queues[1]--;
                    }
                    else{
                        Block b=Queue_imbarco1.dequeue(0);
                        number_queues[0]--;
                    }
                    multiService = getServiceGate(r);
                    s = findOne(event,e+Values.SERVERS_IMBARCO_DEDICATO+1,e+Values.SERVERS_IMBARCO_DEDICATO+Values.SERVERS_IMBARCO );
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                    event[s].priority=block.priority;
                    event[s].arrival_time=block.arrival_time;
                    event[s].counter=block.number;
                    event[s].service=multiService;
                    event[s].departure= t.current + multiService;
                    event[s].job=block.job;


                }
            }

            else {                                         /* process a departure */
                index++;                                     /* from server s       */
               // number--;
                s = e;
                if (s >= 1+1 && s <= 1 + Values.SERVERS_BIGLIETTERIA ) {   /* nodo multiserver */
                    System.out.println("\n*** Sto processando una departure del server ***");
                    nodes[1].number--;
                    nodes[1].index++;
                    event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+1].x=1;
                    event[ 1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+1].t=t.current;
                    event[ 1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+1].job=event[s].job;

                    event[s].departure=t.current;
                    double ResponseTime=event[s].departure-event[s].arrival_time;
                    ResponseTimeN.add(ResponseTime);
                    //risposta_globali[job]+=ResponseTime;
                    double WaitingTime=ResponseTime-event[s].service;
                    WaitN.add(WaitingTime);

                    if (nodes[1].number >= Values.SERVERS_BIGLIETTERIA) {
                        Block block=new Block(Queues_biglietteria.dequeue(0));
                        multiService = getServiceBigl(r);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                        event[s].arrival_time=block.arrival_time;
                        event[s].counter=block.number;
                        event[s].service=multiService;
                        event[s].departure= t.current + multiService;
                        event[s].job=block.job;
                    } else
                        event[s].x = 0;
                }
                else if ( s >= 1 + Values.SERVERS_BIGLIETTERIA+1 && s<= 1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA ) {
                    event[s].departure=t.current;
                    double ResponseTime=event[s].departure-event[s].arrival_time;
                    ResponseTimeFF.add(ResponseTime);
                    //risposta_globali[job]+=ResponseTime;
                    double WaitingTime=ResponseTime-event[s].service;
                    WaitFF.add(WaitingTime);
                    System.out.println("\n*** Sto processando una departure del server dedicato***");
                    event[ 1 + Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2].x=1;
                    event[1 + Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2].t=t.current;
                    event[1 + Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2].job=event[s].job;
                    nodes[0].number--;
                    nodes[0].index++;
                    if (nodes[0].number >= Values.SERVERS_DEDICATO_BIGLIETTERIA) {
                        Block block=new Block(Queues_biglietteriaD.dequeue(0));
                        multiService = getServiceBigl(r);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                        event[s].arrival_time=block.arrival_time;
                        event[s].counter=block.number;
                        event[s].service=multiService;
                        event[s].departure= t.current + multiService;
                        event[s].job=block.job;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if (s>= 1 + Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+1 && s<=1 + Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN) {
                    System.out.println("\n*** Sto processando una departure del server check in dedicato***");
                    //number--;
                    nodes[2].number--;
                    nodes[2].index--;
                    double ResponseTime=event[s].departure-event[s].arrival_time;
                    ResponseTimeCheckInFF.add(ResponseTime);
                    //risposta_globali[job]+=ResponseTime;
                    double WaitingTime=ResponseTime-event[s].service;
                    WaitCheckInFF.add(WaitingTime);
                    int k =find_best_server_dedicato();
                    System.out.println("Il numero dell'evento è:"+k);
                    event[k].x=1;
                    event[k].t=t.current;
                    event[k].job=event[s].job;
                    if (nodes[2].number >= Values.SERVER_DEDICATO_CHECK_IN) {
                        Block block=new Block(Queues_checkinD.dequeue(0));
                        multiService = getServiceChk(r);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                        event[s].arrival_time=block.arrival_time;
                        event[s].counter=block.number;
                        event[s].service=multiService;
                        event[s].departure= t.current + multiService;
                        event[s].job=block.job;
                    } else {
                        event[s].x = 0;
                    }

                }
                else if(s>= 1+ Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+1 && s<= 1+ Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN){
                   // number--;
                    System.out.println("\n*** Sto processando una departure del server check in***");
                    nodes[3].number--;
                    nodes[3].index--;
                    double ResponseTime=event[s].departure-event[s].arrival_time;
                    ResponseTimeCheckInN.add(ResponseTime);
                    //risposta_globali[job]+=ResponseTime;
                    double WaitingTime=ResponseTime-event[s].service;
                    WaitCheckInN.add(WaitingTime);
                    int server=find_best_server();
                    event[server].x=1;
                    event[server].t=t.current;
                    event[server].job=event[s].job;
                    if (nodes[3].number >= Values.SERVERS_CHECK_IN) {
                        Block block=new Block(Queues_checkin.dequeue(0));

                        multiService = getServiceChk(r);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                        event[s].arrival_time=block.arrival_time;
                        event[s].counter=block.number;
                        event[s].service=multiService;
                        event[s].departure= t.current + multiService;
                        event[s].job=block.job;

                    } else {
                        event[s].x = 0;
                    }
                }
                else if(s==1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1 ){
                   // number--;
                    System.out.println("\n*** Sto processando una departure del server dedicato carta imbarco***");
                    event[e+1+Values.SERVERS_SCANSIONE_CARTA+1].x=1;
                    event[e+1+Values.SERVERS_SCANSIONE_CARTA+1].t=t.current;
                    event[e+1+Values.SERVERS_SCANSIONE_CARTA+1].job=event[s].job;;
                    double ResponseTime=event[s].departure-event[s].arrival_time;
                    ResponseTimeCartaFF.add(ResponseTime);
                    //risposta_globali[job]+=ResponseTime;
                    double WaitingTime=ResponseTime-event[s].service;
                    WaitCartaFF.add(WaitingTime);
                    nodes[4].number--;
                    nodes[4].index--;
                    if (nodes[4].number >= 1) {
                        Block block=new Block(Queue_carta_ded1.dequeue(0));
                        singleService = getServiceScann(r);
                        sum[s].service += singleService;
                        sum[s].served++;
                        event[s].t = t.current + singleService;
                        event[s].arrival_time=block.arrival_time;
                        event[s].counter=block.number;
                        event[s].service=singleService;
                        event[s].departure= t.current + singleService;
                        event[s].job=block.job;

                    } else {
                        event[s].x = 0;
                    }
                }
                else if(s==1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+2){
                   // number--;
                    event[e+Values.SERVERS_SCANSIONE_CARTA+1].x=1;
                    event[e+Values.SERVERS_SCANSIONE_CARTA+1].t=t.current;
                    event[e+Values.SERVERS_SCANSIONE_CARTA+1].job=event[s].job;;
                    double ResponseTime=event[s].departure-event[s].arrival_time;
                    ResponseTimeCartaFF.add(ResponseTime);
                    //risposta_globali[job]+=ResponseTime;
                    double WaitingTime=ResponseTime-event[s].service;
                    WaitCartaFF.add(WaitingTime);
                    System.out.println("\n*** Sto processando una departure del server dedicato carta imbarco***");
                    nodes[9].number--;
                    nodes[9].index--;
                    if (nodes[9].number >= 1) {
                        Block block=new Block(Queue_carta_ded2.dequeue(0));
                        singleService = getServiceScann(r);
                        sum[s].service += singleService;
                        sum[s].served++;
                        event[s].t = t.current + singleService;
                        event[s].arrival_time=block.arrival_time;
                        event[s].counter=block.number;
                        event[s].service=singleService;
                        event[s].departure= t.current + singleService;
                        event[s].job=block.job;
                    } else {
                        event[s].x = 0;
                    }
                }

                else if(s>=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA+1 && s<=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2){
                    event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2].x=1;
                    event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2].t=t.current;
                    event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2].job=event[s].job;;

                    System.out.println("\n*** Sto processando una departure del server  carta imbarco***");
                   // number--;
                    double ResponseTime=event[s].departure-event[s].arrival_time;
                    ResponseTimeCartaN.add(ResponseTime);
                    //risposta_globali[job]+=ResponseTime;
                    double WaitingTime=ResponseTime-event[s].service;
                    WaitCartaN.add(WaitingTime);
                    int nu=find_number_node(e);
                    nodes[nu].number--;
                    nodes[nu].index++;

                    if (nodes[nu].number >= 1) {
                        if(s==1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA+1){
                            Block block=new Block(Queue_carta1.dequeue(0));
                            event[s].arrival_time=block.arrival_time;
                            event[s].job=block.job;
                        }
                        if(s==1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA+2){
                            Block block=new Block(Queue_carta2.dequeue(0));
                            event[s].arrival_time=block.arrival_time;
                            event[s].job=block.job;
                        }
                        if(s==1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA+3){
                            Block block=new Block(Queue_carta3.dequeue(0));
                            event[s].arrival_time=block.arrival_time;
                            event[s].job=block.job;
                        }
                        if(s==1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA+4){
                            Block block=new Block(Queue_carta4.dequeue(0));
                            event[s].arrival_time=block.arrival_time;
                            event[s].job=block.job;
                        }
                        singleService = getServiceScann(r);
                        sum[s].service += singleService;
                        sum[s].served++;
                        event[s].t = t.current + singleService;


                        event[s].service=singleService;
                        event[s].departure= t.current + singleService;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if (s>= 1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+1 && s<=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_DEDICATI_SECURITY){
                   //  number--;
                    System.out.println("\n*** Sto processando una departure del server dedicato security**");
                    nodes[10].number--;
                    nodes[10].index++;
                    double ResponseTime=event[s].departure-event[s].arrival_time;
                    ResponseTimeSecurityFF.add(ResponseTime);
                    //risposta_globali[job]+=ResponseTime;
                    double WaitingTime=ResponseTime-event[s].service;
                    WaitSecurityFF.add(WaitingTime);
                    r.selectStream(10 + idx);
                    idx++;
                    rnd = r.random();
                    if(rnd>(1-MMValues.imbarcoPerc)){
                        event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+1].x=1;
                        event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+1].t=t.current;
                        event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+1].job=event[s].job;;
                        event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+1].priority=1;
                    }
                    else {
                        event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1].x=1;
                        event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1].t=t.current;
                        event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1].job=event[s].job;;
                        event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1].priority=event[s].priority;
                    }
                    if (nodes[10].number >= Values.SERVERS_DEDICATI_SECURITY) {
                        Block block=new Block(Queue_securityD.dequeue(0));
                        multiService = getServiceSec(r);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                        event[s].arrival_time=block.arrival_time;
                        event[s].service=multiService;
                        event[s].departure= t.current + multiService;
                        event[s].job=block.job;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if(s>= 1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_DEDICATI_SECURITY+1 && s<=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_DEDICATI_SECURITY+Values.SERVERS_SECURITY){
                     //number--;
                    System.out.println("\n*** Sto processando una departure del server security***");
                    double ResponseTime=event[s].departure-event[s].arrival_time;
                    ResponseTimeSecurityN.add(ResponseTime);
                    //risposta_globali[job]+=ResponseTime;
                    double WaitingTime=ResponseTime-event[s].service;
                    WaitSecurityN.add(WaitingTime);
                    nodes[11].number--;
                    nodes[11].index++;
                    r.selectStream(10 + idx);
                    idx++;
                    rnd = r.random();
                    if(rnd<(1-MMValues.imbarcoPerc)){
                        event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1].x=1;
                        event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1].t=t.current;
                        event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1].priority=event[s].priority;
                        event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1].job=event[s].job;;

                    }
                    else {
                        r.selectStream(10 + idx);
                        idx++;
                        rnd = r.random();

                        if(rnd>0.50) {
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2].x = 1;
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2].t = t.current;

                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2].priority=2;
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2].job=event[s].job;;
                        }
                        else{
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2].x = 1;
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2].t = t.current;
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2].priority=0;
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2].job=event[s].job;;

                        }

                    }

                    if (nodes[11].number >= Values.SERVERS_SECURITY) {
                        Block block=new Block(Queues_security.dequeue(0));
                        multiService = getServiceSec(r);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                        event[s].arrival_time=block.arrival_time;
                        event[s].service=multiService;
                        event[s].departure= t.current + multiService;
                        event[s].job=block.job;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if (s>=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+1 && s<=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI){

                    System.out.println("\n*** Sto processando una departure del server controllo approfondito***");
                    nodes[12].number--;
                    nodes[12].index--;
                   /* double ResponseTime=event[s].departure-event[s].arrival_time;
                    ResponseTimeSecurityAppN.add(ResponseTime);
                    double WaitingTime=ResponseTime-event[s].service;
                    WaitSecurityAppN.add(WaitingTime);*/
                    if(event[s].priority==1){
                        event[s].departure=t.current;
                        double RespondeTime=event[s].departure-event[s].arrival_time;
                        ResponseTimeSecurityAppFF.add(RespondeTime);
                        //risposta_globali[job]+=RespondeTime;
                        double WaitingTime=RespondeTime-event[s].service;
                        WaitSecurityAppFF.add(WaitingTime);
                    }
                    else{
                        event[s].departure=t.current;
                        double RespondeTime=event[s].departure-event[s].arrival_time;
                        ResponseTimeSecurityAppN.add(RespondeTime);
                        //risposta_globali[job]+=RespondeTime;
                        double WaitingTime=RespondeTime-event[s].service;
                        WaitSecurityAppN.add(WaitingTime);
                    }
                    double rnd1 = r.random();
                    r.selectStream(0 );
                    if (rnd1>0.70){
                        if(event[s].priority==1){
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+1].x=1;
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+1].t=t.current;
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+1].priority=event[s].priority;
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+1].passenger_type=event[s].passenger_type;
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+1].job=event[s].job;;


                        }else{
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2].x=1;
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2].t=t.current;
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2].priority=event[s].priority;
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2].passenger_type=event[s].passenger_type;
                            event[1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2].job=event[s].job;;


                        }
                    }
                    else{
                        number--;

                    }
                    if (nodes[12].number >= Values.SERVERS_CONTROLLI_APPROFODNITI) {
                        Block block=new Block(Queue_security1.dequeue(0));
                        multiService = getServiceSec2(r);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                        event[s].arrival_time=block.arrival_time;
                        event[s].service=multiService;
                        event[s].departure= t.current + multiService;
                        event[s].job=block.job;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if (s>=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2+1 && s<=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2+Values.SERVERS_IMBARCO_DEDICATO){
                    System.out.println("\n*** Sto processando una departure del server  dedicato imbarco***");
                    number--;

                    nodes[13].number--;
                    nodes[13].index++;
                    double ResponseTime=event[s].departure-event[s].arrival_time;
                    ResponseTimeImbarcoFF.add(ResponseTime);
                    //risposta_globali[job]+=ResponseTime;
                    double WaitingTime=ResponseTime-event[s].service;
                    WaitImbarcoFF.add(WaitingTime);

                    if (nodes[13].number >= Values.SERVERS_IMBARCO_DEDICATO) {
                        Block block=new Block(Queues_imbarcoD.dequeue(0));
                        singleService = getServiceGate(r);
                        sum[s].service += singleService;
                        sum[s].served++;
                        event[s].t = t.current + singleService;
                        event[s].arrival_time=block.arrival_time;
                        event[s].service=singleService;
                        event[s].departure= t.current + singleService;
                        event[s].job=block.job;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if(s>=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2+Values.SERVERS_IMBARCO_DEDICATO+1 && s<=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA*2+2+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1+Values.SERVERS_CONTROLLI_APPROFODNITI+2+Values.SERVERS_IMBARCO_DEDICATO+Values.SERVERS_IMBARCO ){
                    System.out.println("\n*** Sto processando una departure del server imbarco***");
                    number--;

                    nodes[14].number--;
                    nodes[14].index++;
                    double ResponseTime=event[s].departure-event[s].arrival_time;
                    ResponseTimeImbarcoN.add(ResponseTime);
                    //risposta_globali[job]+=ResponseTime;
                    double WaitingTime=ResponseTime-event[s].service;
                    WaitImbarcoN.add(WaitingTime);
                    if (nodes[14].number >= Values.SERVERS_IMBARCO) {
                        System.out.println("CODA PRIO"+number_queues[1]+"\nCODA NON PRIO:"+number_queues[0]);
                        if(number_queues[1]>0){
                            Block block = new Block(Queue_imbarco1.dequeue(1));
                            singleService = getServiceGate(r);
                            sum[s].service += singleService;
                            sum[s].served++;
                            event[s].t = t.current + singleService;
                            event[s].priority= block.priority;
                            event[s].passenger_type= block.type;
                           number_queues[1] -= 1;
                            event[s].arrival_time=block.arrival_time;
                            event[s].service=singleService;
                            event[s].departure= t.current + singleService;
                            event[s].job=block.job;
                       }
                       else if(number_queues[0]>0){
                            Block block = new Block(Queue_imbarco1.dequeue(0));
                            singleService = getServiceGate(r);
                            sum[s].service += singleService;
                            sum[s].served++;
                            event[s].t = t.current + singleService;
                            number_queues[0] -= 1;
                            event[s].priority= block.priority;
                            event[s].passenger_type= block.type;
                            event[s].arrival_time=block.arrival_time;
                            event[s].service=singleService;
                            event[s].departure= t.current + singleService;
                            event[s].job=block.job;
                        }

                    }
                    else {
                        event[s].x = 0;
                    }
                }
                else{
                    event[s].x = 0;
                }
            }

            iteration++;
            /*System.out.println("\n");
            System.out.println("\n");
            System.out.println("ITERAZIONE N " + iteration);
            System.out.println("Random: " + rnd);
            System.out.println("Index: " + index);
            System.out.println("Index nodo 0: " + nodes[0].index);
            System.out.println("Index nodo 1: " + nodes[1].index);
            System.out.println("Number: " + number);
            System.out.println("Number nodo 0: " + nodes[0].number);
            System.out.println("Number nodo 1: " + nodes[1].number);
            System.out.println("Next event: " + e);
            System.out.println("Next event time: " + event[e].t);
            System.out.println("Event status: " + event[e].x);
            //System.out.println("Event node: " + event[e].node);
            System.out.println("Service time: " + sum[e].service);
            System.out.println("Number served: " + sum[e].served);
            System.out.println("Area: " + area);
            System.out.println("Area nodo 1: " + areas[1].node);
            System.out.println("Area coda 1: " + areas[1].queue);
            System.out.println("Area servizio 1: " + areas[1].service);
            System.out.println("Area nodo 0: " + areas[0].node);
            System.out.println("Area coda 0: " + areas[0].queue);
            System.out.println("Area servizio 0: " + areas[0].service);
            System.out.println("Time current: " + t.current);
            System.out.println("Time next: " + t.next);
            System.out.println("\n");*/

        }
        DecimalFormat f = new DecimalFormat("###0.00");
        DecimalFormat g = new DecimalFormat("###0.000");
        System.out.println("\nthe server statistics are:\n");
        System.out.println("    server     utilization     avg service      share");
        for (s = 1+1; s <= 1+Values.SERVERS_BIGLIETTERIA; s++) {
            if (event[s].t > tBigliett) {
                tBigliett = event[s].t;
            }
        }
        for (s = 1+1; s <= 1+Values.SERVERS_BIGLIETTERIA; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tBigliett) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index)+ "         "+tBigliett);
        }
        for (s = 1+Values.SERVERS_BIGLIETTERIA+1; s <=1+Values.SERVERS_BIGLIETTERIA+ Values.SERVERS_DEDICATO_BIGLIETTERIA; s++) {
            if (event[s].t > tBigliettD) {
                tBigliettD = event[s].t;
            }
        }
        for (s = 1+Values.SERVERS_BIGLIETTERIA+1; s <=1+Values.SERVERS_BIGLIETTERIA+ Values.SERVERS_DEDICATO_BIGLIETTERIA; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tBigliettD) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index)+ "         "+tBigliett);
        }
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVER_DEDICATO_CHECK_IN; s++) {
            if (event[s].t > tCheckIn) {
                tCheckIn = event[s].t;
            }
        }
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVER_DEDICATO_CHECK_IN; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tCheckIn) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index)+ "         "+tBigliett);
        }
        for ( s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVER_DEDICATO_CHECK_IN + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_CHECK_IN; s++
        ) {
            if (event[s].t > tCheckInD) {
                tCheckInD = event[s].t;
            }
        }
        for ( s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVER_DEDICATO_CHECK_IN + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_CHECK_IN; s++
        ) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tCheckInD) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index)+ "         "+tBigliett);
        }
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2; s++)
         {
            if (event[s].t > tScann) {
                tScann = event[s].t;
            }
        }
        for ( s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2; s++)
         {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tScann) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index)+ "         "+tBigliett);
        }
        for ( s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2; s++)
        {
            if (event[s].t > tScannD) {
                tScannD = event[s].t;
            }
        }
        for ( s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2; s++)
        {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tScannD) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index)+ "         "+tBigliett);
        }
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_DEDICATI_SECURITY + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_DEDICATI_SECURITY + Values.SERVERS_SECURITY; s++)
        {
            if (event[s].t > tSecur) {
                tSecur = event[s].t;
            }
        }
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_DEDICATI_SECURITY + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_DEDICATI_SECURITY + Values.SERVERS_SECURITY; s++)
        {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tSecur) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index)+ "         "+tSecur);
        }
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_DEDICATI_SECURITY; s++)
        {
            if (event[s].t > tSecurD) {
                tSecurD = event[s].t;
            }
        }
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_DEDICATI_SECURITY; s++)
        {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tSecurD) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index)+ "         "+tSecurD);
        }
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI; s++)
        {
            if (event[s].t > tSecur2) {
                tSecur2 = event[s].t;
            }
        }
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI; s++)
        {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tSecur2) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index)+ "         "+tSecur2);
        }
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + Values.SERVERS_IMBARCO_DEDICATO; s++)
        {
            if (event[s].t > tGateD) {
                tGateD = event[s].t;
            }
        }
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + Values.SERVERS_IMBARCO_DEDICATO; s++)
        {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tGateD) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index)+ "         "+tGateD);
        }
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + Values.SERVERS_IMBARCO_DEDICATO + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + Values.SERVERS_IMBARCO_DEDICATO + Values.SERVERS_IMBARCO; s++)
        {
            if (event[s].t > tGate) {
                tGate = event[s].t;
            }
        }
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + Values.SERVERS_IMBARCO_DEDICATO + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + Values.SERVERS_IMBARCO_DEDICATO + Values.SERVERS_IMBARCO; s++)
        {
            System.out.print("       " + s + "          " + g.format(sum[s].service / tGate) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index)+ "         "+tGate);
        }
        System.out.println("*********************************");
        double time1=0.0;
        int count=0;
        for (s=0; s<= ResponseTimeN.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA Biglietteria N "+s+"E':"+ ResponseTimeN.get(s));
            count++;
            time1+=ResponseTimeN.get(s);
        }
        System.out.println("Il tempo di risposta medio è:"+(time1/count));
        System.out.println("*********************************");

        time1=0.0;
        count=0;
        for (s=0; s<= ResponseTimeFF.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA Biglietteria FF "+s+"E':"+ ResponseTimeN.get(s));
            time1+=ResponseTimeFF.get(s);
            count++;
        }
        System.out.println("Il tempo di risposta medio è:"+(time1/count));
        System.out.println("*********************************");
        time1=0.0;
        count=0;
        for (s=0; s<= ResponseTimeCheckInN.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA check in N "+s+"E':"+ ResponseTimeCheckInN.get(s));
            time1+=ResponseTimeCheckInN.get(s);
            count++;
        }
        System.out.println("Il tempo di risposta medio è:"+(time1/count));
        System.out.println("*********************************");
        time1=0.0;
        count=0;
        for (s=0; s<= ResponseTimeCheckInFF.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA check in FF "+s+"E':"+ ResponseTimeCheckInFF.get(s));
            time1+=ResponseTimeCheckInFF.get(s);
            count++;
        }
        System.out.println("Il tempo di risposta medio è:"+(time1/count));
        System.out.println("*********************************");
        time1=0.0;
        count=0;
        for (s=0; s<= ResponseTimeCartaN.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA scansione carta N "+s+"E':"+ ResponseTimeCartaN.get(s));
            time1+=ResponseTimeCartaN.get(s);
            count++;
        }
        System.out.println("Il tempo di risposta medio è:"+(time1/count));
        System.out.println("*********************************");
        time1=0.0;
        count=0;
        for (s=0; s<= ResponseTimeCartaFF.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA scansione carta FF "+s+"E':"+ ResponseTimeCartaFF.get(s));
            time1+=ResponseTimeCartaFF.get(s);
            count++;
        }
        System.out.println("Il tempo di risposta medio è:"+(time1/count));
        System.out.println("*********************************");
        time1=0.0;
        count=0;
        for (s=0; s<= ResponseTimeSecurityN.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA security N "+s+"E':"+ ResponseTimeSecurityN.get(s));
            time1+=ResponseTimeSecurityN.get(s);
            count++;
        }
        System.out.println("Il tempo di risposta medio è:"+(time1/count));
        System.out.println("*********************************");
        time1=0.0;
        count=0;
        for (s=0; s<= ResponseTimeSecurityFF.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA security FF "+s+"E':"+ ResponseTimeSecurityFF.get(s));
            time1+=ResponseTimeSecurityFF.get(s);
            count++;
        }
        System.out.println("Il tempo di risposta medio è:"+(time1/count));
        System.out.println("*********************************");
        time1=0.0;
        count=0;
        for (s=0; s<= ResponseTimeSecurityAppN.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA security app N "+s+"E':"+ ResponseTimeSecurityAppN.get(s));
            time1+=ResponseTimeSecurityAppN.get(s);
            count++;
        }
        System.out.println("Il tempo di risposta medio è:"+(time1/count));
        System.out.println("*********************************");
        time1=0.0;
        count=0;
        for (s=0; s<= ResponseTimeSecurityAppFF.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA security app FF "+s+"E':"+ ResponseTimeSecurityAppN.get(s));
            time1+=ResponseTimeSecurityAppFF.get(s);
            count++;
        }
        System.out.println("Il tempo di risposta medio è:"+(time1/count));
        System.out.println("*********************************");
        time1=0.0;
        count=0;
        for (s=0; s<= ResponseTimeImbarcoN.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA imbarco  N "+s+"E':"+ ResponseTimeImbarcoN.get(s));
            time1+=ResponseTimeImbarcoN.get(s);
            count++;
        }
        System.out.println("Il tempo di risposta medio è:"+(time1/count));
        System.out.println("*********************************");
        time1=0.0;
        count=0;
        for (s=0; s<= ResponseTimeImbarcoFF.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA imbarco FF "+s+"E':"+ ResponseTimeImbarcoFF.get(s));
            time1+=ResponseTimeImbarcoFF.get(s);
            count++;
        }
        System.out.println("Il tempo di risposta medio è:"+(time1/count));

/***************************************************************************************************/
/*
        System.out.println("*********************************");
        count=0;
        time1=0.0;
        for (s=0; s<=WaitN.size()-1; s++){
            System.out.println("IL TEMPO DI ATTESA Biglietteria N "+s+"E':"+ WaitN.get(s));
            time1+=WaitN.get(s);
            count++;
        }System.out.println("IL COUNT DEI JOB IN BIGLIETTERIA E'"+count);
        System.out.println("L'attesa media biglitteria è:'"+(time1/count));
        System.out.println("*********************************");
        int countD=0;
        time1=0.0;
        for (s=0; s<= WaitFF.size()-1; s++){
            System.out.println("IL TEMPO DI ATTESA Biglietteria FF "+s+"E':"+ WaitFF.get(s));
            time1+=WaitFF.get(s);
            countD++;
        }
        System.out.println("IL COUNT DEI JOB IN BIGLIETTERIA DEDICATA E'"+countD);
        System.out.println("L'attesa media è:'"+(time1/count));
        System.out.println("*********************************");
        count =0;
        time1=0.0;
        for (s=0; s<= WaitCheckInN.size()-1; s++){
            System.out.println("IL TEMPO DI ATTESA Check in N "+s+"E':"+ WaitCheckInN.get(s));
            time1+=WaitCheckInN.get(s);
            count++;
        }
        time1=0.0;
        System.out.println("IL COUNT DEI JOB IN check in E'"+count);
        System.out.println("L'attesa media è:'"+(time1/count));
        System.out.println("*********************************");
        count=0;
        for (s=0; s<= WaitCheckInFF.size()-1; s++){
            System.out.println("IL TEMPO DI ATTESA check in FF "+s+"E':"+ WaitCheckInFF.get(s));
            time1+=WaitCheckInFF.get(s);
            count++;
        }
        time1=0.0;
        System.out.println("IL COUNT DEI JOB IN check in ff E'"+count);
        System.out.println("L'attesa media è:'"+(time1/count));
        count=0;
        System.out.println("*********************************");
        for (s=0; s<= WaitCartaN.size()-1; s++){
            System.out.println("IL TEMPO DI ATTESA scansione carta N "+s+"E':"+ WaitCartaN.get(s));
            time1+=WaitCartaN.get(s);
            count++;
        }
        time1=0.0;
        System.out.println("IL COUNT DEI JOB IN scansione carta n E'"+count);
        System.out.println("L'attesa media è:'"+(time1/count));
        System.out.println("*********************************");
        count=0;
        time1=0.0;
        for (s=0; s<= WaitCartaFF.size()-1; s++){
            System.out.println("IL TEMPO DI ATTESA scansione carta FF "+s+"E':"+ WaitCartaFF.get(s));
            time1+=WaitCartaFF.get(s);
            count++;
        }
        System.out.println("IL COUNT DEI JOB IN scansione carta ff E'"+count);
        System.out.println("L'attesa media è:'"+(time1/count));
        System.out.println("*********************************");
        count=0;
        time1=0.0;

        for (s=0; s<= WaitSecurityN.size()-1; s++){
            System.out.println("IL TEMPO DI ATTESA security N "+s+"E':"+ WaitSecurityN.get(s));
            time1+=WaitSecurityN.get(s);
            count++;
        }
        System.out.println("IL COUNT DEI JOB IN security n E'"+count);
        System.out.println("L'attesa media è:'"+(time1/count));
        System.out.println("*********************************");
        count=0;
        time1=0.0;
        for (s=0; s<= WaitSecurityFF.size()-1; s++){
            System.out.println("IL TEMPO DI ATTESA security FF "+s+"E':"+ WaitSecurityFF.get(s));
            time1+=WaitSecurityFF.get(s);
            count++;
        }
        time1=0.0;
        System.out.println("IL COUNT DEI JOB IN security ff E'"+count);
        System.out.println("L'attesa media è:'"+(time1/count));
        System.out.println("*********************************");
        count=0;
        time1=0.0;
        for (s=0; s<= WaitSecurityAppN.size()-1; s++){
            System.out.println("IL TEMPO DI ATTESA security app  "+s+"E':"+ WaitSecurityAppN.get(s));
            time1+=WaitSecurityAppN.get(s);
            count++;
        }
        time1=0.0;
        System.out.println("IL COUNT DEI JOB IN security app E'"+count);
        System.out.println("L'attesa media è:'"+(time1/count));
   System.out.println("*********************************");
        count=0;
        time1=0.0;
        for (s=0; s<= WaitSecurityAppFF.size()-1; s++){
            System.out.println("IL TEMPO DI ATTESA security app FF "+s+"E':"+ WaitSecurityAppFF.get(s));
            time1+=WaitSecurityAppN.get(s);
            count++;
        }
        time1=0.0;
        System.out.println("IL COUNT DEI JOB IN security app FF E'"+count);
        System.out.println("L'attesa media è:'"+(time1/count));

        System.out.println("*********************************");
        count=0;
        time1=0.0;
        for (s=0; s<= WaitImbarcoN.size()-1; s++){
            System.out.println("IL TEMPO DI ATTESA imbarco N "+s+"E':"+ WaitImbarcoN.get(s));
            time1+=WaitImbarcoN.get(s);
            count++;
        }
        time1=0.0;
        System.out.println("IL COUNT DEI JOB IN imbarco N E'"+count);
        System.out.println("L'attesa media è:'"+(time1/count));
        System.out.println("*********************************");
        count=0;
        time1=0.0;
        for (s=0; s<= WaitImbarcoFF.size()-1; s++){
            System.out.println("IL TEMPO DI ATTESA imbarco FF "+s+"E':"+ WaitImbarcoFF.get(s));
            time1+=WaitImbarcoFF.get(s);
            count++;
        }


        System.out.println("IL COUNT DEI JOB IN imbarco ff E'"+count);
        System.out.println("L'attesa media è:'"+(time1/count));
        /*
        System.out.println("num wait"+numb_wait_bigl);
        System.out.println("num wait"+numb_wait_biglD);
        System.out.println("num wait"+numb_wait_checkin);
        System.out.println("num wait"+numb_wait_checkinD);
        System.out.println("num wait"+numb_wait_carta);
        System.out.println("num wait"+numb_wait_cartaD);
        System.out.println("num wait"+numb_wait_security);
        System.out.println("num wait"+numb_wait_securityD);
        System.out.println("num wait"+numb_wait_security_app);
        System.out.println("num wait"+numb_wait_imbarco);
        System.out.println("num wait"+numb_wait_imbarcoD);
        int q=1;
        int count1=0;
        double time_service=0;
        double tempo=0.0;

        for (s = 1+1; s <= 1+Values.SERVERS_BIGLIETTERIA; s++) {


                count1+= sum[s].served;
                tempo+=sum[s].service;

        }

        System.out.println("La media servizio biglietteria è:"+(tempo/count));

       count1=0;
       tempo=0.0;
        for (s = 1+Values.SERVERS_BIGLIETTERIA+1; s <=1+Values.SERVERS_BIGLIETTERIA+ Values.SERVERS_DEDICATO_BIGLIETTERIA; s++) {
            count1+= sum[s].served;
            tempo+=sum[s].service;
        }   System.out.println("La media servizio biglietteria è:"+(tempo/count));
        count1=0;
        tempo=0.0;

        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVER_DEDICATO_CHECK_IN; s++) {
            count1+= sum[s].served;
            tempo+=sum[s].service;
        }
        System.out.println("La media servizio biglietteria è:"+(tempo/count));
        count1=0;
        tempo=0.0;
        for ( s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVER_DEDICATO_CHECK_IN + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_CHECK_IN; s++
        ) {
            count1+= sum[s].served;
            tempo+=sum[s].service;
        }   System.out.println("La media servizio biglietteria è:"+(tempo/count));
        count1=0;
        tempo=0.0;
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2; s++)
         {
             count1+= sum[s].served;
             tempo+=sum[s].service;
        }   System.out.println("La media servizio biglietteria è:"+(tempo/count));
        count1=0;
        tempo=0.0;

        for ( s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2; s++)
        {
            count1+= sum[s].served;
            tempo+=sum[s].service;
        }   System.out.println("La media servizio biglietteria è:"+(tempo/count));
        count1=0;
        tempo=0.0;

        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_DEDICATI_SECURITY + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_DEDICATI_SECURITY + Values.SERVERS_SECURITY; s++)
        {
            count1+= sum[s].served;
            tempo+=sum[s].service;
        }   System.out.println("La media servizio biglietteria è:"+(tempo/count));
        count1=0;
        tempo=0.0;

        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_DEDICATI_SECURITY; s++)
        {
            count1+= sum[s].served;
            tempo+=sum[s].service;
        }   System.out.println("La media servizio biglietteria è:"+(tempo/count));
        count1=0;
        tempo=0.0;


        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI; s++)
        {
            count1+= sum[s].served;
            tempo+=sum[s].service;
        }   System.out.println("La media servizio biglietteria è:"+(tempo/count));
        count1=0;
        tempo=0.0;

        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + Values.SERVERS_IMBARCO_DEDICATO; s++)
        {
            count1+= sum[s].served;
            tempo+=sum[s].service;
        }   System.out.println("La media servizio biglietteria è:"+(tempo/count));
        count1=0;
        tempo=0.0;
        for (s = 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + Values.SERVERS_IMBARCO_DEDICATO + 1; s <= 1 + Values.SERVERS_BIGLIETTERIA + Values.SERVERS_DEDICATO_BIGLIETTERIA + 2 + Values.SERVERS_CHECK_IN + Values.SERVER_DEDICATO_CHECK_IN + Values.SERVERS_SCANSIONE_CARTA * 2 + Values.SERVER_SCANSIONE_CARTA_DEDICAT0 * 2 + 2 + Values.SERVERS_SECURITY + Values.SERVERS_DEDICATI_SECURITY + 1 + Values.SERVERS_CONTROLLI_APPROFODNITI + 2 + Values.SERVERS_IMBARCO_DEDICATO + Values.SERVERS_IMBARCO; s++)
        {
            count1+= sum[s].served;
            tempo+=sum[s].service;
        }   System.out.println("La media servizio biglietteria è:"+(tempo/count));
*/

        Path waitBiglietteriaFF = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\waitBiglietteriaFF.txt");
        Path waitBiglietteriaN = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\waitBiglietteriaN.txt");
        Path waitCheckinFF = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\waitCheckinFF.txt");
        Path waitCheckinN = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\waitCheckinN.txt");
        Path waitScannerFF = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\waitScannerFF.txt");
        Path waitScannerN = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\waitScannerN.txt");
        Path waitSecurityFF = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\waitSecurityFF.txt");
        Path waitSecurityN = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\waitSecurityN.txt");
        Path waitSecurity2FF = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\waitSecurity2FF.txt");
        Path waitSecurity2N = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\waitSecurity2N.txt");
        Path waitImbarcoFF = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\waitImbarcoFF.txt");
        Path waitImbarcoN = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\waitImbarcoN.txt");

        Path responseBiglietteriaFF = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseBiglietteriaFF.txt");
        Path responseBiglietteriaN = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseBiglietteriaN.txt");
        Path responseCheckinFF = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseCheckinFF.txt");
        Path responseCheckinN = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseCheckinN.txt");
        Path responseScannerFF = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseScannerFF.txt");
        Path responseScannerN = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseScannerN.txt");
        Path responseSecurityFF = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseSecurityFF.txt");
        Path responseSecurityN = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseSecurityN.txt");
        Path responseSecurity2FF = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseSecurity2FF.txt");
        Path responseSecurity2N = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseSecurity2N.txt");
        Path responseImbarcoFF = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseImbarcoFF.txt");
        Path responseImbarcoN = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\responseImbarcoN.txt");

        String out = "";
        System.out.println("*********************************");
        double summa = 0.0;
        for (s=0; s<= ResponseTimeFF.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA "+s+"E':"+ ResponseTimeFF.get(s));
            out += s+","+ResponseTimeFF.get(s)+"\n";
            summa += ResponseTimeFF.get(s);
        }
        double meanBiglRespFF = summa/ResponseTimeFF.size();
        Files.writeString(responseBiglietteriaFF, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= ResponseTimeN.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA "+s+"E':"+ ResponseTimeN.get(s));
            out += s+","+ResponseTimeN.get(s)+"\n";
            summa += ResponseTimeN.get(s);
        }
        double meanRespBiglN = summa/ResponseTimeN.size();
        Files.writeString(responseBiglietteriaN, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= ResponseTimeCheckInFF.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA "+s+"E':"+ ResponseTimeCheckInFF.get(s));
            out += s+","+ResponseTimeCheckInFF.get(s)+"\n";
            summa += ResponseTimeCheckInFF.get(s);
        }
        double meanRespCheckFF = summa/ResponseTimeCheckInFF.size();
        Files.writeString(responseCheckinFF, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= ResponseTimeCheckInN.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA "+s+"E':"+ ResponseTimeCheckInN.get(s));
            out += s+","+ResponseTimeCheckInN.get(s)+"\n";
            summa += ResponseTimeCheckInN.get(s);
        }
        double meanRespCheckN = summa/ResponseTimeCheckInN.size();
        Files.writeString(responseCheckinN, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= ResponseTimeSecurityN.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA "+s+"E':"+ ResponseTimeSecurityN.get(s));
            out += s+","+ResponseTimeSecurityN.get(s)+"\n";
            summa += ResponseTimeSecurityN.get(s);
        }
        double meanRespSecN = summa/ResponseTimeSecurityN.size();
        Files.writeString(responseSecurityN, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= ResponseTimeSecurityFF.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA "+s+"E':"+ ResponseTimeSecurityFF.get(s));
            out += s+","+ResponseTimeSecurityFF.get(s)+"\n";
            summa += ResponseTimeSecurityFF.get(s);
        }
        double meanRespSecFF = summa/ResponseTimeSecurityFF.size();
        Files.writeString(responseSecurityFF, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= ResponseTimeImbarcoFF.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA "+s+"E':"+ ResponseTimeImbarcoFF.get(s));
            out += s+","+ResponseTimeImbarcoFF.get(s)+"\n";
            summa += ResponseTimeImbarcoFF.get(s);
        }
        double meanRespImbFF = summa/ResponseTimeImbarcoFF.size();
        Files.writeString(responseImbarcoFF, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= ResponseTimeImbarcoN.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA "+s+"E':"+ ResponseTimeImbarcoN.get(s));
            out += s+","+ResponseTimeImbarcoN.get(s)+"\n";
            summa += ResponseTimeImbarcoN.get(s);
        }
        double meanRespImbN = summa/ResponseTimeImbarcoN.size();
        Files.writeString(responseImbarcoN, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= ResponseTimeSecurityAppN.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA "+s+"E':"+ ResponseTimeSecurityAppN.get(s));
            out += s+","+ResponseTimeSecurityAppN.get(s)+"\n";
            summa += ResponseTimeSecurityAppN.get(s);
        }
        double meanRespSecAppN = summa/ResponseTimeSecurityAppN.size();
        Files.writeString(responseSecurity2N, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= ResponseTimeSecurityAppFF.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA "+s+"E':"+ ResponseTimeSecurityAppFF.get(s));
            out += s+","+ResponseTimeSecurityAppFF.get(s)+"\n";
            summa += ResponseTimeSecurityAppFF.get(s);
        }
        double meanRespSecAppFF = summa/ResponseTimeSecurityAppFF.size();
        Files.writeString(responseSecurity2FF, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= ResponseTimeCartaN.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA "+s+"E':"+ ResponseTimeCartaN.get(s));
            out += s+","+ResponseTimeCartaN.get(s)+"\n";
            summa += ResponseTimeCartaN.get(s);
        }
        double meanRespCartaN = summa/ResponseTimeCartaN.size();
        Files.writeString(responseScannerN, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= ResponseTimeCartaFF.size()-1; s++){
            //System.out.println("IL TEMPO DI RISPOSTA "+s+"E':"+ ResponseTimeCartaFF.get(s));
            out += s+","+ResponseTimeCartaFF.get(s)+"\n";
            summa += ResponseTimeCartaFF.get(s);
        }
        double meanRespCartaFF = summa/ResponseTimeCartaFF.size();
        Files.writeString(responseScannerFF, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        System.out.println("*********************************");
        for (s=0; s<= WaitCartaFF.size()-1; s++){
            //System.out.println("IL TEMPO DI ATTESA "+s+"E':"+ WaitCartaFF.get(s));
            double value = WaitCartaFF.get(s);
            if (value < 0)
                value = 0.0;
            out += value+"\n";
            summa += value;
        }
        double meanWaitCartaFF = summa/WaitCartaFF.size();
        Files.writeString(waitScannerFF, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= WaitCartaN.size()-1; s++){
            //System.out.println("IL TEMPO DI ATTESA "+s+"E':"+ WaitCartaN.get(s));
            double value = WaitCartaN.get(s);
            if (value < 0)
                value = 0.0;
            out += value+"\n";
            summa += value;
        }
        double meanWaitCartaN = summa/WaitCartaN.size();
        Files.writeString(waitScannerN, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= WaitFF.size()-1; s++){
            //System.out.println("IL TEMPO DI ATTESA "+s+"E':"+ WaitFF.get(s));
            double value = WaitFF.get(s);
            if (value < 0)
                value = 0.0;
            out += value+"\n";
            summa += value;
        }
        double meanWaitBiglFF = summa/WaitFF.size();
        Files.writeString(waitBiglietteriaFF, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= WaitN.size()-1; s++){
            //System.out.println("IL TEMPO DI ATTESA "+s+"E':"+ WaitN.get(s));
            double value = WaitN.get(s);
            if (value < 0)
                value = 0.0;
            out += value+"\n";
            summa += value;
        }
        double meanWaitBiglN = summa/WaitN.size();
        Files.writeString(waitBiglietteriaN, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= WaitCheckInFF.size()-1; s++){
            //System.out.println("IL TEMPO DI ATTESA "+s+"E':"+ WaitCheckInFF.get(s));
            double value = WaitCheckInFF.get(s);
            if (value < 0)
                value = 0.0;
            out += value+"\n";
            summa += value;
        }
        double meanWaitCheckFF = summa/WaitCheckInFF.size();
        Files.writeString(waitCheckinFF, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= WaitCheckInN.size()-1; s++){
            //System.out.println("IL TEMPO DI ATTESA "+s+"E':"+ WaitCheckInN.get(s));
            double value = WaitCheckInN.get(s);
            if (value < 0)
                value = 0.0;
            out += value+"\n";
            summa += value;
        }
        double meanWaitCheckN = summa/WaitCheckInN.size();
        Files.writeString(waitCheckinN, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= WaitSecurityFF.size()-1; s++){
            //System.out.println("IL TEMPO DI ATTESA "+s+"E':"+ WaitSecurityFF.get(s));
            double value = WaitSecurityFF.get(s);
            if (value < 0)
                value = 0.0;
            out += value+"\n";
            summa += value;
        }
        double meanWaitSecFF = summa/WaitSecurityFF.size();
        Files.writeString(waitSecurityFF, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= WaitSecurityN.size()-1; s++){
            //System.out.println("IL TEMPO DI ATTESA "+s+"E':"+ WaitSecurityN.get(s));
            double value = WaitSecurityN.get(s);
            if (value < 0)
                value = 0.0;
            out += value+"\n";
            summa += value;
        }
        double meanWaitSecN = summa/WaitSecurityN.size();
        Files.writeString(waitSecurityN, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= WaitImbarcoFF.size()-1; s++){
            //System.out.println("IL TEMPO DI ATTESA "+s+"E':"+ WaitImbarcoFF.get(s));
            double value = WaitImbarcoFF.get(s);
            if (value < 0)
                value = 0.0;
            out += value+"\n";
            summa += value;
        }
        double meanWaitImbFF = summa/WaitImbarcoFF.size();
        Files.writeString(waitImbarcoFF, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= WaitImbarcoN.size()-1; s++){
            //System.out.println("IL TEMPO DI ATTESA "+s+"E':"+ WaitImbarcoN.get(s));
            double value = WaitImbarcoN.get(s);
            if (value < 0)
                value = 0.0;
            out += value+"\n";
            summa += value;
        }
        double meanWaitImbN = summa/WaitImbarcoN.size();
        Files.writeString(waitImbarcoN, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= WaitSecurityAppFF.size()-1; s++){
            //System.out.println("IL TEMPO DI ATTESA "+s+"E':"+ WaitSecurityAppFF.get(s));
            double value = WaitSecurityAppFF.get(s);
            if (value < 0)
                value = 0.0;
            out += value+"\n";
            summa += value;
        }
        double meanWaitSecAppFF = summa/WaitSecurityAppFF.size();
        Files.writeString(waitSecurity2FF, out);
        out = "";
        summa = 0.0;
        System.out.println("*********************************");
        for (s=0; s<= WaitSecurityAppN.size()-1; s++){
            //System.out.println("IL TEMPO DI ATTESA "+s+"E':"+ WaitSecurityAppN.get(s));
            double value = WaitSecurityAppN.get(s);
            if (value < 0)
                value = 0.0;
            out += value+"\n";
            summa += value;
        }
        double meanWaitSecAppN = summa/WaitSecurityAppN.size();
        Files.writeString(waitSecurity2N, out);
        System.out.println("*********************************");
        System.out.println("*********************************");
        int q=1;
        count=0;
        double tempo=0.0;
        for (s = 1; s <= Values.SERVERS_BIGLIETTERIA; s++) {
            count+= sum[s].served;
            tempo+=sum[s].service;
        }
        System.out.println("La media servizio biglietteria è:"+(tempo/count));
        q=1;
        tempo=0.0;
        count=0;


        q=1;
        for (s = Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2; s <= Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+1+Values.SERVERS_CHECK_IN; s++) {
            count+= sum[s].served;
            tempo+=sum[s].service;
        }
        System.out.println("La media servizio check in è:"+(tempo/count));
        tempo=0.0;
        count=0;
        for (s = Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+1+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+8; s <= Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+1+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+7+Values.SERVERS_SCANSIONE_CARTA; s++) {
            count+= sum[s].served;
            tempo+=sum[s].service;
        }
        System.out.println("La media servizio carta imbarco è:"+(tempo/count));

        tempo=0.0;
        count=0;
        for (s = Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+1+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+7+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+2; s <= Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+1+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+7+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+1+Values.SERVERS_SECURITY; s++) {
            count+= sum[s].served;
            tempo+=sum[s].service;
        }
        System.out.println("La media servizio security è:"+(tempo/count));

        tempo=0.0;
        count=0;

        for (s = Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+1+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+7+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+1+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+3; s <= Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+1+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+7+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+1+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO; s++) {
            count+= sum[s].served;
            tempo+=sum[s].service;
        }
        System.out.println("La media servizio imbarco è:"+(tempo/count));
        tempo=0.0;
        count=0;

        for (s = Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+1+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+7+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+1+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO+Values.SERVERS_IMBARCO_DEDICATO+1; s <= Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+1+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+7+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+1+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO+Values.SERVERS_IMBARCO_DEDICATO+Values.SERVERS_CONTROLLI_APPROFODNITI; s++) {
            count+= sum[s].served;
            tempo+=sum[s].service;
        }
        System.out.println("La media servizio controllo approfondito è:"+(tempo/count));


        System.out.println("Tempo di risposta medio biglietteria FF: " + meanBiglRespFF);
        System.out.println("Tempo di risposta medio biglietteria N: " + meanRespBiglN);
        System.out.println("Tempo di risposta medio check in FF: " + meanRespCheckFF);
        System.out.println("Tempo di risposta medio check in N: " + meanRespCheckN);
        System.out.println("Tempo di risposta medio scanner FF: " + meanRespCartaFF);
        System.out.println("Tempo di risposta medio scanner N: " + meanRespCartaN);
        System.out.println("Tempo di risposta medio security FF: " + meanRespSecFF);
        System.out.println("Tempo di risposta medio security N: " + meanRespSecN);
        System.out.println("Tempo di risposta medio securityApp FF: " + meanRespSecAppFF);
        System.out.println("Tempo di risposta medio securityApp N: " + meanRespSecAppN);
        System.out.println("Tempo di risposta medio imbarco FF: " + meanRespImbFF);
        System.out.println("Tempo di risposta medio imbarco N: " + meanRespImbN);

        System.out.println("Tempo di attesa medio biglietteria FF: " + meanWaitBiglFF);
        System.out.println("Tempo di attesa medio biglietteria N: " + meanWaitBiglN);
        System.out.println("Tempo di attesa medio check in FF: " + meanWaitCheckFF);
        System.out.println("Tempo di attesa medio check in FF: " + meanWaitCheckN);
        System.out.println("Tempo di attesa medio scanner FF: " + meanWaitCartaFF);
        System.out.println("Tempo di attesa medio scanner N: " + meanWaitCartaN);
        System.out.println("Tempo di attesa medio security FF: " + meanWaitSecFF);
        System.out.println("Tempo di attesa medio security N: " + meanWaitSecN);
        System.out.println("Tempo di attesa medio securityApp FF: " + meanWaitSecAppFF);
        System.out.println("Tempo di attesa medio securityApp N: " + meanWaitSecAppN);
        System.out.println("Tempo di attesa medio imbarco FF: " + meanWaitImbFF);
        System.out.println("Tempo di attesa medio imbarco N: " + meanWaitImbN);

        /*System.out.println(numberBigl);
        System.out.println(numberBiglDed);
        System.out.println(numberCheck);
        System.out.println(numberCheckDed);
        System.out.println(numberCarta);
        System.out.println(numberCartaDedic);
        System.out.println(numberSecurity);
        System.out.println(numberSecurityDedic);
        System.out.println(numberImbarco);
        System.out.println(numberImbarcoDedic);
        System.out.println(numberContrApp);
        System.out.println(numberContrAppDedic);*/

        Path risp_glob = Path.of("C:\\Users\\Ilenia\\Desktop\\valori\\risp_globali.txt");
        out = "";
        System.out.println(jobCounter);
        //double m = risposta_globali[risposta_globali.length-1]/(InfiniteHorizonBatchSimulation.numBatches*InfiniteHorizonBatchSimulation.batchSize);
        /*for(int poiu=0;poiu<InfiniteHorizonBatchSimulation.numBatches*InfiniteHorizonBatchSimulation.batchSize;poiu++){
            System.out.println("numero: "+poiu+"  "+risposta_globali[poiu]);
            if (poiu == risposta_globali.length-1)
                risposta_globali[poiu] = m;
            out +=risposta_globali[poiu]+m+"\n";
        }
        Files.writeString(risp_glob, out);*/


    }


        /*
        DecimalFormat f = new DecimalFormat("###0.00");

        System.out.println("\nfor " + index + " jobs");
        System.out.println("   average interarrival time =   " + f.format(e.last / index));
        System.out.println("   average wait ............ =   " + f.format(area.node / index));
        System.out.println("   average delay ........... =   " + f.format(area.queue / index));
        System.out.println("   average service time .... =   " + f.format(area.service / index));
        System.out.println("   average # in the node ... =   " + f.format(area.node / e.current));
        System.out.println("   average # in the queue .. =   " + f.format(area.queue / e.current));
        System.out.println("   utilization ............. =   " + f.format(area.service / e.current));
    } */


    public static double exponential(double m, Rngs r) {
        /* ---------------------------------------------------
         * generate an Exponential random variate, use m > 0.0
         * ---------------------------------------------------
         */
        return (-m * Math.log(1.0 - r.random()));
    }

    public static double uniform(double a, double b, Rngs r) {
        /* ------------------------------------------------
         * generate an Uniform random variate, use a < b
         * ------------------------------------------------
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
        return (ModelloIniziale.sarrival);
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
        r.selectStream(1);
        return (exponential(SERVICE, r));
    }


    public static int nextEvent(MsqEvent [] event) {
        /* ---------------------------------------
         * return the index of the next event type
         * ---------------------------------------
         */
        int e;
        int i = 0;

        while (event[i].x == 0)       /* find the index of the first 'active' */
            i++;                        /* element in the event list            */
        e = i;
        while (i < 999) {         /* now, check the others to find which  */
            i++;                          /* event type is most imminent          */
            if ((event[i].x == 1) && (event[i].t < event[e].t))
                e = i;
        }
        return (e);
    }

    public static int findOne(MsqEvent [] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i = 1;

        while (event[i].x == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < SERVERS) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
    public static int find_number_node(int e){
        if (e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+1 || e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA+1){
            return 5;
        }
        if(e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+2||  e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA+2){
            return 6;
        }
        if(e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+3 ||  e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA+3){
            return 7;

        }
        if(e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+4 ||  e==1+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0*2+Values.SERVERS_SCANSIONE_CARTA+4){
            return 8;
        }
        return -1;
    }
    public static int findOne(MsqEvent [] event, int min, int max) {

        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i = min;

        while (event[i].x == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < max) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
    public static int find_best_server_dedicato(){

        if(nodes[4].number<=nodes[9].number){
            return 1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+1;
        }
        return 1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+2;
    }
    public static int find_best_server(){
        int i=5;
        int current=5;
        while (i<8){
            if (nodes[i].number>nodes[i+1].number){
                current=i+1;
            }
            i++;
        }
        if(current==5){
            current=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+1;
        }
        if(current==6){
            current=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+2;
        }
        if(current==7){
            current=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+3;
        }
        if(current==8){
            current=1+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+4;
        }
        return current;
    }

}

