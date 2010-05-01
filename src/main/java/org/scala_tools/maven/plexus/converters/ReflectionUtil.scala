package org.scala_tools.maven.plexus.converters

object ReflectionUtil {
  
  /**
	 * Attempts to determine the "type" of a var on a scala object.
	 * 
	 * TODO - figure out what to do with type erasure + generics...
	 * 
	 * @param obj
	 *             The scala object
	 * @param varName
	 *             The name of the var
	 * @return
	 *             The type of the var, or None if no var is found
	 */
  def getVarType(obj : AnyRef, varName : String) : Option[Class[_]] = {
    val potentials = for { method <- obj.getClass.getMethods
         if varName.equals(method.getName) && method.getParameterTypes.size == 0
    } yield method.getReturnType
    potentials.headOption
  }
  
  private def getVarSetMethod(obj : AnyRef, varName : String) : Option[java.lang.reflect.Method] = {
    val varSetterName = varName + "_$eq";
    val methods = for { method <- obj.getClass.getMethods 
        if varSetterName.equals(method.getName)
    } yield method
    methods.headOption
  }
	/**
	 * This method will inject a value into a "var" on a scala object.
	 * @param obj
	 *          The scala object
	 * @param varName
	 *          The name of the var
	 * @param value
	 *          The value to inject
	 * @throws IllegalArgumentException
	 *          This is thrown if any error occurs (i.e. invalid inputs...)
	 */
  def injectIntoVar[A <: AnyRef](obj : AnyRef, varName : String, value : A) {
    getVarSetMethod(obj, varName) match {
      case Some(method) =>
        val varType = method.getParameterTypes.head
        val realClass = value.getClass
        if(!varType.isAssignableFrom(realClass)) {
          throw new IllegalArgumentException("Can not coerce: " + realClass + " into a " + varType);
        }
        //TODO - Handle boxing/unboxing...
        method.invoke(obj, value);
      case _ => throw new IllegalArgumentException("Invalid var for injection: " + varName + " on: " + obj);
    }
  }
}
