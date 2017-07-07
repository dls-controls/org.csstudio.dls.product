package org.csstudio.trayicon;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class TrayIconPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String ID = "org.csstudio.trayicon.preferences";
    public static final String MINIMIZE_TO_TRAY = "minimize_to_tray";
    private ScopedPreferenceStore store;

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        RadioGroupFieldEditor perspectiveEditor = new RadioGroupFieldEditor(
                MINIMIZE_TO_TRAY,
                "Minimize to system tray when closing last window?", 3,
                new String[][] {{"Always", MessageDialogWithToggle.ALWAYS},
                                {"Never", MessageDialogWithToggle.NEVER},
                                {"Prompt", MessageDialogWithToggle.PROMPT}},
                parent, true);
        addField(perspectiveEditor);
    }

    @Override
    public void init(IWorkbench workbench) {
        store = new ScopedPreferenceStore(InstanceScope.INSTANCE, ID);
        setPreferenceStore(store);
        setDescription("Desco");
}

}
