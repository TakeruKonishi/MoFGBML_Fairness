package cilabo.gbml.objectivefunction.pittsburgh.fairness;

import java.util.Arrays;

import cilabo.data.AttributeVector;
import cilabo.data.DataSet;
import cilabo.fuzzy.rule.consequent.classLabel.ClassLabel;
import cilabo.gbml.solution.michiganSolution.MichiganSolution;
import cilabo.gbml.solution.pittsburghSolution.PittsburghSolution;
import cilabo.labo.developing.fairness.FairnessPattern;
import cilabo.labo.developing.fairness.CommandLineArgs;

/**
 * Individual Discrimination Rate:
 *   proportion of samples whose prediction changes
 *   when only the sensitive attribute is flipped.
 */
public final class IndividualDiscrimination<S extends PittsburghSolution<?>> {

    public IndividualDiscrimination() {}

    /**
     * @param solution 評価対象の Pittsburgh 個体
     * @param dataset  評価用の DataSet (FairnessPattern を想定)
     * @return 予測が変化したサンプル割合 (0.0～1.0)
     */
    public double function(S solution, DataSet<?> dataset) {

        if (dataset.getDataSize() == 0) {
            return Double.NaN;
        }

        // コマンドラインで指定したセンシティブ属性インデックス
        final int sIndex = CommandLineArgs.sensitiveIndex;
        if (sIndex < 0) {
            throw new IllegalStateException(
                "Sensitive attribute index must be non-negative.");
        }

        double violationCount = 0.0;
        double validCount     = 0.0;

        final int ABSTAIN = -1;

        for (int p = 0; p < dataset.getDataSize(); p++) {

            FairnessPattern pattern = (FairnessPattern) dataset.getPattern(p);

            // === 元の入力での予測 ===
            MichiganSolution<?> winnerOrig = solution.classify(pattern);

            int yOrig = ABSTAIN;
            if (winnerOrig != null) {
                @SuppressWarnings("unchecked")
                ClassLabel<Integer> classOrig =
                    (ClassLabel<Integer>) winnerOrig.getClassLabel();
                yOrig = classOrig.getClassLabelValue();
            }


            // === 反事実入力の構築 ===

            double[] origAttrs = pattern.getAttributeArray();
            if (sIndex >= origAttrs.length) {
                throw new IllegalStateException(
                    "Sensitive index " + sIndex +
                    " is out of bounds for attribute vector length " +
                    origAttrs.length);
            }

            // 深いコピー：origAttrs をそのまま書き換えないように clone する
            double[] cfAttrs = Arrays.copyOf(origAttrs, origAttrs.length);

            // センシティブ属性の値を反転（-1/-2 コードを想定）
            if (cfAttrs[sIndex] == -1.0) {
                cfAttrs[sIndex] = -2.0;
            } else if (cfAttrs[sIndex] == -2.0) {
                cfAttrs[sIndex] = -1.0;
            } else {
                // 想定外の値はデータ前処理の不整合として例外
                throw new IllegalStateException(
                    "Sensitive attribute at index " + sIndex +
                    " must be coded as -1.0 or -2.0, but found: " +
                    cfAttrs[sIndex]);
            }

            // AttributeVector はコンストラクタ内でさらに Arrays.copyOf するので，
            // cfAttrs との間にも参照の共有は発生しない
            AttributeVector cfAttrVec = new AttributeVector(cfAttrs);

            // グループラベル a も概念的には反転した方が自然だが，
            // 本指標の計算では使用していないのでどちらでもよい．
            // ここでは 0/1 で反転させておく：
            int aOrig = pattern.getA();
            int aCf   = (aOrig == 0) ? 1 : (aOrig == 1 ? 0 : aOrig);

            FairnessPattern cfPattern = new FairnessPattern(
                pattern.getID(),      // 任意（同じ ID でも問題なし）
                cfAttrVec,
                pattern.getTargetClass(), // 真のクラスは同じ
                aCf
            );

            // === 反事実入力での予測 ===
            MichiganSolution<?> winnerCF = solution.classify(cfPattern);

            int yCF = ABSTAIN;
            if (winnerCF != null) {
                @SuppressWarnings("unchecked")
                ClassLabel<Integer> classCF =
                    (ClassLabel<Integer>) winnerCF.getClassLabel();
                yCF = classCF.getClassLabelValue();
            }

            // 有効なサンプルとしてカウント
            validCount += 1.0;
            if (yOrig != yCF) {
                violationCount += 1.0;
            }
        }

        if (validCount == 0.0) {
            return Double.NaN;
        }

        // 「予測が変わった割合」を返す（値が小さいほど望ましい）
        return violationCount / validCount;
    }
}
