package org.key_project.sed.key.core.launch;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IMethod;
import org.key_project.sed.core.model.ISEDMethodReturn;
import org.key_project.sed.key.core.model.KeYDebugTarget;

/**
 * Contains the settings used in an {@link ILaunch} which contains a
 * {@link KeYDebugTarget} as unmodifiable backup of the initial
 * {@link ILaunchConfiguration}. This backup is required because changes on
 * the launch configuration are possible during launch execution.
 * @author Martin Hentschel
 */
public class KeYLaunchSettings {
   /**
    * The {@link IMethod} to debug.
    */
   private IMethod method;
   
   /**
    * Use an existing contract or generate default contract?
    */
   private boolean useExistingContract;
   
   /**
    * The ID of the existing contract to use.
    */
   private String existingContract;
   
   /**
    * If this is {@code true} an {@link ISEDMethodReturn} will contain the return value,
    * but the performance will suffer.
    * If it is {@code false} only the name of the returned method is shown in an {@link ISEDMethodReturn}.
    */
   private boolean showMethodReturnValues;
   
   /**
    * Show variables of selected debug node?
    */
   private boolean showVariablesOfSelectedDebugNode;
   
   /**
    * Show KeY's main window?
    */
   private boolean showKeYMainWindow;
   
   /**
    * Merge branch conditions?
    */
   private boolean mergeBranchConditions;

   /**
    * Constructor.
    * @param method The {@link IMethod} to debug.
    * @param useExistingContract Use an existing contract or generate default contract?
    * @param existingContract The ID of the existing contract to use.
    * @param showMethodReturnValues Show method return values of {@link ISEDMethodReturn} instances?
    * @param showVariablesOfSelectedDebugNode Show variables of selected debug node?
    * @param showKeYMainWindow Show KeY's main window?
    * @param mergeBranchConditions Merge branch conditions?
    */
   public KeYLaunchSettings(IMethod method, 
                            boolean useExistingContract, 
                            String existingContract, 
                            boolean showMethodReturnValues,
                            boolean showVariablesOfSelectedDebugNode,
                            boolean showKeYMainWindow,
                            boolean mergeBranchConditions) {
      this.method = method;
      this.useExistingContract = useExistingContract;
      this.existingContract = existingContract;
      this.showMethodReturnValues = showMethodReturnValues;
      this.showVariablesOfSelectedDebugNode = showVariablesOfSelectedDebugNode;
      this.showKeYMainWindow = showKeYMainWindow;
      this.mergeBranchConditions = mergeBranchConditions;
   }

   /**
    * Returns the {@link IMethod} to debug.
    * @return The {@link IMethod} to debug.
    */
   public IMethod getMethod() {
      return method;
   }

   /**
    * Checks if an existing contract or a generate default contract is used?
    * @return Use an existing contract or generate default contract?
    */
   public boolean isUseExistingContract() {
      return useExistingContract;
   }

   /**
    * Returns the ID of the existing contract to use.
    * @return The ID of the existing contract to use.
    */
   public String getExistingContract() {
      return existingContract;
   }

   /**
    * Checks if method return values of {@link ISEDMethodReturn} instances should be shown.
    * @return Show method return values of {@link ISEDMethodReturn} instances?
    */
   public boolean isShowMethodReturnValues() {
      return showMethodReturnValues;
   }

   /**
    * Checks if KeY's main window should be shown.
    * @return Show KeY's main window?
    */
   public boolean isShowKeYMainWindow() {
      return showKeYMainWindow;
   }

   /**
    * Checks if variables of selected debug node should be shown.
    * @return Show variables of selected debug node?
    */
   public boolean isShowVariablesOfSelectedDebugNode() {
      return showVariablesOfSelectedDebugNode;
   }

   /**
    * Checks if branch conditions are merged.
    * @return Merge branch conditions?
    */
   public boolean isMergeBranchConditions() {
      return mergeBranchConditions;
   }
}