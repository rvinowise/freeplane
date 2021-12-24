
import groovy.util.logging.Log
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter
import groovy.io.FileType

import groovy.time.TimeCategory 
import groovy.time.TimeDuration
import org.apache.commons.io.FilenameUtils
//import groovyx.net.http.URIBuilder
import groovyx.net.http.*
import java.nio.file.Paths

@Log
class Reference_relativiser {

def c
def node
def user_dir

Reference_relativiser(_c, _node) {
    c = _c
    node = _node
	user_dir = c.getUserDirectory()

    FileHandler handler = new FileHandler("$user_dir/logs/Reference_relativiser.log", true);
	handler.setFormatter(new SimpleFormatter())
	log.addHandler(handler)
	
	log.info("Reference_relativiser is constructed")
	handler.flush()
}

def relativise_recursive(root_dir) {
	new File(root_dir).traverse(type: groovy.io.FileType.FILES) { file ->
		if (FilenameUtils.getExtension(file.name) == 'mm') { 
			if (file != node.map.file) {
				relativise_references(file)
			}
		}
	}	
}

def relativise_references(mindmap_file) {
	def mindmap = c.mapLoader(mindmap_file).withView().load()
	mindmap.root.findAll().each{n ->
		def test = n.link.file
		if (node_references_external_file(n)) {
			relativise_reference(n)
		}
	}
}

def node_references_external_file(in_node) {
	def file = in_node.link.file
	if ((file)&&
	(file.toString() != in_node.map.file.parent.toString()) )
	{
		return true
	}
	return false
}

def relativise_reference(in_node) {
	def file = in_node.link.file
	if (file) {
		def basedir = in_node.map.file.parent
		
		def new_uri = get_relative_uri(file, basedir)
		log.info("new_uri="+new_uri.toString())
		//in_node.link.uri = new_uri
		//log.info("relativise_reference. in_node=$in_node, old_file= $file old_uri="+in_node.link.uri.toString()+", new_uri="+new_uri.toString())
	}
}

def get_relative_uri(file, basedir) {
	def file_path = Paths.get(file.getAbsolutePath())
	def base = Paths.get(basedir)
	log.info("file_path=$file_path")
	log.info("base=$base")
	def relative_path = base.relativize( file_path )
	log.info("relative_path=$relative_path")
	def relative_uri = relative_path.toUri()
	log.info("relative_uri=$relative_uri")

	
	return null
}
/*
def rename_mindmap(node) {
	log.info("rename_mindmap $node")
	
	def file = node.link.file
	if (!file) {
		return
	}
	def basedir = node.map.file.parent
	def new_file = java.nio.file.Paths.get(
		file.parentFile.absolutePath, 
		ensure_extension(node.text, FilenameUtils.getExtension(file.name))
	).toFile()
	
	rename_with_references(
		basedir,
		file,
		new_file
	)

	node.link.uri = get_relative_uri(new_file, basedir)
	
	rename_root(new_file, FilenameUtils.removeExtension(node.text))
	focus_on_mindmap(node.map)
}





def rename_referenced_file(node, old_file, new_file) {
	log.info("checking a reference: "+node.name+" "+old_file.toString()+" "+new_file.toString())
	def uri = new URIBuilder(node.link.uri)
	def referenced_node = uri.fragment
	
	if (node.link.file == old_file) {
		def basedir = node.map.file.parent
		def new_uri = new URIBuilder(get_relative_uri(new_file, basedir))
		new_uri.fragment = referenced_node
		//node.link.uri = new_uri
		log.info("renaming a reference: "+node.name+" "+old_file.toString()+" "+new_file.toString())
	}
}

def ensure_extension(String name, String extension) {
	return "$name.$extension"
}
	

def rename_root(mindmap_file, name) {
	def mindmap = c.mapLoader(mindmap_file).withView().load()

	mindmap.root.text = name
	mindmap.save(false)
	mindmap.close(true, false)
}

def focus_on_mindmap(mindmap) {
	c.mapLoader(mindmap.file).withView().load()
}*/

}


Reference_relativiser relativiser = new Reference_relativiser(c, node)
relativiser.relativise_recursive(node.map.file.parent)