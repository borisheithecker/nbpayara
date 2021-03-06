package org.nbpayara.core.gfconfig;

import com.sun.enterprise.security.store.IdentityManagement;
import org.jvnet.hk2.annotations.Service;
import org.netbeans.api.keyring.Keyring;

/**
 *
 * @author boris.heithecker
 */
@Service
public class NbIdentityManagement implements IdentityManagement {

    public static final String KEYRING_KEYSTORE_PASSWORD_KEY = "nbpayara.master.password";

    @Override
    public char[] getMasterPassword() {
        return Keyring.read(KEYRING_KEYSTORE_PASSWORD_KEY);
    }

}
