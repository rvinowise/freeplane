import groovy.util.logging.Log
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter

@Log
class NodeManipulator {

def c = null
def node  = null

def init(in_c, in_node) {
    this.c = in_c
    this.node = in_node
    def user_dir = c.getUserDirectory()
    FileHandler handler = new FileHandler("$user_dir/poetry_logs/poetry_nm.log", true);
	handler.setFormatter(new SimpleFormatter())
	log.addHandler(handler)
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

def find_in_parents(node, condition, final_parent=node.mindMap.root) {
    if (node == final_parent) {
        return null
    }
    if (condition(node)) {
        return node
    }
    return find_in_parents(node.parent, condition, final_parent)
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

}

nm = new NodeManipulator();