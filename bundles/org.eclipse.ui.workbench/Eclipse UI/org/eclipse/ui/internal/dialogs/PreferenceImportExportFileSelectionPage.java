/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The page -- used for both importing and exporting -- that allows the user to
 * select a file, as well as specify how many of the settings they want to use.
 * 
 * @since 3.0
 */
class PreferenceImportExportFileSelectionPage extends AbstractPreferenceImportExportPage {
	
	/**
	 * Listens for changes on the page, and updates the buttons.
	 * @since 3.0
	 */
	private class PageChangeListener implements Listener {
		/**
		 * If the current page is not <code>null</code>, then this updates the
		 * buttons on the wizard dialog.
		 * @param event Ignored.
		 */
		public void handleEvent(Event event) {
			IWizardContainer container = getContainer();
			if (container.getCurrentPage() != null) {
				container.updateButtons();
			}
		}
	}
	
	/**
	 * The accepted extensions for the file chooser dialog.
	 */
	private static final String[] DIALOG_PREFERENCE_EXTENSIONS = new String[] { "*" + PREFERENCE_EXT, "*.*"}; //$NON-NLS-1$ //$NON-NLS-2$
	/**
	 * The message to display when there are no errors on this page.
	 */
	private static final String EXPORT_MESSAGE = WorkbenchMessages.getString("ImportExportPages.exportFileSelect"); //$NON-NLS-1$
	/**
	 * The message to display when there are no errors on this page.
	 */
	private static final String IMPORT_MESSAGE = WorkbenchMessages.getString("ImportExportPages.importFileSelect"); //$NON-NLS-1$
	/**
	 * The name of this page -- used to find the page later.
	 */
	private static final String NAME = "org.eclipse.ui.preferences.importExportFileSelectionPage"; //$NON-NLS-1$
	/**
	 * The radio button signifying that all modified preferences should be 
	 * imported from or exported to the given file.
	 */
	private Button allItemsRadioButton;

	/**
	 * Updates the state of various widgets in response to changes on the page.
	 */
	public final Listener changeListener = new PageChangeListener();
	/**
	 * The text widget containing the absolute path to the file from which to
	 * import or to which to export.
	 */
	private Text fileText;
	/**
	 * The radio button signifying that all modified preferences from the 
	 * currently active page should be exported.
	 */
	private Button pageItemsRadioButton;
	/**
	 * The radio button signifying that the user wishes to review the selected
	 * preferences, and choose which ones to export.
	 */
	private Button selectItemsRadioButton;

	/**
	 * Constructs a new instance of the file selection page.
	 * @param exportWizard Whether this page should be opened in export mode.
	 */
	PreferenceImportExportFileSelectionPage(boolean exportWizard) {
		super(NAME, exportWizard);
	}
	
	/**
	 * Whether this page can finish.  This page can finish if all the data is
	 * valid and the user doesn't want to select the individual preferences.
	 * @return <code>true</code> If the finish button should be enabled;
	 * <code>false</code> otherwise.
	 */
	boolean canFinish() {
		return validate() && !wantsPreferencesSelected();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return validate() && wantsPreferencesSelected();
	}
	
	/**
	 * Chooses the file to use for exporting or importing.  This opens a native
	 * file dialog and sets <code>fileText</code> to the user selection.
	 */
	private void chooseFile() {
		// Find the closest file/directory to what is currently entered. 
		String currentFileName = fileText.getText();
		
		// Open a dialog allowing the user to choose.
		final FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
		fileDialog.setFileName(currentFileName);
		fileDialog.setFilterExtensions(DIALOG_PREFERENCE_EXTENSIONS);
		currentFileName = fileDialog.open();
		
		if (currentFileName == null)
			return;
		
		/* Append the default filename if none was specifed	and such a file does
		 * not exist.
		 */
		String fileName = new File(currentFileName).getName();
		if (fileName.lastIndexOf(".") == -1) { //$NON-NLS-1$
			currentFileName += PREFERENCE_EXT;
		}
		
		fileText.setText(currentFileName);
		canFlipToNextPage();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
	    Font parentFont = parent.getFont();
		final Composite page = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		page.setLayout(layout);
		initializeDialogUnits(page);

		// Set-up the title, subtitle and icon.
		if (export) {
			setTitle(EXPORT_TITLE);
			setMessage(EXPORT_MESSAGE);
			setImageDescriptor(getImageDescriptor("wizban/export_wiz.gif")); //$NON-NLS-1$
		} else {
			setTitle(IMPORT_TITLE);
			setMessage(IMPORT_MESSAGE);
			setImageDescriptor(getImageDescriptor("wizban/import_wiz.gif")); //$NON-NLS-1$
		}

		GridData layoutData;

		// Set up the file selection label.
		final Label fileLabel = new Label(page, SWT.NONE);
		fileLabel.setText(WorkbenchMessages.getString("ImportExportPages.fileLabel")); //$NON-NLS-1$
		fileLabel.setFont(parentFont);
		layoutData = new GridData();
		fileLabel.setLayoutData(layoutData);

		// Set up the text widget containing the file selection.
		fileText = new Text(page, SWT.SINGLE | SWT.BORDER);
		fileText.setFont(parentFont);
		layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		fileText.setLayoutData(layoutData);
		fileText.addListener(SWT.Modify, changeListener);

		// Set up the button for choosing a file.
		final Button browseButton = new Button(page, SWT.PUSH);
		browseButton.setFont(parentFont);
		layoutData = new GridData();
		browseButton.setText(WorkbenchMessages.getString("ImportExportPages.browseButton")); //$NON-NLS-1$
		layoutData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		layoutData.widthHint = computePushButtonWidthHint(browseButton);
		browseButton.setLayoutData(layoutData);
		browseButton.addSelectionListener(new SelectionAdapter() {
			public final void widgetSelected(SelectionEvent event) {
				chooseFile();
			}
		});

		// Insert a chunk of space below the file selection stuff.
		final Composite verticalSpacer = new Composite(page, SWT.NONE);
		layoutData = new GridData();
		layoutData.heightHint = 15;
		layoutData.horizontalSpan = 3;
		verticalSpacer.setLayoutData(layoutData);

		// Create the radio button for the export/import all option.
		allItemsRadioButton = new Button(page, SWT.RADIO | SWT.CENTER);
		allItemsRadioButton.setFont(parentFont);
		layoutData = new GridData();
		layoutData.horizontalSpan = 3;
		allItemsRadioButton.setLayoutData(layoutData);
		allItemsRadioButton.addListener(SWT.Selection, changeListener);

		// Create the radio button for the select preferences to export/import.
		selectItemsRadioButton = new Button(page, SWT.RADIO | SWT.CENTER);
		selectItemsRadioButton.setFont(parentFont);
		layoutData = new GridData();
		layoutData.horizontalSpan = 3;
		selectItemsRadioButton.setLayoutData(layoutData);
		selectItemsRadioButton.addListener(SWT.Selection, changeListener);
		selectItemsRadioButton.setEnabled(false); // TODO allow this option

		if (export) {
			/* If we are exporting, then we also have the option of exporting
			 * items from the currently selected page.
			 */
			pageItemsRadioButton = new Button(page, SWT.RADIO | SWT.CENTER);
			pageItemsRadioButton.setFont(parentFont);
			layoutData = new GridData();
			layoutData.horizontalSpan = 3;
			pageItemsRadioButton.setLayoutData(layoutData);
			pageItemsRadioButton.addListener(SWT.Selection, changeListener);
			pageItemsRadioButton.setEnabled(false); // TODO allow this option

			allItemsRadioButton.setText(WorkbenchMessages.getString("ImportExportPages.exportAllItems")); //$NON-NLS-1$
			selectItemsRadioButton.setText(WorkbenchMessages.getString("ImportExportPages.exportSelectItems")); //$NON-NLS-1$
			pageItemsRadioButton.setText(WorkbenchMessages.getString("ImportExportPages.exportPageItems")); //$NON-NLS-1$

		} else {
			allItemsRadioButton.setText(WorkbenchMessages.getString("ImportExportPages.importAllItems")); //$NON-NLS-1$
			selectItemsRadioButton.setText(WorkbenchMessages.getString("ImportExportPages.importSelectItems")); //$NON-NLS-1$

		}

		// Remember the composite as the top-level control.
		setControl(page);
		
		// Restore all the controls to their previous values.
		init();
	}
	
	/**
	 * An accessor for the path the user has currently selected.
	 * @return The path; may be empty (or invalid), but never <code>null</code>.
	 */
	String getPath() {
		return fileText.getText();
	}

	/**
	 * Initializes all the controls on this page by restoring their previous
	 * values.
	 */
	private void init() {
		String lastFileName = WorkbenchPlugin.getDefault().getDialogSettings().get(WorkbenchPreferenceDialog.FILE_PATH_SETTING);
		if (lastFileName == null) {
			fileText.setText(System.getProperty("user.dir") + System.getProperty("file.separator") + WorkbenchMessages.getString("ImportExportPages.preferenceFileName") + PREFERENCE_EXT);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		} else {
			fileText.setText(lastFileName);
		}
		
		allItemsRadioButton.setSelection(true);
	}
	
	/**
	 * Validates all of the data on the page to make sure that it is all valid.
	 * If some of the data is invalid, then it displays an error message on the
	 * page
	 * @return <code>true</code> If the data is all valid; <code>false</code>
	 * otherwise.
	 */
	boolean validate() {
		final File currentFile = new File(fileText.getText());
		if (export) {
			if (!currentFile.getParentFile().exists()) {
				setErrorMessage(WorkbenchMessages.getString("ImportExportPages.errorDirectoryDoesNotExist")); //$NON-NLS-1$
				return false;
			}
		} else {
			if (!currentFile.exists()) {
				setErrorMessage(WorkbenchMessages.getString("ImportExportPages.errorImportFileDoesNotExist")); //$NON-NLS-1$
				return false;
			}
		}
		
		setErrorMessage(null);
		return true;
	}

	/**
	 * Whether the user has asked to select the preferences by hand.
	 * @return <code>true</code> If the user wants to select the preference by
	 * hand; <code>false</code> otherwise.
	 */
	boolean wantsPreferencesSelected() {
		return (selectItemsRadioButton != null) && (selectItemsRadioButton.getSelection());
	}
}
