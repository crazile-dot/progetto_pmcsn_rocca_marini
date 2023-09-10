// each block represents an element (job) in the queue
public class Block {
    double arrival_time= 0.0;

    int type=0; // frequent flyer= 1 ; normale=0
    int priority=0;
    double departure=0.0;
    int number=0;
    int job=0;
    double arrive=0.0;
    int compagnia=0;
    Block(Block evento) {
        this.arrival_time = evento.arrival_time;
        this.type = evento.type;
        if (this.type == 0) {
            this.priority = 0;
        }
        if (this.type == 1) {
            this.priority = 1;
        }
        if(this.type==2){
            this.priority=2;
        }
    }
    Block(MsqEvent evento){
        this.arrival_time=evento.t;
        this.type= evento.passenger_type;
        if (this.type==0){
            this.priority=0;
        }
        if (this.type==1){
            this.priority=1;
        }
        if(this.type==2){
            this.priority=2;
        }
    }
}
