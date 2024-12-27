package cilabo.labo.developing.fairness;

import cilabo.data.AttributeVector;
//import cilabo.data.InputVector;
import cilabo.data.pattern.Pattern;
import cilabo.fuzzy.rule.consequent.classLabel.impl.ClassLabel_Basic;

/**
 *
 */

public final class FairnessPattern extends Pattern <ClassLabel_Basic> {
	// ************************************************************
	/**
	 * sensitive attribute
	 */
	int a;

	// ************************************************************
	public FairnessPattern(int id, AttributeVector attributeVector, ClassLabel_Basic targetClass, int a) {
		super(id, attributeVector, targetClass);
		this.a = a;
	}


	// ************************************************************

	public int getA() {
		return this.a;
	}

	@Override
	public String toString() {
		if(this.attributeVector == null || this.targetClass == null) {
			return "null";
		}

		String str = String.format("[id:%d, input:{%s}, Class:%s, sensitiveAttribute:%d]", this.id, this.attributeVector.toString(), this.targetClass.toString(), this.a);

		return str;
	}
}
