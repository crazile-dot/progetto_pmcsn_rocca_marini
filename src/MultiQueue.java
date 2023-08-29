import java.util.LinkedList;
import java.util.Queue;

public class MultiQueue {
    public Queue<Block>[] queues;

    @SuppressWarnings("unchecked")
    public MultiQueue(int numQueues) {
        queues = new Queue[numQueues];
        for (int i = 0; i < numQueues; i++) {
            queues[i] = new LinkedList<>();
        }
    }

    public void enqueue(int queueIndex, Block element) {
        if (queueIndex >= 0 && queueIndex < queues.length) {
            queues[queueIndex].offer(element);
        } else {
            throw new IllegalArgumentException("Invalid queue index");
        }
    }

    public Block dequeue(int queueIndex) {
        if (queueIndex >= 0 && queueIndex < queues.length) {
            Block element = queues[queueIndex].poll();
            if (element != null) {
                return element;
            } else {
                throw new IllegalStateException("Queue is empty");
            }
        } else {
            throw new IllegalArgumentException("Invalid queue index");
        }
    }

    public boolean isEmpty(int queueIndex) {
        if (queueIndex >= 0 && queueIndex < queues.length) {
            return queues[queueIndex].isEmpty();
        } else {
            throw new IllegalArgumentException("Invalid queue index");
        }
    }
}