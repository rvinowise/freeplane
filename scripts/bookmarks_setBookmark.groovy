evaluate(new File("../tools/node_manipulation.groovy"))


bookmark_and_mark(c.getSelected());


def bookmark_and_mark(node) {
	bookmark_node = create_bookmark(node);
	//node.setFolded(true);
	mark_node(bookmark_node.parent, node);
}

def mark_node(src_node, dst_node) {
	//node.style.setBackgroundColorCode('#cccccc');
	dst_node.style.setName(src_node.style.getName());
	dst_node.icons.addAll(src_node.icons);
}

def create_bookmark(node) {
	def bookmarksNode = obtain_node('bookmarks: done', node.mindMap.root)
	def recentBookmarkNode = bookmarksNode.createChild(node.text)

	recentBookmarkNode.link.node = node
	return recentBookmarkNode;
}



