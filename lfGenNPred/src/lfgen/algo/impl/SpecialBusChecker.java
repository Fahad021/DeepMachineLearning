package lfgen.algo.impl;

import org.apache.commons.math3.complex.Complex;

import lfgen.algo.ISpecialBusChecker;
import lfgen.datatype.AclfCase;
import lfgen.datatype.RefInfo;

/**
* @author JeremyChenlk
* @version 2019��1��24�� ����10:32:13
*
* Class description:
* 	���ڽ�������ڵ���й�У��Ϊ0
*	���ڽ�����ڵ���й����޹�У��Ϊ0
*	У����������������
*		0   (��֪) = [rY00 rY01 ~][dI0  δ֪
*		dV1 (δ֪) = [rY10 rY11 ~][-I1 = (YV)1 ��֪
*		dV2 (δ֪) = [rY20 rY21 ~][0  ��֪
*	������dI0����dV1 dV2
*/

public class SpecialBusChecker extends AlgoObject implements ISpecialBusChecker {

	public static final String NAME = "Default_SpecialBusChecker";
	public static final String NAME_IN_SHORT = "dSBC";
	public static final String PARA_NEEDED = "NONE";
	
	/**
	 * ��������,���Ǹ�У���йصľ����������������RefNetInfo��
	 */
	protected Complex[][] vt = null;
	
	/*
	 * ��ref�õ���ָ�룬�Ǳ��������úͱ���ֵ�ظ�����Э
	 */
	protected int noBus = 0;
	protected int swingNo = 0;
	

	public SpecialBusChecker(RefInfo refInfo) {
		super(refInfo);
		long startTime = System.currentTimeMillis();
		this.methodName = NAME;
		System.out.print("[REPORT] new "+methodName+"...");
		
		this.noBus = ref.getNoBus();
		this.swingNo = ref.getSwingNo();
		
		Complex[][] yr = ref.getYr();
		int[] busType = ref.getBusType();
		//����m01, m01 = -1/yr00 * yr01, dI0 == m01 * dI1
		Complex[] m01 = new Complex[noBus];
		for (int i=0; i<noBus; ++i) {
			m01[i] = new Complex(0, 0);
			if (busType[i] == RefInfo.CONNECT_BUS_TYPE) //����˵��m01����1����ֵ
				m01[i] = yr[swingNo][i].divide(yr[swingNo][swingNo]).multiply(-1);
		}
		//����z����, z = yr12,0 * m01 + yr12,1, dV = z * dI
		Complex[][] z = new Complex[noBus][noBus];
		for (int i=0; i<noBus; ++i)
			for (int j=0; j<noBus; ++j) {
				z[i][j] = new Complex(0.0, 0.0);
				if (i != swingNo && busType[j] == RefInfo.CONNECT_BUS_TYPE) //��i˵��z��12�У���j˵��z��1��
					z[i][j] = yr[i][swingNo].multiply(m01[j]).add(yr[i][j]);
			}
		
		//����vt, vt = z * -Y1, dV12 = vt * V012
		this.vt = new Complex[noBus][noBus];
		Complex[][] y = ref.getY();
		for (int i=0; i<noBus; ++i)
			for (int j=0; j<noBus; ++j) {
				vt[i][j] = new Complex(0.0, 0.0);
				if (i != swingNo)//����˵��vt��1+2�У���jû�ж�˵��vt��0+1+2��
					for (int k=0; k<noBus; ++k)
						if (busType[k] == RefInfo.CONNECT_BUS_TYPE)
							vt[i][j] = vt[i][j].subtract(z[i][k].multiply(y[k][j]));
			}
		
		//report
		System.out.println(" ...ready. Need to input parameter: "+PARA_NEEDED);
		addInitTime(startTime);
	}
	
	/**
	 * ����������������ڽ��������������ڵ㹦��У��Ϊ0
	 * У����ĵ�ѹ����ͬʱ�޸ĵ�����
	 * @param net
	 * @return У����ĵ�ѹ����
	 */
	@Override
	public void correct(AclfCase aclfCase) {
		long startTime = System.currentTimeMillis();
		addCallTimes();
		
		Complex[] voltage = aclfCase.getVoltage();
		
		//����˷�
		Complex[] dV = new Complex[noBus]; 
		for (int i=0; i<noBus; ++i) {
			dV[i] = new Complex(0.0, 0.0);
			if (i != swingNo)
				for (int j=0; j<noBus; ++j)
					dV[i] = dV[i].add(vt[i][j].multiply(voltage[j]));
		}
		
		//correct v += dV, theta = theta
		for (int i=0; i<noBus; ++i) {
			voltage[i] = voltage[i].add(dV[i]);
		}
		
		//��ʽ��ֵ������case.coincide��false
		aclfCase.setVoltage(voltage);

		addTimeUse(startTime);
	}

	public int getNoBus() {
		return noBus;
	}

	public Complex[][] getVt() {
		return vt;
	}

	@Override
	public String keyMsg() {
		return NAME_IN_SHORT;
	}
}
