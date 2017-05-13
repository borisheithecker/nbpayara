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
public class DomainInfo implements Serializable {

    private final String displayName;
    private final String id;

    public DomainInfo(String displayName, String id) {
        this.displayName = displayName;
        this.id = id;
    }

    public String getURL() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}
