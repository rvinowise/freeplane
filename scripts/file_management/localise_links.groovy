import org.apache.commons.io.FilenameUtils
import groovy.util.logging.Log
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter
import groovy.io.FileType
import groovy.time.TimeCategory 
import groovy.time.TimeDuration


Link_localiser localiser = new Link_localiser(c, node)

localiser.localise_links_in_path(node,c);
//localiser.localise_links_in_mindmap(node.map.file)

@Log
class Link_localiser {
	
def c
def node
def user_dir
def opened_mindmaps

Link_localiser(_c, _node) {
	c = _c
	node = _node
	
	user_dir = c.getUserDirectory()
    FileHandler handler = new FileHandler("$user_dir/logs/Link_localiser.log", true);
	handler.setFormatter(new SimpleFormatter())
	log.addHandler(handler)
	log.info("created")
}
def localise_links_in_path(node,c) {
	
	def root_dir = node.map.file.parent
	opened_mindmaps = c.getOpenMaps()
	new File(root_dir).traverse(type: groovy.io.FileType.FILES) { file ->
		if (FilenameUtils.getExtension(file.name) == 'mm') { 
			localise_links_in_mindmap(file)
			log.info(file.name)
		}
	}
}

def localise_links_in_mindmap(mindmap_file) {
	def mindmap = c.mapLoader(mindmap_file).withView().load()
	mindmap.root.findAll().each{node ->
		localise_link_in_node(node)
	}
	mindmap.save(false)
	if !(mindmap in opened_mindmaps) {
		mindmap.close(true, false)
	}
}

def localise_link_in_node(node) {
	def file = node.link.file
	def old_uri_test = node.link.uri
	if (!file) {
		return
	}
	def uri_str = node.link.uri.toString()
	def referenced_node = ""
	if (uri_str.lastIndexOf("#") != -1) {
		referenced_node = uri_str.substring(uri_str.lastIndexOf("#"));
	}
	log.info("test0")
	def basedir = node.map.file.parent
	def relative_uri = get_relative_uri(file, basedir).toString() + referenced_node
	//node.link.uri = get_relative_uri(file, basedir) + referenced_node
	log.info("localising a reference: "+node.text+" "+old_uri_test.toString()+" "+relative_uri.toString())
}

def get_relative_uri(file, basedir) {
	def file_uri = java.nio.file.Paths.get(file.getAbsolutePath()).toUri()
	log.info("file_uri = $file_uri")
	def base_uri = java.nio.file.Paths.get(basedir).toUri()
	log.info("base_uri = $base_uri")
	def rel_uri = base_uri.relativize( file_uri )
	log.info("url after relativize = $rel_uri")
	return rel_uri
}

}