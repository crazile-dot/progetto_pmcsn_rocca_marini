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

    static double START = 0.0;              /* initial time                   */
    static double STOP  = 10.0;          /* terminal (close the door) time */
    static double INFINITY = 1000.0 * STOP;  /* must be much larger than STOP  */

    static int SERVERS = 3;              /* number of servers */
    static int NODES = 2;

    static double sarrival = START;

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


        MsqEvent [] event = new MsqEvent [2 + SERVERS + 1];  /* tipologie di evento */
        MsqSum [] sum = new MsqSum [2 + SERVERS + 1];
        Node [] nodes = new Node [NODES];
        Area[] areas = new Area[NODES];
        for (s = 0; s < 2 + SERVERS + 1; s++) {
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

        for (n = 2; n < 2 + SERVERS + 1; n++) {
            event[n].t = START;
            event[n].x = 0;
            sum[n].service = 0.0;
            sum[n].served = 0;
        }

        int iteration = 0;
        while ((event[0].x != 0) || event[1].x != 0 || (number != 0)) {
            System.out.println("Lista eventi:\n");
            System.out.println("Stato evento arrivo N: " + event[0].x);
            System.out.println("Stato evento arrivo FF: " + event[1].x);
            System.out.println("Server 1: " + event[2].x);
            System.out.println("Server 2: " + event[3].x);
            System.out.println("Server 3: " + event[4].x);
            System.out.println("Server 4 (FF): " + event[5].x);
            System.out.println("Number nodo N: " + nodes[1].number);
            System.out.println("Number nodo FF: " + nodes[0].number);
            System.out.println("Coda nodo N: " + (nodes[1].number - event[2].x - event[3].x - event[4].x));
            System.out.println("Coda nodo FF: " + (nodes[0].number - 1));

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
                if (e == 0 && nodes[1].number <= SERVERS) {
                    multiService = getService(r);
                    s = findOne(event, 2, SERVERS + 2 - 1);
                    sum[s].service += multiService;
                    sum[s].served++;
                    event[s].t = t.current + multiService;
                    event[s].x = 1;
                }

                if (e == 1 && nodes[0].number == 1) {

                    singleService = ssq.getService(r);
                    sum[5].service += singleService;
                    sum[5].served++;
                    event[5].t = t.current + singleService;
                    event[5].x = 1;
                }

            }
            else {                                         /* process a departure */
                index++;                                     /* from server s       */
                number--;
                s = e;
                if (s >= 2 && s <= 2 + SERVERS - 1) {   /* nodo multiserver */
                    nodes[1].number--;
                    nodes[1].index++;
                    if (nodes[1].number >= SERVERS) {
                        multiService = getService(r);
                        sum[s].service += multiService;
                        sum[s].served++;
                        event[s].t = t.current + multiService;
                    } else
                        event[s].x = 0;
                } else {
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
        while (i < SERVERS + 2) {         /* now, check the others to find which  */
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

}

