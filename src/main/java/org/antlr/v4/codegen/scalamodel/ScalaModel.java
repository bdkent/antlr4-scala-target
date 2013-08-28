package org.antlr.v4.codegen.scalamodel;

import java.util.List;

public interface ScalaModel {
	
	boolean isEmpty();
	
	boolean isRule();
	
	boolean isContainer();
	
	boolean isOption();
	
	boolean isList();
	
	boolean isNonEmptyList();
	
	boolean isChoice();
	
//	boolean isComplexChoice();
	
	boolean isSequence();
	
	String getRuleName();
	
	ScalaModel getContained();
	
	List<ScalaModel> getSequences();
	
	List<ScalaModel> getChoices();
	
//	List<ScalaModel> getComplexChoices();
//	
//	Set<ScalaModel> getLocalRuleChoices();
//	
//	Set<Set<ScalaModel>> getDeepRuleChoices();
}
