package se.adolfsson.dtp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.*;
import lombok.Getter;
import lombok.Setter;
import se.adolfsson.dtp.utils.api.TaintTools;
import se.adolfsson.dtp.utils.api.Taintable;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static se.adolfsson.dtp.utils.SourceSinkOrSanitizers.implementsSourceOrSinkInterface;
import static se.adolfsson.dtp.utils.SourcesSinksOrSanitizersEnum.*;

@Getter
public class SourcesSinksOrSanitizers {
	private List<SourceSinkOrSanitizers> classes;
	private List<SourceSinkOrSanitizers> interfaces;
	@Setter
	private SourcesSinksOrSanitizersEnum SourcesSinksOrSanitizersEnum;

	public static SourcesSinksOrSanitizers getSources() throws IOException {
		SourcesSinksOrSanitizers ret = getSourcesOrSinks("sources.json");
		ret.setSourcesSinksOrSanitizersEnum(SOURCES);
		return ret;
	}

	public static SourcesSinksOrSanitizers getSinks() throws IOException {
		SourcesSinksOrSanitizers ret = getSourcesOrSinks("sinks.json");
		ret.setSourcesSinksOrSanitizersEnum(SINKS);
		return ret;
	}

	public static SourcesSinksOrSanitizers getSanitizers() throws IOException {
		SourcesSinksOrSanitizers ret = getSourcesOrSinks("sanitizers.json");
		ret.setSourcesSinksOrSanitizersEnum(SANITIZERS);
		return ret;
	}

	public static CtClass isSourceSinkOrSanitizer(SourcesSinksOrSanitizers sourcesSinksOrSanitizers, String className, CtClass cClass) {
		try {
			ClassPool cp = ClassPool.getDefault();
			if (cp.get(className).isInterface()) return null;
		} catch (NotFoundException ignored) {
			return null;
		}

		String alteredAsClassName;
		if (isSourceSinkOrSanitizerClass(sourcesSinksOrSanitizers, className))
			return transform(cClass, sourcesSinksOrSanitizers, className, className);
		else if ((alteredAsClassName = usesInterface(sourcesSinksOrSanitizers, className)) != null)
			return transform(cClass, sourcesSinksOrSanitizers, className, alteredAsClassName);
		else if ((alteredAsClassName = extendsSourceOrSinkClass(sourcesSinksOrSanitizers, className)) != null)
			return transform(cClass, sourcesSinksOrSanitizers, className, alteredAsClassName);
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

	private static SourcesSinksOrSanitizers getSourcesOrSinks(String fileName) throws IOException {
		URL fileUrl = ClassLoader.getSystemClassLoader().getResource(fileName);
		ObjectMapper mapper = new ObjectMapper();

		return mapper.readValue(fileUrl, SourcesSinksOrSanitizers.class);
	}

	private static boolean isSourceSinkOrSanitizerClass(SourcesSinksOrSanitizers sourcesSinksOrSanitizers, String className) {
		return isSourceSinkOrSanitizer(sourcesSinksOrSanitizers.getClasses(), className);
	}

	private static String usesInterface(SourcesSinksOrSanitizers sourcesSinksOrSanitizers, String className) {
		boolean ret;
		for (SourceSinkOrSanitizers interfazz : sourcesSinksOrSanitizers.getInterfaces()) {
			ret = implementsSourceOrSinkInterface(interfazz.getClazz(), className);
			if (ret) return interfazz.getClazz();
		}

		return extendsSourceOrSinkInterface(sourcesSinksOrSanitizers, className);
	}

	private static String extendsSourceOrSinkInterface(SourcesSinksOrSanitizers sourcesSinksOrSanitizers, String clazz) {
		boolean ret;
		for (SourceSinkOrSanitizers interfazz : sourcesSinksOrSanitizers.getInterfaces()) {
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

	private static boolean isSourceSinkOrSanitizer(List<SourceSinkOrSanitizers> sourcesOrSinks, String className) {
		for (SourceSinkOrSanitizers source : sourcesOrSinks) {
			if (className.equals(source.getClazz())) return true;
		}

		return false;
	}

	private static CtClass transform(CtClass cClass, SourcesSinksOrSanitizers sourcesSinksOrSanitizersIn, String className, String alteredAsClassName) {
		ClassPool cp = ClassPool.getDefault();

		try {
			print("########################################");
			print("");
			print("Transforming " + (sourcesSinksOrSanitizersIn.getSourcesSinksOrSanitizersEnum() == SOURCES ? "Source: " : "" + "Sink: ") + className + (className.equals(alteredAsClassName) ? "" : " as " + alteredAsClassName));

			if (cClass == null) cClass = cp.getOrNull(className);
			if (cClass == null) {
				print("\tClass not loaded");
				print("");
				print("########################################");
				return null;
			}

			print("Methods: ");
			cClass.defrost();

			List<SourceSinkOrSanitizers> sourceSinkOrSanitizers = (cp.get(alteredAsClassName).isInterface() ? sourcesSinksOrSanitizersIn.getInterfaces() : sourcesSinksOrSanitizersIn.getClasses());

			SourceSinkOrSanitizers source = sourceSinkOrSanitizers.stream()
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
							if (sourcesSinksOrSanitizersIn.getSourcesSinksOrSanitizersEnum() == SOURCES) {
								if (returnType.subtypeOf(ClassPool.getDefault().get(Taintable.class.getName()))) {
									cp.importPackage(TaintUtils.class.getName());
									cp.importPackage(TaintTools.class.getName());
									cMethod.insertAfter("{ TaintUtils.addTaintToMethod($0, $_); }");
									print("\t\tSource Defined");
								} else print("\t\t Untaintable return type: " + returnType.getName());

							} else if (sourcesSinksOrSanitizersIn.getSourcesSinksOrSanitizersEnum() == SINKS) {
								cp.importPackage(TaintUtils.class.getName());
								cp.importPackage(TaintTools.class.getName());
								cMethod.insertBefore("{ TaintUtils.assertNonTaint($0, $args, \"" + cMethod.getSignature() + "\"); }");
								print("\t\tSink Defined");

							} else if (sourcesSinksOrSanitizersIn.getSourcesSinksOrSanitizersEnum() == SANITIZERS) {
								cp.importPackage(TaintUtils.class.getName());
								cp.importPackage(TaintTools.class.getName());
								cMethod.insertAfter("{ TaintUtils.detaintMethodReturn($_); }");
								print("\t\tSanitizer Defined");

							} else print("\t\tError in Enum");
						} else print("\t\tStatic or Native Method, can't taint");
					}
				} else print("\t\tDo not exist in class");
			}

			print("");
			print("########################################");
			print("");

			return cClass;

		} catch (NotFoundException | CannotCompileException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String extendsSourceOrSinkClass(SourcesSinksOrSanitizers sourcesSinksOrSanitizers, String className) {
		ClassPool cp = ClassPool.getDefault();

		try {
			CtClass cClass = cp.get(className);
			CtClass scClass = cClass.getSuperclass();

			if (scClass != null)
				return isSuperSourceOrSink(sourcesSinksOrSanitizers, scClass.getName());
		} catch (NotFoundException ignored) {
		}

		return null;
	}

	private static String isSuperSourceOrSink(SourcesSinksOrSanitizers sourcesSinksOrSanitizers, String className) {
		String ret;
		if (isSourceSinkOrSanitizerClass(sourcesSinksOrSanitizers, className)) return className;
		else if ((ret = usesInterface(sourcesSinksOrSanitizers, className)) != null) return ret;
		else if ((ret = extendsSourceOrSinkClass(sourcesSinksOrSanitizers, className)) != null)
			return ret;
		else return null;
	}

	private static void print(String content) {
		boolean debug = false;
		if (debug) System.out.println(content);
	}
}

