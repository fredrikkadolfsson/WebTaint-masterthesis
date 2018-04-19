package se.adolfsson.dtp.xboot;

import javassist.*;
import se.adolfsson.dtp.utils.TaintUtils;
import se.adolfsson.dtp.utils.api.TaintException;
import se.adolfsson.dtp.utils.api.TaintTools;
import se.adolfsson.dtp.utils.api.Taintable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static se.adolfsson.dtp.utils.SourcesOrSinks.*;


/**
 * We need to prepare a modification of java.lang.Stringuilder ahead of time that we can put on the bootclasspath,
 * since we are unable to add fields to any class that the java agent itself depends on (we can't get to them before
 * they are loaded the first time).
 */
public class TaintFieldAdder {

	public static void main(String[] args) {
		System.out.println();
		System.out.println("Staring TaintFieldAdder");

		new TaintFieldAdder().run();
		System.out.println();
	}

	private void run() {
		try {
			ClassPool cp = ClassPool.getDefault();
			cp.importPackage(TaintUtils.class.getName());

			addTaintableToClass(cp, String.class.getName());
			addTaintableToClass(cp, StringBuffer.class.getName());
			addTaintableToClass(cp, StringBuilder.class.getName());

			writeClass(cp, Taintable.class.getName());
			writeClass(cp, TaintException.class.getName());
			writeClass(cp, TaintTools.class.getName());
			writeClass(cp, TaintUtils.class.getName());

			addSourcesAndSinksRT();
		} catch (IOException | CannotCompileException | NotFoundException e) {
			e.printStackTrace();
		}
	}

	private void addSourcesAndSinksRT() {
		String JREPath = System.getProperty("java.home").concat("/lib/rt.jar");

		try {
			ZipInputStream zip = new ZipInputStream(new FileInputStream(JREPath));
			for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
				if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
					String className = entry.getName().replace('/', '.').replaceAll(".class", "");

					byte[] ret;
					if ((ret = isSourceOrSink(getSources(), className)) != null) writeBytes(className, ret);
					else if ((ret = isSourceOrSink(getSinks(), className)) != null) writeBytes(className, ret);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addTaintableToClass(ClassPool cp, String className) throws NotFoundException, CannotCompileException, IOException {
		CtClass cClass = cp.get(className);
		cClass.defrost();

		cClass.addInterface(cp.get(Taintable.class.getName()));

		addTaintVar(cClass);
		addTaintMethods(cClass);
		propagateTaintInMethods(cClass);
		writeClass(cp, className);
	}

	private void addTaintVar(CtClass cClass) throws CannotCompileException {
		CtField taintField = new CtField(CtClass.booleanType, "tainted", cClass);
		taintField.setModifiers(Modifier.PRIVATE);
		cClass.addField(taintField, "TaintUtils.propagateParameterTaint($0, $args)"); // TODO:
	}

	private void addTaintMethods(CtClass cClass) throws CannotCompileException {
		cClass.addMethod(CtMethod.make("public void setTaint(boolean value){ this.tainted = value; }", cClass));
		cClass.addMethod(CtMethod.make("public boolean isTainted(){ return this.tainted; }", cClass));
	}

	private void propagateTaintInMethods(CtClass cClass) throws NotFoundException, CannotCompileException {
		CtMethod[] cMethods = cClass.getDeclaredMethods();
		for (CtMethod cMethod : cMethods) {
			if (!isStatic(cMethod) &&
					!isNative(cMethod) &&
					!isAbstract(cMethod) &&
					!cMethod.getName().equals("setTaint") &&
					!cMethod.getName().equals("isTainted")) {

				CtClass returnType = cMethod.getReturnType();

				if (returnType.subtypeOf(ClassPool.getDefault().get(Taintable.class.getName()))) {
					cMethod.insertAfter("{ $_.setTaint(TaintUtils.propagateParameterTaint($0, $args)); }");
				}

				if (cMethod.getParameterTypes().length > 0) {
					cMethod.insertBefore("{ $0.setTaint(TaintUtils.propagateParameterTaint($0, $args)); }");
				}
			}
		}
	}

	private void writeClass(ClassPool cp, String className) throws IOException, CannotCompileException, NotFoundException {
		CtClass cClass = cp.get(className);
		byte[] bytes = cClass.toBytecode();

		writeBytes(className, bytes);
	}

	private void writeBytes(String className, byte[] bytes) throws IOException {
		System.out.println("Added taint to: " + className + " " + bytes.length);

		final String s = className.replace(".", "/");
		File f = new File("build/taint/" + s + ".class");
		f.getParentFile().mkdirs();
		final FileOutputStream fos = new FileOutputStream(f);

		fos.write(bytes);
		fos.flush();
		fos.close();
	}
}
