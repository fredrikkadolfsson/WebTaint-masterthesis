package se.adolfsson.dtp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.*;
import lombok.Getter;
import lombok.Setter;
import se.adolfsson.dtp.utils.api.Taintable;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static se.adolfsson.dtp.utils.SourceOrSink.implementsSourceOrSinkInterface;
import static se.adolfsson.dtp.utils.SourcesOrSinksEnum.SINKS;
import static se.adolfsson.dtp.utils.SourcesOrSinksEnum.SOURCES;

@Getter
public class SourcesOrSinks {
	private List<SourceOrSink> classes;
	private List<SourceOrSink> interfaces;
	@Setter
	private SourcesOrSinksEnum SourcesOrSinksEnum;

	public static SourcesOrSinks getSources() throws IOException {
		SourcesOrSinks ret = getSourcesOrSinks("sources.json");
		ret.setSourcesOrSinksEnum(SOURCES);
		return ret;
	}

	public static SourcesOrSinks getSinks() throws IOException {
		SourcesOrSinks ret = getSourcesOrSinks("sinks.json");
		ret.setSourcesOrSinksEnum(SINKS);
		return ret;
	}

	public static byte[] isSourceOrSink(SourcesOrSinks sourcesOrSinks, String className) {
		try {
			ClassPool cp = ClassPool.getDefault();
			if (cp.get(className).isInterface()) return null;
		} catch (NotFoundException ignored) {
			return null;
		}

		String ret;
		if (isSourceOrSinkClass(sourcesOrSinks, className)) return transform(sourcesOrSinks, className, className);
		else if ((ret = usesInterface(sourcesOrSinks, className)) != null) return transform(sourcesOrSinks, className, ret);
		else if ((ret = extendsSourceOrSinkClass(sourcesOrSinks, className)) != null)
			return transform(sourcesOrSinks, className, ret);
		else return null;
	}

	public static boolean isNative(CtMethod method) {
		return Modifier.isNative(method.getModifiers());
	}

	public static boolean isStatic(CtMethod method) {
		return Modifier.isStatic(method.getModifiers());
	}

	public static boolean isAbstract(CtMethod method) {
		return Modifier.isAbstract(method.getModifiers());
	}

	private static SourcesOrSinks getSourcesOrSinks(String fileName) throws IOException {
		URL fileUrl = ClassLoader.getSystemClassLoader().getResource(fileName);
		ObjectMapper mapper = new ObjectMapper();

		return mapper.readValue(fileUrl, SourcesOrSinks.class);
	}

	private static boolean isSourceOrSinkClass(SourcesOrSinks sourcesOrSinks, String className) {
		return isSourceOrSink(sourcesOrSinks.getClasses(), className);
	}

	private static String usesInterface(SourcesOrSinks sourcesOrSinks, String className) {
		boolean ret;
		for (SourceOrSink interfazz : sourcesOrSinks.getInterfaces()) {
			ret = implementsSourceOrSinkInterface(interfazz.getClazz(), className);
			if (ret) return interfazz.getClazz();
		}

		return extendsSourceOrSinkInterface(sourcesOrSinks, className);
	}

	private static String extendsSourceOrSinkInterface(SourcesOrSinks sourcesOrSinks, String clazz) {
		boolean ret;
		for (SourceOrSink interfazz : sourcesOrSinks.getInterfaces()) {
			ClassPool cp = ClassPool.getDefault();

			try {
				CtClass cClass = cp.get(clazz);
				CtClass ecClass = cp.get(interfazz.getClazz());

				ret = cClass.subtypeOf(ecClass);

				if (ret) return interfazz.getClazz();
			} catch (NotFoundException ignored) {
				// ignore
			}
		}
		return null;
	}

	private static boolean isSourceOrSink(List<SourceOrSink> sourcesOrSinks, String className) {
		for (SourceOrSink source : sourcesOrSinks) {
			if (className.equals(source.getClazz())) return true;
		}

		return false;
	}

	private static byte[] transform(SourcesOrSinks sourcesOrSinksIn, String className, String alteredAsClassName) {
		ClassPool cp = ClassPool.getDefault();

		try {
			print("########################################");
			print("");
			print("Transforming " + (sourcesOrSinksIn.getSourcesOrSinksEnum() == SOURCES ? "Source: " : "" + "Sink: ") + className + (className.equals(alteredAsClassName) ? "" : " as " + alteredAsClassName));

			CtClass cClass = cp.getOrNull(className);
			if (cClass == null) {
				print("\tClass not loaded");
				print("");
				print("########################################");
				return null;
			}

			print("Methods: ");
			cClass.defrost();

			List<SourceOrSink> sourceOrSink = (cp.get(alteredAsClassName).isInterface() ? sourcesOrSinksIn.getInterfaces() : sourcesOrSinksIn.getClasses());

			SourceOrSink source = sourceOrSink.stream()
					.filter(
							src -> src.getClazz().equals(alteredAsClassName)
					).findFirst().get();

			String[] methods = source.getMethods();

			if (methods[0].equals("*")) {
				CtMethod[] cMethods = cClass.getDeclaredMethods();
				methods = new String[cMethods.length];

				int idx = 0;
				for (CtMethod cMethod : cMethods) {
					methods[idx++] = cMethod.getName();
				}
			}

			for (String method : methods) {
				print("\t" + method);

				CtMethod[] cMethods = cClass.getDeclaredMethods(method);

				if (cMethods.length > 0) {
					for (CtMethod cMethod : cMethods) {
						if (!isStatic(cMethod) &&
								!isNative(cMethod) &&
								!isAbstract(cMethod)) {
							CtClass returnType = cMethod.getReturnType();
							if (sourcesOrSinksIn.getSourcesOrSinksEnum() == SOURCES) {
								if (returnType.subtypeOf(ClassPool.getDefault().get(Taintable.class.getName()))) {
									cp.importPackage(TaintUtils.class.getName());
									cMethod.insertAfter("{ TaintUtils.propagateMethodTaint($0, $_); }");
									print("\t\tSource Defined");
								} else print("\t\t Untaintable return type: " + returnType.getName());
							} else {
								cp.importPackage(TaintUtils.class.getName());
								cMethod.insertBefore("{ TaintUtils.checkMethodTaint($0, $args, \"" + cMethod.getSignature() + "\"); }");
								print("\t\tSink Defined");
							}
						} else print("\t\tStatic or Native Method, can't taint");
					}
				} else print("\t\tDo not exist in class");
			}

			print("");
			print("########################################");
			print("");

			return cClass.toBytecode();

		} catch (NotFoundException | IOException | CannotCompileException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String extendsSourceOrSinkClass(SourcesOrSinks sourcesOrSinks, String className) {
		ClassPool cp = ClassPool.getDefault();

		try {
			CtClass cClass = cp.get(className);
			CtClass scClass = cClass.getSuperclass();

			if (scClass != null) return isSuperSourceOrSink(sourcesOrSinks, scClass.getName());
		} catch (NotFoundException ignored) {
		}

		return null;
	}

	private static String isSuperSourceOrSink(SourcesOrSinks sourcesOrSinks, String className) {
		String ret;
		if (isSourceOrSinkClass(sourcesOrSinks, className)) return className;
		else if ((ret = usesInterface(sourcesOrSinks, className)) != null) return ret;
		else if ((ret = extendsSourceOrSinkClass(sourcesOrSinks, className)) != null) return ret;
		else return null;
	}

	private static void print(String content) {
		boolean debug = false;
		if (debug) System.out.println(content);
	}
}

