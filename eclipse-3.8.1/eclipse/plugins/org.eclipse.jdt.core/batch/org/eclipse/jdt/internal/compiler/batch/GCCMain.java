/**
 * 
 */
package org.eclipse.jdt.internal.compiler.batch;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.AccessRule;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Messages;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

/**
 * This is an alternate entry point for the command-line compiler which
 * is simpler to integrate into GCC.  In particular the option processing
 * is more GNU-like and the recognized options are similar to those supported
 * by other GCC front ends.
 */
public class GCCMain extends Main {

	// All the compilation units specified on the command line.
	private HashSet commandLineCompilationUnits = new HashSet();
	// True if we are only checking syntax.
	private boolean syntaxOnly;
	// If not null, the name of the output zip file.
	// If null, we are generating class files in the file system,
	// not a zip file.
	private String zipDestination;
	// The zip stream to which we're writing, or null if it hasn't been opened.
	private ZipOutputStream zipStream;
	
	// If not null, the name of the zip file to which dependency class files
	// should be written.
	private String zipDependencyDestination;
	// The zip stream to which dependency files should be written.
	private ZipOutputStream zipDependencyStream;

	public GCCMain(PrintWriter outWriter, PrintWriter errWriter,
			boolean systemExitWhenFinished) {
		super(outWriter, errWriter, systemExitWhenFinished);
		this.logger.setEmacs();
	}

	public GCCMain(PrintWriter outWriter, PrintWriter errWriter,
			boolean systemExitWhenFinished, Map customDefaultOptions) {
		super(outWriter, errWriter, systemExitWhenFinished,
				customDefaultOptions);
		this.logger.setEmacs();
	}

	private void fail(Exception t) {
		t.printStackTrace();
		this.logger.logException(t);
		System.exit(1);
	}

	public CompilationUnit[] getCompilationUnits() {
		CompilationUnit[] units = super.getCompilationUnits();
		for (int i = 0; i < units.length; ++i)
			this.commandLineCompilationUnits.add(units[i]);
		return units;
	}

	private String combine(char[] one, char[] two) {
		StringBuffer b = new StringBuffer();
		b.append(one);
		b.append(two);
		return b.toString();
	}

	private ZipOutputStream getZipOutput() throws IOException {
		if (this.zipDestination != null && this.zipStream == null) {
			OutputStream os;
			if ("-".equals(this.zipDestination)) { //$NON-NLS-1$
				os = System.out;
			} else {
				os = new FileOutputStream(this.zipDestination);
			}
			zipStream = new ZipOutputStream(new BufferedOutputStream(os));
			zipStream.setMethod(ZipOutputStream.STORED);
			// Sun/OpenJDK require at least one entry in the zip file.
			ZipEntry entry = new ZipEntry(".dummy");
			byte[] contents = new byte[0];
			CRC32 crc = new CRC32();
			crc.update(contents);
			entry.setSize(contents.length);
			entry.setCrc(crc.getValue());
			zipStream.putNextEntry(entry);
			zipStream.write(contents);
			zipStream.closeEntry();
		}
		return zipStream;
	}

	private ZipOutputStream getDependencyOutput() throws IOException {
		if (this.zipDependencyDestination != null && this.zipDependencyStream == null) {
			OutputStream os = new FileOutputStream(zipDependencyDestination);
			zipDependencyStream = new ZipOutputStream(new BufferedOutputStream(os));
			zipDependencyStream.setMethod(ZipOutputStream.STORED);
			// Sun/OpenJDK require at least one entry in the zip file.
			ZipEntry entry = new ZipEntry(".dummy");
			byte[] contents = new byte[0];
			CRC32 crc = new CRC32();
			crc.update(contents);
			entry.setSize(contents.length);
			entry.setCrc(crc.getValue());
			zipDependencyStream.putNextEntry(entry);
			zipDependencyStream.write(contents);
			zipDependencyStream.closeEntry();
		}
		return zipDependencyStream;
	}

	public void outputClassFiles(CompilationResult unitResult) {
		if (this.syntaxOnly) {
			return;
		}
		if (this.zipDestination == null) {
			// Nothing special to do here.
			super.outputClassFiles(unitResult);
			return;
		}
		if (unitResult == null || unitResult.hasErrors()) {
			return;
		}

		// If we are compiling with indirect dispatch, we don't need
		// any dependent classes.  If we are using the C++ ABI, then we
		// do need the dependencies in order to do proper layout.
		boolean gcjCompile = this.commandLineCompilationUnits.contains(unitResult.getCompilationUnit());
		if (this.zipDependencyDestination == null && !gcjCompile) {
			return;
		}

		try {
			ZipOutputStream dest = gcjCompile ? getZipOutput() : getDependencyOutput();
			ClassFile[] classFiles = unitResult.getClassFiles();
			for (int i = 0; i < classFiles.length; ++i) {
				ClassFile classFile = classFiles[i];
				String filename = combine(classFile.fileName(), SuffixConstants.SUFFIX_class);
				if (this.verbose)
					this.out.println(
							Messages.bind(
									Messages.compilation_write,
									new String[] {
								String.valueOf(this.exportedClassFilesCounter+1),
								filename
							}));
				ZipEntry entry = new ZipEntry(filename);
				byte[] contents = classFile.getBytes();
				CRC32 crc = new CRC32();
				crc.update(contents);
				entry.setSize(contents.length);
				entry.setCrc(crc.getValue());
				dest.putNextEntry(entry);
				dest.write(contents);
				dest.closeEntry();
			}
		} catch (IOException err) {
			fail(err);
		}
	}
	
	private String getArgument(String option) {
		int index = option.indexOf('=');
		return option.substring(index + 1);
	}

	private void addPath(ArrayList result, String currentClasspathName) {
		String customEncoding = null;
		AccessRule[] accessRules = new AccessRule[0];
		AccessRuleSet accessRuleSet = new AccessRuleSet(accessRules, AccessRestriction.COMMAND_LINE, currentClasspathName);
		FileSystem.Classpath currentClasspath = FileSystem
				.getClasspath(currentClasspathName,
						customEncoding, accessRuleSet);
		if (currentClasspath != null) {
			result.add(currentClasspath);
		}
	}
	
	private void parsePath(ArrayList result, String path) {
		StringTokenizer iter = new StringTokenizer(path, File.pathSeparator);
		while (iter.hasMoreTokens()) {
			addPath(result, iter.nextToken());
		}
	}

	protected void handleWarningToken(String token, boolean isEnabling) {
		// Recognize this for compatibility with older versions of gcj.
		if ("deprecated".equals(token)) //$NON-NLS-1$
			token = "deprecation"; //$NON-NLS-1$
		else if ("static-access".equals(token)   //$NON-NLS-1$
				|| "dep-ann".equals(token) //$NON-NLS-1$
				|| "over-ann".equals(token)) { //$NON-NLS-1$
			// Some exceptions to the warning naming rule.
		} else if ("extraneous-semicolon".equals(token)) { //$NON-NLS-1$
			// Compatibility with earlier versions of gcj.
			token = "semicolon"; //$NON-NLS-1$
		} else {
			// Turn "foo-bar-baz" into eclipse-style "fooBarBaz".
			StringBuffer newToken = new StringBuffer(token.length());
			StringTokenizer t = new StringTokenizer(token, "-"); //$NON-NLS-1$
			boolean first = true;
			while (t.hasMoreTokens()) {
				String next = t.nextToken();
				if (first) {
					newToken.append(next);
					first = false;
				} else {
					newToken.append(Character.toUpperCase(next.charAt(0)));
					newToken.append(next.substring(1));
				}
			}
			token = newToken.toString();
		}
		super.handleWarningToken(token, isEnabling);
	}

	private void turnWarningsToErrors() {
		Object[] entries = this.options.entrySet().toArray();
		for (int i = 0, max = entries.length; i < max; i++) {
			Map.Entry entry = (Map.Entry) entries[i];
			if (!(entry.getKey() instanceof String))
				continue;
			if (!(entry.getValue() instanceof String))
				continue;
			if (((String) entry.getValue()).equals(CompilerOptions.WARNING)) {
				this.options.put(entry.getKey(), CompilerOptions.ERROR);
			}
		}
	}

	/**
	 * Set the debug level to the indicated value.  The level should be
	 * between 0 and 2, inclusive, but this is not checked.
	 * @param level the debug level
	 */
	private void setDebugLevel(int level) {
		this.options.put(
				CompilerOptions.OPTION_LocalVariableAttribute,
				level > 1 ? CompilerOptions.GENERATE : CompilerOptions.DO_NOT_GENERATE);
		this.options.put(
				CompilerOptions.OPTION_LineNumberAttribute,
				level > 0 ? CompilerOptions.GENERATE : CompilerOptions.DO_NOT_GENERATE);
		this.options.put(
				CompilerOptions.OPTION_SourceFileAttribute,
				CompilerOptions.GENERATE);
	}

	private void readFileList(String file, ArrayList result) {
		try {
			BufferedReader b = new BufferedReader(new FileReader(file));
			String line;
			while ((line = b.readLine()) != null) {
				if (line.endsWith(SUFFIX_STRING_java))
					result.add(line);
			}
			b.close();
		} catch (IOException err) {
			fail(err);
		}
	}
	
	private void readAllFileListFiles(ArrayList fileList, ArrayList result) {
		Iterator it = fileList.iterator();
		while (it.hasNext()) {
			readFileList((String) it.next(), result);
		}
	}

	private void handleWall(boolean enable) {
		// A somewhat arbitrary list.  We use the GCC names
		// here, and the local handleWarningToken translates
		// for us.
		handleWarningToken("constructor-name", enable);
		handleWarningToken("pkg-default-method", enable);
		handleWarningToken("masked-catch-block", enable);
		handleWarningToken("all-deprecation", enable);
		handleWarningToken("unused-local", enable);
		handleWarningToken("unused-label", enable);
		handleWarningToken("static-receiver", enable);
		handleWarningToken("indirect-static", enable);
		handleWarningToken("no-effect-assign", enable);
		handleWarningToken("char-concat", enable);
		handleWarningToken("useless-type-check", enable);
		handleWarningToken("final-bound", enable);
		handleWarningToken("assert-identifier", enable);
		handleWarningToken("enum-identifier", enable);
		handleWarningToken("finally", enable);
		handleWarningToken("varargs-cast", enable);
		handleWarningToken("unused", enable);
		handleWarningToken("forbidden", enable);
	}

	public void configure(String[] argv) {
		if ((argv == null) || (argv.length == 0)) {
			// This is a "can't happen".
			System.exit(1);
		}

		ArrayList files = new ArrayList();
		ArrayList otherFiles = new ArrayList();
		String classpath = null;
		boolean haveFileList = false;
		boolean inhibitAllWarnings = false;
		boolean treatWarningsAsErrors = false;

		for (int i = 0; i < argv.length; ++i) {
			String currentArg = argv[i];
			
			if (currentArg.startsWith("-fencoding=")) { //$NON-NLS-1$
				// Simply accept the last one.
				String encoding = getArgument(currentArg);
				try { // ensure encoding is supported
					new InputStreamReader(new ByteArrayInputStream(new byte[0]), encoding);
				} catch (UnsupportedEncodingException e) {
					throw new IllegalArgumentException(
						this.bind("configure.unsupportedEncoding", encoding)); //$NON-NLS-1$
				}
				this.options.put(CompilerOptions.OPTION_Encoding, encoding);
			} else if (currentArg.startsWith("-foutput-class-dir=")) { //$NON-NLS-1$
				String arg = getArgument(currentArg);
				if (this.destinationPath != null) {
					StringBuffer errorMessage = new StringBuffer();
					errorMessage.append("-d"); //$NON-NLS-1$
					errorMessage.append(' ');
					errorMessage.append(arg);
					throw new IllegalArgumentException(
						this.bind("configure.duplicateOutputPath", errorMessage.toString())); //$NON-NLS-1$
				}
				this.setDestinationPath(arg);
			} else if (currentArg.startsWith("-fbootclasspath=")) { //$NON-NLS-1$
				classpath = getArgument(currentArg);
			} else if (currentArg.equals("-fzip-target")) { //$NON-NLS-1$
				++i;
				if (i >= argv.length)
					throw new IllegalArgumentException(this.bind("gcc.zipArg")); //$NON-NLS-1$
				this.zipDestination = argv[i];
			} else if (currentArg.equals("-fzip-dependency")) { //$NON-NLS-1$
				++i;
				if (i >= argv.length)
					throw new IllegalArgumentException(this.bind("gcc.zipDepArg")); //$NON-NLS-1$
				this.zipDependencyDestination = argv[i];
			} else if (currentArg.startsWith("-g")) { //$NON-NLS-1$
				if (currentArg.equals("-g0")) { //$NON-NLS-1$
					setDebugLevel(0);
				} else if (currentArg.equals("-g2") || currentArg.equals("-g3") //$NON-NLS-1$ //$NON-NLS-2$
						|| currentArg.equals("-g")) { //$NON-NLS-1$
					setDebugLevel(2);
				} else {
					// Handle -g1 but also things like -gstabs.
					setDebugLevel(1);
				}
			} else if (currentArg.equals("-Werror")) { //$NON-NLS-1$
				treatWarningsAsErrors = true;
			} else if (currentArg.equals("-Wno-error")) { //$NON-NLS-1$
				treatWarningsAsErrors = false;
			} else if (currentArg.equals("-Wall")) { //$NON-NLS-1$
				handleWall(true);
			} else if (currentArg.equals("-Wno-all")) { //$NON-NLS-1$
				handleWall(false);
			} else if (currentArg.startsWith("-Wno-")) { //$NON-NLS-1$
				handleWarningToken(currentArg.substring(5), false);
			} else if (currentArg.startsWith("-W")) { //$NON-NLS-1$
				handleWarningToken(currentArg.substring(2), true);
			} else if (currentArg.equals("-w")) { //$NON-NLS-1$
				inhibitAllWarnings = true;
			} else if (currentArg.startsWith("-O")) { //$NON-NLS-1$
				// Ignore.
			} else if (currentArg.equals("-v")) { //$NON-NLS-1$
				this.verbose = true;
			} else if (currentArg.equals("-fsyntax-only")) { //$NON-NLS-1$
				this.syntaxOnly = true;
			} else if (currentArg.startsWith("-fsource=")) { //$NON-NLS-1$
				currentArg = getArgument(currentArg);
				if (currentArg.equals("1.3")) { //$NON-NLS-1$
					this.options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
				} else if (currentArg.equals("1.4")) { //$NON-NLS-1$
					this.options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
				} else if (currentArg.equals("1.5") || currentArg.equals("5") || currentArg.equals("5.0")) { //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					this.options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
				} else if (currentArg.equals("1.6") || currentArg.equals("6") || currentArg.equals("6.0")) { //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					this.options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
				} else {
					throw new IllegalArgumentException(this.bind("configure.source", currentArg)); //$NON-NLS-1$
				}
			} else if (currentArg.startsWith("-ftarget=")) { //$NON-NLS-1$
				currentArg = getArgument(currentArg);
				if (currentArg.equals("1.1")) { //$NON-NLS-1$
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
				} else if (currentArg.equals("1.2")) { //$NON-NLS-1$
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_2);
				} else if (currentArg.equals("1.3")) { //$NON-NLS-1$
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_3);
				} else if (currentArg.equals("1.4")) { //$NON-NLS-1$
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
				} else if (currentArg.equals("1.5") || currentArg.equals("5") || currentArg.equals("5.0")) { //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
				} else if (currentArg.equals("1.6") || currentArg.equals("6") || currentArg.equals("6.0")) { //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);
				} else if (currentArg.equals("jsr14")) { //$NON-NLS-1$
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_JSR14);
				} else {
					throw new IllegalArgumentException(this.bind("configure.targetJDK", currentArg)); //$NON-NLS-1$
				}
			} else if (currentArg.equals("-ffilelist-file")) { //$NON-NLS-1$
				haveFileList = true;
			} else if (currentArg.endsWith(SuffixConstants.SUFFIX_STRING_java)) {
				files.add(currentArg);
			} else if (currentArg.charAt(0) == '-'){
				// FIXME: error if not a file?
			} else {
				otherFiles.add(currentArg);
			}
		}

		// Read the file list file.  We read them all, but really there
		// will only be one.
		if (haveFileList)
			readAllFileListFiles(otherFiles, files);

		this.filenames = (String[]) files.toArray(new String[0]);
		this.encodings = new String[this.filenames.length];
		this.destinationPaths = new String[this.filenames.length];
		for (int i = 0; i < this.filenames.length; ++i)
			this.destinationPaths[i] = this.destinationPath;
		
		// Classpath processing.
		ArrayList result = new ArrayList();
		if (classpath == null)
			throw new IllegalArgumentException(this.bind("gcc.noClasspath")); //$NON-NLS-1$
		parsePath(result, classpath);

		// We must always create both output files, even if one is not used.
		// That way we will always pass valid zip file on to jc1.
		try {
			getZipOutput();
			getDependencyOutput();
		} catch (IOException err) {
			fail(err);
		}

		if (inhibitAllWarnings)
			disableAll(ProblemSeverities.Warning);
		if (treatWarningsAsErrors)
			turnWarningsToErrors();

		this.checkedClasspaths = new FileSystem.Classpath[result.size()];
		result.toArray(this.checkedClasspaths);

		this.logger.logCommandLineArguments(argv);
		this.logger.logOptions(this.options);
		this.logger.logClasspath(this.checkedClasspaths);

		this.maxRepetition = 1;
	}

	public boolean compile(String[] argv) {
		boolean result = super.compile(argv);
		try {
			if (zipStream != null) {
				zipStream.finish();
				zipStream.close();
			}
			if (zipDependencyStream != null) {
				zipDependencyStream.finish();
				zipDependencyStream.close();
			}
		} catch (IOException err) {
			fail(err);
		}
		return result;
	}

	public static void main(String[] argv) {
		boolean result = new GCCMain(new PrintWriter(System.out), new PrintWriter(System.err), false).compile(argv);
		System.exit(result ? 0 : 1);
	}
}