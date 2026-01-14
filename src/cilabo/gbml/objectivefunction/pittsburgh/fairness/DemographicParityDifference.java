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

			MichiganSolution<?> winnerSolution = solution.classify(pattern);

			// Sensitive attribute value
			int a = pattern.getA();

			// 分母（グループごとの総サンプル数）
			sizeForSensitive[a]++;

			// rejectedならば次のパターン
			if(winnerSolution == null) {
				continue;
			}

			// Classification
			ClassLabel<Integer> classifiedClass = (ClassLabel<Integer>) winnerSolution.getClassLabel();

			// 分子（陽性と分類されたサンプル数）
			if((int)classifiedClass.getClassLabelValue() == 1) {
				countForSensitive[a]++;
			}
		}

		double[] P_a = new double[2];
		for(int i = 0; i < P_a.length; i++) {
			// 分母 0 は定義不能：NaN を返して上位で扱う
		    if (sizeForSensitive[i] <= 0) {
		        return Double.NaN; 
		    }
		    P_a[i] = countForSensitive[i] / sizeForSensitive[i];
		}

		// Demographic Parity の計算
		double DP_diff = Math.abs(P_a[0] - P_a[1]);
		return DP_diff;
	}
}
