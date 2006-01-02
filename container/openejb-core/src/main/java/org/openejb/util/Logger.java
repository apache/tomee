package org.openejb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Properties;
import java.net.URL;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.openejb.loader.SystemInstance;
import org.openejb.OpenEJBException;

public class Logger {

    protected static final HashMap _loggers = new HashMap();
    protected Category       _logger  = null;
    public    I18N           i18n     = null;

    public static void initialize(Properties props)
    {
        Log4jConfigUtils log4j = new Logger.Log4jConfigUtils(props);

        log4j.configure();
    }

    static public Logger getInstance( String category, String resourceName ) {
	HashMap bundles = (HashMap)_loggers.get( category );
	Logger logger = null;

	if ( bundles == null ) {
	    synchronized (Logger.class) {
		bundles = (HashMap)_loggers.get( category );
		if ( bundles == null ) {
		    bundles = new HashMap();
		    _loggers.put( category, bundles );
		}
	    }
	}

	logger = (Logger)bundles.get( resourceName );
	if ( logger == null ) {
	    synchronized (Logger.class) {
		logger = (Logger)bundles.get( resourceName );
		if ( logger == null ) {
		    logger = new Logger( resourceName );
		    logger._logger = Category.getInstance( category );

		    bundles.put( resourceName, logger );
		}
	    }
	}

	return logger;
    }

    protected Logger( String resourceName ) {
	i18n = new I18N( resourceName );
    }

    public boolean isDebugEnabled() {
	return _logger.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
	return _logger.isEnabledFor( Level.ERROR );
    }

    public boolean isFatalEnabled() {
	return _logger.isEnabledFor( Level.FATAL );
    }

    public boolean isInfoEnabled() {
	return _logger.isInfoEnabled();
    }

    public boolean isWarningEnabled() {
	return _logger.isEnabledFor( Level.WARN );
    }

    public void debug( String message ) {
	if ( isDebugEnabled() ) _logger.debug( message );
    }

    public void debug( String message, Throwable t ) {
	if ( isDebugEnabled() ) _logger.debug( message, t );
    }

    public void error( String message ) {
	if ( isErrorEnabled() ) _logger.error( message );
    }

    public void error( String message, Throwable t ) {
	if ( isErrorEnabled() ) _logger.error( message, t );
    }

    public void fatal( String message ) {
	if ( isFatalEnabled() ) _logger.fatal( message );
    }

    public void fatal( String message, Throwable t ) {
	if ( isFatalEnabled() ) _logger.fatal( message, t );
    }

    public void info( String message ) {
	if ( isInfoEnabled() ) _logger.info( message );
    }

    public void info( String message, Throwable t ) {
	if ( isInfoEnabled() ) _logger.info( message, t );
    }

    public void warning( String message ) {
	if ( isWarningEnabled() ) _logger.warn( message );
    }

    public void warning( String message, Throwable t ) {
	if ( isWarningEnabled() ) _logger.warn( message, t );
    }

    public class I18N {

	protected Messages _messages = null;

	protected I18N( String resourceName ) {
	    _messages = new Messages( resourceName );
	}

	public void info( String code ) {
	    if ( isInfoEnabled() ) _logger.info( _messages.message( code ) );
	}

	public void info( String code, Throwable t ) {
	    if ( isInfoEnabled() ) _logger.info( _messages.message( code ), t );
	}

	public void info( String code, Object arg0 ) {
	    if ( isInfoEnabled() ) {
		Object[] args = { arg0 };
		info( code, args );
	    }
	}

	public void info( String code, Throwable t, Object arg0 ) {
	    if ( isInfoEnabled() ) {
		Object[] args = { arg0 };
		info( code, t, args );
	    }
	}

	public void info( String code, Object arg0, Object arg1 ) {
	    if ( isInfoEnabled() ) {
		Object[] args = { arg0, arg1 };
		info( code, args );
	    }
	}

	public void info( String code, Throwable t, Object arg0, Object arg1 ) {
	    if ( isInfoEnabled() ) {
		Object[] args = { arg0, arg1 };
		info( code, t, args );
	    }
	}

	public void info( String code, Object arg0, Object arg1, Object arg2 ) {
	    if ( isInfoEnabled() ) {
		Object[] args = { arg0, arg1, arg2 };
		info( code, args );
	    }
	}

	public void info( String code, Throwable t, Object arg0, Object arg1, Object arg2 ) {
	    if ( isInfoEnabled() ) {
		Object[] args = { arg0, arg1, arg2 };
		info( code, t, args );
	    }
	}

	public void info( String code, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	    if ( isInfoEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3 };
		info( code, args );
	    }
	}

	public void info( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	    if ( isInfoEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3 };
		info( code, t, args );
	    }
	}

	public void info( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	    if ( isInfoEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4 };
		info( code, args );
	    }
	}

	public void info( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	    if ( isInfoEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4 };
		info( code, t, args );
	    }
	}

	public void info( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	    if ( isInfoEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
		info( code, args );
	    }
	}

	public void info( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	    if ( isInfoEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
		info( code, t, args );
	    }
	}

	public void info( String code, Object[] args ) {
	    _logger.info( _messages.format( code, args ) );
	}

	public void info( String code, Throwable t,  Object[] args ) {
		_logger.info( _messages.format( code, args ), t );
	}

	public void warning( String code ) {
	    if ( isWarningEnabled() ) _logger.warn( _messages.message( code ) );
	}

	public void warning( String code, Throwable t ) {
	    if ( isWarningEnabled() ) _logger.warn( _messages.message( code ), t );
	}

	public void warning( String code, Object arg0 ) {
	    if ( isWarningEnabled() ) {
		Object[] args = { arg0 };
		warning( code, args );
	    }
	}

	public void warning( String code, Throwable t, Object arg0 ) {
	    if ( isWarningEnabled() ) {
		Object[] args = { arg0 };
		warning( code, t, args );
	    }
	}

	public void warning( String code, Object arg0, Object arg1 ) {
	    if ( isWarningEnabled() ) {
		Object[] args = { arg0, arg1 };
		warning( code, args );
	    }
	}

	public void warning( String code, Throwable t, Object arg0, Object arg1 ) {
	    if ( isWarningEnabled() ) {
		Object[] args = { arg0, arg1 };
		warning( code, t, args );
	    }
	}

	public void warning( String code, Object arg0, Object arg1, Object arg2 ) {
	    if ( isWarningEnabled() ) {
		Object[] args = { arg0, arg1, arg2 };
		warning( code, args );
	    }
	}

	public void warning( String code, Throwable t, Object arg0, Object arg1, Object arg2 ) {
	    if ( isWarningEnabled() ) {
		Object[] args = { arg0, arg1, arg2 };
		warning( code, t, args );
	    }
	}

	public void warning( String code, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	    if ( isWarningEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3 };
		warning( code, args );
	    }
	}

	public void warning( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	    if ( isWarningEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3 };
		warning( code, t, args );
	    }
	}

	public void warning( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	    if ( isWarningEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4 };
		warning( code, args );
	    }
	}

	public void warning( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	    if ( isWarningEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4 };
		warning( code, t, args );
	    }
	}

	public void warning( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	    if ( isWarningEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
		warning( code, args );
	    }
	}

	public void warning( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	    if ( isWarningEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
		warning( code, t, args );
	    }
	}

	public void warning( String code, Object[] args ) {
	    _logger.warn( _messages.format( code, args ) );
	}

	public void warning( String code, Throwable t, Object[] args ) {
	    _logger.warn( _messages.format( code, args ), t );
	}

	public void error( String code ) {
	    if ( isErrorEnabled() ) _logger.error( _messages.message( code ) );
	}

	public void error( String code, Throwable t ) {
	    if ( isErrorEnabled() ) _logger.error( _messages.message( code ), t );
	}

	public void error( String code, Object arg0 ) {
	    if ( isErrorEnabled() ) {
		Object[] args = { arg0 };
		error( code, args );
	    }
	}

	public void error( String code, Throwable t, Object arg0 ) {
	    if ( isErrorEnabled() ) {
		Object[] args = { arg0 };
		error( code, t, args );
	    }
	}

	public void error( String code, Object arg0, Object arg1 ) {
	    if ( isErrorEnabled() ) {
		Object[] args = { arg0, arg1 };
		error( code, args );
	    }
	}

	public void error( String code, Throwable t, Object arg0, Object arg1 ) {
	    if ( isErrorEnabled() ) {
		Object[] args = { arg0, arg1 };
		error( code, t, args );
	    }
	}

	public void error( String code, Object arg0, Object arg1, Object arg2 ) {
	    if ( isErrorEnabled() ) {
		Object[] args = { arg0, arg1, arg2 };
		error( code, args );
	    }
	}

	public void error( String code, Throwable t, Object arg0, Object arg1, Object arg2 ) {
	    if ( isErrorEnabled() ) {
		Object[] args = { arg0, arg1, arg2 };
		error( code, t, args );
	    }
	}

	public void error( String code, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	    if ( isErrorEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3 };
		error( code, args );
	    }
	}

	public void error( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	    if ( isErrorEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3 };
		error( code, t, args );
	    }
	}

	public void error( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	    if ( isErrorEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4 };
		error( code, args );
	    }
	}

	public void error( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	    if ( isErrorEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4 };
		error( code, t, args );
	    }
	}

	public void error( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	    if ( isErrorEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
		error( code, args );
	    }
	}

	public void error( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	    if ( isErrorEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
		error( code, t, args );
	    }
	}

	public void error( String code, Object[] args ) {
	    _logger.error( _messages.format( code, args ) );
	}

	public void error( String code, Throwable t, Object[] args ) {
	    _logger.error( _messages.format( code, args ), t );
	}

	public void fatal( String code ) {
	    _logger.fatal( _messages.message( code ) );
	}

	public void fatal( String code, Throwable t ) {
	    _logger.fatal( _messages.message( code ), t );
	}

	public void fatal( String code, Object arg0 ) {
	    Object[] args = { arg0 };
	    fatal( code, args );
	}

	public void fatal( String code, Throwable t, Object arg0 ) {
	    Object[] args = { arg0 };
	    fatal( code, t, args );
	}

	public void fatal( String code, Object arg0, Object arg1 ) {
	    Object[] args = { arg0, arg1 };
	    fatal( code, args );
	}

	public void fatal( String code, Throwable t, Object arg0, Object arg1 ) {
	    Object[] args = { arg0, arg1 };
	    fatal( code, t, args );
	}

	public void fatal( String code, Object arg0, Object arg1, Object arg2 ) {
	    Object[] args = { arg0, arg1, arg2 };
	    fatal( code, args );
	}

	public void fatal( String code, Throwable t, Object arg0, Object arg1, Object arg2 ) {
	    Object[] args = { arg0, arg1, arg2 };
	    fatal( code, t, args );
	}

	public void fatal( String code, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	    Object[] args = { arg0, arg1, arg2, arg3 };
	    fatal( code, args );
	}

	public void fatal( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	    Object[] args = { arg0, arg1, arg2, arg3 };
	    fatal( code, t, args );
	}

	public void fatal( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4 };
	    fatal( code, args );
	}

	public void fatal( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4 };
	    fatal( code, t, args );
	}

	public void fatal( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
	    fatal( code, args );
	}

	public void fatal( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	    Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
	    fatal( code, t, args );
	}

	public void fatal( String code, Object[] args ) {
	    _logger.fatal( _messages.format( code, args ) );
	}

	public void fatal( String code, Throwable t, Object[] args ) {
	    _logger.fatal( _messages.format( code, args ), t );
	}

	public void debug( String code ) {
	    if ( isDebugEnabled() ) _logger.debug( _messages.message( code ) );
	}

	public void debug( String code, Throwable t ) {
	    if ( isDebugEnabled() ) _logger.debug( _messages.message( code ), t );
	}

	public void debug( String code, Object arg0 ) {
	    if ( isDebugEnabled() ) {
		Object[] args = { arg0 };
		debug( code, args );
	    }
	}

	public void debug( String code, Throwable t, Object arg0 ) {
	    if ( isDebugEnabled() ) {
		Object[] args = { arg0 };
		debug( code, t, args );
	    }
	}

	public void debug( String code, Object arg0, Object arg1 ) {
	    if ( isDebugEnabled() ) {
		Object[] args = { arg0, arg1 };
		debug( code, args );
	    }
	}

	public void debug( String code, Throwable t, Object arg0, Object arg1 ) {
	    if ( isDebugEnabled() ) {
		Object[] args = { arg0, arg1 };
		debug( code, t, args );
	    }
	}

	public void debug( String code, Object arg0, Object arg1, Object arg2 ) {
	    if ( isDebugEnabled() ) {
		Object[] args = { arg0, arg1, arg2 };
		debug( code, args );
	    }
	}

	public void debug( String code, Throwable t, Object arg0, Object arg1, Object arg2 ) {
	    if ( isDebugEnabled() ) {
		Object[] args = { arg0, arg1, arg2 };
		debug( code, t, args );
	    }
	}

	public void debug( String code, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	    if ( isDebugEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3 };
		debug( code, args );
	    }
	}

	public void debug( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3 ) {
	    if ( isDebugEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3 };
		debug( code, t, args );
	    }
	}

	public void debug( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	    if ( isDebugEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4 };
		debug( code, args );
	    }
	}

	public void debug( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4 ) {
	    if ( isDebugEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4 };
		debug( code, t, args );
	    }
	}

	public void debug( String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	    if ( isDebugEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
		debug( code, args );
	    }
	}

	public void debug( String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5 ) {
	    if ( isDebugEnabled() ) {
		Object[] args = { arg0, arg1, arg2, arg3, arg4, arg5 };
		debug( code, t, args );
	    }
	}

	public void debug( String code, Object[] args ) {
	    _logger.debug( _messages.format( code, args ) );
	}

	public void debug( String code, Throwable t, Object[] args ) {
	    _logger.debug( _messages.format( code, args ), t );
	}
    }

    static class Log4jConfigUtils {

        Properties props;

        public Log4jConfigUtils(Properties props)
        {
            this.props = props;
        }

        public void configure(){
            String config = props.getProperty( "log4j.configuration" );
            if (config == null) {
                config = "conf/logging.conf";
            }
            try{

                config = getAbsolutePath(config, "conf/default.logging.conf", false);

                Properties log4jProps = loadProperties(config);

                PropertyConfigurator.configure(filterProperties(log4jProps));
            } catch (Exception e){
                System.err.println("Failed to configure log4j. "+e.getMessage());
            }
        }

        public Properties loadProperties(String file) throws Exception{
            Properties props = new Properties();
            FileInputStream fin = null;

            try{
                fin = new FileInputStream(file);
                props.load(fin);
            } finally {
                if (fin != null) fin.close();
            }
            return props;
        }

        public Properties filterProperties(Properties log4jProps) {
            Object[] names = log4jProps.keySet().toArray();
            for (int i=0; i < names.length; i++){
                String name = (String)names[i];
                if (name.endsWith(".File")) {
                    String path = log4jProps.getProperty(name);
                    try {
                        File file = SystemInstance.get().getBase().getFile(path, false);
                        if (!file.getParentFile().exists()) {
                            file = SystemInstance.get().getHome().getFile(path, false);
                        }
                        path = file.getPath();
                    } catch (IOException ignored) {

                    }
                    log4jProps.setProperty(name, path);
                }
            }
            return log4jProps;
        }

        public String getAbsolutePath(String path, String secondaryPath, boolean create)
                throws OpenEJBException {
            File file = null;

            if (path != null) {
                /*
                 * [1] Try finding the file relative to the current working
                 * directory
                 */
                file = new File(path);
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }

                /*
                 * [2] Try finding the file relative to the openejb.base directory
                 */
                try {
                    file = SystemInstance.get().getBase().getFile(path);
                    if (file != null && file.exists() && file.isFile()) {
                        return file.getAbsolutePath();
                    }
                } catch (FileNotFoundException ignored) {
                } catch (IOException ignored) {
                }

                /*
                 * [3] Try finding the file relative to the openejb.home directory
                 */
                try {
                    file = SystemInstance.get().getHome().getFile(path);
                    if (file != null && file.exists() && file.isFile()) {
                        return file.getAbsolutePath();
                    }
                } catch (FileNotFoundException ignored) {
                } catch (IOException ignored) {
                }

            }

            try {
                /*
                 * [4] Try finding the secondaryPath file relative to the
                 * openejb.base directory
                 */
                try {
                    file = SystemInstance.get().getBase().getFile(secondaryPath);
                    if (file != null && file.exists() && file.isFile()) {
                        return file.getAbsolutePath();
                    }
                } catch (java.io.FileNotFoundException ignored) {
                }

                /*
                 * [5] Try finding the secondaryPath file relative to the
                 * openejb.home directory
                 */
                try {
                    file = SystemInstance.get().getHome().getFile(secondaryPath);
                    if (file != null && file.exists() && file.isFile()) {
                        return file.getAbsolutePath();
                    }
                } catch (java.io.FileNotFoundException ignored) {
                }

                if (create)
                {
                    File confDir = SystemInstance.get().getBase().getDirectory("conf", true);

                    file = createConfig(new File(confDir, secondaryPath));
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
                throw new OpenEJBException("Could not locate config file: ", e);
            }

            return (file == null) ? null : file.getAbsolutePath();
        }

        private static File createConfig(File file) throws java.io.IOException{
            try{
                URL defaultConfig = new URL("resource:/" + file.getName());
                InputStream in = defaultConfig.openStream();
                FileOutputStream out = new FileOutputStream(file);

                int b = in.read();

                while (b != -1) {
                    out.write(b);
                    b = in.read();
                }

                in.close();
                out.close();

            } catch (Exception e){
                e.printStackTrace();
            }

            return file;
        }

    }
}