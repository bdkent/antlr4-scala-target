package org.antlr.v4.codegen.scalamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

public class ScalaModels {

	private enum ContainerType {
		LIST, NELIST, OPTION
	}

	private static final ScalaModel EMPTY = new BaseScalaModel() {
		public boolean isEmpty() {
			return true;
		}
	};

	private static final List<ScalaModel> EMPTIES = Collections.singletonList(EMPTY);

	static List<ScalaModel> list(List<? extends Object> asts) {

		final List<ScalaModel> list = new ArrayList<ScalaModel>();

		for (Object ast : asts) {
			if (ast != null) {
				list.add(generate((GrammarAST) ast));
			}
		}

		list.removeAll(EMPTIES);

		return Collections.unmodifiableList(list);
	}

	static ScalaModel empty() {
		return EMPTY;
	}

	static ScalaModel choice(List<? extends Object> asts) {

		final List<ScalaModel> list = list(asts);

		if (list.isEmpty()) {
			return empty();
		} else {
			if (list.size() == 1) {
				return list.get(0);
			} else {
				return new BaseScalaModel() {
					@Override
					public boolean isChoice() {
						return true;
					}

					@Override
					public List<ScalaModel> getChoices() {
						return Collections.unmodifiableList(list);
					}
				};
			}
		}
	}

	static ScalaModel sequence(List<? extends Object> asts) {

		final List<ScalaModel> list = list(asts);

		if (list.isEmpty()) {
			return empty();
		} else {
			if (list.size() == 1) {
				return list.get(0);
			} else {
				return new BaseScalaModel() {
					@Override
					public boolean isSequence() {
						return true;
					}

					@Override
					public List<ScalaModel> getSequences() {
						return Collections.unmodifiableList(list);
					}
				};
			}
		}
	}

	static ScalaModel container(ContainerType type, GrammarAST ast) {
		final ScalaModel contained = sequence(ast.getChildren());

		if (contained.isEmpty()) {
			return empty();
		} else {

			class ContainerScalaModel extends BaseScalaModel {
				
				@Override
				public boolean isContainer() {
					return true;
				}
				@Override
				public ScalaModel getContained() {
					return contained;
				}
			}

			switch (type) {
			case LIST:
				return new ContainerScalaModel() {
					public boolean isList() {
						return true;
					}
				};

			case NELIST:
				return new ContainerScalaModel() {
					public boolean isNonEmptyList() {
						return true;
					}
				};

			case OPTION:
				return new ContainerScalaModel() {
					public boolean isOption() {
						return true;
					}
				};
			default:
				throw new UnsupportedOperationException(type.toString());
			}
		}
	}

	static ScalaModel rule(final String name) {
		if (name == null) {
			return empty();
		} else {
			return new BaseScalaModel() {
				public boolean isRule() {
					return true;
				}

				public String getRuleName() {
					return name;
				}
			};
		}
	}

	private static ScalaModel generate(GrammarAST ast) {
		if (ast instanceof RuleAST) {
			RuleAST rule = (RuleAST) ast;
			return rule(rule.getRuleName());
		} else if (ast instanceof RuleRefAST) {
			return rule(ast.getText());
		} else if (ast instanceof AltAST) {
			return sequence(ast.getChildren());
		} else if (ast instanceof BlockAST) {
			return choice(ast.getChildren());
		} else if (ast instanceof StarBlockAST) {
			return container(ContainerType.LIST, ast);
		} else if (ast instanceof PlusBlockAST) {
			return container(ContainerType.NELIST, ast);
		} else if (ast instanceof OptionalBlockAST) {
			return container(ContainerType.OPTION, ast);
		} else {
			return empty();
		}
	}

	public static Map<String, ScalaModel> generate(Grammar g) throws Exception {

		Map<String, ScalaModel> models = new LinkedHashMap<String, ScalaModel>();

		for (String ruleName : g.rules.keySet()) {
			final Rule rule = g.rules.get(ruleName);
			if (rule != null && rule.alt != null) {
				List<Object> altAsts = new ArrayList<Object>();
				for (Alternative alt : rule.alt) {
					if (alt != null) {
						if (alt.ast != null) {
							altAsts.add(alt.ast);
						}
					}
				}
				models.put(ruleName, choice(altAsts));
			}
		}

		return models;
	}
}
