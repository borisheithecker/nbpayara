/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.core;

import java.io.Serializable;

/**
 *
 * @author boris.heithecker
 */
public interface ProviderInfo extends Serializable {

    public String getDescription();

    //unique === authority
    public String getURL();

    public String getDisplayName();
}
