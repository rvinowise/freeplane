//// @ExecutionModes({ON_SINGLE_NODE})

add_reciprocal_link()

def add_reciprocal_link() {
	
	def selecteds = c.getSelecteds()
	def main_node = selecteds[0]
	for (int i=1;i<c.selecteds.size();i++) {
		def node = c.selecteds[i]
		main_node.link.node = node
		node.link.node = main_node
	}
}