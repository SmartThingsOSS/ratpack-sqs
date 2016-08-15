package smartthings.ratpack.sqs.event;

/**
 * Defines all possible events emitted by this SQS consumer integration.
 */
public class SqsConsumerEvent {

    public static final SqsConsumerEvent STARTED_EVENT = new SqsConsumerEvent("Started");
    public static final SqsConsumerEvent STOPPED_EVENT = new SqsConsumerEvent("Stopped");
    public static final SqsConsumerEvent SUSPENDED_EVENT = new SqsConsumerEvent("Suspended");
    public static final SqsConsumerEvent RESUMED_EVENT = new SqsConsumerEvent("Resumed");

    private final String name;

    private SqsConsumerEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass()) {
            return false;
        } else if (o == this || ((SqsConsumerEvent) o).getName().equals(getName())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
