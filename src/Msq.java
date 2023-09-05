import com.sun.jdi.Value;

import java.lang.*;
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
    static double STOP    = 20000.0;        /* terminal (close the door) time  20000.0; */
    static int    SERVERS = 4;              /* number of servers              */

    static double LAMBDA;
    static double SERVICE = 2;
    static double fasciaOraria = MMValues.fasciaOraria3;

    static int SERVER_DEDICATO=1; /* SERVER DEDICATO per i frequent flyers */
    static double sarrival = START;
    static int[] number_queues={0,0};
    static int[] number_queues_checkin={0,0};
    static int[] number_queues_security={0,0};
    static int[] number_queues_imbarco={0,0,0};
    static int number_queues_security_app=0;
    static long[] number_nodes={0,0,0,0,0,0,0,0,0,0,0};
    static long   number = 0;
    public static void main(String[] args) {

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

        t.current    = START;
        event[0].t   = getArrival(r);
        event[0].x   = 1;
        for (s = 1; s <= 38; s++) {
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
            System.out.println("Lista eventi:");
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
            System.out.println("server check in 2:"+event[27].x);
            System.out.println("server check in 3:"+event[28].x);
            System.out.println("server check in 4:"+event[29].x);
            System.out.println("server check dedicato:"+event[30].x);
            System.out.println("arrivi security:"+event[32].x);
            System.out.println("server imbarco:"+event[33].x);
            System.out.println("server imbarco 2:"+event[34].x);
            System.out.println("server imbarco 3:"+event[35].x);
            System.out.println("server imbarco 4:"+event[36].x);
            System.out.println("server imbarco dedicato:"+event[37].x);
            System.out.println("server imbarco coda ff:"+number_queues_imbarco[1]);
            System.out.println("server coda N2:"+number_queues_imbarco[2]);
            System.out.println("server coda N:"+number_queues_imbarco[0]);



            e         = m.nextEvent(event);                /* next ev1ent index */

            t.next    = event[e].t;                         /* next event time  */

            area     += (t.next - t.current) * number;     /* update integral  */
            areaBiglietteria+=(t.next - t.current) * number_nodes[0];
            if (number_nodes[0]>SERVERS+SERVER_DEDICATO){
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

            if (e == 0) {                                  /* process an arrival*/
                System.out.println("\n\n****Sto processando un arrivo*****");
                r.selectStream(0 );
                double rnd = r.random();                        // Prendo un numero casuale da Rngs
                System.out.println("\nil valore di rnd è:"+rnd);
                if( rnd <0.50){
                    event[0].passenger_type=0;
                    event[0].priority=0;
                }
                else {
                    event[0].passenger_type=1;
                    event[0].priority=1;
                }
                System.out.println("il tipo di passeggero è:"+event[0].passenger_type);
                Block block=new Block(event[0]);

                Queues.enqueue(block.priority,block);  // enqueue the current arrival into the respective queue defined by 'priority'
                number_queues[event[0].priority] += 1;// incremento temporaneamente il numero dei job nella coda di appartenenza
                number++; // incremento numero dei job nel sistema
                number_nodes[0]++;
                event[0].t        = getArrival(r);
                if (event[0].t > STOP)
                    event[0].x      = 0;
                if (number_nodes[0] <= SERVERS+SERVER_DEDICATO) {
                    if (block.priority==1 && event[5].x==0){
                        areaBiglietteriaDedicata     += (t.next - t.current) * number;
                        Block passenger_served = Queues.dequeue(block.priority);
                        service         = getService(r);
                        sum[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO].service += service;
                        sum[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO].served++;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO].t      = t.current + service;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO].x      = 1;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO].priority= block.priority;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO].passenger_type= block.type;
                        number_queues[event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO].priority] -= 1;

                    }
                    else {
                        int l=0;
                        for(int z=1;z<=MMValues.SERVER_BIGLIETTERIA;z++){
                            if(event[z].x==0){
                                l=1;
                            }
                        }
                        if( l==1 ){
                            System.out.println("\n***sono dentro else***");
                            Block passenger_served = Queues.dequeue(block.priority);
                            service = getService(r);
                            s = m.findOne(event);
                            sum[s].service += service;
                            sum[s].served++;
                            event[s].t = t.current + service;
                            event[s].x = 1;
                            event[s].priority = block.priority;
                            event[s].passenger_type = block.type;
                            number_queues[event[s].priority] -= 1;
                        }
                    }
                }

            }
            else if(e==MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1){ /* process an arrival at check-in*/
                event[MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1].x=0; //6
                System.out.println("\n *** Sto processando un arrivo al check in ***");
                System.out.println("il tipo di passeggero è:"+event[6].passenger_type);
                Block block=new Block(event[6]);

                Queues_checkin.enqueue(block.priority,block);  // enqueue the current arrival into the respective queue defined by 'priority'
                number_queues_checkin[event[6].priority] += 1;// incremento temporaneamente il numero dei job nella coda di appartenenza
                //number++; // incremento numero dei job nel sistema
                number_nodes[1]++;
                if (number_nodes[1] <= MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO) {
                    if (block.priority==1 && event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].x==0){
                        Block passenger_served = Queues_checkin.dequeue(block.priority);
                        service         = getService(r);
                        sum[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].service += service;
                        sum[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].served++;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].t      = t.current + service;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].x      = 1;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].priority= block.priority;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].passenger_type= block.type;
                        number_queues_checkin[event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].priority] -= 1;

                    }
                    else {
                        int l=0;
                        for(int z=7;z<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN;z++){
                            if(event[z].x==0){
                                l=1;
                            }
                        }
                        if( l==1 ){
                            System.out.println("\n***sono dentro else***");
                            Block passenger_served = Queues_checkin.dequeue(block.priority);
                            service = getService(r);
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
                }


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


                        service = getService(r);
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


                        service = getService(r);
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
                System.out.println("il tipo di passeggero è:"+event[25].passenger_type);
                Block block=new Block(event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2]);

                Queues_security.enqueue(block.priority,block);  // enqueue the current arrival into the respective queue defined by 'priority'
                number_queues_security[event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2].priority] += 1;// incremento temporaneamente il numero dei job nella coda di appartenenza
                //number++; // incremento numero dei job nel sistema
                ;
                number_nodes[8]++;
                if (number_nodes[8] <= MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO) {
                    int l=0;
                    for(int z=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+1;z<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO;z++){
                        if(event[z].x==0){
                            l=1;
                        }
                    }
                    if (block.priority==1 && l==1){
                        Block passenger_served = Queues_security.dequeue(block.priority);
                        service         = getService(r);
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
                            service = m.getService(r);
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
                }
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
                if (number_nodes[9] <= MMValues.SERVER_IMBARCO_DEDICATO+MMValues.SERVER_IMBARCO) {
                    int poi=0;
                    if (block.priority==1 && event[37].x==0){
                        System.out.println("AOOOOOOOOOOOOOOOO");
                        Block passenger_served = Queues_imbarco.dequeue(block.priority);
                        service         = m.getService(r);
                        //37
                        sum[ MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO].service += service;
                        sum[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO].served++;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO].t      = t.current + service;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO].x      = 1;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO].priority= block.priority;
                        event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO].passenger_type= block.type;
                        number_queues_imbarco[event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO].priority] -= 1;
                        poi=1;
                    }
                    int  l=0;
                    for(int z=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+3;
                        z<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO;z++){
                        if(event[z].x==0){
                            l=1;
                        }
                    }
                    if (block.priority==2 && (l==1)){
                        System.out.println("\nsono nell'if");
                        Block passenger_served = Queues_imbarco.dequeue(block.priority);
                        service = m.getService(r);
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
                        service = m.getService(r);
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

                }
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
                    service = m.getService(r);
                    s=findOne(event, MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO+1, MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+1+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+MMValues.SERVER_CARTA_IMBARCO+MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+ MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO+MMValues.SERVER_CONT_APP);
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

                if(s==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO){
                    number_nodes[0]--;
                    System.out.println("\n***Sto processando la departure del server dedicato***");
                    System.out.println(number_queues[1]);
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].x=1;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].t=t.current;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].priority=1;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1].passenger_type=1;

                    if(number_queues[1]>0 ) {

                        Block block = new Block(Queues.dequeue(1));
                        service = m.getService(r);
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
                else if(s==1+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN){ //11
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
                        service = m.getService(r);
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
                        service = m.getService(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues[1] -= 1;
                    }
                    else if (number_queues[0]>0){
                        Block block = new Block(Queues.dequeue(0));
                        service = m.getService(r);
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

                        int k=fine_best_node();
                        System.out.println(k);
                        event[k].x=1;
                        event[k].t=t.current;
                        event[k].priority=0;
                        event[k].passenger_type=0;

                    }

                    if(number_queues_checkin[1]>0 ) {

                        Block block = new Block(Queues_checkin.dequeue(1));
                        service = m.getService(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_checkin[1] -= 1;
                    }
                    else if (number_queues_checkin[0]>0){
                        Block block = new Block(Queues_checkin.dequeue(0));
                        service = m.getService(r);
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
                        service = m.getService(r);
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
                    System.out.println("\n**** Sto processando una departure dal server dedicato carta imbarco****");
                    System.out.println("\n"+number);
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3].x=1;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3].t=t.current;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3].priority=1;
                    event[MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3].passenger_type=1;
                    int k =findNode(s);
                    number_nodes[k]--;
                    if(number_nodes[k]>0 ) {

                        Block block = new Block(event[s]);
                        service = m.getService(r);
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
                        service = m.getService(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_security[1] -= 1;
                    }
                    else if (number_queues_security[0]>0){
                        Block block = new Block(Queues_security.dequeue(0));
                        service = m.getService(r);
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
                else if(s==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO){
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
                        service = m.getService(r);
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
                        service = m.getService(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_imbarco[1] -= 1;
                    }
                    else if (number_queues_imbarco[2]>0){
                        Block block = new Block(Queues_imbarco.dequeue(2));
                        service = m.getService(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= block.type;
                        number_queues_imbarco[2] -= 1;
                    }
                    else if (number_queues_imbarco[0]>0){
                        Block block = new Block(Queues_imbarco.dequeue(0));
                        service = m.getService(r);
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
                else if(s>=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+1 && s<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+MMValues.SERVER_IMBARCO_DEDICATO){
                    number--;
                    number_nodes[9]--;
                    if(number_queues_imbarco[1]>MMValues.SERVER_IMBARCO_DEDICATO ) {

                        Block block = new Block(Queues_imbarco.dequeue(1));
                        service = m.getService(r);
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
                else if(s==MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_CARTA_IMBARCO*2+MMValues.SERVER_CARTA_IMBARCO_DEDICATO*2+ 3+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO+1+MMValues.SERVER_IMBARCO_DEDICATO){
//38
                    number_nodes[10]--;
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
                        service = m.getService(r);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].priority= block.priority;
                        event[s].passenger_type= 0;
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

        for (s = 1; s <= SERVERS+SERVER_DEDICATO; s++)          /* adjust area to calculate */
            area -= sum[s].service;              /* averages for the queue   */

        System.out.println("  avg delay .......... =   " + f.format(area / index));
        System.out.println("  avg # in queue ..... =   " + f.format(area / t.current));
        System.out.println("\nthe server statistics are:\n");
        System.out.println("    server     utilization     avg service      share");
        for (s = 1; s <= SERVERS+SERVER_DEDICATO; s++) {
            System.out.print("       " + s + "          " + g.format(sum[s].service / t.current) + "            ");
            System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)index));
        }
        for (s = 7; s < 7+SERVERS+SERVER_DEDICATO; s++) {
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
        }
        System.out.println("");

    }

    private static int findOne_controllo_dedicato() {
        int i=1+MMValues.SERVER_CHECK_DEDICATO+MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CARTA_IMBARCO+1;
        int current=16;

        if (number_nodes[findNode(current)]>number_nodes[findNode(i+1)]){
            current=i+1;
        }


        return current;
    }

    private static int fine_best_node() {
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
        if(e==12 || e==19){
            return 2;
        }
        if (e==13 || e==20){
            return 3;
        }
        if (e==14 || e==21) return 4;
        if(e==22 || e==15) return 5;
        if(e==16 || e==23){
            return 6;
        }
        if(e==17 || e==24){
            return 7;
        }
        if(e==25){
            return 8;
        }
        return 100000;
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
        sarrival += exponential(LAMBDA, r);
        return (sarrival);
    }


    public static void getServiceBigl(Rngs r) {
        SERVICE = MMValues.biglService;
        getService(r);
    }

    public static void getServiceChk(Rngs r) {
        SERVICE = MMValues.chckinService;
        getService(r);
    }

    public static void getServiceScann(Rngs r) {
        SERVICE = MMValues.scannService;
        getService(r);
    }

    public static void getServiceSec(Rngs r) {
        SERVICE = MMValues.securService;
        getService(r);
    }

    public static void getServiceSec2(Rngs r) {
        SERVICE = MMValues.secur2Service;
        getService(r);
    }

    public static void getServiceGate(Rngs r) {
        SERVICE = MMValues.gateService;
        getService(r);
    }

    public static double getService(Rngs r) {
        /* ------------------------------
         * generate the next service time, with rate 1/6
         * ------------------------------
         */
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
        while (i <40) {         /* now, check the others to find which  */
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
        int i = 7;

        while (event[i].x == 1 && i<=MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < 10) {         /* now, check the others to find which   */
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
        while (i < MMValues.SERVER_BIGLIETTERIA+MMValues.SERVER_BIGLIETTERIA_DEDICATO+1+MMValues.SERVER_CHECK_IN+MMValues.SERVER_CHECK_DEDICATO+2*MMValues.SERVER_CARTA_IMBARCO+2*MMValues.SERVER_CARTA_IMBARCO_DEDICATO+2+MMValues.SERVER_SECURITY+MMValues.SERVER_SECURITY_DEDICATO+2+MMValues.SERVER_IMBARCO-1) {         /* now, check the others to find which   */
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

        if (e==12){
            return 19;
        }
        if (e==13) return 20;
        if (e==14) return 21;
        if (e== 15) return 22;
        if (e==16)return 23;
        if (e==17)return 24;
        return 1000000;

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
