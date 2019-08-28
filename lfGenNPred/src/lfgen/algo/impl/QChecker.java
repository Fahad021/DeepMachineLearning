package lfgen.algo.impl;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.emf.common.util.EList;
import org.interpss.numeric.exp.IpssNumericException;
import org.interpss.numeric.sparse.ISparseEqnComplex;
import org.interpss.numeric.sparse.ISparseEqnDouble;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;

import lfgen.algo.IQChecker;
import lfgen.datatype.AclfCase;
import lfgen.datatype.RefInfo;

/**
* @author JeremyChenlk
* @version 2019��1��24�� ����10:09:06
*
* Class description:
*	���ڵ��޹��Ƿ�Խ�޵ķ��������Խ��Ӧ���ṩ����
*	����B11��һ��
*/

public class QChecker extends AlgoObject implements IQChecker {

	public static final String NAME = "Default_QChecker";
	public static final String NAME_IN_SHORT = "dQC";
	public static final String PARA_NEEDED = "qLimitSparseFactor";
	
	protected static final double DEFAULT_Q_SPARSE = 0.95;

	/**
	 * ����ʵ�������У�������޵Ļ�������΢�����г������������������Ҫ���ɳ�����
	 */
	protected double qLimitSparseFactor = 0;
	
	/*
	 * ��ref�õ���ָ�룬�Ǳ��������úͱ���ֵ�ظ�����Э
	 */
	protected int noBus = 0;
	protected double[][] b11r = null;
	protected int swingNo = 0;
	protected String[] busCode = null;
	protected int[] busType = null;
	protected double[] maxQGenLimit = null;
	protected double[] minQGenLimit = null;
	
	public QChecker(RefInfo refInfo) {
		super(refInfo);
		long startTime = System.currentTimeMillis();
		this.methodName = "Default_QChecker";
		System.out.print("[REPORT] new "+methodName+"...");
		
		this.qLimitSparseFactor = QChecker.DEFAULT_Q_SPARSE;
		
		noBus = ref.getNoBus();
		maxQGenLimit = ref.getMaxQGenLimit();
		minQGenLimit = ref.getMinQGenLimit();
		b11r = ref.getB11r();
		busCode = ref.getBusCode();
		busType = ref.getBusType();
		swingNo = ref.getSwingNo();
		
		//report
		System.out.println(" ...ready. Need to input parameter: "+PARA_NEEDED);
		addInitTime(startTime);
	}
	
	/**
	 * ������Խ�޵����ʱ���õķ�����Ӧ��PQ�ֽⷨ����B����������ѹ������
	 * ֻ�޸�busCode==GenPV�Ľڵ�
	 * ����LFTest()��ȷ�ϣ�min/maxQGenLimit ��Ϊ����ֵ
	 * -dV = br * dQ/V
	 * V = V - dV
	 * TODO��ע�⣡������������������������������������������������������������������������������������������������PQ+PV�ڵ㣬������Ϊֻ�����ƴ�PV�ڵ�
	 * @param voltage ��ѹֵ
	 * @param power ��ǰ���ʷֲ���������ȡ�޹�������
	 * @return
	 * @throws InterpssException
	 */
	@Override
	public boolean correct(AclfCase aclfCase) {
		long startTime = System.currentTimeMillis();
		
		Complex[] power = aclfCase.getPower();
		//get dQ[]
		boolean qOverflow = false;
		double[] dQ = new double[noBus];
		for (int i=0; i<noBus; ++i) {
			double q = power[i].getImaginary();
			if (busType[i] == RefInfo.ONLY_PV_BUS_TYPE /*busCode[i].equals("GenPV")*/) {
				if (q > maxQGenLimit[i]) {
					dQ[i] = maxQGenLimit[i] * this.qLimitSparseFactor - q;
					qOverflow = true;
				}else if (q < minQGenLimit[i]) {
					dQ[i] = minQGenLimit[i] * this.qLimitSparseFactor - q;
					qOverflow = true;
				}else
					dQ[i] = 0;
			}else {
				dQ[i] = 0;
			}
		}
		//���ûԽ�����������棬ֱ�ӷ���ûԽ�޾�����
		if (!qOverflow) {
			addTimeUse(startTime);
			return true;
		}
		addCallTimes();
		
		//���Խ���ˣ�������ѹ
		Complex[] voltage = aclfCase.getVoltage();
		//get dQ/V
		double[] dqv = new double[noBus]; 
		for (int i=0; i<noBus; ++i) {
			dqv[i] = dQ[i] / voltage[i].abs();
		}
		//����˷�  -dV = br * dQ/V
		double[] dV = new double[noBus]; 
		for (int i=0; i<noBus; ++i) {
			dV[i] = 0;
			if (i != swingNo && !busCode[i].equals("GenPV"))//ע�⵽�����ϵ�b11r�����������b11rȥ��swing��PV����
				for (int j=0; j<noBus; ++j)
					if (j != swingNo && !busCode[i].equals("GenPV"))
						dV[i] -= b11r[i][j] * dqv[j];
		}
				
		//correct v += dV, theta = theta
		for (int i=0; i<noBus; ++i) 
			if (i != swingNo){
				double v = voltage[i].abs() + dV[i];
				voltage[i] = voltage[i].multiply(v/voltage[i].abs());
			}
		
		aclfCase.setVoltage(voltage);
		
		addTimeUse(startTime);
		return false;
	}

	/**
	 * ֧���޸�Ĭ��ֵ
	 */
	@Override
	public void setQLimitSparseFactor(double qLimitSparseFactor) {
		this.qLimitSparseFactor = qLimitSparseFactor;
		System.out.println("[REPORT] "+methodName+" paramter set. qLimitSparseFactor = "+qLimitSparseFactor);
	}

	public int getNoBus() {
		return noBus;
	}

	public double[][] getB11r() {
		return b11r;
	}

	public String[] getBusCode() {
		return busCode;
	}

	public double[] getMaxQGenLimit() {
		return maxQGenLimit;
	}

	public double[] getMinQGenLimit() {
		return minQGenLimit;
	}

	public double getQLimitSparseFactor() {
		return qLimitSparseFactor;
	}

	@Override
	public String keyMsg() {
		return NAME_IN_SHORT+"-qf-"+this.qLimitSparseFactor;
	}
	
}
