package de.uka.ilkd.key.logic.op;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;

public interface IObserverFunction extends SortedOperator {

   /**
    * Returns the result type of this symbol.
    */
   public abstract KeYJavaType getType();

   /**
    * Returns the container type of this symbol; for non-static observer 
    * symbols, this corresponds to the sort of its second argument.
    */
   public abstract KeYJavaType getContainerType();

   /**
    * Tells whether the observer symbol is static.
    */
   public abstract boolean isStatic();

   /**
    * Gives the number of parameters of the observer symbol. "Parameters" here
    * includes only the *explicit* parameters, not the heap and the receiver
    * object. Thus, for observer symbols representing model fields, this will
    * always return 0.
    */
   public abstract int getNumParams();

   /**
    * Gives the type of the i-th parameter of this observer symbol. 
    * "Parameters" here includes only the *explicit* parameters, not the heap 
    * and the receiver object. 
    */
   public abstract KeYJavaType getParamType(int i);

   /**
    * Returns the parameter types of this observer symbol. "Parameters" here
    * includes only the *explicit* parameters, not the heap and the receiver
    * object. 
    */
   public abstract ImmutableArray<KeYJavaType> getParamTypes();

}