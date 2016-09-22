/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.core.gfconfig.uix;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import org.nbpayara.core.gfconfig.uix.Listener.InstanceImpl;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author boris.heithecker
 */
class RequireRestartException extends PropertyVetoException {

    static boolean USER_CONFIRM_RESTART = true;
    private final InstanceImpl running;

    RequireRestartException(String mess, PropertyChangeEvent evt, InstanceImpl running) {
        super(mess, evt);
        this.running = running;
    }

    @NbBundle.Messages("Listener.maybeRestartPlatform.userMessage=Restart the application now after change of selected domain?")
    static void maybeRestartPlatform(PropertyChangeEvent evt) throws PropertyVetoException {
        if (USER_CONFIRM_RESTART) {
            String message = NbBundle.getMessage(Listener.class, "Listener.maybeRestartPlatform.userMessage");
            NotifyDescriptor.Confirmation ndc = new NotifyDescriptor.Confirmation(message, NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(ndc) == NotifyDescriptor.OK_OPTION) {
                return;
            }
        }
        throw new PropertyVetoException("", evt);
    }

    void enqueForStopAndPlatformRestart() {
        if (running != null) {
            try {
                running.stopAndRestartPlatform();
            } catch (IllegalStateException e) {
            }
        }
    }

}
