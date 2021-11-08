/**
 * Command for SafeHome.
 *
 * Command includes the structure of a command and the corresponding operations.
 *
 * @author Shegufta Ahsan
 * @project SafeHomeFramework
 * @date 18-Jul-19
 * @time 12:03 AM
 *
 *       Paper: Home, SafeHome: Smart Home Reliability with Visibility and
 *              Atomicity (Eurosys 2021)
 *     Authors: Shegufta Bakht Ahsan*, Rui Yang*, Shadi Abdollahian Noghabi^,
 *              Indranil Gupta*
 * Institution: *University of Illinois at Urbana-Champaign,
 *              ^Microsoft Research
 *
 */

package SafeHomeSimulator;


public class Command
{
    public DEV_ID devID;
    public int duration;
    public boolean isMust;
    public int startTime;
    public double mustPercent;
    public DEV_STATE targetStatus;

    public Command(DEV_ID _devID, int _duration, boolean _isMust, double _mustPercent)
    {
        this.devID = _devID;
        this.duration = _duration;
        this.isMust = _isMust;
        this.mustPercent = _mustPercent;
        this.targetStatus = DEV_STATE.CLOSED;
    }

    public Command(DEV_ID _devID, DEV_STATE _dev_state, int _duration, boolean _isMust, double _mustPercent)
    {
        this.devID = _devID;
        this.duration = _duration;
        this.isMust = _isMust;
        this.mustPercent = _mustPercent;
        this.targetStatus = _dev_state;
    }


    public int getCmdEndTime()
    {
        return this.startTime + this.duration;
    }

    public boolean isCmdOverlapsWithWatchTime(int queryTime)
    {
        boolean insideBound = false;

        if(this.startTime <= queryTime && queryTime < getCmdEndTime())
        { // NOTE: the start time is inclusive, whereas the end time is exclusive. e.g.   [3,7)
            insideBound = true;
        }

        return insideBound;
    }

    public int compareTimeline(int queryTime)
    {
        if(this.getCmdEndTime() <= queryTime)
            return -1; //  Cmd ends before query

        if(this.startTime <= queryTime && queryTime < getCmdEndTime())
            return 0; // cmd overlaps

        return 1; // cmd starts after query
    }

    public Command getDeepCopy()
    {
        Command deepCopyCommand = new Command(this.devID, this.duration, this.isMust, this.mustPercent);
        deepCopyCommand.startTime = this.startTime;
        return deepCopyCommand;
    }

    /*
    public void overrideDurationForWeakVisibilityModel(int weakVisibilityDeviceAccessTime)
    {
        this.duration = weakVisibilityDeviceAccessTime;
    }
    */

    @Override
    public String toString()
    {
        String str = "";

        str += "[ " + this.devID.name() + ":" + startTime + ", " + (startTime + duration) + "]";

        return str;
    }
}
