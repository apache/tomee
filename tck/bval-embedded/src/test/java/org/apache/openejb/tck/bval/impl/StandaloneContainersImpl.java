package org.apache.openejb.tck.bval.impl;

import org.apache.openejb.tck.impl.Archive;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

/**
 * inspired from the hibernate implementation.
 *
 * @author Romain Manni-Bucau
 */
public class StandaloneContainersImpl extends org.apache.openejb.tck.impl.StandaloneContainersImpl {
    @Override public boolean deploy(Collection<Class<?>> classes, Collection<URL> xmls) {
        if ( xmls == null || !xmls.iterator().hasNext() ) {
			Thread.currentThread().setContextClassLoader( new IgnoringValidationXmlClassLoader() );
			return true;
		}

		URL validationXmlUrl = xmls.iterator().next();
		Thread.currentThread().setContextClassLoader( new CustomValidationXmlClassLoader( validationXmlUrl.getPath() ) );

        return super.deploy(classes, xmls);
    }

    private static class CustomValidationXmlClassLoader extends ClassLoader {
		private final String customValidationXmlPath;

		CustomValidationXmlClassLoader(String pathToCustomValidationXml) {
			super( CustomValidationXmlClassLoader.class.getClassLoader() );
			customValidationXmlPath = pathToCustomValidationXml;
		}

		public InputStream getResourceAsStream(String path) {
			InputStream in;
			if ( "META-INF/validation.xml".equals( path ) ) {
				if ( customValidationXmlPath.contains( ".jar!" ) ) {
					path = customValidationXmlPath.substring( customValidationXmlPath.indexOf( "!" ) + 2 );
					in = super.getResourceAsStream( path );
				}
				else {
					in = loadFromDisk();
				}
			}
			else {
				in = super.getResourceAsStream( path );
			}
			return in;
		}

		private InputStream loadFromDisk() {
			InputStream in;
			try {
				in = new BufferedInputStream( new FileInputStream( customValidationXmlPath ) );
			}
			catch (IOException ioe) {
				String msg = "Unble to load " + customValidationXmlPath + " from  disk";
				throw new RuntimeException( msg );
			}
			return in;
		}
	}

	private static class IgnoringValidationXmlClassLoader extends ClassLoader {
		IgnoringValidationXmlClassLoader() {
			super( IgnoringValidationXmlClassLoader.class.getClassLoader() );
		}

		public InputStream getResourceAsStream(String path) {
			if ( "META-INF/validation.xml".equals( path ) ) {
				return null;
			}
			return super.getResourceAsStream( path );
		}
	}
}
