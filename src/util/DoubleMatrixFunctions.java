package util;

import org.jblas.*;

public abstract class DoubleMatrixFunctions {
	
	public static DoubleMatrix ColumnWiseNormalize(DoubleMatrix P)
	{
		DoubleMatrix PNorm = new DoubleMatrix(P.rows,P.columns);
		for(int k=0; k<P.columns; k++)
        {
			DoubleMatrix ColK = P.getColumn(k);
        	double norm = ColK.norm2();
        	ColK.divi(norm);
        	PNorm.putColumn(k,ColK);
        }
		return PNorm;
	}
	
	public static DoubleMatrix IncrementColumn(DoubleMatrix inp, int ColumnIndex,DoubleMatrix ColumnVector)
	{
		//System.out.println(inp.columns + " " + ColumnIndex);
		DoubleMatrix IncColumn = inp.getColumn(ColumnIndex);
		inp.putColumn(ColumnIndex, IncColumn.addi(ColumnVector));
		return inp;
	}
	
	public static double SquaredNorm(DoubleMatrix inp)
	{
		return (inp.mul(inp)).sum();
	}
	
	public static void prettyPrint(DoubleMatrix inp)
	{
		System.out.println(">>");
		for(int i=0; i< Math.min(Integer.MAX_VALUE,inp.rows); i++)
		{
			for(int j=0; j< Math.min(Integer.MAX_VALUE,inp.columns); j++)
				System.out.printf("%.4f ", inp.get(i, j));
			System.out.println();
		}
		System.out.println("<<");
	}
}
