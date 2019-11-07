import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;

public class MethodVisitor extends ASTVisitor { 
	int open_curly_brackets_counter;
	int open_curly_brackets_that_are_first_character_in_a_line;
	int open_curly_brackets_that_are_last_character_in_a_line;
	int open_curly_brackets_that_are_middle_in_a_line;
	int open_curly_brackets_that_are_alone_in_a_line;
	
	int close_curly_brackets_counter;
	int close_curly_brackets_that_are_first_character_in_a_line;
	int close_curly_brackets_that_are_last_character_in_a_line;
	int close_curly_brackets_that_are_middle_in_a_line;
	int close_curly_brackets_that_are_alone_in_a_line;
	
	boolean blank_Line_Before_Close_Curly_Brackets;
	
	ArrayList<Integer>line_length = new ArrayList<Integer>();
	
	ArrayList<Integer> SLOC_per_function = new ArrayList<Integer>();
	ArrayList<Integer> function_name_length = new ArrayList<Integer>();
	ArrayList<Integer> uppercase_characters_in_function_name = new ArrayList<Integer>();
	ArrayList<Integer> lowercase_characters_in_function_name = new ArrayList<Integer>();
	ArrayList<Integer> number_characters_in_function_name = new ArrayList<Integer>();
	ArrayList<Integer> underscores_characters_in_function_name = new ArrayList<Integer>();

	ArrayList<Integer> variable_name_length = new ArrayList<Integer>();
	ArrayList<Integer> uppercase_characters_in_variable_name = new ArrayList<Integer>();
	ArrayList<Integer> lowercase_characters_in_variable_name = new ArrayList<Integer>();
	ArrayList<Integer> number_characters_in_variable_name = new ArrayList<Integer>();
	ArrayList<Integer> underscores_characters_in_variable_name = new ArrayList<Integer>();

	ArrayList<Variable> variables = new ArrayList<Variable>();

	int for_counter = 0;
	int foreach_counter = 0;
	int enhance_for_counter = 0;
	int do_counter = 0;
	int while_counter = 0;

	int if_counter = 0;
	int switch_counter = 0;
	
	int break_statement = 0;
	int catch_clause = 0;
	int assert_statement = 0;
	int continue_statement = 0;
	int return_statement = 0;
	int this_expression = 0;
	int throw_statement = 0;
	int try_statement = 0;
	int synchronized_statement = 0;
	int super_constructor_invocation = 0;
	int super_field_access = 0;
	int super_method_invocation = 0;
	int super_method_reference = 0;
	int instanceof_expression = 0;

	public boolean visit(MethodDeclaration methodDeclaration) {
		SimpleName name = methodDeclaration.getName();
		
		System.out.println(name+ " "+methodDeclaration.getModifiers());
		
		Block block = methodDeclaration.getBody();
		if(block==null) {
			return false;
		}
		String method_body = methodDeclaration.toString();
		
		curly_Brace_Handler(method_body);

		function_Naming_Preference_Counter(name.toString());

		function_name_length.add(name.getLength());

		int counter = Preprocessor.compilation_unit.getLineNumber(block.getStartPosition()+block.getLength())-Preprocessor.compilation_unit.getLineNumber(name.getStartPosition())+1;
		SLOC_per_function.add(counter);

		for (Object parameter : methodDeclaration.parameters()) {
			VariableDeclaration variableDeclaration = (VariableDeclaration) parameter;
			//			System.out.println(variableDeclaration + " "+variableDeclaration.getName().toString().length());
			variable_name_length.add(variableDeclaration.getName().toString().length());
			variable_Naming_Preference_Counter(variableDeclaration.getName().toString());
		}
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(SimpleName child) {    
				IBinding binding = (IBinding) ((SimpleName) child).resolveBinding();
				if(binding!=null && binding.getKind()==IBinding.VARIABLE) {
					System.out.println(child.toString());
					Variable variable = new Variable();
					variable.name = binding.getName();

					if((((SimpleName) child).resolveTypeBinding())!=null) {
						variable.type = (((SimpleName) child).resolveTypeBinding()).getQualifiedName();
					}
					else {
						variable.type = "";
					}
					boolean match = false;
					for(int i=0; i<variables.size();i++) {
						if(variables.get(i).name.equals(variable.name) && variables.get(i).type.equals(variable.type)) {
							match = true;
							break;
						}
					}
					if(match == false) {
						//					System.out.println(variable);
						variables.add(variable);
						variable_name_length.add(variable.name.length());
						variable_Naming_Preference_Counter(variable.name);
					}
				}

				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(ForeachStatement foreach) {
				foreach_counter++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(EnhancedForStatement enhance_for_statement) {
				enhance_for_counter++;
				return true;
			}    
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(ForStatement for_statement) {
				for_counter++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(DoStatement do_statement) {
				do_counter++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(WhileStatement while_statement) {
				while_counter++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(IfStatement if_statement) {
				if_counter++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(SwitchCase switch_case) {
				switch_counter++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(BreakStatement breakStatement) {
				break_statement++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(CatchClause catchClause) {
				catch_clause++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(AssertStatement assertStatement) {
				assert_statement++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(ContinueStatement continueStatement) {
				continue_statement++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(ReturnStatement returnStatement) {
				return_statement++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(ThisExpression thisExpression) {
				this_expression++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(ThrowStatement throwStatement) {
				throw_statement++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(TryStatement tryStatement) {
				try_statement++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(SynchronizedStatement synchronizedStatement) {
				synchronized_statement++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(SuperConstructorInvocation superConstructorInvocation) {
				super_constructor_invocation++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(SuperFieldAccess superFieldAccess) {
				super_field_access++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(SuperMethodInvocation superMethodInvocation) {
				super_method_invocation++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(SuperMethodReference superMethodReference) {
				super_method_reference++;
				return true;
			}
		});
		methodDeclaration.accept(new ASTVisitor() {
			public boolean visit(InstanceofExpression instanceofExpression) {
				instanceof_expression++;
				return true;
			}
		});
		
		return false;
	} 

	void curly_Brace_Handler(String str) {
		String array[] = str.split("\n");
		String line = "";
		
		for(int i=0; i<array.length;i++) {
			line = array[i];
			System.out.println("LINE "+line);
			if(line.contains("{")) {
				open_curly_brackets_counter++;
			}
			if(line.startsWith("{")) {
				open_curly_brackets_that_are_first_character_in_a_line++;
			}
			if(line.endsWith("{")) {
				open_curly_brackets_that_are_last_character_in_a_line++;
			}
			if(line.startsWith("{") == false && line.endsWith("{") == false && line.contains("{")) {
				open_curly_brackets_that_are_middle_in_a_line++;
			}
			if(line.length()==1 && line.contains("{")) {
				open_curly_brackets_that_are_alone_in_a_line++;
			}
			
			if(line.contains("}")) {
				close_curly_brackets_counter++;
			}
			if(line.startsWith("}")) {
				close_curly_brackets_that_are_first_character_in_a_line++;
			}
			if(line.endsWith("}")) {
				close_curly_brackets_that_are_last_character_in_a_line++;
			}
			if(line.startsWith("}") == false && line.endsWith("}") == false && line.contains("{")) {
				close_curly_brackets_that_are_middle_in_a_line++;
			}
			if(line.length()==1 && line.contains("}")) {
				close_curly_brackets_that_are_alone_in_a_line++;
			}
			
			line_length.add(line.length());
		}
		
		blank_Line_Before_Close_Curly_Brackets = false;
		for(int i = array.length-1;i>=0;i++) {
			line = array[i];
			if(is_Blank_Line(line)==false) {
				break;
			}
			else {
				blank_Line_Before_Close_Curly_Brackets = true;
			}
		}
	}

	void function_Naming_Preference_Counter(String str) {
		int upper_case_counter = 0;
		int lower_case_counter = 0;
		int underscore_counter = 0;
		int digit_counter = 0;

		//		System.out.println(str);
		for(int i=0 ; i<str.length();i++) {
			if(Character.isUpperCase(str.charAt(i))) {
				upper_case_counter++;
			}
			else if(Character.isLowerCase(str.charAt(i))) {
				lower_case_counter++;
			}

			else if(Character.isDigit(str.charAt(i))) {
				digit_counter++;
			}
			else if(str.charAt(i)=='_') {
				underscore_counter++;
			}
		}
		//    	System.out.println(upper_case_counter);
		//    	System.out.println(lower_case_counter);
		//    	System.out.println(underscore_counter);
		//    	System.out.println(digit_counter);
		uppercase_characters_in_function_name.add(upper_case_counter);
		lowercase_characters_in_function_name.add(lower_case_counter);
		number_characters_in_function_name.add(digit_counter);
		underscores_characters_in_function_name.add(underscore_counter);
	}
	
	boolean is_Blank_Line(String str) {
		if (str.length()==0 || str.trim().length() == 0) {
			return true;
		}
		return false;
	}
	

	void variable_Naming_Preference_Counter(String str) {
		int upper_case_counter = 0;
		int lower_case_counter = 0;
		int underscore_counter = 0;
		int digit_counter = 0;

//		System.out.println(str);
		for(int i=0 ; i<str.length();i++) {
			if(Character.isUpperCase(str.charAt(i))) {
				upper_case_counter++;
			}
			else if(Character.isLowerCase(str.charAt(i))) {
				lower_case_counter++;
			}

			else if(Character.isDigit(str.charAt(i))) {
				digit_counter++;
			}
			else if(str.charAt(i)=='_') {
				underscore_counter++;
			}
		}
//		System.out.println(upper_case_counter);
//		System.out.println(lower_case_counter);
//		System.out.println(underscore_counter);
//		System.out.println(digit_counter);
		uppercase_characters_in_variable_name.add(upper_case_counter);
		lowercase_characters_in_variable_name.add(lower_case_counter);
		number_characters_in_variable_name.add(digit_counter);
		underscores_characters_in_variable_name.add(underscore_counter);
	}
}
