package SafeHomeSimulator;

public class ActionConditionTuple
{
    private DEV_ID actionDevId;
    public DEV_STATE actionDevStatus;
    private DEV_ID conditionDevId;
    public DEV_STATE conditionDevStatus;

    public ActionConditionTuple() { } // need this default constructor for JSON parser

    public ActionConditionTuple(DEV_ID actionDevId, DEV_STATE actionDevStatus, DEV_ID conditionDevId, DEV_STATE conditionDevStatus)
    {
        this.actionDevId = actionDevId;
        this.actionDevStatus = actionDevStatus;
        this.conditionDevId = conditionDevId;
        this.conditionDevStatus = conditionDevStatus;
    }

    public DevNameDevStatusTuple getAction()
    {
        return new DevNameDevStatusTuple(this.actionDevId, this.actionDevStatus);
    }

    public DevNameDevStatusTuple getCondition()
    {
        return new DevNameDevStatusTuple(this.conditionDevId, conditionDevStatus);
    }

    @Override
    public String toString()
    {
        return "ActionConditionTuple{" +
                "actionDevId='" + actionDevId + '\'' +
                ", actionDevStatus=" + actionDevStatus +
                ", conditionDevId='" + conditionDevId + '\'' +
                ", conditionDevStatus=" + conditionDevStatus +
                '}';
    }
}
