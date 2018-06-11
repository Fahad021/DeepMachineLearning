 /*
  * @(#)AclfPyGateway.java   
  *
  * Copyright (C) 2005-17 www.interpss.org
  *
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

    	http://www.apache.org/licenses/LICENSE-2.0
    
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
  *
  * @Author Mike Zhou
  * @Version 1.0
  * @Date 04/7/2017
  * 
  *   Revision History
  *   ================
  *
  */

package org.interpss.service;

import org.apache.commons.math3.complex.Complex;
import org.interpss.IpssCorePlugin;
import org.interpss.service.train_data.IAclfTrainCaseBuilder;
import org.interpss.service.train_data.multiNet.IMultiNetTrainCaseBuilder;
import org.interpss.service.util.UtilFunction;

import com.interpss.common.exp.InterpssException;

import py4j.GatewayServer;

/**
 * InterPSS AC Loadflow training data service gateway
 * 
 * @author Mike
 *
 */ 
public class AclfPyGateway implements IAclfModelService {
	private IAclfTrainCaseBuilder trainCaseBuilder;
	
	/*
	 *  Multi-Network object functions
	 *  ============================== 
	 */
	/* (non-Javadoc)
	 * @see org.interpss.service.IAclfModelService#loadMultiCases(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */	
	@Override
	public int[] loadMultiCases(String filenames, String buildername, 
			                    String busIdMappingFile, String branchIdMappingFile,
			                    String netOptPatternFile) {
		IpssCorePlugin.init();
		
		try {
			this.trainCaseBuilder = UtilFunction.createMultiNetBuilder(filenames, buildername, 
					                   busIdMappingFile, branchIdMappingFile, netOptPatternFile);
		} catch ( InterpssException e) {
			e.printStackTrace();
			return new int[] {0, 0};
		}
		
		return new int[] {this.trainCaseBuilder.getNoBus(), 
				          this.trainCaseBuilder.getNoBranch(),
				          ((IMultiNetTrainCaseBuilder)this.trainCaseBuilder).getNoNetOptPatterns()};
	}
	
	
	/*
	 *  Single Network object functions
	 *  =============================== 
	 */
	
	/* (non-Javadoc)
	 * @see org.interpss.service.IAclfModelService#loadCase(java.lang.String, java.lang.String)
	 */
	@Override
	public int[] loadCase(String filename, String buildername) {
		IpssCorePlugin.init();
		
		try {
			this.trainCaseBuilder = UtilFunction.createSingleNetBuilder(filename, buildername);
		} catch ( InterpssException e) {
			e.printStackTrace();
			return new int[] {0, 0};
		}	
		
		return new int[] {this.trainCaseBuilder.getNoBus(), this.trainCaseBuilder.getNoBranch()};		
	}

	/* (non-Javadoc)
	 * @see org.interpss.service.IAclfModelService#loadCase(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */	
	@Override
	public int[] loadCase(String filename, String buildername, String busIdMappingFile, String branchIdMappingFile) {
		IpssCorePlugin.init();
		
		try {
			this.trainCaseBuilder = UtilFunction.createSingleNetBuilder(filename, buildername, busIdMappingFile, branchIdMappingFile);
		} catch ( InterpssException e) {
			e.printStackTrace();
			return new int[] {0, 0};
		}		
	
		return new int[] {this.trainCaseBuilder.getNoBus(), this.trainCaseBuilder.getNoBranch()};
	}
	
	/*
	 *  Common functions
	 *  ================ 
	 */
	
	/* (non-Javadoc)
	 * @see org.interpss.service.IAclfModelService#getTrainSet(int)
	 */
	@Override
	public String[][] getTrainSet(int points) {
		String[][] trainSet = new String[2][points];
		for (int i = 0; i < points; i++) {
			this.trainCaseBuilder.createTrainCase(i, points);
			double[] input = this.trainCaseBuilder.getNetInput();
			double[] output = this.trainCaseBuilder.getNetOutput();
			trainSet[0][i] = UtilFunction.array2String(input);
			trainSet[1][i] = UtilFunction.array2String(output);
		}
		return trainSet;
	}
	
	/* (non-Javadoc)
	 * @see org.interpss.service.IAclfModelService#getTestCase()
	 */
	@Override
	public String[][] getTestCase() {
		String [][] data = new String[2][1];
		
		this.trainCaseBuilder.createTestCase();
		double[] input = this.trainCaseBuilder.getNetInput();
		double[] output = this.trainCaseBuilder.getNetOutput();	
		data[0][0] = UtilFunction.array2String(input);
		data[1][0] = UtilFunction.array2String(output);
		
		return data;
	}	
	
	/* (non-Javadoc)
	 * @see org.interpss.service.IAclfModelService#getTestCase(double)
	 */
	@Override
	public String[][] getTestCase(double factor) {
		String [][] data = new String[2][1];
		
		this.trainCaseBuilder.createTestCase(factor);
			
		double[] input = this.trainCaseBuilder.getNetInput();
		double[] output = this.trainCaseBuilder.getNetOutput();	
		data[0][0] = UtilFunction.array2String(input);
		data[1][0] = UtilFunction.array2String(output);
		
		return data;
	}	

	/* (non-Javadoc)
	 * @see org.interpss.service.IAclfModelService#getMismatchInfo(double[])
	 */
	@Override
	public String getMismatchInfo(double[] netVolt) {
		return this.trainCaseBuilder.calMismatch(netVolt).toString();
	}	
	
	/* (non-Javadoc)
	 * @see org.interpss.service.IAclfModelService#getMismatch(double[])
	 */
	@Override
	public double[] getMismatch(double[] netVolt) {
		Complex maxMis= this.trainCaseBuilder.calMismatch(netVolt).maxMis;
		return new double[] {maxMis.getReal(),maxMis.getImaginary()};
	}
	
	/**
	 * main method to start the service
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		IAclfModelService app = new AclfPyGateway();
		// app is now the gateway.entry_point
		GatewayServer server = new GatewayServer(app);
		System.out.println("Starting Py4J " + app.getClass().getTypeName() + " ...");
		server.start();
	}	
}
