/**
 *
 */
package cilabo.gbml.objectivefunction.pittsburgh;

import cilabo.data.DataSet;
import cilabo.data.pattern.Pattern;
import cilabo.fuzzy.rule.consequent.classLabel.ClassLabel;
import cilabo.gbml.solution.michiganSolution.MichiganSolution;
import cilabo.gbml.solution.pittsburghSolution.PittsburghSolution;

/**
 * G-mean evaluation function (No side effects).
 * - Assumes binary classification.
 * - Rejected (winnerSolution == null) is treated as misclassification.
 * - Does not write any attributes to solutions/rules.
 *
 * Side-effect-free version of: cilabo.gbml.objectivefunction.pittsburgh.Gmean
 *
 * @author Takeru Konishi
 *
 * @param <S>
 */
public final class GmeanNoSideEffect<S extends PittsburghSolution<?>> {

    public GmeanNoSideEffect() {}

    /**
     * 2-class assumption
     * @param solution
     * @param dataset
     * @return double
     */
    public double function(S solution, DataSet<?> dataset) {

        double[] sizeForClass = new double[2];
        double[] correctForClass = new double[2];

        for (int p = 0; p < dataset.getDataSize(); p++) {

            Pattern<?> pattern = dataset.getPattern(p);

            ClassLabel<Integer> trueClass = (ClassLabel<Integer>) pattern.getTargetClass();

            MichiganSolution<?> winnerSolution = solution.classify(pattern);

            // If output is rejected then count it as an error (but still count the true class size)
            if (winnerSolution == null) {
                sizeForClass[(int) trueClass.getClassLabelValue()]++;
                continue;
            } else {
                sizeForClass[(int) trueClass.getClassLabelValue()]++;

                ClassLabel<Integer> classifiedClass = (ClassLabel<Integer>) winnerSolution.getClassLabel();

                if (trueClass.equalsClassLabel(classifiedClass)) {
                    correctForClass[(int) classifiedClass.getClassLabelValue()]++;
                }
            }
        }

        // Per-class accuracy
        double[] P = new double[2];
        for (int i = 0; i < P.length; i++) {
            P[i] = correctForClass[i] / sizeForClass[i];
        }

		double gmean = Math.sqrt(P[0] * P[1]);
		return gmean;
    }
}
