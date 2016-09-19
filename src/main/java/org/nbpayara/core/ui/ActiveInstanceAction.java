package org.nbpayara.core.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.ui.util.WideJXComboBox;

/**
 * @author boris.heithecker, adapted from
 * org.netbeans.modules.project.ui.actions.ActiveConfigAction
 */
@ActionID(id = "org.thespheres.betula.glassfish.startup.ActiveInstanceAction", category = "Project")
@ActionRegistration(displayName = "#ActiveInstanceAction.label", lazy = false)
@ActionReferences({
    @ActionReference(path = "Toolbars/Settings", position = 11000, separatorBefore = 10000)
})
@NbBundle.Messages({"ActiveInstanceAction.name=Domain",
    "ActiveInstanceAction.label=Mandant:"})
public class ActiveInstanceAction extends CallableSystemAction implements VetoableChangeListener {

    private final static ProviderInfo NULL = new ProviderInfo() {
        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String getURL() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return "Kein Mandant";
        }
    };
    private static final DefaultComboBoxModel EMPTY_MODEL = new DefaultComboBoxModel();
    private JXComboBox configListCombo;

    @SuppressWarnings("LeakingThisInConstructor")
    public ActiveInstanceAction() {
        super();
        putValue("noIconInMenu", true);
        EventQueue.invokeLater(this::initConfigListCombo);
    }

    private void initConfigListCombo() {
        assert EventQueue.isDispatchThread();
        if (configListCombo != null) {
            return;
        }
        configListCombo = new WideJXComboBox();
        configListCombo.addPopupMenuListener(new PopupMenuListener() {
            private Component prevFocusOwner = null;

            public @Override
            void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                prevFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                configListCombo.setFocusable(true);
                configListCombo.requestFocusInWindow();
            }

            public @Override
            void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (prevFocusOwner != null) {
                    prevFocusOwner.requestFocusInWindow();
                }
                prevFocusOwner = null;
                configListCombo.setFocusable(false);
            }

            public @Override
            void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        configListCombo.setToolTipText(org.openide.awt.Actions.cutAmpersand(getName()));
        configListCombo.setFocusable(false);
        configListCombo.setRenderer(new DefaultListRenderer(o -> o instanceof ProviderInfo ? ((ProviderInfo) o).getDisplayName() : " "));
        configListCombo.addHighlighter(new ItalicHighlighter());
        configListCombo.setEditable(false);
        configurationsListChanged();
        InstanceList.getInstance().addVetoableChangeListener(this);
    }

    private ProviderInfo getActiveInstance() {
        return InstanceList.getInstance().getSelectedDomain();
    }

    private void configurationsListChanged() {
        List<ProviderInfo> configs = Arrays.stream(InstanceList.getInstance().getDomains())
                .collect(Collectors.toList());
        if (configs == null || configs.isEmpty()) {
            EventQueue.invokeLater(() -> {
                configListCombo.setModel(EMPTY_MODEL);
                configListCombo.setEnabled(false); // possibly redundant, but just in case
            });
        } else {
            configs.add(0, NULL);
            final DefaultComboBoxModel model = new ComboModel(configs.toArray());
            EventQueue.invokeLater(() -> {
                configListCombo.setModel(model);
                configListCombo.setEnabled(true);
            });
        }
        activeConfigurationChanged(getActiveInstance());
    }

    private void activeConfigurationChanged(final ProviderInfo config) {
        EventQueue.invokeLater(() -> {
//            listeningToCombo = false;
            try {
                configListCombo.setSelectedIndex(-1);
                if (config != null) {
                    ComboBoxModel m = configListCombo.getModel();
                    for (int i = 0; i < m.getSize(); i++) {
                        if (config.equals(m.getElementAt(i))) {
                            configListCombo.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            } finally {
//                listeningToCombo = true;
            }
        });
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        switch (evt.getPropertyName()) {
            case InstanceList.SELECTED_DOMAIN:
                activeConfigurationChanged((ProviderInfo) evt.getNewValue());
                break;
            case InstanceList.LISTED_DOMAINS:
                configurationsListChanged();
                break;
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.thespheres.betula.glassfish.startup.ActiveInstanceAction");
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ActiveInstanceAction.class, "ActiveInstanceAction.name");
    }

    public @Override
    void performAction() {
        Toolkit.getDefaultToolkit().beep();
    }

    @Override
    public Component getToolbarPresenter() {
        // Do not return combo box directly; looks bad.
        JPanel toolbarPanel = new JPanel(new GridBagLayout());
        toolbarPanel.setOpaque(false); // don't interrupt JToolBar background
        toolbarPanel.setMaximumSize(new Dimension(350, 80));
        toolbarPanel.setMinimumSize(new Dimension(150, 0));
//        toolbarPanel.setPreferredSize(new Dimension(150, 23));
        initConfigListCombo();
        // XXX top inset of 2 looks better w/ small toolbar, but 1 seems to look better for large toolbar (the default):
        JLabel label = new JLabel(NbBundle.getMessage(ActiveInstanceAction.class, "ActiveInstanceAction.label"));
        label.setBorder(new EmptyBorder(0, 2, 0, 10));
        label.setLabelFor(configListCombo);
        toolbarPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(1, 6, 1, 0), 0, 0));
        toolbarPanel.add(configListCombo, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 1, 5), 0, 0));
        return toolbarPanel;
    }
    
    private class ItalicHighlighter extends FontHighlighter implements HighlightPredicate {

        @SuppressWarnings("OverridableMethodCallInConstructor")
        private ItalicHighlighter() {
            super();
            setHighlightPredicate(this);
        }

        @Override
        protected boolean canHighlight(Component component, ComponentAdapter adapter) {
            setFont(component.getFont().deriveFont(Font.ITALIC));
            return super.canHighlight(component, adapter);
        }

        @Override
        public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
            return NULL == adapter.getValue();
        }

    }

    private class ComboModel extends DefaultComboBoxModel {

        private ComboModel(Object[] arr) {
            super(arr);
        }

        @Override
        public void setSelectedItem(Object o) {
            if (o != null) {
                ProviderInfo cfg = (ProviderInfo) o;
                try {
                    if (NULL == cfg) {
                        InstanceList.getInstance().setSelectedDomain(null);
                    } else if (!Objects.equals(cfg, getActiveInstance())) {
                        InstanceList.getInstance().setSelectedDomain(cfg);
                    }
                } catch (PropertyVetoException ex) {
                    return;
                }
            }
            super.setSelectedItem(o);
        }

    }
}
