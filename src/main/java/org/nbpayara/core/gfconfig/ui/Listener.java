/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.core.gfconfig.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.nbpayara.core.Domain;
import org.nbpayara.core.DomainInfo;
import org.openide.LifecycleManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"Listener.StartDomain.exception=Domain „{0}“ could not be started. Reason: {1}",
    "Listener.StartDomain.exception.title=Domain misconfigured."})
public class Listener implements VetoableChangeListener {

    private static final Listener INSTANCE = new Listener();
    private GlassFishRuntime runtime;
    private InstanceImpl current;
    final static RequestProcessor RP = new RequestProcessor(Listener.class);
    private boolean starting;
    private InstanceImpl stopped;

    static void create(GlassFishRuntime gfRuntime) {
        INSTANCE.start(gfRuntime);
    }

    static void shutDown() {
        if (INSTANCE.current != null) {
            INSTANCE.current.down();
        }
    }

    private void start(GlassFishRuntime gfRuntime) {
        this.runtime = gfRuntime;
        startPriorSelection();
        InstanceList.getInstance().addVetoableChangeListener(this);
    }

    public static void ensureRunning(String pUrl) throws IOException {
        INSTANCE.running(pUrl);
    }

    private void running(final String provider) throws IOException {
        try {
            boolean c = RP.submit(() -> current != null && current.running && current.activeInstance.getProviderInfo().getURL().equals(provider)).get();
            if (!c) {
                throw new IOException(provider + " is not running.");
            }
        } catch (InterruptedException | ExecutionException ex) {
            throw new IOException();
        }
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        switch (evt.getPropertyName()) {
            case InstanceList.SELECTED_DOMAIN:
                if (starting) {
                    throw new PropertyVetoException("Already starting domain.", evt);
                }
                Domain newInstance = InstanceList.findDomain((DomainInfo) evt.getNewValue());
                if (stopped != null) {
                    throw new RequireRestartException("Must restart platform after shutdown of previous glassfish domain.", evt, current);
                } else if (current == null && newInstance != null) {
                    try {
                    } catch (IllegalStateException illex) {
                        PropertyVetoException th = new PropertyVetoException("KeyStores.init threw an IllegalStateException", evt);
                        th.initCause(illex);
                        throw th;
                    }
                    RP.post(new StartDomain(newInstance));
                } else if (current != null) {
                    if (newInstance == null) {
                        current.stop();
                    } else {
                        throw new RequireRestartException("Must restart platform after shutdown of previous glassfish domain.", evt, current);
                    }
                }
                break;
            case InstanceList.LISTED_DOMAINS:
                startPriorSelection();
                break;
        }
    }

    private void startPriorSelection() {
        DomainInfo selected;
        if (current == null && !starting && (selected = InstanceList.getInstance().getSelectedDomain()) != null) {
            Domain start = InstanceList.findDomain(selected);
            if (start != null) {
                RP.post(new StartDomain(start));
            }
        }
    }

    @NbBundle.Messages({"ModuleControlImpl.restartPlatform.exception.message=Could not restart the application."})
    private static void requestRestart() throws RuntimeException, UnsupportedOperationException {
        LifecycleManager lcm = Lookup.getDefault().lookup(LifecycleManager.class);
        if (lcm != null) {
            lcm.markForRestart();
            lcm.exit();
        }
        final String msg = NbBundle.getMessage(Listener.class, "ModuleControlImpl.restartPlatform.exception.message");
        throw new RuntimeException(msg);
    }

    private class StartDomain implements Runnable {

        private final Domain domain;

        private StartDomain(Domain d) {
            Listener.this.starting = true;
            this.domain = d;
        }

        @Override
        public void run() {
            try {
                InstanceList.getInstance().updateStoppedDomains(domain.getProviderInfo(), false);
                GlassFish gf = runtime.newGlassFish(new GlassFishProperties(domain.getInstanceProperties()));
                gf.start();
                Listener.this.current = new InstanceImpl(gf, domain);
                Listener.this.current.running = true;
                Listener.this.starting = false;
                current.notifyIn.post(() -> domain.instanceStarted(current));
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    class InstanceImpl implements Runnable, Domain.Instance {

        private GlassFish glassFish;
        private final Domain activeInstance;
        private boolean running = false;
        private final RequestProcessor notifyIn;

        private InstanceImpl(GlassFish glassFish, Domain instance) {
            this.glassFish = glassFish;
            this.activeInstance = instance;
            this.notifyIn = new RequestProcessor(InstanceImpl.class.getName() + ":" + activeInstance.getProviderInfo().getURL());
        }

        @Override
        public void stop() {
            if (!running) {
                throw new IllegalStateException("Not running.");
            }
            RP.post(this, 0, Thread.NORM_PRIORITY);
        }

        void stopAndRestartPlatform() {
            Listener.RP.post(() -> {
                if (running) {
                    run();
                }
                Listener.RP.post(Listener::requestRestart, 0, Thread.MIN_PRIORITY);
            }, 0, Thread.NORM_PRIORITY);
        }

        @Override
        public void run() {
            try {
                Listener.this.current = null;
                glassFish.stop();
                running = false;
                glassFish = null;
                activeInstance.instanceStopped();
                Listener.this.stopped = this;
                InstanceList.getInstance().updateStoppedDomains(activeInstance.getProviderInfo(), true);
            } catch (GlassFishException ex) {
                Logger.getLogger(InstanceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //This is called when the platform is being shut down,
        //we do not changed active selection or stopped domain
        private void down() {
            try {
                glassFish.stop();
            } catch (GlassFishException ex) {
                Logger.getLogger(InstanceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
