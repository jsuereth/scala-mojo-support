package org.scala_tools.maven.mojo.annotations




// Annotations for var's / constructor args
/** Used to define a parameter from a var field */
class parameter extends StaticAnnotation
/** Used to mark a parameter as required to execute*/
class required extends StaticAnnotation
/** Used to define a parameter as readonly - Cannot be replaced by user in POM */
class readOnly extends StaticAnnotation
/** Expression to use when setting a parameter */
class expression(value : String) extends StaticAnnotation
/** Alias to use for a parameter in the pom */
class alias(value : String) extends StaticAnnotation
/** Injects a plexus component */
class component(role : String, roleHint : Option[String]) extends StaticAnnotation



//Annotations for Mojos
/** @configurator <roleHint> 	The configurator type to use when injecting parameter values into this Mojo. The value is normally deduced from the Mojo's implementation language, but can be specified to allow a custom ComponentConfigurator implementation to be used. NOTE: This will only be used in very special cases, using a highly controlled vocabulary of possible values. (Elements like this are why it's a good idea to use the descriptor tools.) */
class configurator(roleHint : String ) extends StaticAnnotation
/** Binds this Mojo to a particular phase of the standard build lifecycle, if specified. NOTE: This is only required if this Mojo is to participate in the standard build process. */
class phase(val name : String) extends StaticAnnotation
class goal(val name : String) extends StaticAnnotation
// I split the executes into separate annotations for easier use in code
class executeGoal(value : String) extends StaticAnnotation
class executePhase(value : String) extends StaticAnnotation
class executePhaseInLifecycle(phase : String, lifecycle : String) extends StaticAnnotation
class executionStrategy extends StaticAnnotation
class inheritByDefault(value : Boolean)	extends StaticAnnotation
class instantiationStrategy(value : String) extends StaticAnnotation
class requiresDependencyResolution(scope : String) extends StaticAnnotation
class requiresDirectInvocation(value : Boolean) extends StaticAnnotation
class requiresOnline(value : Boolean) extends StaticAnnotation
class requiresProject(value : Boolean) extends StaticAnnotation
class requiresReports(value : Boolean) extends StaticAnnotation
class description(value : String) extends StaticAnnotation
class since(value : String) extends StaticAnnotation


