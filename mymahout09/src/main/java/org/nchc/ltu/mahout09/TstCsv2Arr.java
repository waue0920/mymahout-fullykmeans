package org.nchc.ltu.mahout09;

public class TstCsv2Arr {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inputFile ="D://windoop/abaloneTst.csv";
		double[][] outputArr = { {1, 1, 2}, {2, 1, 2}, {1, 2, 2},
                 {2, 2, 2}, {3, 3, 2}, {8, 8, 4},
                 {9, 8, 4}, {8, 9, 4}, {9, 9, 4}};
		
//		double[][] outputArr = Csv2Arr.toArr(inputFile);
		int Rowc = outputArr.length;
		for(int i=0;i<outputArr[Rowc-1].length;i++){
			System.out.print("outputArr["+(Rowc-1)+"]["+ i +"] = "+ outputArr[Rowc-1][i]+"\t");				
		}
		System.out.println();

	}

}
