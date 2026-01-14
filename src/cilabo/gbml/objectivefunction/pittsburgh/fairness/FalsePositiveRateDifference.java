/**
 *
 */
package cilabo.gbml.objectivefunction.pittsburgh.fairness;

import cilabo.data.DataSet;
//import cilabo.data.InputVector;
import cilabo.fuzzy.rule.consequent.classLabel.ClassLabel;
import cilabo.gbml.solution.michiganSolution.MichiganSolution;
//import cilabo.gbml.solution.michiganSolution.impl.MichiganSolution_Rejected;
import cilabo.gbml.solution.pittsburghSolution.PittsburghSolution;
import cilabo.labo.developing.fairness.FairnessPattern;

/**
 * @author Takeru Konishi
 *
 */
public final class FalsePositiveRateDifference <S extends PittsburghSolution<?>>{

	public FalsePositiveRateDifference() {}

	/**
	 * @param solution
	 * @param dataset
	 * @return double
	 */
	public double function(S solution, DataSet<?> dataset) {

		double[] sizeForSensitive = new double[2];
		double[] countForSensitive = new double[2];

		for(int p = 0; p < dataset.getDataSize(); p++) {

			FairnessPattern pattern = (FairnessPattern)dataset.getPattern(p);
			ClassLabel<Integer> trueClass = pattern.getTargetClass();

            // Sensitive attribute value
            int a = pattern.getA();

			// 分母（本来 0 であるサンプル数）
			if((int)trueClass.getClassLabelValue() == 0) {
				sizeForSensitive[a]++;
			}

			MichiganSolution<?> winnerSolution = solution.classify(pattern);

			// rejectedならば次のパターン
			if(winnerSolution == null) {
				continue;
			}

			// Classification
			ClassLabel<Integer> classifiedClass = (ClassLabel<Integer>) winnerSolution.getClassLabel();

			// 分子（真に 0 かつ 1 と識別されたサンプル数）
			if((int)trueClass.getClassLabelValue() == 0 && (int)classifiedClass.getClassLabelValue() == 1) {
				countForSensitive[a]++;
			}
		}

		double[] P_a = new double[2];
		for(int i = 0; i < P_a.length; i++) {
			// 分母 0 は定義不能：NaN を返して上位で扱う
			if(sizeForSensitive[i] <= 0) {
				return Double.NaN;
			}
			P_a[i] = countForSensitive[i] / sizeForSensitive[i];
		}

		double FPR_diff = Math.abs(P_a[0] - P_a[1]);
		return FPR_diff;
	}
}