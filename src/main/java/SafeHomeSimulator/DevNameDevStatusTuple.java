package SafeHomeSimulator;

public class DevNameDevStatusTuple
{
    private DEV_ID devId;

    private DEV_STATE devStatus;

    public DevNameDevStatusTuple() {} // Default Constructor for JSON

    public DevNameDevStatusTuple(DEV_ID devId, DEV_STATE devStatus)
    {
        this.devId = devId;
        this.devStatus = devStatus;
    }

    @Override
    public String toString() {
        return "[" +
                "devName='" + devId + '\'' +
                ", devStatus=" + devStatus +
                ']';
    }

    @Override
    public int hashCode() {
        return this.devId.hashCode() + this.devStatus.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        // If the object is compared with itself then return true
        if (obj == this) {
            return true;
        }

        // Check if obj is an instance of DevNameDevStatusTuple or not
        //"null instanceof [type]" also returns false
        if (!(obj instanceof DevNameDevStatusTuple)) {
            return false;
        }

        // typecast obj to Complex so that we can compare data members
        DevNameDevStatusTuple incomingInstance = (DevNameDevStatusTuple) obj;

        boolean isEqual = (
                (0 == this.devId.compareTo(incomingInstance.devId))
                        &&
                        (this.devStatus == incomingInstance.devStatus)
        );

        return isEqual;
    }

    public DEV_ID getDevId() {
        return this.devId;
    }

    public DEV_STATE getDevStatus() {
        return this.devStatus;
    }
}

/**
 * https://stackoverflow.com/questions/9440380/using-an-instance-of-an-object-as-a-key-in-hashmap-and-then-access-it-with-exac
 * https://www.geeksforgeeks.org/overriding-equals-method-in-java/
 */
