package classify;

import math.*;
import org.jblas.*;
import util.*;

public class SoftmaxCost extends MemoizedDifferentiableFunction
{
	DoubleMatrix Features, Labels;
	DifferentiableMatrixFunction Activation;
	ClassifierTheta Gradient;
	double Lambda;
	int CatSize, FeatureLength;
	
	public SoftmaxCost(DoubleMatrix Features, int[] Labels, int CatSize, double Lambda)
	{
		this.CatSize = CatSize;
		FeatureLength = Features.rows;

		this.Features = Features;
		this.Lambda = Lambda;
		this.Labels = getLabelRepresentation(Labels);
		Activation = CatSize > 1 ? new Softmax() :new Sigmoid();
//		Activation = new Softmax();
		Gradient = null;
		initPrevQuery();
	}
	
	public SoftmaxCost(DoubleMatrix Features, DoubleMatrix Labels, double Lambda)
	{
		this.Features = Features;
		this.Lambda = Lambda;
		this.Labels = Labels;
		CatSize = Labels.rows;
		FeatureLength = Features.rows;
		Activation = CatSize > 1 ? new Softmax() :new Sigmoid();
//		Activation = new Softmax();
		Gradient = null;
		initPrevQuery();
	}
	
	public SoftmaxCost (int CatSize, int FeatureLength, double Lambda)
	{
		Features = null;
		Labels = null;
		Gradient = null;
		
		this.Lambda = Lambda;
		this.CatSize = CatSize;
		this.FeatureLength = FeatureLength;
		
		Activation = CatSize > 1 ? new Softmax() :new Sigmoid();
//		Activation = new Softmax();
		initPrevQuery();
	}
	
	@Override
	public int dimension() 
	{
		return (CatSize - 1) * (FeatureLength + 1);
	}

	/**
	* Finds the predicted labels for input data as parameterized by ClassifierTheta
	* @param Theta Classifier parameters
	* @param Features Input Data of dimensions featureLenght by numDataItems
	* @return A Prediction matrix of numCategories by numDataItems Matrix of predictions
	*/	
	public DoubleMatrix getPredictions (ClassifierTheta Theta, DoubleMatrix Features)
	{
//		DoubleArrays.prettyPrint(Theta.Theta);
		int numDataItems = Features.columns;
		DoubleMatrix Input = ((Theta.W.transpose()).mmul(Features)).addColumnVector(Theta.b);
		Input = DoubleMatrix.concatVertically(Input, DoubleMatrix.zeros(1,numDataItems));
		return Activation.valueAt(Input); 
	}
	
	public DoubleMatrix getGradient (ClassifierTheta Theta, DoubleMatrix Features, DoubleMatrix Labels)
	{
//		int[] requiredRows = ArraysHelper.makeArray(0, CatSize-2);
		
//		DoubleArrays.prettyPrint(Theta.Theta);
		
		int numDataItems = Labels.length;
		DoubleMatrix Prediction = getPredictions (Theta, Features);
//		double MeanTerm = 1.0 / (double) numDataItems;
		DoubleMatrix Diff = Prediction.sub(Labels); //.muli(MeanTerm);
	    return Diff; //.getRows(requiredRows);	
	}
	
	public DoubleMatrix getLoss (DoubleMatrix Prediction, DoubleMatrix Labels)
	{
		DoubleMatrix logLoss = MatrixFunctions.log((Labels.mul(Prediction)).columnSums()).muli(-1);
//		DoubleMatrixFunctions.prettyPrint(logLoss);
		return logLoss;
	}

	@Override
	public double valueAt(double[] x) 
	{
		if( !requiresEvaluation(x) )
			return value;
		int numDataItems = Features.columns;
		
		int[] requiredRows = ArraysHelper.makeArray(0, CatSize-2);
		ClassifierTheta Theta = new ClassifierTheta(x,FeatureLength,CatSize);
//		
//		DoubleMatrix Input = ((Theta.W.transpose()).mmul(Features)).addColumnVector(Theta.b);
//		Input = DoubleMatrix.concatVertically(Input, DoubleMatrix.zeros(1,numDataItems));
		DoubleMatrix Prediction = getPredictions (Theta, Features);
//		Activation.valueAt(Input);
		
		double MeanTerm = 1.0 / (double) numDataItems;
		double Cost = getLoss (Prediction, Labels).sum() * MeanTerm; 
		double RegularisationTerm = 0.5 * Lambda * DoubleMatrixFunctions.SquaredNorm(Theta.W);
		
		DoubleMatrix Diff = Prediction.sub(Labels).muli(MeanTerm);
	    DoubleMatrix Delta = Features.mmul(Diff.transpose());
	
	    DoubleMatrix gradW = Delta.getColumns(requiredRows);
	    DoubleMatrix gradb = ((Diff.rowSums()).getRows(requiredRows));
//	    DoubleMatrix gradW = Features.mmul(Diff.transpose()).getColumns(requiredRows);
	    
//	    System.err.println (gradW.sub(gradWW).sum());
	    
	    DoubleMatrixFunctions.prettyPrint(gradW);
	    
	    if (gradW.rows != Theta.W.rows)
	    	System.err.println ("W FAIL 1" + gradW.rows +" "+ Theta.W.rows);
	    
	    if (gradW.columns != Theta.W.columns)
	    	System.err.println ("W FAIL 2"+gradW.columns+ " " + Theta.W.columns);
	    
	    if (gradb.rows != Theta.b.rows || gradb.columns != Theta.b.columns)
	    	System.err.println ("b FAIL");
	    
	    //Regularizing. Bias does not have one.
	    gradW = gradW.addi(Theta.W.mul(Lambda));
	    
	    Gradient = new ClassifierTheta(gradW,gradb);
	    value = Cost + RegularisationTerm;
	    gradient = Gradient.Theta;
		return value; 
	}
	
	public DoubleMatrix getLabelRepresentation (int[] Labels)
	{
//		System.err.println (CatSize);
		int numDataItems = Labels.length;
		DoubleMatrix LabelRep = DoubleMatrix.zeros(CatSize,numDataItems);
		for(int i=0; i<Labels.length; i++)
		{
			if( Labels[i] < 0 || Labels[i] > CatSize )
				System.err.println("Bad Data : " + Labels[i] + " | " + i);
			else
				LabelRep.put(Labels[i],i,1);
		}
		return LabelRep;
	}
}