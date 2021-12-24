import groovy.util.logging.Log
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter
import groovy.io.FileType
import java.nio.file.Files

import groovy.time.TimeCategory 
import groovy.time.TimeDuration

import com.rvinowise.freeplane.*

Mindmap_creator creator = new Mindmap_creator(node, c)

creator.create_mindmap(node)

@Log
class Mindmap_creator {
	

def c
def node
def user_dir
def nm
def file_system

Mindmap_creator(_node, _c) {
    c = _c
    node = _node
	nm = new NodeManipulator(c, node);
	file_system = new File_system(c, node);
	
    user_dir = c.getUserDirectory()
    
    FileHandler handler = new FileHandler("$user_dir/logs/create_mindmap.log", true);
	handler.setFormatter(new SimpleFormatter())
	log.addHandler(handler)
}

def create_mindmap(map_node) {
    def folder = file_system.find_folder_back_in_hierarchy(map_node)
	def type = find_type_in_ancestors(map_node)
	def new_map_path = java.nio.file.Paths.get(folder.toString(), map_node.text+".mm",)
	def new_map_file = Files.copy(
		map_node.map.file.toPath(),
		new_map_path
	).toFile()
	
	map_node.link.file = new_map_file
	map_node.map.save(false)
	
	c.statusInfo = "new: "+new_map_file+" old: " +new_map_file
	
	prepare_new_map(new_map_file, map_node.text, type)
}

def create_mindmap_OLD(map_node) {
	def template_file = new File("D:\\archive\\cloud_storages\\google\\religion\\doubts.mm")

    def folder = file_system.find_folder_back_in_hierarchy(map_node)
	def type = find_type_in_ancestors(map_node)
	def new_map_file = java.nio.file.Paths.get(folder.toString(), map_node.text+".mm",).toFile()
	
	def loader = c.mapLoader(template_file)
	def template_map = loader.withView().load()
	log.info("new mindmap filename = "+new_map_file.toString())
	def result = template_map.saveAs(new_map_file)
	c.statusInfo = result
	template_map.close(true, false)
	
	
	
	map_node.link.file = new_map_file
	map_node.map.save(false)
	
	c.statusInfo = "new: "+new_map_file+" old: " +template_file
	
	prepare_new_map(new_map_file, map_node.text, type)
}

def prepare_new_map(new_map_file, name, type) {
	def loader = c.mapLoader(new_map_file)
	def new_map = loader.withView().load()
	init_template_map(new_map, name, type)
	new_map.save(false) 
}



def find_type_in_ancestors(node) {
	def type_node = nm.find_back_in_hierarchy(
		node, 
		this.&is_type_node, 
		node.mindMap.root
	)
	
	if (type_node) {
		return type_node.text
	}		
	
	return ""
}

def init_template_map(map, name, type) {
	map.root.text = name
	map.root.children.each{ child->
		if (!(child.isLeft())) {
			child.delete()
		} else
		if (child.text.startsWith("type")) {
			child.delete()
		}
	}
	
	add_type(map.root, type)
	add_blank_node(map.root)
}

def add_type(root, type) {
	def type_node = root.createChild(0)
	type_node.text = "type: $type"
	type_node.left = true
}

def add_blank_node(root) {
	def blank_node = root.createChild()
	blank_node.left = false
	c.select(blank_node)
}




def is_type_node(node) {
	return (node.icons.contains("info_circle@b"))
}

}

