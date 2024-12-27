package cilabo.labo.developing.fairness;

import java.util.List;

import org.uma.jmetal.problem.Problem;

import cilabo.data.DataSet;
import cilabo.fuzzy.classifier.Classifier;
import cilabo.gbml.objectivefunction.pittsburgh.Gmean;
import cilabo.gbml.objectivefunction.pittsburgh.NumberOfRules;
import cilabo.gbml.objectivefunction.pittsburgh.fairness.PositivePredictiveValuesDifference;
import cilabo.gbml.problem.pittsburghFGBML_Problem.AbstractPittsburghFGBML;
import cilabo.gbml.solution.michiganSolution.MichiganSolution;
import cilabo.gbml.solution.michiganSolution.MichiganSolution.MichiganSolutionBuilder;
//import cilabo.gbml.solution.michiganSolution.impl.MichiganSolution_Rejected;
import cilabo.gbml.solution.pittsburghSolution.impl.PittsburghSolution_Basic;

/**
 * cilabo.gbml.problem.pittsburghFGBML_Problem.impl.PittsburghFGBML_Basic; を参考に作成
 *
 */
public class MOP5_fairness<michiganSolution extends MichiganSolution<?>>
        extends AbstractPittsburghFGBML<PittsburghSolution_Basic<michiganSolution>, michiganSolution> implements Problem<PittsburghSolution_Basic<michiganSolution>> {

       public MOP5_fairness(
              int numberOfVariables,
              int numberOfObjectives,
              int numberOfConstraints,
              DataSet<?> train,
              MichiganSolutionBuilder<michiganSolution> michiganSolutionBuilder,
              Classifier<michiganSolution> classifier) {
              super(numberOfVariables, numberOfObjectives, numberOfConstraints,
                	  train, michiganSolutionBuilder, classifier);
              this.setName("MOP5");
            }

   	@Override
   	public void evaluate(PittsburghSolution_Basic<michiganSolution> solution) {
   		/* The first objective */
   		Gmean<PittsburghSolution_Basic<michiganSolution>> function1 = new Gmean<PittsburghSolution_Basic<michiganSolution>>();
   		double f1 = function1.function(solution, train);
   		/* The second objective */
   		NumberOfRules<PittsburghSolution_Basic<michiganSolution>> function2 = new NumberOfRules<PittsburghSolution_Basic<michiganSolution>>();
   		double f2 = function2.function(solution);
		/* The Third objective */
		PositivePredictiveValuesDifference<PittsburghSolution_Basic<michiganSolution>> function3 = new PositivePredictiveValuesDifference<PittsburghSolution_Basic<michiganSolution>>();
		double f3 = function3.function(solution, train);

		// Gmean is a maximization objective: Calculate 1-Gmean and convert to minimization
		solution.setObjective(0, 1.0-f1);
   		solution.setObjective(1, f2);
   		solution.setObjective(2, f3);
   	}

	/*public void removeNoWinnerMichiganSolution(PittsburghSolution_Basic<michiganSolution> solution) {
		for(int i=0; i<solution.getNumberOfVariables(); i++) {
			if((int)solution.getVariable(i).getAttribute((new NumberOfWinner()).getAttributeId()) < 1) {
				solution.removeVariable(i); i--;
			}
		}
		//ルール数がゼロになった場合，ダミールールで満たした個体で置換する
		if(solution.getNumberOfVariables() == 0) {
			solution.clearVariable(this.getNumberOfVariables());
			for(int i=0; i<this.getNumberOfVariables(); i++) {
				solution.setVariable(i, (michiganSolution) MichiganSolution_Rejected.getInstance());
			}
			solution.setObjective(0, 1);
			solution.setObjective(1, this.getNumberOfVariables());
			solution.setObjective(2, 1);
//			throw new ArithmeticException("This PittsburghSolution has no michiganSolution");
		}
	}*/

	@Override
	public PittsburghSolution_Basic<michiganSolution> createSolution() {
		PittsburghSolution_Basic<michiganSolution> pittsburghSolution = new PittsburghSolution_Basic<michiganSolution>(
				this.getNumberOfVariables(),
				this.getNumberOfObjectives(),
				this.getNumberOfConstraints(),
				this.michiganSolutionBuilder.copy(),
				this.classifier.copy());

		List<michiganSolution> solutionArray = this.michiganSolutionBuilder.createMichiganSolutions(this.getNumberOfVariables());
		for(int i=0; i<this.getNumberOfVariables(); i++) {
			pittsburghSolution.setVariable(i, solutionArray.get(i));
		}
		return pittsburghSolution;
	}

	@Override
	public String toString() {
		return "MOP5_fairness [michiganSolutionBuilder=" + michiganSolutionBuilder
				+ ", classifier=" + classifier + "]";
	}

}