package se.adolfsson.dtp.agent;

import se.adolfsson.dtp.utils.SourcesOrSinks;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static se.adolfsson.dtp.utils.SourcesOrSinks.isSourceOrSink;

public class TransformerAgent implements ClassFileTransformer {
	private SourcesOrSinks sources;
	private SourcesOrSinks sinks;

	TransformerAgent(SourcesOrSinks sources, SourcesOrSinks sinks) {
		this.sources = sources;
		this.sinks = sinks;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className,
	                        Class classBeingRedefined, ProtectionDomain protectionDomain,
	                        byte[] classfileBuffer) {

		className = className.replaceAll("/", ".");

		byte[] ret;
		if ((ret = isSourceOrSink(sources, className)) != null) return ret;
		else if ((ret = isSourceOrSink(sinks, className)) != null) return ret;
		else return null;
	}
}