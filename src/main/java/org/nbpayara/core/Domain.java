/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.core;

import java.io.IOException;
import java.util.Properties;
import org.nbpayara.core.gfconfig.ui.Listener;

/**
 *
 * @author boris.heithecker
 */
public interface Domain {

    public static final String INSTANCE_ROOT_PROP_NAME = "com.sun.aas.instanceRoot";

    //IllegalStateException, if instance cannot be started because e.g. missing licence, private key, certificate etc. 
    public Properties getInstanceProperties() throws IllegalStateException;

    //set callback interface to stop the instance
    public void instanceStarted(Instance cb);

    public void instanceStopped();

    public DomainInfo getProviderInfo();

    public static void ensureRunning(String providerUrl) throws IOException {
        Listener.ensureRunning(providerUrl);
    }

    public static interface Instance {

        public abstract void stop();
    }
}
