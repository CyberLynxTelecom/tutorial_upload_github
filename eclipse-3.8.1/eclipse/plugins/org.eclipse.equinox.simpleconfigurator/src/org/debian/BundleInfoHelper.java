/*******************************************************************************
 * Copyright (c) 2012 Jakub Adam.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * The code is based on getOSGiManifest() method from
 * org.eclipse.equinox.internal.frameworkadmin.utils.Utils and
 * org.eclipse.osgi.util.ManifestElement classes. The original code copyright
 * holder is IBM Corporation.
 *******************************************************************************/

package org.debian;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.equinox.internal.simpleconfigurator.utils.BundleInfo;
import org.eclipse.equinox.internal.simpleconfigurator.utils.URIUtil;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/** 
 * In bundles.info there is a list of bundles load by Equinox simple configurator
 * on startup. This file is generated when Eclipse is built from sources and its
 * contents are fixed thereafter. This class tries to solve a situation when some
 * of orbit dependencies are upgraded by operating system's package manager.
 * 
 * The bundle's record in bundles.info contains a version number it had at the
 * time of compilation. When this number becomes different from the version of
 * that is actually installed in the system, Eclipse rejects to load the bundle
 * which leads to missing plugins or even inability to start the workbench.
 * 
 * When bundles.info is loaded by simpleconfigurator, the BundleInfoHelper class
 * inspects OSGi metadata of the installed bundles and replaces the version
 * number from bundles.info with the real one. It does this only to the runtime
 * data structures, bundles file is kept read only. After that, simpleconfigurator
 * can load the upgraded bundles.
 */
public class BundleInfoHelper {

	public static List useDebianBundleVersions(List bundles) {
		List newBundles = new ArrayList();

		ListIterator it = bundles.listIterator();
		while (it.hasNext()) {
			BundleInfo bundle = (BundleInfo) it.next();

			try {
				URI fullUri;
				if (bundle.getBaseLocation() != null)
					fullUri = new URI(bundle.getBaseLocation().toString() + bundle.getLocation().toString());
				else
					fullUri = bundle.getLocation();

				Dictionary manifest = getOSGiManifest(fullUri);

				if (manifest != null) {
					String debianVersion = (String) manifest.get(Constants.BUNDLE_VERSION);
					debianVersion = Version.parseVersion(debianVersion).toString();
					BundleInfo newBundle = new BundleInfo(bundle.getSymbolicName(), debianVersion, bundle.getLocation(), bundle.getStartLevel(), bundle.isMarkedAsStarted());
					newBundle.setBaseLocation(bundle.getBaseLocation());
					bundle = newBundle;
				}
			} catch (URISyntaxException e) {
				// Use original bundle
			}

			newBundles.add(bundle);
		}

		return newBundles;
	}

	/* Following code is copied from org.eclipse.equinox.internal.frameworkadmin.utils.Utils
	 * with some minor changes. It is here because required bundle may not be 
	 * loaded yet at the early time when useDebianBundleVersions() is run. */

	private static final String FILE_SCHEME = "file"; //$NON-NLS-1$

	private static Dictionary getOSGiManifest(URI location) {
		if (location == null)
			return null;
		// if we have a file-based URL that doesn't end in ".jar" then...
		if (FILE_SCHEME.equals(location.getScheme()))
			return basicLoadManifest(URIUtil.toFile(location));

		try {
			URL url = new URL("jar:" + location.toString() + "!/"); //$NON-NLS-1$//$NON-NLS-2$
			JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
			ZipFile jar = jarConnection.getJarFile();

			try {
				ZipEntry entry = jar.getEntry(JarFile.MANIFEST_NAME);
				if (entry == null)
					return null;

				Map manifest = parseBundleManifest(jar.getInputStream(entry), null);
				if (manifest.get(Constants.BUNDLE_SYMBOLICNAME) == null) {
					return null;
				}
				return manifestToProperties(manifest);
			} catch (Exception e) {
				return null;
			} finally {
				jar.close();
			}
		} catch (IOException e) {
			if (System.getProperty("osgi.debug") != null) {
				System.err.println("location=" + location);
				e.printStackTrace();
			}
		}
		return null;
	}

	//Return a dictionary representing a manifest. The data may result from plugin.xml conversion  
	private static Dictionary basicLoadManifest(File bundleLocation) {
		InputStream manifestStream = null;
		ZipFile jarFile = null;
		try {
			try {
				// Handle a JAR'd bundle
				if (bundleLocation.isFile()) {
					jarFile = new ZipFile(bundleLocation, ZipFile.OPEN_READ);
					ZipEntry manifestEntry = jarFile.getEntry(JarFile.MANIFEST_NAME);
					if (manifestEntry != null) {
						manifestStream = jarFile.getInputStream(manifestEntry);
					}
				} else {
					// we have a directory-based bundle
					File bundleManifestFile = new File(bundleLocation, JarFile.MANIFEST_NAME);
					if (bundleManifestFile.exists())
						manifestStream = new BufferedInputStream(new FileInputStream(new File(bundleLocation, JarFile.MANIFEST_NAME)));
				}
			} catch (IOException e) {
				//ignore
			}
			try {
				Map manifest = parseBundleManifest(manifestStream, null);
				// add this check to handle the case were we read a non-OSGi manifest
				if (manifest.get(Constants.BUNDLE_SYMBOLICNAME) == null)
					return null;
				return manifestToProperties(manifest);
			} catch (IOException ioe) {
				return null;
			} catch (Exception e) {
				return null;
			}
		} finally {
			try {
				if (manifestStream != null)
					manifestStream.close();
			} catch (IOException e1) {
				//Ignore
			}
			try {
				if (jarFile != null)
					jarFile.close();
			} catch (IOException e2) {
				//Ignore
			}
		}
	}

	private static Properties manifestToProperties(Map d) {
		Iterator iter = d.keySet().iterator();
		Properties result = new Properties();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			result.put(key, d.get(key));
		}
		return result;
	}

	// Copied from org.eclipse.osgi.util.ManifestElement

	/**
	 * Parses a bundle manifest and puts the header/value pairs into the supplied Map.
	 * Only the main section of the manifest is parsed (up to the first blank line).  All
	 * other sections are ignored.  If a header is duplicated then only the last  
	 * value is stored in the map.
	 * <p>
	 * The supplied input stream is consumed by this method and will be closed.
	 * If the supplied Map is null then a Map is created to put the header/value pairs into.
	 * </p>
	 * @param manifest an input stream for a bundle manifest.
	 * @param headers a map used to put the header/value pairs from the bundle manifest.  This value may be null.
	 * @throws Exception if the manifest has an invalid syntax
	 * @throws IOException if an error occurs while reading the manifest
	 * @return the map with the header/value pairs from the bundle manifest
	 */
	public static Map parseBundleManifest(InputStream manifest, Map headers) throws IOException, Exception {
		if (headers == null)
			headers = new HashMap();
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(manifest, "UTF8")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			br = new BufferedReader(new InputStreamReader(manifest));
		}
		try {
			String header = null;
			StringBuffer value = new StringBuffer(256);
			boolean firstLine = true;

			while (true) {
				String line = br.readLine();
				/* The java.util.jar classes in JDK 1.3 use the value of the last
				 * encountered manifest header. So we do the same to emulate
				 * this behavior. We no longer throw a BundleException
				 * for duplicate manifest headers.
				 */

				if ((line == null) || (line.length() == 0)) /* EOF or empty line */
				{
					if (!firstLine) /* flush last line */
					{
						headers.put(header, value.toString().trim());
					}
					break; /* done processing main attributes */
				}

				if (line.charAt(0) == ' ') /* continuation */
				{
					if (firstLine) /* if no previous line */
					{
						throw new Exception("The manifest line has an invalid leading space");
					}
					value.append(line.substring(1));
					continue;
				}

				if (!firstLine) {
					headers.put(header, value.toString().trim());
					value.setLength(0); /* clear StringBuffer */
				}

				int colon = line.indexOf(':');
				if (colon == -1) /* no colon */
				{
					throw new Exception("The manifest line is invalid; it has no colon '':'' character after the header key");
				}
				header = line.substring(0, colon).trim();
				value.append(line.substring(colon + 1));
				firstLine = false;
			}
		} finally {
			try {
				br.close();
			} catch (IOException ee) {
				// do nothing
			}
		}
		return headers;
	}
}
