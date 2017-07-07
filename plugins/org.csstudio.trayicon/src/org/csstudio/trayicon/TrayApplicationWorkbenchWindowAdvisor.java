package org.csstudio.trayicon;

import java.io.IOException;

import org.csstudio.utility.product.ApplicationWorkbenchWindowAdvisor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class TrayApplicationWorkbenchWindowAdvisor extends ApplicationWorkbenchWindowAdvisor {

    public static final String REMEMBER_DECISION = "Remember my decision";
    public static final String DIALOG_TITLE = "Minimize to System Tray?";
    public static final String DIALOG_QUESTION = "This is the last CS-Studio window.  Should CS-Studio minimize to the System Tray or exit?";

    // This requires internal understanding.  Since we have changed the labels on the dialog,
    // MessageButtonWithDialog does not assign standard return codes.  This is fixed in Oxygen
    // but for now we need to know what is going to be returned.
    private static final int BUTTON1_ID = 256;
    private static final int BUTTON2_ID = 257;
    private static final int DIALOG_CLOSED = -1;

    private TrayIcon trayIcon;

    public TrayApplicationWorkbenchWindowAdvisor(
            IWorkbenchWindowConfigurer configurer, TrayIcon trayIcon) {
        super(configurer);
        this.trayIcon = trayIcon;
    }

    public int prompt() {
        Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, TrayIconPreferencePage.ID);
        String[] labels = {"Minimize", "Exit Now", "Cancel"};
        MessageDialogWithToggle dialog = new MessageDialogWithToggle(parent, DIALOG_TITLE, null, DIALOG_QUESTION,
                MessageDialog.QUESTION, labels, 2, REMEMBER_DECISION, false);
        dialog.open();
        int response = dialog.getReturnCode();
        if (dialog.getToggleState()) {
            if (response == BUTTON1_ID) {
                store.setValue(TrayIconPreferencePage.MINIMIZE_TO_TRAY, "always");
            } else if (response == BUTTON2_ID) {
                store.setValue(TrayIconPreferencePage.MINIMIZE_TO_TRAY, "never");
            }
            try {
                store.save();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return response;
    }

    @Override
    public boolean preWindowShellClose() {
        if (PlatformUI.getWorkbench().getWorkbenchWindowCount() > 1) {
            return super.preWindowShellClose();
        } else {
            IPreferencesService prefs = Platform.getPreferencesService();
            String minPref = prefs.getString(TrayIconPreferencePage.ID,
                    TrayIconPreferencePage.MINIMIZE_TO_TRAY, null, null);
            if (trayIcon.isMinimized() || minPref.equals(MessageDialogWithToggle.NEVER)) {
                return super.preWindowShellClose();
            } else {
                if (minPref.equals(MessageDialogWithToggle.PROMPT)) {
                    int response = prompt();
                    if (response == IDialogConstants.CANCEL_ID || response == DIALOG_CLOSED) {
                        return false;
                    }
                    if (response == BUTTON2_ID) {
                        return super.preWindowShellClose();
                    }
                }
                trayIcon.minimize();
                return false;
            }
        }
    }

}
