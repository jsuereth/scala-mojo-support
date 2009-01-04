package org.scala_tools.maven.plexus

import org.codehaus.plexus.component.configurator._;
import org.codehaus.plexus.component.configurator.converters._;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.classworlds.ClassRealm;

/**
 * This class is responsible for configuring (injecting?) a scala component from Plexus Configuration.
 */
class ScalaComponentConfigurator extends AbstractComponentConfigurator {
  @throws(classOf[ComponentConfigurationException])
  override def configureComponent(component : AnyRef,
		    configuration : PlexusConfiguration ,
			expressionEvaluator : ExpressionEvaluator, 
            containerRealm : ClassRealm ,
			listener : ConfigurationListener ) {
		converterLookup.registerConverter( new ClassRealmConverter( containerRealm ) );
		
		//This makes sure we configure the scala component appropriately.
        val converter = new ScalaConfigurationConverter();
        converter.processConfiguration( converterLookup, component, containerRealm.getClassLoader(), configuration,
                expressionEvaluator, listener );
	}
}

/**
 * This class is responsible for converting configuration into injected dependencies.
 */
class ScalaConfigurationConverter extends AbstractConfigurationConverter {

	override def canConvert(someType : Class[_]) : Boolean = {
		// We should be able to handle every java type.
		return true;
	}
    @throws(classOf[ComponentConfigurationException])
	override def fromConfiguration(converterLookup : ConverterLookup, configuration : PlexusConfiguration, 
                                someType : Class[_],
            baseType : Class[_],  classLoader : ClassLoader, expressionEvaluator : ExpressionEvaluator ,
             listener : ConfigurationListener) : AnyRef = {
		
		var retValue = fromExpression( configuration, expressionEvaluator, someType );
		System.err.println("Result of expression evaluation: " + retValue);
        if ( retValue == null )
        {
            try {
                // it is a "composite" - we compose it from its children. It does not have a value of its own
                val implementation = getClassForImplementationHint( someType, configuration, classLoader );

                retValue = instantiateObject( implementation );

                processConfiguration( converterLookup, retValue, classLoader, configuration, expressionEvaluator, listener );
            } catch {
              case e : ComponentConfigurationException =>
                if ( e.getFailedConfiguration() == null )
                {
                    e.setFailedConfiguration( configuration );
                }
                throw e;
              case t : Throwable => throw t;
            }
        }
        retValue;
	}
 
    @throws(classOf[ComponentConfigurationException])
	def processConfiguration(converterLookup : ConverterLookup,
			component : AnyRef, classLoader : ClassLoader,
			configuration : PlexusConfiguration,
			expressionEvaluator : ExpressionEvaluator,
			listener : ConfigurationListener)  {
		//TODO - Inject into component the configuration properties.
		val items = configuration.getChildCount();
		System.err.println("Processing Configuration for child elements");
        for (  i <- 0 until items ) {
            val childConfiguration = configuration.getChild( i );

            val elementName = childConfiguration.getName();
            System.err.println("Processing child [" + elementName + "]");
            
            //TODO - Make sure the var setter is working correctly and we're passing the correct information to it.
            //TODO - Make sure we can handle "property object" configuration items.
            val varSetter = new ScalaVarSetter(fromXML( elementName ), component, converterLookup, listener);
            varSetter.configure(childConfiguration, classLoader, expressionEvaluator);
        }	
	}
}

/**
 * This class is responsible for injecting dependencies into a component for a given "var".
 * 
 * In its constructor, it attempts to grab the "var" from the given "obj".  It will then try to find a "converter"
 * that will convert configuration into the required type for the "var".
 * 
 * If any of the above fails, an exception is thrown.
 * 
 */
class ScalaVarSetter(fieldName : String, obj : AnyRef, lookup : ConverterLookup, listener : ConfigurationListener) {
    //TODO - Handle errors!
    import converters.ReflectionUtil._
	val setterParamType = getVarType(obj, fieldName).get
    val setterTypeConverter = lookup.lookupConverterForType( setterParamType );
	def configure( config : PlexusConfiguration , cl : ClassLoader , evaluator : ExpressionEvaluator  ) {
	   val value = setterTypeConverter.fromConfiguration( lookup, config, setterParamType, obj.getClass(), cl,
	                                                              evaluator, listener );
	   if ( value != null ) {
	      setValueUsingSetter( value );
	   }
	} 
 
    private def setValueUsingSetter(value : AnyRef) {
       if ( setterParamType == null ) {
	            throw new ComponentConfigurationException( "No setter found" );
	        }

	        val exceptionInfo = obj.getClass().getName() + "." + fieldName + " = ( " + setterParamType.getClass().getName() + " )";

	        if ( listener != null ) {
	            listener.notifyFieldChangeUsingSetter( fieldName, value, obj );
	        }

	        try {
	            import converters.ReflectionUtil._
	        	//TODO - make sure this is correct.
	        	injectIntoVar(obj, fieldName, value);
	        } catch {
	          case e : IllegalArgumentException =>
	            throw new ComponentConfigurationException( "Invalid parameter supplied while setting '" + value + "' to "
	                + exceptionInfo, e );
	        }
    }
}
