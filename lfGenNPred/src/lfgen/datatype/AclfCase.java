package lfgen.datatype;

import org.apache.commons.math3.complex.Complex;

import lfgen.algo.impl.LoadFlowGenerator;

/**
* @author JeremyChenlk
* @version 2019��2��10�� ����10:48:07
*
* Class description:
*	������
*/

public class AclfCase {
	
	/**
	 * ���ڵ�ѹ�����仯֮����Ҫ����powerʱ���Զ�����power
	 */
	private static LoadFlowGenerator lfg = null;
	
	/**
	 * ����״̬
	 * 0 - �µ�V��PV�Բ���
	 * 1 - V���䣬PV�Ե���
	 */
	private boolean coincide = false;
	
	private Complex[] voltage = null;
	private Complex[] power = null;

	public AclfCase(Complex[] voltage) {
		this.voltage = voltage;
		this.power = new Complex[voltage.length];//Ӧ��ע���ʱ�ǿյ�
		this.coincide = false;
	}

	public boolean getCoincide() {
		return coincide;
	}

	public Complex[] getVoltage() {
		return voltage;
	}

	public void setVoltage(Complex[] voltage) {
		this.voltage = voltage;
		this.coincide = false;
	}

	public Complex[] getPower() {
		if (!this.coincide) {
			power = lfg.genFlow(voltage);
			coincide = true;
		}
		return power;
	}
	
	@Override
	public String toString() {
		String str = new String("");
		String[] busCode = lfg.getRefInfo().getBusCode();
		for (int i=0; i<voltage.length; ++i) {
			str += "Bus "+i+"\t"+busCode[i]+"\t";
			if (busCode[i].equals("Swing")) {
				str += getVoltage()[i];
			}else if (busCode[i].equals("GenPV")) {
				str += getPower()[i].getReal()+"\t"+getVoltage()[i].abs();
			}else if (busCode[i].equals("GenPQ")) {
				str += getPower()[i];
			}
			str += "\n";
		}
		return str;
	}
	
	/**
	 * �ڳ����ʼ���׶θ������ֵ��֮������ʽ����lfg
	 * @param lfg
	 */
	public static void init(LoadFlowGenerator lfg) {
		AclfCase.lfg = lfg;
	}
	
}
