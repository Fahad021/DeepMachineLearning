package lfgen.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.math3.complex.Complex;

import com.interpss.common.exp.InterpssException;

import lfgen.algo.impl.AlgoObject;
import lfgen.algo.impl.LFComparator;
import lfgen.algo.impl.QChecker;
import lfgen.condition_gen.PVCG2;
import lfgen.datatype.AclfCase;
import lfgen.platform.LoadFlowCaseGenerator;

/**
* @author JeremyChenlk
* @version 2019��2��3�� ����2:13:15
*
* Class description:
*	
*/

public class CaseBuilder14 extends LoadFlowCaseGenerator {

	public static final String NAME = "CaseBuilder14";
	public static final String NAME_IN_SHORT = "Case14";
	public static final String PARA_NEEDED = "NONE";

	/**
	 * validation
	 */
	private int nCase = 0;
	private int okCase = 0;
	
	private int caseCount = 0;
	private PrintStream f = null;
	
	private Complex[] branchY = null;

	public CaseBuilder14(String baseCasePath) throws InterpssException {
		super(baseCasePath);
		long startTime = System.currentTimeMillis();
		platformName = NAME;
		nameInShort = NAME_IN_SHORT;
		System.out.print("[REPORT] new "+platformName+"... ...ready.");
		
		System.out.println("[Validation] ON...");
		System.out.println("\tItem 1\tif swing bus V = 1, 0"
				+"\n\tItem 2\tif other v in internal"+LFComparator.V_MIN+", "+LFComparator.V_MAX
				+"\n\tItem 3\tif other th less than LFComparator.TH_ABS_MAX = "+LFComparator.TH_ABS_MAX
				+"\n\tItem 4\tif P less than LFComparator.P_ABS_MAX = "+LFComparator.P_ABS_MAX
				+"\n\tItem 5\tif Q less than LFComparator.Q_ABS_MAX = "+LFComparator.Q_ABS_MAX
				+"\n\tItem 6\tif Q of ONLY_PV bus in internal refInfo.QLimit"
				+"\n\tItem 7\tif PQ of CONNECT bus = 0, 0");
		nCase = 0;
		okCase = 0;
		addInitTime(startTime);
	}
	
	/**
	 * ���컯�ĳ�ʼ�����������������ʽ����ִ��һ��
	 * ��Ҫ�̳к��Լ���д
	 * @throws InterpssException 
	 */
	@Override
	public void init() {
		long startTime = System.currentTimeMillis();
		//�������վɡ�
		super.init();
		//�����������
		voltageGenConditionBuilder = new PVCG2(refInfo);
		
		
		this.branchY = new Complex[5];
		Complex[][] y = refInfo.getY();
		this.branchY[0] = new Complex(y[0][1].getReal(), y[0][1].getImaginary());
		this.branchY[1] = new Complex(y[0][4].getReal(), y[0][4].getImaginary());
		this.branchY[2] = new Complex(y[1][2].getReal(), y[1][2].getImaginary());
		this.branchY[3] = new Complex(y[1][3].getReal(), y[1][3].getImaginary());
		this.branchY[4] = new Complex(y[1][4].getReal(), y[1][4].getImaginary());

		//new 
		FileOutputStream fs = null;
		SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//�������ڸ�ʽ
		try {
			fs = new FileOutputStream(new File("doc/Data/"+NAME_IN_SHORT+"/PVCase"
					+"-"+((AlgoObject) voltageGenConditionBuilder).keyMsg()
					+"-"+((AlgoObject) qChecker).keyMsg()
					+"-"+caseCount
					+"-"+dataFormat.format(new Date())+".txt"));
			} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		f = new PrintStream(fs);
		this.caseCount = 0;
		
		addInitTime(startTime);
	}
	
	@Override
	public void boom() throws InterpssException {
		
		double qbf = 0.95;
		
		((PVCG2)voltageGenConditionBuilder).init(1.0, 0.06, 0.06);
		((QChecker)qChecker).setQLimitSparseFactor(qbf);
		this.setMaxIter(10);
		
		double[] vfactor = new double[] {1};//������Ҫ������
		for (double vf:vfactor) {
			
			this.resetStatistics();
			
			FileOutputStream fs = null;
			SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//�������ڸ�ʽ
			
			try {
				fs = new FileOutputStream(new File("doc/Data/"+NAME_IN_SHORT+"/PVCaseBuildReport"
						+"-"+((AlgoObject)voltageGenConditionBuilder).keyMsg()
						+"-"+((AlgoObject)qChecker).keyMsg()
						+"-"+caseCount
						+"-"+dataFormat.format(new Date())+".txt"));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			PrintStream f = new PrintStream(fs);
			f.println(getReportTitle());
			
			int i;
			for (i=0; i<(1<<26); ++i) {
				go(voltageGenConditionBuilder.nextCondition());
				//ÿ1m�����һ�Σ���Լ��20�������һ��
				if (i % 1000000 == 0) {
					System.out.println("=================================[REPORT] �� "+i+"��=================================");
					report();
					System.out.println("===================================================================================");
					f.println(getReportStr());
				}
			}
			
			System.out.println("=================================[REPORT] �� "+i+"��=================================");
			report();
			System.out.println("===================================================================================");
			f.println(getReportStr());
			
			f.close();
		}
	}
	
	/**
	 * ����ض�������
	 * @param onGoNet
	 * @param voltage
	 * @param power
	 */
	@Override
	protected void successGenReaction(AclfCase aclfCase) {
		nCase += 1;
		if (LFComparator.checkCase(refInfo, aclfCase)) {
			okCase += 1;
			outCaseStr(aclfCase);
		}
	}
	
	private void outCaseStr(AclfCase aclfCase) {
		caseCount += 1;
		
		Complex[] voltage = aclfCase.getVoltage();
		Complex[] power = aclfCase.getPower();
		if (caseCount % 100000 == 0) {
			f.close();
			//new 
			FileOutputStream fs = null;
			SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//�������ڸ�ʽ
			try {
				fs = new FileOutputStream(new File("doc/Data/"+NAME_IN_SHORT+"/PVCase"
						+"-"+((AlgoObject) voltageGenConditionBuilder).keyMsg()
						+"-"+((AlgoObject) qChecker).keyMsg()
						+"-"+dataFormat.format(new Date())+".txt"));
				} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			f = new PrintStream(fs);
		}
		
		//report pqvth
		String report = new String("");
		String[] busCode = refInfo.getBusCode();
		for (int i=0; i<refInfo.getNoBus(); ++i) {
			if (busCode[i].equals("Swing")) {
				report += voltage[i].abs() +"\t"+ voltage[i].getArgument()+"\t";
			}else if (busCode[i].equals("GenPV")) {
				report += power[i].getReal() +"\t"+ voltage[i].abs()+"\t";
			}else {
				report += power[i].getReal() +"\t"+ power[i].getImaginary()+"\t";
			}
		}
		
		// S = V(YdV)*
		report += voltage[0].multiply((branchY[0].multiply(voltage[0].subtract(voltage[1]))).conjugate()).getReal()+"\t";
		report += voltage[0].multiply((branchY[1].multiply(voltage[0].subtract(voltage[4]))).conjugate()).getReal()+"\t";
		report += voltage[1].multiply((branchY[2].multiply(voltage[1].subtract(voltage[2]))).conjugate()).getReal()+"\t";
		report += voltage[1].multiply((branchY[3].multiply(voltage[1].subtract(voltage[3]))).conjugate()).getReal()+"\t";
		report += voltage[1].multiply((branchY[4].multiply(voltage[1].subtract(voltage[4]))).conjugate()).getReal()+"\n";
		
		f.print(report);
		
	}
	
	@Override
	public void report() {
		System.out.println("\n\t==============================================\n"
				+"\t[REPORT] nSuccessCase = "+"\t"+nCase+"\tokCase = "+okCase+"\n"
				+"\t==============================================");
		super.report();
	}
	
	@Override
	public String getReportTitle() {
		return super.getReportTitle()+"\tnSuccessCase\nOKCase";
	}
	
	@Override
	public String getReportStr() {
		return super.getReportStr()+"\t"+nCase+"\t"+okCase;
	}
	
	/**
	 * ����汾�ŵ��������ܣ�ȷ�Ϸ������ռ����600G
	 * @param args
	 * @throws InterpssException
	 */
	public static void main(String[] args) throws InterpssException {
		String baseCasePath = "testdata/cases/ieee14.ieee";
		CaseBuilder14 g = new CaseBuilder14(baseCasePath);
		g.init();
		g.boom();
	}

}
