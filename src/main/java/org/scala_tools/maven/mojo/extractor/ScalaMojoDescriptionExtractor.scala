package org.scala_tools.maven.mojo.extractor

import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.tools.plugin.extractor.ExtractionException;
import org.apache.maven.tools.plugin.extractor.MojoDescriptorExtractor;
import org.apache.maven.tools.plugin.util.PluginUtils;

import scala.collection.JavaConversions._

/**
 * This class is responsible for extracting mojo descriptions from scala files.
 */
class ScalaMojoDescriptionExtractor extends MojoDescriptorExtractor {
    /**
	 * This is responsible for pulling mojo descriptions off of scala files in the project.
	 */
    @throws(classOf[ExtractionException]) 
	override def execute(project : MavenProject, pluginDescriptor : PluginDescriptor) : java.util.List[_]= {
		//TODO - parse through scala file and rip out MOJO annotations
		val sourceFiles = for {
		  root <- project.getCompileSourceRoots().asInstanceOf[java.util.List[String]]
          if new java.io.File(root).isDirectory
          source <- PluginUtils.findSources(root, "**/*.scala")
		} yield root + java.io.File.separator + source
         
        val compiler = new MojoExtractorCompiler(project)
        val mojoDescriptors = compiler.extract(sourceFiles : _*).toArray
        for(mojoDescriptor <- mojoDescriptors) {
          mojoDescriptor.setPluginDescriptor(pluginDescriptor)
          mojoDescriptor.setVersion(project.getModelVersion());
          mojoDescriptor.setLanguage("scala")
        }
        java.util.Arrays.asList(mojoDescriptors : _*)
	}
}
