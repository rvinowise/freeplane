def bookmarkNodes = c.find { it.text == 'Bookmarks' }
if (bookmarkNodes) {
    def children = bookmarkNodes.get(0).children
    if (children) {
        c.select(children.get(0))
    }
}