/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.core.ui;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbPreferences;
import org.thespheres.betula.glassfish.Domain;
import org.thespheres.betula.services.ProviderInfo;

/**
 *
 * @author boris.heithecker
 */
final class InstanceList implements LookupListener {

    final static String SELECTED_DOMAIN = "org.thespheres.betula.glassfish.selected.domain";
    final static String STOPPED_DOMAINS = "org.thespheres.betula.glassfish.stopped.domains";
    final static String LISTED_DOMAINS = "org.thespheres.betula.glassfish.listed.domains";
    private final static InstanceList INSTANCE = new InstanceList();
    private final Lookup.Result<Domain> result;
    private final VetoableChangeSupport propertyChangeSupport = new VetoableChangeSupport(this);
    private final ArrayList<ProviderInfo> domains = new ArrayList<>();//Comparator

    @SuppressWarnings("LeakingThisInConstructor")
    private InstanceList() {
        result = Lookup.getDefault().lookupResult(Domain.class);
        result.addLookupListener(this);
        initImpl();
    }

    static InstanceList getInstance() {
        return INSTANCE;
    }

    static void init() {
//        INSTANCE.initImpl();
    }

    private synchronized void initImpl() {
        ArrayList<ProviderInfo> dd = new ArrayList<>();
        Set<String> stopped = getStoppedDomains();
        List<Domain> toStop = new ArrayList<>();
        String selected = NbPreferences.forModule(getClass()).get(SELECTED_DOMAIN, null);
        for (Domain d : result.allInstances()) {
            ProviderInfo pi = d.getProviderInfo();
            dd.add(pi);
            if (!stopped.contains(pi.getURL()) && !(selected != null && selected.equals(pi.getURL()))) {
                toStop.add(d);
            }
        }
        synchronized (domains) {
            domains.clear();
            domains.addAll(dd);
        }
        toStop.stream()
                .forEach(d -> Listener.RP.post(new StopDomain(d)));
    }

    public ProviderInfo[] getDomains() {
        return domains.stream()
                .toArray(ProviderInfo[]::new);
    }

    ProviderInfo getSelectedDomain() {
        String provider = NbPreferences.forModule(getClass()).get(SELECTED_DOMAIN, null);
        if (provider != null) {
            return findProviderInfo(provider);
        }
        return null;
    }

    ProviderInfo findProviderInfo(String url) {
        for (ProviderInfo pi : domains) {
            if (pi.getURL().equals(url)) {
                return pi;
            }
        }
        return null;
    }

    private Domain findDomainImpl(ProviderInfo pi) {
        if (pi != null) {
            for (Domain d : result.allInstances()) {
                if (d.getProviderInfo().getURL().equals(pi.getURL())) {
                    return d;
                }
            }
        }
        return null;
    }

    public void setSelectedDomain(final ProviderInfo domain) throws PropertyVetoException {
        assert domain == null || domains.contains(domain);
        ProviderInfo old = getSelectedDomain();
        try {
            propertyChangeSupport.fireVetoableChange(SELECTED_DOMAIN, old, domain);
        } catch (RequireRestartException rsex) {
            RequireRestartException.maybeRestartPlatform(rsex.getPropertyChangeEvent());
            rsex.enqueForStopAndPlatformRestart();
        }
        updateSelectedDomain(domain);
    }

    private void updateSelectedDomain(final ProviderInfo domain) {
        if (domain != null) {
            NbPreferences.forModule(getClass()).put(SELECTED_DOMAIN, domain.getURL());
            updateStoppedDomains(domain, false);
        } else {
            NbPreferences.forModule(getClass()).remove(SELECTED_DOMAIN);
        }
    }

    Set<String> getStoppedDomains() {
        String v = NbPreferences.forModule(getClass()).get(STOPPED_DOMAINS, null);
        if (v != null) {
            return Arrays.asList(v.split(",")).stream().collect(Collectors.toSet());
        }
        return Collections.EMPTY_SET;
    }

    synchronized void updateStoppedDomains(final ProviderInfo domain, boolean add) {
        StringJoiner sj = new StringJoiner(",");
        for (String v : getStoppedDomains()) {
            if (domain.getURL().equals(v)) {
                if (add) {
                    //Already contained
                    return;
                }
            } else {
                sj.add(v);
            }
        }
        if (add) {
            sj.add(domain.getURL());
        }
        NbPreferences.forModule(getClass()).put(STOPPED_DOMAINS, sj.toString());
    }

    void addVetoableChangeListener(VetoableChangeListener listener) {
        propertyChangeSupport.addVetoableChangeListener(listener);
    }

    void removeVetoableChangeListener(VetoableChangeListener listener) {
        propertyChangeSupport.removeVetoableChangeListener(listener);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        initImpl();
        try {
            propertyChangeSupport.fireVetoableChange(LISTED_DOMAINS, null, null);
        } catch (PropertyVetoException ex) {
        }
    }

    static Domain findDomain(ProviderInfo provider) {
        return INSTANCE.findDomainImpl(provider);
    }

    private class StopDomain implements Runnable {

        private final Domain domain;

        private StopDomain(Domain d) {
            domain = d;
        }

        @Override
        public void run() {
            domain.instanceStopped();
            updateStoppedDomains(domain.getProviderInfo(), true);
        }

    }
}
