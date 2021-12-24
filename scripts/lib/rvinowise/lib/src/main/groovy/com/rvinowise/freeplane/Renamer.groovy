package com.rvinowise.freeplane

import groovy.util.logging.Log
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter
import java.util.logging.Level
import groovy.io.FileType

import groovy.time.TimeCategory 
import groovy.time.TimeDuration
import org.apache.commons.io.FilenameUtils
import java.net.URI
import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.Files

@Log
class Renamer {

def c
def node
def user_dir
def file_system
def opened_mindmaps
def global_root

Renamer(_c, _node) {
    c = _c
    node = _node
	user_dir = c.getUserDirectory()
	file_system = new File_system(c, node);
	global_root = java.nio.file.Paths.get(
		"D:\\archive\\cloud_storages\\google"
	)
	
    FileHandler handler = new FileHandler("$user_dir/logs/Renamer.log", true);
	handler.setFormatter(new SimpleFormatter())
	log.addHandler(handler)
	log.setLevel(Level.OFF)
	log.info("Renamer is constructed")
	handler.flush()
}

public def rename_mindmap(node) {
	log.info("renaming mindmap $node")
	
	def file = node.link.file
	if (!file) {
		return
	}
	
	def basedir = node.map.file.parentFile
	log.info("basedir=$basedir")
	def new_path = java.nio.file.Paths.get(
		file.parentFile.toString(), 
		FilenameUtils.getBaseName(node.text)+"."+
		FilenameUtils.getExtension(file.name)
	)

	def result = rename_with_references(
		basedir,
		file,
		new_path
	)
	if (!result) {
		c.statusInfo = "can't rename $file into $new_path"
		log.info("can't rename $file into $new_path")
		return
	}
	
	rename_root(new_path.toFile(), FilenameUtils.removeExtension(node.text))
	focus_on_mindmap(node.map)
}



def rename_with_references(root_dir, old_file, new_path) {
	log.info("renaming the file with all references: $old_file into $new_path")
	Path old_path = old_file.toPath().toAbsolutePath().normalize()
	log.info("old_path after normalization: $old_path")
	def result = Files.move(old_path, new_path)
	if (!result) {
		c.statusInfo = "can't rename $old_path into $new_path"
		log.info("can't rename $old_path into $new_path")
		return result
	}
	
	root_dir.traverse(type: groovy.io.FileType.FILES) { file ->
		if (FilenameUtils.getExtension(file.name) == 'mm') { 
			rename_references(file, old_path, new_path.toFile())
		}
	}
	return result
}

def rename_references(mindmap_file, old_path, new_file) {
	log.info("renaming references in mindmap=$mindmap_file")
	def mindmap = c.mapLoader(mindmap_file).withView().load()
	mindmap.root.findAll().each{node ->
		if (!node.link.file) {
			return
		}
		def checked_path = node.link.file.toPath().toAbsolutePath().normalize()
		log.info("checking node: $node\n node_file= "+checked_path.toString()+"\n old_path: "+old_path.toString())
		if (checked_path.toString() == old_path.toString()) {
			file_system.reference_file(node, new_file)
		}
	}
	mindmap.save(false)
	if (!opened_mindmaps.contains(mindmap)) {
		mindmap.close(true, false)
	}
}




def rename_root(mindmap_file, name) {
	def mindmap = c.mapLoader(mindmap_file).withView().load()

	mindmap.root.text = name
	mindmap.save(false)
	mindmap.close(true, false)
}

def focus_on_mindmap(mindmap) {
	c.mapLoader(mindmap.file).withView().load()
}

def wrong_link_name(node) {
	//def folder = file_system.find_folder_back_in_hierarchy(path_node)
	if (node.link.file) {
		return node.text != FilenameUtils.getBaseName(node.link.file.name)
	}
	return false
}
public def create_folder_for_branch(new_path_node) {
	def folder = file_system.obtain_folders(new_path_node)
	file_system.reference_file(new_path_node, folder)
	new_path_node.children.each() {node ->
		reroute_paths_for_branch(node)
	}
}
public def reroute_paths_for_branch(path_node) {
	log.info("beginning of reroute_paths_for_branch, path_node= "+path_node.toString())
	
	opened_mindmaps = c.getOpenMaps()
	
	path_node.findAll().each() {node ->
		log.info("checking a node for changing path: "+node.toString())
		if (node.link.file) {
			reroute_path(node)
		}
	}
}

private def reroute_path(path_node) {
	def folder = file_system.find_folder_back_in_hierarchy(path_node.parent)
	log.info("beginning of reroute_path, folder= "+folder.toString())
	def file = path_node.link.file
	def new_path = java.nio.file.Paths.get(
		folder.toString(), 
		FilenameUtils.getBaseName(path_node.text)+"."+
		FilenameUtils.getExtension(file.name)
	)
	if (new_path.toString() != file.toString()) {
		rename_with_references(
			global_root.toFile(),
			path_node.link.file,
			new_path
		)
	}
}




}


