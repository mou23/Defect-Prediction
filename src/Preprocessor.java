import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jface.text.Document;

public class Preprocessor {
	int total_LOC = 0;
	
	static CompilationUnit compilation_unit;
	ArrayList<Integer>line_length = new ArrayList<Integer>();
	

	void calculateMetrics(File file) {
		AST_Calculator(file);
	}
	
	void showResult() {
	}
	

	void AST_Calculator(File file) {
		ASTParser parser = ASTParser.newParser(AST.JLS10);
		String fileContent = readFileToString(file.getAbsolutePath());
		Document document = new Document(fileContent);
		Map<String, String> options = JavaCore.getOptions();
		options.put("org.eclipse.jdt.core.compiler.source", "1.8");
		parser.setCompilerOptions(options);
		parser.setSource(document.get().toCharArray());
		parser.setStatementsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		
		parser.setEnvironment(new String[0], new String[0] , null, true);
		parser.setUnitName("file.java");
		compilation_unit = (CompilationUnit) parser.createAST(null);
		compilation_unit.accept(new MethodVisitor());
	}
	
	public String readFileToString(String file_path) {
		StringBuilder file_data = new StringBuilder(100000);
		try{		
			BufferedReader reader = new BufferedReader(new FileReader(file_path));

			char[] buffer = new char[10];
			int numRead = 0;
			while ((numRead = reader.read(buffer)) != -1) {
				String readData = String.valueOf(buffer, 0, numRead);
				file_data.append(readData);
				buffer = new char[1024];
			}

			reader.close();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}

		return  file_data.toString();	
	}
}
