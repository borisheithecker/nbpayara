/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.core.gfconfig;

import com.sun.enterprise.connectors.jms.config.JmsService;
import com.sun.enterprise.glassfish.bootstrap.EmbeddedMain;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.security.store.IdentityManagement;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Singleton;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.DescriptorFileFinder;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.PopulatorPostProcessor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.orb.admin.config.IiopService;
import org.jvnet.hk2.config.RetryableException;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author boris.heithecker
 */
@Messages({"GFMain.createServiceLocator.setIIOPPort=Allocated port {0} for iiop-listener with id {1}"})
public class NbMain extends EmbeddedMain {

//    private ServiceLocator locator;
    public NbMain(ClassLoader cl) {
        super(cl);
    }

    @Override
    public ServiceLocator createServiceLocator(ModulesRegistry mr, StartupContext context, List<PopulatorPostProcessor> postProcessors, DescriptorFileFinder descriptorFileFinder) throws BootException {

        final ServiceLocator locator = super.createServiceLocator(mr, context, postProcessors, descriptorFileFinder); //To change body of generated methods, choose Tools | Templates.

        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        DescriptorImpl retVal = BuilderHelper.link(IdM.class).to(IdentityManagement.class).in(Singleton.class).ofRank(Integer.MAX_VALUE).build();
        config.bind(retVal);
        config.commit();

        final IiopService iiop = locator.getService(IiopService.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
        final ArrayList<Integer> allocated = new ArrayList<>();
        iiop.getIiopListener().forEach(l -> {
            String port = Util.findAvailablePort(35000, allocated);
            final Transaction iioptransaction = new Transaction();
            try {
                IiopListener listener = iioptransaction.enroll(l);
                listener.setPort(port);
                listener.setAddress("0.0.0.0");
                iioptransaction.commit();
                Logger.getLogger(NbMain.class.getName()).log(Level.INFO, NbBundle.getMessage(NbMain.class, "GFMain.createServiceLocator.setIIOPPort", port, listener.getId()));
            } catch (TransactionFailure | RetryableException | PropertyVetoException ex) {
                Logger.getLogger(NbMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        JmsService jms = locator.getService(JmsService.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
        jms.getProperty().stream()
                .filter(p -> "password".equals(p.getName()))
                .forEach((p) -> {
                    final Transaction t = new Transaction();
                    try {
                        Property password = t.enroll(p);
                        password.setValue("guest");
                        t.commit();
                    } catch (TransactionFailure | RetryableException | PropertyVetoException ex) {
                        Logger.getLogger(NbMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });//                }
//            }

        return locator;
    }

    @Override
    protected ClassLoader getParentClassLoader() {
        return super.getParentClassLoader();
    }

}
