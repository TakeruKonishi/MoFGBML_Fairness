package cilabo.main;

public class ExperienceParameter {

	public static CLASS_LABEL classlabel = CLASS_LABEL.Fairness; //クラスラベル

	/**データセットのクラスラベル方式 */
	public static enum CLASS_LABEL{
		Single, //単一のクラスラベル
		Multi, //複数のクラスラベル
        Fairness //Fairnessデータセットを用いる場合
	}

	public static enum DIVISION_TYPE{
		equalDivision,
		entropyDivision
	}

	public static enum SHAPE_TYPE_NAME{
		gaussian,
		trapezoid,
		interval,
		triangle
	}

	public static enum OBJECTIVES_FOR_MICHIGAN{
		FitnessValue(0),
		RuleLength(1);

		private final int objectivesIndexForMichigan;
		private OBJECTIVES_FOR_MICHIGAN(int objectivesIndexForMichigan) {
			this.objectivesIndexForMichigan = objectivesIndexForMichigan;
		}
		public int toInt() {return  objectivesIndexForMichigan;}
	}

	public static enum OBJECTIVES_FOR_PITTSBURGH{
		ErrorRateDtra(0),
		NumberOfRule(1),
		ErrorRateDtst(2);

		private final int objectivesIndexForPittsburgh;
		private OBJECTIVES_FOR_PITTSBURGH(int objectivesForPittsburgh) {
			this.objectivesIndexForPittsburgh = objectivesForPittsburgh;
		}
		public int toInt() {return  objectivesIndexForPittsburgh;}
	}
}
