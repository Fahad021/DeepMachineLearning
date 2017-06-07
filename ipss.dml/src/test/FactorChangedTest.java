package test;

import static org.junit.Assert.assertTrue;

import org.interpss.IpssCorePlugin;
import org.interpss.service.UtilFunction;
import org.interpss.service.train_data.ITrainCaseBuilder;
import org.interpss.service.train_data.singleNet.aclf.load_change.BusVoltLoadChangeTrainCaseBuilder;
import org.junit.Test;

import com.interpss.common.exp.InterpssException;

public class FactorChangedTest {
	@Test 
	public void testSingleNet() throws InterpssException {
		IpssCorePlugin.init();
		
  		ITrainCaseBuilder caseBuilder = UtilFunction.createSingleNetBuilder(
  				       "testdata/ieee14.ieee", "BusVoltageTrainCaseBuilder");
  		
  		caseBuilder.createTestCase(1.5);
  		double[] netVolt = caseBuilder.getNetOutput();
  		
  		caseBuilder.createTestCase(0.5);
  		
  		caseBuilder.createTestCase(1.5);
  		double[] netVolt2 = caseBuilder.getNetOutput();
  		
  		System.out.println(netVolt[1]);
  		System.out.println(netVolt2[1]);
  		assertTrue(Math.abs(netVolt[1]-netVolt2[1])<0.01);
   	}
	
	@Test 
	public void testSingleNet2() throws InterpssException {
		IpssCorePlugin.init();
		
  		ITrainCaseBuilder caseBuilder = UtilFunction.createSingleNetBuilder(
  				       "testdata/ieee14.ieee", "BusVoltageTrainCaseBuilder");
  		
  		caseBuilder.createTestCase(1.5);
  		double[] netVolt = caseBuilder.getNetOutput();
  		
  		caseBuilder.createTestCase(0.5);
  		
  		BusVoltLoadChangeTrainCaseBuilder builder =((BusVoltLoadChangeTrainCaseBuilder)caseBuilder);
  		builder.getAclfNet().getBusList().forEach(bus->{
			if(bus.isGenPV())
				bus.getBusControl().setStatus(true);
		});
  		caseBuilder.createTestCase(1.5);
  		double[] netVolt2 = caseBuilder.getNetOutput();
  		
  		System.out.println(netVolt[1]);
  		System.out.println(netVolt2[1]);
  		assertTrue(Math.abs(netVolt[1]-netVolt2[1])<0.01);
   	}
}
