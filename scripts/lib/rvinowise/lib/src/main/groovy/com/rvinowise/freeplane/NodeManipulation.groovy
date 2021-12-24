package com.rvinowise.freeplane

import groovy.util.logging.Log
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatterimport java.util.logging.Level

//import org.freeplane.features.nodestyle.NodeStyleModel

@Log
class NodeManipulator {

def c = null
def node  = null

NodeManipulator() {
	
}

NodeManipulator(_c, _node) {
	init(_c, _node)
}

def init(in_c, in_node) {
    this.c = in_c
    this.node = in_node
    def user_dir = c.getUserDirectory()
    FileHandler handler = new FileHandler("$user_dir/logs/node_manipulator.log", true);
	handler.setFormatter(new SimpleFormatter())
	log.addHandler(handler)	log.setLevel(Level.OFF)
}

def obtain_node(String nodeTitle, parentNode) {
    def matches = []
    parentNode.children.each{ n -> 
        if (n.text.equals(nodeTitle)) {
            matches.add(n)    
        }
    }
    return matches ? matches.getAt(0) : parentNode.createChild(nodeTitle)
}

def get_farthest_parent_of_one(node, recursive=false) {
    if (node.parent.getChildren().size() == 1) {
        return get_farthest_parent_of_one(node.parent, true)
    }
    return node
}

def compare_node_text(a,b) {
    return a.text <=> b.text
}

def intersect(
    array1,
    array2,
    comparator
) {
    def result = []
    for (item1 in array1) {
        for (item2 in array2) {
            if (comparator(item1,item2) == 0) {
                result.add(item1)
            }
        }
    }
    return result
}

def testlol() {
    return obtain_node("type:", c.getSelected().mindMap.root)
}

def find_back_in_hierarchy(node, condition, final_parent=node.mindMap.root) {
    if (node == final_parent) {
        return null
    }
    if (condition(node)) {
        return node
    }
    return find_back_in_hierarchy(node.parent, condition, final_parent)
}

def find_text_in_parents(node, text, final_parent=node.mindMap.root) {
    if (node == final_parent) {
        return null
    }
    if (node.text == text) {
        return node
    }
    return find_text_in_parents(node.parent, text, final_parent)
}

def copy_style(src_node, dst_node) {
	dst_node.style.backgroundColor = src_node.style.backgroundColor;
	node.style.textColor = src_node.style.textColor
	dst_node.icons.addAll(src_node.icons);
	
	// NodeStyleModel.setShape(
		// dst_node.delegate, 
		// NodeStyleModel.getShape(src_node.delegate)
	// )
	
}

def apply_local_style(style_hub_name, style_name, node) {
	def styles_hub = obtain_node(
		style_hub_name, node.mindMap.root
	)
	def style_node = obtain_node(
		style_name, styles_hub
	)
	copy_style(style_node, node)
}

}

//nm = new NodeManipulator();