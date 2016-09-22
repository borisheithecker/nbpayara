/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.core.gfconfig.uix;

import com.sun.enterprise.glassfish.bootstrap.Constants;
import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.nbpayara.core.gfconfig.NbGlassFishRuntimeBuilder;
import org.openide.modules.OnStart;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
@OnStart
public class Starter implements Runnable {

    @Override
    public void run() {
        try {
            GlassFishRuntime gfRuntime = createRuntime();
            Listener.create(gfRuntime);
        } catch (GlassFishException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static GlassFishRuntime createRuntime() throws GlassFishException {
        BootstrapProperties bsProps = new BootstrapProperties();
        bsProps.setProperty(Constants.BUILDER_NAME_PROPERTY, NbGlassFishRuntimeBuilder.class.getName());
        ClassLoader sysCl = Lookup.getDefault().lookup(ClassLoader.class);
        return GlassFishRuntime.bootstrap(bsProps, sysCl); 
    }

}
