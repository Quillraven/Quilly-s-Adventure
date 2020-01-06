allprojects {
    version = Apps.versionName

    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        jcenter()
        google()
    }
}
