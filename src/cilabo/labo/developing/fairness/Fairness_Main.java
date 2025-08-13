package cilabo.labo.developing.fairness;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.component.termination.Termination;
import org.uma.jmetal.component.termination.impl.TerminationByEvaluations;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.observer.impl.EvaluationObserver;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import cilabo.data.DataSet;
import cilabo.data.DataSetManager;
import cilabo.data.Input;
import cilabo.fuzzy.classifier.Classifier;
import cilabo.fuzzy.classifier.classification.Classification;
import cilabo.fuzzy.classifier.classification.impl.SingleWinnerRuleSelection;
import cilabo.fuzzy.classifier.impl.Classifier_basic;
import cilabo.fuzzy.knowledge.factory.HomoTriangleKnowledgeFactory;
import cilabo.fuzzy.knowledge.membershipParams.Parameters;
import cilabo.fuzzy.rule.Rule.RuleBuilder;
import cilabo.fuzzy.rule.antecedent.factory.impl.HeuristicRuleGenerationMethod;
import cilabo.fuzzy.rule.impl.Rule_Basic;
import cilabo.gbml.algorithm.HybridMoFGBMLwithNSGAII;
import cilabo.gbml.objectivefunction.michigan.RuleLength;
import cilabo.gbml.objectivefunction.pittsburgh.Gmean;
import cilabo.gbml.objectivefunction.pittsburgh.NumberOfRules;
import cilabo.gbml.objectivefunction.pittsburgh.fairness.DemographicParityDifference;
import cilabo.gbml.objectivefunction.pittsburgh.fairness.FalsePositiveRateDifference;
import cilabo.gbml.objectivefunction.pittsburgh.fairness.PositivePredictiveValuesDifference;
import cilabo.gbml.operator.crossover.HybridGBMLcrossover;
import cilabo.gbml.operator.crossover.MichiganCrossover;
import cilabo.gbml.operator.crossover.PittsburghCrossover;
import cilabo.gbml.operator.mutation.PittsburghMutation;
import cilabo.gbml.solution.michiganSolution.AbstractMichiganSolution;
import cilabo.gbml.solution.michiganSolution.MichiganSolution.MichiganSolutionBuilder;
import cilabo.gbml.solution.michiganSolution.impl.MichiganSolution_Basic;
import cilabo.gbml.solution.pittsburghSolution.impl.PittsburghSolution_Basic;
import cilabo.main.Consts;
import cilabo.util.fileoutput.PittsburghSolutionListOutputX;
import cilabo.utility.Output;
import cilabo.utility.Parallel;
import cilabo.utility.Random;

/**
 * @version 1.0
 *
 * 2023, May
 *
 */
public class Fairness_Main {
	public static void main(String[] args) throws JMetalException, FileNotFoundException {
		String sep = File.separator;

		/* ********************************************************* */
		System.out.println();
		System.out.println("==== INFORMATION ====");
		System.out.println("main: " + Fairness_Main.class.getCanonicalName());
		String version = "1.0";
		System.out.println("version: " + version);
		System.out.println();
		System.out.println("Algorithm: Hybrid-style Multiobjective Fuzzy Genetics-Based Machine Learning for Fairness Datasets");
		System.out.println("EMOA: NSGA-II");
		System.out.println();
		/* ********************************************************* */
		// Load consts.properties
		Consts.set("consts");
		// make result directory
		Output.mkdirs(Consts.ROOTFOLDER);

		// set command arguments to static variables
		CommandLineArgs.loadArgs(CommandLineArgs.class.getCanonicalName(), args);
		// Output constant parameters
		String fileName = Consts.EXPERIMENT_ID_DIR + sep + "Consts.txt";
		Output.writeln(fileName, Consts.getString(), true);
		Output.writeln(fileName, CommandLineArgs.getParamsString(), true);

		// Initialize ForkJoinPool
		Parallel.getInstance().initLearningForkJoinPool(CommandLineArgs.parallelCores);

		System.out.println("Processors: " + Runtime.getRuntime().availableProcessors() + " ");
		System.out.print("args: ");
		for(int i = 0; i < args.length; i++) {
			System.out.print(args[i] + " ");
		}
		System.out.println();
		System.out.println("=====================");
		System.out.println();

		/* ********************************************************* */
		System.out.println("==== EXPERIMENT =====");
		Date start = new Date();
		System.out.println("START: " + start);

		/* Random Number ======================= */
		Random.getInstance().initRandom(CommandLineArgs.seed);
		JMetalRandom.getInstance().setSeed(CommandLineArgs.seed);

		/* Load Dataset ======================== */
		Input.loadTrainTestFiles_Fairness(CommandLineArgs.trainFile, CommandLineArgs.testFile);
		DataSet<FairnessPattern> test = (DataSet<FairnessPattern>) DataSetManager.getInstance().getTests().get(0);
		DataSet<FairnessPattern> train = (DataSet<FairnessPattern>) DataSetManager.getInstance().getTrains().get(0);


		/* Run MoFGBML algorithm =============== */
		fairnessMoFGBML(train, test);
		/* ===================================== */

		Date end = new Date();
		System.out.println("END: " + end);
		System.out.println("=====================");
		/* ********************************************************* */

		System.exit(0);
	}

	/**
	 *
	 */
	public static void fairnessMoFGBML(DataSet<FairnessPattern> train, DataSet<FairnessPattern> test) {
		Random.getInstance().initRandom(CommandLineArgs.seed);
		String sep = File.separator;

		Parameters parameters = new Parameters(train);
		HomoTriangleKnowledgeFactory KnowledgeFactory = new HomoTriangleKnowledgeFactory(parameters);
		KnowledgeFactory.create2_3_4_5();

		List<Pair<Integer, Integer>> bounds_Michigan = AbstractMichiganSolution.makeBounds();
		int numberOfObjectives_Michigan = 1;
		int numberOfConstraints_Michigan = 0;

		int numberOfvariables_Pittsburgh = Consts.INITIATION_RULE_NUM;
		//int numberOfObjectives_Pittsburgh = 2;
		//int numberOfConstraints_Pittsburgh = 0;

		RuleBuilder<Rule_Basic, ?, ?> ruleBuilder = new Rule_Basic.RuleBuilder_Basic(
				new HeuristicRuleGenerationMethod(train),
				new MoFGBML_Learning_Fairness(train));

		MichiganSolutionBuilder<MichiganSolution_Basic<Rule_Basic>> michiganSolutionBuilder
		= new MichiganSolution_Basic.MichiganSolutionBuilder_Basic<Rule_Basic>(
				bounds_Michigan,
				numberOfObjectives_Michigan,
				numberOfConstraints_Michigan,
				ruleBuilder);

		Classification<MichiganSolution_Basic<Rule_Basic>> classification = new SingleWinnerRuleSelection<MichiganSolution_Basic<Rule_Basic>>();

		Classifier<MichiganSolution_Basic<Rule_Basic>> classifier = new Classifier_basic<>(classification);

		Problem<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> problem = null;

		switch(CommandLineArgs.mopIndex) {
		case 1:
			problem = new MOP1_fairness<MichiganSolution_Basic<Rule_Basic>>(numberOfvariables_Pittsburgh,2,0,train,michiganSolutionBuilder,classifier);
			break;

		case 2:
			problem = new MOP2_fairness<MichiganSolution_Basic<Rule_Basic>>(numberOfvariables_Pittsburgh,2,0,train,michiganSolutionBuilder,classifier);
			break;

		case 3:
			problem = new MOP3_fairness<MichiganSolution_Basic<Rule_Basic>>(numberOfvariables_Pittsburgh,2,0,train,michiganSolutionBuilder,classifier);
			break;

		case 4:
			problem = new MOP4_fairness<MichiganSolution_Basic<Rule_Basic>>(numberOfvariables_Pittsburgh,3,0,train,michiganSolutionBuilder,classifier);
			break;

		case 5:
			problem = new MOP5_fairness<MichiganSolution_Basic<Rule_Basic>>(numberOfvariables_Pittsburgh,3,0,train,michiganSolutionBuilder,classifier);
			break;

		case 6:
			problem = new MOP6_fairness<MichiganSolution_Basic<Rule_Basic>>(numberOfvariables_Pittsburgh,3,0,train,michiganSolutionBuilder,classifier);
			break;

		case 7:
			problem = new MOP7_fairness<MichiganSolution_Basic<Rule_Basic>>(numberOfvariables_Pittsburgh,4,0,train,michiganSolutionBuilder,classifier);
			break;

		case 8:
			problem = new MOP8_fairness<MichiganSolution_Basic<Rule_Basic>>(numberOfvariables_Pittsburgh,2,0,train,michiganSolutionBuilder,classifier);
			break;
		}

		/* Crossover: Hybrid-style GBML specific crossover operator. */
		double crossoverProbability = 1.0;

		/* Michigan operation */
		CrossoverOperator<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> michiganX
		         = new MichiganCrossover<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>, MichiganSolution_Basic<Rule_Basic>>(Consts.MICHIGAN_CROSS_RT, train);
		/* Pittsburgh operation */
		CrossoverOperator<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> pittsburghX
		         = new PittsburghCrossover<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>, MichiganSolution_Basic<Rule_Basic>>(Consts.PITTSBURGH_CROSS_RT);
		/* Hybrid-style crossover */
		CrossoverOperator<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> crossover
		         = new HybridGBMLcrossover<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>, MichiganSolution_Basic<Rule_Basic>>(crossoverProbability, Consts.MICHIGAN_OPE_RT, michiganX, pittsburghX);
		/* Mutation: Pittsburgh-style GBML specific mutation operator. */
		MutationOperator<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> mutation
		         = new PittsburghMutation<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>, MichiganSolution_Basic<Rule_Basic>>(train);


		/* Termination: Number of total evaluations */
		Termination termination = new TerminationByEvaluations(Consts.TERMINATE_EVALUATION);

		/* Algorithm: Hybrid-style MoFGBML with NSGA-II */
		HybridMoFGBMLwithNSGAII<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> algorithm
			= new HybridMoFGBMLwithNSGAII<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>>(problem,
					Consts.POPULATION_SIZE,
					Consts.OFFSPRING_POPULATION_SIZE,
					Consts.OUTPUT_FREQUENCY,
					Consts.EXPERIMENT_ID_DIR,
					crossover,
					mutation,
					termination);

		/* Running observation */
		EvaluationObserver evaluationObserver = new EvaluationObserver(Consts.OUTPUT_FREQUENCY);
		algorithm.getObservable().register(evaluationObserver);

		/* === GA RUN === */
		algorithm.run();
		/* ============== */

		/* Non-dominated solutions in final generation */
		List<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> nonDominatedSolutions = algorithm.getResult();

		/* archive population */
		Set<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> ARC = algorithm.getArchivePopulation();

		List<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> ARCList = new ArrayList<>(ARC);

		/*アーカイブから非劣解を抽出（分割なしversion）*/
		//List<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> nonDominatedSolutionsARC = SolutionListUtils.getNonDominatedSolutions(ARCList);

		/*アーカイブから非劣解を抽出（分割ありversion）*/
		//サブリスト数（暫定で100に設定）
		int numberOfSublists = 100;

		//サブリストを格納するリスト
		List<List<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>>> partitionedList = new ArrayList<>();

		//分割に用いるパラメータの算出
		int totalSize = ARCList.size();
        int chunkSize = totalSize / numberOfSublists;
        int remainder = totalSize % numberOfSublists;
        int start = 0;

        // 元のリストの要素をサブリストに分割
        for (int i = 0; i < numberOfSublists; i++) {
            int end = start + chunkSize + (i < remainder ? 1 : 0);
            partitionedList.add(new ArrayList<>(ARCList.subList(start, end)));
            start = end;
        }

        // partitionedList内の各サブリストにgetNonDominatedSolutionsを適用し、結果を統合
        List<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> mergedList = partitionedList.stream()
                .flatMap(list -> SolutionListUtils.getNonDominatedSolutions(list).stream())
                .collect(Collectors.toList());

        //統合後のリストから非劣解を抽出し，最終的な個体群とする
        List<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> nonDominatedSolutionsARC = SolutionListUtils.getNonDominatedSolutions(mergedList);

		String outputRootDir = Consts.EXPERIMENT_ID_DIR;
		new PittsburghSolutionListOutputX(nonDominatedSolutionsARC)
        .setVarFileOutputContext(new DefaultFileOutputContext(outputRootDir + sep + String.format("VARARC-%d.csv", Consts.TERMINATE_EVALUATION), ","))
        .setFunFileOutputContext(new DefaultFileOutputContext(outputRootDir + sep + String.format("FUNARC-%d.csv", Consts.TERMINATE_EVALUATION), ","))
        .print();

        //バグ含むのでコメントアウト（修正するならJmetal仕様のメソッドを書き換える）
	    /*new SolutionListOutput(nonDominatedSolutions)
        	.setVarFileOutputContext(new DefaultFileOutputContext(Consts.EXPERIMENT_ID_DIR+sep+"VAR.csv", ","))
        	.setFunFileOutputContext(new DefaultFileOutputContext(Consts.EXPERIMENT_ID_DIR+sep+"FUN.csv", ","))
        	.print();*/

		//Results of final generation
	    ArrayList<String> strs = new ArrayList<>();
	    String str = "pop,Gmean_Dtra,Gmean_Dtst,FPR_Dtra,FPR_Dtst,PPV_Dtra,PPV_Dtst,ruleNum,RL,Cover,RW,DP_Dtra,DP_Dtst";
	    strs.add(str);

	    for(int i = 0; i < nonDominatedSolutions.size(); i++) {
	    	PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>> solution = nonDominatedSolutions.get(i);
			Gmean<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> function1
				= new Gmean<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>>();
			FalsePositiveRateDifference<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> function2
			= new FalsePositiveRateDifference<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>>();
			PositivePredictiveValuesDifference<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> function3
			= new PositivePredictiveValuesDifference<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>>();
			NumberOfRules<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> function4
			= new NumberOfRules<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>>();
			DemographicParityDifference<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> function5
			= new DemographicParityDifference<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>>();

			double Gmean_Dtra = function1.function(solution, train);
			double Gmean_Dtst= function1.function(solution, test);
			double FPR_Dtra = function2.function(solution, train);
			double FPR_Dtst = function2.function(solution, test);
			double PPV_Dtra = function3.function(solution, train);
			double PPV_Dtst = function3.function(solution, test);
			double ruleNum = function4.function(solution);
			double DP_Dtra = function5.function(solution, train);
			double DP_Dtst = function5.function(solution, test);

			double Gmean_tra = 1-Gmean_Dtra;
			double Gmean_tst = 1-Gmean_Dtst;

            RuleLength<MichiganSolution_Basic<Rule_Basic>> RuleLengthFunc = new RuleLength<MichiganSolution_Basic<Rule_Basic>>();
            double TotalRuleLength = 0;
            for (int j = 0; j < nonDominatedSolutions.get(i).getNumberOfVariables(); j++) {
                 double RuleLength = RuleLengthFunc.function(nonDominatedSolutions.get(i).getVariable(j));
                 TotalRuleLength += RuleLength;
            }

            double TotalCover = 0;
            for (int j = 0; j < nonDominatedSolutions.get(i).getNumberOfVariables(); j++) {

            	 double Cover = 0;
            	 List<Double> support = new ArrayList<Double>();

            	 for (int k = 0; k < train.getNdim(); k++) {
            		  if (nonDominatedSolutions.get(i).getVariable(j).getVariable(k) != 0) {

            			  if ((nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 1) ||
            				  (nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 2) ||
            				  (nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 4)){
            				   support.add(1.0);
            			  }else if ((nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 3) ||
                				  (nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 5) ||
                				  (nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 11) ||
                				  (nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 12) ||
                				  (nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 13)){
                				   support.add(1.0/2);
            			  }else if ((nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 6) ||
                				  (nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 9)){
                				   support.add(1.0/3);
            			  }else if ((nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 7) ||
                				  (nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 8)){
           				   support.add(2.0/3);
       			          }else if ((nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 10) ||
                				  (nonDominatedSolutions.get(i).getVariable(j).getVariable(k) == 14)){
           				   support.add(1.0/4);
       			          }
            		  }
            	 }
            	 if (!support.isEmpty()) {
            	     Cover = support.stream().reduce(1.0, (a, b) -> a * b);
                   	 TotalCover += Cover;
                 }
            }

            double TotalRW = 0;
            for (int j = 0; j < nonDominatedSolutions.get(i).getNumberOfVariables(); j++) {
                double RW = (Double) nonDominatedSolutions.get(i).getVariable(j).getRuleWeight().getRuleWeightValue();
                TotalRW += RW;
            }
            double AveRW = TotalRW/(nonDominatedSolutions.get(i).getNumberOfVariables());

	    	str = String.valueOf(i);
	    	str += "," + Gmean_tra + "," + Gmean_tst + "," + FPR_Dtra + "," + FPR_Dtst + "," + PPV_Dtra + "," + PPV_Dtst + "," + ruleNum + "," + TotalRuleLength + "," + TotalCover + "," + AveRW + "," + DP_Dtra + "," + DP_Dtst;
	    	strs.add(str);
	    }
	    String fileName = Consts.EXPERIMENT_ID_DIR + sep + "results.csv";
	    Output.writeln(fileName, strs, false);

	    //Results of archive population
	    ArrayList<String> strsARC = new ArrayList<>();
	    String strARC = "pop,Gmean_Dtra,Gmean_Dtst,FPR_Dtra,FPR_Dtst,PPV_Dtra,PPV_Dtst,ruleNum,RL,Cover,RW,DP_Dtra,DP_Dtst";
	    strsARC.add(strARC);

	    for(int i = 0; i < nonDominatedSolutionsARC.size(); i++) {
	    	PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>> solutionARC = nonDominatedSolutionsARC.get(i);
			Gmean<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> function1ARC
				= new Gmean<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>>();
			FalsePositiveRateDifference<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> function2ARC
			= new FalsePositiveRateDifference<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>>();
			PositivePredictiveValuesDifference<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> function3ARC
			= new PositivePredictiveValuesDifference<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>>();
			NumberOfRules<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> function4ARC
			= new NumberOfRules<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>>();
			DemographicParityDifference<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>> function5ARC
			= new DemographicParityDifference<PittsburghSolution_Basic<MichiganSolution_Basic<Rule_Basic>>>();

			double Gmean_DtraARC = function1ARC.function(solutionARC, train);
			double Gmean_DtstARC= function1ARC.function(solutionARC, test);
			double FPR_DtraARC = function2ARC.function(solutionARC, train);
			double FPR_DtstARC = function2ARC.function(solutionARC, test);
			double PPV_DtraARC = function3ARC.function(solutionARC, train);
			double PPV_DtstARC = function3ARC.function(solutionARC, test);
			double ruleNumARC = function4ARC.function(solutionARC);
			double DP_DtraARC = function5ARC.function(solutionARC, train);
			double DP_DtstARC = function5ARC.function(solutionARC, test);

			double Gmean_traARC = 1-Gmean_DtraARC;
			double Gmean_tstARC = 1-Gmean_DtstARC;

            RuleLength<MichiganSolution_Basic<Rule_Basic>> RuleLengthFuncARC = new RuleLength<MichiganSolution_Basic<Rule_Basic>>();
            double TotalRuleLengthARC = 0;
            for (int j = 0; j < nonDominatedSolutionsARC.get(i).getNumberOfVariables(); j++) {
                 double RuleLengthARC = RuleLengthFuncARC.function(nonDominatedSolutionsARC.get(i).getVariable(j));
                 TotalRuleLengthARC += RuleLengthARC;
            }

            double TotalCoverARC = 0;
            for (int j = 0; j < nonDominatedSolutionsARC.get(i).getNumberOfVariables(); j++) {

            	 double CoverARC = 0;
            	 List<Double> supportARC = new ArrayList<Double>();

            	 for (int k = 0; k < train.getNdim(); k++) {
            		  if (nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) != 0) {

            			  if ((nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 1) ||
            				  (nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 2) ||
            				  (nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 4)){
            				   supportARC.add(1.0);
            			  }else if ((nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 3) ||
                				  (nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 5) ||
                				  (nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 11) ||
                				  (nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 12) ||
                				  (nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 13)){
                				   supportARC.add(1.0/2);
            			  }else if ((nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 6) ||
                				  (nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 9)){
                				   supportARC.add(1.0/3);
            			  }else if ((nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 7) ||
                				  (nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 8)){
           				   supportARC.add(2.0/3);
       			          }else if ((nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 10) ||
                				  (nonDominatedSolutionsARC.get(i).getVariable(j).getVariable(k) == 14)){
           				   supportARC.add(1.0/4);
       			          }
            		  }
            	 }
            	 if (!supportARC.isEmpty()) {
            	     CoverARC = supportARC.stream().reduce(1.0, (a, b) -> a * b);
                   	 TotalCoverARC += CoverARC;
                 }
            }

            double TotalRWARC = 0;
            for (int j = 0; j < nonDominatedSolutionsARC.get(i).getNumberOfVariables(); j++) {
                double RWARC = (Double) nonDominatedSolutionsARC.get(i).getVariable(j).getRuleWeight().getRuleWeightValue();
                TotalRWARC += RWARC;
            }
            double AveRWARC = TotalRWARC/(nonDominatedSolutionsARC.get(i).getNumberOfVariables());

	    	strARC = String.valueOf(i);
	    	strARC += "," + Gmean_traARC + "," + Gmean_tstARC + "," + FPR_DtraARC + "," + FPR_DtstARC + "," + PPV_DtraARC + "," + PPV_DtstARC + "," + ruleNumARC + "," + TotalRuleLengthARC + "," + TotalCoverARC + "," + AveRWARC + "," + DP_DtraARC + "," + DP_DtstARC;
	    	strsARC.add(strARC);
	    }
	    String fileNameARC = Consts.EXPERIMENT_ID_DIR + sep + "resultsARC.csv";
	    Output.writeln(fileNameARC, strsARC, false);

		return;
	}
}
