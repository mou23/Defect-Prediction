import java.io.File;

public class MyMain {
	public static void main(String[] args) {
		Preprocessor metricsCalculator = new Preprocessor();
		metricsCalculator.calculateMetrics(new File("test.java"));
		metricsCalculator.showResult();
	}
}
