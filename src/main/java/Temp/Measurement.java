package Temp;

import java.util.*;

/**
 * @author Shegufta Ahsan
 * @project SafeHomeFramework
 * @date 04-Sep-19
 * @time 10:37 PM
 */
public class Measurement
{
    Map<Float, Integer> parallelismHistogram = new HashMap<>();
    Map<Float, Integer> orderingMismatchPrcntHistogram = new HashMap<>();
    Map<Float, Integer> devUtilizationPrcntHistogram = new HashMap<>();

    //public List<Float> devUtilizationPercentList = new ArrayList<>();
    //public float orderMismatchPercent = 0.0f;

    Map<Float, Integer> isvltn1_perRtnCollisionCountHistogram = new HashMap<>();
    Map<Float, Integer> isvltn3_CMDviolationPercentHistogram = new HashMap<>(); // Command Violation Per Routine
    Map<Float, Integer> isvltn2_RTNviolationPercentHistogram = new HashMap<>();
    Map<Float, Integer> isvltn4_cmdToCommitCollisionTimespanPrcntHistogram = new HashMap<>();
    //ISVLTN4_CMD_TO_COMMIT_COLLISION_TIMESPAN_PRCNT
    /////////////////////////////////////////////

    private void measureDeviceUtilization(final LockTable _lockTable)
    {
        if(_lockTable.consistencyType == CONSISTENCY_TYPE.WEAK)
        {
            this.devUtilizationPrcntHistogram.put(100.0f, 1);
            return;
        }
        else
        {
            final Map<DEV_ID, List<Routine>> lockTable = _lockTable.lockTable;

            for(Map.Entry<DEV_ID, List<Routine>> entry : lockTable.entrySet())
            {
                DEV_ID devID = entry.getKey();
                List<Routine> lineage = entry.getValue();
                int entryCount = lineage.size();

                if(entryCount == 0)
                    continue;

                float earliestAccessRequestTime = Float.MAX_VALUE;
                for(Routine rtn : lineage)
                {
                    if(rtn.registrationTime < earliestAccessRequestTime)
                        earliestAccessRequestTime = rtn.registrationTime;
                }

                Command lastCmd = lineage.get(entryCount - 1).getCommandByDevID(devID);
                final double totalTimeSpan = lastCmd.getCmdEndTime() - earliestAccessRequestTime;

                double cmdExecutionSpan = 0.0f;

                for(Routine rtn : lineage)
                {
                    Command cmd = rtn.getCommandByDevID(devID);
                    cmdExecutionSpan += cmd.getCmdEndTime() - cmd.startTime;
                }


                double utilization = ( cmdExecutionSpan / totalTimeSpan) * 100.0;
                //devUtilizationPercentList.add( utilization );

                Float data = (float)utilization;
                Integer count = this.devUtilizationPrcntHistogram.get(data);

                if(count == null)
                    this.devUtilizationPrcntHistogram.put(data, 1);
                else
                    this.devUtilizationPrcntHistogram.put(data, count + 1);
            }
        }
    }

    private void measureParallelization(final LockTable _lockTable)
    {
        Integer minStartTimeInclusive = Integer.MAX_VALUE;
        Integer maxEndTimeExclusive = Integer.MIN_VALUE;

        for(Routine rtn : _lockTable.getAllRoutineSet())
        {
            if(rtn.routineStartTime() < minStartTimeInclusive)
                minStartTimeInclusive = rtn.routineStartTime();

            if(maxEndTimeExclusive < rtn.routineEndTime())
                maxEndTimeExclusive = rtn.routineEndTime();
        }

        assert(minStartTimeInclusive < maxEndTimeExclusive);

        int totalTimeSpan = maxEndTimeExclusive - minStartTimeInclusive; // start time is inclusive, end time is exclusive. e.g.  J : [<R1|C1>:1:2) [<R0|C3>:3:4) [<R2|C0>:4:5)



        //this.parallelRtnCntList = new ArrayList<>(Collections.nCopies(totalTimeSpan, 0.0f));

        int[] histogram = new int[totalTimeSpan];

        for(Routine rtn : _lockTable.getAllRoutineSet())
        {
            int startIdx = rtn.routineStartTime() - minStartTimeInclusive;
            int endIdx = rtn.routineEndTime() - minStartTimeInclusive;

            for(int I = startIdx ; I < endIdx ; I++)
            {
                histogram[I]++;
                //this.parallelRtnCntList.add(I, (this.parallelRtnCntList.get(I) + 1));
            }
        }

        for(float frequency : histogram)
        {
            Integer count = parallelismHistogram.get(frequency);
            // here the count is the data. we have to count how many time these "count" appear

            if(count == null)
                parallelismHistogram.put(frequency, 1);
            else
                parallelismHistogram.put(frequency, count + 1);
        }
    }

    private void measureOrderingMismatch(final LockTable _lockTable, ORDER_MISTACH_MEASURE_TYPE type) {
        if (type == ORDER_MISTACH_MEASURE_TYPE.L1_DISTANCE) {
            measureOrderingMismatch(_lockTable);
        } else if (type == ORDER_MISTACH_MEASURE_TYPE.L2_DISTANCE) {
            measureOrderingMismathL2(_lockTable);
        } else if (type == ORDER_MISTACH_MEASURE_TYPE.BUBBLE_SWAP) {
            measureOrderingMismatchBubble(_lockTable);
        }
    }

    private void measureOrderingMismatchBubble(final LockTable _lockTable) {
        if(_lockTable.consistencyType == CONSISTENCY_TYPE.WEAK) {
            this.orderingMismatchPrcntHistogram.put(-1.0f, -1);
        } else {
            for (List<Routine> lineage : _lockTable.lockTable.values()) {
                double orderMismatchPercent;
                if (lineage.size() == 1) {
                    orderMismatchPercent = 0.0;
                } else {
                    List<Integer> rtnList = new ArrayList<>();
                    for (Routine rtn : lineage) {
                        rtnList.add(rtn.ID);
                    }
                    System.out.println(Arrays.toString(rtnList.toArray()));
                    int num_swap = sortRtnList(rtnList);
                    int max_swap = rtnList.size() * (rtnList.size() - 1) / 2;

                    orderMismatchPercent = (num_swap / max_swap) * 100.0;
                    System.out.printf("num_swap : %d, pecentage: %f\n", num_swap, orderMismatchPercent);
                }

                Float data = (float) orderMismatchPercent;
                if (data > 0) {
                    System.out.printf("-----\nBubble: %f", data);
                }
                this.orderingMismatchPrcntHistogram.merge(data, 1, (a, b) -> a + b);
            }
        }
    }

    private int sortRtnList(List<Integer> rtnList) {
        int i, j, temp;
        int swaps = 0;
        for(i = 0; i < rtnList.size() - 1; ++i){
            for(j=0; j< rtnList.size() - 1 - i; ++j){

                if(rtnList.get(j) > rtnList.get(j+1)){

                    temp = rtnList.get(j+1);
                    rtnList.set(j+1, rtnList.get(j));
                    rtnList.set(j, temp);
                    swaps++;
                }
            }
        }
        return swaps;
    }

    private void measureOrderingMismathL2(final LockTable _lockTable) {
        if(_lockTable.consistencyType == CONSISTENCY_TYPE.WEAK) {
            this.orderingMismatchPrcntHistogram.put(-1.0f, -1);
        } else {
            for (List<Routine> lineage : _lockTable.lockTable.values()) {
                double orderMismatchPercent;
                if (lineage.size() == 1) {
                    orderMismatchPercent = 0.0;
                } else {
                    Map<Integer, Integer> rtnIDVsCurrentIndexMap = new HashMap<>();
                    List<Integer> sortedRtnList = new ArrayList<>();
                    int index = 0;
                    for (Routine rtn : lineage) {
                        rtnIDVsCurrentIndexMap.put(rtn.ID, index++);
                        sortedRtnList.add(rtn.ID);
                    }
                    Collections.sort(sortedRtnList);

                    double maxPossibleMismatch = 0;
                    double orderMismatch = 0;
                    final int maxIndex = sortedRtnList.size() - 1;
                    for (int I = 0; I < sortedRtnList.size(); I++) {
                        int rtnID = sortedRtnList.get(I);
                        int indexInLineage = rtnIDVsCurrentIndexMap.get(rtnID);

                        orderMismatch += Math.pow((I - indexInLineage), 2);
                        maxPossibleMismatch += Math.pow((2 * I - maxIndex), 2);
                    }
                    orderMismatchPercent = (Math.sqrt(orderMismatch) / Math.sqrt(maxPossibleMismatch)) * 100.0;
                }

                Float data = (float) orderMismatchPercent;
                if (data > 0) {
                    System.out.printf("  L1: %f -----\n", data);
                }
                this.orderingMismatchPrcntHistogram.merge(data, 1, (a, b) -> a + b);
            }
        }
    }

    private void measureOrderingMismatch(final LockTable _lockTable)
    {
        if(_lockTable.consistencyType == CONSISTENCY_TYPE.WEAK)
        {
            this.orderingMismatchPrcntHistogram.put(-1.0f, -1);
        }
        else
        {
            for(List<Routine> lineage : _lockTable.lockTable.values())
            {
                double orderMismatchPercent = 0.0;

                if(lineage.size() == 1)
                {
                    orderMismatchPercent = 0.0;
                }
                else
                {
                    Map<Integer, Integer> rtnIDVsCurrentIndexMap = new HashMap<>();
                    List<Integer> sortedRtnList = new ArrayList<>();

                    int index = 0;
                    for(Routine rtn : lineage)
                    {
                        rtnIDVsCurrentIndexMap.put(rtn.ID, index++);
                        sortedRtnList.add(rtn.ID);
                        System.out.println(Arrays.toString(sortedRtnList.toArray()));
                    }
                    Collections.sort(sortedRtnList);

                    double maxPossibleMismatch = 0;
                    double orderMismatch = 0;
                    final int maxIndex = sortedRtnList.size() - 1;

                    for(int I = 0 ; I < sortedRtnList.size() ; I++)
                    {
                        int rtnID = sortedRtnList.get(I);
                        int indexInLineage = rtnIDVsCurrentIndexMap.get(rtnID);

                        orderMismatch += Math.abs(I - indexInLineage);
                        maxPossibleMismatch += Math.abs(2*I - maxIndex);
                    }
                    orderMismatchPercent = (orderMismatch / maxPossibleMismatch) * 100.0;
                }

                Float data = (float)orderMismatchPercent;
                Integer count = this.orderingMismatchPrcntHistogram.get(data);

                if(count == null)
                    this.orderingMismatchPrcntHistogram.put(data, 1);
                else
                    this.orderingMismatchPrcntHistogram.put(data, count + 1);
            }
        }

        /*
        if(_lockTable.consistencyType == CONSISTENCY_TYPE.WEAK)
        {
            orderMismatchPercent = -1.0f;
            return;
        }
        final Map<DEV_ID, List<Routine>> lockTable = _lockTable.lockTable;
        Map<Integer, Integer> routineOrderingViolation = new HashMap<>();
        float totalCount = 0.0f;
        float violationCount = 0.0f;

        for(List<Routine> rtnList : lockTable.values())
        {
            for(int index =0 ; index < (rtnList.size() - 1) ; index++)
            {
                totalCount++;

                if( rtnList.get(index + 1).ID < rtnList.get(index).ID)
                    violationCount++;
            }
        }

        orderMismatchPercent = (totalCount == 0.0f)? 0.0f : (violationCount/totalCount)*100.0f;
        */
    }

    private void isolationViolation(final LockTable _lockTable)
    {
        List<Routine> allRtnList = _lockTable.getAllRoutineSet();

        Map<Routine, Set<Routine>> victimRtnAndAttackerRtnSetMap = new HashMap<>();
        Map<Routine, Set<Command>> victimRtnAndItsVictimCmdSetMap = new HashMap<>();

        for(Routine rtn1 : allRtnList)
        {
            assert(!victimRtnAndAttackerRtnSetMap.containsKey(rtn1));
            victimRtnAndAttackerRtnSetMap.put(rtn1, new HashSet<>());

            assert(!victimRtnAndItsVictimCmdSetMap.containsKey(rtn1));
            victimRtnAndItsVictimCmdSetMap.put(rtn1, new HashSet<>());


            for(Command cmd1 : rtn1.commandList)
            {
                DEV_ID devID = cmd1.devID;
                int spanStartTimeInclusive = cmd1.startTime;
                int spanEndTimeExclusive = rtn1.routineEndTime();

                float earliestCollisionTime = Float.MAX_VALUE;

                for (Routine rtn2 : allRtnList)
                {
                    if (rtn1 == rtn2)
                        continue;

                    boolean isAttackedByRtn2 = rtn2.isDevAccessStartsDuringTimeSpan(devID, spanStartTimeInclusive, spanEndTimeExclusive);

                    if (isAttackedByRtn2)
                    {
                        victimRtnAndAttackerRtnSetMap.get(rtn1).add(rtn2);
                        victimRtnAndItsVictimCmdSetMap.get(rtn1).add(cmd1);

                        float collisionTime = rtn2.getCommandByDevID(devID).startTime;

                        if(collisionTime < earliestCollisionTime)
                            earliestCollisionTime = collisionTime;
                    }
                }

                float timeSpentInCollisionRatio = 0.0f;

                if(earliestCollisionTime < Float.MAX_VALUE)
                {
                    assert(spanStartTimeInclusive <= earliestCollisionTime);

                    float expectedConsistencySpanCmd1 = spanEndTimeExclusive - spanStartTimeInclusive;
                    float collisionTime = spanEndTimeExclusive - earliestCollisionTime;

                    timeSpentInCollisionRatio = (collisionTime / expectedConsistencySpanCmd1) * 100.0f;
                }

                Float data = timeSpentInCollisionRatio;
                Integer count = this.isvltn4_cmdToCommitCollisionTimespanPrcntHistogram.get(data);

                if(count == null)
                    this.isvltn4_cmdToCommitCollisionTimespanPrcntHistogram.put(data, 1);
                else
                    this.isvltn4_cmdToCommitCollisionTimespanPrcntHistogram.put(data, count + 1);
            }

        }


        float victimRoutineCount = 0.0f;

        for(Routine rtn1 : allRtnList)
        {
            float victimCommandCount = victimRtnAndItsVictimCmdSetMap.get(rtn1).size();

            float isvltn_perRtnVictimCmdPrcnt = 0.0f;
            float isvltn_totalUniqueAttackerPerRoutine = 0.0f;

            if(0.0 < victimCommandCount)
            {
                victimRoutineCount++;
                float totalCommand = rtn1.commandList.size();

                isvltn_perRtnVictimCmdPrcnt = (victimCommandCount/totalCommand)*100.0f;
                isvltn_totalUniqueAttackerPerRoutine = (float)victimRtnAndAttackerRtnSetMap.get(rtn1).size();
            }

            //////////////////////////////////////////////////////////////////
            Float data;
            Integer count;
            //////////////////////////////////////////
            //isvltn_perRtnVictimCmdPrcntList.add(perRtnSpoiledCmdPercent);
            data = isvltn_perRtnVictimCmdPrcnt;
            count = this.isvltn3_CMDviolationPercentHistogram.get(data);

            if(count == null)
                this.isvltn3_CMDviolationPercentHistogram.put(data, 1);
            else
                this.isvltn3_CMDviolationPercentHistogram.put(data, count + 1);
            //////////////////////////////////////////////////////////////////

            data = isvltn_totalUniqueAttackerPerRoutine;
            count = this.isvltn1_perRtnCollisionCountHistogram.get(data);

            if(count == null)
                this.isvltn1_perRtnCollisionCountHistogram.put(data, 1);
            else
                this.isvltn1_perRtnCollisionCountHistogram.put(data, count + 1);
            //////////////////////////////////////////////////////////////////
        }
        /////////////////////////////////////////////////////////////////
        float totalRoutine = allRtnList.size();
        //isvltn_victimRtnPercentPerRun = (victimRoutineCount / totalRoutine)*100.0f;
        Float data = (victimRoutineCount / totalRoutine)*100.0f;
        Integer count = this.isvltn2_RTNviolationPercentHistogram.get(data);

        if(count == null)
            this.isvltn2_RTNviolationPercentHistogram.put(data, 1);
        else
            this.isvltn2_RTNviolationPercentHistogram.put(data, count + 1);
        /////////////////////////////////////////////////////////////////
    }

    public Measurement(final LockTable lockTable)
    {
        measureParallelization(lockTable);
//        measureOrderingMismatch(lockTable, ORDER_MISTACH_MEASURE_TYPE.BUBBLE_SWAP);
        measureOrderingMismatch(lockTable, ORDER_MISTACH_MEASURE_TYPE.L1_DISTANCE);
        measureDeviceUtilization(lockTable);
        isolationViolation(lockTable);


        assert(!isvltn3_CMDviolationPercentHistogram.isEmpty());
        assert(!isvltn1_perRtnCollisionCountHistogram.isEmpty());
        assert(!isvltn2_RTNviolationPercentHistogram.isEmpty());
        assert(!isvltn4_cmdToCommitCollisionTimespanPrcntHistogram.isEmpty());
        assert(!orderingMismatchPrcntHistogram.isEmpty());
        assert(!parallelismHistogram.isEmpty());
        assert(!devUtilizationPrcntHistogram.isEmpty());
    }
}
