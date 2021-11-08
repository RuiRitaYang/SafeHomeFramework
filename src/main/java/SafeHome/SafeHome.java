/**
 * (Deprecated) SafeHome main runner.
 *
 * @author Shegufta Ahsan
 * @project SafeHomeFramework
 * @date 6/20/2019
 * @time 7:41 AM
 *
 *       Paper: Home, SafeHome: Smart Home Reliability with Visibility and
 *              Atomicity (Eurosys 2021)
 *     Authors: Shegufta Bakht Ahsan*, Rui Yang*, Shadi Abdollahian Noghabi^,
 *              Indranil Gupta*
 * Institution: *University of Illinois at Urbana-Champaign,
 *              ^Microsoft Research
 *
 */

package SafeHome;

import LockTableManager.LockTableSingleton;
import SelfExecutingRoutine.SelfExecutingRoutine;
import Utility.OldCommand;
import Utility.DEV_ID;
import Utility.DEV_STATUS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SafeHome
{
    public SafeHome(Set<DEV_ID> _devIDset)
    {
        LockTableSingleton.getInstance().initDeviceList(_devIDset);
    }

    public void registerRoutine(SelfExecutingRoutine newRoutine)
    {
        LockTableSingleton.getInstance().registerRoutine(newRoutine);
    }

    public static void main(String[] args)
    {
        SafeHome safeHome = new SafeHome(getDevIDSet());

        System.out.println("=============================");
        safeHome.registerRoutine(getRoutine1());
        System.out.println("=============================");
//        System.out.println("=============================");
//        safeHome.registerRoutine(getRoutine2());
//        System.out.println("=============================");

        try
        {
            Thread.sleep(99999999);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }


    }

    public static SelfExecutingRoutine getRoutine1()
    {
        List<OldCommand> cmdChain1 = new ArrayList<>();

        cmdChain1.add(new OldCommand(DEV_ID.FAN, DEV_STATUS.ON, 0));
        cmdChain1.add(new OldCommand(DEV_ID.DUMMY_WAIT, DEV_STATUS.WAIT, 5000));
        cmdChain1.add(new OldCommand(DEV_ID.FAN, DEV_STATUS.ON, 0));
        cmdChain1.add(new OldCommand(DEV_ID.LIGHT, DEV_STATUS.ON, 0));
        cmdChain1.add(new OldCommand(DEV_ID.DUMMY_WAIT, DEV_STATUS.WAIT, 5000));
        cmdChain1.add(new OldCommand(DEV_ID.LIGHT, DEV_STATUS.OFF, 0));

        SelfExecutingRoutine selfExcRtn1 = new SelfExecutingRoutine();
        selfExcRtn1.addCmdChain(cmdChain1);

        return selfExcRtn1;
    }

    public static SelfExecutingRoutine getRoutine2()
    {
        List<OldCommand> cmdChain2 = new ArrayList<>();

        cmdChain2.add(new OldCommand(DEV_ID.FAN, DEV_STATUS.ON, 0));
        cmdChain2.add(new OldCommand(DEV_ID.DUMMY_WAIT, DEV_STATUS.WAIT, 5000));
        cmdChain2.add(new OldCommand(DEV_ID.LIGHT, DEV_STATUS.ON, 0));
        cmdChain2.add(new OldCommand(DEV_ID.FAN, DEV_STATUS.OFF, 0));
        //cmdChain2.add(new Command(DEV_ID.LIGHT, DEV_STATUS.OFF, 0));

        SelfExecutingRoutine selfExcRtn2 = new SelfExecutingRoutine();
        selfExcRtn2.addCmdChain(cmdChain2);

        return selfExcRtn2;
    }


    public static Set<DEV_ID> getDevIDSet()
    {
        Set<DEV_ID> devSet = new HashSet<>();

        devSet.add(DEV_ID.FAN);
        devSet.add(DEV_ID.LIGHT);
        devSet.add(DEV_ID.MICROWAVE);

        return devSet;
    }
}
