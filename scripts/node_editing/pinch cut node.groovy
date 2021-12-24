if (node.parent) {
    def position = node.parent.getChildPosition(node)
    node.children.each {
        it.moveTo(node.parent, position++)
    }
    node.delete()
}