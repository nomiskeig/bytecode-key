package org.key_project.sed.key.ui.launch;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.internal.debug.ui.launcher.AbstractJavaMainTab;
import org.eclipse.jdt.internal.debug.ui.launcher.DebugTypeSelectionDialog;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.key_project.key4eclipse.starter.core.property.KeYResourceProperties;
import org.key_project.key4eclipse.starter.core.provider.ImmutableCollectionContentProvider;
import org.key_project.key4eclipse.starter.core.util.KeYUtil;
import org.key_project.sed.key.core.launch.KeYLaunchSettings;
import org.key_project.sed.key.core.util.KeySEDUtil;
import org.key_project.sed.key.ui.dialog.ContractSelectionDialog;
import org.key_project.sed.key.ui.jdt.AllOperationsSearchEngine;
import org.key_project.sed.key.ui.jdt.AllTypesSearchEngine;
import org.key_project.sed.key.ui.util.LogUtil;
import org.key_project.util.eclipse.ResourceUtil;
import org.key_project.util.eclipse.swt.SWTUtil;
import org.key_project.util.java.ObjectUtil;
import org.key_project.util.java.StringUtil;
import org.key_project.util.java.SwingUtil;
import org.key_project.util.java.thread.AbstractRunnableWithProgressAndResult;
import org.key_project.util.java.thread.AbstractRunnableWithResult;
import org.key_project.util.java.thread.IRunnableWithProgressAndResult;
import org.key_project.util.java.thread.IRunnableWithResult;
import org.key_project.util.jdt.JDTUtil;

import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.logic.op.IProgramMethod;
import de.uka.ilkd.key.proof.init.InitConfig;
import de.uka.ilkd.key.speclang.FunctionalOperationContract;

/**
 * Contains the controls to define a project, type, method and an operation contract to debug.
 * @author Martin Hentschel
 */
@SuppressWarnings("restriction")
public class MainLaunchConfigurationComposite extends AbstractTabbedPropertiesAndLaunchConfigurationTabComposite {
   /**
    * Defines the project that contains the type to debug.
    */
   private Text projectText;
   
   /**
    * Defines the type to debug.
    */
   private Text typeText;
   
   /**
    * Defines the method to debug.
    */
   private Text methodText;
   
   /**
    * Defines that a default contract will be generated.
    */
   private Button useGeneratedContractButton;
   
   /**
    * Defines that one existing contract will be used.
    */
   private Button useExistingContractButton;

   /**
    * Defines the existing contract to use.
    */
   private Text existingContractText;
   
   /**
    * Last loaded {@link InitConfig}.
    */
   private InitConfig initConfig;
   
   /**
    * The name of the project that is loaded in {@link #initConfig}.
    */
   private String initConfigProject;
   
   /**
    * {@link Button} to browse a contract.
    */
   private Button browseContractButton;
   
   /**
    * The last defined existing contract.
    */
   private String lastDefinedExistingContract;

   /**
    * Constructor.
    * @param parent The parent {@link Composite}.
    * @param style The style.
    * @param parentTab An optional {@link AbstractTabbedPropertiesAndLaunchConfigurationTab} to make this {@link Composite} editable.
    * @param widgetFactory An optional {@link TabbedPropertySheetWidgetFactory} to use.
    */
   public MainLaunchConfigurationComposite(Composite parent, 
                                           int style, 
                                           AbstractTabbedPropertiesAndLaunchConfigurationTab parentTab,
                                           TabbedPropertySheetWidgetFactory widgetFactory) {
      super(parent, style, parentTab);
      setLayout(new FillLayout());
      if (widgetFactory == null) {
         widgetFactory = new NoFormTabbedPropertySheetWidgetFactory();
      }
      // Content composite
      Composite composite = widgetFactory.createFlatFormComposite(this);
      composite.setLayout(new GridLayout(1, false));
      // Project
      Group projectGroup = widgetFactory.createGroup(composite, "Project");
      projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      projectGroup.setLayout(new GridLayout(isEditable() ? 3 : 2, false));
      widgetFactory.createLabel(projectGroup, "&Project name");
      projectText = widgetFactory.createText(projectGroup, null);
      projectText.setEditable(isEditable());
      projectText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      projectText.addModifyListener(new ModifyListener() {
          @Override
          public void modifyText(ModifyEvent e) {
              unsetInitConfig();
              updateLaunchConfigurationDialog();
          }
      });
      if (isEditable()) {
         Button browseProjectButton = widgetFactory.createButton(projectGroup, "B&rowse", SWT.PUSH);
         browseProjectButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 browseProject();
             }
         });
      }
      // Java
      Group javaGroup = widgetFactory.createGroup(composite, "Java");
      javaGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      javaGroup.setLayout(new GridLayout(isEditable() ? 3 : 2, false));
      widgetFactory.createLabel(javaGroup, "&Type");
      typeText = widgetFactory.createText(javaGroup, null);
      typeText.setEditable(isEditable());
      typeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      typeText.addModifyListener(new ModifyListener() {
          @Override
          public void modifyText(ModifyEvent e) {
              updateLaunchConfigurationDialog();
          }
      });
      if (isEditable()) {
         Button browseTypeButton = widgetFactory.createButton(javaGroup, "&Browse", SWT.PUSH);
         browseTypeButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 browseType();
             }
         });
      }
      widgetFactory.createLabel(javaGroup, "&Method");
      methodText = widgetFactory.createText(javaGroup, null);
      methodText.setEditable(isEditable());
      methodText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      methodText.addModifyListener(new ModifyListener() {
          @Override
          public void modifyText(ModifyEvent e) {
              updateLaunchConfigurationDialog();
          }
      });
      if (isEditable()) {
         Button browseMethodButton = widgetFactory.createButton(javaGroup, "Bro&wse", SWT.PUSH);
         browseMethodButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 browseMethod();
             }
         });
      }
      // Verification
      Group verificationGroup = widgetFactory.createGroup(composite, "Verification");
      verificationGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      verificationGroup.setLayout(new GridLayout(1, false));
      Composite usedContractComposite = widgetFactory.createPlainComposite(verificationGroup, SWT.NONE);
      usedContractComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      usedContractComposite.setLayout(new GridLayout(2, false));
      useGeneratedContractButton = widgetFactory.createButton(usedContractComposite, "Use &generated contract", SWT.RADIO);
      useGeneratedContractButton.setEnabled(isEditable());
      useGeneratedContractButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
              if (useGeneratedContractButton.getSelection()) {
                  updateLaunchConfigurationDialog();
                  updateExistingContractState();
              }
          }
      });
      useExistingContractButton = widgetFactory.createButton(usedContractComposite, "Use &existing contract", SWT.RADIO);
      useExistingContractButton.setEnabled(isEditable());
      useExistingContractButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
              if (useExistingContractButton.getSelection()) {
                  updateLaunchConfigurationDialog();
                  updateExistingContractState();
              }
          }
      });
      Composite existingContractComposite = widgetFactory.createPlainComposite(verificationGroup, SWT.NONE);
      existingContractComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      existingContractComposite.setLayout(new GridLayout(isEditable() ? 3 : 2, false));
      widgetFactory.createLabel(existingContractComposite, "Contr&act");
      existingContractText = widgetFactory.createText(existingContractComposite, null);
      existingContractText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      existingContractText.addModifyListener(new ModifyListener() {
          @Override
          public void modifyText(ModifyEvent e) {
              updateLaunchConfigurationDialog();
          }
      });
      if (isEditable()) {
         browseContractButton = widgetFactory.createButton(existingContractComposite, "Brow&se", SWT.PUSH);
         browseContractButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 browseContract();
             }
         });
      }
   }

   /**
    * Updates the shown operation contract.
    */
   protected void updateExistingContractState() {
       boolean useExistingContract = useExistingContractButton.getSelection();
       existingContractText.setEditable(useExistingContract && isEditable());
       if (browseContractButton != null) {
          browseContractButton.setEnabled(useExistingContract && isEditable());
       }
       if (useExistingContract) {
           SWTUtil.setText(existingContractText, lastDefinedExistingContract);
       }
       else {
           lastDefinedExistingContract = existingContractText.getText();
           existingContractText.setText(StringUtil.EMPTY_STRING);
       }
   }

   /**
    * Unsets the loaded {@link InitConfig}.
    */
   protected void unsetInitConfig() {
       if (!ObjectUtil.equals(initConfigProject, projectText.getText())) {
           initConfig = null;
           initConfigProject = null;
       }
   }

   /**
    * Opens a dialog to select a contract for the specified method.
    */
   public void browseContract() {
       try {
           final IMethod method = getMethod();
           if (method != null && method.exists()) {
               IProject project = method.getResource().getProject();
               final File location = ResourceUtil.getLocation(project);
               final File bootClassPath = KeYResourceProperties.getKeYBootClassPathLocation(project);
               final List<File> classPaths = KeYResourceProperties.getKeYClassPathEntries(project);
               // Load location
               if (initConfig == null) {
                   IRunnableWithProgressAndResult<InitConfig> run = new AbstractRunnableWithProgressAndResult<InitConfig>() {
                       @Override
                       public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                           SWTUtil.checkCanceled(monitor);
                           monitor.beginTask("Receiving contracts.", IProgressMonitor.UNKNOWN);
                           IRunnableWithResult<InitConfig> run = new AbstractRunnableWithResult<InitConfig>() {
                               @Override
                               public void run() {
                                   try {
                                       InitConfig initConfig = KeYUtil.internalLoad(location, classPaths, bootClassPath, false);
                                       setResult(initConfig);
                                   }
                                   catch (Exception e) {
                                       setException(e);
                                   }
                               }
                           };
                           SWTUtil.checkCanceled(monitor);
                           SwingUtil.invokeAndWait(run);
                           if (run.getException() != null) {
                               throw new InvocationTargetException(run.getException());
                           }
                           SWTUtil.checkCanceled(monitor);
                           setResult(run.getResult());
                           monitor.done();
                       }
                   };
                   getLaunchConfigurationDialog().run(true, false, run);
                   initConfig = run.getResult();
                   initConfigProject = project.getName();
               }
               if (initConfig != null) {
                   // Get method to proof in KeY
                   IProgramMethod pm = KeYUtil.getProgramMethod(method, initConfig.getServices().getJavaInfo());
                   if (pm != null) {
                       KeYJavaType type = pm.getContainerType();
                       ImmutableSet<FunctionalOperationContract> operationContracts = initConfig.getServices().getSpecificationRepository().getOperationContracts(type, pm);
                       // Open selection dialog
                       Services services = initConfig.getServices();
                       ContractSelectionDialog dialog = new ContractSelectionDialog(getShell(), ImmutableCollectionContentProvider.getInstance(), services);
                       dialog.setTitle("Contract selection");
                       dialog.setMessage("Select a contract to debug.");
                       dialog.setInput(operationContracts);
                       FunctionalOperationContract selectedContract = KeySEDUtil.findContract(operationContracts, getContractId());
                       if (selectedContract != null) {
                           dialog.setInitialSelections(new Object[] {selectedContract});
                       }
                       if (dialog.open() == ContractSelectionDialog.OK) {
                           Object result = dialog.getFirstResult();
                           if (result instanceof FunctionalOperationContract) {
                               FunctionalOperationContract foc = (FunctionalOperationContract)result;
                               existingContractText.setText(foc.getName());
                           }
                       }
                   }
                   else {
                       throw new IllegalStateException("Can't find method \"" + JDTUtil.getQualifiedMethodLabel(method) + "\" in KeY.");
                   }
               }
           }
       }
       catch (Exception e) {
           LogUtil.getLogger().logError(e);
           LogUtil.getLogger().openErrorDialog(getShell(), e);
       }
   }

   /**
    * <p>
    * Opens the dialog to select a Java project.
    * </p>
    * <p>
    * The implementation is oriented at {@link AbstractJavaMainTab#handleProjectButtonSelected()}
    * and {@link AbstractJavaMainTab#chooseJavaProject()}.
    * </p>
    */
   public void browseProject() {
       ILabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
       ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
       dialog.setTitle("Project Selection"); 
       dialog.setMessage("Select a project to constrain your search."); 
       try {
           dialog.setElements(JDTUtil.getAllJavaProjects());
       }
       catch (JavaModelException jme) {
           LogUtil.getLogger().logError(jme);
       }
       IJavaProject javaProject = getJavaProject();
       if (javaProject != null) {
           dialog.setInitialSelections(new Object[] {javaProject});
       }
       if (dialog.open() == ElementListSelectionDialog.OK) {
           IJavaProject project = (IJavaProject)dialog.getFirstResult();
           projectText.setText(project.getElementName());
       }               
   }
   
   /**
    * Returns the selected {@link IJavaProject} or {@code null} if no one is defined.
    * @return The selected {@link IJavaProject} or {@code null} if no one is defined.
    */
   protected IJavaProject getJavaProject() {
       String projectName = projectText.getText().trim();
       return JDTUtil.getJavaProject(projectName);
   }

   /**
    * <p>
    * Opens the dialog to select a Java type ({@link IType}).
    * </p>
    * <p>
    * The implementation is oriented at {@link JavaMainTab#handleSearchButtonSelected()}.
    * </p>
    */
   public void browseType() {
       try {
           // Search all Java types
           IJavaProject selectedProject = getJavaProject();
           IJavaElement[] elements;
           if (selectedProject != null && selectedProject.exists()) {
               elements = new IJavaElement[] {selectedProject};
           }
           else {
               elements = JDTUtil.getAllJavaProjects();
           }
           if (elements == null) {
               elements = new IJavaElement[] {};
           }
           IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(elements, IJavaSearchScope.SOURCES);
           AllTypesSearchEngine engine = new AllTypesSearchEngine();
           IType[] types = engine.searchTypes(getLaunchConfigurationDialog(), searchScope);
           // Open selection dialog
           DebugTypeSelectionDialog mmsd = new DebugTypeSelectionDialog(getShell(), types, "Select Type");
           IType selectedType = getType();
           if (selectedType != null) {
               mmsd.setInitialElementSelections(Collections.singletonList(selectedType));
           }
           if (mmsd.open() == DebugTypeSelectionDialog.OK) {
               Object[] results = mmsd.getResult();    
               if (results != null && results.length >= 1 && results[0] instanceof IType) {
                   IType type = (IType)results[0];
                   projectText.setText(type.getJavaProject().getElementName());
                   typeText.setText(type.getFullyQualifiedName());
               }
           }
       }
       catch (Exception e) {
           LogUtil.getLogger().logError(e);
           LogUtil.getLogger().openErrorDialog(getShell(), e);
       }
   }

   /**
    * Returns the currently selected Java type ({@link IType}) or {@code null} if no one is selected.
    * @return The currently selected Java type ({@link IType}) or {@code null} if no one is selected.
    */
   protected IType getType() {
       try {
           String text = typeText.getText().trim();
           if (!text.isEmpty()) {
               IJavaProject project = getJavaProject();
               if (project != null) {
                   return project.findType(text);
               }
               else {
                   return null;
               }
           }
           else {
               return null;
           }
       }
       catch (JavaModelException e) {
           return null;
       }
   }
   
   /**
    * Returns the ID of the existing contract.
    * @return The ID of the existing contract.
    */
   protected String getContractId() {
       return existingContractText.getText();
   }

   /**
    * Opens a dialog to select a Java method ({@link IMethod}).
    */
   public void browseMethod() {
       try {
           // Search all Java types
           IType selectedType = getType();
           IJavaElement[] elements;
           if (selectedType != null && selectedType.exists()) {
               elements = new IJavaElement[] {selectedType};
           }
           else {
               elements = JDTUtil.getAllJavaProjects();
           }
           if (elements == null) {
               elements = new IJavaElement[] {};
           }
           IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(elements, IJavaSearchScope.SOURCES);
           AllOperationsSearchEngine engine = new AllOperationsSearchEngine();
           IMethod[] methods = engine.searchOperations(getLaunchConfigurationDialog(), searchScope);
           // Open selection dialog
           ILabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
           ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
           dialog.setTitle("Method Selection"); 
           dialog.setMessage("Select a method to debug."); 
           dialog.setElements(methods);
           IMethod oldMethod = getMethod();
           if (oldMethod != null) {
               dialog.setInitialSelections(new Object[] {oldMethod});
           }
           if (dialog.open() == ElementListSelectionDialog.OK) {
               IMethod newMethod = (IMethod)dialog.getFirstResult();
               projectText.setText(KeySEDUtil.getProjectValue(newMethod));
               typeText.setText(KeySEDUtil.getTypeValue(newMethod));
               methodText.setText(KeySEDUtil.getMethodValue(newMethod));
           }
       }
       catch (Exception e) {
           LogUtil.getLogger().logError(e);
           LogUtil.getLogger().openErrorDialog(getShell(), e);
       }
   }

   /**
    * Returns the selected Java method ({@link IMethod}) or {@code null}
    * if no one is selected.
    * @return The selected Java method or {@code null} if no one is selected.
    */
   protected IMethod getMethod() {
       try {
           String text = methodText.getText().trim();
           if (!text.isEmpty()) {
               IType type = getType();
               if (type != null) {
                   return JDTUtil.getElementForQualifiedMethodLabel(type.getMethods(), text);
               }
               else {
                   return null;
               }
           }
           else {
               return null;
           }
       }
       catch (JavaModelException e) {
           return null;
       }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getNotValidMessage() {
      String message = null;
      // Validate Java project
      if (message == null) {
          IJavaProject project = getJavaProject();
          if (project == null || !project.exists()) {
              message = "No existing Java project selected.";
          }
      }
      // Validate type
      if (message == null) {
          IType type = getType();
          if (type == null || !type.exists()) {
              message = "No existing type selected.";
          }
      }
      // Validate method
      if (message == null) {
          IMethod method = getMethod();
          if (method == null || !method.exists()) {
              message = "No existing method selected.";
          }
      }
      // Validate contract
      if (message == null) {
          if (useExistingContractButton.getSelection()) {
              if (StringUtil.isTrimmedEmpty(getContractId())) {
                  message = "No existing contract defined.";
              }
          }
      }
      return message;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void initializeFrom(ILaunchConfiguration configuration) {
      try {
         projectText.setText(KeySEDUtil.getProjectValue(configuration));
         typeText.setText(KeySEDUtil.getTypeValue(configuration));
         methodText.setText(KeySEDUtil.getMethodValue(configuration));
         boolean useExistingContract = KeySEDUtil.isUseExistingContractValue(configuration);
         useGeneratedContractButton.setSelection(!useExistingContract);
         useExistingContractButton.setSelection(useExistingContract);
         existingContractText.setText(KeySEDUtil.getExistingContractValue(configuration));
         lastDefinedExistingContract = existingContractText.getText();
         updateExistingContractState();
      } 
      catch (CoreException e) {
         LogUtil.getLogger().logError(e);
      }
   }

   /**
    * Shows the content of the given {@link KeYLaunchSettings}.
    * @param settings The {@link KeYLaunchSettings} to show.
    */
   public void initializeFrom(KeYLaunchSettings settings) {
      try {
         IMethod method = settings.getMethod();
         projectText.setText(KeySEDUtil.getProjectValue(method));
         typeText.setText(KeySEDUtil.getTypeValue(method));
         methodText.setText(KeySEDUtil.getMethodValue(method));
         boolean useExistingContract = settings.isUseExistingContract();
         useGeneratedContractButton.setSelection(!useExistingContract);
         useExistingContractButton.setSelection(useExistingContract);
         existingContractText.setText(settings.getExistingContract());
         lastDefinedExistingContract = existingContractText.getText();
         updateExistingContractState();
      }
      catch (JavaModelException e) {
         LogUtil.getLogger().logError(e);
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void performApply(ILaunchConfigurationWorkingCopy configuration) {
      configuration.setAttribute(KeySEDUtil.LAUNCH_CONFIGURATION_TYPE_ATTRIBUTE_PROJECT, projectText.getText());
      configuration.setAttribute(KeySEDUtil.LAUNCH_CONFIGURATION_TYPE_ATTRIBUTE_TYPE, typeText.getText());
      configuration.setAttribute(KeySEDUtil.LAUNCH_CONFIGURATION_TYPE_ATTRIBUTE_METHOD, methodText.getText());
      configuration.setAttribute(KeySEDUtil.LAUNCH_CONFIGURATION_TYPE_ATTRIBUTE_USE_EXISTING_CONTRACT, useExistingContractButton.getSelection());
      configuration.setAttribute(KeySEDUtil.LAUNCH_CONFIGURATION_TYPE_ATTRIBUTE_EXISTING_CONTRACT, existingContractText.getText());
   }
}