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


/*
    COSE DA RIVEDERE:
    - distribuzione tempi di arrivo e di servizio: tipo di distribuzione e valori dei parametri
    - generazione valore randomico per la scelta del nodo: modalità e valori, avanzamento del seed (idx)
    - cambiare NODES come valore della lista perché se non è 2 sfaciola
    - sostituire i numeri che indicizzano gli eventi con le variabili
    - aggiungere gestione fasce orarie (cambia il LAMBDA)
    - DA RICORDARE: il primo evento è l'arrivo al nodo 1, il secondo è l'arrivo al nodo 0, poi gli m server e poi il single server
 */

public class ModelloIniziale {
    static int SERVERS_DEDICATO=1;
    static double START = 0.0;              /* initial time                   */
    static double STOP  = 200.0;          /* terminal (close the door) time */
    static double INFINITY = 1000.0 * STOP;  /* must be much larger than STOP  */


    static int SERVERS = 4;              /* number of servers */
    static int NODES = 15;
    static int[] number_queues={0,0};
    static double sarrival = START;
    static  Node [] nodes = new Node [NODES];
    public static void main(String[] args) {

        long index = 0;                  /* used to count departed jobs         */
        long number = 0;                  /* number in the node                  */

        int idx = 1;

        Rngs_1 r = new Rngs_1();
        r.plantSeeds(123456789);


        //long   numberM = 0;             /* number in the node                 */
        int    e;                      /* next event index                   */
        int    s;                      /* server index                       */
        int    n;                       /* node index */
        //long   indexM  = 0;             /* used to count processed jobs       */
        double area   = 0.0;           /* time integrated number in the node */
        double multiService;
        double singleService;

        double rnd = 0.0;

        //Msq m = new Msq();
        Ssq3 ssq = new Ssq3();
        //r.plantSeeds(987654321);


        MsqEvent [] event = new MsqEvent [2 + SERVERS + SERVERS_DEDICATO+SERVERS+SERVERS_DEDICATO+2+2*SERVERS+2*SERVERS_DEDICATO+SERVERS+30];  /* tipologie di evento */
        MsqSum [] sum = new MsqSum [2 + SERVERS + SERVERS_DEDICATO+SERVERS+SERVERS_DEDICATO+2+2*SERVERS+2*SERVERS_DEDICATO+SERVERS+30];
       // Node [] nodes = new Node [NODES];
        Area[] areas = new Area[NODES];
        for (s = 0; s <2 + SERVERS + SERVERS_DEDICATO+SERVERS+SERVERS_DEDICATO+2+2*SERVERS+2*SERVERS_DEDICATO+SERVERS+30; s++) {
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
        event[1].t = getArrival(r);
        event[1].x = 1;

        for (n = 2; n < 2 + SERVERS + SERVERS_DEDICATO+SERVERS+SERVERS_DEDICATO+2+2*SERVERS+2*SERVERS_DEDICATO+SERVERS+30; n++) {
            event[n].t = START;
            event[n].x = 0;
            sum[n].service = 0.0;
            sum[n].served = 0;
        }
        MultiQueue Queue_security1=new MultiQueue(1);
        MultiQueue Queue_imbarco1=new MultiQueue(2);
        int iteration = 0;
        while ((event[0].x != 0) || event[1].x != 0 || (number != 0)) {

            System.out.println("Lista eventi:\n");
            System.out.println("Stato evento arrivo N: " + event[0].x);
            System.out.println("Stato evento arrivo FF: " + event[1].x);
            System.out.println("Server 1: " + event[2].x);
            System.out.println("Server 2: " + event[3].x);
            System.out.println("Server 3: " + event[4].x);
            System.out.println("Server 4: " + event[5].x);
            System.out.println("Server 5 (FF): " + event[6].x);
           // System.out.println("Number nodo N: " + nodes[1].number);
            //System.out.println("Number nodo FF: " + nodes[0].number);
            System.out.println("Coda nodo N: " + (nodes[1].number - event[2].x - event[3].x - event[4].x-event[5].x));
            System.out.println("Coda nodo FF: " + (nodes[0].number - 1));
            System.out.println("Server 1 check in: " + event[10].x);
            System.out.println("Server 2 check in: " + event[11].x);
            System.out.println("Server 3 check in: " + event[12].x);
            System.out.println("Server 4 check in: " + event[13].x);
            System.out.println("Server 5 check in (FF): " + event[9].x);
            System.out.println("Coda nodo N: " + (nodes[3].number - event[10].x - event[11].x - event[12].x-event[13].x));
            System.out.println("Coda nodo FF: " + (nodes[2].number - event[9].x));
            System.out.println("Server 1 scansione carta d'imbarco: " + event[22].x);
            System.out.println("Server 2 scansione carta d'imbarco: " + event[23].x);
            System.out.println("Server 3 scansione carta d'imbarco: " + event[24].x);
            System.out.println("Server 4 scansione carta d'imbarco: " + event[25].x);
            System.out.println("Server 5 (FF) scansione carta d'imbarco: " + event[20].x);
            System.out.println("Server 5 (FF) scansione carta d'imbarco: " + event[21].x);
            System.out.println("Coda nodo N 1: " + (nodes[5].number - event[22].x ));
            System.out.println("Coda nodo N 1: " + (nodes[6].number - event[23].x ));
            System.out.println("Coda nodo N 1: " + (nodes[7].number - event[24].x ));
            System.out.println("Coda nodo N 1: " + (nodes[8].number - event[25].x ));
            System.out.println("Coda nodo FF: " + (nodes[4].number - event[20].x));
            System.out.println("Coda nodo FF: " + (nodes[9].number - event[21].x));
            System.out.println("Server 1 imbarco: " + event[40].x);
            System.out.println("Server 2 imbarco: " + event[41].x);
            System.out.println("Server 3 imbarco: " + event[42].x);
            System.out.println("Server 4 imbarco: " + event[43].x);
            System.out.println("Server 5 imbarco (FF): " + event[39].x);
            System.out.println("Coda nodo N: " + (nodes[14].number - event[40].x - event[41].x - event[42].x-event[43].x));
            System.out.println("Coda nodo FF: " + (nodes[13].number - event[39].x));
            System.out.println(number);
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
                /* process an arrival*/
                number++;
                System.out.println("\n*** Sto Processando un arrivo ***");
                if (e == 0) {
                    nodes[1].number++;
                } else {
                    nodes[0].number++;
                }

                r.selectStream(10 + idx);
                idx++;
                rnd = r.random();

                if (rnd > 0.2305) {
                    event[0].t = getArrival(r);
                    //System.out.println("Primo arrivo normale: " + event[0].t);
                    if (event[0].t > STOP){
                        event[0].x = 0;
                    }

                } else {
                    event[1].t = getArrival(r);
                    //System.out.println("Primo arrivo ff: " + event[1].t);
                    if (event[1].t > STOP) {
                        event[1].x = 0;
                    }
                }
                if (e == Values.SERVERS_DEDICATO_BIGLIETTERIA && nodes[0].number == 1) {

                    singleService = ssq.getService(r);
                    sum[6].service += singleService;
                    sum[6].served++;
                    event[6].t = t.current + singleService;
                    event[6].x = 1;
                }
               else if (e == 0 && nodes[1].number <= Values.SERVERS_BIGLIETTERIA) {
                    multiService = getService(r);
                    s = findOne(event, 2, SERVERS + 2 - 1);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                }




            }
            else if(e==2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1){ /* server dedicato*/
                event[8].x=0;
                //number++;
                nodes[2].number++;
                //idx++????
                if(nodes[2].number==1){
                    singleService = ssq.getService(r);
                    sum[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN].service += singleService;
                    sum[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN].served++;
                    event[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN].t = t.current + singleService;
                    event[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN].x = 1;
                }

            }
            else if(e==2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA){
                event[7].x=0;
               // number++;
                nodes[3].number++;
                //idx++????
                if(nodes[3].number<=Values.SERVERS_CHECK_IN){
                    multiService = getService(r);
                    s = findOne(event, 2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+1, 2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                }
            }
            else if(e==2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+2){
                event[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+2].x=0;
                //number++;
                nodes[9].number++;
                //idx++????
                if(nodes[9].number==1){
                    singleService = ssq.getService(r);
                    sum[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].service += singleService;
                    sum[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].served++;
                    event[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].t = t.current + singleService;
                    event[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].x = 1;
                }
            }
            else if (e==2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+1){
                event[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+1].x=0;
                //number++;
                nodes[4].number++;
                //idx++????
                if(nodes[4].number==1){
                    singleService = ssq.getService(r);
                    sum[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].service += singleService;
                    sum[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].served++;
                    event[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].t = t.current + singleService;
                    event[2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].x = 1;
                }
            }
            else if(e>=2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+1 && e<=2+Values.SERVERS_DEDICATO_BIGLIETTERIA+Values.SERVERS_BIGLIETTERIA+1+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA){
                event[e].x=0;
                //number++;
                int p=find_number_node(e);
                nodes[p].number++;
                //idx++????
                if(nodes[p].number==1){
                    singleService = ssq.getService(r);

                    sum[e+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].service += singleService;
                    sum[e+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].served++;
                    event[e+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].t = t.current + singleService;
                    event[e+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0].x = 1;
                }
            }
            else if (e==2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA){  /* servers prioritario secuirty*/
                event[2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA].x=0;
                // number++;
                nodes[10].number++;
                //idx++????
                if(nodes[10].number<=2){
                    multiService = getService(r);
                    s = findOne(event, 2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+2, 2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                }
            }
            else if(e==2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1){
                event[2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+1].x=0;
                // number++;
                nodes[11].number++;
                //idx++????
                if(nodes[11].number<=SERVERS){
                    multiService = getService(r);
                    s = findOne(event, 2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY, 2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                }
            }
            else if (e==2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1) {
                event[2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1].x=0;
                System.out.println("\n *** Sto processando un arrivo al controllo approfondito ***");
                nodes[12].number++;
                Block block=new Block(event[2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+1]);

                Queue_security1.enqueue(block.priority,block);  // enqueue the current arrival into the respective queue defined by 'priority'
                if(nodes[12].number<=2){
                    Block pa=Queue_security1.dequeue(0);
                    multiService = getService(r);
                    s = findOne(event, 2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2, 2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO_DEDICATO);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                    event[s].priority=pa.priority;
                }

            }
            else if(e==2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO_DEDICATO+1){
                event[2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO_DEDICATO+1].x=0;
                System.out.println("\n *** Sto processando un arrivo all'imbarco FF***");
                System.out.println("il tipo di passeggero è:"+event[2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO_DEDICATO+1].priority);
                nodes[13].number++;
                if(nodes[13].number<=1){

                    multiService = getService(r);

                    sum[2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO_DEDICATO+2+Values.SERVERS_IMBARCO_DEDICATO].service += multiService;
                    sum[2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO_DEDICATO+2+Values.SERVERS_IMBARCO_DEDICATO].served++;
                    event[2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO_DEDICATO+2+Values.SERVERS_IMBARCO_DEDICATO].t = t.current + multiService;
                    event[2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO_DEDICATO+2+Values.SERVERS_IMBARCO_DEDICATO].x = 1;

                }
            }
            else if(e==2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO_DEDICATO+2){
                event[2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO_DEDICATO+2].x=0;

                System.out.println("\n *** Sto processando un arrivo all'imbarco priority ***");
                System.out.println("il tipo di passeggero è:"+event[38].priority);
                Block block=new Block(event[2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO_DEDICATO+2]);
                if(block.priority==2){
                    Queue_imbarco1.enqueue(1, block);
                    number_queues[1]++;
                }
                else{
                    Queue_imbarco1.enqueue(block.priority, block);
                    number_queues[0]++;
                }

                nodes[14].number++;
                if(nodes[14].number<=SERVERS){
                    if(block.priority==2){
                        Block b=Queue_imbarco1.dequeue(1);
                        number_queues[1]--;
                    }
                    else{
                        Block b=Queue_imbarco1.dequeue(block.priority);
                        number_queues[1]--;
                    }
                    multiService = getService(r);
                    s = findOne(event, 2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO_DEDICATO+2+Values.SERVERS_IMBARCO_DEDICATO+1, 2+Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVER_DEDICATO_CHECK_IN+Values.SERVERS_CHECK_IN+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVER_SCANSIONE_CARTA_DEDICAT0+Values.SERVERS_SCANSIONE_CARTA+Values.SERVERS_SECURITY+Values.SERVERS_DEDICATI_SECURITY+2+Values.SERVERS_IMBARCO_DEDICATO+2+Values.SERVERS_IMBARCO_DEDICATO+Values.SERVERS_IMBARCO_DEDICATO);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;

                }
            }

            else {                                         /* process a departure */
                index++;                                     /* from server s       */
               // number--;
                s = e;
                if (s >= 2 && s <= 2 + Values.SERVERS_BIGLIETTERIA - 1) {   /* nodo multiserver */
                    System.out.println("\n*** Sto processando una departure del server ***");
                    nodes[1].number--;
                    nodes[1].index++;
                    event[SERVERS+SERVERS_DEDICATO+2].x=1;
                    event[ 2 + Values.SERVERS_BIGLIETTERIA +1].t=t.current;

                    if (nodes[1].number >= Values.SERVERS_BIGLIETTERIA) {
                        multiService = getService(r);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                    } else
                        event[s].x = 0;
                }
                else if ( s == 2 + Values.SERVERS_BIGLIETTERIA ) {
                    System.out.println("\n*** Sto processando una departure del server dedicato***");
                    event[ 3 + Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA].x=1;
                    event[3 + Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA].t=t.current;
                    nodes[0].number--;
                    nodes[0].index++;
                    if (nodes[0].number >= 1) {
                        singleService = ssq.getService(r);
                        sum[s].service += singleService;
                        sum[s].served++;
                        event[s].t = t.current + singleService;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if (s== 2 + Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2) {
                    System.out.println("\n*** Sto processando una departure del server check in dedicato***");
                    //number--;
                    nodes[2].number--;
                    nodes[2].index--;
                    int k =find_best_server_dedicato();
                    System.out.println("Ilnumero dell'evento è:"+k);
                    event[k].x=1;
                    event[k].t=t.current;
                    if (nodes[2].number >= 1) {
                        singleService = ssq.getService(r);
                        sum[s].service += singleService;
                        sum[s].served++;
                        event[s].t = t.current + singleService;
                    } else {
                        event[s].x = 0;
                    }

                }
                else if(s>= 2 + Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+1 && s<= 2 + Values.SERVERS_BIGLIETTERIA+Values.SERVERS_DEDICATO_BIGLIETTERIA+2+Values.SERVERS_CHECK_IN){
                   // number--;
                    System.out.println("\n*** Sto processando una departure del server check in***");
                    nodes[3].number--;
                    nodes[3].index--;
                    int server=find_best_server();
                    event[server].x=1;
                    event[server].t=t.current;
                    if (nodes[3].number >= Values.SERVERS_CHECK_IN) {
                        multiService = getService(r);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if(e==20 ){
                   // number--;
                    System.out.println("\n*** Sto processando una departure del server dedicato carta imbarco***");
                    event[26].x=1;
                    event[26].t=t.current;
                    nodes[4].number--;
                    nodes[4].index--;
                    if (nodes[4].number >= 1) {
                        singleService = ssq.getService(r);
                        sum[s].service += singleService;
                        sum[s].served++;
                        event[s].t = t.current + singleService;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if(e==21){
                   // number--;
                    event[26].x=1;
                    event[26].t=t.current;
                    System.out.println("\n*** Sto processando una departure del server dedicato carta imbarco***");
                    nodes[9].number--;
                    nodes[9].index--;
                    if (nodes[9].number >= 1) {
                        singleService = ssq.getService(r);
                        sum[s].service += singleService;
                        sum[s].served++;
                        event[s].t = t.current + singleService;
                    } else {
                        event[s].x = 0;
                    }
                }

                else if(e>=22 && e<=25){
                    event[27].x=1;
                    event[27].t=t.current;
                    System.out.println("\n*** Sto processando una departure del server  carta imbarco***");
                   // number--;
                    int nu=find_number_node(e);
                    nodes[nu].number--;
                    nodes[nu].index++;
                    if (nodes[nu].number >= 1) {
                        singleService = ssq.getService(r);
                        sum[s].service += singleService;
                        sum[s].served++;
                        event[s].t = t.current + singleService;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if (e==28 || e==29){
                   //  number--;
                    System.out.println("\n*** Sto processando una departure del server dedicato security**");
                    nodes[10].number--;
                    nodes[10].index--;
                    r.selectStream(10 + idx);
                    idx++;
                    rnd = r.random();
                    if(rnd>0.50){
                        event[34].x=1;
                        event[34].t=t.current;
                        event[34].priority=1;
                    }
                    else {
                        event[37].x=1;
                        event[37].t=t.current;

                    }
                    if (nodes[10].number >= 2) {
                        multiService = getService(r);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if(e>=30 && e<=33){
                     //number--;
                    System.out.println("\n*** Sto processando una departure del server security***");
                    nodes[11].number--;
                    nodes[11].index--;
                    r.selectStream(10 + idx);
                    idx++;
                    rnd = r.random();
                    if(rnd>0.50){
                        event[34].x=1;
                        event[34].t=t.current;
                        event[34].priority=0;
                    }
                    else {
                        r.selectStream(10 + idx);
                        idx++;
                        rnd = r.random();

                        if(rnd>0.50) {
                            event[38].x = 1;
                            event[38].t = t.current;
                            event[38].priority=2;
                        }
                        else{
                            event[38].x = 1;
                            event[38].t = t.current;
                            event[38].priority=2;
                        }

                    }

                    if (nodes[11].number >= SERVERS) {
                        multiService = getService(r);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if (e==35 || e==36){
                    number--;
                    System.out.println("\n*** Sto processando una departure del server controllo approfondito***");
                    nodes[12].number--;
                    nodes[12].index--;


                    if (nodes[12].number >= 2) {
                        multiService = getService(r);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if (e==39){
                    System.out.println("\n*** Sto processando una departure del server  dedicato imbarco***");
                    number--;

                    nodes[13].number--;
                    nodes[13].index++;
                    if (nodes[13].number >= 1) {
                        singleService = ssq.getService(r);
                        sum[s].service += singleService;
                        sum[s].served++;
                        event[s].t = t.current + singleService;
                    } else {
                        event[s].x = 0;
                    }
                }
                else if(e>=40 && e<=43){
                    System.out.println("\n*** Sto processando una departure del server imbarco***");
                    number--;

                    nodes[14].number--;
                    nodes[14].index++;
                    if (nodes[14].number >= SERVERS) {
                        if(number_queues[1]>0){
                            Block block = new Block(Queue_imbarco1.dequeue(1));
                            singleService = ssq.getService(r);
                            sum[s].service += singleService;
                            sum[s].served++;
                            event[s].t = t.current + singleService;
                            event[s].priority= block.priority;
                            event[s].passenger_type= block.type;
                            number_queues[1] -= 1;
                        }
                        else if(number_queues[0]>0){
                            Block block = new Block(Queue_imbarco1.dequeue(0));
                            singleService = ssq.getService(r);
                            sum[s].service += singleService;
                            sum[s].served++;
                            event[s].t = t.current + singleService;
                            number_queues[0] -= 1;
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


    public static double exponential(double m, Rngs_1 r) {
        /* ---------------------------------------------------
         * generate an Exponential random variate, use m > 0.0
         * ---------------------------------------------------
         */
        return (-m * Math.log(1.0 - r.random()));
    }

    public static double uniform(double a, double b, Rngs_1 r) {
        /* ------------------------------------------------
         * generate an Uniform random variate, use a < b
         * ------------------------------------------------
         */
        return (a + (b - a) * r.random());
    }

    public static double getArrival(Rngs_1 r) {
        /* --------------------------------------------------------------
         * generate the next arrival time, with rate 1/2
         * --------------------------------------------------------------
         */
        r.selectStream(0);
        sarrival += exponential(2.0, r);
        return (ModelloIniziale.sarrival);
    }


    public static double getService(Rngs_1 r) {
        /* ------------------------------
         * generate the next service time, with rate 1/6
         * ------------------------------
         */
        r.selectStream(1);
        return (uniform(2.0, 10.0, r));
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
        while (i < 46) {         /* now, check the others to find which  */
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
        if (e==16 || e==22){
            return 5;
        }
        if(e==17 || e==23){
            return 6;
        }
        if(e==18 || e==24){
            return 7;

        }
        if(e==19 || e==25){
            return 8;
        }
        return 10000;
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
            return 14;
        }
        return 15;
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
            current=16;
        }
        if(current==6){
            current=17;
        }
        if(current==7){
            current=18;
        }
        if(current==8){
            current=19;
        }
        return current;
    }

}

