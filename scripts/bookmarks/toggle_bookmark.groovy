import com.rvinowise.freeplane.*

nm = new NodeManipulator(c, node);
//def bookmarks_root = nm.obtain_node('bookmarks: done', node.mindMap.root)
def bookmarks_root = get_active_bookmark_root()

toggle_bookmark(bookmarks_root, c.getSelecteds())


def get_active_bookmark_root() {
	def selector_node = nm.obtain_node('active bookmarks', node.mindMap.root)
	return selector_node.children[0]
}

def toggle_bookmark(bookmarks_root, nodes) {
	nodes.each{ node ->
		if (has_bookmark(bookmarks_root, node)) {
			remove_bookmark(bookmarks_root, node)
		} else {
			add_bookmark(bookmarks_root, node)
		}
	}
}

def has_bookmark(bookmarks_root, node) {
	for (icon in bookmarks_root.icons) {
		if (!node.icons.contains(icon)) {
			return false;
		}
	}
	return true
	
}

def add_bookmark(bookmarks_root, node) {
	bookmark_node = create_bookmark(bookmarks_root, node);
	//node.setFolded(true);
	mark_node(bookmarks_root, node);
}

def remove_bookmark(bookmarks_root, node) {
	def bookmark_node = get_bookmark_for(bookmarks_root, node)
	bookmark_node.delete()

	unmark_node(bookmarks_root, node)
}

def get_bookmark_for(bookmarks_root, node) {
	for (bookmark in bookmarks_root.children) {
		if (bookmark.link.node == node) {
			return bookmark
		}
	}
	return null
}

def mark_node(src_node, dst_node) {
	if (src_node.style.backgroundColorCode != "#ffffff") {
		dst_node.style.backgroundColor = src_node.style.backgroundColor;
	}
	dst_node.icons.addAll(src_node.icons);
}

def unmark_node(src_node, dst_node) {
	if (dst_node.style.backgroundColor == src_node.style.backgroundColor) {
		dst_node.style.backgroundColorCode = "#ffffff"
	}
	src_node.icons.each { icon ->
		dst_node.icons.remove(icon)
	}
}

def create_bookmark(bookmarks_root, node) {
	def new_bookmark = bookmarks_root.createChild(node.text)

	new_bookmark.link.node = node
	return new_bookmark;
}



