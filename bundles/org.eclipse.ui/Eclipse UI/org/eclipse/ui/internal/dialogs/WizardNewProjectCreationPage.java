package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * First page for the new project creation wizard. This page
 * collects the name and location of the new project.
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new WizardNewProjectCreationPage("wizardNewProjectCreationPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Create a new project.");
 * </pre>
 * </p>
 */
public class WizardNewProjectCreationPage extends WizardPage {
	// Whether to use default or custom project location
	private boolean useDefaults = true;

	// initial value stores
	private String initialProjectFieldValue;
	private IPath initialLocationFieldValue;

	// the value the user has entered
	private String customLocationFieldValue;

	// widgets
	private Text projectNameField;
	private Text locationPathField;
	private Label locationLabel;
	private Button browseButton;

	private Listener nameModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setLocationForSelection();
			setPageComplete(validatePage());
		}
	};

	private Listener locationModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setPageComplete(validatePage());
		}
	};

	// constants
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	private static final int SIZING_INDENTATION_WIDTH = 10;
	
	/**
	 * Creates a new project creation wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public WizardNewProjectCreationPage(String pageName) {
		super(pageName);
		setPageComplete(false);
		initialLocationFieldValue = Platform.getLocation();
		customLocationFieldValue = ""; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizardPage
	 */
	public boolean canFlipToNextPage() {
		// Already know there is a next page...
		return isPageComplete();
	}

	/* (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		WorkbenchHelp.setHelp(composite, IHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

		createProjectNameGroup(composite);
		createProjectLocationGroup(composite);
		
		projectNameField.setFocus();
		validatePage();
		
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}
	
	/**
	 * Creates the project location specification controls.
	 *
	 * @param parent the parent composite
	 */
	private final void createProjectLocationGroup(Composite parent) {

		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project label
		Label projectContentsLabel = new Label(projectGroup, SWT.NONE);
		projectContentsLabel.setText(WorkbenchMessages.getString("WizardNewProjectCreationPage.projectContentsLabel")); //$NON-NLS-1$

		GridData labelData = new GridData();
		labelData.horizontalSpan = 3;
		projectContentsLabel.setLayoutData(labelData);

		final Button useDefaultsButton = new Button(projectGroup, SWT.CHECK | SWT.RIGHT);
		useDefaultsButton.setText(WorkbenchMessages.getString("WizardNewProjectCreationPage.useDefaultLabel")); //$NON-NLS-1$
		useDefaultsButton.setSelection(useDefaults);

		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 3;
		useDefaultsButton.setLayoutData(buttonData);

		createUserSpecifiedProjectLocationGroup(projectGroup, !useDefaults);

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				useDefaults = useDefaultsButton.getSelection();
				browseButton.setEnabled(!useDefaults);
				locationPathField.setEnabled(!useDefaults);
				locationLabel.setEnabled(!useDefaults);
				if (useDefaults) {
					customLocationFieldValue = locationPathField.getText();
					setLocationForSelection();
				} else {
					locationPathField.setText(customLocationFieldValue);
				}
			}
		};
		useDefaultsButton.addSelectionListener(listener);
	}
	
	/**
	 * Creates the project name specification controls.
	 *
	 * @param parent the parent composite
	 */
	private final void createProjectNameGroup(Composite parent) {
		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project label
		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setText(WorkbenchMessages.getString("WizardNewProjectCreationPage.nameLabel")); //$NON-NLS-1$

		// new project name entry field
		projectNameField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		projectNameField.setLayoutData(data);

		// Set the initial value first before listener
		// to avoid handling an event during the creation.
		if (initialProjectFieldValue != null)
			projectNameField.setText(initialProjectFieldValue);
		projectNameField.addListener(SWT.Modify, nameModifyListener);
	}
	
	/**
	 * Creates the project location specification controls.
	 *
	 * @param projectGroup the parent composite
	 * @param boolean - the initial enabled state of the widgets created
	 */
	private void createUserSpecifiedProjectLocationGroup(Composite projectGroup, boolean enabled) {

		// location label
		locationLabel = new Label(projectGroup, SWT.NONE);
		locationLabel.setText(WorkbenchMessages.getString("WizardNewProjectCreationPage.locationLabel")); //$NON-NLS-1$
		locationLabel.setEnabled(enabled);

		// project location entry field
		locationPathField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		locationPathField.setLayoutData(data);
		locationPathField.setEnabled(enabled);

		// browse button
		browseButton = new Button(projectGroup, SWT.PUSH);
		browseButton.setText(WorkbenchMessages.getString("WizardNewProjectCreationPage.browseLabel")); //$NON-NLS-1$
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleLocationBrowseButtonPressed();
			}
		});

		browseButton.setEnabled(enabled);

		// Set the initial value first before listener
		// to avoid handling an event during the creation.
		if (initialLocationFieldValue != null)
			locationPathField.setText(initialLocationFieldValue.toOSString());
		locationPathField.addListener(SWT.Modify, locationModifyListener);
	}
	
	/**
	 * Returns the current project location path as entered by 
	 * the user, or its anticipated initial value.
	 *
	 * @return the project location path, its anticipated initial value, or <code>null</code>
	 *   if no project location path is known
	 */
	/* package */ IPath getLocationPath() {
		if (useDefaults)
			return initialLocationFieldValue;

		return new Path(getProjectLocationFieldValue());
	}
	
	/**
	 * Creates a project resource handle for the current project name field value.
	 * <p>
	 * This method does not create the project resource; this is the responsibility
	 * of <code>IProject::create</code> invoked by the new project resource wizard.
	 * </p>
	 *
	 * @return the new project resource handle
	 */
	/* package */ IProject getProjectHandle() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
	}
	
	/**
	 * Returns the current project name as entered by the user, or its anticipated
	 * initial value.
	 *
	 * @return the project name, its anticipated initial value, or <code>null</code>
	 *   if no project name is known
	 */
	/* package */ String getProjectName() {
		if (projectNameField == null)
			return initialProjectFieldValue;

		return getProjectNameFieldValue();
	}
	
	/**
	 * Returns the value of the project name field
	 * with leading and trailing spaces removed.
	 * 
	 * @return the project name in the field
	 */
	private String getProjectNameFieldValue() {
		if (projectNameField == null)
			return ""; //$NON-NLS-1$
		else
			return projectNameField.getText().trim();
	}
	
	/**
	 * Returns the value of the project location field
	 * with leading and trailing spaces removed.
	 * 
	 * @return the project location directory in the field
	 */
	private String getProjectLocationFieldValue() {
		if (locationPathField == null)
			return ""; //$NON-NLS-1$
		else
			return locationPathField.getText().trim();
	}
	
	/**
	 *	Open an appropriate directory browser
	 */
	private void handleLocationBrowseButtonPressed() {
		DirectoryDialog dialog = new DirectoryDialog(locationPathField.getShell());
		dialog.setMessage(WorkbenchMessages.getString("WizardNewProjectCreationPage.directoryLabel")); //$NON-NLS-1$

		String dirName = getProjectLocationFieldValue();
		if (!dirName.equals("")) { //$NON-NLS-1$
			File path = new File(dirName);
			if (path.exists())
				dialog.setFilterPath(dirName);
		}

		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			customLocationFieldValue = selectedDirectory;
			locationPathField.setText(customLocationFieldValue);
		}
	}
	
	/**
	 * Sets the initial project name that this page will use when
	 * created. The name is ignored if the createControl(Composite)
	 * method has already been called. Leading and trailing spaces
	 * in the name are ignored.
	 * 
	 * @param name initial project name for this page
	 */
	/* package */ void setInitialProjectName(String name) {
		if (name == null)
			initialProjectFieldValue = null;
		else
			initialProjectFieldValue = name.trim();
	}
	
	/**
	 * Set the location to the default location if we are set to useDefaults.
	 */
	private void setLocationForSelection() {
		if (useDefaults) {
			IPath defaultPath = Platform.getLocation().append(getProjectNameFieldValue());
			locationPathField.setText(defaultPath.toOSString());
		}
	}
	
	/**
	 * Returns whether this page's controls currently all contain valid 
	 * values.
	 *
	 * @return <code>true</code> if all controls are valid, and
	 *   <code>false</code> if at least one is invalid
	 */
	private boolean validatePage() {
		IWorkspace workspace = WorkbenchPlugin.getPluginWorkspace();

		String projectFieldContents = getProjectNameFieldValue();
		if (projectFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage(WorkbenchMessages.getString("WizardNewProjectCreationPage.projectNameEmpty")); //$NON-NLS-1$
			return false;
		}

		IStatus nameStatus = workspace.validateName(projectFieldContents, IResource.PROJECT);
		if (!nameStatus.isOK()) {
			setErrorMessage(nameStatus.getMessage());
			return false;
		}

		String locationFieldContents = getProjectLocationFieldValue();

		if (locationFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage(WorkbenchMessages.getString("WizardNewProjectCreationPage.projectLocationEmpty")); //$NON-NLS-1$
			return false;
		}

		IPath path = new Path(""); //$NON-NLS-1$
		if (!path.isValidPath(locationFieldContents)) {
			setErrorMessage(WorkbenchMessages.getString("WizardNewProjectCreationPage.locationError")); //$NON-NLS-1$
			return false;
		}
		if (!useDefaults && Platform.getLocation().isPrefixOf(new Path(locationFieldContents))) {
			setErrorMessage(WorkbenchMessages.getString("WizardNewProjectCreationPage.defaultLocationError")); //$NON-NLS-1$
			return false;
		}

		if (getProjectHandle().exists()) {
			setErrorMessage(WorkbenchMessages.getString("WizardNewProjectCreationPage.projectExistsMessage")); //$NON-NLS-1$
			return false;
		}

		setErrorMessage(null);
		setMessage(null);
		return true;
	}
}