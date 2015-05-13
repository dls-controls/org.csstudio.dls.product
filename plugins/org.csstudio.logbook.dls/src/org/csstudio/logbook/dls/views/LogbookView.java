package org.csstudio.logbook.dls.views;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.csstudio.logbook.dls.ELog;
import org.csstudio.logbook.dls.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

//TODO: Contribute to CSS Menus, via extension point?
public class LogbookView extends ViewPart {

    public static final String ID = "com.diamond.logbook.views.LogBookView";
    private ELog elog;
    private AttachmentManager attachmentManager;

    public class AttachmentManager {
        private ArrayList<String> attachments;
        private ArrayList<Button> buttons;
        private ArrayList<Label> labels;
        private Group parentGroup;
        private Label defaultLabel;

        public AttachmentManager(Group parentGroup) {
            this.parentGroup = parentGroup;
            attachments = new ArrayList<String>();
            buttons = new ArrayList<Button>();
            labels = new ArrayList<Label>();
            defaultLabel = new Label(parentGroup, SWT.NONE);
            defaultLabel.setText("No Attachments");
        }

        public void addAttachment(final String filePath) throws Exception {
            final String fileExtension = filePath.substring(filePath.lastIndexOf(".")+1);
            if(!elog.isExtensionSupported(fileExtension)) {
                MessageDialog.openError(parentGroup.getShell(), "Error", "File extension " + fileExtension + " is not supported.");
                return;
            }

            System.out.println("Disposing default label");
            defaultLabel.dispose();
            attachments.add(filePath);

            // Get file name from full path for display
            final String fileName = Paths.get(filePath).getFileName().toString();

            // Add a new label for each filename
            final Label attachmentLabel = new Label(parentGroup, SWT.NONE);
            labels.add(attachmentLabel);
            attachmentLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
            attachmentLabel.setText(fileName);

            // Add a remove button
            final Button removeButton = new Button(parentGroup, SWT.NONE);
            buttons.add(removeButton);
            removeButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            removeButton.setText("Remove");
            removeButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    attachments.remove(filePath);
                    attachmentLabel.dispose();
                    removeButton.dispose();

                    if(attachments.isEmpty()) {
                        defaultLabel = new Label(parentGroup, SWT.NONE);
                        defaultLabel.setText("No Attachments");
                    }

                    parentGroup.getParent().layout();
                }
            });

            // Force redrawing
            parentGroup.getParent().layout();
        }

        public ArrayList<String> getAttachments() {
            return attachments;
        }

        public void clear() {
            for(Button button : buttons) {
                button.dispose();
            }
            for(Label label : labels) {
                label.dispose();
            }

            buttons.clear();
            labels.clear();
            attachments.clear();

            defaultLabel = new Label(parentGroup, SWT.NONE);
            defaultLabel.setText("No Attachments");
            parentGroup.getParent().layout();
        }
    }

    public LogbookView() {
        try {
            elog = new ELog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createPartControl(final Composite parent) {
        if(elog == null) {
            parent.setLayout(new GridLayout(1, false));
            final Label errorLabel = new Label(parent, SWT.FILL | SWT.CENTER);
            errorLabel.setText("Error: Could not connect to logbook.");
            return;
        }
        parent.setLayout(new GridLayout(2,  false));

        /* USERNAME */
        final Label userLabel = new Label(parent, SWT.NONE);
        userLabel.setText("Username:");

        final Text usernameText = new Text(parent, SWT.BORDER);
        usernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        /* PASSWORD */
        final Label passwordLabel = new Label(parent, SWT.NONE);
        passwordLabel.setText("Password:");

        final Text passwordText = new Text(parent, SWT.PASSWORD | SWT.BORDER);
        passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        /* LOGBOOKS */
        ArrayList<String> logbooksList = elog.getLogbooks();
        final String[] logbooks = new String[logbooksList.size()];
        logbooksList.toArray(logbooks);

        final Label lookbookLabel = new Label(parent, SWT.NONE);
        lookbookLabel.setText("Logbook:");

        final Combo logbooksCombo = new Combo(parent, SWT.NONE);
        logbooksCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        logbooksCombo.setItems(logbooks);
        logbooksCombo.select(logbooksList.indexOf(Preferences.getDefaultLogbook()));

        /* TITLE AND MESSAGE */
        final Group titleMessageGroup = new Group(parent, SWT.NONE);
        titleMessageGroup.setText("Title / Message");
        titleMessageGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        titleMessageGroup.setLayout(new GridLayout(1, false));

        final Text titleText = new Text(titleMessageGroup, SWT.BORDER);
        titleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Text messageText = new Text(titleMessageGroup, SWT.V_SCROLL | SWT.WRAP);
        messageText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        /* ATTACHMENTS LIST */
        final Group attachmentsGroup = new Group(parent, SWT.NONE);
        attachmentsGroup.setText("Attachments");
        attachmentsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        attachmentsGroup.setLayout(new GridLayout(2, false));

        attachmentManager = new AttachmentManager(attachmentsGroup);

        /* BUTTONS */
        final Button attachButton = new Button(parent, SWT.NONE);
        attachButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
        attachButton.setText("Attach File");
        attachButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                final FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.OPEN);
                final String filePath = fileDialog.open();

                // In case the dialog is cancelled
                if(filePath != null) {
                    try {
                        attachmentManager.addAttachment(filePath);
                    } catch (Exception e) {
                        handleException(parent.getShell(), e);
                    }
                }
            }
        });

        final Button submitButton = new Button(parent, SWT.NONE);
        submitButton.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
        submitButton.setText("Submit");
        submitButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    elog.createEntry(usernameText.getText(), passwordText.getText(), titleText.getText(), messageText.getText(), logbooksCombo.getText(), attachmentManager.getAttachments());
                    MessageDialog.openInformation(parent.getShell(), "ELog Committed", "Your ELog entry '" + titleText.getText() + "' was committed successfully.");
                    attachmentManager.clear();
                    messageText.setText("");
                    titleText.setText("");
                }
                catch (Exception e) {
                    handleException(parent.getShell(), e);
                }
            }
        });
    }

    @Override
    public void setFocus() {
        // Do nothing
    }

    private void handleException(Shell shell, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String[] stack = sw.toString().split("\r\n|\r|\n");

        // Shorten to 8 lines
        String stackTrace = "";
        final int maxLines = 8;
        for(int i=0; i<maxLines && i<stack.length; i++) {
            stackTrace += stack[i] + "\n";
        }

//        MessageDialog.openError(shell, "Error", e.getLocalizedMessage() + "\n\n" + stackTrace);
        MessageDialog.openError(shell, "Error", e.getLocalizedMessage());
        e.printStackTrace();
    }
}