package rocks.inspectit.agent.java.javaagent;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

final public class Matrixx implements Runnable {
	private final int M; // number of rows
	private final int N; // number of columns
	private final double[][] data; // M-by-N array
	// test client
	@Override
	public void run() {
		while (true) {

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {

		args = new String[] { "200", "Gauss", "10" };
		// monitored threads, interval, threads name, matrix size (n), number of computing threads
		long startTime = System.currentTimeMillis();
		if (args.length != 3) {
			System.err.printf("Arguments: <computing threads> <thread name> <matrix size> ");
			System.exit(1);
		}
		final int cthreads = getIntArg(args[0], 1, 1000, "Invalid computing threads number %d, must be between 1 and 1000");
		String threadname = args[1];
		final int matrixSize = getIntArg(args[2], 10, 10000, "Invalid matrix size %d, must be between 10 and 10000");
		// creating the Matrix
		Thread threads[] = new Thread[cthreads];
		for (int i = 0; i < cthreads; i++) {
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					for (int xx = 0; xx < 100; xx++) {
						// creating random matrixes and performing operations on them
						Matrixx A = Matrixx.random(matrixSize, matrixSize);
						Matrixx B = Matrixx.random(matrixSize, matrixSize);
						A.plus(B).show();
						B.times(A).show();
						A.show();
						// creating 2 other matrixes for Gaussian Elimination
						Matrixx a = Matrixx.random(5, 5);
						Matrixx b = Matrixx.random(5, 1);
						Matrixx x = a.solve(b);
						x.show();

						Matrixx c = Matrixx.random(5, 5);
						Matrixx d = Matrixx.random(5, 1);
						Matrixx z = c.solve(d);
						z.show();
					}
				}
			});
			threads[i].setName("Gauss");
		}
		for (Thread t : threads) {
			t.start();
		}
		long stopTime = System.currentTimeMillis();
		System.out.println("############################### MATRIX: Elapsed time was " + (stopTime - startTime) + " miliseconds. ###############################");
	}
	// create M-by-N Matrixx of 0's
	public Matrixx(int N) {
		this(N, N);
	}
	// create M-by-N Matrixx of 0's
	public Matrixx(int M, int N) {
		this.M = M;
		this.N = N;
		data = new double[M][N];
	}
	// create Matrixx based on 2d array
	public Matrixx(double[][] data) {
		M = data.length;
		N = data[0].length;
		this.data = new double[M][N];
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				this.data[i][j] = data[i][j];
			}
		}
	}
	// copy constructor
	private Matrixx(Matrixx A)
	{
		this(A.data);
	}
	// create and return a random M-by-N Matrixx with values between 0 and 1
	public static Matrixx random(int M, int N) {
		Matrixx A = new Matrixx(M, N);
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				A.data[i][j] = Math.random();
			}
		}
		return A;
	}
	// create and return the N-by-N identity matrix
	public static Matrixx identity(int N) {
		Matrixx I = new Matrixx(N, N);
		for (int i = 0; i < N; i++) {
			I.data[i][i] = 1;
		}
		return I;
	}
	// swap rows i and j
	private void swap(int i, int j) {
		double[] temp = data[i];
		data[i] = data[j];
		data[j] = temp;
	}
	// create and return the transpose of the invoking matrix
	public Matrixx transpose() {
		Matrixx A = new Matrixx(N, M);
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				A.data[j][i] = this.data[i][j];
			}
		}
		return A;
	}
	// return C = A + B
	public Matrixx plus(Matrixx B) {
		Matrixx A = this;
		if ((B.M != A.M) || (B.N != A.N)) {
			throw new RuntimeException("Illegal Matrix dimensions.1");
		}
		Matrixx C = new Matrixx(M, N);
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				C.data[i][j] = A.data[i][j] + B.data[i][j];
			}
		}
		return C;
	}
	// return C = A - B
	public Matrixx minus(Matrixx B) {
		Matrixx A = this;
		if ((B.M != A.M) || (B.N != A.N)) {
			throw new RuntimeException("Illegal matrix dimensions.2");
		}
		Matrixx C = new Matrixx(M, N);
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				C.data[i][j] = A.data[i][j] - B.data[i][j];
			}
		}
		return C;
	}
	// does A = B exactly?
	public boolean eq(Matrixx B) {
		Matrixx A = this;
		if ((B.M != A.M) || (B.N != A.N)) {
			throw new RuntimeException("Illegal matrix dimensions.3");
		}
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				if (A.data[i][j] != B.data[i][j]) {
					return false;
				}
			}
		}
		return true;
	}
	// return C = A * B
	public Matrixx times(Matrixx B) {
		Matrixx A = this;
		if (A.N != B.M) {
			throw new RuntimeException("Illegal matrix dimensions.4");
		}
		Matrixx C = new Matrixx(A.M, B.N);
		for (int i = 0; i < C.M; i++) {
			for (int j = 0; j < C.N; j++) {
				for (int k = 0; k < A.N; k++) {
					C.data[i][j] += (A.data[i][k] * B.data[k][j]);
				}
			}
		}
		return C;
	}
	// return x = A^-1 b, assuming A is square and has full rank
	public Matrixx solve(Matrixx rhs) {
		if ((M != N) || (rhs.M != N) || (rhs.N != 1)) {
			throw new RuntimeException("Illegal matrix dimensions.5");
		}
		// create copies of the data
		Matrixx A = new Matrixx(this);
		Matrixx b = new Matrixx(rhs);
		// Gaussian elimination with partial pivoting
		for (int i = 0; i < N; i++) {
			// find pivot row and swap
			int max = i;
			for (int j = i + 1; j < N; j++) {
				if (Math.abs(A.data[j][i]) > Math.abs(A.data[max][i])) {
					max = j;
				}
			}
			A.swap(i, max);
			b.swap(i, max);
			// singular
			if (A.data[i][i] == 0.0) {
				throw new RuntimeException("Matrixx is singular.");
			}
			// pivot within b
			for (int j = i + 1; j < N; j++) {
				b.data[j][0] -= (b.data[i][0] * A.data[j][i]) / A.data[i][i];
			}
			// pivot within A
			for (int j = i + 1; j < N; j++) {
				double m = A.data[j][i] / A.data[i][i];
				for (int k = i + 1; k < N; k++) {
					A.data[j][k] -= A.data[i][k] * m;
				}
				A.data[j][i] = 0.0;
			}
		}
		// back substitution
		Matrixx x = new Matrixx(N, 1);
		for (int j = N - 1; j >= 0; j--) {
			double t = 0.0;
			for (int k = j + 1; k < N; k++) {
				t += A.data[j][k] * x.data[k][0];
			}
			x.data[j][0] = (b.data[j][0] - t) / A.data[j][j];
		}
		return x;

	}
	// #############################################################################################################################
	// print matrix to standard output
	public void show() {
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("theMatrix.txt"), "utf-8"));
			String toPrint = "";
			for (int i = 0; i < M; i++) {
				for (int j = 0; j < N; j++) {
					toPrint = String.format("%9.4f ", data[i][j]);
					writer.write(toPrint);
				}
				writer.write("\n");
			}
		} catch (IOException ex) {
			System.out.println("Error while writting in file..");
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				/* ignore */
			}
		}
	}
	// #############################################################################################################################
	private static int getIntArg(String arg, int min, int max, String mesg) {
		try {
			int result = Integer.parseInt(arg);
			if ((result < min) || (result > max)) {
				System.err.printf(mesg, result);
				System.exit(1);
			}
			return result;
		} catch (NumberFormatException ex) {
			System.err.println(String.format("Invalid integer input %s", arg));
			System.exit(1);
		}
		return -1;
	}
}