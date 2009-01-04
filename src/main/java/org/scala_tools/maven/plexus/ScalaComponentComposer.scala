package org.scala_tools.maven.plexus

import java.util.List;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.composition.AbstractComponentComposer;
import org.codehaus.plexus.component.composition.ComponentComposer;
import org.codehaus.plexus.component.composition.CompositionException;
import org.codehaus.plexus.component.composition.UndefinedComponentComposerException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;

class ScalaComponentComposer extends AbstractComponentComposer {
  override def assembleComponent(component : AnyRef,
			componentDescriptor : ComponentDescriptor, container : PlexusContainer) : List[_] = {
		//TODO - Is this where we inject dependencies.
		 null;
	}

}
