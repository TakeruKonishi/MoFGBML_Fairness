package cilabo.fuzzy.rule.antecedent.impl;

import java.util.Arrays;

import org.w3c.dom.Element;

import cilabo.data.AttributeVector;
import cilabo.fuzzy.rule.antecedent.AbstractAntecedent;
import xml.XML_TagName;
import xml.XML_manager;

/**
 * Antecedent実装クラス
 * @author Takigawa Hiroki
 */
public final class Antecedent_Basic extends AbstractAntecedent{

	@Override
	public double[] getCompatibleGrade(int[] antecedentIndex, AttributeVector attributeVector) {
		double[] grade = new double[antecedentIndex.length];
		/*german*/
		/*int[] count_negative = {4, 0, 5, 10, 0, 5, 5, 0, 4, 3, 0, 4, 0, 3, 3, 0, 4, 0, 2, 2};*/
		/*propublica-recidivism*/
		/*int[] count_negative = {2,0,3,6,0,0,0,0,2,437,0,3};*/
		/*adult*/
		/*int[] count_negative = {0,7,0,16,0,7,14,6,5,2,0,0,0,41};*/
		if(antecedentIndex.length != attributeVector.getNumberOfDimension()) {
			throw new IllegalArgumentException("antecedentIndex and pattern must be same length");
		}else {
			for(int i = 0; i < attributeVector.getNumberOfDimension(); i++) {
				if(antecedentIndex[i] < 0 && attributeVector.getAttributeValue(i) < 0) {
					// categorical
					if(antecedentIndex[i] == (int)attributeVector.getAttributeValue(i)) grade[i] = 1.0;
					else grade[i] = 0.0;
					/*else grade[i] = 0.1;*/
					/*else grade[i] = 1.0/(antecedentIndex.length);*/
					/*else grade[i] = 1.0/count_negative[i];*/
				}else if(antecedentIndex[i] > 0 && attributeVector.getAttributeValue(i) >= 0){
					// numerical
					grade[i] = this.getFuzzySet(i, antecedentIndex[i]).getMembershipValue((float)attributeVector.getAttributeValue(i));
				}else if(antecedentIndex[i] == 0) {
					//don't care
					grade[i] = 1.0;
				}else {
					throw new IllegalArgumentException();
				}
			}
		}
		return grade;
	}

	@Override
	public double getCompatibleGradeValue(int[] antecedentIndex, AttributeVector attributeVector) {
		if(antecedentIndex.length != attributeVector.getNumberOfDimension()) {
			throw new IllegalArgumentException("antecedentIndex and pattern must be same length");
		}

		double[] buf = this.getCompatibleGrade(antecedentIndex, attributeVector);
		double grade = Arrays.stream(buf).reduce(1, (multi, i) -> multi*i);
		return grade;
	}

	@Override
	public int getRuleLength(int[] antecedentIndex) {
		int length = 0;
		for(int i = 0; i < antecedentIndex.length; i++) {
			if(antecedentIndex[i] != 0) {length++; }
		}
		return length;
	}

	@Override
	public Antecedent_Basic copy() {
		return new Antecedent_Basic();
	}

	@Override
	public Element toElement() {
		Element antecedent = XML_manager.getInstance().createElement(XML_TagName.antecedent);
		return antecedent;
	}
}
