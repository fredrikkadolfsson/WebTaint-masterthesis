package se.adolfsson.dtp.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import static se.adolfsson.dtp.utils.SourcesSinksOrSanitizers.*;

public class TaintAgent {
	public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("Executing taint premain.........");
		System.out.println();
		try {
			inst.addTransformer(new TransformerAgent(getSources(), getSinks(), getSanitizers()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}