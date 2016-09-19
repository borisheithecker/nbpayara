/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.core.ui;

import org.openide.modules.OnStop;

/**
 *
 * @author boris.heithecker
 */
@OnStop
public class ShutDown implements Runnable {

    @Override
    public void run() {
        Listener.shutDown();
    }
    
}
