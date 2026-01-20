package cilabo.gbml.problem.pittsburghFGBML_Problem;

import org.uma.jmetal.problem.AbstractGenericProblem;
import org.uma.jmetal.problem.Problem;

import cilabo.data.DataSet;
import cilabo.fuzzy.classifier.Classifier;
import cilabo.gbml.objectivefunction.pittsburgh.fairness.IndividualDiscriminationRate;
import cilabo.gbml.solution.michiganSolution.MichiganSolution;
import cilabo.gbml.solution.michiganSolution.MichiganSolution.MichiganSolutionBuilder;
import cilabo.gbml.solution.pittsburghSolution.PittsburghSolution;
import cilabo.gbml.solution.pittsburghSolution.impl.PittsburghSolution_Basic;
import cilabo.gbml.solution.util.attribute.NumberOfWinner;

public abstract class AbstractPittsburghFGBML <pittsburghSolutionObject extends PittsburghSolution<michiganSolution>,
		michiganSolution extends MichiganSolution<?>>
		extends AbstractGenericProblem <pittsburghSolutionObject> implements Problem<pittsburghSolutionObject>{

	protected DataSet<?> train;
	protected MichiganSolutionBuilder<michiganSolution> michiganSolutionBuilder;

	/** 識別方式
	 * @see cilabo.fuzzy.classifier.classification */
	protected Classifier<michiganSolution> classifier;

	/** Constructor */
	public AbstractPittsburghFGBML(
			int numberOfVariables,
			int numberOfObjectives,
			int numberOfConstraints,
			DataSet<?> train,
			MichiganSolutionBuilder<michiganSolution> michiganSolutionBuilder,
			Classifier<michiganSolution> classifier) {
		setNumberOfVariables(numberOfVariables);
		setNumberOfObjectives(numberOfObjectives);
		setNumberOfConstraints(numberOfConstraints);
		this.train = train;
		this.michiganSolutionBuilder = michiganSolutionBuilder;
		this.classifier = classifier;
	}


	public void removeNoWinnerMichiganSolution(PittsburghSolution<michiganSolution> solution) {
		if(solution.getNumberOfVariables() == 0) {throw new ArithmeticException("This PittsburghSolution has no michiganSolution");}
		for(int i=0; i<solution.getNumberOfVariables(); i++) {
			if((int)solution.getVariable(i).getAttribute((new NumberOfWinner<PittsburghSolution<michiganSolution>>()).getAttributeId()) < 1) {
				solution.removeVariable(i); i--;
			}
		}
		/*ルール数が第2目的でない場合はコメントアウト*/
		/*NumberOfRules<PittsburghSolution<michiganSolution>> function2 = new NumberOfRules<PittsburghSolution<michiganSolution>>();
		double f2 = function2.function(solution);
		solution.setObjective(OBJECTIVES_FOR_PITTSBURGH.NumberOfRule.toInt(), f2);*/

		/*IDRが第2目的でない場合はコメントアウト*/
		/*IndividualDiscriminationRate<PittsburghSolution<michiganSolution>> function2 = new IndividualDiscriminationRate<PittsburghSolution<michiganSolution>>();
		double f2 = function2.function(solution, train);
		solution.setObjective(1, f2);*/

		if(solution.getNumberOfVariables() == 0) {
			throw new ArithmeticException("PittsburghSolution has no winner michiganSolution");
		}
	}

	public DataSet<?> getTrain() {
		return train;
	}

	public MichiganSolutionBuilder<michiganSolution> getMichiganSolutionBuilder() {
		return michiganSolutionBuilder;
	}

	public Classifier<michiganSolution> getClassifier() {
		return classifier;
	}
}
