def old_children = node.children.clone()def new_node = node.createChild()old_children.each {
	it.moveTo(new_node)
}c.select(new_node)