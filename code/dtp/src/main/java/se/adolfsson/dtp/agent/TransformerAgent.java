package se.adolfsson.dtp.agent;

import javassist.CannotCompileException;
import javassist.CtClass;
import se.adolfsson.dtp.utils.SourcesSinksOrSanitizers;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static se.adolfsson.dtp.utils.SourcesSinksOrSanitizers.*;

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

		try {
			CtClass ret, tmp;
			ret = isSourceSinkOrSanitizer(getSources(), className, null);
			if ((tmp = isSourceSinkOrSanitizer(getSinks(), className, ret)) != null) ret = tmp;
			if ((tmp = isSourceSinkOrSanitizer(getSanitizers(), className, ret)) != null) ret = tmp;
			if (ret != null) return ret.toBytecode();
		} catch (IOException | CannotCompileException e) {
			e.printStackTrace();
		}
		return null;
	}
}