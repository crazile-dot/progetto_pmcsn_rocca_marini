import java.text.DecimalFormat;


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
        double service;

        Msq m = new Msq();
        Rngs r = new Rngs();
        r.plantSeeds(123456789);

        for(int mo=0;mo<2;mo++){
            number_queues[mo]=0;
        }
        MsqEvent [] event = new MsqEvent [39];
        MsqSum [] sum = new MsqSum [39];
        for (s = 0; s < 39; s++) {
            event[s] = new MsqEvent();
            sum [s]  = new MsqSum();
        }

        MsqT t = new MsqT();

        t.current    = START;
        event[0].t   = m.getArrival(r);
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

            e         = m.nextEvent(event);                /* next event index */

            t.next    = event[e].t;                         /* next event time  */

            area     += (t.next - t.current) * number;     /* update integral  */
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
                event[0].t        = m.getArrival(r);
                if (event[0].t > STOP)
                    event[0].x      = 0;
                if (number_nodes[0] <= SERVERS+SERVER_DEDICATO) {
                    if (block.priority==1 && event[5].x==0){
                        Block passenger_served = Queues.dequeue(block.priority);
                        service         = m.getService(r);
                        sum[5].service += service;
                        sum[5].served++;
                        event[5].t      = t.current + service;
                        event[5].x      = 1;
                        event[5].priority= block.priority;
                        event[5].passenger_type= block.type;
                        number_queues[event[5].priority] -= 1;

                    }
                    else {
                        if( event[1].x==0 || event[2].x==0  || event[3].x==0 ||event[4].x==0 ){
                            System.out.println("\n***sono dentro else***");
                            Block passenger_served = Queues.dequeue(block.priority);
                            service = m.getService(r);
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
            else if(e==6){ /* process an arrival at check-in*/
                event[6].x=0;
                System.out.println("\n *** Sto processando un arrivo al check in ***");
                System.out.println("il tipo di passeggero è:"+event[6].passenger_type);
                Block block=new Block(event[6]);

                Queues_checkin.enqueue(block.priority,block);  // enqueue the current arrival into the respective queue defined by 'priority'
                number_queues_checkin[event[6].priority] += 1;// incremento temporaneamente il numero dei job nella coda di appartenenza
                //number++; // incremento numero dei job nel sistema
                number_nodes[1]++;
                if (number_nodes[1] <= SERVERS+SERVER_DEDICATO) {
                    if (block.priority==1 && event[11].x==0){
                        Block passenger_served = Queues_checkin.dequeue(block.priority);
                        service         = m.getService(r);
                        sum[11].service += service;
                        sum[11].served++;
                        event[11].t      = t.current + service;
                        event[11].x      = 1;
                        event[11].priority= block.priority;
                        event[11].passenger_type= block.type;
                        number_queues_checkin[event[11].priority] -= 1;

                    }
                    else {
                        if( event[7].x==0 || event[8].x==0  || event[9].x==0 ||event[10].x==0 ){
                            System.out.println("\n***sono dentro else***");
                            Block passenger_served = Queues_checkin.dequeue(block.priority);
                            service = m.getService(r);
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
            else if(e>=12 && e<16){
                event[e].x=0;
                System.out.println("\n *** Sto processando un arrivo al controllo ***");
                System.out.println("il tipo di passeggero è:"+event[e].passenger_type);
                Block block=new Block(event[e]);


               // number++; // incremento numero dei job nel sistema
                int k = findNode(e);
                number_nodes[k]++;
                if (number_nodes[k] <= 1) {

                    if( event[e].x==0  ){


                           service = m.getService(r);
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
            else if (e==16 || e==17){
                event[e].x=0;
                System.out.println("\n *** Sto processando un arrivo al controllo ***");
                System.out.println("il tipo di passeggero è:"+event[e].passenger_type);
                Block block=new Block(event[e]);


                //number++; // incremento numero dei job nel sistema
                int k = findNode(e);
                number_nodes[k]++;
                if (number_nodes[k] <= 1) {

                    if( event[e].x==0  ){


                        service = m.getService(r);
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
            else if (e==25) {
                event[25].x=0;
                System.out.println("\n *** Sto processando un arrivo alla security ***");
                System.out.println("il tipo di passeggero è:"+event[25].passenger_type);
                Block block=new Block(event[25]);

                Queues_security.enqueue(block.priority,block);  // enqueue the current arrival into the respective queue defined by 'priority'
                number_queues_security[event[25].priority] += 1;// incremento temporaneamente il numero dei job nella coda di appartenenza
                //number++; // incremento numero dei job nel sistema
                ;
                number_nodes[8]++;
                if (number_nodes[8] <= SERVERS+SERVER_DEDICATO) {
                    if (block.priority==1 && event[30].x==0){
                        Block passenger_served = Queues_security.dequeue(block.priority);
                        service         = m.getService(r);
                        sum[30].service += service;
                        sum[30].served++;
                        event[30].t      = t.current + service;
                        event[30].x      = 1;
                        event[30].priority= block.priority;
                        event[30].passenger_type= block.type;
                        number_queues_security[event[30].priority] -= 1;

                    }
                    else {
                        if( event[26].x==0 || event[27].x==0  || event[28].x==0 ||event[29].x==0 ){

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
            else if ( e== 32){
                event[32].x=0;
                System.out.println("\n *** Sto processando un arrivo all'imbarco***");
                System.out.println("il tipo di passeggero è:"+event[32].passenger_type+"\nla prio è:"+event[32].priority);
                Block block=new Block(event[e]);
                System.out.println(block.priority);
                Queues_imbarco.enqueue(block.priority,block);  // enqueue the current arrival into the respective queue defined by 'priority'

                number_queues_imbarco[event[32].priority] += 1;// incremento temporaneamente il numero dei job nella coda di appartenenza
                //number++; // incremento numero dei job nel sistema
                ;
                number_nodes[9]++;
                if (number_nodes[9] <= SERVERS+SERVER_DEDICATO) {
                    if (block.priority==1 && event[37].x==0){
                        Block passenger_served = Queues_imbarco.dequeue(block.priority);
                        service         = m.getService(r);
                        sum[37].service += service;
                        sum[37].served++;
                        event[37].t      = t.current + service;
                        event[37].x      = 1;
                        event[37].priority= block.priority;
                        event[37].passenger_type= block.type;
                        number_queues_imbarco[event[37].priority] -= 1;

                    }
                    else if (block.priority==2 && (event[33].x==0 || event[34].x==0  || event[35].x==0 ||event[36].x==0 )){
                        System.out.println("\nono nell'if");
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
                    else {
                        if( event[33].x==0 || event[34].x==0  || event[35].x==0 ||event[36].x==0){

                            Block passenger_served = Queues_imbarco.dequeue(block.priority);
                            service = m.getService(r);
                            s = m.findOne_imbarco(event);
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
            }
            else if (e==31){
                event[31].x=0;
                System.out.println("\n *** Sto processando un arrivo security app***");
                //System.out.println("il tipo di passeggero è:"+event[31].passenger_type+"\nla prio è:"+event[31].priority);
                Block block=new Block(event[e]);
                Queues_security_app.enqueue(0,block); // enqueue the current arrival into the respective queue defined by 'priority'

                number_queues_security_app += 1;// incremento temporaneamente il numero dei job nella coda di appartenenza


                number_nodes[10]++;
                if (number_nodes[10] <= 1){
                    Block passenger_served = Queues_security_app.dequeue(0);
                    service = m.getService(r);
                    sum[38].service += service;
                    sum[38].served++;
                    event[38].t = t.current + service;
                    event[38].x = 1;
                    event[38].priority = block.priority;
                    event[38].passenger_type = block.type;
                    number_queues_security_app-= 1;
                }
            }
            else {                                         /* process a departure */

                index++;                                     /* from server s       */
               // number--;
                s = e;

                if(s==5){
                    number_nodes[0]--;
                    System.out.println("\n***Sto processando la departure del server dedicato***");
                    System.out.println(number_queues[1]);
                    event[6].x=1;
                    event[6].t=t.current;
                    event[6].priority=1;
                    event[6].passenger_type=1;

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
                else if(s==2+SERVERS+SERVERS+SERVER_DEDICATO){
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
                else if (s>0 && s<SERVERS+1) {
                    number_nodes[0]--;
                    System.out.println("\n***Sto processando la departure del server***");
                    System.out.println(number_queues[0]);
                    if(event[s].priority==1){


                        event[6].x=1;
                        event[6].t=t.current;
                        event[6].priority=1;
                        event[6].passenger_type=1;

                    } else if (event[s].priority==0) {


                        event[6].x=1;
                        event[6].t=t.current;
                        event[6].priority=0;
                        event[6].passenger_type=0;

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
                else if (s>6 && s<2+SERVERS+SERVERS+SERVER_DEDICATO) {
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
                else if (s>=19&& s<=22 ) {
                   // number--;
                    System.out.println("\n**** Sto processando una departure dal server carta imbarco****");
                    System.out.println("\n"+number);
                    event[25].x=1;
                    event[25].t=t.current;
                    event[25].priority=0;
                    event[25].passenger_type=0;
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
                else if (s==23 || s==24) {
                   // number--;
                    System.out.println("\n**** Sto processando una departure dal server dedicato carta imbarco****");
                    System.out.println("\n"+number);
                    event[25].x=1;
                    event[25].t=t.current;
                    event[25].priority=1;
                    event[25].passenger_type=1;
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
                else if(s>=26 && s<=29){
                   // number--;
                    number_nodes[8]--;
                    r.selectStream(0 );
                    double rnd = r.random();
                    if(rnd<0.99){
                        r.selectStream(10 );
                        double rnd1 = r.random();
                        if (event[s].priority==1){
                            event[32].x = 1;
                            event[32].t = t.current;
                            event[32].priority = event[s].priority;
                            event[32].passenger_type = event[s].passenger_type;
                        }
                        else if(rnd1<0.50) {
                            event[32].x = 1;
                            event[32].t = t.current;
                            event[32].priority = event[s].priority;
                            event[32].passenger_type = event[s].passenger_type;
                        }
                        else{
                            event[32].x = 1;
                            event[32].t = t.current;
                            event[32].priority = 2;
                            event[32].passenger_type = 2;
                        }
                    }
                    else {
                        event[31].x=1;
                        event[31].t=t.current;
                        event[31].priority=event[s].priority;
                        event[31].passenger_type=event[s].passenger_type;
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
                else if(s==30){
                    //number--;
                    number_nodes[8]--;
                    r.selectStream(0 );
                    double rnd = r.random();
                    if(rnd<0.50){
                        event[32].x=1;
                        event[32].t=t.current;
                        event[32].priority=event[s].priority;
                        event[32].passenger_type=event[s].passenger_type;
                    }
                    else {
                        event[31].x=1;
                        event[31].t=t.current;
                        event[31].priority=event[s].priority;
                        event[31].passenger_type=event[s].passenger_type;
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
                else if(s>=33 && s<=36){
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
                else if(s==37){
                    number--;
                    number_nodes[9]--;
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
                    else {
                        event[s].x=0;
                    }
                }
                else if(s==38){
                    number--;
                    number_nodes[10]--;
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

        System.out.println("");

    }

    private static int findOne_controllo_dedicato() {
        int i=16;
        int current=16;

        if (number_nodes[findNode(current)]>number_nodes[findNode(i+1)]){
            current=i+1;
        }


        return current;
    }

    private static int fine_best_node() {
        int i=12;
        int current=12;
        while (i<15){
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


    double exponential(double m, Rngs r) {
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

    double getArrival(Rngs r) {
        /* --------------------------------------------------------------
         * generate the next arrival time, with rate 1/2
         * --------------------------------------------------------------
         */
        r.selectStream(0);
        sarrival += exponential(2.0, r);
        return (sarrival);
    }


    double getService(Rngs r) {
        /* ------------------------------
         * generate the next service time, with rate 1/6
         * ------------------------------
         */
        r.selectStream(1);
        return (uniform(2.0, 10.0, r));
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
        while (i <38) {         /* now, check the others to find which  */
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

        while (event[i].x == 1 && i<=4)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < SERVERS) {         /* now, check the others to find which   */
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

        while (event[i].x == 7 && i<=10)       /* find the index of the first available */
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
        int i = 26;

        while (event[i].x == 26 && i<=29)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < 29) {         /* now, check the others to find which   */
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
        int i = 33;

        while (event[i].x == 33 && i<=36)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < 35) {         /* now, check the others to find which   */
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

}
