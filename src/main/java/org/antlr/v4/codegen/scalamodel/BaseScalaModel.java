package org.antlr.v4.codegen.scalamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BaseScalaModel implements ScalaModel {

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean isRule() {
		return false;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public boolean isOption() {
		return false;
	}

	@Override
	public boolean isList() {
		return false;
	}

	@Override
	public boolean isNonEmptyList() {
		return false;
	}

	@Override
	public boolean isChoice() {
		return false;
	}

	@Override
	public boolean isSequence() {
		return false;
	}

	@Override
	public String getRuleName() {
		return null;
	}

	@Override
	public ScalaModel getContained() {
		return null;
	}

	@Override
	public List<ScalaModel> getSequences() {
		return Collections.emptyList();
	}

	@Override
	public List<ScalaModel> getChoices() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		if (isSequence()) {
			builder.append("( ");
			builder.append(join(getSequences(), ", "));
			builder.append(" )");
		}

		if (isChoice()) {

			final List<ScalaModel> ch = new ArrayList<ScalaModel>(getChoices());

			if (!ch.isEmpty()) {
				if (ch.size() == 1) {
					builder.append(ch.get(0));
				} else {
					builder.append("Either[");
					builder.append(join(ch.subList(0, ch.size() - 1), ",Either["));
					builder.append(",");
					builder.append(ch.get(ch.size() - 1));
					for (int i = 0; i < ch.size() - 1; i++) {
						builder.append("]");
					}
				}
			}

		}

		if (isContainer()) {
			if (isList()) {
				builder.append("List");
			} else if (isNonEmptyList()) {
				builder.append("NonEmptyList");
			} else if (isOption()) {
				builder.append("Option");
			}

			if (getContained() != null) {
				builder.append("[");
				builder.append(getContained());
				builder.append("]");
			}
		}

		if (isRule()) {
			builder.append(getRuleName());
		}

		if (isEmpty()) {
			return "String";
		}

		return builder.toString();
	}

	static <A> String join(List<A> list, String sep) {
		StringBuilder builder = new StringBuilder();
		for (Object o : list) {
			if (builder.length() > 0) {
				builder.append(sep);
			}
			builder.append(o);
		}

		return builder.toString();
	}

//	@Override
//	public final boolean isComplexChoice() {
//		return isChoice() && (getComplexChoices().size() < getChoices().size());
//	}
//
//	@Override
//	public final List<ScalaModel> getComplexChoices() {
//		if (!isChoice()) {
//			return Collections.emptyList();
//		} else {
//
//			final List<ScalaModel> complex = new ArrayList<ScalaModel>();
//			
//			final Set<ScalaModel> rules = new LinkedHashSet<ScalaModel>();
//
//			for (ScalaModel sm : getChoices()) {
//				if (!sm.isRule()) {
//					complex.add(sm);
//				}
//				else {
//					rules.add(sm);
//				}
//			}
//			
//			if(!rules.isEmpty()) {
//				complex.add(mergeRules(rules));
//			}
//
//			return Collections.unmodifiableList(complex);
//		}
//	}
//	
//	private ScalaModel mergeRules(Collection<ScalaModel> rules) {
//		StringBuilder builder = new StringBuilder();
//		for(ScalaModel sm : rules) {
//			if(sm.isRule()) {
//				builder.append(sm.getRuleName() + "Or");
//			}
//		}
//		
//		return ScalaModels.rule(builder.toString());
//	}
//	
//	@Override
//	public Set<ScalaModel> getLocalRuleChoices() {
//		if (!isChoice()) {
//			return Collections.emptySet();
//		} else {
//
//			final Set<ScalaModel> rules = new HashSet<ScalaModel>();
//
//			for (ScalaModel sm : getChoices()) {
//				if (sm.isRule()) {
//					rules.add(sm);
//				}
//			}
//
//			return Collections.unmodifiableSet(rules);
//		}
//	}
//	
//	@Override
//	public Set<Set<ScalaModel>> getDeepRuleChoices() {
//		if(isEmpty() || isRule()) {
//			return Collections.emptySet();
//		}
//		else if(isContainer()) {
//			return getContained().getDeepRuleChoices();
//		}
//		else if(isSequence()) {
//			final Set<Set<ScalaModel>> deep = new HashSet<Set<ScalaModel>>();
//			for(ScalaModel sm : getSequences()) {
//				deep.addAll(sm.getDeepRuleChoices());
//			}
//			deep.remove(Collections.emptySet());
//			return deep;
//		}
//		else if(isChoice()) {
//			if(isComplexChoice()) {
//				final Set<Set<ScalaModel>> deep = new HashSet<Set<ScalaModel>>();
//				for(ScalaModel sm : getComplexChoices()) {
//					deep.addAll(sm.getDeepRuleChoices());
//				}
//				deep.add(getLocalRuleChoices());
//				deep.remove(Collections.emptySet());
//				return deep;
//			}
//			else {
//				final Set<Set<ScalaModel>> deep = new HashSet<Set<ScalaModel>>();
//				for(ScalaModel sm : getChoices()) {
//					deep.addAll(sm.getDeepRuleChoices());
//				}
//				deep.remove(Collections.emptySet());
//				return deep;
//			}
//		}
//		else {
//			throw new UnsupportedOperationException("unknown ScalaModel");
//		}
//	}
}