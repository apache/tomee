package jug.client.util;

import jug.client.command.api.Command;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.UrlSet;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandManager {
    private static final Logger LOGGER = Logger.getLogger(CommandManager.class.getName());
    private static final Map<String, Class<?>> COMMANDS = new TreeMap<String, Class<?>>();

    static {
        final ClassLoader loader = CommandManager.class.getClassLoader();
        try {
            UrlSet urlSet = new UrlSet(loader);
            urlSet = urlSet.exclude(loader.getParent());

            final IAnnotationFinder finder = new AnnotationFinder(new ConfigurableClasspathArchive(loader, urlSet.getUrls()));
            for (Annotated<Class<?>> cmd : finder.findMetaAnnotatedClasses(Command.class)) {
                try {
                    final Command annotation = cmd.getAnnotation(Command.class);
                    final String key = annotation.name();
                    if (!COMMANDS.containsKey(key)) {
                        COMMANDS.put(key, cmd.get());
                    } else {
                        LOGGER.warning("command " + key + " already exists, this one will be ignored ( " + annotation.description() + ")");
                    }
                } catch (Exception e) {
                    // command ignored
                }
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "an error occured while getting commands", e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "can't get commands");
        }
    }

    public static Map<String, Class<?>> getCommands() {
        return COMMANDS;
    }

    public static int size() {
        return COMMANDS.size();
    }

    public static Set<String> keys() {
        return COMMANDS.keySet();
    }
}
