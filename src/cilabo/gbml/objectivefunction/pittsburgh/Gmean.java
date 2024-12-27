/**
 *
 */
package cilabo.gbml.objectivefunction.pittsburgh;

import java.util.ArrayList;

import cilabo.data.DataSet;
//import cilabo.data.InputVector;
import cilabo.data.pattern.Pattern;
import cilabo.fuzzy.rule.consequent.classLabel.ClassLabel;
import cilabo.gbml.solution.michiganSolution.MichiganSolution;
//import cilabo.gbml.solution.michiganSolution.impl.MichiganSolution_Rejected;
import cilabo.gbml.solution.pittsburghSolution.PittsburghSolution;
import cilabo.gbml.solution.util.attribute.ErroredPatternsAttribute;
import cilabo.gbml.solution.util.attribute.NumberOfClassifierPatterns;
import cilabo.gbml.solution.util.attribute.NumberOfWinner;

/**
 * @author Takeru Konishi
 */
public final class Gmean <S extends PittsburghSolution<?>>{

	public Gmean() {}

	/**
	 * 2クラス問題を前提
	 * @param solution
	 * @param dataset
	 * @return double
	 */
	public double function(S solution, DataSet<?> dataset) {

		String attributeId = new NumberOfWinner<S>().getAttributeId();
		String attributeIdFitness = new NumberOfClassifierPatterns<S>().getAttributeId();
		for(MichiganSolution<?> michiganSolution: solution.getVariables()) {
			michiganSolution.setAttribute(attributeId, 0);
			michiganSolution.setAttribute(attributeIdFitness, 0);
		}

		ArrayList<Pattern<?>> erroredPatterns = new ArrayList<Pattern<?>>();

		double[] sizeForClass = new double[2];

		double[] correctForClass = new double[2];

		for(int p = 0; p < dataset.getDataSize(); p++) {

			Pattern<?> pattern = dataset.getPattern(p);

			//InputVector vector = pattern.getInputVector();

			ClassLabel<Integer> trueClass = (ClassLabel<Integer>) pattern.getTargetClass();

			MichiganSolution<?> winnerSolution = solution.classify(pattern);

			// If output is rejected then continue next pattern.
			if(winnerSolution == null) {
				erroredPatterns.add(pattern);
				sizeForClass[(int) trueClass.getClassLabelValue()]++;
				continue;
			}else{
				int buf = (int) winnerSolution.getAttribute(attributeId);
				winnerSolution.setAttribute(attributeId, buf+1);

				sizeForClass[(int) trueClass.getClassLabelValue()]++;

				ClassLabel<Integer> classifiedClass = (ClassLabel<Integer>) winnerSolution.getClassLabel();

				if(trueClass.equalsClassLabel(classifiedClass)) {
					int buf2 = (int) winnerSolution.getAttribute(attributeIdFitness);
					winnerSolution.setAttribute(attributeIdFitness, buf2+1);

					correctForClass[(int) classifiedClass.getClassLabelValue()]++;
				}else {
				   erroredPatterns.add(pattern);
				}
			}
		}

		solution.setAttribute(new ErroredPatternsAttribute<S>().getAttributeId(), erroredPatterns);

		// 各クラスの正解率を計算
		double[] P = new double[2];
		for(int i = 0; i < P.length; i++) {
			P[i] = correctForClass[i] / sizeForClass[i];
		}

		double gmean = Math.sqrt(P[0] * P[1]);
		return gmean;
	}
}
