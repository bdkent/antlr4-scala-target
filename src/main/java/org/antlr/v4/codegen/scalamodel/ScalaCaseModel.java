package org.antlr.v4.codegen.scalamodel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.tool.Grammar;

public class ScalaCaseModel {

	private final String caseName;

	private final Map<String, ScalaModel> params;

	private ScalaCaseModel(String caseName, Map<String, ScalaModel> params) {
		this.caseName = caseName;
		this.params = params;
	}

	public String getCaseName() {
		return caseName;
	}

	public Map<String, ScalaModel> getParams() {
		return params;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("case class ");
		builder.append(caseName);
		builder.append(" (");

		boolean first = true;
		for (Entry<String, ScalaModel> entry : params.entrySet()) {
			if (!first) {
				builder.append(", ");
			}
			builder.append(entry.getKey());
			builder.append(": ");
			builder.append(entry.getValue());
			first = false;
		}

		builder.append(")");

		return builder.toString();
	}

	public static List<ScalaCaseModel> generate(Grammar g) throws Exception {
		Map<String, ScalaModel> modelMap = ScalaModels.generate(g);

		List<ScalaCaseModel> caseModels = new ArrayList<ScalaCaseModel>(modelMap.size());
		for (Entry<String, ScalaModel> e : modelMap.entrySet()) {
			caseModels.add(generate(e.getKey(), e.getValue()));
		}

		return caseModels;
	}

	private static ScalaCaseModel generate(String name, ScalaModel model) {
		final Map<String, ScalaModel> paramMap = new LinkedHashMap<String, ScalaModel>();

		if (model.isEmpty()) {
			paramMap.put(name(model), model);
		} else if (model.isSequence()) {
			int i = 1;
			for (ScalaModel sm : model.getSequences()) {
				final String paramName = name(sm);
				if (paramMap.containsKey(paramName)) {
					paramMap.put(paramName + i, sm);
					i++;
				} else {
					paramMap.put(paramName, sm);
				}
			}
		} else if (model.isChoice()) {
			paramMap.put(name(model), model);
		} else {
			paramMap.put(name(model), model);
		}

		return new ScalaCaseModel(name, paramMap);
	}

	private static String name(ScalaModel model) {
		if (model.isEmpty()) {
			return "value";
		} else if (model.isRule()) {
			return model.getRuleName();
		} else if (model.isContainer()) {
			return name(model.getContained());
		}

		return "ast";
	}
}
