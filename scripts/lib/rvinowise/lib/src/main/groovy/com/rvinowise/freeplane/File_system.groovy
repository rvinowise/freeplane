package com.rvinowise.freeplane

import groovy.util.logging.Log
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatterimport java.util.logging.Level

import groovy.io.FileType

import groovy.time.TimeCategory 
import groovy.time.TimeDuration
import org.apache.commons.io.FilenameUtils
import java.net.URI
import java.nio.file.Paths

@Log
class File_system {

def c
def node
def user_dir
def nm

File_system(_c, _node) {
    c = _c
    node = _node
	user_dir = c.getUserDirectory()
	nm = new NodeManipulator(c, node);

    FileHandler handler = new FileHandler("$user_dir/logs/File_system.log", true);
	handler.setFormatter(new SimpleFormatter())
	log.addHandler(handler)	log.setLevel(Level.OFF)
	
	log.info("File_system is constructed")
	handler.flush()
}

public def find_folder_back_in_hierarchy(node) {
	def folder_node = nm.find_back_in_hierarchy(
		node, 
		this.&is_file_node, 
		node.mindMap.root
	)
	
	if (folder_node) {
		return get_folder_from(folder_node)
	}		
	
	return node.map.file.getParentFile()
}

def is_folder_node(node) {
	return (node.link.file != null) && (node.link.file.isDirectory())
}
def is_file_node(node) {
	return (node.link.file != null)
}

def get_folder_from(node) {
	if (node.link.file) {
		if (node.link.file.isDirectory()) {
			return node.link.file
		} else {
			return node.link.file.parentFile
		}
	}
	return null
}

def reference_file(node, new_file) {
	def fragment = node.link.uri ? node.link.uri.fragment : null
	String old_file = node.link.file ? node.link.file.toString() : "null"
	log.info("changing referenced_file: "+node+"\n"+old_file+"\n"+new_file.toString())

	def base_path = node.map.file.parentFile.toURI()
	def new_path = new_file.toURI()
	def relative_path = base_path.relativize(new_path).toString()
	if (fragment!= null) {
		relative_path = relative_path + "#" + fragment
	}
	node.link.text = relative_path

}

public def obtain_folders(folder_node) {
	def parent_folder = find_folder_back_in_hierarchy(folder_node)
	def needed_folder = java.nio.file.Paths.get(parent_folder.toString(), folder_node.text).toFile()
	needed_folder.mkdirs()
	return needed_folder
}

}


