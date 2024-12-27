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

			//InputVector vector = pattern.getInputVector();

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

            //分母
            if((int)trueClass.getClassLabelValue() == 0) {
                sizeForSensitive[a]++;
                //分子
                if((int)classifiedClass.getClassLabelValue() == 1) {
                    countForSensitive[a]++;
                }
            }
		}

		double[] P_a = new double[2];
		for(int i = 0; i < P_a.length; i++) {
			//TODO 分母が0にならないように処理（2で埋めて良いかは要検討）(大きなデータセットなら大丈夫だと思うが小さなデータセットで結果が完全に偏っているときは要相談）
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

		double FPR_diff = Math.abs(P_a[0] - P_a[1]);
		return FPR_diff;
	}
}