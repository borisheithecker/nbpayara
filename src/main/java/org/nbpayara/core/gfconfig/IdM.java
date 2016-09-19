package org.nbpayara.core.gfconfig;

import com.sun.enterprise.security.store.IdentityManagement;
import org.jvnet.hk2.annotations.Service;
import org.netbeans.api.keyring.Keyring;
import org.thespheres.betula.services.ui.KeyStores;

/**
 *
 * @author boris.heithecker
 */
@Service
public class IdM implements IdentityManagement {

    @Override
    public char[] getMasterPassword() {
        return Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
    }

}
