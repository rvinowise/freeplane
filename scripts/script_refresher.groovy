

delete_compiled_classes();

def delete_compiled_classes() {
    // def dir = new File(
    //     c.getUserDirectory().getName()+'/1.9.x/compiledscripts'
    // )
    // def dir2 = new File(
    //     c.getUserDirectory().getName()+'/1.9.x/compiledscripts2'
    // )

    def dir = new File(
        'D:\\archive\\cloud_storages\\google\\interface\\freeplane_user\\1.9.x\\compiledscripts'
    )
    def dir2 = new File(
        'D:\\archive\\cloud_storages\\google\\interface\\freeplane_user\\1.9.x\\compiledscripts2'
    )


    dir.deleteDir()
    dir2.deleteDir()
}