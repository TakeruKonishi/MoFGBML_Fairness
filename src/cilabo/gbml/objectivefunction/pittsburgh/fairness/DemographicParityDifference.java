/**
 *
 */
package cilabo.gbml.objectivefunction.pittsburgh.fairness;

import cilabo.data.DataSet;
import cilabo.fuzzy.rule.consequent.classLabel.ClassLabel;
import cilabo.gbml.solution.michiganSolution.MichiganSolution;
import cilabo.gbml.solution.pittsburghSolution.PittsburghSolution;
import cilabo.labo.developing.fairness.FairnessPattern;

/**
 * @author Takeru Konishi
 *
 */
public final class DemographicParityDifference <S extends PittsburghSolution<?>>{

	public DemographicParityDifference() {}

	/**
	 * @param solution
	 * @param dataset
	 * @return double
	 */
	public double function(S solution, DataSet<?> dataset) {

		double[] sizeForSensitive = new double[2];  // 各グループのサンプル数
		double[] countForSensitive = new double[2]; // 各グループで陽性予測されたサンプル数

		for(int p = 0; p < dataset.getDataSize(); p++) {

			FairnessPattern pattern = (FairnessPattern)dataset.getPattern(p);
			ClassLabel<Integer> trueClass = pattern.getTargetClass();

			MichiganSolution<?> winnerSolution = solution.classify(pattern);

			// rejectedならば次のパターン
			if(winnerSolution == null) {
				continue;
			}

			// Classification
			ClassLabel<Integer> classifiedClass = (ClassLabel<Integer>) winnerSolution.getClassLabel();

			// Sensitive attribute value
			int a = pattern.getA();

			// 分母（グループごとの総サンプル数）
			sizeForSensitive[a]++;

			// 分子（陽性と分類されたサンプル数）
			if((int)classifiedClass.getClassLabelValue() == 1) {
				countForSensitive[a]++;
			}
		}

		double[] P_a = new double[2];
		for(int i = 0; i < P_a.length; i++) {
			// 分母が 0 にならないように処理
			if(sizeForSensitive[i] <= 0) {
				if(countForSensitive[i] <= 0) {
					P_a[i] = 1;
				}
				else {
					P_a[i] = 2;
				}
			}
			else {
				P_a[i] = countForSensitive[i] / sizeForSensitive[i];
			}
		}

		// Demographic Parity の計算
		double DP_diff = Math.abs(P_a[0] - P_a[1]);
		return DP_diff;
	}
}
