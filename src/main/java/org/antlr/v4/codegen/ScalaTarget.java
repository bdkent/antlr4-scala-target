package org.antlr.v4.codegen;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.antlr.v4.codegen.scalamodel.ScalaCaseModel;
import org.antlr.v4.codegen.scalamodel.ScalaModel;
import org.antlr.v4.codegen.scalamodel.ScalaModels;
import org.antlr.v4.tool.Alternative;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.AltAST;
import org.antlr.v4.tool.ast.BlockAST;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.OptionalBlockAST;
import org.antlr.v4.tool.ast.PlusBlockAST;
import org.antlr.v4.tool.ast.RuleAST;
import org.antlr.v4.tool.ast.RuleRefAST;
import org.antlr.v4.tool.ast.StarBlockAST;
import org.antlr.v4.tool.ast.TerminalAST;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.StringRenderer;

public class ScalaTarget extends Target {

	protected static final String[] javaKeywords = { "abstract", "assert",
			"boolean", "break", "byte", "case", "catch", "char", "class",
			"const", "continue", "default", "do", "double", "else", "enum",
			"extends", "false", "final", "finally", "float", "for", "if",
			"implements", "import", "instanceof", "int", "interface", "long",
			"native", "new", "null", "package", "private", "protected",
			"public", "return", "short", "static", "strictfp", "super",
			"switch", "synchronized", "this", "throw", "throws", "transient",
			"true", "try", "void", "volatile", "while",

			"def", "forSome", "implicit", "lazy", "match", "object",
			"override", "sealed", "trait", "type", "val", "var", "with",
			"yield", };

	/** Avoid grammar symbols in this set to prevent conflicts in gen'd code. */
	protected final Set<String> badWords = new HashSet<String>();

	public ScalaTarget(CodeGenerator gen) {
		super(gen, "Scala");
	}

	public Set<String> getBadWords() {
		if (badWords.isEmpty()) {
			addBadWords();
		}

		return badWords;
	}

	protected void addBadWords() {
		badWords.addAll(Arrays.asList(javaKeywords));
		badWords.add("rule");
		badWords.add("parserRule");
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * For Java, this is the translation {@code 'a\n"'} &rarr; {@code "a\n\""}.
	 * Expect single quotes around the incoming literal. Just flip the quotes
	 * and replace double quotes with {@code \"}.
	 * <p/>
	 * Note that we have decided to allow people to use '\"' without penalty, so
	 * we must build the target string in a loop as {@link String#replace}
	 * cannot handle both {@code \"} and {@code "} without a lot of messing
	 * around.
	 */
	@Override
	public String getTargetStringLiteralFromANTLRStringLiteral(
			CodeGenerator generator, String literal, boolean addQuotes) {
		StringBuilder sb = new StringBuilder();
		String is = literal;

		if (addQuotes)
			sb.append('"');

		for (int i = 1; i < is.length() - 1; i++) {
			if (is.charAt(i) == '\\') {
				// Anything escaped is what it is! We assume that
				// people know how to escape characters correctly. However
				// we catch anything that does not need an escape in Java (which
				// is what the default implementation is dealing with and remove
				// the escape. The C target does this for instance.
				//
				switch (is.charAt(i + 1)) {
				// Pass through any escapes that Java also needs
				//
				case '"':
				case 'n':
				case 'r':
				case 't':
				case 'b':
				case 'f':
				case '\\':
					// Pass the escape through
					sb.append('\\');
					break;

				case 'u': // Assume unnnn
					// Pass the escape through as double \\
					// so that Java leaves as \u0000 string not char
					sb.append('\\');
					sb.append('\\');
					break;

				default:
					// Remove the escape by virtue of not adding it here
					// Thus \' becomes ' and so on
					break;
				}

				// Go past the \ character
				i++;
			} else {
				// Characters that don't need \ in ANTLR 'strings' but do in
				// Java
				if (is.charAt(i) == '"') {
					// We need to escape " in Java
					sb.append('\\');
				}
			}
			// Add in the next character, which may have been escaped
			sb.append(is.charAt(i));
		}

		if (addQuotes)
			sb.append('"');

		return sb.toString();
	}

	@Override
	public String encodeIntAsCharEscape(int v) {
		if (v < Character.MIN_VALUE || v > Character.MAX_VALUE) {
			throw new IllegalArgumentException(String.format(
					"Cannot encode the specified value: %d", v));
		}

		if (v >= 0 && v < targetCharValueEscape.length
				&& targetCharValueEscape[v] != null) {
			return targetCharValueEscape[v];
		}

		if (v >= 0x20 && v < 127
				&& (!Character.isDigit(v) || v == '8' || v == '9')) {
			return String.valueOf((char) v);
		}

		if (v >= 0 && v <= 127) {
			String oct = Integer.toOctalString(v);
			return "\\" + oct;
		}

		String hex = Integer.toHexString(v | 0x10000).substring(1, 5);
		return "\\u" + hex;
	}

	@Override
	public int getSerializedATNSegmentLimit() {
		// 65535 is the class file format byte limit for a UTF-8 encoded string
		// literal
		// 3 is the maximum number of bytes it takes to encode a value in the
		// range 0-0xFFFF
		return 65535 / 3;
	}

	@Override
	protected boolean visibleGrammarSymbolCausesIssueInGeneratedCode(
			GrammarAST idNode) {
		return getBadWords().contains(idNode.getText());
	}

	@Override
	protected STGroup loadTemplates() {
		STGroup result = super.loadTemplates();
		result.registerRenderer(String.class, new JavaStringRenderer(), true);
		return result;
	}
	
	@Override
	protected void genFile(Grammar g, ST outputFileST, String fileName) {
		if(fileName.toLowerCase().contains("parser")) {
			if(outputFileST.getAttributes().containsKey("namedActions")) {
				final Object attr = outputFileST.getAttribute("namedActions");
				if(attr instanceof Map) {
					try {
//						final Map<String,ScalaModel> map = ScalaModels.generate(g);
//						for(Entry<String,ScalaModel> e : map.entrySet()) {
//							System.out.println(e.getKey() + " = " + e.getValue() + "\n");
//						}
						List<ScalaCaseModel> model = ScalaCaseModel.generate(g);
						for(ScalaCaseModel m : model) {
							System.out.println(m + "\n");
						}

						((Map)attr).put("__MAGIC_SCALA_TARGET_MODEL__", model);
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		
		super.genFile(g, outputFileST, fileName);
	}
	
	private void tester(Grammar g) throws Exception {
		

//		final Set<Integer> visited = new HashSet<Integer>();
		for (String ruleName : g.rules.keySet()) {
			final Rule rule = g.rules.get(ruleName);
			if (rule != null && rule.alt != null) {
				System.out.print(ruleName + " = ");
				for (Alternative alt : rule.alt) {
					if(alt != null) {
						if(alt.ast != null) {
							tester(alt.ast);
						}
					}
				}
				System.out.println("\n");
			}
		}
	}
	
	private void tester(GrammarAST ast) {
		if (ast instanceof RuleAST) {
			RuleAST rule = (RuleAST) ast;
			System.out.print(rule.getRuleName());
		} else if (ast instanceof RuleRefAST) {
			RuleRefAST ruleRef = (RuleRefAST) ast;
			System.out.print(" " + ruleRef.getText());
		} else if (ast instanceof AltAST) {
			tester(ast.getChildren());
			System.out.print(" | ");
		} else if (ast instanceof StarBlockAST) {
			System.out.print(" List[");
			tester(ast.getChildren());
			System.out.print("]");
		} else if (ast instanceof PlusBlockAST) {
			System.out.print(" NonEmptyList[");
			tester(ast.getChildren());
			System.out.print("]");
		} else if (ast instanceof BlockAST) {
			System.out.print(" (");
			tester(ast.getChildren());
			System.out.print(")");
		} else if (ast instanceof OptionalBlockAST) {
			System.out.print(" Option[");
			tester(ast.getChildren());
			System.out.print("]");
		} else if (ast instanceof TerminalAST) {
			// SKIP
		} else {
			System.out.println(ast.getClass());
		}
	}
	
	private void tester(List<? extends Object> asts) {
		for(Object ast : asts) {
			tester((GrammarAST)ast);
		}
	}

	protected static class JavaStringRenderer extends StringRenderer {

		@Override
		public String toString(Object o, String formatString, Locale locale) {
			if ("java-escape".equals(formatString)) {
				// 5C is the hex code for the \ itself
				return ((String) o).replace("\\u", "\\u005Cu");
			}

			return super.toString(o, formatString, locale);
		}

	}
}
