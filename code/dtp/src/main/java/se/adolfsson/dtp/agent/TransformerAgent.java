package se.adolfsson.dtp.agent;

import se.adolfsson.dtp.utils.SourcesSinksOrSanitizers;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static se.adolfsson.dtp.utils.SourcesSinksOrSanitizers.isSourceOrSink;

public class TransformerAgent implements ClassFileTransformer {
	private final SourcesSinksOrSanitizers sources;
	private final SourcesSinksOrSanitizers sinks;
	private final SourcesSinksOrSanitizers sanitizers;

	TransformerAgent(SourcesSinksOrSanitizers sources, SourcesSinksOrSanitizers sinks, SourcesSinksOrSanitizers sanitizers) {
		this.sources = sources;
		this.sinks = sinks;
		this.sanitizers = sanitizers;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className,
	                        Class classBeingRedefined, ProtectionDomain protectionDomain,
	                        byte[] classfileBuffer) {

		className = className.replaceAll("/", ".");

		byte[] ret;
		if ((ret = isSourceOrSink(sources, className)) != null) return ret;
		else if ((ret = isSourceOrSink(sinks, className)) != null) return ret;
		else if ((ret = isSourceOrSink(sanitizers, className)) != null) return ret;
		else return null;
	}
}