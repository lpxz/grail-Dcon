package Jama;

public class ScalarMultiplication {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        double[][] array = {{-1, 0 , 0 , 0 , 0, 0, 0},
        		            {1, -1, 0, 0, 0, 0, 0},
        		            {0, 1, -1, 0, 0, 0, 0},
        		            {0, 0, 1, -1, 0, 0, 0},
        		            {0, 0, 0, 1, -1, 0, 0},
        		            {0, 0, 0, 0, 1,  0, 0},
        		            {0, 0, 0, 0, 0, 1, -1},
        		            {1, 0, 0, 0, 0, -1, 0},
        		            {0, 0, 0, 0, -1, 0, 1},
        		            {0, -1, 0, 1, 0, 0, 0},
        		            };
        Matrix D = new Matrix(array);
        
        double[][] array2 = {{0,0,1,1,0,0, 1,0,0,0}};
        Matrix l = new Matrix(array2);
        
        Matrix C =l.times(D).times(-1);
        C.print(7, 1);
        
        double[][] array3 = {{1, 1, 1 , 0, 0},
        		{0, 0, 1 , 1, 1},
	            {1,1,0,1, 0},
	            {1,1,0,0, 1},
	            {1,0,0,1, 1},
	            {0,1,0,1, 1},
	           

	            };
        Matrix D3 = new Matrix(array3);
        System.out.println(D3.rank());
    

	}

}
