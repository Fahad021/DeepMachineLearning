package lfgen.algo;

import org.apache.commons.math3.complex.Complex;

/**
* @author JeremyChenlk
* @version 2019��2��13�� ����4:39:49
*
* Class description:
*	
*/

public interface ILoadFlowGenerator {
	Complex[] genFlow(Complex[] voltage);
}
