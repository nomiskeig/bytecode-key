/*******************************************************************************
 * Copyright (c) 2014 Karlsruhe Institute of Technology, Germany
 *                    Technical University Darmstadt, Germany
 *                    Chalmers University of Technology, Sweden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Technical University Darmstadt - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.key_project.ui.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.key_project.core.Activator;
import org.key_project.util.eclipse.BundleUtil;
import org.key_project.util.java.IOUtil;
import org.osgi.framework.Bundle;

import de.uka.ilkd.key.core.Main;
import de.uka.ilkd.key.gui.ExampleChooser;

/**
 * Provides static methods to work with the KeY examples in the Eclipse
 * integration of KeY.
 * @author Martin Hentschel
 */
public class KeYExampleUtil {
    /**
     * The key used in the example properties to store the current version.
     */
    public static final String VERSION_KEY = "exampleVersion";
    
    /**
     * Forbid instances.
     */
    private KeYExampleUtil() {
    }
    
    /**
     * Returns a *.key with a fast and simple proof.
     * @return A *.key with a fast and simple proof.
     */
    public static File getExampleProof() {
       String exampleDir = Main.getExamplesDir();
       return new File(exampleDir, "firstTouch" + File.separator + "02-Subset" + File.separator + "project.key");
    }

    /**
     * Returns a specified example directory in bundle file "customTargets.xml".
     * This file is only available if the plug-in was loaded in a started
     * Eclipse product via the Eclipse development IDE. In a real deployed
     * product it will return {@code null}.
     * @return The local example directory or {@code null} if it is not available.
     */
    public static String getLocalExampleDirectory() {
        String localKeyHome = getLocalKeYHomeDirectory();
        return localKeyHome != null ? 
               localKeyHome + File.separator + "key" + File.separator + "key.ui" + File.separator + ExampleChooser.EXAMPLES_PATH : 
               null;
    }

    /**
     * Returns a specified KeY external library directory in bundle file "customTargets.xml".
     * This file is only available if the plug-in was loaded in a started
     * Eclipse product via the Eclipse development IDE. In a real deployed
     * product it will return {@code null}.
     * @return The local library directory or {@code null} if it is not available.
     */
    public static String getLocalKeYExtraLibsDirectory() {
        return getLocalPropertyValue("ext.dir");
    }

    /**
     * Returns a specified KeY repository home directory in bundle file "customTargets.xml".
     * This file is only available if the plug-in was loaded in a started
     * Eclipse product via the Eclipse development IDE. In a real deployed
     * product it will return {@code null}.
     * @return The local KeY repository directory or {@code null} if it is not available.
     */
    public static String getLocalKeYHomeDirectory() {
        return getLocalPropertyValue("key.rep");
    }

    /**
     * Returns a specified value in bundle file "customTargets.xml".
     * This file is only available if the plug-in was loaded in a started
     * Eclipse product via the Eclipse development IDE. In a real deployed
     * product it will return {@code null}.
     * @param key The key for that the value should be returned if possible.
     * @return The value or {@code null} if it is not available.
     */
    public static String getLocalPropertyValue(String key) {
        if (key != null) {
            Properties props = getLocalProperties();
            if (props != null) {
                String dir = props.getProperty(key);
                if (dir != null && dir.trim().length() >= 1) {
                    return dir.trim();
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    /**
     * Returns the properties in bundle file "customTargets.xml".
     * This file is only available if the plug-in was loaded in a started
     * Eclipse product via the Eclipse development IDE. In a real deployed
     * product it will return {@code null}.
     * @return The properties or {@code null} if it is not available.
     */
    public static Properties getLocalProperties() {
        try {
            if (Activator.getDefault() != null) {
                Bundle bundle = Activator.getDefault().getBundle();
                URL customTargetsURL = bundle.getEntry("customTargets.properties");
                if (customTargetsURL != null) {
                    InputStream in = null;
                    try {
                        in = customTargetsURL.openStream();
                        Properties props = new Properties();
                        props.load(in);
                        return props;
                    }
                    finally {
                        if (in != null) {
                            in.close();
                        }
                    }
                }
                else {
                    return null;
                }
            }
            else {
                return null; // Plug-in is not loaded, may used in normal Java application
            }
        }
        catch (IOException e) {
            return null; // Nothing to do.
        }
    }

    /**
     * Updates the example directory in the workspace if required. The example
     * directory is extracted from bundle and stored in the plug-in data folder
     * of this bundle together with a properties file that contains the bundle
     * version that has created the folder. If the current bundle version is
     * different to the one in the properties file the existing example
     * directory is deleted and recreated.
     * @param bundleVersion The current version
     * @param bundleId The ID of the plug-in that contains the example content.
     * @param pathInBundle The path in the plug-in to the example content.
     * @param keyExampleFile The properties file to store the bundle version in.
     * @param keyExampleDir The example directory to use.
     * @throws CoreException Occurred Exception.
     */
    public static void updateExampleDirectory(String bundleVersion,
                                               String bundleId,
                                               String pathInBundle,
                                               String keyExampleFile, 
                                               String keyExampleDir) throws CoreException {
        if (keyExampleDir != null && keyExampleFile != null) {
            // Get actual example version
            Properties properties = new Properties();
            File keyFile = new File(keyExampleFile);
            try {
                if (keyFile.exists()) {
                    properties.load(new FileReader(keyFile));
                }
            }
            catch (IOException e) {
                // Nothing to do.
            }
            if (bundleVersion != null && !bundleVersion.equals(properties.get(VERSION_KEY))) {
                // Update example version
                try {
                    properties.put(VERSION_KEY, bundleVersion);
                    properties.store(new FileOutputStream(keyFile), null);
                }
                catch (IOException e) {
                    // Nothing to do.
                }
                // Update directory.
                File dir = new File(keyExampleDir);
                IOUtil.delete(dir);
                dir.mkdirs();
                BundleUtil.extractFromBundleToFilesystem(bundleId, pathInBundle, dir);
            }
        }
    }
}