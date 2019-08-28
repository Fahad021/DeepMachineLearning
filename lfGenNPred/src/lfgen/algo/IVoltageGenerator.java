package lfgen.algo;

import org.apache.commons.math3.complex.Complex;

import lfgen.datatype.VoltageGenCondition;

/**
* @author JeremyChenlk
* @version 2019��2��13�� ����4:23:32
*
* Class description:
*	
*/

public interface IVoltageGenerator {
	Complex[] genVoltage(VoltageGenCondition c);
}
