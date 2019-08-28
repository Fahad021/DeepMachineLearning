package lfgen.condition_gen;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.emf.common.util.EList;

import com.interpss.core.aclf.AclfBus;

import lfgen.algo.IVgcBuilder;
import lfgen.algo.impl.AlgoObject;
import lfgen.datatype.RefInfo;
import lfgen.datatype.VoltageGenCondition;

/**
* @author JeremyChenlk
* @version 2019��2��13�� ����6:32:59
*
* Class description:
*	
*/

public class VThScanConditionBuilder extends AlgoObject implements IVgcBuilder {

	public static final String NAME = "VTHScanBuilder";
	public static final String NAME_IN_SHORT = "VTH";
	public static final String PARA_NEEDED = "vSparseFactor";

	private static final double DEFAULT_BOUNDARY_FACTOR = 1.0;
	private static final int DEFAULT_BEGIN_STATUS = -1;
	
	private VoltageGenCondition c = null;
	
	private int noBus = 0;
	private int swingNo = 0;
	
	private int status = 0;
	
	private double[] baseTheta = null;
	private double[] baseV = null;
	private double[] voltageMagError = null;
	private double boundaryFactor = 1;
	
	private Complex[] voltage = null;
	
	public VThScanConditionBuilder(RefInfo refInfo) {
		super(refInfo);
		long startTime = System.currentTimeMillis();
		this.methodName = NAME;
		System.out.print("[REPORT] new "+methodName+"...");
		
		c = new VoltageGenCondition(true);
		this.noBus = ref.getNoBus();
		this.swingNo = ref.getSwingNo();
		boundaryFactor = DEFAULT_BOUNDARY_FACTOR;
		this.status = DEFAULT_BEGIN_STATUS;

		baseTheta = new double[noBus];
		baseV = new double[noBus];
		voltageMagError = new double[noBus];
		voltage = new Complex[noBus];
		
		EList<AclfBus> busList = ref.getNet().getBusList();
		for (int i=0; i<noBus; ++i) {
			baseTheta[i] = busList.get(i).getVoltageAng();
			baseV[i] = busList.get(i).getVoltageMag();
			voltageMagError[i] = getVoltageMagError(busList.get(i).getBaseVoltage());
		}

		//report
		System.out.println(" ...ready. Need to input parameter: "+PARA_NEEDED);
		System.out.println("[WARNING] this method is only used in IEEE 14 bus system!");
		addInitTime(startTime);
	}
	
	public void setVBoundaryFactor(double f) {
		this.boundaryFactor = f;
		System.out.println("[REPORT] "+methodName+" paramter set. vBoundaryFactor = "+boundaryFactor);
	}

	@Override
	public VoltageGenCondition nowCondition() {
		return c;
	}

	@Override
	public VoltageGenCondition nextCondition() {
		long startTime = System.currentTimeMillis();
		
		this.status += 1;
		for (int i=0; i<noBus; ++i) 
			if (i != swingNo) {
				//λ����
				double mag = baseV[i] * (1 - boundaryFactor * voltageMagError[i] * (1 - 2 * (status & 1)));
				status = status>>>1;
				//�����������...�����õ�ѹ�ķ�ֵ�����Ʒ���
				double ang = baseTheta[i] * (1 - boundaryFactor * voltageMagError[i] * (1 - 2 * (status & 1)));
				status = status>>>1;
			
				voltage[i] = new Complex(mag * Math.cos(ang), mag * Math.sin(ang));
			}else
				voltage[i] = new Complex(1.0, 0);
		
		//��ʽ��ֵ��ƽ��ڵ㻹�ǲ����
		voltage[swingNo] = new Complex(1.0, 0.0);
		if (status == 1<<26)
			status = -1;
		c.setVoltage(voltage);
		
		addTimeUse(startTime);
		return c;
	}
	
	public VoltageGenCondition getC() {
		return c;
	}

	public int getNoBus() {
		return noBus;
	}

	public int getSwingNo() {
		return swingNo;
	}

	public int getStatus() {
		return status;
	}

	public double[] getBaseTheta() {
		return baseTheta;
	}

	public double[] getBaseV() {
		return baseV;
	}

	public double[] getVoltageMagError() {
		return voltageMagError;
	}

	public double getBoundaryFactor() {
		return boundaryFactor;
	}

	public Complex[] getVoltage() {
		return voltage;
	}

	/**
	 * �Ը����ĵ�ѹ�ȼ���������ĵ�ѹƫ��ٷֱȣ�Ref������ϵͳ�����ϲᣬP5.
	 * note: IEEE��ʽ�г���14bus��135��35��10֮�⣬�����ľ�Ϊ100
	 * note: ���ڹ��캯����ִ��һ��
	 * @param voltageLevel
	 * @return
	 */
	protected static double getVoltageMagError(double voltageLevel) {
		if (voltageLevel < 34)
			return 0.07;
		return 0.07;
	}

	@Override
	public String keyMsg() {
		return NAME_IN_SHORT+"-vsf-"+boundaryFactor;
	}
}
