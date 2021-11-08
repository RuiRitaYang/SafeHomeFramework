package SafeHomeSimulator;

import java.util.*;

public class SafetyChecker {

    public void staticCheck() {

    }
    private boolean validateSingleSafetyRules(
            DevNameDevStatusTuple condition, DevNameDevStatusTuple action,
            HashMap<DevNameDevStatusTuple, List<DevNameDevStatusTuple>> conditionVsRequiredActionsMap,
            HashMap<String, HashMap<DEV_STATE, List<DevNameDevStatusTuple>>> actionVsRelatedConditionsMap) {

        /* Starting validating conflict rules. Below is supposed to be part of static checking in high-level design.*/
        // Category 1: Each condition could only ``enforce'' at most one condition of each device. TODO: any exception?
        //             If Multiple, only keep the first one.
        if (conditionVsRequiredActionsMap.containsKey(condition)) {
            for (final DevNameDevStatusTuple existing_action : conditionVsRequiredActionsMap.get(condition)) {
                if (action.equals(existing_action)) {
                    System.out.println("STATIC CHECKING -- DUPILICATED RULES");
                    return false;
                }
                if (action.getDevId().equals(existing_action.getDevId()) &&
                        !action.getDevStatus().equals(existing_action.getDevStatus())) {
                    System.out.println("STATIC CHECKING -- Conflict Safety rules with existing rules with same condition");
                    return false;
                }
            }
        }

        // Category 2: The condition sets of two different states of the same dev should always be the same.
        //             If violated, send out notification. (maybe remove the later one?)

        final DEV_ID act_dev_name = action.getDevId();
        final DEV_STATE act_dev_stat = action.getDevStatus();

        if (!actionVsRelatedConditionsMap.containsKey(act_dev_name)) {
            return true;
        }

        final Map<DEV_STATE, List<DEV_ID>> dev_set = getConditionDevSetMapForOneActionDev(
                actionVsRelatedConditionsMap.get(act_dev_name));
        if (dev_set.containsKey(act_dev_stat)) {
            dev_set.get(act_dev_stat).add(condition.getDevId());
        } else {
            dev_set.put(act_dev_stat, Collections.singletonList(condition.getDevId()));
        }

        final List<DEV_ID> dev_set_max = getLargestDevSetForOneActionDev(dev_set);

//        for (Map.Entry<DEV_STATE, List<String>> entry : dev_set.entrySet()) {
//            System.out.println("-----------------------");
//            System.out.println("Device: " + act_dev_name + " Status: " + entry.getKey().toString() + " con_set: " + dev_set.toString());
//        }

        for (final DEV_STATE dev_stat : dev_set.keySet()) {
            if (!dev_set_max.containsAll(dev_set.get(dev_stat))) {
                System.out.println("STATIC CHECKING -- IF A THEN B  AND IF C THEN NOT B conflict happens for " + act_dev_name + act_dev_stat);
                return false;
            }
        }

        return true;
    }
    
    private List<DEV_ID> getLargestDevSetForOneActionDev(Map<DEV_STATE, List<DEV_ID>> dev_set) {
        List<DEV_ID> res = new ArrayList<>();
        int max_size = 0;
        for (final List<DEV_ID> set: dev_set.values()) {
            if (set.size() > max_size) { res = new ArrayList<>(set);}
        }
        return res;
    }

    private Map<DEV_STATE, List<DEV_ID>> getConditionDevSetMapForOneActionDev(
            Map<DEV_STATE, List<DevNameDevStatusTuple>> deviceStatusListHashMap) {
        final Map<DEV_STATE, List<DEV_ID>> res = new HashMap<>();
        for (final DEV_STATE stat: deviceStatusListHashMap.keySet()) {
            res.put(stat, new ArrayList<>());
            final List<DevNameDevStatusTuple> conditions = deviceStatusListHashMap.get(stat);
            for (final DevNameDevStatusTuple cond : conditions) {
                res.get(stat).add(cond.getDevId());
            }
        }
        return res;
    }

    private HashMap<DevNameDevStatusTuple,List<DevNameDevStatusTuple>> getValidSafetyRules(
            HashMap<DevNameDevStatusTuple, List<DevNameDevStatusTuple>> conditionVsRequiredActionsMap) {

        // TODO: This func is for optimization of group safety rules. (Unfinished)

        /* Starting validating conflict rules. Below is supposed to be part of static checking in high-level design.*/
        // Category 1: Each condition could only ``enforce'' at most one condition of each device.
        //             If Multiple, only keep the first one.
        for (final List<DevNameDevStatusTuple> action_list : conditionVsRequiredActionsMap.values()) {
            final Set<DEV_ID> act_devs = new HashSet<>();
            Iterator<DevNameDevStatusTuple> itr = action_list.iterator();
            while (itr.hasNext()) {
                final DEV_ID dev_name = itr.next().getDevId();
                if (act_devs.contains(dev_name)) { itr.remove(); } else { act_devs.add(dev_name); }
            }
        }
        // Category 2: The condition sets of two different states of the same dev should always be the same.
        //             If violated, send out notification. (maybe remove the later one?)

        // Category 3: TODO: loop detection

        return new HashMap<>(conditionVsRequiredActionsMap);
    }

    private Command devStateToCommand(DevNameDevStatusTuple req) {
        // TODO: add more mechanism for command priority.
        return new Command(req.getDevId(), // devID
                req.getDevStatus(),        // targetStatus
                1,                // duration TODO: may change to random
                true,              // is must
                1.0           // mustPercent TODO: what's this?
        );
    }

    /***
     *
     * @param rt: the routine waiting for static checking
     * @param safety_rules: existing safety rules
     * @return A routine that satisfied all the safety rules. (The missing actions will be silently added.)
     */
    private Routine staticSafetyCheckPerRoutine(Routine rt,
                                                HashMap<DevNameDevStatusTuple, List<DevNameDevStatusTuple>> safety_rules) {

        // TODO: Needs modification for intentionally duplicated or repetitive cmd in long-running routines

        // LinkedHashSet does not work well for Command. Thu,s here use List and existing_targets together to track.
        Boolean isSafe = true;
        List<Command> cmd_list = rt.commandList;
        List<Command> res_list = new ArrayList<>();
        Set<DevNameDevStatusTuple> existing_targets = new HashSet<>();

        for (final Command cmd: cmd_list) {
            // Get the pre-requests (all actions)
            DevNameDevStatusTuple cmd_dev_stat = new DevNameDevStatusTuple(cmd.devID, cmd.targetStatus);
            List<DevNameDevStatusTuple> pre_requets = getPreReqPerDevState(cmd_dev_stat, safety_rules);
            for (final DevNameDevStatusTuple req: pre_requets) {
                if (!existing_targets.contains(req)) {
                    // There is pre-request not guaranteed inside routine.
                    isSafe = false;
                    res_list.add(devStateToCommand(req));
                    existing_targets.add(req);
                }
            }
            if (!existing_targets.contains(cmd_dev_stat)) {
                res_list.add(cmd);
                existing_targets.add(cmd_dev_stat);
            }
        }

        // TODO (@ruiyang): check whether the logic is correct (may need id etc)
        Routine res_routine = new Routine(rt.abbr);
        for (Command cmd: res_list) {
            res_routine.addCommand(cmd);
        }

        if (!isSafe) {
            System.out.println("**************************************");
            System.out.println("STATIC CHECKING ---- Routine Modified with new Routine: \n" +res_routine);
        }

        return res_routine;
    }

    private List<DevNameDevStatusTuple> getPreReqPerDevState(
            DevNameDevStatusTuple target_dev_stat,
            HashMap<DevNameDevStatusTuple, List<DevNameDevStatusTuple>> safety_rules) {
        List<DevNameDevStatusTuple> res = new ArrayList<>();
        for (final DevNameDevStatusTuple condition: safety_rules.keySet()) {
            if (condition.equals(target_dev_stat)) {
                return safety_rules.get(target_dev_stat);
            }
        }
        return res;
    }
}
