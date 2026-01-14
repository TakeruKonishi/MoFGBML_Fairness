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
public final class EqualOpportunityDifference <S extends PittsburghSolution<?>>{

	public EqualOpportunityDifference() {}

	/**
	 * @param solution
	 * @param dataset
	 * @return double
	 */
	public double function(S solution, DataSet<?> dataset) {

		double[] sizeForSensitive = new double[2];  // 各グループで実際に 1 であるサンプル数
		double[] countForSensitive = new double[2]; // 各グループで正しく 1 と識別されたサンプル数 (True Positives)

		for(int p = 0; p < dataset.getDataSize(); p++) {

			FairnessPattern pattern = (FairnessPattern)dataset.getPattern(p);
			ClassLabel<Integer> trueClass = pattern.getTargetClass();

			// Sensitive attribute value
			int a = pattern.getA();

			// 分母（本来 1 であるサンプル数）
			if((int)trueClass.getClassLabelValue() == 1) {
				sizeForSensitive[a]++;
			}

			MichiganSolution<?> winnerSolution = solution.classify(pattern);

			// rejectedならば次のパターン
			if(winnerSolution == null) {
				continue;
			}

			// Classification
			ClassLabel<Integer> classifiedClass = (ClassLabel<Integer>) winnerSolution.getClassLabel();

			// 分子（真に 1 かつ 1 と識別されたサンプル数）
			if((int)trueClass.getClassLabelValue() == 1 && (int)classifiedClass.getClassLabelValue() == 1) {
				countForSensitive[a]++;
			}

		}

		double[] TPR_a = new double[2];
		for(int i = 0; i < TPR_a.length; i++) {
			// 分母 0 は定義不能：NaN を返して上位で扱う
			if(sizeForSensitive[i] <= 0) {
				return Double.NaN;
			}
			TPR_a[i] = countForSensitive[i] / sizeForSensitive[i];
		}

		// Equal Opportunity の計算
		double EOpp_diff = Math.abs(TPR_a[0] - TPR_a[1]);
		return EOpp_diff;
	}
}