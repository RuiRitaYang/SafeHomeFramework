/**
 * SafeHome Simulator.
 *
 * Main class to simualate different SafeHome models under no failures.
 *
 * @author Shegufta Ahsan
 * @project SafeHomeFramework
 * @date 17-Jul-19
 * @time 10:32 AM
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


import BenchmarkingTool.*;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.genetics.RandomKeyMutation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;


// graph plotting tool command:  "python .\gen_all.py -d C:\Users\shegufta\Desktop\smartHomeData\1568337471715_VARY_maxConcurrentRtn_R_101_C_6"

public class SafeHomeSimulator
{
    public static final boolean IS_RUNNING_BENCHMARK = SysParamSngltn.getInstance().IS_RUNNING_BENCHMARK; //false; // Careful... if it is TRUE, all other parameters will be in don't care mode!
    private static final int totalSampleCount = SysParamSngltn.getInstance().totalSampleCount; //1000;//7500;//10000; // 100000;

    public static final boolean isVaryShrinkFactor = SysParamSngltn.getInstance().isVaryShrinkFactor;
    public static final boolean isVaryCommandCntPerRtn = SysParamSngltn.getInstance().isVaryCommandCntPerRtn;
    public static final boolean isVaryZipfAlpha = SysParamSngltn.getInstance().isVaryZipfAlpha;
    public static final boolean isVaryLongRunningPercent = SysParamSngltn.getInstance().isVaryLongRunningPercent;
    public static final boolean isVaryLongRunningDuration = SysParamSngltn.getInstance().isVaryLongRunningDuration;
    public static final boolean isVaryShortRunningDuration = SysParamSngltn.getInstance().isVaryShortRunningDuration;
    public static final boolean isVaryMustCmdPercentage = SysParamSngltn.getInstance().isVaryMustCmdPercentage;
    public static final boolean isVaryDevFailureRatio = SysParamSngltn.getInstance().isVaryDevFailureRatio;

    public static final String commaSeprtdVarListString = SysParamSngltn.getInstance().commaSeprtdVarListString;
    public static List<Double> variableList = SysParamSngltn.getInstance().variableList;

    public static final String commaSeprtdCorrespondingUpperBoundListString =  SysParamSngltn.getInstance().commaSeprtdCorrespondingUpperBoundListString;
    public static final List<Double> variableCorrespndinMaxValList = SysParamSngltn.getInstance().variableCorrespndinMaxValList;

    private static double shrinkFactor = SysParamSngltn.getInstance().shrinkFactor; // 0.25; // shrink the total time... this parameter controls the concurrency
    private static double minCmdCntPerRtn = SysParamSngltn.getInstance().minCmdCntPerRtn; //  1;
    private static double maxCmdCntPerRtn = SysParamSngltn.getInstance().maxCmdCntPerRtn; //  3;

    private static double zipF = SysParamSngltn.getInstance().zipF; //  0.01;
    public static int devRegisteredOutOf65Dev = SysParamSngltn.getInstance().devRegisteredOutOf65Dev;
    private static int numDevState = SysParamSngltn.getInstance().numDevState; // 2
    private static int numSafetyRule = SysParamSngltn.getInstance().numSafetyRule; // 10
    private static int maxConcurrentRtn = SysParamSngltn.getInstance().maxConcurrentRtn; //  100; //in current version totalConcurrentRtn = maxConcurrentRtn;

    private static double longRrtnPcntg = SysParamSngltn.getInstance().longRrtnPcntg; //  0.1;
    private static final boolean isAtleastOneLongRunning = SysParamSngltn.getInstance().isAtleastOneLongRunning; //  false;
    private static double minLngRnCmdTimSpn = SysParamSngltn.getInstance().minLngRnCmdTimSpn; //  2000;
    private static double maxLngRnCmdTimSpn = SysParamSngltn.getInstance().maxLngRnCmdTimSpn; //  minLngRnCmdTimSpn * 2;

    private static double minShrtCmdTimeSpn = SysParamSngltn.getInstance().minShrtCmdTimeSpn; //  10;
    private static double maxShrtCmdTimeSpn = SysParamSngltn.getInstance().maxShrtCmdTimeSpn; //  minShrtCmdTimeSpn * 6;

    private static double devFailureRatio = SysParamSngltn.getInstance().devFailureRatio; //  0.0;
    private static final boolean atleastOneDevFail = SysParamSngltn.getInstance().atleastOneDevFail; //  false;
    private static double mustCmdPercentage = SysParamSngltn.getInstance().mustCmdPercentage; //  1.0;
    private static int failureAnalyzerSampleCount = SysParamSngltn.getInstance().failureAnalyzerSampleCount;

    public static final boolean IS_PRE_LEASE_ALLOWED = SysParamSngltn.getInstance().IS_PRE_LEASE_ALLOWED; // true;
    public static final boolean IS_POST_LEASE_ALLOWED = SysParamSngltn.getInstance().IS_POST_LEASE_ALLOWED; // true;

    private static final int SIMULATION_START_TIME = SysParamSngltn.getInstance().SIMULATION_START_TIME; //  0;
    public static final int MAX_DATAPOINT_COLLECTON_SIZE = SysParamSngltn.getInstance().MAX_DATAPOINT_COLLECTON_SIZE; //  5000;
    private static final int RANDOM_SEED = SysParamSngltn.getInstance().RANDOM_SEED; //  -1;
    private static final Random rand = RANDOM_SEED > 0 ? new Random(RANDOM_SEED) : new Random();
    private static final int MINIMUM_CONCURRENCY_LEVEL_FOR_BENCHMARKING = SysParamSngltn.getInstance().MINIMUM_CONCURRENCY_LEVEL_FOR_BENCHMARKING; //  5;

    private static final String dataStorageDirectory = SysParamSngltn.getInstance().dataStorageDirectory; //  "C:\\Users\\shegufta\\Desktop\\smartHomeData";

    private static final boolean isMeasureEVroutineInsertionTime = SysParamSngltn.getInstance().isMeasureEVroutineInsertionTime;
    private static final boolean isSchedulingPoliciesComparison = SysParamSngltn.isSchedulingPoliciesComparison;
    private static boolean isAnalyzingTLunderEV = SysParamSngltn.isAnalyzingTLunderEV;
    private static boolean isGenerateSeparateOutputDir = SysParamSngltn.isGenerateSeparateOutputDir;

    private static List<DEV_ID> devIDlist = new ArrayList<>();
    private static Map<DEV_ID, ZipfProbBoundary> devID_ProbBoundaryMap = new HashMap<>();

    //<<<<<<< Updated upstream
//=======
//    private static final int SIMULATION_START_TIME = 0;
//    public static final int MAX_DATAPOINT_COLLECTON_SIZE = 5000;
//    private static final int RANDOM_SEED = -1;
//    private static final int MINIMUM_CONCURRENCY_LEVEL_FOR_BENCHMARKING = 5;
//
////    private static final String dataStorageDirectory = "C:\\Users\\shegufta\\Desktop\\smartHomeData";
//
//>>>>>>> Stashed changes
    ///////////////////////////////////////////////////////////////////////////////////
    private static List<CONSISTENCY_TYPE> CONSISTENCY_ORDERING_LIST = new ArrayList<>();
    ///////////////////////////////////////////////////////////////////////////////////


    private static void initiateSyntheticDevices() {
        int count = devRegisteredOutOf65Dev;
        int totalAvailable = DEV_ID.values().length;

        count = Math.min(count, totalAvailable);

        for (DEV_ID devID : DEV_ID.values()) {
            count--;
            devIDlist.add(devID);

            if (count <= 0)
                break;
        }
    }


    private static String preparePrintableParameters() {
        String logStr = "";

        System.out.println("###################################");
        logStr += "###################################\n";

        System.out.println("IS_RUNNING_BENCHMARK = " + IS_RUNNING_BENCHMARK);
        logStr += "IS_RUNNING_BENCHMARK = " + IS_RUNNING_BENCHMARK + "\n";
        System.out.println("totalSampleCount = " + totalSampleCount + "\n");
        logStr += "totalSampleCount = " + totalSampleCount + "\n\n";


        System.out.println("isVaryShrinkFactor = " + isVaryShrinkFactor);
        logStr += "isVaryShrinkFactor = " + isVaryShrinkFactor + "\n";
        System.out.println("isVaryCommandCntPerRtn = " + isVaryCommandCntPerRtn);
        logStr += "isVaryCommandCntPerRtn = " + isVaryCommandCntPerRtn + "\n";
        System.out.println("isVaryZipfAlpha = " + isVaryZipfAlpha);
        logStr += "isVaryZipfAlpha = " + isVaryZipfAlpha + "\n";
        System.out.println("isVaryLongRunningPercent = " + isVaryLongRunningPercent);
        logStr += "isVaryLongRunningPercent = " + isVaryLongRunningPercent + "\n";
        System.out.println("isVaryLongRunningDuration = " + isVaryLongRunningDuration);
        logStr += "isVaryLongRunningDuration = " + isVaryLongRunningDuration + "\n";
        System.out.println("isVaryShortRunningDuration = " + isVaryShortRunningDuration);
        logStr += "isVaryShortRunningDuration = " + isVaryShortRunningDuration + "\n";
        System.out.println("isVaryMustCmdPercentage = " + isVaryMustCmdPercentage);
        logStr += "isVaryMustCmdPercentage = " + isVaryMustCmdPercentage + "\n";
        System.out.println("isVaryDevFailureRatio = " + isVaryDevFailureRatio + "\n");
        logStr += "isVaryDevFailureRatio = " + isVaryDevFailureRatio + "\n\n";


        System.out.println("commaSeprtdVarListString = " + commaSeprtdVarListString);
        logStr += "commaSeprtdVarListString = " + commaSeprtdVarListString + "\n";
        System.out.println("commaSeprtdCorrespondingUpperBoundListString = " + commaSeprtdCorrespondingUpperBoundListString + "\n");
        logStr += "commaSeprtdCorrespondingUpperBoundListString = " + commaSeprtdCorrespondingUpperBoundListString + "\n\n";


        System.out.println("shrinkFactor = " + shrinkFactor);
        logStr += "shrinkFactor = " + shrinkFactor + "\n";
        System.out.println("minCmdCntPerRtn = " + minCmdCntPerRtn);
        logStr += "minCmdCntPerRtn = " + minCmdCntPerRtn + "\n";
        System.out.println("maxCmdCntPerRtn = " + maxCmdCntPerRtn + "\n");
        logStr += "maxCmdCntPerRtn = " + maxCmdCntPerRtn + "\n\n";


        System.out.println("zipF = " + zipF);
        logStr += "zipF = " + zipF + "\n";
        System.out.println("devRegisteredOutOf65Dev = " + devRegisteredOutOf65Dev);
        logStr += "devRegisteredOutOf65Dev = " + devRegisteredOutOf65Dev + "\n";
        System.out.println("maxConcurrentRtn = " + maxConcurrentRtn + "\n");
        logStr += "maxConcurrentRtn = " + maxConcurrentRtn + "\n\n";


        System.out.println("longRrtnPcntg = " + longRrtnPcntg);
        logStr += "longRrtnPcntg = " + longRrtnPcntg + "\n";
        System.out.println("isAtleastOneLongRunning = " + isAtleastOneLongRunning);
        logStr += "isAtleastOneLongRunning = " + isAtleastOneLongRunning + "\n";
        System.out.println("minLngRnCmdTimSpn = " + minLngRnCmdTimSpn);
        logStr += "minLngRnCmdTimSpn = " + minLngRnCmdTimSpn + "\n";
        System.out.println("maxLngRnCmdTimSpn = " + maxLngRnCmdTimSpn + "\n");
        logStr += "maxLngRnCmdTimSpn = " + maxLngRnCmdTimSpn + "\n\n";

        System.out.println("minShrtCmdTimeSpn = " + minShrtCmdTimeSpn);
        logStr += "minShrtCmdTimeSpn = " + minShrtCmdTimeSpn + "\n";
        System.out.println("maxShrtCmdTimeSpn = " + maxShrtCmdTimeSpn + "\n");
        logStr += "maxShrtCmdTimeSpn = " + maxShrtCmdTimeSpn + "\n\n";


        System.out.println("devFailureRatio = " + devFailureRatio);
        logStr += "devFailureRatio = " + devFailureRatio + "\n";
        System.out.println("atleastOneDevFail = " + atleastOneDevFail);
        logStr += "atleastOneDevFail = " + atleastOneDevFail + "\n";
        System.out.println("mustCmdPercentage = " + mustCmdPercentage);
        logStr += "mustCmdPercentage = " + mustCmdPercentage + "\n";
        System.out.println("failureAnalyzerSampleCount = " + failureAnalyzerSampleCount + "\n");
        logStr += "failureAnalyzerSampleCount = " + failureAnalyzerSampleCount + "\n\n";


        System.out.println("IS_PRE_LEASE_ALLOWED = " + IS_PRE_LEASE_ALLOWED);
        logStr += "IS_PRE_LEASE_ALLOWED = " + IS_PRE_LEASE_ALLOWED + "\n";
        System.out.println("IS_POST_LEASE_ALLOWED = " + IS_POST_LEASE_ALLOWED + "\n");
        logStr += "IS_POST_LEASE_ALLOWED = " + IS_POST_LEASE_ALLOWED + "\n\n";

        System.out.println("SIMULATION_START_TIME = " + SIMULATION_START_TIME);
        logStr += "SIMULATION_START_TIME = " + SIMULATION_START_TIME + "\n";
        System.out.println("MAX_DATAPOINT_COLLECTON_SIZE = " + MAX_DATAPOINT_COLLECTON_SIZE);
        logStr += "MAX_DATAPOINT_COLLECTON_SIZE = " + MAX_DATAPOINT_COLLECTON_SIZE + "\n";
        System.out.println("RANDOM_SEED = " + RANDOM_SEED);
        logStr += "RANDOM_SEED = " + RANDOM_SEED + "\n";
        System.out.println("MINIMUM_CONCURRENCY_LEVEL_FOR_BENCHMARKING = " + MINIMUM_CONCURRENCY_LEVEL_FOR_BENCHMARKING + "\n");
        logStr += "MINIMUM_CONCURRENCY_LEVEL_FOR_BENCHMARKING = " + MINIMUM_CONCURRENCY_LEVEL_FOR_BENCHMARKING + "\n\n";

        System.out.println("dataStorageDirectory = " + dataStorageDirectory + "\n");
        logStr += "dataStorageDirectory = " + dataStorageDirectory + "\n\n";

        System.out.println("isMeasureEVroutineInsertionTime = " + isMeasureEVroutineInsertionTime + "\n");
        logStr += "isMeasureEVroutineInsertionTime = " + isMeasureEVroutineInsertionTime + "\n\n";

        System.out.println("isSchedulingPoliciesComparison = " + isSchedulingPoliciesComparison + "\n");
        logStr += "isSchedulingPoliciesComparison = " + isSchedulingPoliciesComparison + "\n\n";

        System.out.println("isAnalyzingTLunderEV = " + isAnalyzingTLunderEV + "\n");
        logStr += "isAnalyzingTLunderEV = " + isAnalyzingTLunderEV + "\n\n";

        System.out.println("isGenerateSeparateOutputDir = " + isGenerateSeparateOutputDir + "\n");
        logStr += "isGenerateSeparateOutputDir = " + isGenerateSeparateOutputDir + "\n\n";

        System.out.println("###################################");
        logStr += "###################################\n";

        return logStr;
    }

    public static void main (String[] args) throws Exception {
        if (!IS_RUNNING_BENCHMARK) { initiateSyntheticDevices(); }

        //////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////---CHECKING-DIRECTORY-///////////////////////////////

        File dataStorageDir = new File(dataStorageDirectory);

        if (SysParamSngltn.isGenerateSeparateOutputDir) { // original approach
            if (!dataStorageDir.exists()) {
                System.out.println("\n ERROR: directory not found: " + dataStorageDirectory);
                System.exit(1);
            }
        }
        else {  //Rui's version
            if (!dataStorageDir.exists()) {
                dataStorageDir.mkdirs();
                System.out.println("\n Creating directory for: " + dataStorageDirectory);
            }
        }
        //////////////////////////////////////////////////////////////////////////////////

        String logStr = "";

        MeasurementCollector measurementCollector = new MeasurementCollector(MAX_DATAPOINT_COLLECTON_SIZE);

        //Map<Double, List<Float>> globalDataCollector = new HashMap<>();
        Map<Double, Map<MEASUREMENT_TYPE, Map<CONSISTENCY_TYPE, Double>>> globalDataCollector = new LinkedHashMap<>();

        //List<Double> variableTrakcer = new ArrayList<>();
        Double changingParameterValue = -1.0;
        double lastGeneratedZipfeanFor = Double.MAX_VALUE; // NOTE: declare zipfean here... DO NOT declare it inside the for loop!

        List<MEASUREMENT_TYPE> measurementList = getMeasurementList();

        boolean isBenchmarkingDoneForSinglePass = false;
        String changingParameterName = null;
        for (int varIdx = 0; varIdx < variableList.size() || IS_RUNNING_BENCHMARK; varIdx++) {
            if (IS_RUNNING_BENCHMARK && isBenchmarkingDoneForSinglePass) { break; }
            if (IS_RUNNING_BENCHMARK) {
                variableList = new ArrayList<>();
                variableList.add(-123.0);

                isBenchmarkingDoneForSinglePass = true;
                changingParameterValue = variableList.get(0);
                changingParameterName = "benchmarking";
            } else {
                changingParameterValue = variableList.get(varIdx);
                changingParameterName = setChangingParameterName(varIdx);

                if (lastGeneratedZipfeanFor != zipF) {
                    lastGeneratedZipfeanFor = zipF;
                    String zipFianStr = prepareZipfian();
                    System.out.println(zipFianStr);
                    logStr += zipFianStr;
                }

                logStr += preparePrintableParameters();
            }

            int resolution = 10;
            int stepSize = Math.max(totalSampleCount / resolution, 1);

            for (int I = 0 ; I < totalSampleCount ; I++) {
                // Report progress
                if (I == totalSampleCount - 1 || totalSampleCount % stepSize == 0) {
                    if (IS_RUNNING_BENCHMARK)
                        System.out.println("currently Running BENCHMARK...... Progress = " + (int) (100.0 * (I + 1) / totalSampleCount) + "%");
                    else
                        System.out.println("currently Running for, " + changingParameterName + " = " +  changingParameterValue  + " Progress = " + (int) (100.0 * (I + 1) / totalSampleCount) + "%");
                }

                runAndCollect(changingParameterName, changingParameterValue, measurementList, measurementCollector);
            }

            logStr += "\n=========================================================================\n";

            if (!globalDataCollector.containsKey(changingParameterValue))
                globalDataCollector.put(changingParameterValue, new LinkedHashMap<>());

            for (MEASUREMENT_TYPE measurementType : measurementList) {
                if (!globalDataCollector.get(changingParameterValue).containsKey(measurementType))
                    globalDataCollector.get(changingParameterValue).put(measurementType, new LinkedHashMap<>());

                if (measurementType == MEASUREMENT_TYPE.STRETCH_RATIO ) {
                    if (SafeHomeSimulator.CONSISTENCY_ORDERING_LIST.contains(CONSISTENCY_TYPE.EVENTUAL)) {
                        double avg = measurementCollector.finalizePrepareStatsAndGetAvg(changingParameterValue, CONSISTENCY_TYPE.EVENTUAL, measurementType);
                        avg = (double)((int)(avg * 1000.0))/1000.0;
                        globalDataCollector.get(changingParameterValue).get(measurementType).put(CONSISTENCY_TYPE.EVENTUAL, avg);
                    }
                }
                else if (measurementType == MEASUREMENT_TYPE.COMPARE_WV_VS_GSV_END_STATE) {
                    if (SafeHomeSimulator.CONSISTENCY_ORDERING_LIST.contains(CONSISTENCY_TYPE.STRONG) && SafeHomeSimulator.CONSISTENCY_ORDERING_LIST.contains(CONSISTENCY_TYPE.WEAK)) {
                        double avg = measurementCollector.finalizePrepareStatsAndGetAvg(changingParameterValue, CONSISTENCY_TYPE.WEAK, measurementType);
                        avg = (double)((int)(avg * 1000.0))/1000.0;
                        globalDataCollector.get(changingParameterValue).get(measurementType).put(CONSISTENCY_TYPE.WEAK, avg);
                    }
                }
                else
                {
                    for (CONSISTENCY_TYPE consistency_type :  SafeHomeSimulator.CONSISTENCY_ORDERING_LIST) {
                        double avg = measurementCollector.finalizePrepareStatsAndGetAvg(changingParameterValue, consistency_type, measurementType);
                        avg = (double)((int)(avg * 1000.0))/1000.0;
                        globalDataCollector.get(changingParameterValue).get(measurementType).put(consistency_type, avg);
                    }
                }
            }
        }

        Map<MEASUREMENT_TYPE, String> perMeasurementAvgMap = new LinkedHashMap<>();
        String globalResult = printSummary(
                changingParameterName, changingParameterValue,
                measurementList, globalDataCollector,
                perMeasurementAvgMap);

        logStr += globalResult;

        ////////////////////-CREATING-SUBDIRECTORY-/////////////////////////////
        if (changingParameterName == null) {
            System.out.println("\n\n ERROR: changingParameterName was not initialized! something is wrong. Terminating...\n\n");
            System.exit(1);
        }


        String parentDirPath = "";

        if (SysParamSngltn.isGenerateSeparateOutputDir) { // original approach
            String epoch = System.currentTimeMillis() + "";
            parentDirPath = dataStorageDirectory + File.separator + epoch + "_VARY_"+ changingParameterName;
            parentDirPath += "_R_" + maxConcurrentRtn + "_C_" + minCmdCntPerRtn + "-" + maxCmdCntPerRtn;
        }
        else
        { //Rui's version
            parentDirPath = dataStorageDirectory;
        }

        File parentDir = new File(parentDirPath);
        if (!parentDir.exists()) {
            parentDir.mkdir();
        }

        String avgMeasurementDirectoryPath = parentDirPath + File.separator + "avg";
        File avgDir = new File(avgMeasurementDirectoryPath);
        if (!avgDir.exists()) {
            avgDir.mkdir();
        }

        ////////////////////////////////////////////////////////////////

        try
        {
            //String fileName = "VARY_" + changingParameterName + ".dat";
            String fileName = "Overall" + changingParameterName + ".txt";
            String filePath = parentDirPath + File.separator + fileName;

            Writer fileWriter = new FileWriter(filePath);
            fileWriter.write(logStr);
            fileWriter.close();

            for (Map.Entry<MEASUREMENT_TYPE, String> entry : perMeasurementAvgMap.entrySet()) {
                String measurementFilePath = avgMeasurementDirectoryPath + File.separator + entry.getKey().name() + ".dat";

                fileWriter = new FileWriter(measurementFilePath);
                fileWriter.write(entry.getValue());
                fileWriter.close();
            }
        }
        catch (IOException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        System.out.println("\n\nPROCESSING.....");
        measurementCollector.writeStatsInFile(parentDirPath, changingParameterName,
                HeaderListSnglTn.getInstance().CONSISTENCY_HEADER,
                CONSISTENCY_ORDERING_LIST);
        System.out.println(globalResult);


    }

    private static String printSummary(
            String changingParameterName,
            Double changingParameterValue,
            List<MEASUREMENT_TYPE> measurementList,
            Map<Double, Map<MEASUREMENT_TYPE, Map<CONSISTENCY_TYPE, Double>>> globalDataCollector,
            Map<MEASUREMENT_TYPE, String> perMeasurementAvgMap) {

        String globalResult = "\n--------------------------------\n";
        globalResult += "Summary-Start\t\n";

        for (MEASUREMENT_TYPE measurementType : measurementList) {
            if (measurementType == MEASUREMENT_TYPE.STRETCH_RATIO && !SafeHomeSimulator.CONSISTENCY_ORDERING_LIST.contains(CONSISTENCY_TYPE.EVENTUAL))
                continue;

            if (measurementType == MEASUREMENT_TYPE.COMPARE_WV_VS_GSV_END_STATE && ( !SafeHomeSimulator.CONSISTENCY_ORDERING_LIST.contains(CONSISTENCY_TYPE.STRONG) || !SafeHomeSimulator.CONSISTENCY_ORDERING_LIST.contains(CONSISTENCY_TYPE.WEAK) )   )
                continue;

            globalResult += "================================\n";
            globalResult += "MEASURING: " + measurementType.name() + "\n";

            String perMeasurementInfo = "";

            boolean isHeaderPrinted = false;
            for (double variable : variableList) {
                if (!isHeaderPrinted) {
                    perMeasurementInfo += changingParameterName + "\t";
                    for (CONSISTENCY_TYPE consistency_type : globalDataCollector.get(variable).get(measurementType).keySet()) {
                        perMeasurementInfo += HeaderListSnglTn.getInstance().CONSISTENCY_HEADER.get(consistency_type) + "\t";
                    }
                    perMeasurementInfo += "\n";

                    isHeaderPrinted = true;
                }

                perMeasurementInfo += variable + "\t";

                for (CONSISTENCY_TYPE consistency_type : globalDataCollector.get(changingParameterValue).get(measurementType).keySet()) {
                    double avg = globalDataCollector.get(variable).get(measurementType).get(consistency_type);
                    perMeasurementInfo += avg + "\t";
                }
                perMeasurementInfo += "\n";
            }
            globalResult += perMeasurementInfo;
            globalResult += "================================\n";

            perMeasurementAvgMap.put(measurementType, perMeasurementInfo);
        }

        globalResult += "Summary-End\t\n";
        globalResult += "--------------------------------\n";
        return globalResult;
    }

    private static void runAndCollect(
            String changingParameterName,
            Double changingParameterValue,
            List<MEASUREMENT_TYPE> measurementList,
            MeasurementCollector measurementCollector) throws Exception {
        List<Routine> routineSet = null;
        List<ActionConditionTuple> safetyRules = null;

        if (IS_RUNNING_BENCHMARK) {
            Benchmark benchmarkingTool = new Benchmark(RANDOM_SEED, MINIMUM_CONCURRENCY_LEVEL_FOR_BENCHMARKING);
            benchmarkingTool.initiateDevices(devIDlist);
            // Get routines.
            routineSet = benchmarkingTool.GetOneWorkload();
            System.out.printf("Routines: %s \n", routineSet.toString());
            int total_num_command = 0;
            for (Routine aRoutineSet : routineSet) {
                total_num_command += aRoutineSet.getNumberofCommand();
            }
            System.out.printf("Average number of command: %f \n", total_num_command * 1.0 / routineSet.size());
            // Get and register safety rules.
            safetyRules = generateSafetyRules();
            SafetyChecker safetyChecker = new SafetyChecker(safetyRules);
            if (!safetyChecker.registerAndValidateRules()) {
                System.out.println("Invalid Safety rule set for benchmark!");
            }
            System.exit(0);

        } else {
            routineSet = generateAutomatedRtn();
            //System.out.printf("Routines: %s \n", routineSet.toString());
            boolean valid = false;
            while (!valid) {
                safetyRules = generateSafetyRules();
                SafetyChecker safetyChecker = new SafetyChecker(safetyRules);
                valid = safetyChecker.registerAndValidateRules();
                if (!valid) {
                    System.out.println("Invalid Safety rule set!");
                }
            }
        }

        Map<DEV_ID, Routine> GSV_devID_lastAccesedRtn_Map = null;
        Map<DEV_ID, Routine> WV_devID_lastAccesedRtn_Map = null;

        for (CONSISTENCY_TYPE consistency_type :  SafeHomeSimulator.CONSISTENCY_ORDERING_LIST) {
            ExpResults expResult = runExperiment(devIDlist, consistency_type, routineSet, SIMULATION_START_TIME);

            if (measurementList.contains(MEASUREMENT_TYPE.COMPARE_WV_VS_GSV_END_STATE)) {
                if (consistency_type == CONSISTENCY_TYPE.WEAK) {
                    WV_devID_lastAccesedRtn_Map = expResult.measurement.devID_lastAccesedRtn_Map;
                }
                else if (consistency_type == CONSISTENCY_TYPE.STRONG) {
                    GSV_devID_lastAccesedRtn_Map = expResult.measurement.devID_lastAccesedRtn_Map;
                }
            }

            measurementCollector.collectData(changingParameterValue, consistency_type,
                    MEASUREMENT_TYPE.WAIT_TIME,
                    expResult.waitTimeHistogram);

            measurementCollector.collectData(changingParameterValue, consistency_type,
                    MEASUREMENT_TYPE.BACK2BACK_RTN_CMD_EXCTN_TIME,
                    expResult.back2backRtnExectnTimeHistogram);

            measurementCollector.collectData(changingParameterValue, consistency_type,
                    MEASUREMENT_TYPE.E2E_RTN_TIME,
                    expResult.e2eTimeHistogram);

            measurementCollector.collectData(changingParameterValue, consistency_type,
                    MEASUREMENT_TYPE.LATENCY_OVERHEAD,
                    expResult.latencyOverheadHistogram);

            measurementCollector.collectData(changingParameterValue, consistency_type,
                    MEASUREMENT_TYPE.E2E_VS_WAITTIME,
                    expResult.e2eVsWaitTimeHistogram);

            measurementCollector.collectData(changingParameterValue,consistency_type,
                    MEASUREMENT_TYPE.PARALLEL_DELTA,
                    expResult.measurement.deltaParallelismHistogram);

            measurementCollector.collectData(changingParameterValue,consistency_type,
                    MEASUREMENT_TYPE.PARALLEL_RAW,
                    expResult.measurement.rawParallelismHistogram);

            measurementCollector.collectData(changingParameterValue, consistency_type,
                    MEASUREMENT_TYPE.ISVLTN5_RTN_LIFESPAN_COLLISION_PERCENT,
                    expResult.measurement.isvltn5_routineLvlIsolationViolationTimePrcntHistogram);

//                    measurementCollector.collectData(changingParameterValue, consistency_type,
//                            MEASUREMENT_TYPE.ISVLTN4_CMD_TO_COMMIT_COLLISION_TIMESPAN_PRCNT,
//                            expResult.measurement.isvltn4_cmdToCommitCollisionTimespanPrcntHistogram);
//
//                    measurementCollector.collectData(changingParameterValue, consistency_type,
//                            MEASUREMENT_TYPE.ISVLTN3_CMD_VIOLATION_PRCNT_PER_RTN,
//                            expResult.measurement.isvltn3_CMDviolationPercentHistogram);
//
//                    measurementCollector.collectData(changingParameterValue, consistency_type,
//                            MEASUREMENT_TYPE.ISVLTN2_VIOLATED_RTN_PRCNT,
//                            expResult.measurement.isvltn2_RTNviolationPercentHistogram);
//
//                    measurementCollector.collectData(changingParameterValue, consistency_type,
//                            MEASUREMENT_TYPE.ISVLTN1_PER_RTN_COLLISION_COUNT,
//                            expResult.measurement.isvltn1_perRtnCollisionCountHistogram);

            measurementCollector.collectData(changingParameterValue, consistency_type,
                    MEASUREMENT_TYPE.ORDERR_MISMATCH_BUBBLE,
                    expResult.measurement.orderingMismatchPrcntBUBBLEHistogram);

            measurementCollector.collectData(changingParameterValue, consistency_type,
                    MEASUREMENT_TYPE.DEVICE_UTILIZATION,
                    expResult.measurement.devUtilizationPrcntHistogram);

            if (consistency_type == CONSISTENCY_TYPE.EVENTUAL) {
                measurementCollector.collectData(changingParameterValue, consistency_type,
                        MEASUREMENT_TYPE.STRETCH_RATIO,
                        expResult.stretchRatioHistogram);
            }
        }

        if (measurementList.contains(MEASUREMENT_TYPE.COMPARE_WV_VS_GSV_END_STATE)) {
            if ( (WV_devID_lastAccesedRtn_Map != null) && (GSV_devID_lastAccesedRtn_Map != null) ) {
                assert(GSV_devID_lastAccesedRtn_Map.size() == WV_devID_lastAccesedRtn_Map.size());

                float totalDevice = GSV_devID_lastAccesedRtn_Map.size();
                float endStateMismatchCount = 0;

                for (Map.Entry<DEV_ID, Routine> entry : GSV_devID_lastAccesedRtn_Map.entrySet() ) {
                    DEV_ID device = entry.getKey();
                    Routine routineGSV = entry.getValue();

                    assert(WV_devID_lastAccesedRtn_Map.containsKey(device));

                    Routine routineWV = WV_devID_lastAccesedRtn_Map.get(device);

                    if (routineWV.ID != routineGSV.ID) {
                        endStateMismatchCount++;
                    }
                }

                float endStateMismatchPercentage = (endStateMismatchCount/totalDevice)*100.0f;

                Map<Float, Integer> endStateMismatchPercentageHistogram = new HashMap<>();
                endStateMismatchPercentageHistogram.put(endStateMismatchPercentage , 1);

                measurementCollector.collectData(changingParameterValue, CONSISTENCY_TYPE.WEAK,
                        MEASUREMENT_TYPE.COMPARE_WV_VS_GSV_END_STATE,
                        endStateMismatchPercentageHistogram);
            }
        }
    }

    private static List<MEASUREMENT_TYPE> getMeasurementList() {
        List<MEASUREMENT_TYPE> measurementList = new ArrayList<>();
        if (isSchedulingPoliciesComparison) {
            // in the config file, set it true only for generating Fig14 data (EUROSYS 2021 submission)
            CONSISTENCY_ORDERING_LIST.add(CONSISTENCY_TYPE.LAZY_FCFS);
            CONSISTENCY_ORDERING_LIST.add(CONSISTENCY_TYPE.LAZY_PRIORITY);
            CONSISTENCY_ORDERING_LIST.add(CONSISTENCY_TYPE.EVENTUAL);
            measurementList.add(MEASUREMENT_TYPE.E2E_RTN_TIME);
            measurementList.add(MEASUREMENT_TYPE.PARALLEL_DELTA);
            measurementList.add(MEASUREMENT_TYPE.ISVLTN5_RTN_LIFESPAN_COLLISION_PERCENT);
        } else if (isAnalyzingTLunderEV) {
            CONSISTENCY_ORDERING_LIST.add(CONSISTENCY_TYPE.STRONG); //GSV
            CONSISTENCY_ORDERING_LIST.add(CONSISTENCY_TYPE.EVENTUAL);
            measurementList.add(MEASUREMENT_TYPE.E2E_RTN_TIME);
            measurementList.add(MEASUREMENT_TYPE.ISVLTN5_RTN_LIFESPAN_COLLISION_PERCENT);
        } else {
            CONSISTENCY_ORDERING_LIST.add(CONSISTENCY_TYPE.STRONG);
            CONSISTENCY_ORDERING_LIST.add(CONSISTENCY_TYPE.RELAXED_STRONG);
            CONSISTENCY_ORDERING_LIST.add(CONSISTENCY_TYPE.EVENTUAL);
            CONSISTENCY_ORDERING_LIST.add(CONSISTENCY_TYPE.WEAK);
            CONSISTENCY_ORDERING_LIST.add(CONSISTENCY_TYPE.LAZY_FCFS);
            CONSISTENCY_ORDERING_LIST.add(CONSISTENCY_TYPE.LAZY_PRIORITY);
            measurementList.add(MEASUREMENT_TYPE.WAIT_TIME);
            measurementList.add(MEASUREMENT_TYPE.BACK2BACK_RTN_CMD_EXCTN_TIME);
            measurementList.add(MEASUREMENT_TYPE.E2E_RTN_TIME);
            measurementList.add(MEASUREMENT_TYPE.LATENCY_OVERHEAD);
            measurementList.add(MEASUREMENT_TYPE.E2E_VS_WAITTIME);
            measurementList.add(MEASUREMENT_TYPE.STRETCH_RATIO);
            measurementList.add(MEASUREMENT_TYPE.PARALLEL_DELTA);
            measurementList.add(MEASUREMENT_TYPE.PARALLEL_RAW);
            measurementList.add(MEASUREMENT_TYPE.ORDERR_MISMATCH_BUBBLE);
            measurementList.add(MEASUREMENT_TYPE.DEVICE_UTILIZATION);
//          measurementList.add(MEASUREMENT_TYPE.ISVLTN1_PER_RTN_COLLISION_COUNT);
//          measurementList.add(MEASUREMENT_TYPE.ISVLTN2_VIOLATED_RTN_PRCNT);
//          measurementList.add(MEASUREMENT_TYPE.ISVLTN3_CMD_VIOLATION_PRCNT_PER_RTN);
//          measurementList.add(MEASUREMENT_TYPE.ISVLTN4_CMD_TO_COMMIT_COLLISION_TIMESPAN_PRCNT);
            measurementList.add(MEASUREMENT_TYPE.ISVLTN5_RTN_LIFESPAN_COLLISION_PERCENT);
            measurementList.add(MEASUREMENT_TYPE.COMPARE_WV_VS_GSV_END_STATE);
        }
        return measurementList;
    }

    private static String setChangingParameterName(int varIdx) {
        Double changingParameterValue = variableList.get(varIdx);
        if (isVaryShrinkFactor) {
            shrinkFactor = changingParameterValue;
            return "shrinkFactor";
        } else if (isVaryZipfAlpha) {
            zipF = changingParameterValue;
            return "zipF";
        } else if (isVaryLongRunningPercent) {
            longRrtnPcntg = changingParameterValue;
            return "longRrtnPcntg";
        } else if (isVaryCommandCntPerRtn) {
            double maxVal = variableCorrespndinMaxValList.get(varIdx);
            minCmdCntPerRtn = changingParameterValue;
            maxCmdCntPerRtn = maxVal;
            return "minCmdCntPerRtn";
        } else if (isVaryLongRunningDuration) {
            double maxVal = variableCorrespndinMaxValList.get(varIdx);
            minLngRnCmdTimSpn = changingParameterValue;
            maxLngRnCmdTimSpn = maxVal;
            return "minLngRnCmdTimSpn";
        } else if (isVaryShortRunningDuration) {
            double maxVal = variableCorrespndinMaxValList.get(varIdx);
            minShrtCmdTimeSpn = changingParameterValue;
            maxShrtCmdTimeSpn = maxVal;
            return "minShrtCmdTimeSpn";
        } else if (isVaryMustCmdPercentage) {
            mustCmdPercentage = changingParameterValue;
            return "mustPrcnt";
        } else if (isVaryDevFailureRatio) {
            devFailureRatio = changingParameterValue;
            return "DevFailPrcnt";
        } else {
            System.out.println("Error: unknown selection.... Terminating...");
            System.exit(1);
        }
        return "InvalidChangingParam";
    }

    private static DEV_ID getZipfDistDevID(float randDouble) {
        assert(0 < devID_ProbBoundaryMap.size());
        assert(0.0f <= randDouble && randDouble <= 1.0f);

        for (Map.Entry<DEV_ID, ZipfProbBoundary> entry : devID_ProbBoundaryMap.entrySet()) {
            if (entry.getValue().isInsideBoundary(randDouble))
                return entry.getKey();
        }

        assert(false); // code should not reach to this line
        return null; // key not found in map... something is wrong;
    }

    private static String prepareZipfian() {
        assert(0 < devIDlist.size());

        int numberOfElements = devIDlist.size();

        ZipfDistribution zipf = new ZipfDistribution(numberOfElements, zipF);

        List<Float> cumulativeProbabilityList = new ArrayList<>();

        for (int I = 0 ; I < devIDlist.size() ; I++) {
            float probability = (float)zipf.probability(I + 1);

            if (I == 0)
                cumulativeProbabilityList.add(probability);
            else
                cumulativeProbabilityList.add(probability + cumulativeProbabilityList.get(I - 1));
        }

        //System.out.println(cumulativeProbabilityList);

        float lowerInclusive = 0.0f;

        for (int I = 0 ; I < devIDlist.size() ; I++) {
            float upperExclusive = cumulativeProbabilityList.get(I);

            if (I == devIDlist.size() - 1)
                upperExclusive = 1.01f;

            //System.out.println( "item " + I + " lowerInclusive = " + lowerInclusive + " upperExclusive = " + upperExclusive );

            ZipfProbBoundary zipfProbBoundary = new ZipfProbBoundary(lowerInclusive, upperExclusive);
            devID_ProbBoundaryMap.put( devIDlist.get(I) ,zipfProbBoundary);

            lowerInclusive = cumulativeProbabilityList.get(I);
        }

        Map<DEV_ID, Integer> histogram = new HashMap<>();

        Double sampleSize = 1000000.0;
        for (int I = 0 ; I < sampleSize ; I++) {
            DEV_ID devId = getZipfDistDevID(rand.nextFloat());
            if (!histogram.containsKey(devId))
                histogram.put(devId, 0);

            histogram.put(devId, histogram.get(devId) + 1 );
        }

        String str = "";

        for (DEV_ID devId: devIDlist) {
            if (histogram.containsKey(devId)) {
                Double percentage = (histogram.get(devId) / sampleSize) * 100.0;
                String formattedStr = String.format("%s -> selection probability = %.2f%%", devId.name(), percentage);
                str += formattedStr + "\n";
            }

        }
        return str;
    }


    private static int ROUTINE_ID = 0;

    public static int getUniqueRtnID() {
        return SafeHomeSimulator.ROUTINE_ID++;
    }

    private static DEV_STATE getRandomDevState() {
        return DEV_STATE.values()[rand.nextInt(numDevState)];
    }

    private static List<Routine> generateAutomatedRtn() {
        if (maxCmdCntPerRtn < minCmdCntPerRtn ||
                maxLngRnCmdTimSpn < minLngRnCmdTimSpn ||
                maxShrtCmdTimeSpn < minShrtCmdTimeSpn) {
            System.out.println("\n ERROR: maxCmdCntPerRtn = " + maxCmdCntPerRtn + ", minCmdCntPerRtn = " + minCmdCntPerRtn);
            System.out.println("\n ERROR: maxLngRnCmdTimSpn = " + maxLngRnCmdTimSpn + ", minLngRnCmdTimSpn = " + minLngRnCmdTimSpn);
            System.out.println("\n ERROR: maxShrtCmdTimeSpn = " + maxShrtCmdTimeSpn + ", minShrtCmdTimeSpn = " + minShrtCmdTimeSpn + "\n Terminating.....");
            System.exit(1);
        }

        List<Routine> routineList = new ArrayList<>();

        int totalConcurrentRtn = maxConcurrentRtn;

        int longRunningRoutineCount = 0;

        for (int RoutineCount = 0 ; RoutineCount < totalConcurrentRtn ; ++RoutineCount) {
            float nextDbl = rand.nextFloat();
            nextDbl = (nextDbl == 1.0f) ? nextDbl - 0.001f : nextDbl;
            boolean isLongRunning = (nextDbl < longRrtnPcntg);

            if (isLongRunning)
                longRunningRoutineCount++;

            if (isAtleastOneLongRunning && (RoutineCount == totalConcurrentRtn - 1) && longRunningRoutineCount == 0) {
                isLongRunning = true; // at least one routine will be long running;
            }


            int difference = 1 + (int)maxCmdCntPerRtn - (int)minCmdCntPerRtn;
            int totalCommandInThisRtn = (int)minCmdCntPerRtn + rand.nextInt(difference);

            if (devIDlist.size() < totalCommandInThisRtn ) {
                System.out.println("\n ERROR: ID 2z3A9s : totalCommandInThisRtn = " + totalCommandInThisRtn + " > devIDlist.size() = " + devIDlist.size());
                System.exit(1);
            }

            Map<DEV_ID, Integer> devIDDurationMap = new HashMap<>();
            List<DEV_ID> devList = new ArrayList<>();

            while(devIDDurationMap.size() < totalCommandInThisRtn) {
                DEV_ID devID;

                devID = getZipfDistDevID(rand.nextFloat());

                if (devIDDurationMap.containsKey(devID))
                    continue;

                int duration;
                int currentDurationMapSize = devIDDurationMap.size();
                int middleCommandIndex = totalCommandInThisRtn / 2;
                if (isLongRunning && ( currentDurationMapSize == middleCommandIndex) ) { // select the  middle command as long running command
                    difference = 1 + (int)maxLngRnCmdTimSpn - (int)minLngRnCmdTimSpn;
                    duration = (int)minLngRnCmdTimSpn + rand.nextInt(difference);
                } else {
                    difference = 1 + (int)maxShrtCmdTimeSpn - (int)minShrtCmdTimeSpn;
                    duration = (int)minShrtCmdTimeSpn + rand.nextInt(difference);
                }

                devIDDurationMap.put(devID, duration);
                devList.add(devID);
            }

            Routine rtn = new Routine();

            for (DEV_ID devID : devList) {
                assert(devIDDurationMap.containsKey(devID));

                nextDbl = rand.nextFloat();
                nextDbl = (nextDbl == 1.0f) ? nextDbl - 0.001f : nextDbl;
                boolean isMust = (nextDbl < mustCmdPercentage);
                DEV_STATE dev_state = getRandomDevState();
                Command cmd = new Command(devID,dev_state, devIDDurationMap.get(devID), isMust, mustCmdPercentage);
                rtn.addCommand(cmd);
            }
            routineList.add(rtn);
        }

        Collections.shuffle(routineList, rand);

        if (shrinkFactor == 0.0) {
            for (int index = 0 ; index < routineList.size() ; ++index) {
                routineList.get(index).registrationTime = SIMULATION_START_TIME;
            }
        } else {
            float allRtnBackToBackExcTime = 0.0f;
            for (Routine rtn : routineList) {
                allRtnBackToBackExcTime += rtn.getBackToBackCmdExecutionTimeWithoutGap();
            }

            double simulationLastRtnStartTime = allRtnBackToBackExcTime * shrinkFactor;

            int upperLimit = (int)Math.ceil(simulationLastRtnStartTime);

            List<Integer> randStartPointList = new ArrayList<>();
            for (int I = 0 ; I < routineList.size() ; I++) {
                int randStartPoint = SIMULATION_START_TIME +  ((upperLimit == 0) ? 0 : rand.nextInt(upperLimit));
                randStartPointList.add(randStartPoint);
            }

            Collections.sort(randStartPointList);

            for (int I = 0 ; I < routineList.size() ; I++) {
                routineList.get(I).registrationTime = randStartPointList.get(I);
            }
        }

        for (int index = 0 ; index < routineList.size() ; ++index) {
            routineList.get(index).ID = getUniqueRtnID();
        }


        return routineList;
    }

    private static List<ActionConditionTuple> generateSafetyRules() {
        List<ActionConditionTuple> rules = new ArrayList<>();
        for (int i = 0; i < numSafetyRule; ++i) {
            ActionConditionTuple rule = new ActionConditionTuple(
                    getZipfDistDevID(rand.nextFloat()), // actionDevID
                    getRandomDevState(),
                    getZipfDistDevID(rand.nextFloat()), // conditionID
                    getRandomDevState()
            );
            rules.add(rule);
        }
        return rules;
    }

    private static String printInitialRoutineList(List<Routine> routineList) {
        String logStr = "";
        for (Routine rtn : routineList) {
            System.out.println("# " + rtn);
            logStr += "#" + rtn + "\n";
        }
        return logStr;
    }

    public static ExpResults runExperiment(
            List<DEV_ID> _devIDlist,
            CONSISTENCY_TYPE _consistencyType,
            final List<Routine> _originalRtnList,
            int _simulationStartTime) {

        LockTable lockTable = new LockTable(_devIDlist, _consistencyType);
        List<Routine> perExpRtnList = new ArrayList<>();
        for (Routine originalRtn: _originalRtnList) {
            perExpRtnList.add(originalRtn.getDeepCopy());
        }


        lockTable.register(perExpRtnList, _simulationStartTime);

//        if (_consistencyType == CONSISTENCY_TYPE.EVENTUAL) {
//            System.out.println(lockTable);
//            System.out.println("Lock table printed... Terminating program....");
//            System.exit(1);
//        }

        ExpResults expResults = new ExpResults();

//        if (_consistencyType != CONSISTENCY_TYPE.WEAK)
//            expResults.failureAnalyzer = new FailureAnalyzer(lockTable.lockTable, _consistencyType);

        expResults.measurement = new Measurement(lockTable);


        for (Routine routine : perExpRtnList) {
            float data;
            Integer count;

            //////////////////////////////////////////////////
            //expResults.waitTimeList.add(routine.getStartDelay());
            data = routine.getStartDelay();
            count = expResults.waitTimeHistogram.get(data);

            if (count == null)
                expResults.waitTimeHistogram.put(data, 1);
            else
                expResults.waitTimeHistogram.put(data, count + 1);
            //////////////////////////////////////////////////
            //////////////////////////////////////////////////
            data = routine.getEndToEndLatency();
            count = expResults.e2eTimeHistogram.get(data);

            if (count == null)
                expResults.e2eTimeHistogram.put(data, 1);
            else
                expResults.e2eTimeHistogram.put(data, count + 1);

            //////////////////////////////////////////////////
            //////////////////////////////////////////////////
            data = routine.backToBackCmdExecutionWithoutGap;

            count = expResults.back2backRtnExectnTimeHistogram.get(data);

            if (count == null)
                expResults.back2backRtnExectnTimeHistogram.put(data, 1);
            else
                expResults.back2backRtnExectnTimeHistogram.put(data, count + 1);

            //////////////////////////////////////////////////

            //expResults.latencyOverheadList.add(routine.getLatencyOverheadPrcnt());
            data = routine.getLatencyOverheadPrcnt();
            count = expResults.latencyOverheadHistogram.get(data);

            if (count == null)
                expResults.latencyOverheadHistogram.put(data, 1);
            else
                expResults.latencyOverheadHistogram.put(data, count + 1);
            //////////////////////////////////////////////////
            data = routine.getE2EvsWaittime();
            count = expResults.e2eVsWaitTimeHistogram.get(data);

            if (count == null)
                expResults.e2eVsWaitTimeHistogram.put(data, 1);
            else
                expResults.e2eVsWaitTimeHistogram.put(data, count + 1);

            //////////////////////////////////////////////////
            //////////////////////////////////////////////////
            //expResults.stretchRatioList.add(routine.getStretchRatio());
            data = routine.getStretchRatio();
            count = expResults.stretchRatioHistogram.get(data);

            if (count == null)
                expResults.stretchRatioHistogram.put(data, 1);
            else
                expResults.stretchRatioHistogram.put(data, count + 1);
            //////////////////////////////////////////////////

            assert(!expResults.waitTimeHistogram.isEmpty());
            assert(!expResults.latencyOverheadHistogram.isEmpty());
            assert(!expResults.stretchRatioHistogram.isEmpty());
        }

        return expResults;
    }
}
