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
public final class EqualOpportunityDifference <S extends PittsburghSolution<?>>{

	public EqualOpportunityDifference() {}

	/**
	 * @param solution
	 * @param dataset
	 * @return double
	 */
	public double function(S solution, DataSet<?> dataset) {

		double[] sizeForSensitive = new double[2];  // 各グループで実際に 1 であるサンプル数
		double[] countForSensitive = new double[2]; // 各グループで正しく 1 と分類されたサンプル数 (True Positives)

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

			// 分母（本来 1 であるサンプル数）
			if((int)trueClass.getClassLabelValue() == 1) {
				sizeForSensitive[a]++;
				// 分子（正しく 1 と分類されたサンプル数）
				if((int)classifiedClass.getClassLabelValue() == 1) {
					countForSensitive[a]++;
				}
			}
		}

		double[] TPR_a = new double[2];
		for(int i = 0; i < TPR_a.length; i++) {
			// 分母が 0 にならないように処理
			if(sizeForSensitive[i] <= 0) {
				if(countForSensitive[i] <= 0) {
					TPR_a[i] = 1;
				}
				else {
					TPR_a[i] = 2;
				}
			}
			else {
				TPR_a[i] = countForSensitive[i] / sizeForSensitive[i];
			}
		}

		// Equal Opportunity の計算
		double EOpp_diff = Math.abs(TPR_a[0] - TPR_a[1]);
		return EOpp_diff;
	}
}
