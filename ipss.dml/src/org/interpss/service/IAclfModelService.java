package org.interpss.service;

public interface IAclfModelService {

	/*
	 *  Multi-Network object functions
	 *  ============================== 
	 */
	/**
	 * Load multiple loadflow cases in IEEE CMD format and create the TrainCaseBuilder object
	 * 
	 * @param filenames Loadflow case filesnames "file1,file2,...". It could be a dir path.
	 * @param buildername training set builder name (see details in TrainDataBuilderFactory.java)
	 * @param busIdMappingFile busId mapping filename
	 * @param branchIdMappingFile branchId mapping filename
	 * @param netOptPatternFile network operation pattern info filename
	 * @return an int[3] array, [bus nn　model dimension, branch nn　model dimension, no of NetOptPattern]
	 */
	int[] loadMultiCases(String filenames, String buildername, String busIdMappingFile, String branchIdMappingFile,
			String netOptPatternFile);

	/**
	 * Load a loadflow case in IEEE CMD format and create the TrainCaseBuilder object
	 * 
	 * @param filename
	 * @param buildername training set builder name (see details in TrainDataBuilderFactory.java)
	 * @return an int[2] array, [bus nn　model dimension, branch nn　model dimension]
	 */
	int[] loadCase(String filename, String buildername);

	/**
	 * Load a loadflow case in IEEE CMD format and create the TrainCaseBuilder object
	 * 
	 * @param filename
	 * @param buildername training set builder name (see details in TrainDataBuilderFactory.java)
	 * @param busIdMappingFile
	 * @param branchIdMappingFile
	 * @return an int[2] array, [bus nn　model dimension, branch nn　model dimension]
	 */
	int[] loadCase(String filename, String buildername, String busIdMappingFile, String branchIdMappingFile);

	/**
	 * create and return a set of training cases, 
	 * 
	 *   Data format: [2][points][]
	 *       [
	 *         [input, output], ... [input, output]
	 *       ]
	 * 
	 * input/output is a string of "x1 x2 ...", representing
	 * a double[] for the large-scale array performance reason. 
	 *                
	 * @param points number of training cases
	 * @return the training set
	 */
	String[][] getTrainSet(int points);

	/**
	 * create and return a random test case, 
	 * 
	 *   Data format: [2][]
	 *              [
	 *                 input, output
	 *              ]
	 *                
	 *	input/output is a string of "x1 x2 ...", representing
	 *  a double[] for the large-scale array performance reason.                        
	
	 * @return the training set
	 */
	String[][] getTestCase();

	/**
	 * create and return a test case using the factor to generate the case,
	 *   
	 *   Data format: [2][]
	 *              [
	 *                input, output
	 *              ]  
	
	 *	input/output is a string of "x1 x2 ...", representing
	 *  a double[] for the large-scale array performance reason.                        
	 *                     
	 * @param factor some value for creating the test case
	 * @return the training set
	 */
	String[][] getTestCase(double factor);

	/**
	 * compute and return the mismatch info based on the network solution 
	 * for bus voltage
	 * 
	 * @param netVolt network bus voltage solution
	 * @return mismatch info string
	 */
	String getMismatchInfo(double[] netVolt);

	/**
	 * compute and return the mismatch based on the network solution 
	 * for bus voltage
	 * 
	 * @param netVolt network bus voltage solution
	 * @return mismatch info string
	 */
	double[] getMismatch(double[] netVolt);

}