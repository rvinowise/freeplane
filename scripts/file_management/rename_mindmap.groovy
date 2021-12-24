

rename_mindmap(node,c);

def rename_mindmap(node,c) {
	
	def mindmap_file_name = node.link.file
	
    def mindmap_file = new File(
        mindmap_file_name
    )

	c.statusInfo = mindmap_file
}