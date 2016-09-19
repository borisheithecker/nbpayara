/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.core.ui;

import com.sun.enterprise.glassfish.bootstrap.Constants;
import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.openide.modules.OnStart;
import org.openide.util.Lookup;
import org.thespheres.betula.glassfish.Domain;
import org.thespheres.betula.glassfish.config.StaticGlassFishRuntimeBuilder;

/**
 *
 * @author boris.heithecker
 */
@OnStart
public class Starter implements Runnable {

    @Override
    public void run() {
        try {
            InstanceList.init();
            String p1 = System.getProperty(Domain.INSTANCE_ROOT_PROP_NAME);
            GlassFishRuntime gfRuntime = createRuntime();
            String p2 = System.getProperty(Domain.INSTANCE_ROOT_PROP_NAME);
//            System.clearProperty(Domain.INSTANCE_ROOT_PROP_NAME);
            Listener.create(gfRuntime);
        } catch (GlassFishException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static GlassFishRuntime createRuntime() throws GlassFishException {
        BootstrapProperties bsProps = new BootstrapProperties();
        bsProps.setProperty(Constants.BUILDER_NAME_PROPERTY, StaticGlassFishRuntimeBuilder.class.getName());
        //        bsProps.setInstallRoot(System.getEnv("GF_INSTALLATION"));
        ClassLoader sysCl = Lookup.getDefault().lookup(ClassLoader.class);
        GlassFishRuntime gfRuntime = GlassFishRuntime.bootstrap(bsProps, sysCl); //GlassFishRuntime.bootstrap(bsProps);
        return gfRuntime;
    }

}
