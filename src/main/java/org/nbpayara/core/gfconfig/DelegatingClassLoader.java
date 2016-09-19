package org.nbpayara.core.gfconfig;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author boris.heithecker
 */
@Messages({"DelegatingClassLoader.loadClass.useOther=Could not load class {0} from ClassLoader {1} but from {2}"})
class DelegatingClassLoader extends ClassLoader {

    private final ClassLoader first;
    private final ClassLoader other;

    DelegatingClassLoader(ClassLoader first, ClassLoader other) {
        super(first);
        this.first = first;
        this.other = other;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return first.loadClass(name);
        } catch (ClassNotFoundException | LinkageError clex) {
            try {
                final Class<?> ret = other.loadClass(name);
                final String message = NbBundle.getMessage(DelegatingClassLoader.class, "DelegatingClassLoader.loadClass.useOther", new Object[]{name, first.toString(), other.toString()});
                Logger.getLogger(DelegatingClassLoader.class.getName())
                        .log(Level.FINE, message);
                return ret;
            } catch (ClassNotFoundException clex3) {
                throw clex3;
            }
        }
    }

    @Override
    public URL getResource(String name) {
        URL ret = first.getResource(name);
        if (ret == null) {
            return other.getResource(name);
        } else {
            return ret;
        }
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        first.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        first.setPackageAssertionStatus(packageName, enabled);
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        first.setClassAssertionStatus(className, enabled);
    }

    @Override
    public void clearAssertionStatus() {
        first.clearAssertionStatus();
    }

}
