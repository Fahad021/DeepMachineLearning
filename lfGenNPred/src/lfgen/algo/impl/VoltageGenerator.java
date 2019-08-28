package lfgen.algo.impl;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.emf.common.util.EList;
import org.interpss.numeric.exp.IpssNumericException;
import org.interpss.pssl.simu.IpssDclf;
import org.interpss.pssl.simu.IpssDclf.DclfAlgorithmDSL;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.dclf.DclfAlgorithm;
import com.interpss.core.dclf.common.ReferenceBusException;

import lfgen.datatype.VoltageGenCondition;
import lfgen.algo.IVoltageGenerator;
import lfgen.datatype.RefInfo;

/**
* @author JeremyChenlk
* @version 2019��1��24�� ����8:50:43
*
* Class description:
*	1. ��ֵ���������ṩ�ķ�����
*	������ѹ��ֵ���й�ˮƽ����case
*	����ĳ��λ���������ɵ�ѹ���й�ˮƽ��������case
*	�����й�ˮƽ��ĳ��λ����������case
*/

public class VoltageGenerator extends AlgoObject implements IVoltageGenerator {

	public static final String NAME = "Default_VoltageGenerator";
	public static final String NAME_IN_SHORT = "dVG";
	public static final String PARA_NEEDED = "NONE";
	
	protected double[][] b1r = null;
	protected int noBus = 0;
	protected int swingNo = 0;
	
	public VoltageGenerator(RefInfo refInfo) {
		super(refInfo);
		long startTime = System.currentTimeMillis();
		this.methodName = NAME;
		System.out.print("[REPORT] new "+methodName+"...");
		
		this.b1r = ref.getB1r();
		this.noBus = ref.getNoBus();
		this.swingNo = ref.getSwingNo();
		
		//report
		System.out.println(" ...ready. Need to input parameter: "+PARA_NEEDED);
		addInitTime(startTime);
	}
	
	/**
	 * ��Ŀ���Ե�����
	 * ���ɵ�ѹ�����������ɷ�ֵ���й����ɡ�����Ϊ�����й�һ���Ƿ��Ϲ����ϵ���Ҫ��һ���Ƿ���ʵ�������������ռ䣬�����Ч�Եı�Ҫ��
	 * note: �й�ˮƽ��Ϊ��Ҫ���Ų�����ǰ��
	 * @param v
	 * @param p
	 * @return
	 */
	@Override
	public Complex[] genVoltage(VoltageGenCondition c) {
		long startTime = System.currentTimeMillis();
		addCallTimes();
		
		Complex[] voltage;
		if (c.isThVMethod()) {
			voltage = c.getVoltage();
		}else {
			voltage = toComplexV(c.getV(), genTheta(c.getP()));
		}
		
		addTimeUse(startTime);
		return voltage;
	}
	
	/**
	 * ���ݸ�����p������ֱ�������õ���ǣ�д���ġ�
	 * P=B�� => �� = B^-1 * P
	 * @param p
	 * @return
	 */
	private double[] genTheta(double[] p) {
		double[] theta = new double[noBus];
		
		for (int i=0; i<noBus; ++i) {
			theta[i] = 0;
			if (i != swingNo)
				for (int j=0; j<noBus; ++j) 
					if (j != swingNo)
					theta[i] -= this.b1r[i][j] * p[j];
		}
		
		return theta;
	}

	/**
	 * ���ݵ�ѹ��ֵ��������ɵ�ѹ������Ψһ�����ص㣩������ֻ��Ҫ�ڴ���ʽ�ؽ�swing�ڵ��ѹ�Ļ����ͺ���
	 * @param v
	 * @param th
	 * @return
	 */
	 private Complex[] toComplexV(double[] v, double[] th) {
		Complex[] voltage = new Complex[noBus];
		for (int i=0; i<noBus; ++i) {
			voltage[i] = new Complex(v[i]*Math.cos(th[i]), v[i]*Math.sin(th[i]));
		}
//		//��ʽ�ؽ�swing��ֵУ��
//		if (!Complex.equals(voltage[this.swingNo], new Complex(1, 0))) {
//			//Debug
//			System.out.println("[DEBUG] VoltageGenerator.toComplexV: voltage[swingNo] != (1, 0), == "+voltage[this.swingNo]+"\t, but it is already fixed to (1, 0)!");
//			String errStr = new String("");
//			for (int i=0; i<noBus; ++i) 
//				errStr += "Bus "+i+" V = "+voltage[i]+"\t";
//			System.out.println(errStr);
//			
//			voltage[this.swingNo] = new Complex(1, 0);
//		}
		return voltage;
	}
	
	public double[][] getB1r() {
		return b1r;
	}

	public int getNoBus() {
		return noBus;
	}

	public int getSwingNo() {
		return swingNo;
	}

	@Override
	public String keyMsg() {
		return NAME_IN_SHORT;
	}
}
